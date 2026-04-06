package com.attendance.service;

import com.attendance.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    public LoginResult login(String email, String password) throws Exception {
        try (Connection con = DBConnection.getConnection()) {

            // Check Admin
            String adminSql = "SELECT * FROM Admin WHERE email = ? AND password = ?";
            try (PreparedStatement adminPs = con.prepareStatement(adminSql)) {
                adminPs.setString(1, email);
                adminPs.setString(2, password);

                try (ResultSet adminRs = adminPs.executeQuery()) {
                    if (adminRs.next()) {
                        return new LoginResult("ADMIN", null);
                    }
                }
            }

            // Check Teacher
            String teacherSql = "SELECT * FROM Teacher WHERE email = ? AND password = ?";
            try (PreparedStatement teacherPs = con.prepareStatement(teacherSql)) {
                teacherPs.setString(1, email);
                teacherPs.setString(2, password);

                try (ResultSet teacherRs = teacherPs.executeQuery()) {
                    if (teacherRs.next()) {
                        return new LoginResult("TEACHER", null);
                    }
                }
            }

            // Check Student - FIXED: Added password check + get student_id
            String studentSql = "SELECT student_id, email, password FROM Student WHERE email = ? AND password = ?";
            try (PreparedStatement studentPs = con.prepareStatement(studentSql)) {
                studentPs.setString(1, email);
                studentPs.setString(2, password);

                try (ResultSet studentRs = studentPs.executeQuery()) {
                    if (studentRs.next()) {
                        int studentId = studentRs.getInt("student_id");
                        return new LoginResult("STUDENT", studentId);
                    }
                }
            }

            return null;
        }
    }
}