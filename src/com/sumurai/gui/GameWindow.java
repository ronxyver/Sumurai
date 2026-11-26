// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.engine.BattleEngine;
import com.sumurai.engine.ProblemGenerator;
import com.sumurai.model.Apprentice;
import com.sumurai.model.BattleOutcome;
import com.sumurai.model.Opponent;
import com.sumurai.model.Player;
import com.sumurai.model.UserProfile;
import com.sumurai.storage.BattleHistoryRepository;
import com.sumurai.storage.UserRepository;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class GameWindow extends JFrame {
    private static final String LOGIN = "login";
    private static final String HOME = "home";
    private static final String BATTLE = "battle";
    private static final String RESULT = "result";
    private static final String DASHBOARD = "dashboard";
    private static final String LEADERBOARD = "leaderboard";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final Map<String, JPanel> screenByName = new HashMap<String, JPanel>();
    private final UserRepository userRepository;
    private final BattleHistoryRepository historyRepository;
    private UserProfile currentProfile;
    private BattleEngine currentBattle;

    public GameWindow(UserRepository users, BattleHistoryRepository history) {
        super("SUMURAI");
        this.userRepository = users;
        this.historyRepository = history;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(850, 620));
        setSize(950, 700);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(screens, BorderLayout.CENTER);
        showLogin();
    }

    public void showLogin() {
        showCard(LOGIN, new LoginPanel(userRepository, this::showHome));
    }

    public void showHome(UserProfile profile) {
        this.currentProfile = profile;
        showCard(HOME, new HomePanel(profile,
                () -> showBattle(new Apprentice()),
                this::showDashboard,
                this::showLeaderboard));
    }

    public void showBattle(UserProfile profile, Opponent opponent) {
        this.currentProfile = profile;
        startBattle(opponent);
    }

    public void showResults(BattleOutcome outcome) {
        if (currentProfile == null || currentBattle == null || outcome == BattleOutcome.IN_PROGRESS) {
            throw new IllegalStateException("A completed battle is required to show results.");
        }
        saveCompletedBattle(outcome);
        showCard(RESULT, new ResultPanel(outcome, currentProfile.getStats(),
                () -> showHome(currentProfile),
                this::showDashboard,
                this::showLeaderboard));
    }

    private void showBattle(Opponent opponent) {
        startBattle(opponent);
    }

    private void startBattle(Opponent opponent) {
        if (currentProfile == null) {
            throw new IllegalStateException("Choose a player before starting a battle.");
        }
        Player player = new Player(currentProfile, 100);
        currentBattle = new BattleEngine(new ProblemGenerator());
        currentBattle.startBattle(player, opponent);
        showCard(BATTLE, new BattlePanel(currentBattle, this::showResults));
    }

    private void showDashboard() {
        showCard(DASHBOARD, new DashboardPanel(currentProfile, () -> showHome(currentProfile)));
    }

    private void showLeaderboard() {
        JPanel loading = new JPanel(new BorderLayout());
        loading.setBackground(ThemeManager.backgroundColor());
        JLabel label = new JLabel("Loading leaderboard...");
        label.setForeground(ThemeManager.textColor());
        loading.add(label, BorderLayout.CENTER);
        showCard(LEADERBOARD, loading);

        SwingWorker<List<UserProfile>, Void> worker = new SwingWorker<List<UserProfile>, Void>() {
            @Override
            protected List<UserProfile> doInBackground() throws Exception {
                return userRepository.findAllProfiles();
            }

            @Override
            protected void done() {
                try {
                    showCard(LEADERBOARD,
                            new LeaderboardPanel(get(), currentProfile.getUsername(),
                                    () -> showHome(currentProfile)));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Loading the leaderboard was interrupted.");
                } catch (ExecutionException exception) {
                    showError(messageFor(exception));
                }
            }
        };
        worker.execute();
    }

    private void saveCompletedBattle(BattleOutcome outcome) {
        UserProfile completedProfile = currentProfile;
        int completedTurns = currentBattle.getTurns();
        Instant completedAt = Instant.now();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userRepository.save(completedProfile);
                historyRepository.appendResult(completedProfile.getUsername(), outcome,
                        completedTurns, completedAt);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Saving battle results was interrupted.");
                } catch (ExecutionException exception) {
                    showError(messageFor(exception));
                }
            }
        };
        worker.execute();
    }

    private void showCard(String name, JPanel panel) {
        JPanel previous = screenByName.put(name, panel);
        if (previous != null) {
            screens.remove(previous);
        }
        screens.add(panel, name);
        cardLayout.show(screens, name);
        screens.revalidate();
        screens.repaint();
    }

    private String messageFor(ExecutionException exception) {
        Throwable cause = exception.getCause();
        return cause == null ? exception.getMessage() : cause.getMessage();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Sumurai error", JOptionPane.ERROR_MESSAGE);
    }
}
