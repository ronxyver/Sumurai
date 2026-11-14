// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BattleStats;
import com.sumurai.model.UserProfile;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public final class LeaderboardPanel extends JPanel {
    public LeaderboardPanel(List<UserProfile> profiles, Runnable onBack) {
        setBackground(ThemeManager.backgroundColor());
        setLayout(new BorderLayout(10, 10));
        JLabel title = new JLabel("LEADERBOARD");
        title.setForeground(ThemeManager.textColor());
        title.setFont(ThemeManager.headingFont(30));
        add(title, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(Math.max(1, profiles.size()), 1, 4, 4));
        rows.setOpaque(false);
        if (profiles.isEmpty()) {
            addRow(rows, "No saved players yet.");
        } else {
            int rank = 1;
            for (UserProfile profile : profiles) {
                BattleStats stats = profile.getStats();
                addRow(rows, rank++ + ". " + profile.getUsername() + " — "
                        + stats.getWins() + " wins — level " + profile.getLevel());
            }
        }
        add(rows, BorderLayout.CENTER);
        JButton backButton = new JButton("BACK TO HOME");
        backButton.addActionListener(event -> onBack.run());
        add(backButton, BorderLayout.SOUTH);
    }

    private void addRow(JPanel rows, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemeManager.textColor());
        rows.add(label);
    }
}
