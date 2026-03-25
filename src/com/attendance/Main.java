package com.attendance;

import com.attendance.ui.LoginFrame;
import com.attendance.util.UITheme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Enable hardware acceleration
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "false");

        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalTheme();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
