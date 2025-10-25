package edu.college.courses.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a course in the college system
 */
public class Course {
    private int courseId;
    private String courseName;
    private int creditHours;
    private MajorType majorType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum MajorType {
        Major1("Major1"),
        Major2("Major2"),
        GeneralEducation("GeneralEducation"),
        Minor("Minor");

        private final String value;

        MajorType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static MajorType fromString(String value) {
            for (MajorType type : MajorType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid major type: " + value);
        }
    }

    // Constructors
    public Course() {}

    public Course(String courseName, int creditHours, MajorType majorType, String description) {
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.majorType = majorType;
        this.description = description;
    }

    public Course(int courseId, String courseName, int creditHours, MajorType majorType, 
                  String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.majorType = majorType;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public MajorType getMajorType() {
        return majorType;
    }

    public void setMajorType(MajorType majorType) {
        this.majorType = majorType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseId == course.courseId && Objects.equals(courseName, course.courseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, courseName);
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", creditHours=" + creditHours +
                ", majorType=" + majorType +
                ", description='" + description + '\'' +
                '}';
    }
}

