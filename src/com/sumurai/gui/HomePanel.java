// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.UserProfile;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class HomePanel extends JPanel {
    public HomePanel(UserProfile profile, Runnable onPlay, Runnable onDashboard, Runnable onLeaderboard) {
        setBackground(ThemeManager.backgroundColor());
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(12, 10, 12, 10);

        JLabel title = new JLabel("Welcome, " + profile.getUsername());
        title.setFont(ThemeManager.headingFont(30));
        title.setForeground(ThemeManager.textColor());
        add(title, constraints);

        JButton levelOne = new JButton("LEVEL 1 — Apprentices' Dojo");
        JButton dashboard = new JButton("DASHBOARD");
        JButton leaderboard = new JButton("LEADERBOARD");
        style(levelOne);
        style(dashboard);
        style(leaderboard);

        constraints.gridy++;
        add(levelOne, constraints);
        constraints.gridy++;
        add(dashboard, constraints);
        constraints.gridy++;
        add(leaderboard, constraints);

        levelOne.addActionListener(event -> onPlay.run());
        dashboard.addActionListener(event -> onDashboard.run());
        leaderboard.addActionListener(event -> onLeaderboard.run());
    }

    private void style(JButton button) {
        button.setBackground(ThemeManager.accentColor());
        button.setForeground(ThemeManager.textColor());
        button.setFocusPainted(false);
        button.setPreferredSize(new java.awt.Dimension(280, 46));
    }
}
