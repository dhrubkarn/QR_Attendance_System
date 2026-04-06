package com.attendance.view;

import com.attendance.config.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentAttendanceReport extends JFrame {

    private final int studentId;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblStudentHeader;
    private JLabel lblTotalSessions;
    private JLabel lblAttendedSessions;
    private JLabel lblTotalPercentage;

    public StudentAttendanceReport(int studentId) {
        this.studentId = studentId;

        setTitle("My Attendance Report");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        lblStudentHeader = new JLabel("Student Attendance Report", JLabel.CENTER);
        lblStudentHeader.setFont(new Font("Serif", Font.BOLD, 26));
        lblStudentHeader.setForeground(new Color(25, 45, 95));
        add(lblStudentHeader, BorderLayout.NORTH);

        String[] columns = {
                "Course Name",
                "Total Sessions",
                "Marked Sessions",
                "Attended Sessions",
                "Percentage"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Serif", Font.BOLD, 16));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(103, 160, 166));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setGridColor(Color.GRAY);
        table.setShowGrid(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(4, 1, 5, 5));

        lblTotalSessions = new JLabel("Total Session: 0", JLabel.CENTER);
        lblAttendedSessions = new JLabel("Total Attended Session: 0", JLabel.CENTER);
        lblTotalPercentage = new JLabel("Total Percentage: 0.00%", JLabel.CENTER);

        Font summaryFont = new Font("Serif", Font.BOLD, 22);
        Color summaryColor = new Color(0, 51, 102);

        lblTotalSessions.setFont(summaryFont);
        lblAttendedSessions.setFont(summaryFont);
        lblTotalPercentage.setFont(summaryFont);

        lblTotalSessions.setForeground(summaryColor);
        lblAttendedSessions.setForeground(summaryColor);
        lblTotalPercentage.setForeground(summaryColor);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.addActionListener(e -> dispose());

        summaryPanel.add(lblTotalSessions);
        summaryPanel.add(lblAttendedSessions);
        summaryPanel.add(lblTotalPercentage);
        summaryPanel.add(backButton);

        add(summaryPanel, BorderLayout.SOUTH);

        loadStudentAttendanceSummary();
    }

    private void loadStudentAttendanceSummary() {
        model.setRowCount(0);

        int grandTotalSessions = 0;
        int grandAttendedSessions = 0;

        try (Connection con = DBConnection.getConnection()) {

            String studentSql = "SELECT student_id, full_name FROM Student WHERE student_id = ?";
            PreparedStatement studentPs = con.prepareStatement(studentSql);
            studentPs.setInt(1, studentId);
            ResultSet studentRs = studentPs.executeQuery();

            if (studentRs.next()) {
                String sid = studentRs.getString("student_id");
                String fullName = studentRs.getString("full_name");
                lblStudentHeader.setText(sid + " - " + fullName);
            }

            String sql = """
                    SELECT s.subject_name,
                           COUNT(a.attendance_id) AS total_sessions,
                           COUNT(a.attendance_id) AS marked_sessions,
                           SUM(
                               CASE
                                   WHEN UPPER(a.status) IN ('PRESENT', 'LATE', 'EXCUSED')
                                   THEN 1
                                   ELSE 0
                               END
                           ) AS attended_sessions
                    FROM Attendance a
                    JOIN Subject s ON a.subject_id = s.subject_id
                    WHERE a.student_id = ?
                    GROUP BY a.subject_id, s.subject_name
                    ORDER BY s.subject_name
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String subjectName = rs.getString("subject_name");
                int totalSessions = rs.getInt("total_sessions");
                int markedSessions = rs.getInt("marked_sessions");
                int attendedSessions = rs.getInt("attended_sessions");

                grandTotalSessions += totalSessions;
                grandAttendedSessions += attendedSessions;

                String percentage = totalSessions > 0
                        ? String.format("%.2f%%", (attendedSessions * 100.0) / totalSessions)
                        : "-";

                model.addRow(new Object[]{
                        subjectName,
                        totalSessions,
                        markedSessions,
                        attendedSessions,
                        percentage
                });
            }

            double overallPercentage = grandTotalSessions > 0
                    ? (grandAttendedSessions * 100.0) / grandTotalSessions
                    : 0.0;

            lblTotalSessions.setText("Total Session: " + grandTotalSessions);
            lblAttendedSessions.setText("Total Attended Session: " + grandAttendedSessions);
            lblTotalPercentage.setText("Total Percentage: " + String.format("%.2f%%", overallPercentage));

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading attendance report: " + ex.getMessage());
        }
    }
}