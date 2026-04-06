package com.attendance.view;

import com.attendance.exception.AttendanceException;
import com.attendance.service.AuthService;
import com.attendance.service.LoginResult;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private AuthService authService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public LoginFrame() {
        authService = new AuthService();

        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel());
        loginButton = new JButton("Login");
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required.");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format. Use something@domain.com");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required.");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.");
            passwordField.requestFocus();
            return;
        }

        try {
            LoginResult result = authService.login(email, password);

            if (result == null || result.role == null || result.role.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Login successful!");

            if ("ADMIN".equalsIgnoreCase(result.role)) {
                new AdminDashboard().setVisible(true);
                dispose();
            } else if ("TEACHER".equalsIgnoreCase(result.role)) {
                new TeacherDashboard().setVisible(true);
                dispose();
            } else if ("STUDENT".equalsIgnoreCase(result.role)) {
                if (result.userId != null) {
                    new StudentDashboard(result.userId).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Student ID not found.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Unknown user role: " + result.role);
            }

        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}