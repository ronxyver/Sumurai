// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BattleStats;
import com.sumurai.model.UserProfile;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class DashboardPanel extends JPanel {
    public DashboardPanel(UserProfile profile, Runnable onBack) {
        setBackground(ThemeManager.backgroundColor());
        setLayout(new GridBagLayout());
        BattleStats stats = profile.getStats();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        addLine("DASHBOARD — " + profile.getUsername(), 30, constraints);
        constraints.gridy++;
        addLine("Level " + profile.getLevel() + "    Experience " + profile.getExperience(), 18, constraints);
        constraints.gridy++;
        addLine("Battles: " + stats.getBattlesPlayed(), 18, constraints);
        constraints.gridy++;
        addLine("Wins: " + stats.getWins() + "    Losses: " + stats.getLosses(), 18, constraints);
        constraints.gridy++;
        addLine("Correct answers: " + stats.getCorrectAnswers(), 18, constraints);
        constraints.gridy++;
        JButton backButton = new JButton("BACK TO HOME");
        backButton.addActionListener(event -> onBack.run());
        add(backButton, constraints);
    }

    private void addLine(String text, int size, GridBagConstraints constraints) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemeManager.textColor());
        label.setFont(ThemeManager.headingFont(size));
        add(label, constraints);
    }
}
