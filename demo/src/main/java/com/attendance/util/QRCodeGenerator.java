package com.attendance.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static String generateDynamicQR(String sessionData, String filename) {
        try {
            String qrData = "ATTENDANCE:" + sessionData;

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = new MultiFormatWriter().encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    300,
                    300,
                    hints
            );

            File dir = new File("qr_sessions");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, filename + ".png");
            Path path = file.toPath();
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);

            return qrData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}