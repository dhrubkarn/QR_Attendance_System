package com.attendance.dao;

import java.util.List;
import com.attendance.model.Attendance;

public interface AttendanceDAO {
    void markAttendance(Attendance attendance) throws Exception;
    List<Attendance> getAttendanceByStudent(int studentId) throws Exception;
    boolean isAttendanceAlreadyMarked(int studentId, String qrSession, String deviceHash) throws Exception;
}