# Sumurai AI and contributor contract

Sumurai is a Java Swing math-combat game. This file and [`docs/project-blueprint.md`](docs/project-blueprint.md) define the shared architecture. The blueprint contains the full file responsibilities, method contracts, and team workflow.

## Package structure

```text
src/com/sumurai/
├── Main.java
├── model/       # GameCharacter, Player, Opponent + variants, abstract Move + moves,
│                # Buff, Debuff, MathProblem, OperationType, Difficulty,
│                # BattleOutcome, BattleStats, UserProfile, InvalidAnswerException
├── engine/      # BattleEngine, ProblemGenerator
├── storage/     # UserRepository, BattleHistoryRepository
└── gui/         # GameWindow, LoginPanel, HomePanel, BattlePanel,
                 # ResultPanel, DashboardPanel, LeaderboardPanel, ThemeManager
tests/
├── com/sumurai/diagnostics/EngineDiagnosticsTest.java
├── com/sumurai/gui/GuiSmokeTest.java
├── com/sumurai/tools/BalanceSimulatorTool.java
└── negative/BoundaryViolationTest.java  # isolated; intentionally not a normal test source
assets/          # images, gifs, and fonts
data/            # runtime saves; never commit
out/             # build output; never commit
```

## Strict boundaries

- All Java packages start with `com.sumurai`; source paths mirror package names.
- `model/` and `engine/` must contain **no** `javax.swing`, `java.awt`, or file-I/O imports or behavior. In particular, keep `java.io` and `java.nio.file` out of both packages.
- `model/` contains domain data and small domain operations. `engine/` owns question generation and battle rules. `storage/` owns all persistence and reports `IOException` instead of hiding failures. `gui/` owns Swing display and input.
- GUI code calls the engine/repositories through their public contracts; it does not duplicate combat or persistence rules. Do not block the Swing Event Dispatch Thread on file I/O.
- Keep passwords out of plaintext storage. Keep generated files and runtime saves out of Git.

## Agreed contracts

Use the full signatures and responsibilities in `docs/project-blueprint.md`. Do not rename or change a public contract without team agreement.

- `ProblemGenerator.generate(Difficulty): MathProblem`
- `BattleEngine.startBattle(Player, Opponent): MathProblem`
- `BattleEngine.submitAnswer(String, Move): BattleOutcome throws InvalidAnswerException`
- `BattleEngine.endBattleByTimeout(): BattleOutcome`
- `BattleEngine.getCurrentProblem(): MathProblem`
- `BattleEngine.getPlayer(): Player`
- `BattleEngine.getOpponent(): Opponent`
- `BattleEngine.getOutcome(): BattleOutcome`
- `UserRepository.authenticate(String, char[]): Optional<UserProfile> throws IOException`
- `UserRepository.register(String, char[]): UserProfile throws IOException`
- `BattleHistoryRepository.appendResult(String, BattleOutcome, int, Instant): void throws IOException`

A malformed answer raises `InvalidAnswerException` without consuming a turn. A well-formed incorrect answer triggers the opponent-attack rule; a correct answer applies the selected move. Finished battles reject further submissions with `IllegalStateException`.

## Style and collaboration

- Prefer small, cohesive classes, explicit access modifiers, descriptive names, and focused methods. Validate constructor and public-method inputs; keep health, shield, counters, and difficulty bounds valid.
- Use standard Java formatting and the repository's chosen Java version/build command. Add no new library or configuration without team approval.
- Each member owns their assigned files. Use a focused branch/PR, coordinate contract changes first, and do not rewrite another member's changes.
- Add focused tests for changed behavior. Keep negative compile/boundary fixtures isolated from the normal source and test compile.
- Before finishing, inspect the diff and run the narrowest relevant compile, tests, and package-boundary check. Report any check that could not be run.

For the complete architecture, execution plan, and copy-paste session prompt, see [`docs/project-blueprint.md`](docs/project-blueprint.md).
