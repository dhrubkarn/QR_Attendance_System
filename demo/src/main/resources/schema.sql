CREATE DATABASE IF NOT EXISTS attendance_system;
USE attendance_system;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Attendance;
DROP TABLE IF EXISTS lecture_session;
DROP TABLE IF EXISTS Subject;
DROP TABLE IF EXISTS Student;
DROP TABLE IF EXISTS Teacher;
DROP TABLE IF EXISTS Admin;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Teacher (
    teacher_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Student (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_no VARCHAR(50) UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    department VARCHAR(100) NOT NULL,
    class_name VARCHAR(50) DEFAULT 'SE-A',
    system_unique_id VARCHAR(255) DEFAULT 'TEMP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Subject (
    subject_id INT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(30) UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lecture_session (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    session_code VARCHAR(150) NOT NULL UNIQUE,
    subject_id INT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    session_date DATE NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lecture_subject
        FOREIGN KEY (subject_id) REFERENCES Subject(subject_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    teacher_id INT NULL,
    session_id INT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    marked_by VARCHAR(50) NULL,
    remarks VARCHAR(255) NULL,
    device_id VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_id) REFERENCES Student(student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_attendance_subject
        FOREIGN KEY (subject_id) REFERENCES Subject(subject_id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_attendance_teacher
        FOREIGN KEY (teacher_id) REFERENCES Teacher(teacher_id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT fk_attendance_session
        FOREIGN KEY (session_id) REFERENCES lecture_session(session_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_attendance_student_date
ON Attendance(student_id, attendance_date);

CREATE INDEX idx_attendance_session_student
ON Attendance(session_id, student_id);

CREATE INDEX idx_attendance_subject_date
ON Attendance(subject_id, attendance_date);

INSERT INTO Subject (subject_code, subject_name, department) VALUES
('MTH101', 'Mathematics', 'Computer Engineering'),
('PHY101', 'Physics', 'Computer Engineering'),
('CHM101', 'Chemistry', 'Computer Engineering'),
('CSC101', 'Computer Science', 'Computer Engineering');

INSERT INTO Admin (full_name, email, username, password) VALUES
('System Admin', 'admin@gmail.com', 'admin', 'admin1234');