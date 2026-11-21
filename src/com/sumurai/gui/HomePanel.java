// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BattleStats;
import com.sumurai.model.UserProfile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Objects;
import java.util.function.IntConsumer;

/** Three-panel home screen containing journey navigation, stages, and profile widgets. */
public final class HomePanel extends JPanel {
    private static final Color SIDEBAR_COLOR = new Color(20, 21, 39);
    private static final Color CARD_COLOR = new Color(39, 42, 63);
    private static final Color SELECTED_COLOR = new Color(64, 83, 145);
    private static final Color MUTED_COLOR = new Color(185, 190, 210);

    public HomePanel(UserProfile profile, Runnable onPlay, Runnable onDashboard,
                     Runnable onLeaderboard) {
        this(profile, level -> onPlay.run(), onPlay, onDashboard, onLeaderboard,
                () -> { });
    }

    public HomePanel(UserProfile profile, IntConsumer onLevelSelected, Runnable onPractice,
                     Runnable onDashboard, Runnable onLeaderboard, Runnable onHistory) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(onLevelSelected, "onLevelSelected");
        Objects.requireNonNull(onPractice, "onPractice");
        Objects.requireNonNull(onDashboard, "onDashboard");
        Objects.requireNonNull(onLeaderboard, "onLeaderboard");
        Objects.requireNonNull(onHistory, "onHistory");

        setBackground(ThemeManager.backgroundColor());
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setLayout(new BorderLayout(14, 0));
        add(createSidebar(onPractice, onDashboard, onLeaderboard, onHistory), BorderLayout.WEST);
        add(createJourney(profile, onLevelSelected), BorderLayout.CENTER);
        add(createWidgets(profile), BorderLayout.EAST);
    }

    private JPanel createSidebar(Runnable onPractice, Runnable onDashboard,
                                 Runnable onLeaderboard, Runnable onHistory) {
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 9));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        sidebar.setPreferredSize(new Dimension(174, 0));

        String[] labels = {"JOURNEY", "PRACTICE", "HISTORY", "LEADERBOARD", "PROFILE"};
        for (int index = 0; index < labels.length; index++) {
            final String label = labels[index];
            JButton button = createNavigationButton(label, index == 0);
            button.addActionListener(event -> {
                switch (label) {
                    case "JOURNEY":
                        break;
                    case "PRACTICE":
                        onPractice.run();
                        break;
                    case "HISTORY":
                        onHistory.run();
                        break;
                    case "LEADERBOARD":
                        onLeaderboard.run();
                        break;
                    case "PROFILE":
                        onDashboard.run();
                        break;
                    default:
                        throw new AssertionError(label);
                }
            });
            sidebar.add(button);
        }
        return sidebar;
    }

    private JPanel createJourney(UserProfile profile, IntConsumer onLevelSelected) {
        JPanel journey = new JPanel(new BorderLayout(0, 14));
        journey.setOpaque(false);
        journey.add(createChapterCard(profile), BorderLayout.NORTH);

        JPanel pathCard = new JPanel(new BorderLayout());
        pathCard.setBackground(CARD_COLOR);
        pathCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 80, 110), 1),
                BorderFactory.createEmptyBorder(18, 18, 12, 18)));
        pathCard.add(new LevelPathPanel(profile, onLevelSelected), BorderLayout.CENTER);
        journey.add(pathCard, BorderLayout.CENTER);
        return journey;
    }

    private JPanel createChapterCard(UserProfile profile) {
        JPanel chapter = new JPanel(new BorderLayout(0, 8));
        chapter.setBackground(CARD_COLOR);
        chapter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 80, 110), 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel heading = new JLabel("Chapter Introduction", SwingConstants.CENTER);
        heading.setForeground(ThemeManager.textColor());
        heading.setFont(ThemeManager.headingFont(25));
        JLabel description = new JLabel(
                "Welcome, " + profile.getUsername() + ". Win each battle to unlock the next stage.",
                SwingConstants.CENTER);
        description.setForeground(MUTED_COLOR);
        chapter.add(heading, BorderLayout.NORTH);
        chapter.add(description, BorderLayout.CENTER);
        return chapter;
    }

    private JPanel createWidgets(UserProfile profile) {
        JPanel widgets = new JPanel(new GridLayout(2, 1, 0, 12));
        widgets.setOpaque(false);
        widgets.setPreferredSize(new Dimension(224, 0));
        widgets.add(createLevelWidget(profile));
        widgets.add(createStatsWidget(profile.getStats()));
        return widgets;
    }

    private JPanel createLevelWidget(UserProfile profile) {
        JPanel card = createWidgetCard("Player Level / XP");
        JLabel level = new JLabel(profile.getLevel() >= UserProfile.MAX_PLAYER_LEVEL
                ? "LEVEL 100 - MAX" : "LEVEL " + profile.getLevel());
        level.setForeground(ThemeManager.textColor());
        level.setFont(ThemeManager.headingFont(19));
        card.add(level, BorderLayout.CENTER);

        JProgressBar experience = new JProgressBar(0, 100);
        if (profile.getLevel() >= UserProfile.MAX_PLAYER_LEVEL) {
            experience.setValue(100);
            experience.setString("MAX LEVEL");
        } else {
            long xpAtLevelStart = (long) (profile.getLevel() - 1) * 100L;
            long xpIntoLevel = Math.max(0, profile.getExperience() - xpAtLevelStart);
            experience.setValue((int) Math.min(100, xpIntoLevel));
            experience.setString(Math.min(100, xpIntoLevel) + " / 100 XP");
        }
        experience.setStringPainted(true);
        experience.setForeground(ThemeManager.accentColor());
        experience.setBackground(SIDEBAR_COLOR);
        card.add(experience, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createStatsWidget(BattleStats stats) {
        JPanel card = createWidgetCard("Profile Stats");
        JPanel lines = new JPanel(new GridLayout(3, 1, 0, 5));
        lines.setOpaque(false);
        lines.add(createStatLine("Battles", Integer.toString(stats.getBattlesPlayed())));
        lines.add(createStatLine("Wins", Integer.toString(stats.getWins())));
        lines.add(createStatLine("Correct answers", Integer.toString(stats.getCorrectAnswers())));
        card.add(lines, BorderLayout.CENTER);
        return card;
    }

    private JLabel createStatLine(String title, String value) {
        JLabel label = new JLabel(title + ": " + value);
        label.setForeground(MUTED_COLOR);
        return label;
    }

    private JPanel createWidgetCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 80, 110), 1),
                BorderFactory.createEmptyBorder(13, 14, 13, 14)));
        JLabel heading = new JLabel(title);
        heading.setForeground(ThemeManager.textColor());
        heading.setFont(ThemeManager.headingFont(16));
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    private JButton createNavigationButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(ThemeManager.textColor());
        button.setBackground(selected ? SELECTED_COLOR : CARD_COLOR);
        button.setFont(ThemeManager.headingFont(13));
        button.setMargin(new Insets(8, 12, 8, 6));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(76, 80, 110), 1));
        return button;
    }
}
