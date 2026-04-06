package com.attendance.service;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.impl.AttendanceDAOImpl;
import com.attendance.exception.AttendanceException;
import com.attendance.model.Attendance;

import java.util.List;

public class AttendanceService {

    private AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAOImpl();
    }

    public void markAttendance(Attendance attendance) throws AttendanceException {
        try {
            boolean alreadyMarked = attendanceDAO.isAttendanceAlreadyMarked(
                    attendance.getStudentId(),
                    attendance.getQrSession(),
                    attendance.getDeviceHash()
            );

            if (alreadyMarked) {
                throw new AttendanceException("Attendance already marked for this session from this device.");
            }

            attendanceDAO.markAttendance(attendance);

        } catch (AttendanceException e) {
            throw e;
        } catch (Exception e) {
            throw new AttendanceException("Error while marking attendance: " + e.getMessage());
        }
    }

    public List<Attendance> getAttendanceByStudent(int studentId) throws AttendanceException {
        try {
            return attendanceDAO.getAttendanceByStudent(studentId);
        } catch (Exception e) {
            throw new AttendanceException("Error fetching attendance: " + e.getMessage());
        }
    }
}