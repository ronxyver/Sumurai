# Sumurai Project Blueprint

This is the architecture and workflow reference for the Java prototype. It follows the inventory in [`map.md`](map.md). The game loop is math-answer combat: correct answers let the player use a move, incorrect answers allow the opponent to attack, and the battle ends when a character is defeated or the timer expires.

`Main.java` is the entry point. The former `LoginUI.java` has been replaced by `LoginPanel.java`.

## Part 1 — Java file architecture

All source files use package names matching their paths under `src/com/sumurai/`. Tests use the corresponding paths under `tests/`.

### `com.sumurai`

#### `Main.java` — application entry point
- **Responsibility:** Construct storage services and launch the Swing window on the Event Dispatch Thread.
- **Key fields:** None required.
- **Key methods:** `public static void main(String[] args): void`.
- **Dependencies:** `GameWindow`, `UserRepository`, `BattleHistoryRepository`; `SwingUtilities` (allowed here).

### `com.sumurai.model`

This package contains game data and domain behavior only. It must not import Swing/AWT or perform file I/O.

#### `GameCharacter.java` — shared character state
- **Responsibility:** Hold common combat state and enforce health/shield bounds.
- **Key fields:** `String name`, `int maxHealth`, `int health`, `int shieldPoints`.
- **Key methods:** `protected GameCharacter(String name, int maxHealth)`, `String getName()`, `int getHealth()`, `int getMaxHealth()`, `boolean isAlive()`, `int takeDamage(int amount)`, `int heal(int amount)`, `void addShield(int amount)`.
- **Dependencies:** None.

#### `Player.java` — player combatant
- **Responsibility:** Represent the player in a battle and connect them to their profile.
- **Key fields:** `UserProfile profile`.
- **Key methods:** `Player(UserProfile profile, int maxHealth)`, `UserProfile getProfile()`.
- **Dependencies:** `GameCharacter`, `UserProfile`.

#### `Opponent.java` — shared opponent state
- **Responsibility:** Add opponent difficulty and base attack behavior to a character.
- **Key fields:** `Difficulty difficulty`, `int damagePerTurn`.
- **Key methods:** `protected Opponent(String name, int maxHealth, Difficulty difficulty, int damagePerTurn)`, `Difficulty getDifficulty()`, `int getDamagePerTurn()`.
- **Dependencies:** `GameCharacter`, `Difficulty`.

#### `Apprentice.java` — easiest opponent
- **Responsibility:** Provide the Apprentice opponent's name, health, difficulty, and attack values.
- **Key fields:** None beyond inherited fields.
- **Key methods:** `Apprentice()`.
- **Dependencies:** `Opponent`, `Difficulty`.

#### `Ronin.java` — medium opponent
- **Responsibility:** Provide the Ronin opponent's name, health, difficulty, and attack values.
- **Key fields:** None beyond inherited fields.
- **Key methods:** `Ronin()`.
- **Dependencies:** `Opponent`, `Difficulty`.

#### `Shogun.java` — hardest opponent
- **Responsibility:** Provide the Shogun opponent's name, health, difficulty, and attack values.
- **Key fields:** None beyond inherited fields.
- **Key methods:** `Shogun()`.
- **Dependencies:** `Opponent`, `Difficulty`.

#### `Move.java` — combat move contract
- **Responsibility:** Give all player moves a common display and power contract.
- **Key fields:** `String name`, `int power`.
- **Key methods:** `protected Move(String name, int power)`, `String getName()`, `int getPower()`.
- **Dependencies:** None.

#### `BasicAttack.java` — standard attack move
- **Responsibility:** Define the player's no-cost basic damage move.
- **Key fields:** Inherited name and power.
- **Key methods:** `BasicAttack(int damage)`.
- **Dependencies:** `Move`.

#### `SpecialAttack.java` — enhanced attack move
- **Responsibility:** Define a stronger attack move.
- **Key fields:** Inherited name and power.
- **Key methods:** `SpecialAttack(int damage)`.
- **Dependencies:** `Move`.

#### `Heal.java` — healing move
- **Responsibility:** Define a move that restores player health.
- **Key fields:** Inherited name and power.
- **Key methods:** `Heal(int healingAmount)`.
- **Dependencies:** `Move`.

