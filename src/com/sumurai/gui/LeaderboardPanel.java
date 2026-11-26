package com.sumurai.gui;

import com.sumurai.leaderboard.LeaderboardCategory;
import com.sumurai.leaderboard.LeaderboardEntry;
import com.sumurai.leaderboard.LeaderboardService;
import com.sumurai.model.UserProfile;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public final class LeaderboardPanel extends JPanel {

    // ===== COLORS =====
    private final Color NAVY = new Color(27, 42, 65);
    private final Color RED = new Color(145, 35, 35);
    private final Color GOLD = new Color(218, 165, 32);
    private final Color STAR_GOLD = new Color(184, 134, 11);
    private final Color CREAM = new Color(250, 235, 205);
    private final Color ROW_ALT = new Color(243, 224, 190);
    private final Color HIGHLIGHT = new Color(246, 219, 145);
    private final Color TAB = new Color(245, 222, 179);
    private final Color DARK = new Color(55, 45, 35);

    private static final class PlayerRecord {
        final String username;
        final int level;
        final int stars;
        final double accuracy;
        final double averageTime;
        final boolean accuracyAvailable;
        final boolean timingAvailable;

        PlayerRecord(LeaderboardEntry entry) {
            this.username = entry.getUsername();
            this.level = entry.getLevel();
            this.stars = entry.getStars();
            this.accuracy = entry.getAccuracy();
            this.averageTime = entry.getAverageTime();
            this.accuracyAvailable = entry.hasAccuracyData();
            this.timingAvailable = entry.hasTimingData();
        }
    }

    private final LeaderboardService service;
    private final String currentUsername;
    private final Runnable onBack;

    // The (up to) five rows currently shown in the table
    private List<PlayerRecord> shownPlayers = new ArrayList<>();

    private JTable leaderboardTable;
    private DefaultTableModel tableModel;

    private JLabel rankLabel;
    private JLabel playerInfoLabel;

    private JButton highestButton;
    private JButton accuracyButton;
    private JButton fastestButton;

    // ===== CONSTRUCTORS =====

    public LeaderboardPanel(List<UserProfile> profiles, Runnable onBack) {
        this(profiles, null, onBack);
    }

    public LeaderboardPanel(List<UserProfile> profiles, String currentUsername, Runnable onBack) {
        this.service = LeaderboardService.fromProfiles(profiles);
        this.currentUsername = currentUsername;
        this.onBack = Objects.requireNonNull(onBack, "onBack");

        setBackground(CREAM);
        setLayout(new BorderLayout(15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        displayLeaderboard(LeaderboardCategory.HIGHEST_LEVEL);
    }

    // ===== HEADER =====
    private JPanel createHeader() {

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setBackground(CREAM);
        header.setBorder(BorderFactory.createEmptyBorder(
                25, 20, 10, 20));

        // Plain text only: emoji can show as empty boxes in Swing
        JLabel gameTitle = new JLabel(
                "SUMURAI", SwingConstants.CENTER);

        gameTitle.setFont(new Font(
                "Serif", Font.BOLD | Font.ITALIC, 44));

        gameTitle.setForeground(RED);

        JLabel leaderboardTitle = new JLabel(
                "LEADERBOARD", SwingConstants.CENTER);

        leaderboardTitle.setFont(new Font(
                "Serif", Font.BOLD, 32));

        leaderboardTitle.setForeground(NAVY);

        header.add(gameTitle);
        header.add(leaderboardTitle);

        return header;
    }

    // ===== MAIN CONTENT =====
    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(10, 15));
        main.setBackground(CREAM);
        main.setBorder(BorderFactory.createEmptyBorder(
                5, 30, 10, 30));

        main.add(createCategoryButtons(), BorderLayout.NORTH);
        main.add(createLeaderboardTable(), BorderLayout.CENTER);

        return main;
    }

    // ===== CATEGORY BUTTONS =====
    private JPanel createCategoryButtons() {

        JPanel panel = new JPanel(
                new GridLayout(1, 3, 10, 0));

        panel.setBackground(CREAM);

        highestButton = createCategoryButton("Highest Level");
        accuracyButton = createCategoryButton("Best Accuracy");
        fastestButton = createCategoryButton("Fastest Average");

        highestButton.addActionListener(e ->
                displayLeaderboard(LeaderboardCategory.HIGHEST_LEVEL));

        accuracyButton.addActionListener(e ->
                displayLeaderboard(LeaderboardCategory.BEST_ACCURACY));

        fastestButton.addActionListener(e ->
                displayLeaderboard(LeaderboardCategory.FASTEST_AVERAGE));

        panel.add(highestButton);
        panel.add(accuracyButton);
        panel.add(fastestButton);

        return panel;
    }

    private JButton createCategoryButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font(
                "Serif", Font.BOLD, 18));

        button.setOpaque(true);
        button.setBackground(TAB);
        button.setForeground(DARK);

        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 48));

        button.setBorder(BorderFactory.createLineBorder(
                NAVY, 2));

        return button;
    }

    /** Active tab = deep red with white text; inactive = tan with dark text. */
    private void styleTab(JButton button, boolean selected) {

        button.setBackground(selected ? RED : TAB);
        button.setForeground(selected ? Color.WHITE : DARK);
    }

    // ===== LEADERBOARD TABLE =====
    private JScrollPane createLeaderboardTable() {

        String[] columns = {
                "Rank",
                "Player",
                "Level",
                "Stars",
                "Accuracy",
                "Average Time"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable = new JTable(tableModel);

        leaderboardTable.setRowHeight(48);
        leaderboardTable.setBackground(CREAM);
        leaderboardTable.setGridColor(new Color(180, 160, 130));
        leaderboardTable.setShowGrid(true);
        leaderboardTable.setRowSelectionAllowed(false);

        // TABLE HEADER
        JTableHeader header = leaderboardTable.getTableHeader();

        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        header.setFont(new Font(
                "Serif", Font.BOLD, 18));

        header.setPreferredSize(new Dimension(0, 50));

        // CELL RENDERER (centered text, medals, zebra rows, highlight)
        RowRenderer renderer = new RowRenderer();

        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {

            leaderboardTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        // PLAYER COLUMN
        leaderboardTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(230);

        JScrollPane scroll = new JScrollPane(leaderboardTable);

        // Fixes the white strip under the rows
        scroll.getViewport().setBackground(CREAM);
        scroll.setBorder(BorderFactory.createLineBorder(GOLD, 2));

        return scroll;
    }

    // ===== DISPLAY LEADERBOARD =====
    private void displayLeaderboard(LeaderboardCategory category) {
        List<PlayerRecord> sortedPlayers = new ArrayList<>();
        for (LeaderboardEntry entry : service.getRanked(category)) {
            sortedPlayers.add(new PlayerRecord(entry));
        }

        // UPDATE TABLE (top five only)
        tableModel.setRowCount(0);

        int limit = Math.min(5, sortedPlayers.size());
        shownPlayers = new ArrayList<>(sortedPlayers.subList(0, limit));

        for (int i = 0; i < limit; i++) {

            PlayerRecord player = shownPlayers.get(i);

            tableModel.addRow(new Object[]{

                    i + 1,   // the renderer turns ranks 1-3 into medals
                    player.username,
                    player.level,
                    starText(player),
                    accuracyText(player),
                    timeText(player)
            });
        }

        // UPDATE SELECTED CATEGORY
        styleTab(highestButton, category == LeaderboardCategory.HIGHEST_LEVEL);
        styleTab(accuracyButton, category == LeaderboardCategory.BEST_ACCURACY);
        styleTab(fastestButton, category == LeaderboardCategory.FASTEST_AVERAGE);

        updatePlayerRank(sortedPlayers);
    }

    private static String starText(int stars) {

        int filled = Math.max(0, Math.min(5, stars));

        return "\u2605".repeat(filled) + "\u2606".repeat(5 - filled);
    }

    private static String starText(PlayerRecord player) {
        return player.accuracyAvailable ? starText(player.stars) : "-";
    }

    private static String accuracyText(PlayerRecord player) {
        return player.accuracyAvailable
                ? String.format(Locale.ROOT, "%.1f%%", player.accuracy) : "-";
    }

    private static String timeText(PlayerRecord player) {
        return player.timingAvailable
                ? String.format(Locale.ROOT, "%.2f sec", player.averageTime) : "-";
    }

    // ===== PLAYER'S PERSONAL RANK =====
    private void updatePlayerRank(List<PlayerRecord> sortedPlayers) {

        int playerRank = -1;

        PlayerRecord currentPlayer = null;

        for (int i = 0; i < sortedPlayers.size(); i++) {

            if (sortedPlayers.get(i).username.equalsIgnoreCase(currentUsername)) {

                playerRank = i + 1;
                currentPlayer = sortedPlayers.get(i);

                break;
            }
        }

        rankLabel.setText("YOUR RANK");

        if (currentPlayer != null) {

            playerInfoLabel.setText(
                    "#" + playerRank
                    + "   \u2022   " + currentPlayer.username
                    + "   |   Level " + currentPlayer.level
                    + "   |   " + starText(currentPlayer)
                    + "   |   " + accuracyText(currentPlayer)
                    + "   |   " + timeText(currentPlayer)
            );

        } else {

            playerInfoLabel.setText(
                    "No leaderboard record available.");
        }
    }

    // ===== FOOTER / YOUR RANK =====
    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout(10, 10));

        footer.setBackground(CREAM);

        footer.setBorder(BorderFactory.createEmptyBorder(
                5, 30, 25, 30));

        JPanel rankPanel = new JPanel(new BorderLayout(5, 8));

        rankPanel.setBackground(CREAM);

        rankPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 3),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        rankLabel = new JLabel(
                "YOUR RANK", SwingConstants.CENTER);

        rankLabel.setFont(new Font(
                "Serif", Font.BOLD, 22));

        rankLabel.setForeground(NAVY);

        playerInfoLabel = new JLabel(
                "", SwingConstants.CENTER);

        playerInfoLabel.setFont(new Font(
                "Serif", Font.BOLD, 16));

        playerInfoLabel.setForeground(DARK);

        rankPanel.add(rankLabel, BorderLayout.NORTH);
        rankPanel.add(playerInfoLabel, BorderLayout.CENTER);

        // BACK BUTTON
        JButton backButton = new JButton("BACK");

        backButton.setFont(new Font(
                "Serif", Font.BOLD, 20));

        backButton.setOpaque(true);
        backButton.setBackground(RED);
        backButton.setForeground(Color.WHITE);

        backButton.setFocusPainted(false);

        backButton.setPreferredSize(new Dimension(150, 65));

        backButton.addActionListener(e -> {

            onBack.run();
        });

        footer.add(rankPanel, BorderLayout.CENTER);
        footer.add(backButton, BorderLayout.EAST);

        return footer;
    }

    // ===== TABLE CELL RENDERER =====
    private class RowRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(
                    table, value, false, false, row, column);

            setIcon(null);
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(DARK);
            setFont(new Font("Serif", Font.PLAIN, 18));
            setOpaque(true);

            boolean mine = currentUsername != null && row < shownPlayers.size()
                    && shownPlayers.get(row).username.equalsIgnoreCase(currentUsername);

            setBackground(mine ? HIGHLIGHT : (row % 2 == 0 ? CREAM : ROW_ALT));

            if (column == 0) {                       // Rank: medal for 1-3
                int rank = (Integer) value;
                if (rank <= 3) {
                    setText("");
                    setIcon(new MedalIcon(rank));
                }
            } else if (column == 1) {                // Player name
                setFont(new Font("Serif", Font.BOLD, 19));
            } else if (column == 3) {                // Stars in gold
                setForeground(STAR_GOLD);
            }

            return this;
        }
    }

    // ===== MEDAL ICON (gold / silver / bronze, drawn in code) =====
    private static class MedalIcon implements Icon {

        private static final int SIZE = 34;
        private final int rank;

        MedalIcon(int rank) {
            this.rank = rank;
        }

        @Override public int getIconWidth()  { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color fill, rim, text;

            if (rank == 1) {
                fill = new Color(240, 190, 50);
                rim = new Color(154, 112, 16);
                text = new Color(90, 60, 0);
            } else if (rank == 2) {
                fill = new Color(205, 210, 220);
                rim = new Color(125, 131, 145);
                text = new Color(50, 55, 70);
            } else {
                fill = new Color(205, 130, 70);
                rim = new Color(122, 68, 20);
                text = new Color(255, 242, 225);
            }

            g2.setColor(fill);
            g2.fillOval(x, y, SIZE - 1, SIZE - 1);

            g2.setColor(rim);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(x + 1, y + 1, SIZE - 3, SIZE - 3);

            g2.setColor(text);
            g2.setFont(new Font("Serif", Font.BOLD, 20));

            FontMetrics fm = g2.getFontMetrics();
            String number = String.valueOf(rank);

            g2.drawString(number,
                    x + (SIZE - fm.stringWidth(number)) / 2,
                    y + (SIZE + fm.getAscent() - fm.getDescent()) / 2);

            g2.dispose();
        }
    }

}
