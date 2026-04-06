package com.attendance.view;

import javax.swing.*;
import java.awt.GridLayout;

public class TeacherDashboard extends JFrame {

    public TeacherDashboard() {
        setTitle("Teacher Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome Teacher", JLabel.CENTER);
        JButton markAttendanceButton = new JButton("Mark Attendance");
        JButton viewReportsButton = new JButton("View Session Reports");
        JButton logoutButton = new JButton("Logout");

        panel.add(welcomeLabel);
        panel.add(markAttendanceButton);
        panel.add(viewReportsButton);
        panel.add(logoutButton);

        add(panel);

        markAttendanceButton.addActionListener(e -> new MarkAttendanceFrame().setVisible(true));

        viewReportsButton.addActionListener(e -> new TeacherSessionReport().setVisible(true));

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