package edu.college.courses.dao;

import edu.college.courses.config.DatabaseConfig;
import edu.college.courses.model.Course;
import edu.college.courses.model.Prerequisite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Prerequisite operations
 */
public class PrerequisiteDAO {
    private static final Logger logger = LoggerFactory.getLogger(PrerequisiteDAO.class);

    // SQL queries
    private static final String INSERT_PREREQUISITE = 
        "INSERT INTO prerequisites (course_id, prerequisite_course_id, is_required) VALUES (?, ?, ?)";
    
    private static final String SELECT_PREREQUISITES_FOR_COURSE = 
        "SELECT p.*, c.course_name, c.credit_hours, c.major_type, c.description " +
        "FROM prerequisites p " +
        "JOIN courses c ON p.prerequisite_course_id = c.course_id " +
        "WHERE p.course_id = ?";
    
    private static final String SELECT_COURSES_REQUIRING_PREREQUISITE = 
        "SELECT p.*, c.course_name, c.credit_hours, c.major_type, c.description " +
        "FROM prerequisites p " +
        "JOIN courses c ON p.course_id = c.course_id " +
        "WHERE p.prerequisite_course_id = ?";
    
    private static final String SELECT_ALL_PREREQUISITES = 
        "SELECT p.*, c1.course_name, c1.credit_hours, c1.major_type, c1.description as course_desc, " +
        "c2.course_name as prereq_name, c2.credit_hours as prereq_credits, c2.major_type as prereq_major, " +
        "c2.description as prereq_desc " +
        "FROM prerequisites p " +
        "JOIN courses c1 ON p.course_id = c1.course_id " +
        "JOIN courses c2 ON p.prerequisite_course_id = c2.course_id " +
        "ORDER BY c1.course_name, c2.course_name";
    
    private static final String DELETE_PREREQUISITE = 
        "DELETE FROM prerequisites WHERE prerequisite_id = ?";
    
    private static final String DELETE_PREREQUISITES_FOR_COURSE = 
        "DELETE FROM prerequisites WHERE course_id = ?";
    
    private static final String CHECK_PREREQUISITE_EXISTS = 
        "SELECT COUNT(*) FROM prerequisites WHERE course_id = ? AND prerequisite_course_id = ?";

    /**
     * Insert a new prerequisite relationship
     */
    public int insertPrerequisite(Prerequisite prerequisite) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PREREQUISITE, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, prerequisite.getCourseId());
            stmt.setInt(2, prerequisite.getPrerequisiteCourseId());
            stmt.setBoolean(3, prerequisite.isRequired());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int prerequisiteId = generatedKeys.getInt(1);
                        prerequisite.setPrerequisiteId(prerequisiteId);
                        logger.info("Prerequisite inserted with ID: {}", prerequisiteId);
                        return prerequisiteId;
                    }
                }
            }
            
            throw new SQLException("Failed to insert prerequisite");
        }
    }

    /**
     * Get all prerequisites for a specific course
     */
    public List<Prerequisite> getPrerequisitesForCourse(int courseId) throws SQLException {
        List<Prerequisite> prerequisites = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PREREQUISITES_FOR_COURSE)) {
            
            stmt.setInt(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prerequisites.add(mapResultSetToPrerequisite(rs));
                }
            }
        }
        
        return prerequisites;
    }

    /**
     * Get all courses that require a specific prerequisite
     */
    public List<Prerequisite> getCoursesRequiringPrerequisite(int prerequisiteCourseId) throws SQLException {
        List<Prerequisite> prerequisites = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_COURSES_REQUIRING_PREREQUISITE)) {
            
            stmt.setInt(1, prerequisiteCourseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prerequisites.add(mapResultSetToPrerequisite(rs));
                }
            }
        }
        
        return prerequisites;
    }

    /**
     * Get all prerequisite relationships with full course details
     */
    public List<Prerequisite> getAllPrerequisitesWithDetails() throws SQLException {
        List<Prerequisite> prerequisites = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_PREREQUISITES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prerequisites.add(mapResultSetToPrerequisiteWithDetails(rs));
            }
        }
        
        return prerequisites;
    }

    /**
     * Check if a prerequisite relationship already exists
     */
    public boolean prerequisiteExists(int courseId, int prerequisiteCourseId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_PREREQUISITE_EXISTS)) {
            
            stmt.setInt(1, courseId);
            stmt.setInt(2, prerequisiteCourseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Delete a specific prerequisite
     */
    public boolean deletePrerequisite(int prerequisiteId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PREREQUISITE)) {
            
            stmt.setInt(1, prerequisiteId);
            
            int affectedRows = stmt.executeUpdate();
            logger.info("Prerequisite deleted: {} rows affected", affectedRows);
            return affectedRows > 0;
        }
    }

    /**
     * Delete all prerequisites for a course
     */
    public boolean deletePrerequisitesForCourse(int courseId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PREREQUISITES_FOR_COURSE)) {
            
            stmt.setInt(1, courseId);
            
            int affectedRows = stmt.executeUpdate();
            logger.info("Prerequisites deleted for course {}: {} rows affected", courseId, affectedRows);
            return affectedRows > 0;
        }
    }

    /**
     * Map ResultSet to Prerequisite object (basic)
     */
    private Prerequisite mapResultSetToPrerequisite(ResultSet rs) throws SQLException {
        Prerequisite prerequisite = new Prerequisite();
        prerequisite.setPrerequisiteId(rs.getInt("prerequisite_id"));
        prerequisite.setCourseId(rs.getInt("course_id"));
        prerequisite.setPrerequisiteCourseId(rs.getInt("prerequisite_course_id"));
        prerequisite.setRequired(rs.getBoolean("is_required"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            prerequisite.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Set prerequisite course details
        Course prerequisiteCourse = new Course();
        prerequisiteCourse.setCourseName(rs.getString("course_name"));
        prerequisiteCourse.setCreditHours(rs.getInt("credit_hours"));
        prerequisiteCourse.setMajorType(Course.MajorType.fromString(rs.getString("major_type")));
        prerequisiteCourse.setDescription(rs.getString("description"));
        prerequisite.setPrerequisiteCourse(prerequisiteCourse);
        
        return prerequisite;
    }

    /**
     * Map ResultSet to Prerequisite object with full course details
     */
    private Prerequisite mapResultSetToPrerequisiteWithDetails(ResultSet rs) throws SQLException {
        Prerequisite prerequisite = new Prerequisite();
        prerequisite.setPrerequisiteId(rs.getInt("prerequisite_id"));
        prerequisite.setCourseId(rs.getInt("course_id"));
        prerequisite.setPrerequisiteCourseId(rs.getInt("prerequisite_course_id"));
        prerequisite.setRequired(rs.getBoolean("is_required"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            prerequisite.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Set main course details
        Course course = new Course();
        course.setCourseName(rs.getString("course_name"));
        course.setCreditHours(rs.getInt("credit_hours"));
        course.setMajorType(Course.MajorType.fromString(rs.getString("major_type")));
        course.setDescription(rs.getString("course_desc"));
        prerequisite.setCourse(course);
        
        // Set prerequisite course details
        Course prerequisiteCourse = new Course();
        prerequisiteCourse.setCourseName(rs.getString("prereq_name"));
        prerequisiteCourse.setCreditHours(rs.getInt("prereq_credits"));
        prerequisiteCourse.setMajorType(Course.MajorType.fromString(rs.getString("prereq_major")));
        prerequisiteCourse.setDescription(rs.getString("prereq_desc"));
        prerequisite.setPrerequisiteCourse(prerequisiteCourse);
        
        return prerequisite;
    }
}