#### `Shield.java` — defensive move
- **Responsibility:** Define a move that grants shield points.
- **Key fields:** Inherited name and power.
- **Key methods:** `Shield(int shieldAmount)`.
- **Dependencies:** `Move`.

#### `Buff.java` — positive temporary effect
- **Responsibility:** Define a temporary attack-damage buff move.
- **Key fields:** Inherited power and `int remainingTurns`.
- **Key methods:** `Buff(int magnitude, int remainingTurns)`, `int getMagnitude()`, `int getRemainingTurns()`.
- **Dependencies:** `Move`.

#### `Debuff.java` — negative temporary effect
- **Responsibility:** Define a temporary opponent-damage debuff move.
- **Key fields:** Inherited power and `int remainingTurns`.
- **Key methods:** `Debuff(int magnitude, int remainingTurns)`, `int getMagnitude()`, `int getRemainingTurns()`.
- **Dependencies:** `Move`.

#### `MathProblem.java` — generated question data
- **Responsibility:** Store one arithmetic question and check a submitted numeric answer.
- **Key fields:** `int leftOperand`, `int rightOperand`, `OperationType operation`, `int correctAnswer`.
- **Key methods:** `MathProblem(int leftOperand, int rightOperand, OperationType operation)`, `String getPrompt()`, `boolean isCorrect(int answer)`, `int getCorrectAnswer()` (engine/tests only; do not reveal it in the UI).
- **Dependencies:** `OperationType`.

#### `OperationType.java` — arithmetic operation enum
- **Responsibility:** Identify supported arithmetic operations and their display symbols.
- **Key fields:** Enum values `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`.
- **Key methods:** `String getSymbol()`, `int calculate(int left, int right)`.
- **Dependencies:** None.

#### `Difficulty.java` — difficulty enum
- **Responsibility:** Name the difficulty levels and provide problem-generation bounds.
- **Key fields:** Enum values `EASY`, `MEDIUM`, `HARD`; each has a maximum operand bound.
- **Key methods:** `int getMaxOperand()`.
- **Dependencies:** None.

#### `BattleOutcome.java` — battle result enum
- **Responsibility:** Represent whether a battle is ongoing or has been won by either side.
- **Key fields:** Enum values `IN_PROGRESS`, `PLAYER_WON`, `OPPONENT_WON`.
- **Key methods:** None.
- **Dependencies:** None.

#### `BattleStats.java` — profile statistics
- **Responsibility:** Track battles and correct-answer totals.
- **Key fields:** `int battlesPlayed`, `int wins`, `int losses`, `int correctAnswers`.
- **Key methods:** `BattleStats()`, `void recordAnswer(boolean correct)`, `void recordBattle(BattleOutcome outcome)`, `int getBattlesPlayed()`, `int getWins()`, `int getLosses()`, `int getCorrectAnswers()`.
- **Dependencies:** `BattleOutcome`.

#### `UserProfile.java` — saved player account state
- **Responsibility:** Hold a player's identity, progression, and aggregate statistics; never store a plaintext password.
- **Key fields:** `String username`, `String passwordHash`, `int level`, `long experience`, `int highestUnlockedStage`, `BattleStats stats`, `boolean guest`.
- **Key methods:** `UserProfile(String username, String passwordHash, int level, long experience, BattleStats stats)`, the persistence overload accepting `highestUnlockedStage`, `String getUsername()`, `String getPasswordHash()`, `int getLevel()`, `long getExperience()`, `int getHighestUnlockedStage()`, `void recordStageVictory(int stage)`, `BattleStats getStats()`, `boolean isGuest()`, `void addExperience(long amount)`, `static UserProfile guest()`.
- **Dependencies:** `BattleStats`.
- **Progression:** Player level and Journey stage are capped at 100. Stage 1 is unlocked for new and legacy profiles; winning the currently unlocked stage unlocks the next stage.

#### `InvalidAnswerException.java` — malformed-answer error
- **Responsibility:** Report an answer that cannot be parsed as a valid integer without advancing the battle.
- **Key fields:** Standard exception message/cause.
- **Key methods:** `InvalidAnswerException(String message)`, `InvalidAnswerException(String message, Throwable cause)`.
- **Dependencies:** `Exception`.

### `com.sumurai.engine`

