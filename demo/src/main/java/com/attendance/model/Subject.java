package com.attendance.model;

public class Subject {
    private int subjectId;
    private String subjectName;
    private int classId;

    public Subject(int subjectId, String subjectName, int classId) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.classId = classId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }
}