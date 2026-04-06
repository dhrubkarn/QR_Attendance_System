package com.attendance.model;

public class ClassRoom {
    private int classId;
    private String className;
    private int year;

    public ClassRoom(int classId, String className, int year) {
        this.classId = classId;
        this.className = className;
        this.year = year;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}