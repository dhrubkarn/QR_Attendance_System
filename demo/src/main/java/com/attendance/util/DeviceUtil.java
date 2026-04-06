package com.attendance.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;

public class DeviceUtil {

    public static String generateSystemUniqueId() {
        try {
            StringBuilder raw = new StringBuilder();

            String userName = System.getProperty("user.name", "");
            String osName = System.getProperty("os.name", "");
            String osArch = System.getProperty("os.arch", "");
            String hostName = InetAddress.getLocalHost().getHostName();

            raw.append(userName).append("|")
               .append(osName).append("|")
               .append(osArch).append("|")
               .append(hostName).append("|")
               .append(getPrimaryMacAddress());

            return sha256(raw.toString());

        } catch (Exception e) {
            return "UNKNOWN_SYSTEM";
        }
    }

    private static String getPrimaryMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (ni == null || ni.isLoopback() || ni.isVirtual() || !ni.isUp()) {
                    continue;
                }

                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return "NO_MAC";
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "HASH_ERROR";
        }
    }
}