package com.attendance.view;

import com.attendance.config.DBConnection;
import com.attendance.util.DeviceUtil;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class StudentQRScanner extends JFrame implements Runnable {

    private final int studentId;

    private JPanel contentPane;
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private Thread scannerThread;

    private JLabel lblTitle;
    private JLabel lblStudentInfo;
    private JLabel lblStatus;
    private JLabel lblPreview;

    private JButton btnStartCamera;
    private JButton btnStopCamera;
    private JButton btnUploadQR;
    private JButton btnMarkAttendance;
    private JButton btnClose;

    private volatile boolean running = false;
    private volatile boolean qrScanned = false;
    private String scannedQRText = "";

    public StudentQRScanner(int studentId) {
        this.studentId = studentId;

        setTitle("Student QR Scanner");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        JPanel topPanel = new JPanel();
        lblTitle = new JLabel("Student Attendance QR Scanner");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        topPanel.add(lblTitle);
        contentPane.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        contentPane.add(centerPanel, BorderLayout.CENTER);

        webcam = Webcam.getDefault();

        if (webcam != null) {
            webcam.setViewSize(new Dimension(640, 480));
            webcamPanel = new WebcamPanel(webcam);
            webcamPanel.setFPSDisplayed(true);
            webcamPanel.setMirrored(true);
            centerPanel.add(webcamPanel);
        } else {
            JLabel noCamLabel = new JLabel("No webcam detected", SwingConstants.CENTER);
            noCamLabel.setFont(new Font("Arial", Font.BOLD, 18));
            centerPanel.add(noCamLabel);
        }

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Attendance Details"));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        lblStudentInfo = new JLabel("Student ID: " + studentId);
        lblStatus = new JLabel("Status: Waiting for scan...");
        lblStatus.setForeground(Color.BLUE);

        infoPanel.add(lblStudentInfo);
        infoPanel.add(new JLabel("Scan or upload QR image."));
        infoPanel.add(lblStatus);

        lblPreview = new JLabel("Uploaded QR Preview", SwingConstants.CENTER);
        lblPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblPreview.setPreferredSize(new Dimension(280, 280));

        detailsPanel.add(infoPanel, BorderLayout.NORTH);
        detailsPanel.add(lblPreview, BorderLayout.CENTER);

        centerPanel.add(detailsPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnStartCamera = new JButton("Start Scanner");
        btnStopCamera = new JButton("Stop Scanner");
        btnUploadQR = new JButton("Upload QR Image");
        btnMarkAttendance = new JButton("Mark Attendance");
        btnClose = new JButton("Close");

        buttonPanel.add(btnStartCamera);
        buttonPanel.add(btnStopCamera);
        buttonPanel.add(btnUploadQR);
        buttonPanel.add(btnMarkAttendance);
        buttonPanel.add(btnClose);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        btnStartCamera.addActionListener(e -> startScanner());
        btnStopCamera.addActionListener(e -> stopScanner());
        btnUploadQR.addActionListener(e -> uploadQRImage());
        btnMarkAttendance.addActionListener(e -> markAttendanceWithQR());
        btnClose.addActionListener(e -> closeWindow());

        btnStopCamera.setEnabled(false);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopScanner();
            }
        });
    }

    private void startScanner() {
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "Webcam not found.");
            return;
        }

        if (!webcam.isOpen()) {
            webcam.open();
        }

        running = true;
        qrScanned = false;
        scannedQRText = "";
        lblStatus.setText("Status: Scanning QR...");
        lblStatus.setForeground(Color.BLUE);

        scannerThread = new Thread(this);
        scannerThread.setDaemon(true);
        scannerThread.start();

        btnStartCamera.setEnabled(false);
        btnStopCamera.setEnabled(true);
    }

    private void stopScanner() {
        running = false;

        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }

        btnStartCamera.setEnabled(true);
        btnStopCamera.setEnabled(false);
    }

    private void uploadQRImage() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);

        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                JOptionPane.showMessageDialog(this, "Invalid image file.");
                return;
            }

            Image scaled = image.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            lblPreview.setIcon(new ImageIcon(scaled));
            lblPreview.setText("");

            BinaryBitmap binaryBitmap =
                    new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));

            Result result = new MultiFormatReader().decode(binaryBitmap);

            if (result != null) {
                scannedQRText = result.getText() == null ? "" : result.getText().trim();
                qrScanned = !scannedQRText.isEmpty();
                lblStatus.setText("QR Uploaded: " + scannedQRText);
                lblStatus.setForeground(new Color(0, 128, 0));
            } else {
                JOptionPane.showMessageDialog(this, "QR not detected in image.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to read QR image: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }

            if (!running || webcam == null || !webcam.isOpen()) {
                continue;
            }

            BufferedImage image = webcam.getImage();
            if (image == null) {
                continue;
            }

            try {
                BinaryBitmap binaryBitmap =
                        new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));

                Result result = new MultiFormatReader().decode(binaryBitmap);

                if (result != null) {
                    scannedQRText = result.getText() == null ? "" : result.getText().trim();
                    qrScanned = !scannedQRText.isEmpty();

                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("QR Scanned: " + scannedQRText);
                        lblStatus.setForeground(new Color(0, 128, 0));
                    });

                    stopScanner();
                    break;
                }

            } catch (Exception ignored) {
            }

        } while (running);
    }

    private void markAttendanceWithQR() {
        String qrValue = scannedQRText == null ? "" : scannedQRText.trim();

        if (!qrScanned || qrValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please scan or upload the QR code first.");
            return;
        }

        if (!validateSystemUniqueId(studentId)) {
            JOptionPane.showMessageDialog(this,
                    "This system is not registered for this student.\nAttendance blocked by System Unique ID validation.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sessionSql = """
                    SELECT session_id, subject_id, end_time, status
                    FROM lecture_session
                    WHERE TRIM(session_code) = ?
                    LIMIT 1
                    """;

            PreparedStatement sessionPs = con.prepareStatement(sessionSql);
            sessionPs.setString(1, qrValue);
            ResultSet sessionRs = sessionPs.executeQuery();

            if (!sessionRs.next()) {
                JOptionPane.showMessageDialog(this, "Invalid QR session.\nScanned value: " + qrValue);
                return;
            }

            int sessionId = sessionRs.getInt("session_id");
            int subjectId = sessionRs.getInt("subject_id");
            LocalDateTime endTime = sessionRs.getTimestamp("end_time").toLocalDateTime();
            String sessionStatus = sessionRs.getString("status");

            if (!"ACTIVE".equalsIgnoreCase(sessionStatus) || endTime.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "QR session has expired.");
                return;
            }

            if (isAttendanceAlreadyMarked(con, sessionId, studentId)) {
                JOptionPane.showMessageDialog(this, "Attendance already marked for this session.");
                return;
            }

            String insertSql = """
                    INSERT INTO Attendance
                    (student_id, subject_id, attendance_date, attendance_time, qr_session, device_hash, status, marked_by, remarks, session_id, device_id)
                    VALUES (?, ?, CURDATE(), NOW(), ?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement insertPs = con.prepareStatement(insertSql);
            insertPs.setInt(1, studentId);
            insertPs.setInt(2, subjectId);
            insertPs.setString(3, qrValue);
            insertPs.setString(4, DeviceUtil.generateSystemUniqueId());
            insertPs.setString(5, "Present");
            insertPs.setString(6, "Student");
            insertPs.setString(7, "Marked by QR");
            insertPs.setInt(8, sessionId);
            insertPs.setString(9, DeviceUtil.generateSystemUniqueId());

            int inserted = insertPs.executeUpdate();

            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "Attendance marked successfully.");
                lblStatus.setText("Status: Attendance marked");
                lblStatus.setForeground(new Color(0, 128, 0));
                scannedQRText = "";
                qrScanned = false;
                lblPreview.setIcon(null);
                lblPreview.setText("Uploaded QR Preview");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to mark attendance.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private boolean isAttendanceAlreadyMarked(Connection con, int sessionId, int studentId) {
        try {
            String sql = "SELECT attendance_id FROM Attendance WHERE session_id = ? AND student_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, sessionId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateSystemUniqueId(int studentId) {
        String currentSystemUniqueId = DeviceUtil.generateSystemUniqueId();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT system_unique_id FROM Student WHERE student_id = ?"
            );
            ps.setInt(1, studentId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String savedSystemUniqueId = rs.getString("system_unique_id");

                if (savedSystemUniqueId == null || savedSystemUniqueId.trim().isEmpty()
                        || "TEMP".equalsIgnoreCase(savedSystemUniqueId)) {

                    PreparedStatement updatePs = con.prepareStatement(
                            "UPDATE Student SET system_unique_id = ? WHERE student_id = ?"
                    );
                    updatePs.setString(1, currentSystemUniqueId);
                    updatePs.setInt(2, studentId);
                    updatePs.executeUpdate();
                    return true;
                }

                return savedSystemUniqueId.equals(currentSystemUniqueId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void closeWindow() {
        stopScanner();
        dispose();
    }
}