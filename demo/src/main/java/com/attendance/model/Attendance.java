package com.attendance.model;

import java.sql.Date;

public class Attendance {
    private int attendanceId;
    private int studentId;
    private int subjectId;
    private Date attendanceDate;
    private String status;
    private String qrSession;
    private String deviceHash;

    public Attendance() {
    }

    public Attendance(int attendanceId, int studentId, int subjectId, Date attendanceDate,
                      String status, String qrSession, String deviceHash) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.qrSession = qrSession;
        this.deviceHash = deviceHash;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrSession() {
        return qrSession;
    }

    public void setQrSession(String qrSession) {
        this.qrSession = qrSession;
    }

    public String getDeviceHash() {
        return deviceHash;
    }

    public void setDeviceHash(String deviceHash) {
        this.deviceHash = deviceHash;
    }
}