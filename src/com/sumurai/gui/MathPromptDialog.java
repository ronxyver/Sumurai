// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Displays the current arithmetic problem and returns the entered answer. */
public final class MathPromptDialog {
    private MathPromptDialog() {
    }

    /**
     * Shows a modal question dialog.
     *
     * @param parent component that owns the dialog
     * @param prompt arithmetic question to display
     * @return the entered answer, or {@code null} if the dialog was cancelled
     */
    public static String show(Component parent, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("A math prompt is required.");
        }
        return (String) JOptionPane.showInputDialog(
                parent,
                prompt,
                "Solve the problem",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "");
    }
}
