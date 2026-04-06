package com.attendance.service;

public class LoginResult {
    public final String role;
    public final Integer userId;  // student_id for students, null for others
    
    public LoginResult(String role, Integer userId) {
        this.role = role;
        this.userId = userId;
    }
}