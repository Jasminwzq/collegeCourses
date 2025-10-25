package edu.college.courses.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a prerequisite relationship between courses
 */
public class Prerequisite {
    private int prerequisiteId;
    private int courseId;
    private int prerequisiteCourseId;
    private boolean isRequired;
    private LocalDateTime createdAt;
    
    // Additional fields for display purposes
    private Course course;
    private Course prerequisiteCourse;

    // Constructors
    public Prerequisite() {}

    public Prerequisite(int courseId, int prerequisiteCourseId, boolean isRequired) {
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
        this.isRequired = isRequired;
    }

    public Prerequisite(int prerequisiteId, int courseId, int prerequisiteCourseId, 
                       boolean isRequired, LocalDateTime createdAt) {
        this.prerequisiteId = prerequisiteId;
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
        this.isRequired = isRequired;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getPrerequisiteId() {
        return prerequisiteId;
    }

    public void setPrerequisiteId(int prerequisiteId) {
        this.prerequisiteId = prerequisiteId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getPrerequisiteCourseId() {
        return prerequisiteCourseId;
    }

    public void setPrerequisiteCourseId(int prerequisiteCourseId) {
        this.prerequisiteCourseId = prerequisiteCourseId;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Course getPrerequisiteCourse() {
        return prerequisiteCourse;
    }

    public void setPrerequisiteCourse(Course prerequisiteCourse) {
        this.prerequisiteCourse = prerequisiteCourse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prerequisite that = (Prerequisite) o;
        return courseId == that.courseId && prerequisiteCourseId == that.prerequisiteCourseId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, prerequisiteCourseId);
    }

    @Override
    public String toString() {
        return "Prerequisite{" +
                "prerequisiteId=" + prerequisiteId +
                ", courseId=" + courseId +
                ", prerequisiteCourseId=" + prerequisiteCourseId +
                ", isRequired=" + isRequired +
                '}';
    }
}

