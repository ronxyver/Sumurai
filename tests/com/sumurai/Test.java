package com.sumurai;

import com.sumurai.engine.BattleEngine;
import com.sumurai.engine.ProblemGenerator;
import com.sumurai.gui.BattleArenaPanel;
import com.sumurai.gui.BattleResultMetrics;
import com.sumurai.gui.HomePanel;
import com.sumurai.model.Apprentice;
import com.sumurai.model.BasicAttack;
import com.sumurai.model.BattleOutcome;
import com.sumurai.model.Buff;
import com.sumurai.model.Debuff;
import com.sumurai.model.Heal;
import com.sumurai.model.InvalidAnswerException;
import com.sumurai.model.MathProblem;
import com.sumurai.model.Move;
import com.sumurai.model.Opponent;
import com.sumurai.model.Player;
import com.sumurai.model.Ronin;
import com.sumurai.model.Shogun;
import com.sumurai.model.SpecialAttack;
import com.sumurai.model.UserProfile;
import com.sumurai.storage.UserRepository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class Test {
    private Test() {
    }

    public static void main(String[] args) throws Exception {
        testMalformedAnswerDoesNotConsumeTurn();
        testWrongAnswerDamagesPlayer();
        testCorrectAnswerDamagesOpponent();
        testTimeoutEndsBattle();
        testFinishedBattleRejectsSubmission();
        testBuffAddsDamageAndExpires();
        testBuffIsPreservedAcrossNonAttackMoves();
        testDebuffReducesOpponentDamage();
        testWinningAnswerRecordsPlayerVictory();
        testBattleResultMetrics();
        testProblemsScaleWithLevel();
        testProgressionRequiresWinningCurrentStage();
        testLevelDamageMultiplier();
        testJourneyOpponentMapping();
        testStandardMoveCatalog();
        testSpecialAttackFramesRenderAtArenaScale();
        testLegacyProfilesMigrateWithStageProgress();
        testPlayerLevelStopsAt100();
        testJourneyPanelContainsThreeMainSectionsAnd100Stages();
        System.out.println("Battle tests passed.");
    }

    private static void testMalformedAnswerDoesNotConsumeTurn() throws Exception {
        BattleEngine engine = startBattle();
        int turnsBefore = engine.getTurns();

        expectInvalidAnswer(() -> engine.submitAnswer("not a number", new BasicAttack(12)));
        assertEquals(turnsBefore, engine.getTurns(), "Malformed answer consumed a turn.");
    }

    private static void testWrongAnswerDamagesPlayer() throws InvalidAnswerException {
        BattleEngine engine = startBattle();
        int healthBefore = engine.getPlayer().getHealth();
        MathProblem problem = engine.getCurrentProblem();

        engine.submitAnswer(Integer.toString(problem.getCorrectAnswer() + 1), new BasicAttack(12));

        assertTrue(engine.getPlayer().getHealth() < healthBefore,
                "Wrong answer did not damage the player.");
    }

    private static void testCorrectAnswerDamagesOpponent() throws InvalidAnswerException {
        BattleEngine engine = startBattle();
        int healthBefore = engine.getOpponent().getHealth();
        MathProblem problem = engine.getCurrentProblem();

        engine.submitAnswer(Integer.toString(problem.getCorrectAnswer()), new BasicAttack(12));

        assertEquals(healthBefore - 12, engine.getOpponent().getHealth(),
                "Correct answer did not apply the selected move.");
    }

    private static void testTimeoutEndsBattle() {
        BattleEngine engine = startBattle();

        BattleOutcome outcome = engine.endBattleByTimeout();

        assertEquals(BattleOutcome.OPPONENT_WON, outcome,
                "Timeout did not end the battle with an opponent victory.");
    }

    private static void testFinishedBattleRejectsSubmission() throws Exception {
        BattleEngine engine = startBattle();
        engine.endBattleByTimeout();

        expectIllegalState(() -> engine.submitAnswer("1", new BasicAttack(12)));
    }

    private static void testBuffAddsDamageAndExpires() throws InvalidAnswerException {
        BattleEngine engine = startBattle();
        MathProblem buffProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(buffProblem.getCorrectAnswer()), new Buff(5, 2));

        int healthBeforeAttack = engine.getOpponent().getHealth();
        MathProblem attackProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(attackProblem.getCorrectAnswer()), new BasicAttack(12));
        assertEquals(healthBeforeAttack - 17, engine.getOpponent().getHealth(),
                "Buff did not add bonus damage.");

        healthBeforeAttack = engine.getOpponent().getHealth();
        attackProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(attackProblem.getCorrectAnswer()), new BasicAttack(12));
        assertEquals(healthBeforeAttack - 17, engine.getOpponent().getHealth(),
                "Buff expired too early.");

        healthBeforeAttack = engine.getOpponent().getHealth();
        attackProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(attackProblem.getCorrectAnswer()), new BasicAttack(12));
        assertEquals(healthBeforeAttack - 12, engine.getOpponent().getHealth(),
                "Buff did not expire after its turn limit.");
    }

    private static void testBuffIsPreservedAcrossNonAttackMoves() throws InvalidAnswerException {
        BattleEngine engine = startBattle();
        MathProblem buffProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(buffProblem.getCorrectAnswer()), new Buff(5, 2));

        engine.getPlayer().takeDamage(20);
        MathProblem healProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(healProblem.getCorrectAnswer()), new Heal(18));

        int healthBeforeAttack = engine.getOpponent().getHealth();
        MathProblem attackProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(attackProblem.getCorrectAnswer()),
                new BasicAttack(12));
        assertEquals(healthBeforeAttack - 17, engine.getOpponent().getHealth(),
                "Non-attack move consumed the attack buff.");
    }

    private static void testDebuffReducesOpponentDamage() throws InvalidAnswerException {
        BattleEngine engine = startBattle();
        MathProblem debuffProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(debuffProblem.getCorrectAnswer()), new Debuff(4, 2));

        int healthBefore = engine.getPlayer().getHealth();
        MathProblem wrongProblem = engine.getCurrentProblem();
        engine.submitAnswer(Integer.toString(wrongProblem.getCorrectAnswer() + 1),
                new BasicAttack(12));
        assertEquals(healthBefore - 3, engine.getPlayer().getHealth(),
                "Debuff did not reduce opponent damage.");
    }

    private static void testWinningAnswerRecordsPlayerVictory() throws InvalidAnswerException {
        BattleEngine engine = new BattleEngine(new ProblemGenerator());
        Player player = new Player(UserProfile.guest(), 100);
        engine.startBattle(player, new Apprentice());

        BattleOutcome outcome = BattleOutcome.IN_PROGRESS;
        while (outcome == BattleOutcome.IN_PROGRESS) {
            outcome = engine.submitAnswer(
                    Integer.toString(engine.getCurrentProblem().getCorrectAnswer()),
                    new SpecialAttack(12));
        }

        assertEquals(BattleOutcome.PLAYER_WON, outcome,
                "Defeating the opponent did not produce a player victory.");
        assertEquals(1, player.getProfile().getStats().getWins(),
                "Player victory was not recorded.");
    }

    private static void testBattleResultMetrics() {
        BattleResultMetrics metrics = new BattleResultMetrics(42, 4, 3, 6400, 2);

        assertEquals(75, metrics.getAccuracyPercent(), "Battle accuracy was incorrect.");
        assertEquals(1600L, metrics.getAverageAnswerTimeMillis(),
                "Average answer time was incorrect.");
        assertEquals(42, metrics.getSecondsRemaining(), "Time remaining was incorrect.");
        assertEquals(2, metrics.getLongestStreak(), "Longest correct streak was incorrect.");
    }

    private static void testProblemsScaleWithLevel() {
        ProblemGenerator generator = new ProblemGenerator();
        for (int level = 1; level <= UserProfile.MAX_JOURNEY_STAGE; level++) {
            int operandLimit = level * 10;
            for (int attempt = 0; attempt < 5; attempt++) {
                MathProblem problem = generator.generateForLevel(level);
                assertTrue(problem.getLeftOperand() <= operandLimit
                                && problem.getRightOperand() <= operandLimit,
                        "Generated operand exceeded the selected level's bound.");
                if (problem.getOperation().getSymbol().equals("/")) {
                    assertEquals(0, problem.getLeftOperand() % problem.getRightOperand(),
                            "Generated division problem was not exact.");
                }
            }
            expectIllegalArgument(() -> generator.generateForLevel(101));
        }
    }

    private static void testProgressionRequiresWinningCurrentStage() throws Exception {
        UserProfile profile = UserProfile.guest();
        BattleEngine engine = new BattleEngine(new ProblemGenerator());
        Player player = new Player(profile, 100);
        engine.startBattle(player, new Apprentice(), 1);
        assertEquals(1, profile.getHighestUnlockedStage(),
                "Starting a stage unlocked the next stage.");
        engine.submitAnswer(Integer.toString(engine.getCurrentProblem().getCorrectAnswer()),
                new SpecialAttack(100));
        assertEquals(2, profile.getHighestUnlockedStage(),
                "Winning the current stage did not unlock the next stage.");

        engine.startPracticeBattle(new Player(profile, 100), new Ronin(), 2);
        engine.submitAnswer(Integer.toString(engine.getCurrentProblem().getCorrectAnswer()),
                new SpecialAttack(100));
        assertEquals(2, profile.getHighestUnlockedStage(),
                "Practice incorrectly unlocked a progression stage.");

        expectIllegalArgument(() -> engine.startBattle(
                new Player(profile, 100), new Ronin(), 3));
    }

    private static void testLevelDamageMultiplier() throws InvalidAnswerException {
        UserProfile profile = UserProfile.guest();
        int[] levels = {1, 25, 26, 60, 61, 100};
        int[] baseDamages = {7, 7, 11, 11, 15, 15};
        for (int index = 0; index < levels.length; index++) {
            int level = levels[index];
            while (profile.getHighestUnlockedStage() < level) {
                profile.recordStageVictory(profile.getHighestUnlockedStage());
            }
            Opponent opponent = level <= 25 ? new Apprentice()
                    : level <= 60 ? new Ronin() : new Shogun();
            BattleEngine engine = new BattleEngine(new ProblemGenerator());
            engine.startBattle(new Player(profile, 100), opponent, level);
            int answer = engine.getCurrentProblem().getCorrectAnswer() + 1;
            engine.submitAnswer(Integer.toString(answer), new BasicAttack(12));
            int expectedDamage = (int) Math.round(baseDamages[index]
                    * (1.0 + (level - 1) * 0.02));
            assertEquals(100 - expectedDamage, engine.getPlayer().getHealth(),
                    "Enemy damage multiplier was incorrect at level " + level + ".");
        }
    }

    private static void testJourneyOpponentMapping() {
        assertTrue(BattleEngine.createOpponentForLevel(1) instanceof Apprentice,
                "Level 1 should map to Apprentice.");
        assertTrue(BattleEngine.createOpponentForLevel(25) instanceof Apprentice,
                "Level 25 should map to Apprentice.");
        assertTrue(BattleEngine.createOpponentForLevel(26) instanceof Ronin,
                "Level 26 should map to Ronin.");
        assertTrue(BattleEngine.createOpponentForLevel(60) instanceof Ronin,
                "Level 60 should map to Ronin.");
        assertTrue(BattleEngine.createOpponentForLevel(61) instanceof Shogun,
                "Level 61 should map to Shogun.");
        assertTrue(BattleEngine.createOpponentForLevel(100) instanceof Shogun,
                "Level 100 should map to Shogun.");
        expectIllegalArgument(() -> BattleEngine.createOpponentForLevel(0));
        expectIllegalArgument(() -> BattleEngine.createOpponentForLevel(101));
    }

    private static void testStandardMoveCatalog() {
        List<Move> moves = BattleEngine.createStandardMoves();
        assertEquals(6, moves.size(), "Standard move catalog should include six moves.");
        assertEquals(12, moves.get(0).getPower(), "Basic Attack power changed.");
        assertEquals(22, moves.get(1).getPower(), "Special Attack power changed.");
        assertEquals(15, moves.get(2).getPower(), "Shield power changed.");
        assertEquals(18, moves.get(3).getPower(), "Heal power changed.");
        assertEquals(5, moves.get(4).getPower(), "Buff magnitude changed.");
        assertEquals(3, ((Buff) moves.get(4)).getRemainingTurns(), "Buff duration changed.");
        assertEquals(4, moves.get(5).getPower(), "Debuff magnitude changed.");
        assertEquals(3, ((Debuff) moves.get(5)).getRemainingTurns(), "Debuff duration changed.");
        try {
            moves.clear();
            throw new AssertionError("Standard move catalog must be immutable.");
        } catch (UnsupportedOperationException expected) {
            // Expected: callers should not mutate the engine's move catalog.
        }
    }

    private static void testSpecialAttackFramesRenderAtArenaScale() throws Exception {
        BattleArenaPanel arena = new BattleArenaPanel("Apprentice");
        javax.swing.JLayeredPane[] layers = new javax.swing.JLayeredPane[1];
        javax.swing.JPanel[] overlay = new javax.swing.JPanel[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            arena.setSize(400, 280);
            arena.doLayout();
            for (java.awt.Component component : arena.getComponents()) {
                if (component instanceof javax.swing.JLayeredPane) {
                    layers[0] = (javax.swing.JLayeredPane) component;
                    break;
                }
            }
            assertTrue(layers[0] != null, "Battle arena fighter layers were not found.");
            layers[0].doLayout();
            for (java.awt.Component component : layers[0].getComponents()) {
                if (component.getClass().getName().endsWith("$SpecialAttackOverlay")) {
                    overlay[0] = (javax.swing.JPanel) component;
                    break;
                }
            }
            assertTrue(overlay[0] != null, "Special attack overlay was not found.");
            arena.playPlayerMove(new SpecialAttack(22));
        });

        long deadline = System.currentTimeMillis() + 30000;
        boolean animationLoaded = false;
        while (!animationLoaded && System.currentTimeMillis() < deadline) {
            boolean[] visible = new boolean[1];
            javax.swing.SwingUtilities.invokeAndWait(
                    () -> visible[0] = overlay[0].isVisible());
            animationLoaded = visible[0];
            if (!animationLoaded) {
                Thread.sleep(50);
            }
        }
        assertTrue(animationLoaded, "Special attack frames did not load.");

        try {
            java.awt.image.BufferedImage firstFrame = renderComponent(overlay[0]);
            assertTrue(countOpaquePixels(firstFrame) > 0,
                    "Special attack character was not rendered.");
            assertEquals(0, firstFrame.getRGB(80, 10) >>> 24,
                    "The special attack frame background was not transparent.");

            Thread.sleep(1200);
            java.awt.image.BufferedImage laterFrame = renderComponent(overlay[0]);
            assertTrue(countDifferentPixels(firstFrame, laterFrame) > 100,
                    "The special attack animation did not advance through its frames.");
        } finally {
            javax.swing.SwingUtilities.invokeAndWait(arena::setIdle);
        }
    }

    private static java.awt.image.BufferedImage renderComponent(
            javax.swing.JComponent component) throws Exception {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                component.getWidth(), component.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            java.awt.Graphics graphics = image.getGraphics();
            component.paint(graphics);
            graphics.dispose();
        });
        return image;
    }

    private static int countOpaquePixels(java.awt.image.BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countDifferentPixels(java.awt.image.BufferedImage first,
                                            java.awt.image.BufferedImage second) {
        int count = 0;
        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                if (first.getRGB(x, y) != second.getRGB(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void testLegacyProfilesMigrateWithStageProgress() throws Exception {
        Path directory = Files.createTempDirectory("sumurai-profile-test");
        Path dataFile = directory.resolve("users.txt");
        try {
            Files.write(dataFile, Collections.singletonList(
                    "legacy\tunused\t120\t11900\t0\t0\t0\t0"), StandardCharsets.UTF_8);
            UserRepository repository = new UserRepository(dataFile);
            UserProfile profile = repository.findLeaderboard(10).get(0);
            assertEquals(100, profile.getLevel(),
                    "Legacy profile level was not capped at 100.");
            assertEquals(1, profile.getHighestUnlockedStage(),
                    "Legacy profile did not start at stage 1.");
            profile.recordStageVictory(1);
            repository.save(profile);

            UserProfile loaded = repository.findLeaderboard(10).get(0);
            assertEquals(2, loaded.getHighestUnlockedStage(),
                    "Unlocked stage was not persisted.");
            assertEquals(9, Files.readAllLines(dataFile, StandardCharsets.UTF_8)
                            .get(0).split("\\t", -1).length,
                    "Profile save did not write the progression field.");
        } finally {
            Files.deleteIfExists(dataFile);
            Files.deleteIfExists(directory);
        }
    }

    private static void testJourneyPanelContainsThreeMainSectionsAnd100Stages() {
        AtomicInteger selectedStage = new AtomicInteger();
        AtomicInteger practiceStarts = new AtomicInteger();
        HomePanel panel = new HomePanel(UserProfile.guest(), selectedStage::set,
                practiceStarts::incrementAndGet, () -> { }, () -> { }, () -> { });
        assertEquals(3, panel.getComponentCount(),
                "Journey screen should contain sidebar, journey, and widgets panels.");
        assertEquals(105, countButtons(panel),
                "Journey screen should include five navigation items and 100 level nodes.");
        requireButton(panel, "1").doClick();
        assertEquals(1, selectedStage.get(), "Selecting a level did not start that stage.");
        assertTrue(!requireButton(panel, "2").isEnabled(),
                "A locked stage was selectable.");
        requireButton(panel, "PRACTICE").doClick();
        assertEquals(1, practiceStarts.get(), "Practice did not start a practice battle.");

        testLevelPathSupportsScrollbarWheelAndDrag(panel);
    }

    private static void testLevelPathSupportsScrollbarWheelAndDrag(HomePanel panel) {
        javax.swing.JScrollPane scrollPane = requireScrollPane(panel);
        assertEquals(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                scrollPane.getHorizontalScrollBarPolicy(),
                "Level path does not expose a horizontal scrollbar.");
        scrollPane.setSize(390, 175);
        scrollPane.doLayout();
        javax.swing.JViewport viewport = scrollPane.getViewport();
        java.awt.Component track = viewport.getView();
        viewport.setExtentSize(new java.awt.Dimension(390, 155));
        viewport.setViewSize(track.getPreferredSize());

        int beforeWheel = viewport.getViewPosition().x;
        java.awt.event.MouseWheelEvent wheel = new java.awt.event.MouseWheelEvent(
                scrollPane, java.awt.event.MouseEvent.MOUSE_WHEEL,
                System.currentTimeMillis(), 0, 100, 80, 1, false,
                java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, 1);
        for (java.awt.event.MouseWheelListener listener : scrollPane.getMouseWheelListeners()) {
            listener.mouseWheelMoved(wheel);
        }
        assertTrue(viewport.getViewPosition().x > beforeWheel,
                "Mouse wheel did not scroll the level path horizontally.");

        int beforeDrag = viewport.getViewPosition().x;
        java.awt.event.MouseEvent press = new java.awt.event.MouseEvent(track,
                java.awt.event.MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                0, 200, 76, 1, false, java.awt.event.MouseEvent.BUTTON1);
        for (java.awt.event.MouseListener listener : track.getMouseListeners()) {
            listener.mousePressed(press);
        }
        java.awt.event.MouseEvent drag = new java.awt.event.MouseEvent(track,
                java.awt.event.MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(),
                0, 100, 76, 0, false, java.awt.event.MouseEvent.NOBUTTON);
        for (java.awt.event.MouseMotionListener listener : track.getMouseMotionListeners()) {
            listener.mouseDragged(drag);
        }
        assertTrue(viewport.getViewPosition().x > beforeDrag,
                "Dragging did not pan the level path.");
        java.awt.event.MouseEvent release = new java.awt.event.MouseEvent(track,
                java.awt.event.MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
                0, 100, 76, 1, false, java.awt.event.MouseEvent.BUTTON1);
        for (java.awt.event.MouseListener listener : track.getMouseListeners()) {
            listener.mouseReleased(release);
        }
    }

    private static void testPlayerLevelStopsAt100() {
        UserProfile profile = UserProfile.guest();
        profile.addExperience(20000);
        assertEquals(UserProfile.MAX_PLAYER_LEVEL, profile.getLevel(),
                "Player level exceeded 100.");
        profile.addExperience(500);
        assertEquals(UserProfile.MAX_PLAYER_LEVEL, profile.getLevel(),
                "Player continued leveling past 100.");
    }

    private static javax.swing.JButton findButton(java.awt.Container container, String text) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof javax.swing.JButton
                    && text.equals(((javax.swing.JButton) component).getText())) {
                return (javax.swing.JButton) component;
            }
            if (component instanceof java.awt.Container) {
                javax.swing.JButton found = findButton((java.awt.Container) component, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static javax.swing.JButton requireButton(java.awt.Container container, String text) {
        javax.swing.JButton button = findButton(container, text);
        if (button == null) {
            throw new AssertionError("Button not found: " + text);
        }
        return button;
    }

    private static javax.swing.JScrollPane requireScrollPane(java.awt.Container container) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof javax.swing.JScrollPane) {
                return (javax.swing.JScrollPane) component;
            }
            if (component instanceof java.awt.Container) {
                javax.swing.JScrollPane found = findScrollPane((java.awt.Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        throw new AssertionError("Level path scroll pane not found.");
    }

    private static javax.swing.JScrollPane findScrollPane(java.awt.Container container) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof javax.swing.JScrollPane) {
                return (javax.swing.JScrollPane) component;
            }
            if (component instanceof java.awt.Container) {
                javax.swing.JScrollPane found = findScrollPane((java.awt.Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static int countButtons(java.awt.Container container) {
        int count = 0;
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof javax.swing.JButton) {
                String text = ((javax.swing.JButton) component).getText();
                if (text.matches("JOURNEY|PRACTICE|HISTORY|LEADERBOARD|PROFILE|[0-9]+|LOCK")) {
                    count++;
                }
            }
            if (component instanceof java.awt.Container) {
                count += countButtons((java.awt.Container) component);
            }
        }
        return count;
    }

    private static BattleEngine startBattle() {
        BattleEngine engine = new BattleEngine(new ProblemGenerator());
        Player player = new Player(UserProfile.guest(), 100);
        engine.startBattle(player, new Apprentice());
        return engine;
    }

    private static void expectInvalidAnswer(ThrowingAnswer action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Expected InvalidAnswerException.");
        } catch (InvalidAnswerException expected) {
            // Expected.
        }
    }

    private static void expectIllegalState(ThrowingAnswer action) throws Exception {
        try {
            action.run();
            throw new AssertionError("Expected IllegalStateException.");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected " + expected + ", but got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface ThrowingAnswer {
        void run() throws Exception;
    }
}
