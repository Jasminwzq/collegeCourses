-- Database schema for course prerequisites system
CREATE DATABASE IF NOT EXISTS college_courses;
USE college_courses;

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(50) NOT NULL UNIQUE,
    credit_hours INT NOT NULL,
    major_type ENUM('Major1', 'Major2', 'GeneralEducation', 'Minor') NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Prerequisites table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS prerequisites (
    prerequisite_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    prerequisite_course_id INT NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (prerequisite_course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY unique_prerequisite (course_id, prerequisite_course_id)
);


-- Sample data insertion
INSERT INTO courses (course_name, credit_hours, major_type, description) VALUES
('Math1151', 5, 'Major1', 'Calculus II'),
('Math2153', 4, 'Major1', 'Calculus III'),
('Math2182H', 5, 'Major1', 'Honors Calculus II'),
('Math2568', 3, 'Major1', 'Linear Algebra'),
('Math2568H', 3, 'Major1', 'Honors Linear Algebra'),
('Math3345', 3, 'Major1', 'Foundations of Higher Mathematics'),
('Math3345H', 3, 'Major1', 'Honors Foundations of Higher Mathematics'),
('Math4530', 3, 'Major1', 'Probability'),
('Stat4201', 4, 'Major1', 'Intro to Math Stat I'),
('Stat4202', 4, 'Major1', 'Intro to Mathematical Statistics II');

-- Insert prerequisite relationships
INSERT INTO prerequisites (course_id, prerequisite_course_id) VALUES
-- Math 2568 requires Math 2153
((SELECT course_id FROM courses WHERE course_name = 'Math2568'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math2153')),

-- Math 2568H requires Math 2153  
((SELECT course_id FROM courses WHERE course_name = 'Math2568H'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math2153')),

-- Math 3345 requires Math 2153
((SELECT course_id FROM courses WHERE course_name = 'Math3345'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math2153')),

-- Math 3345H requires Math 2153
((SELECT course_id FROM courses WHERE course_name = 'Math3345H'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math2153')),

-- Math 4530 requires Math 2153
((SELECT course_id FROM courses WHERE course_name = 'Math4530'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math2153')),

-- Stat 4201 requires Math 2153
((SELECT course_id FROM courses WHERE course_name = 'Stat4201'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math2153')),

-- Stat 4202 requires Math 4530 OR Stat 4201
((SELECT course_id FROM courses WHERE course_name = 'Stat4202'), 
 (SELECT course_id FROM courses WHERE course_name = 'Math4530')),
((SELECT course_id FROM courses WHERE course_name = 'Stat4202'), 
 (SELECT course_id FROM courses WHERE course_name = 'Stat4201'));

