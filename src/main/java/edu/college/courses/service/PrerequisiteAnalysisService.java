package edu.college.courses.service;

import edu.college.courses.dao.CourseDAO;
import edu.college.courses.dao.PrerequisiteDAO;
import edu.college.courses.model.Course;
import edu.college.courses.model.Prerequisite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing prerequisite relationships
 */
public class PrerequisiteAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(PrerequisiteAnalysisService.class);
    
    private final CourseDAO courseDAO;
    private final PrerequisiteDAO prerequisiteDAO;
    
    public PrerequisiteAnalysisService() {
        this.courseDAO = new CourseDAO();
        this.prerequisiteDAO = new PrerequisiteDAO();
    }

    /**
     * Get all prerequisite relationships
     */
    public List<Prerequisite> getAllPrerequisiteRelationships() throws SQLException {
        return prerequisiteDAO.getAllPrerequisitesWithDetails();
    }

    /**
     * Get prerequisites for a specific course
     */
    public List<Prerequisite> getPrerequisitesForCourse(String courseName) throws SQLException {
        Optional<Course> course = courseDAO.findByName(courseName);
        if (course.isEmpty()) {
            logger.warn("Course not found: {}", courseName);
            return Collections.emptyList();
        }
        
        return prerequisiteDAO.getPrerequisitesForCourse(course.get().getCourseId());
    }

    /**
     * Get all courses that require a specific prerequisite
     */
    public List<Prerequisite> getCoursesRequiringPrerequisite(String prerequisiteName) throws SQLException {
        Optional<Course> prerequisite = courseDAO.findByName(prerequisiteName);
        if (prerequisite.isEmpty()) {
            logger.warn("Prerequisite course not found: {}", prerequisiteName);
            return Collections.emptyList();
        }
        
        return prerequisiteDAO.getCoursesRequiringPrerequisite(prerequisite.get().getCourseId());
    }

    /**
     * Find prerequisite chains (courses that have prerequisites of prerequisites)
     */
    public List<List<String>> findPrerequisiteChains(String courseName) throws SQLException {
        List<List<String>> chains = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        findPrerequisiteChainsRecursive(courseName, new ArrayList<>(), chains, visited);
        
        return chains;
    }

    /**
     * Recursive helper for finding prerequisite chains
     */
    private void findPrerequisiteChainsRecursive(String courseName, List<String> currentChain, 
                                               List<List<String>> allChains, Set<String> visited) throws SQLException {
        if (visited.contains(courseName)) {
            // Circular dependency detected
            return;
        }
        
        visited.add(courseName);
        currentChain.add(courseName);
        
        List<Prerequisite> prerequisites = getPrerequisitesForCourse(courseName);
        
        if (prerequisites.isEmpty()) {
            // End of chain
            allChains.add(new ArrayList<>(currentChain));
        } else {
            for (Prerequisite prereq : prerequisites) {
                String prereqName = prereq.getPrerequisiteCourse().getCourseName();
                findPrerequisiteChainsRecursive(prereqName, new ArrayList<>(currentChain), allChains, new HashSet<>(visited));
            }
        }
    }

    /**
     * Find courses with no prerequisites
     */
    public List<Course> findCoursesWithNoPrerequisites() throws SQLException {
        List<Course> allCourses = courseDAO.findAll();
        List<Course> coursesWithNoPrereqs = new ArrayList<>();
        
        for (Course course : allCourses) {
            List<Prerequisite> prerequisites = prerequisiteDAO.getPrerequisitesForCourse(course.getCourseId());
            if (prerequisites.isEmpty()) {
                coursesWithNoPrereqs.add(course);
            }
        }
        
        return coursesWithNoPrereqs;
    }

    /**
     * Find courses that are prerequisites for many other courses
     */
    public List<Course> findPopularPrerequisites(int minCount) throws SQLException {
        List<Prerequisite> allPrerequisites = prerequisiteDAO.getAllPrerequisitesWithDetails();
        
        Map<String, Integer> prerequisiteCounts = new HashMap<>();
        
        for (Prerequisite prereq : allPrerequisites) {
            String prereqName = prereq.getPrerequisiteCourse().getCourseName();
            prerequisiteCounts.put(prereqName, prerequisiteCounts.getOrDefault(prereqName, 0) + 1);
        }
        
        return prerequisiteCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= minCount)
                .map(entry -> {
                    try {
                        return courseDAO.findByName(entry.getKey());
                    } catch (SQLException e) {
                        logger.error("Error finding course: {}", entry.getKey(), e);
                        return Optional.<Course>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Generate prerequisite report
     */
    public String generatePrerequisiteReport() throws SQLException {
        StringBuilder report = new StringBuilder();
        
        report.append("=== PREREQUISITE RELATIONSHIP REPORT ===\n\n");
        
        // Get all relationships
        List<Prerequisite> allPrerequisites = getAllPrerequisiteRelationships();
        
        if (allPrerequisites.isEmpty()) {
            report.append("No prerequisite relationships found.\n");
            return report.toString();
        }
        
        // Group by course
        Map<String, List<Prerequisite>> prerequisitesByCourse = allPrerequisites.stream()
                .collect(Collectors.groupingBy(p -> p.getCourse().getCourseName()));
        
        // Report by course
        report.append("PREREQUISITES BY COURSE:\n");
        report.append("=======================\n");
        
        for (Map.Entry<String, List<Prerequisite>> entry : prerequisitesByCourse.entrySet()) {
            String courseName = entry.getKey();
            List<Prerequisite> prerequisites = entry.getValue();
            
            report.append(String.format("\n%s:\n", courseName));
            for (Prerequisite prereq : prerequisites) {
                report.append(String.format("  - %s (%d credits, %s)\n", 
                    prereq.getPrerequisiteCourse().getCourseName(),
                    prereq.getPrerequisiteCourse().getCreditHours(),
                    prereq.getPrerequisiteCourse().getMajorType().getValue()));
            }
        }
        
        // Report by prerequisite
        report.append("\n\nCOURSES BY PREREQUISITE:\n");
        report.append("=======================\n");
        
        Map<String, List<Prerequisite>> coursesByPrerequisite = allPrerequisites.stream()
                .collect(Collectors.groupingBy(p -> p.getPrerequisiteCourse().getCourseName()));
        
        for (Map.Entry<String, List<Prerequisite>> entry : coursesByPrerequisite.entrySet()) {
            String prereqName = entry.getKey();
            List<Prerequisite> courses = entry.getValue();
            
            report.append(String.format("\n%s is a prerequisite for:\n", prereqName));
            for (Prerequisite prereq : courses) {
                report.append(String.format("  - %s (%d credits, %s)\n", 
                    prereq.getCourse().getCourseName(),
                    prereq.getCourse().getCreditHours(),
                    prereq.getCourse().getMajorType().getValue()));
            }
        }
        
        // Statistics
        report.append("\n\nSTATISTICS:\n");
        report.append("===========\n");
        report.append(String.format("Total prerequisite relationships: %d\n", allPrerequisites.size()));
        report.append(String.format("Courses with prerequisites: %d\n", prerequisitesByCourse.size()));
        report.append(String.format("Courses that are prerequisites: %d\n", coursesByPrerequisite.size()));
        
        // Popular prerequisites
        List<Course> popularPrereqs = findPopularPrerequisites(2);
        if (!popularPrereqs.isEmpty()) {
            report.append("\nPOPULAR PREREQUISITES (required by 2+ courses):\n");
            for (Course course : popularPrereqs) {
                int count = coursesByPrerequisite.get(course.getCourseName()).size();
                report.append(String.format("  - %s (required by %d courses)\n", course.getCourseName(), count));
            }
        }
        
        return report.toString();
    }

    /**
     * Check for circular dependencies
     */
    public List<String> findCircularDependencies() throws SQLException {
        List<String> circularDeps = new ArrayList<>();
        List<Prerequisite> allPrerequisites = getAllPrerequisiteRelationships();
        
        // Build adjacency list
        Map<String, List<String>> graph = new HashMap<>();
        for (Prerequisite prereq : allPrerequisites) {
            String course = prereq.getCourse().getCourseName();
            String prereqCourse = prereq.getPrerequisiteCourse().getCourseName();
            
            graph.computeIfAbsent(course, k -> new ArrayList<>()).add(prereqCourse);
        }
        
        // Check for cycles using DFS
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String course : graph.keySet()) {
            if (!visited.contains(course)) {
                if (hasCycle(course, graph, visited, recursionStack, circularDeps)) {
                    // Cycle found
                }
            }
        }
        
        return circularDeps;
    }

    /**
     * DFS helper to detect cycles
     */
    private boolean hasCycle(String course, Map<String, List<String>> graph, 
                           Set<String> visited, Set<String> recursionStack, List<String> circularDeps) {
        visited.add(course);
        recursionStack.add(course);
        
        List<String> prerequisites = graph.get(course);
        if (prerequisites != null) {
            for (String prereq : prerequisites) {
                if (!visited.contains(prereq)) {
                    if (hasCycle(prereq, graph, visited, recursionStack, circularDeps)) {
                        return true;
                    }
                } else if (recursionStack.contains(prereq)) {
                    circularDeps.add(String.format("Circular dependency detected: %s -> %s", course, prereq));
                    return true;
                }
            }
        }
        
        recursionStack.remove(course);
        return false;
    }
}
