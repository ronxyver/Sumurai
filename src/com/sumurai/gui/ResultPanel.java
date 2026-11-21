// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BattleOutcome;
import com.sumurai.model.BattleStats;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Objects;

/** Presents the completed-battle outcome, metrics, and next-step actions. */
public final class ResultPanel extends JPanel {
    private static final Color CARD_COLOR = new Color(225, 209, 177);
    private static final Color INK_COLOR = new Color(42, 34, 29);
    private static final Color BORDER_COLOR = new Color(91, 61, 43);

    public ResultPanel(BattleOutcome outcome, BattleStats stats,
                       Runnable onHome, Runnable onDashboard, Runnable onLeaderboard) {
        this(outcome, stats, emptyMetrics(),
                "HOME", "DASHBOARD", "LEADERBOARD",
                onHome, onDashboard, onLeaderboard, true);
    }

    public ResultPanel(BattleOutcome outcome, BattleStats stats, BattleResultMetrics metrics,
                       Runnable onNextLevel, Runnable onPlayAgain, Runnable onHome,
                       boolean canAdvance) {
        this(outcome, stats, metrics,
                canAdvance ? "NEXT LEVEL" : outcome == BattleOutcome.PLAYER_WON
                        ? "STAGE COMPLETE" : "RETRY LEVEL",
                "PLAY AGAIN", "HOME",
                onNextLevel, onPlayAgain, onHome,
                canAdvance || outcome == BattleOutcome.OPPONENT_WON);
    }

    private ResultPanel(BattleOutcome outcome, BattleStats stats, BattleResultMetrics metrics,
                        String primaryLabel, String secondaryLabel, String homeLabel,
                        Runnable onPrimary, Runnable onSecondary, Runnable onHome,
                        boolean primaryEnabled) {
        Objects.requireNonNull(outcome, "outcome");
        if (outcome == BattleOutcome.IN_PROGRESS) {
            throw new IllegalArgumentException("A completed battle is required.");
        }
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(onPrimary, "onPrimary");
        Objects.requireNonNull(onSecondary, "onSecondary");
        Objects.requireNonNull(onHome, "onHome");

        setBackground(ThemeManager.backgroundColor());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_COLOR, 2),
                BorderFactory.createEmptyBorder(22, 38, 28, 38)));
        setLayout(new BorderLayout(0, 22));

        JLabel title = new JLabel(outcome == BattleOutcome.PLAYER_WON ? "VICTORY" : "DEFEAT");
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setForeground(outcome == BattleOutcome.PLAYER_WON
                ? new Color(223, 190, 105) : new Color(214, 111, 91));
        title.setFont(ThemeManager.headingFont(38));

        JLabel record = new JLabel("Wins: " + stats.getWins() + "     Losses: "
                + stats.getLosses());
        record.setHorizontalAlignment(JLabel.CENTER);
        record.setForeground(ThemeManager.textColor());

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(record);
        add(heading, BorderLayout.NORTH);

        JPanel metricsGrid = new JPanel(new GridLayout(2, 2, 18, 18));
        metricsGrid.setOpaque(false);
        metricsGrid.add(createMetricCard("Time Left",
                formatTime(metrics.getSecondsRemaining()), BATTLE_SECONDS,
                metrics.getSecondsRemaining()));
        metricsGrid.add(createMetricCard("Accuracy",
                metrics.getAccuracyPercent() + "%  ("
                        + metrics.getCorrectAnswers() + "/" + metrics.getAnsweredTurns() + ")",
                100, metrics.getAccuracyPercent()));
        long averageMillis = metrics.getAverageAnswerTimeMillis();
        metricsGrid.add(createMetricCard("Average Answer Time",
                String.format(java.util.Locale.ROOT, "%.1f s", averageMillis / 1000.0),
                BATTLE_SECONDS * 1000, (int) Math.min(BATTLE_SECONDS * 1000L, averageMillis)));
        metricsGrid.add(createMetricCard("Longest Streak",
                metrics.getLongestStreak() + " answers",
                Math.max(1, metrics.getAnsweredTurns()), metrics.getLongestStreak()));
        add(metricsGrid, BorderLayout.CENTER);

        JButton primaryButton = createButton(primaryLabel);
        JButton secondaryButton = createButton(secondaryLabel);
        JButton homeButton = createButton(homeLabel);
        primaryButton.setEnabled(primaryEnabled);
        primaryButton.addActionListener(event -> onPrimary.run());
        secondaryButton.addActionListener(event -> onSecondary.run());
        homeButton.addActionListener(event -> onHome.run());

        JPanel actions = new JPanel(new GridLayout(1, 3, 18, 0));
        actions.setOpaque(false);
        actions.add(primaryButton);
        actions.add(secondaryButton);
        actions.add(homeButton);
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel createMetricCard(String title, String value, int maximum, int progress) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel label = new JLabel(title);
        label.setForeground(INK_COLOR);
        label.setFont(ThemeManager.headingFont(17));
        card.add(label, BorderLayout.NORTH);

        JProgressBar bar = new JProgressBar(0, Math.max(1, maximum));
        bar.setValue(Math.min(Math.max(0, progress), Math.max(1, maximum)));
        bar.setString(value);
        bar.setStringPainted(true);
        bar.setForeground(new Color(126, 67, 47));
        bar.setBackground(new Color(193, 184, 166));
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setPreferredSize(new Dimension(0, 28));
        card.add(bar, BorderLayout.CENTER);
        return card;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(INK_COLOR);
        button.setBackground(CARD_COLOR);
        button.setFont(ThemeManager.headingFont(16));
        button.setFocusPainted(false);
        button.setMargin(new Insets(10, 12, 10, 12));
        button.setPreferredSize(new Dimension(0, 56));
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        return button;
    }

    private static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private static BattleResultMetrics emptyMetrics() {
        return new BattleResultMetrics(0, 0, 0, 0, 0);
    }

    private static final int BATTLE_SECONDS = 60;
}
