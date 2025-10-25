package edu.college.courses;

import edu.college.courses.config.DatabaseConfig;
import edu.college.courses.service.CSVImportService;
import edu.college.courses.service.PrerequisiteAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main application class for course prerequisite management
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting Course Prerequisite Management Application");
        
        try {
            // Test database connection
            if (!DatabaseConfig.testConnection()) {
                logger.error("Database connection failed. Please check your MySQL configuration.");
                System.exit(1);
            }
            logger.info("Database connection successful");
            
            // Initialize services
            CSVImportService csvImportService = new CSVImportService();
            PrerequisiteAnalysisService analysisService = new PrerequisiteAnalysisService();
            
            // Example usage
            demonstrateUsage(csvImportService, analysisService);
            
        } catch (Exception e) {
            logger.error("Application error", e);
        } finally {
            // Clean up
            DatabaseConfig.closeDataSource();
            logger.info("Application shutdown complete");
        }
    }

    /**
     * Demonstrate the application functionality
     */
    private static void demonstrateUsage(CSVImportService csvImportService, 
                                       PrerequisiteAnalysisService analysisService) {
        try {
            // Example 1: Import from CSV
            logger.info("=== CSV Import Example ===");
            String csvFilePath = "src/main/resources/sample-courses.csv";
            
            try {
                CSVImportService.ImportResult result = csvImportService.importFromCSV(csvFilePath);
                logger.info("Import completed: {}", result);
                
                if (result.hasErrors()) {
                    logger.warn("Import had {} errors:", result.getErrors().size());
                    for (String error : result.getErrors()) {
                        logger.warn("  - {}", error);
                    }
                }
            } catch (IOException e) {
                logger.warn("CSV file not found, using existing data: {}", e.getMessage());
            }
            
            // Example 2: Analyze prerequisite relationships
            logger.info("\n=== Prerequisite Analysis ===");
            
            // Get all relationships
            var allRelationships = analysisService.getAllPrerequisiteRelationships();
            logger.info("Found {} prerequisite relationships", allRelationships.size());
            
            // Generate comprehensive report
            String report = analysisService.generatePrerequisiteReport();
            logger.info("Prerequisite Report:\n{}", report);
            
            // Check for circular dependencies
            var circularDeps = analysisService.findCircularDependencies();
            if (!circularDeps.isEmpty()) {
                logger.warn("Circular dependencies found:");
                for (String dep : circularDeps) {
                    logger.warn("  - {}", dep);
                }
            } else {
                logger.info("No circular dependencies found");
            }
            
            // Find popular prerequisites
            var popularPrereqs = analysisService.findPopularPrerequisites(2);
            if (!popularPrereqs.isEmpty()) {
                logger.info("Popular prerequisites (required by 2+ courses):");
                for (var course : popularPrereqs) {
                    logger.info("  - {}", course.getCourseName());
                }
            }
            
            // Find courses with no prerequisites
            var noPrereqCourses = analysisService.findCoursesWithNoPrerequisites();
            logger.info("Courses with no prerequisites: {}", noPrereqCourses.size());
            for (var course : noPrereqCourses) {
                logger.info("  - {}", course.getCourseName());
            }
            
        } catch (SQLException e) {
            logger.error("Database error during analysis", e);
        }
    }
}
