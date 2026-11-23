// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.engine.BattleEngine;
import com.sumurai.model.BattleOutcome;
import com.sumurai.model.InvalidAnswerException;
import com.sumurai.model.Move;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BattlePanel extends JPanel {
    private static final int BATTLE_SECONDS = 60;
    private static final Color PANEL_COLOR = new Color(34, 30, 29);
    private static final Color CARD_COLOR = new Color(225, 209, 177);
    private static final Color INK_COLOR = new Color(42, 34, 29);

    private final BattleEngine battleEngine;
    private final BiConsumer<BattleOutcome, BattleResultMetrics> onFinished;
    private final Runnable onMenu;
    private final JProgressBar playerHealth = new JProgressBar();
    private final JProgressBar opponentHealth = new JProgressBar();
    private final JLabel playerShield = new JLabel();
    private final JLabel opponentShield = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Choose a move to answer the math problem.");
    private final JButton[] moveButtons = new JButton[6];
    private final JTextArea battleLog = new JTextArea();
    private final Timer timer;
    private final BattleArenaPanel arena;
    private int secondsRemaining = BATTLE_SECONDS;
    private int answeredTurns;
    private int correctAnswers;
    private int currentCorrectStreak;
    private int longestCorrectStreak;
    private long totalAnswerTimeMillis;
    private boolean finished;

    public BattlePanel(BattleEngine engine, Consumer<BattleOutcome> onFinished) {
        this(engine, onFinished, () -> { });
    }

    public BattlePanel(BattleEngine engine, Consumer<BattleOutcome> onFinished, Runnable onMenu) {
        this(engine, (outcome, metrics) -> onFinished.accept(outcome), onMenu);
    }

    public BattlePanel(BattleEngine engine,
                       BiConsumer<BattleOutcome, BattleResultMetrics> onFinished,
                       Runnable onMenu) {
        this.battleEngine = Objects.requireNonNull(engine, "engine");
        this.onFinished = Objects.requireNonNull(onFinished, "onFinished");
        this.onMenu = Objects.requireNonNull(onMenu, "onMenu");
        setBackground(PANEL_COLOR);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        setLayout(new BorderLayout(10, 10));

        arena = new BattleArenaPanel(battleEngine.getOpponent().getName());
        add(createHeader(), BorderLayout.NORTH);
        add(createCombatArea(), BorderLayout.CENTER);
        add(createActionArea(), BorderLayout.SOUTH);

        timer = new Timer(1000, event -> tickTimer());
        refreshBattleView();
        appendLog("Battle started against " + battleEngine.getOpponent().getName() + ".");
        timer.start();
    }

    public void refreshBattleView() {
        playerHealth.setValue(battleEngine.getPlayer().getHealth());
        playerHealth.setString(battleEngine.getPlayer().getHealth() + " / "
                + battleEngine.getPlayer().getMaxHealth() + " HP");
        opponentHealth.setValue(battleEngine.getOpponent().getHealth());
        opponentHealth.setString(battleEngine.getOpponent().getHealth() + " / "
                + battleEngine.getOpponent().getMaxHealth() + " HP");
        playerShield.setText("Shield: " + battleEngine.getPlayer().getShieldPoints());
        opponentShield.setText("Shield: " + battleEngine.getOpponent().getShieldPoints());
        timerLabel.setText(String.format("%02d:%02d",
                secondsRemaining / 60, secondsRemaining % 60));
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        timerLabel.setForeground(CARD_COLOR);
        timerLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        header.add(timerLabel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 2, 8, 0));
        controls.setOpaque(false);
        JButton forfeitButton = createHeaderButton("Forfeit");
        forfeitButton.addActionListener(event -> confirmForfeit());
        JButton menuButton = createHeaderButton("Menu");
        menuButton.addActionListener(event -> confirmLeave());
        controls.add(forfeitButton);
        controls.add(menuButton);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel createCombatArea() {
        JPanel combat = new JPanel(new BorderLayout(12, 0));
        combat.setOpaque(false);

        combat.add(createCharacterCard(battleEngine.getPlayer().getName(),
                battleEngine.getPlayer().getMaxHealth(), playerHealth, playerShield),
                BorderLayout.WEST);
        arena.setPreferredSize(new Dimension(400, 280));
        combat.add(arena, BorderLayout.CENTER);
        combat.add(createCharacterCard(battleEngine.getOpponent().getName(),
                battleEngine.getOpponent().getMaxHealth(), opponentHealth, opponentShield),
                BorderLayout.EAST);

        return combat;
    }

    private JPanel createCharacterCard(String name, int maximumHealth,
                                       JProgressBar healthBar, JLabel shieldLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(91, 61, 43), 3),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        card.setPreferredSize(new Dimension(200, 220));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(INK_COLOR);
        nameLabel.setFont(ThemeManager.headingFont(17));
        card.add(nameLabel, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(3, 1, 0, 10));
        stats.setOpaque(false);
        configureHealthBar(healthBar, maximumHealth);
        stats.add(healthBar);
        shieldLabel.setForeground(INK_COLOR);
        stats.add(shieldLabel);
        JLabel status = new JLabel("Status: Ready");
        status.setForeground(INK_COLOR);
        stats.add(status);
        card.add(stats, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActionArea() {
        JPanel moves = new JPanel(new GridLayout(2, 3, 12, 10));
        moves.setOpaque(false);
        String[] names = {"Basic Attack", "Special Attack", "Block", "Heal", "Buff", "Debuff"};
        List<Move> availableMoves = BattleEngine.createStandardMoves();
        for (int index = 0; index < names.length; index++) {
            final Move move = availableMoves.get(index);
            JButton button = createMoveButton(names[index], move);
            button.addActionListener(event -> askForAnswer(move));
            moveButtons[index] = button;
            moves.add(button);
        }
        JPanel logPanel = new JPanel(new BorderLayout(0, 6));
        logPanel.setBackground(CARD_COLOR);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(91, 61, 43), 3),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        logPanel.setPreferredSize(new Dimension(300, 0));
        JLabel logTitle = new JLabel("BATTLE LOG");
        logTitle.setForeground(INK_COLOR);
        logTitle.setFont(ThemeManager.headingFont(15));
        logPanel.add(logTitle, BorderLayout.NORTH);

        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setForeground(INK_COLOR);
        battleLog.setBackground(CARD_COLOR);
        battleLog.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logScroll.getViewport().setBackground(CARD_COLOR);
        logPanel.add(logScroll, BorderLayout.CENTER);
        statusLabel.setForeground(CARD_COLOR);
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.setPreferredSize(new Dimension(0, 226));
        bottom.add(statusLabel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, moves, logPanel);
        split.setResizeWeight(0.68);
        split.setDividerSize(8);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        bottom.add(split, BorderLayout.CENTER);
        return bottom;
    }

    private void askForAnswer(Move move) {
        if (finished) {
            return;
        }

        long promptStartedAt = System.nanoTime();
        String answer = MathPromptDialog.show(
                this, battleEngine.getCurrentProblem().getPrompt());
        if (answer == null) {
            arena.playPlayerHurt();
            statusLabel.setText("Answer cancelled. " + move.getName() + " was not used.");
            appendLog("Answer cancelled; no turn was taken.");
            return;
        }

        long answerTimeMillis = (System.nanoTime() - promptStartedAt) / 1_000_000L;
        boolean answerWasCorrect = false;
        try {
            int numericAnswer = Integer.parseInt(answer.trim());
            answerWasCorrect = battleEngine.getCurrentProblem().isCorrect(numericAnswer);
        } catch (NumberFormatException exception) {
            // BattleEngine reports malformed answers without consuming a turn.
        }
        submitAnswer(answer, move, answerWasCorrect, answerTimeMillis);
    }

    private void submitAnswer(String answer, Move move, boolean answerWasCorrect,
                              long answerTimeMillis) {
        if (finished) {
            return;
        }
        try {
            BattleOutcome outcome = battleEngine.submitAnswer(answer, move);
            recordAnswer(answerWasCorrect, answerTimeMillis);
            if (answerWasCorrect) {
                arena.playPlayerMove(move);
                statusLabel.setText("Correct! " + move.getName() + " used.");
                appendLog("Correct answer. " + move.getName() + " used.");
            } else {
                arena.playPlayerHurt();
                statusLabel.setText("Incorrect answer. " + move.getName() + " was not used.");
                appendLog("Incorrect answer. " + battleEngine.getOpponent().getName()
                        + " attacked.");
            }
            refreshBattleView();
            if (outcome != BattleOutcome.IN_PROGRESS) {
                finish(outcome);
            }
        } catch (InvalidAnswerException exception) {
            arena.playPlayerHurt();
            statusLabel.setText(exception.getMessage());
            appendLog("Invalid answer; no turn was taken.");
        } catch (IllegalStateException exception) {
            finish(battleEngine.getOutcome());
        }
    }

    private void tickTimer() {
        secondsRemaining--;
        refreshBattleView();
        if (secondsRemaining <= 0) {
            appendLog("Time expired.");
            finish(battleEngine.endBattleByTimeout());
        }
    }

    private void finish(BattleOutcome outcome) {
        if (finished) {
            return;
        }
        finished = true;
        timer.stop();
        for (JButton button : moveButtons) {
            button.setEnabled(false);
        }
        appendLog(outcome == BattleOutcome.PLAYER_WON ? "Victory!" : "Defeat.");
        onFinished.accept(outcome, new BattleResultMetrics(secondsRemaining, answeredTurns,
                correctAnswers, totalAnswerTimeMillis, longestCorrectStreak));
    }

    private void recordAnswer(boolean correct, long answerTimeMillis) {
        answeredTurns++;
        totalAnswerTimeMillis += answerTimeMillis;
        if (correct) {
            correctAnswers++;
            currentCorrectStreak++;
            longestCorrectStreak = Math.max(longestCorrectStreak, currentCorrectStreak);
        } else {
            currentCorrectStreak = 0;
        }
    }

    private void confirmForfeit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Forfeit this battle?", "Forfeit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION && !finished) {
            appendLog("You forfeited the battle.");
            finish(battleEngine.endBattleByTimeout());
        }
    }

    private void confirmLeave() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Leave the battle and return to the menu? This battle will not be recorded.",
                "Return to menu", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION && !finished) {
            finished = true;
            timer.stop();
            onMenu.run();
        }
    }

    private void appendLog(String message) {
        battleLog.append(message + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(CARD_COLOR);
        button.setBackground(new Color(82, 57, 43));
        button.setFocusPainted(false);
        return button;
    }

    private JButton createMoveButton(String text, Move move) {
        JButton button = new JButton(text);
        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setIcon(arena.moveIcon(move));
        button.setIconTextGap(4);
        button.setForeground(INK_COLOR);
        button.setBackground(CARD_COLOR);
        button.setFont(ThemeManager.headingFont(14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(91, 61, 43), 3),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return button;
    }

    private void configureHealthBar(JProgressBar progressBar, int maximum) {
        progressBar.setMinimum(0);
        progressBar.setMaximum(maximum);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(125, 45, 37));
        progressBar.setBackground(new Color(195, 177, 147));
        progressBar.setPreferredSize(new Dimension(160, 24));
    }
}
