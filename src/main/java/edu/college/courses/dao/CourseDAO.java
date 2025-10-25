package edu.college.courses.dao;

import edu.college.courses.config.DatabaseConfig;
import edu.college.courses.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Course operations
 */
public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    // SQL queries
    private static final String INSERT_COURSE = 
        "INSERT INTO courses (course_name, credit_hours, major_type, description) VALUES (?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM courses WHERE course_id = ?";
    
    private static final String SELECT_BY_NAME = 
        "SELECT * FROM courses WHERE course_name = ?";
    
    private static final String SELECT_ALL = 
        "SELECT * FROM courses ORDER BY course_name";
    
    private static final String UPDATE_COURSE = 
        "UPDATE courses SET course_name = ?, credit_hours = ?, major_type = ?, description = ? WHERE course_id = ?";
    
    private static final String DELETE_COURSE = 
        "DELETE FROM courses WHERE course_id = ?";
    
    private static final String SEARCH_COURSES = 
        "SELECT * FROM courses WHERE course_name LIKE ? OR description LIKE ? ORDER BY course_name";

    /**
     * Insert a new course
     */
    public int insertCourse(Course course) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_COURSE, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, course.getCourseName());
            stmt.setInt(2, course.getCreditHours());
            stmt.setString(3, course.getMajorType().getValue());
            stmt.setString(4, course.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int courseId = generatedKeys.getInt(1);
                        course.setCourseId(courseId);
                        logger.info("Course inserted with ID: {}", courseId);
                        return courseId;
                    }
                }
            }
            
            throw new SQLException("Failed to insert course");
        }
    }

    /**
     * Find course by ID
     */
    public Optional<Course> findById(int courseId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find course by name
     */
    public Optional<Course> findByName(String courseName) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NAME)) {
            
            stmt.setString(1, courseName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCourse(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get all courses
     */
    public List<Course> findAll() throws SQLException {
        List<Course> courses = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }
        
        return courses;
    }

    /**
     * Update course
     */
    public boolean updateCourse(Course course) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_COURSE)) {
            
            stmt.setString(1, course.getCourseName());
            stmt.setInt(2, course.getCreditHours());
            stmt.setString(3, course.getMajorType().getValue());
            stmt.setString(4, course.getDescription());
            stmt.setInt(5, course.getCourseId());
            
            int affectedRows = stmt.executeUpdate();
            logger.info("Course updated: {} rows affected", affectedRows);
            return affectedRows > 0;
        }
    }

    /**
     * Delete course
     */
    public boolean deleteCourse(int courseId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_COURSE)) {
            
            stmt.setInt(1, courseId);
            
            int affectedRows = stmt.executeUpdate();
            logger.info("Course deleted: {} rows affected", affectedRows);
            return affectedRows > 0;
        }
    }

    /**
     * Search courses by name or description
     */
    public List<Course> searchCourses(String searchTerm) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_COURSES)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }
        
        return courses;
    }

    /**
     * Map ResultSet to Course object
     */
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseName(rs.getString("course_name"));
        course.setCreditHours(rs.getInt("credit_hours"));
        course.setMajorType(Course.MajorType.fromString(rs.getString("major_type")));
        course.setDescription(rs.getString("description"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            course.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            course.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return course;
    }
}
