package com.attendance.model;

public class Student extends User {
    private String rollNo;
    private int classId;
    
    public Student(int id, String name, String email, String passwordHash, String rollNo, int classId) {
        super(id, name, email, passwordHash);
        this.rollNo = rollNo;
        this.classId = classId;
    }
    
    @Override
    public String getRole() {
        return "STUDENT";
    }
    
    // Getters/Setters
    public String getRollNo() { return rollNo; }
    public int getClassId() { return classId; }
}