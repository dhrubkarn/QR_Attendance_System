package com.attendance.dao.impl;

import com.attendance.config.DBConnection;
import com.attendance.dao.AttendanceDAO;
import com.attendance.model.Attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAOImpl implements AttendanceDAO {

    @Override
    public void markAttendance(Attendance attendance) throws Exception {
        String sql = "INSERT INTO Attendance (student_id, subject_id, attendance_date, status, qr_session, device_hash) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, attendance.getStudentId());
        ps.setInt(2, attendance.getSubjectId());
        ps.setDate(3, attendance.getAttendanceDate());
        ps.setString(4, attendance.getStatus());
        ps.setString(5, attendance.getQrSession());
        ps.setString(6, attendance.getDeviceHash());

        ps.executeUpdate();
    }

    @Override
    public List<Attendance> getAttendanceByStudent(int studentId) throws Exception {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE student_id = ?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Attendance attendance = new Attendance(
                rs.getInt("attendance_id"),
                rs.getInt("student_id"),
                rs.getInt("subject_id"),
                rs.getDate("attendance_date"),
                rs.getString("status"),
                rs.getString("qr_session"),
                rs.getString("device_hash")
            );
            attendanceList.add(attendance);
        }

        return attendanceList;
    }

    @Override
    public boolean isAttendanceAlreadyMarked(int studentId, String qrSession, String deviceHash) throws Exception {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE student_id = ? AND qr_session = ? AND device_hash = ?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, studentId);
        ps.setString(2, qrSession);
        ps.setString(3, deviceHash);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}