This package owns game rules and state transitions. It must not import Swing/AWT or perform file I/O.

#### `ProblemGenerator.java` — arithmetic question generation
- **Responsibility:** Generate valid questions within a requested difficulty's bounds or a Journey stage's operand range.
- **Key fields:** `Random random`.
- **Key methods:** `ProblemGenerator()`, `ProblemGenerator(Random random)`, `MathProblem generate(Difficulty difficulty)`, `MathProblem generateForLevel(int level)`.
- **Dependencies:** `Difficulty`, `MathProblem`, `OperationType`.
- **Journey scaling:** Each operand is capped at `level * 10` through level 100 (maximum 1000); division questions remain exact.

#### `BattleEngine.java` — battle rules and turn transitions
- **Responsibility:** Start battles, validate answers, apply moves/opponent attacks, update stats, and expose current battle state.
- **Key fields:** `ProblemGenerator problemGenerator`, `Player player`, `Opponent opponent`, `MathProblem currentProblem`, `BattleOutcome outcome`.
- **Key methods:** `BattleEngine(ProblemGenerator problemGenerator)`, `MathProblem startBattle(Player player, Opponent opponent)`, level-aware `MathProblem startBattle(Player player, Opponent opponent, int level)`, `MathProblem startPracticeBattle(Player player, Opponent opponent, int level)`, `BattleOutcome submitAnswer(String answer, Move move) throws InvalidAnswerException`, `BattleOutcome endBattleByTimeout()`, `MathProblem getCurrentProblem()`, `Player getPlayer()`, `Opponent getOpponent()`, `BattleOutcome getOutcome()`, `int getTurns()`.
- **Dependencies:** `ProblemGenerator` and relevant `model/` classes.
- **Contract note:** Malformed text throws `InvalidAnswerException` without consuming a turn. A well-formed wrong answer follows the opponent-attack rule. A correct answer applies the selected move. Finished battles reject further submissions with `IllegalStateException`. The battle panel owns the countdown and ends the battle through `endBattleByTimeout()`.
- **Journey rules:** The level-aware battle start validates the profile's unlock, scales opponent damage by `1.0 + 0.02 * (level - 1)` (1.0x–2.98x), and records a stage victory only for progression battles. Practice uses the selected stage's questions/damage but does not unlock stages.
- **Journey opponent mapping:** Apprentice at levels 1–25, Ronin at levels 26–60, and Shogun at levels 61–100. Damage uses the mapped opponent's base damage and rounds the scaled result to the nearest integer.

### `com.sumurai.storage`

Storage owns filesystem access. Repository methods report I/O errors; they must not silently return success-shaped defaults.

#### `UserRepository.java` — account persistence
- **Responsibility:** Register, authenticate, update, and rank saved profiles in `data/users.txt`.
- **Key fields:** `Path dataFile`.
- **Key methods:** `UserRepository(Path dataFile)`, `Optional<UserProfile> authenticate(String username, char[] password) throws IOException`, `UserProfile register(String username, char[] password) throws IOException`, `void save(UserProfile profile) throws IOException`, `List<UserProfile> findLeaderboard(int limit) throws IOException`.
- **Dependencies:** `UserProfile`, `BattleStats`; Java file I/O and password-hashing APIs.
- **Format compatibility:** Reads existing eight-field account records as stage 1, caps older profile levels at 100, and writes the ninth highest-unlocked-stage field on save.

#### `BattleHistoryRepository.java` — battle-log persistence
- **Responsibility:** Append completed battle summaries and retrieve a player's recent history from `data/history.txt`.
- **Key fields:** `Path dataFile`.
- **Key methods:** `BattleHistoryRepository(Path dataFile)`, `void appendResult(String username, BattleOutcome outcome, int turns, Instant completedAt) throws IOException`, `List<String> findRecent(String username, int limit) throws IOException`.
- **Dependencies:** `BattleOutcome`; Java file I/O and date/time APIs.

### `com.sumurai.gui`

Swing components display state and forward user actions; game rules stay in `engine/`.

