package com.attendance.view;

import com.attendance.config.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

public class AdminDashboard extends JFrame {

    private JTextField txtTeacherName, txtTeacherEmail, txtTeacherPhone, txtTeacherUsername;
    private JPasswordField txtTeacherPassword;

    private JTextField txtStudentName, txtStudentEmail, txtStudentPhone, txtStudentDepartment;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        topPanel.add(logoutButton);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add Teacher", createTeacherPanel());
        tabbedPane.addTab("Add Student", createStudentPanel());

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        setVisible(true);
    }

    private JPanel createTeacherPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtTeacherName = new JTextField();
        txtTeacherEmail = new JTextField();
        txtTeacherPhone = new JTextField();
        txtTeacherUsername = new JTextField();
        txtTeacherPassword = new JPasswordField();

        JButton btnAddTeacher = new JButton("Add Teacher");
        btnAddTeacher.addActionListener(e -> addTeacher());

        panel.add(new JLabel("Full Name:"));
        panel.add(txtTeacherName);

        panel.add(new JLabel("Email:"));
        panel.add(txtTeacherEmail);

        panel.add(new JLabel("Phone:"));
        panel.add(txtTeacherPhone);

        panel.add(new JLabel("Username:"));
        panel.add(txtTeacherUsername);

        panel.add(new JLabel("Password:"));
        panel.add(txtTeacherPassword);

        panel.add(new JLabel(""));
        panel.add(btnAddTeacher);

        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtStudentName = new JTextField();
        txtStudentEmail = new JTextField();
        txtStudentPhone = new JTextField();
        txtStudentDepartment = new JTextField();

        JButton btnAddStudent = new JButton("Add Student");
        btnAddStudent.addActionListener(e -> addStudent());

        panel.add(new JLabel("Full Name:"));
        panel.add(txtStudentName);

        panel.add(new JLabel("Email:"));
        panel.add(txtStudentEmail);

        panel.add(new JLabel("Phone:"));
        panel.add(txtStudentPhone);

        panel.add(new JLabel("Department:"));
        panel.add(txtStudentDepartment);

        panel.add(new JLabel(""));
        panel.add(btnAddStudent);

        return panel;
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && NAME_PATTERN.matcher(name).matches();
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    private boolean isUsernameUnique(String username) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT username FROM Admin WHERE username = ? " +
                         "UNION " +
                         "SELECT username FROM Teacher WHERE username = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);

            ResultSet rs = ps.executeQuery();
            return !rs.next();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking username: " + e.getMessage());
            return false;
        }
    }

    private void addTeacher() {
        String name = txtTeacherName.getText().trim();
        String email = txtTeacherEmail.getText().trim();
        String phone = txtTeacherPhone.getText().trim();
        String username = txtTeacherUsername.getText().trim();
        String password = new String(txtTeacherPassword.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All teacher fields are required.");
            return;
        }

        if (!isValidName(name)) {
            JOptionPane.showMessageDialog(this, "Name must contain only letters and spaces.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }

        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.");
            return;
        }

        if (!isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long.");
            return;
        }

        if (!isUsernameUnique(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another username.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO Teacher(full_name, email, phone, username, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, username);
            ps.setString(5, password);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Teacher added successfully.");
            clearTeacherFields();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding teacher: " + e.getMessage());
        }
    }

    private void addStudent() {
        String name = txtStudentName.getText().trim();
        String email = txtStudentEmail.getText().trim();
        String phone = txtStudentPhone.getText().trim();
        String department = txtStudentDepartment.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || department.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All student fields are required.");
            return;
        }

        if (!isValidName(name)) {
            JOptionPane.showMessageDialog(this, "Name must contain only letters and spaces.");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }

        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO Student(full_name, email, phone, department) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, department);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added successfully.");
            clearStudentFields();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding student: " + e.getMessage());
        }
    }

    private void clearTeacherFields() {
        txtTeacherName.setText("");
        txtTeacherEmail.setText("");
        txtTeacherPhone.setText("");
        txtTeacherUsername.setText("");
        txtTeacherPassword.setText("");
    }

    private void clearStudentFields() {
        txtStudentName.setText("");
        txtStudentEmail.setText("");
        txtStudentPhone.setText("");
        txtStudentDepartment.setText("");
    }
}