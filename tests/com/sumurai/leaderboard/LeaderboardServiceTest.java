package com.sumurai.leaderboard;

import com.sumurai.engine.BattleEngine;
import com.sumurai.engine.ProblemGenerator;
import com.sumurai.gui.LeaderboardPanel;
import com.sumurai.model.Apprentice;
import com.sumurai.model.BasicAttack;
import com.sumurai.model.BattleStats;
import com.sumurai.model.InvalidAnswerException;
import com.sumurai.model.Player;
import com.sumurai.model.UserProfile;
import com.sumurai.storage.UserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public final class LeaderboardServiceTest {
    private LeaderboardServiceTest() {
    }

    public static void main(String[] args) throws Exception {
        recordsValidAnswersAndTiming();
        ranksProfilesWithoutMetricsLast();
        readsAndMigratesLegacyProfiles();
        constructsLeaderboardPanel();
    }

    private static void recordsValidAnswersAndTiming() throws InvalidAnswerException {
        UserProfile profile = UserProfile.guest();
        BattleEngine engine = new BattleEngine(new ProblemGenerator());
        engine.startBattle(new Player(profile, 100), new Apprentice());

        try {
            engine.submitAnswer("invalid", new BasicAttack(1));
            throw new AssertionError("Malformed answer should be rejected.");
        } catch (InvalidAnswerException expected) {
            assertEquals(0, profile.getStats().getMeasuredAnswers());
        }

        int incorrectAnswer = engine.getCurrentProblem().getCorrectAnswer() + 1;
        engine.submitAnswer(Integer.toString(incorrectAnswer), new BasicAttack(1));
        int correctAnswer = engine.getCurrentProblem().getCorrectAnswer();
        engine.submitAnswer(Integer.toString(correctAnswer), new BasicAttack(1));

        BattleStats stats = profile.getStats();
        assertEquals(2, stats.getMeasuredAnswers());
        assertEquals(1, stats.getMeasuredCorrectAnswers());
        assertEquals(50.0, stats.getAccuracy());
        if (stats.getTotalAnswerTimeMillis() < 2) {
            throw new AssertionError("Each valid answer should record elapsed time.");
        }
    }

    private static void ranksProfilesWithoutMetricsLast() {
        BattleStats measuredStats = new BattleStats();
        measuredStats.recordAnswer(true, 200);
        UserProfile measured = new UserProfile("Measured", "", 1, 0, measuredStats);
        UserProfile legacy = new UserProfile("Legacy", "", 5, 0, new BattleStats());
        LeaderboardService service = LeaderboardService.fromProfiles(Arrays.asList(legacy, measured));

        assertEquals("Measured", service.getTop(LeaderboardCategory.BEST_ACCURACY, 2)
                .get(0).getUsername());
        assertEquals("Measured", service.getTop(LeaderboardCategory.FASTEST_AVERAGE, 2)
                .get(0).getUsername());
        try {
            service.getTop(LeaderboardCategory.HIGHEST_LEVEL, -1);
            throw new AssertionError("Negative leaderboard counts should be rejected.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static void readsAndMigratesLegacyProfiles() throws IOException {
        Path directory = Files.createTempDirectory("sumurai-leaderboard-test");
        Path dataFile = directory.resolve("users.txt");
        try {
            Files.write(dataFile, Arrays.asList("LegacyUser\thash\t2\t100\t1\t1\t0\t7"),
                    StandardCharsets.UTF_8);
            UserRepository repository = new UserRepository(dataFile);
            UserProfile legacy = repository.findAllProfiles().get(0);
            assertEquals(7, legacy.getStats().getCorrectAnswers());
            assertEquals(0, legacy.getStats().getMeasuredAnswers());

            repository.save(legacy);
            List<String> storedLines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
            assertEquals(11, storedLines.get(0).split("\\t", -1).length);

            UserProfile migrated = new UserRepository(dataFile).findAllProfiles().get(0);
            assertEquals(7, migrated.getStats().getCorrectAnswers());
            assertEquals(0, migrated.getStats().getMeasuredAnswers());
        } finally {
            Files.deleteIfExists(dataFile);
            Files.deleteIfExists(directory);
        }
    }

    private static void constructsLeaderboardPanel() throws Exception {
        BattleStats highLevelStats = new BattleStats();
        highLevelStats.recordAnswer(false, 1000);
        UserProfile highLevel = new UserProfile("HighLevel", "", 8, 0, highLevelStats);

        BattleStats accuracyStats = new BattleStats();
        accuracyStats.recordAnswer(true, 250);
        UserProfile accurate = new UserProfile("Accurate", "", 2, 0, accuracyStats);
        AtomicBoolean returnedHome = new AtomicBoolean();

        SwingUtilities.invokeAndWait(() -> {
            LeaderboardPanel panel = new LeaderboardPanel(
                    Arrays.asList(accurate, highLevel), highLevel.getUsername(),
                    () -> returnedHome.set(true));
            JTable table = findComponent(panel, JTable.class);
            assertEquals(2, table.getRowCount());
            assertEquals("HighLevel", (String) table.getValueAt(0, 1));

            findButton(panel, "Best Accuracy").doClick();
            assertEquals("Accurate", (String) table.getValueAt(0, 1));
            findButton(panel, "BACK").doClick();
        });
        if (!returnedHome.get()) {
            throw new AssertionError("Back button should return to the home screen.");
        }
    }

    private static <T extends java.awt.Component> T findComponent(
            java.awt.Container container, Class<T> componentType) {
        for (java.awt.Component component : container.getComponents()) {
            if (componentType.isInstance(component)) {
                return componentType.cast(component);
            }
            if (component instanceof java.awt.Container) {
                T nested = findComponent((java.awt.Container) component, componentType);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static JButton findButton(java.awt.Container container, String text) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof java.awt.Container) {
                JButton nested = findButton((java.awt.Container) component, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private static void assertEquals(String expected, String actual) {
        if (actual == null || !expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private static void assertEquals(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}
