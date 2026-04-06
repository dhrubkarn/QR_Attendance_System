package com.attendance.view;

import com.attendance.config.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class TeacherAttendancePanel extends JFrame {

    private JTextField txtStudentId, txtSubjectId, txtTeacherId, txtRemarks;
    private JComboBox<String> cmbStatus;
    private JButton btnSave;

    public TeacherAttendancePanel() {
        setTitle("Teacher Manual Attendance");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2, 10, 10));

        txtStudentId = new JTextField();
        txtSubjectId = new JTextField();
        txtTeacherId = new JTextField();
        txtRemarks = new JTextField();

        cmbStatus = new JComboBox<>(new String[]{"Present", "Absent", "Late", "Excused", "Rejected"});
        btnSave = new JButton("Save Attendance");

        add(new JLabel("Student ID:"));
        add(txtStudentId);

        add(new JLabel("Subject ID:"));
        add(txtSubjectId);

        add(new JLabel("Teacher ID:"));
        add(txtTeacherId);

        add(new JLabel("Status:"));
        add(cmbStatus);

        add(new JLabel("Remarks:"));
        add(txtRemarks);

        add(new JLabel());
        add(btnSave);

        btnSave.addActionListener(e -> saveManualAttendance());
    }

    private void saveManualAttendance() {
        String studentIdText = txtStudentId.getText().trim();
        String subjectIdText = txtSubjectId.getText().trim();
        String teacherIdText = txtTeacherId.getText().trim();
        String remarks = txtRemarks.getText().trim();
        String status = cmbStatus.getSelectedItem().toString();

        if (studentIdText.isEmpty() || subjectIdText.isEmpty() || teacherIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student ID, Subject ID, and Teacher ID are required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO Attendance(student_id, subject_id, teacher_id, attendance_date, status, marked_by, remarks) "
                    + "VALUES(?,?,?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(studentIdText));
            ps.setInt(2, Integer.parseInt(subjectIdText));
            ps.setInt(3, Integer.parseInt(teacherIdText));
            ps.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(5, status);
            ps.setString(6, "Teacher");
            ps.setString(7, remarks);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Attendance saved successfully.");
            clearFields();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtStudentId.setText("");
        txtSubjectId.setText("");
        txtTeacherId.setText("");
        txtRemarks.setText("");
        cmbStatus.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TeacherAttendancePanel().setVisible(true));
    }
}