#### `GameWindow.java` — screen navigation
- **Responsibility:** Own the main frame, shared services, and screen navigation.
- **Key fields:** `CardLayout cardLayout`, `JPanel screens`, `UserRepository userRepository`, `BattleHistoryRepository historyRepository`.
- **Key methods:** `GameWindow(UserRepository users, BattleHistoryRepository history)`, `void showLogin()`, `void showHome(UserProfile profile)`, `void showBattle(UserProfile profile, Opponent opponent)`, `void showResults(BattleOutcome outcome)`.
- **Dependencies:** All screen panels, repositories, engine, and relevant model classes.

#### `LoginPanel.java` — sign-in and guest entry
- **Responsibility:** Collect credentials for sign-in/registration and report successful sign-in, registration, or guest entry to the window.
- **Key fields:** `JTextField usernameField`, `JPasswordField passwordField`, `UserRepository userRepository`.
- **Key methods:** `LoginPanel(UserRepository users, Consumer<UserProfile> onAuthenticated)`, `void register(String username, char[] password)`, `void clearFields()`.
- **Dependencies:** Swing, `UserRepository`, `UserProfile`.
- **Threading contract:** Run repository calls off the Event Dispatch Thread (for example with `SwingWorker`) and return UI updates to that thread.

#### `HomePanel.java` — journey screen
- **Responsibility:** Render the three-panel Journey home screen: left navigation, chapter and level path, and right-side player level/XP and profile stats widgets.
- **Key fields:** `UserProfile profile`.
- **Key methods:** Legacy `HomePanel(UserProfile profile, Runnable onPlay, Runnable onDashboard, Runnable onLeaderboard)` and the callback-based constructor for level selection, practice, history, dashboard, and leaderboard.
- **Dependencies:** Swing, `UserProfile`, `LevelPathPanel`.
- **Navigation:** Journey, Practice, History, Leaderboard, and Profile are wired to app actions.

#### `LevelPathPanel.java` — horizontally scrollable stage path
- **Responsibility:** Display 100 stage nodes, disable locked nodes, focus the newest unlocked stage, and support horizontal scrollbar, mouse-wheel, and mouse-drag navigation.
- **Key methods:** `LevelPathPanel(UserProfile profile, IntConsumer onLevelSelected)`.
- **Dependencies:** Swing, `UserProfile`.

#### `BattlePanel.java` — battle presentation and input
- **Responsibility:** Display question/health/countdown, submit answers through move controls, and finish on defeat or timeout.
- **Key fields:** `BattleEngine battleEngine`, question/answer labels and fields, health bars, move controls, Swing `Timer`.
- **Key methods:** `BattlePanel(BattleEngine engine, Consumer<BattleOutcome> onFinished)`, `BattlePanel(BattleEngine engine, BiConsumer<BattleOutcome, BattleResultMetrics> onFinished, Runnable onMenu)`, `void refreshBattleView()`.
- **Dependencies:** Swing, `BattleEngine`, `MathProblem`, `Move`, `BattleOutcome`.

#### `BattleArenaPanel.java`, `MathPromptDialog.java`, and `BattleResultMetrics.java`
- **Responsibility:** Render the battle scene and sprite animations; display math prompts; carry immutable, current-battle results metrics to the results screen.
- **Key methods:** `BattleArenaPanel(String opponentName)`, `MathPromptDialog.show(Component parent, String prompt)`, `BattleResultMetrics(...)`.
- **Dependencies:** Swing, battle model move types.

#### `ResultPanel.java` — completed battle summary
- **Responsibility:** Present the outcome and let the player continue.
- **Key fields:** `BattleOutcome outcome`, `BattleStats stats`.
- **Key methods:** `ResultPanel(BattleOutcome outcome, BattleStats stats, Runnable onHome, Runnable onDashboard, Runnable onLeaderboard)`, plus the results-layout overload accepting current-battle metrics and next-level/replay/home actions.
- **Dependencies:** Swing, `BattleOutcome`, `BattleStats`.

#### `DashboardPanel.java` — player progression
- **Responsibility:** Present the current profile level, experience, and statistics.
- **Key fields:** `UserProfile profile`.
- **Key methods:** `DashboardPanel(UserProfile profile, Runnable onBack)`.
- **Dependencies:** Swing, `UserProfile`, `BattleStats`.

#### `LeaderboardPanel.java` — ranked profiles
- **Responsibility:** Display the leaderboard supplied by the window.
- **Key fields:** `List<UserProfile> profiles`.
- **Key methods:** `LeaderboardPanel(List<UserProfile> profiles, Runnable onBack)`.
- **Dependencies:** Swing, `UserProfile`, `BattleStats`.

