package com.attendance.view;

import javax.swing.*;
import java.awt.GridLayout;

public class StudentDashboard extends JFrame {

    private final int studentId;

    public StudentDashboard(int studentId) {
        this.studentId = studentId;

        setTitle("Student Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome Student", JLabel.CENTER);
        JButton viewAttendanceButton = new JButton("View My Attendance");
        JButton qrScannerButton = new JButton("Scan QR Code");
        JButton logoutButton = new JButton("Logout");

        panel.add(welcomeLabel);
        panel.add(viewAttendanceButton);
        panel.add(qrScannerButton);
        panel.add(logoutButton);

        add(panel);

        viewAttendanceButton.addActionListener(e ->
                new StudentAttendanceReport(studentId).setVisible(true)
        );

        qrScannerButton.addActionListener(e ->
                new StudentQRScanner(studentId).setVisible(true)
        );

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
    }
}