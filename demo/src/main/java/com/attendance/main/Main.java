package com.attendance.main;

import javax.swing.SwingUtilities;
import com.attendance.view.LoginFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}