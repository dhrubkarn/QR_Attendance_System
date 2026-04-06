package com.attendance.view;

import com.attendance.config.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TeacherSessionReport extends JFrame {

    private JTable sessionTable;
    private JTable attendanceTable;
    private DefaultTableModel sessionModel;
    private DefaultTableModel attendanceModel;

    public TeacherSessionReport() {
        setTitle("Teacher Session Report");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        sessionModel = new DefaultTableModel(
                new String[]{"Session ID", "Session Code", "Subject", "Class", "Date", "Present", "Absent", "Status"}, 0
        );
        sessionTable = new JTable(sessionModel);

        attendanceModel = new DefaultTableModel(
                new String[]{"Attendance ID", "Student ID", "Student Name", "Status"}, 0
        );
        attendanceTable = new JTable(attendanceModel);

        JButton loadDetailsButton = new JButton("Load Selected Session");
        JButton toggleStatusButton = new JButton("Toggle Present/Absent");

        loadDetailsButton.addActionListener(e -> loadSelectedSessionAttendance());
        toggleStatusButton.addActionListener(e -> updateSelectedAttendanceStatus());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);
        topPanel.add(loadDetailsButton, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        bottomPanel.add(toggleStatusButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);

        loadSessions();
    }

    private void loadSessions() {
        sessionModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {
            String sql = """
                    SELECT ls.session_id,
                           ls.session_code,
                           sub.subject_name,
                           ls.class_name,
                           ls.session_date,
                           SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
                           SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
                           ls.status
                    FROM lecture_session ls
                    JOIN Subject sub ON sub.subject_id = ls.subject_id
                    LEFT JOIN Attendance a ON a.session_id = ls.session_id
                    GROUP BY ls.session_id, ls.session_code, sub.subject_name, ls.class_name, ls.session_date, ls.status
                    ORDER BY ls.session_date DESC, ls.session_id DESC
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sessionModel.addRow(new Object[]{
                        rs.getInt("session_id"),
                        rs.getString("session_code"),
                        rs.getString("subject_name"),
                        rs.getString("class_name"),
                        rs.getDate("session_date"),
                        rs.getInt("present_count"),
                        rs.getInt("absent_count"),
                        rs.getString("status")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading sessions: " + ex.getMessage());
        }
    }

    private void loadSelectedSessionAttendance() {
        int selectedRow = sessionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a session.");
            return;
        }

        int sessionId = Integer.parseInt(sessionModel.getValueAt(selectedRow, 0).toString());
        attendanceModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {
            String sql = """
                    SELECT a.attendance_id, s.student_id, s.full_name, a.status
                    FROM Attendance a
                    JOIN Student s ON s.student_id = a.student_id
                    WHERE a.session_id = ?
                    ORDER BY s.student_id
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                attendanceModel.addRow(new Object[]{
                        rs.getInt("attendance_id"),
                        rs.getInt("student_id"),
                        rs.getString("full_name"),
                        rs.getString("status")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading session attendance: " + ex.getMessage());
        }
    }

    private void updateSelectedAttendanceStatus() {
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student attendance row.");
            return;
        }

        int attendanceId = Integer.parseInt(attendanceModel.getValueAt(selectedRow, 0).toString());
        String currentStatus = attendanceModel.getValueAt(selectedRow, 3).toString();
        String newStatus = currentStatus.equalsIgnoreCase("PRESENT") ? "ABSENT" : "PRESENT";

        try (Connection con = DBConnection.getConnection()) {
            String sql = "UPDATE Attendance SET status = ? WHERE attendance_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, attendanceId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                attendanceModel.setValueAt(newStatus, selectedRow, 3);
                JOptionPane.showMessageDialog(this, "Attendance updated successfully.");
                loadSessions();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating attendance: " + ex.getMessage());
        }
    }
}