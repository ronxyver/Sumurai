// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;

public final class ThemeManager {
    private static final Color BACKGROUND = new Color(25, 25, 45);
    private static final Color ACCENT = new Color(86, 126, 234);
    private static final Color TEXT = new Color(245, 245, 250);

    private ThemeManager() {
    }

    public static Color backgroundColor() {
        return BACKGROUND;
    }

    public static Color accentColor() {
        return ACCENT;
    }

    public static Color textColor() {
        return TEXT;
    }

    public static Font headingFont(float size) {
        return new Font("SansSerif", Font.BOLD, Math.max(12, Math.round(size)));
    }

    public static ImageIcon loadIcon(String resourcePath) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource path is required.");
        }
        URL resource = ThemeManager.class.getResource(resourcePath.startsWith("/")
                ? resourcePath
                : "/" + resourcePath);
        return resource == null ? new ImageIcon() : new ImageIcon(resource);
    }
}
