// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BattleOutcome;
import com.sumurai.model.BattleStats;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class ResultPanel extends JPanel {
    public ResultPanel(BattleOutcome outcome, BattleStats stats,
            Runnable onHome, Runnable onDashboard, Runnable onLeaderboard) {
        setBackground(ThemeManager.backgroundColor());
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);

        String titleText = outcome == BattleOutcome.PLAYER_WON ? "VICTORY" : "DEFEAT";
        JLabel title = new JLabel(titleText);
        title.setForeground(ThemeManager.textColor());
        title.setFont(ThemeManager.headingFont(38));
        add(title, constraints);

        constraints.gridy++;
        JLabel summary = new JLabel("Wins: " + stats.getWins() + "    Losses: " + stats.getLosses()
                + "    Correct answers: " + stats.getCorrectAnswers());
        summary.setForeground(ThemeManager.textColor());
        add(summary, constraints);

        JButton homeButton = new JButton("HOME");
        JButton dashboardButton = new JButton("DASHBOARD");
        JButton leaderboardButton = new JButton("LEADERBOARD");
        style(homeButton);
        style(dashboardButton);
        style(leaderboardButton);
        constraints.gridy++;
        add(homeButton, constraints);
        constraints.gridy++;
        add(dashboardButton, constraints);
        constraints.gridy++;
        add(leaderboardButton, constraints);
        homeButton.addActionListener(event -> onHome.run());
        dashboardButton.addActionListener(event -> onDashboard.run());
        leaderboardButton.addActionListener(event -> onLeaderboard.run());
    }

    private void style(JButton button) {
        button.setBackground(ThemeManager.accentColor());
        button.setForeground(ThemeManager.textColor());
        button.setFocusPainted(false);
        button.setPreferredSize(new java.awt.Dimension(230, 42));
    }
}