#### `ThemeManager.java` — shared visual theme
- **Responsibility:** Centralize colors, fonts, and classpath-based asset loading.
- **Key fields:** Theme color/font constants.
- **Key methods:** `static Color backgroundColor()`, `static Color accentColor()`, `static Font headingFont(float size)`, `static ImageIcon loadIcon(String resourcePath)`.
- **Dependencies:** Swing/AWT and bundled assets only; no dependency on `model/` or `engine/`.

### `tests/`

Tests and tools are outside production packages. Keep `tests/negative/` out of the normal compile command; its fixtures are expected to fail the relevant compile or boundary check.

#### `tests/com/sumurai/diagnostics/EngineDiagnosticsTest.java`
- **Responsibility:** Verify problem bounds, malformed-answer behavior, and battle turn/outcome transitions.
- **Key fields:** Deterministic `ProblemGenerator`/`BattleEngine` fixtures.
- **Key methods:** `void generatesProblemsWithinDifficultyBounds()`, `void malformedAnswersDoNotAdvanceTurns()`, `void answersProduceExpectedCombatOutcomes()`.
- **Dependencies:** `model/`, `engine/`, chosen test runner.

#### `tests/com/sumurai/gui/GuiSmokeTest.java`
- **Responsibility:** Smoke-test screen construction/navigation without requiring a real display where headless testing is available.
- **Key fields:** Test profiles and in-memory/test repositories.
- **Key methods:** `void panelsConstructInHeadlessMode()`, `void navigationShowsExpectedScreen()`.
- **Dependencies:** `gui/`, `model/`, storage test doubles, chosen test runner.

#### `tests/com/sumurai/tools/BalanceSimulatorTool.java`
- **Responsibility:** Run repeatable simulated battles and summarize win rates for balancing.
- **Key fields:** Seed/configuration values and aggregate counters.
- **Key methods:** `public static void main(String[] args): void`.
- **Dependencies:** `engine/`, `model/`.

