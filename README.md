# Course Prerequisites Management System

A Java application for managing and analyzing course prerequisite relationships using MySQL database.

## Features

- **CSV Import**: Import course data and prerequisite relationships from CSV files
- **Prerequisite Analysis**: Analyze prerequisite chains, circular dependencies, and popular prerequisites
- **Database Management**: Full CRUD operations for courses and prerequisites
- **Relationship Queries**: Find prerequisites for courses and courses requiring specific prerequisites

## Project Structure

```
src/main/java/edu/college/courses/
├── Application.java                    # Main application class
├── config/
│   └── DatabaseConfig.java            # Database connection configuration
├── dao/
│   ├── CourseDAO.java                 # Course data access operations
│   └── PrerequisiteDAO.java           # Prerequisite data access operations
├── model/
│   ├── Course.java                    # Course entity
│   └── Prerequisite.java              # Prerequisite entity
└── service/
    ├── CSVImportService.java          # CSV import functionality
    └── PrerequisiteAnalysisService.java # Prerequisite analysis
```

## Database Schema

The application uses two main tables:

### Courses Table
- `course_id` (Primary Key)
- `course_name` (Unique)
- `credit_hours`
- `major_type` (Major1/Major2/GeneralEducation/Minor)
- `description`
- `created_at`, `updated_at`

### Prerequisites Table
- `prerequisite_id` (Primary Key)
- `course_id` (Foreign Key to courses)
- `prerequisite_course_id` (Foreign Key to courses)
- `is_required` (Boolean)
- `created_at`

## Setup Instructions

### 1. Database Setup

1. Install MySQL 8.0+
2. Create a database user with appropriate permissions
3. Run the SQL schema file:
   ```bash
   mysql -u root -p < src/main/resources/database-schema.sql
   ```

### 2. Configuration

Update the database connection settings in `DatabaseConfig.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/college_courses";
private static final String DB_USERNAME = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### 3. Build and Run

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.college.courses.Application"
```

## CSV Format

The CSV import expects the following format:
```
CourseName,PreqCourseName,CreditHours,Major1/Major2/GenedEdu/Minor,Description
```

Example:
```
Math1151,Math1150,5,Major1,Calculus II
Math2153,Math1151,4,Major1,Calculus III
Math2568,Math2153,3,Major1,Linear Algebra
```

## Usage Examples

### Import CSV Data
```java
CSVImportService importService = new CSVImportService();
ImportResult result = importService.importFromCSV("path/to/courses.csv");
```

### Analyze Prerequisites
```java
PrerequisiteAnalysisService analysisService = new PrerequisiteAnalysisService();

// Get all prerequisite relationships
List<Prerequisite> allRelationships = analysisService.getAllPrerequisiteRelationships();

// Find prerequisites for a specific course
List<Prerequisite> prereqs = analysisService.getPrerequisitesForCourse("Math2568");

// Generate comprehensive report
String report = analysisService.generatePrerequisiteReport();

// Check for circular dependencies
List<String> circularDeps = analysisService.findCircularDependencies();
```

### Database Operations
```java
CourseDAO courseDAO = new CourseDAO();
PrerequisiteDAO prerequisiteDAO = new PrerequisiteDAO();

// Create a new course
Course course = new Course("CS101", 3, Course.MajorType.Major1, "Introduction to Programming");
int courseId = courseDAO.insertCourse(course);

// Add prerequisite relationship
Prerequisite prerequisite = new Prerequisite(courseId, prereqCourseId, true);
prerequisiteDAO.insertPrerequisite(prerequisite);
```

## Key Features

### Prerequisite Analysis
- **Chain Analysis**: Find complete prerequisite chains
- **Circular Dependency Detection**: Identify circular prerequisite relationships
- **Popular Prerequisites**: Find courses that are prerequisites for many other courses
- **No-Prerequisite Courses**: Identify entry-level courses

### CSV Import
- **Flexible Format**: Handles quoted fields and commas in descriptions
- **Error Handling**: Reports import errors with line numbers
- **Duplicate Prevention**: Avoids creating duplicate prerequisite relationships
- **Auto-Creation**: Automatically creates prerequisite courses if they don't exist

### Database Features
- **Connection Pooling**: Uses HikariCP for efficient database connections
- **Transaction Safety**: Proper error handling and rollback
- **Performance**: Indexed queries for fast prerequisite lookups
- **Scalability**: Supports large numbers of courses and relationships

## Dependencies

- MySQL Connector/J 8.0.33
- HikariCP 5.0.1 (Connection Pooling)
- SLF4J 2.0.7 (Logging)
- JUnit 5.9.2 (Testing)

## Error Handling

The application includes comprehensive error handling:
- Database connection failures
- SQL constraint violations
- CSV parsing errors
- Circular dependency detection
- Missing prerequisite courses

All errors are logged with appropriate detail levels for debugging and monitoring.
