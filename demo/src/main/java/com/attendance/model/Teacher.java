package com.attendance.model;

public class Teacher extends User {

    public Teacher(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash);
    }

    @Override
    public String getRole() {
        return "TEACHER";
    }
}