package com.attendance.view;

import com.attendance.config.DBConnection;
import com.attendance.util.QRCodeGenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MarkAttendanceFrame extends JFrame {

    private JComboBox<SubjectItem> subjectComboBox;
    private JSpinner durationSpinner;
    private JLabel qrSessionLabel;
    private JLabel qrImageLabel;
    private JLabel timerLabel;
    private JButton generateQRButton;
    private JButton stopSessionButton;

    private String currentQRSession;
    private Integer currentSessionId;
    private javax.swing.Timer sessionTimer;
    private int remainingSeconds;

    public MarkAttendanceFrame() {
        setTitle("QR Attendance - Dynamic QR with Timer");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        topPanel.add(new JLabel("Subject:", JLabel.CENTER));
        subjectComboBox = new JComboBox<>();
        loadSubjects();
        topPanel.add(subjectComboBox);

        topPanel.add(new JLabel("QR Duration (2-15 min):", JLabel.CENTER));
        durationSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 15, 1));
        topPanel.add(durationSpinner);

        topPanel.add(new JLabel("QR Session ID:", JLabel.CENTER));
        qrSessionLabel = new JLabel("Click Generate QR to start session", JLabel.CENTER);
        topPanel.add(qrSessionLabel);

        generateQRButton = new JButton("Generate Dynamic QR");
        stopSessionButton = new JButton("Stop Session");
        stopSessionButton.setEnabled(false);

        topPanel.add(generateQRButton);
        topPanel.add(stopSessionButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        qrImageLabel = new JLabel("QR Code will appear here", JLabel.CENTER);
        qrImageLabel.setPreferredSize(new Dimension(320, 320));
        qrImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        centerPanel.add(qrImageLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("Session Active: --:--", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(new Color(0, 102, 204));
        centerPanel.add(timerLabel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            if (sessionTimer != null && sessionTimer.isRunning()) {
                sessionTimer.stop();
            }
            dispose();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        generateQRButton.addActionListener(e -> generateDynamicQRWithTimer());
        stopSessionButton.addActionListener(e -> closeCurrentSession(true));
    }

    private void loadSubjects() {
        subjectComboBox.removeAllItems();

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT subject_id, subject_name FROM Subject ORDER BY subject_id";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int subjectId = rs.getInt("subject_id");
                String subjectName = rs.getString("subject_name");
                subjectComboBox.addItem(new SubjectItem(subjectId, subjectName));
            }

            if (subjectComboBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No subjects found in database.");
                generateQRButton.setEnabled(false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + ex.getMessage());
            generateQRButton.setEnabled(false);
        }
    }

    private void generateDynamicQRWithTimer() {
        SubjectItem selectedSubject = (SubjectItem) subjectComboBox.getSelectedItem();

        if (selectedSubject == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject.");
            return;
        }

        int subjectId = selectedSubject.getSubjectId();
        String className = "SE-A";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        int durationMinutes = (Integer) durationSpinner.getValue();

        currentQRSession = "SUB" + subjectId + "_" + className + "_" + timestamp;
        remainingSeconds = durationMinutes * 60;

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        String fileName = "session_" + timestamp;
        String generatedQRValue = QRCodeGenerator.generateDynamicQR(currentQRSession, fileName);

        if (generatedQRValue == null || generatedQRValue.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Failed to generate QR code.");
            return;
        }

        currentQRSession = generatedQRValue.trim();

        try (Connection con = DBConnection.getConnection()) {
            String insertSessionSql = """
                    INSERT INTO lecture_session
                    (session_code, subject_id, class_name, session_date, start_time, end_time, status)
                    VALUES (?, ?, ?, CURDATE(), ?, ?, 'ACTIVE')
                    """;

            PreparedStatement ps = con.prepareStatement(insertSessionSql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, currentQRSession);
            ps.setInt(2, subjectId);
            ps.setString(3, className);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(startTime));
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(endTime));

            int inserted = ps.executeUpdate();
            if (inserted <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to save session.");
                return;
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                currentSessionId = rs.getInt(1);
            } else {
                JOptionPane.showMessageDialog(this, "Session created but ID was not returned.");
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save session: " + ex.getMessage());
            return;
        }

        qrSessionLabel.setText(currentQRSession);

        try {
            File qrFile = new File("qr_sessions", fileName + ".png");

            if (!qrFile.exists()) {
                throw new RuntimeException("QR file not found: " + qrFile.getAbsolutePath());
            }

            BufferedImage bufferedImage = ImageIO.read(qrFile);
            if (bufferedImage == null) {
                throw new RuntimeException("Unable to read QR image.");
            }

            Image scaledImage = bufferedImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            qrImageLabel.setIcon(new ImageIcon(scaledImage));
            qrImageLabel.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "QR generated but image could not load.\n" + ex.getMessage());
            return;
        }

        startSessionTimer();
        generateQRButton.setEnabled(false);
        stopSessionButton.setEnabled(true);

        JOptionPane.showMessageDialog(
                this,
                "QR Session Started\nSubject: " + selectedSubject.getSubjectName()
                        + "\nDuration: " + durationMinutes + " minutes"
                        + "\nSession ID: " + currentSessionId
        );
    }

    private void startSessionTimer() {
        if (sessionTimer != null && sessionTimer.isRunning()) {
            sessionTimer.stop();
        }

        updateTimerLabel();

        sessionTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            updateTimerLabel();

            if (remainingSeconds <= 0) {
                sessionTimer.stop();
                closeCurrentSession(false);
            }
        });

        sessionTimer.start();
    }

    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("Session Active: %02d:%02d", minutes, seconds));

        if (remainingSeconds <= 30) {
            timerLabel.setForeground(Color.RED);
        } else {
            timerLabel.setForeground(new Color(0, 102, 204));
        }
    }

    private void closeCurrentSession(boolean manualClose) {
        if (sessionTimer != null && sessionTimer.isRunning()) {
            sessionTimer.stop();
        }

        if (currentSessionId == null) {
            resetUI(manualClose ? "Session stopped" : "Session expired");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String absentSql = """
                    INSERT INTO Attendance
                    (student_id, subject_id, attendance_date, attendance_time, qr_session, device_hash, status, marked_by, remarks, session_id, device_id)
                    SELECT s.student_id,
                           ls.subject_id,
                           ls.session_date,
                           NOW(),
                           ls.session_code,
                           NULL,
                           'Absent',
                           'Teacher',
                           'Auto-marked absent',
                           ls.session_id,
                           NULL
                    FROM Student s
                    JOIN lecture_session ls ON ls.session_id = ?
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM Attendance a
                        WHERE a.session_id = ls.session_id
                          AND a.student_id = s.student_id
                    )
                    """;

            PreparedStatement absentPs = con.prepareStatement(absentSql);
            absentPs.setInt(1, currentSessionId);
            absentPs.executeUpdate();

            String updateSessionSql = "UPDATE lecture_session SET status = 'CLOSED' WHERE session_id = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSessionSql);
            updatePs.setInt(1, currentSessionId);
            updatePs.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    manualClose ? "Session closed and absentees marked." : "Session expired and absentees marked."
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error closing session: " + ex.getMessage());
        }

        resetUI(manualClose ? "Session stopped" : "Session expired");
        currentSessionId = null;
        currentQRSession = null;
        remainingSeconds = 0;
    }

    private void resetUI(String statusText) {
        qrSessionLabel.setText(statusText);
        qrImageLabel.setIcon(null);
        qrImageLabel.setText("QR Code will appear here");
        timerLabel.setText(statusText);
        timerLabel.setForeground(Color.GRAY);

        generateQRButton.setEnabled(true);
        stopSessionButton.setEnabled(false);
    }

    private static class SubjectItem {
        private final int subjectId;
        private final String subjectName;

        public SubjectItem(int subjectId, String subjectName) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }

        public int getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        @Override
        public String toString() {
            return subjectName + " (ID:" + subjectId + ")";
        }
    }
}