#### `tests/negative/BoundaryViolationTest.java`
- **Responsibility:** Verify that forbidden package dependencies or intentionally invalid API usage are detected by the isolated negative-check command.
- **Key fields:** None required.
- **Key methods:** `public static void main(String[] args): void` (or the chosen runner's equivalent entry point).
- **Dependencies:** Java compiler/check tooling and the source tree; excluded from normal production/test compilation.

## Part 2 — Three-person execution plan

### 1. Role and file ownership

| Member | Owns | Avoid editing |
|---|---|---|
| **1 — Core & Logic Lead** | `model/`, `engine/`, their tests' expected behavior/contracts | `storage/`, `gui/` |
| **2 — Data & QA Lead** | `storage/`, `tests/` including isolated negative checks | `model/`, `engine/`, `gui/` |
| **3 — UI & Presentation Lead** | `gui/`, `Main.java`, `assets/` | `model/`, `engine/`, `storage/` |

If a contract needs to change, open a small PR or agree on the signature first; do not locally redefine another member's API. Keep one owner per file, use small PRs, and integrate through `main` rather than having everyone edit a shared branch.

### 2. Day-1 contract-first setup

1. Agree on the package tree, Java version, test runner/build command, and the contracts in this document. Keep data types and method names stable after sign-off.
2. Member 1 creates compile-ready model/engine skeletons first: constructors, enums, fields, getters, and the public signatures for `ProblemGenerator.generate`, `BattleEngine.startBattle`, `BattleEngine.submitAnswer`, and current-state getters. Bodies can be explicit `UnsupportedOperationException` stubs temporarily, but must be replaced before playable integration.
3. In the same first integration, Member 2 creates repository signatures: `authenticate`, `register`, `save`, `findLeaderboard`, `appendResult`, and `findRecent`, with explicit `IOException` handling in callers.
4. Member 3 creates constructor-only panels and navigation against those signatures. `Main` must be able to construct the repositories and launch `GameWindow`, even before real gameplay exists.
5. Merge a compile-only skeleton PR from each owner. Run a clean compile before feature work; after that, each member can implement their owned files in parallel.

### 3. Build order and milestones

1. **Foundation:** Confirm the map, contracts, Java version, compile/test commands, `.gitignore`, and repository workflow.
2. **Compile-ready skeleton:** Add all agreed source files with only required declarations; check package names and cross-package imports.
3. **Domain and engine:** Implement bounds and validations in model classes, then deterministic problem generation and battle transitions. Test edge cases before connecting Swing.
4. **Persistence and QA:** Implement repository format, malformed/corrupt-record handling, duplicate accounts, leaderboard ordering, and history. Add tests and isolated negative checks.
5. **UI shell:** Build themed panels and screen navigation with test/dummy profile data; keep long-running file work off the Swing Event Dispatch Thread.
6. **Vertical slice:** Connect login/guest → home → battle → result → saved history/dashboard. Integrate only through the agreed constructors and callbacks.
7. **Playtest and polish:** Run normal and negative checks, test each difficulty and malformed input, verify assets load from the packaged/classpath root, and fix integration issues in their owning package.
8. **Release candidate:** Clean build from a fresh clone, review PRs, record known limitations, and capture screenshots only after behavior is stable.

### 4. Git and repository rules

- Branches: `feature/core-<topic>`, `feature/storage-<topic>`, `feature/ui-<topic>`, `test/<topic>`, and `fix/<topic>`.
- One contributor owns a file at a time. Avoid shared edits to `Main.java`, `CLAUDE.md`, this blueprint, or build configuration; assign an owner and review proposed edits.
- Keep PRs small and focused. Before opening one: pull/rebase the current `main`, run the documented clean compile and test commands, inspect `git diff`, and verify generated files are absent.
- Require at least one teammate review plus passing compile, normal tests, negative boundary/compile checks, and whitespace/diff checks before merging.
- Merge one PR at a time when contracts or shared entry points change. Resolve conflicts in the owning branch; do not overwrite another member's work.
- Root `.gitignore` excludes `/out/`, `/data/`, and compiled `.class` files. Commit source, tests, docs, and licensed assets; never commit generated class files or runtime saves.

## Part 3 — Universal AI alignment prompt

Copy this prompt into a fresh AI session and fill in both placeholders. Attach or point the AI at the current repository files; this document is the shared source of truth.

```text
You are helping build Sumurai, a Java Swing math-combat game. My role is [MY_ROLE], and the only file I am asking you to build or change is [FILE_TO_BUILD].

Before editing, inspect the existing repository and read CLAUDE.md, docs/project-blueprint.md, and docs/map.md. Treat the current source and the agreed contracts as authoritative; do not assume files or requirements that are not present.

Scope and safety:
- Work only on [FILE_TO_BUILD] and directly necessary call sites that I explicitly approve. Do not overwrite another member's work or make unrelated refactors.
- If an API change is required, stop and propose the exact signature change before implementing it.
- Follow the existing package and folder structure. Do not create duplicate classes, alternate package roots, generated files, or dependencies without asking.
- Keep model/ and engine/ free of javax.swing, java.awt, and file-I/O imports or behavior. Keep filesystem access in storage/. Keep Swing presentation in gui/.
- Do not put game rules in GUI classes. Do not put rendering or persistence in model/ or engine/.
- Keep file I/O errors explicit; do not silently swallow exceptions or return fake success/default data. Never store plaintext passwords.
- Keep Swing UI work on the Event Dispatch Thread and move blocking repository operations off it.

Contracts:
- Use the exact class inventory and method contracts in docs/project-blueprint.md and CLAUDE.md. In particular, preserve ProblemGenerator.generate(Difficulty), BattleEngine.startBattle(Player, Opponent), and BattleEngine.submitAnswer(String, Move) throws InvalidAnswerException and its stated turn behavior.
- Preserve package names under com.sumurai. Do not rename or remove public APIs without approval.

Workflow:
1. Briefly state the target file's responsibility and any dependencies you will touch.
2. Make the smallest complete change, matching local style and Java version.
3. Add or update only focused tests for behavior changed.
4. Run the narrowest relevant compile/test and boundary check. Report commands and results honestly; do not claim checks passed if not run.
5. Summarize changed files, behavior, checks, and any unresolved contract question.
If the request conflicts with the repository or these contracts, do not guess: explain the conflict and ask before changing the contract.
```
