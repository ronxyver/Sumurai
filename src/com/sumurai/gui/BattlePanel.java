// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.engine.BattleEngine;
import com.sumurai.model.BattleOutcome;
import com.sumurai.model.BasicAttack;
import com.sumurai.model.Buff;
import com.sumurai.model.Debuff;
import com.sumurai.model.Heal;
import com.sumurai.model.InvalidAnswerException;
import com.sumurai.model.Move;
import com.sumurai.model.Shield;
import com.sumurai.model.SpecialAttack;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

public final class BattlePanel extends JPanel {
    private static final int BATTLE_SECONDS = 60;

    private final BattleEngine battleEngine;
    private final Consumer<BattleOutcome> onFinished;
    private final JProgressBar playerHealth = new JProgressBar();
    private final JProgressBar opponentHealth = new JProgressBar();
    private final JLabel timerLabel = new JLabel();
    private final JLabel questionLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Solve the problem, then choose a move.");
    private final JTextField answerField = new JTextField(12);
    private final JButton[] moveButtons = new JButton[6];
    private final Timer timer;
    private int secondsRemaining = BATTLE_SECONDS;
    private boolean finished;

    public BattlePanel(BattleEngine engine, Consumer<BattleOutcome> onFinished) {
        this.battleEngine = engine;
        this.onFinished = onFinished;
        setBackground(ThemeManager.backgroundColor());
        setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new GridLayout(3, 1, 8, 8));
        top.setOpaque(false);
        JLabel playerLabel = new JLabel("PLAYER");
        JLabel opponentLabel = new JLabel(battleEngine.getOpponent().getName());
        playerLabel.setForeground(ThemeManager.textColor());
        opponentLabel.setForeground(ThemeManager.textColor());
        configureHealthBar(playerHealth, battleEngine.getPlayer().getMaxHealth());
        configureHealthBar(opponentHealth, battleEngine.getOpponent().getMaxHealth());
        top.add(combine(playerLabel, playerHealth));
        top.add(combine(opponentLabel, opponentHealth));
        timerLabel.setForeground(ThemeManager.textColor());
        top.add(timerLabel);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 1, 8, 8));
        center.setOpaque(false);
        questionLabel.setFont(ThemeManager.headingFont(32));
        questionLabel.setForeground(ThemeManager.textColor());
        center.add(questionLabel);
        center.add(answerField);
        statusLabel.setForeground(ThemeManager.textColor());
        center.add(statusLabel);
        add(center, BorderLayout.CENTER);

        JPanel moves = new JPanel(new GridLayout(2, 3, 8, 8));
        moves.setOpaque(false);
        String[] names = {"Basic Attack", "Special Attack", "Heal", "Shield", "Buff", "Debuff"};
        for (int index = 0; index < names.length; index++) {
            final Move move = createMove(index);
            JButton button = new JButton(names[index]);
            button.setBackground(ThemeManager.accentColor());
            button.setForeground(ThemeManager.textColor());
            button.setFocusPainted(false);
            button.addActionListener(event -> submit(move));
            moveButtons[index] = button;
            moves.add(button);
        }
        add(moves, BorderLayout.SOUTH);
        answerField.addActionListener(event -> submit(new BasicAttack(12)));

        timer = new Timer(1000, event -> tickTimer());
        refreshBattleView();
        timer.start();
    }

    public void refreshBattleView() {
        if (battleEngine.getCurrentProblem() != null) {
            questionLabel.setText(battleEngine.getCurrentProblem().getPrompt());
        }
        playerHealth.setValue(battleEngine.getPlayer().getHealth());
        playerHealth.setString(battleEngine.getPlayer().getHealth() + " / "
                + battleEngine.getPlayer().getMaxHealth() + " HP");
        opponentHealth.setValue(battleEngine.getOpponent().getHealth());
        opponentHealth.setString(battleEngine.getOpponent().getHealth() + " / "
                + battleEngine.getOpponent().getMaxHealth() + " HP");
        timerLabel.setText("Time remaining: " + secondsRemaining + " seconds");
    }

    private void submit(Move move) {
        if (finished) {
            return;
        }
        try {
            BattleOutcome outcome = battleEngine.submitAnswer(answerField.getText(), move);
            answerField.setText("");
            statusLabel.setText(outcome == BattleOutcome.IN_PROGRESS
                    ? "Answer recorded. " + move.getName() + " selected."
                    : outcome == BattleOutcome.PLAYER_WON ? "Victory!" : "Defeat.");
            refreshBattleView();
            if (outcome != BattleOutcome.IN_PROGRESS) {
                finish(outcome);
            }
        } catch (InvalidAnswerException exception) {
            statusLabel.setText(exception.getMessage());
            answerField.requestFocusInWindow();
        } catch (IllegalStateException exception) {
            finish(battleEngine.getOutcome());
        }
    }

    private void tickTimer() {
        secondsRemaining--;
        refreshBattleView();
        if (secondsRemaining <= 0) {
            finish(battleEngine.endBattleByTimeout());
        }
    }

    private void finish(BattleOutcome outcome) {
        if (finished) {
            return;
        }
        finished = true;
        timer.stop();
        answerField.setEnabled(false);
        for (JButton button : moveButtons) {
            button.setEnabled(false);
        }
        onFinished.accept(outcome);
    }

    private Move createMove(int index) {
        switch (index) {
            case 0:
                return new BasicAttack(12);
            case 1:
                return new SpecialAttack(22);
            case 2:
                return new Heal(18);
            case 3:
                return new Shield(15);
            case 4:
                return new Buff(5, 3);
            case 5:
                return new Debuff(4, 3);
            default:
                throw new IllegalArgumentException("Unknown move index.");
        }
    }

    private void configureHealthBar(JProgressBar progressBar, int maximum) {
        progressBar.setMinimum(0);
        progressBar.setMaximum(maximum);
        progressBar.setStringPainted(true);
    }

    private JPanel combine(JLabel label, JProgressBar progressBar) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(label, BorderLayout.WEST);
        row.add(progressBar, BorderLayout.CENTER);
        return row;
    }
}
