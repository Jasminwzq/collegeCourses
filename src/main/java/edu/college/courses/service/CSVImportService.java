package edu.college.courses.service;

import edu.college.courses.dao.CourseDAO;
import edu.college.courses.dao.PrerequisiteDAO;
import edu.college.courses.model.Course;
import edu.college.courses.model.Prerequisite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for importing course data from CSV files
 */
public class CSVImportService {
    private static final Logger logger = LoggerFactory.getLogger(CSVImportService.class);
    
    private final CourseDAO courseDAO;
    private final PrerequisiteDAO prerequisiteDAO;
    
    public CSVImportService() {
        this.courseDAO = new CourseDAO();
        this.prerequisiteDAO = new PrerequisiteDAO();
    }

    /**
     * Import courses and prerequisites from CSV file
     * Expected CSV format: CourseName,PreqCourseName,CreditHours,Major1/Major2/GenedEdu/Minor,Description
     */
    public ImportResult importFromCSV(String filePath) throws IOException, SQLException {
        logger.info("Starting CSV import from: {}", filePath);
        
        List<String> errors = new ArrayList<>();
        int coursesImported = 0;
        int prerequisitesImported = 0;
        
        // Map to store course names to IDs for prerequisite relationships
        Map<String, Integer> courseNameToId = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    String[] fields = parseCSVLine(line);
                    if (fields.length < 5) {
                        errors.add("Line " + lineNumber + ": Insufficient fields");
                        continue;
                    }
                    
                    String courseName = fields[0].trim();
                    String prerequisiteName = fields[1].trim();
                    int creditHours = Integer.parseInt(fields[2].trim());
                    String majorType = fields[3].trim();
                    String description = fields[4].trim();
                    
                    // Import course if not already imported
                    if (!courseNameToId.containsKey(courseName)) {
                        Course course = new Course();
                        course.setCourseName(courseName);
                        course.setCreditHours(creditHours);
                        course.setMajorType(parseMajorType(majorType));
                        course.setDescription(description);
                        
                        int courseId = courseDAO.insertCourse(course);
                        courseNameToId.put(courseName, courseId);
                        coursesImported++;
                        logger.debug("Imported course: {} with ID: {}", courseName, courseId);
                    }
                    
                    // Import prerequisite if specified and not empty
                    if (!prerequisiteName.isEmpty() && !prerequisiteName.equals(courseName)) {
                        // Check if prerequisite course exists, if not create it
                        if (!courseNameToId.containsKey(prerequisiteName)) {
                            Course prerequisiteCourse = new Course();
                            prerequisiteCourse.setCourseName(prerequisiteName);
                            prerequisiteCourse.setCreditHours(0); // Default value
                            prerequisiteCourse.setMajorType(Course.MajorType.Major1); // Default value
                            prerequisiteCourse.setDescription("Imported as prerequisite");
                            
                            int prereqId = courseDAO.insertCourse(prerequisiteCourse);
                            courseNameToId.put(prerequisiteName, prereqId);
                            coursesImported++;
                            logger.debug("Imported prerequisite course: {} with ID: {}", prerequisiteName, prereqId);
                        }
                        
                        // Create prerequisite relationship
                        int courseId = courseNameToId.get(courseName);
                        int prerequisiteId = courseNameToId.get(prerequisiteName);
                        
                        // Check if relationship already exists
                        if (!prerequisiteDAO.prerequisiteExists(courseId, prerequisiteId)) {
                            Prerequisite prerequisite = new Prerequisite();
                            prerequisite.setCourseId(courseId);
                            prerequisite.setPrerequisiteCourseId(prerequisiteId);
                            prerequisite.setRequired(true);
                            
                            prerequisiteDAO.insertPrerequisite(prerequisite);
                            prerequisitesImported++;
                            logger.debug("Created prerequisite: {} -> {}", prerequisiteName, courseName);
                        }
                    }
                    
                } catch (Exception e) {
                    String error = "Line " + lineNumber + ": " + e.getMessage();
                    errors.add(error);
                    logger.warn("Error processing line {}: {}", lineNumber, e.getMessage());
                }
            }
        }
        
        logger.info("CSV import completed. Courses: {}, Prerequisites: {}, Errors: {}", 
                   coursesImported, prerequisitesImported, errors.size());
        
        return new ImportResult(coursesImported, prerequisitesImported, errors);
    }

    /**
     * Parse a CSV line handling quoted fields and commas
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }

    /**
     * Parse major type from string
     */
    private Course.MajorType parseMajorType(String majorType) {
        switch (majorType.toUpperCase()) {
            case "M1":
            case "MAJOR1":
                return Course.MajorType.Major1;
            case "M2":
            case "MAJOR2":
                return Course.MajorType.Major2;
            case "GE":
            case "GENERALEDUCATION":
                return Course.MajorType.GeneralEducation;
            case "M":
            case "MINOR":
                return Course.MajorType.Minor;
            default:
                return Course.MajorType.Major1; // Default
        }
    }

    /**
     * Result class for import operations
     */
    public static class ImportResult {
        private final int coursesImported;
        private final int prerequisitesImported;
        private final List<String> errors;

        public ImportResult(int coursesImported, int prerequisitesImported, List<String> errors) {
            this.coursesImported = coursesImported;
            this.prerequisitesImported = prerequisitesImported;
            this.errors = errors;
        }

        public int getCoursesImported() {
            return coursesImported;
        }

        public int getPrerequisitesImported() {
            return prerequisitesImported;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("ImportResult{courses=%d, prerequisites=%d, errors=%d}", 
                               coursesImported, prerequisitesImported, errors.size());
        }
    }
}
