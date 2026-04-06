package com.attendance.view;

import com.attendance.service.AttendanceService;
import com.attendance.exception.AttendanceException;
import com.attendance.model.Attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ViewReportFrame extends JFrame {

    private AttendanceService attendanceService;

    public ViewReportFrame() {
        attendanceService = new AttendanceService();
        
        setTitle("Attendance Report");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTable table = createReportTable();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, java.awt.BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back");
        
        refreshButton.addActionListener(e -> loadReport(table));
        backButton.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);
        add(buttonPanel, java.awt.BorderLayout.SOUTH);

        loadReport(table);
    }

    private JTable createReportTable() {
        String[] columns = {"Attendance ID", "Student ID", "Subject ID", "Date", "Status", "QR Session", "Device"};
        return new JTable(new DefaultTableModel(columns, 0));
    }

    private void loadReport(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        try {
            List<Attendance> attendances = attendanceService.getAttendanceByStudent(1); // Student ID 1
            for (Attendance att : attendances) {
                model.addRow(new Object[]{
                    att.getAttendanceId(),
                    att.getStudentId(),
                    att.getSubjectId(),
                    att.getAttendanceDate(),
                    att.getStatus(),
                    att.getQrSession(),
                    att.getDeviceHash()
                });
            }
        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}