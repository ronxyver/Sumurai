```text
Sumurai/
├── .gitignore
├── CLAUDE.md                        # Shared AI alignment & package boundary rules
├── README.md
├── assets/                          # Bundled art and fonts, loaded via classpath root
│   ├── CREDITS.md
│   ├── fonts/
│   │   ├── SamuraiDisplay.ttf
│   │   └── DojoMono.ttf
│   └── images/
│       ├── logo.png
│       ├── dojo_bg.png
│       ├── player_samurai.png
│       ├── bot_apprentice.png
│       ├── bot_ronin.png
│       └── bot_shogun.png
├── data/                            # Runtime saves, gitignored
│   ├── users.txt                    # Player credentials, unlocked level, career stats
│   └── history.txt                  # Completed battle logs & leaderboard records
├── docs/
│   ├── project-proposal.pdf
│   ├── requirements.md
│   ├── design-decisions.md
│   ├── system-overview.md
│   ├── oop-map.md
│   ├── class-diagram.png
│   ├── program-flow.png
│   ├── presentation.pptx
│   └── test-logs/
│       └── diagnostics-run.log
├── out/                             # Compiled .class files, gitignored
├── screenshots/
│   ├── 01_login.png
│   ├── 02_home_journey.png
│   ├── 03_battle_arena.png
│   ├── 04_battle_results.png
│   ├── 05_leaderboard.png
│   └── 06_dashboard.png
├── src/
│   └── com/sumurai/
│       ├── Main.java                # App entry point (initializes storage & launches GameWindow)
│       ├── model/                   # Game elements — ZERO Swing, ZERO File I/O
│       │   ├── Apprentice.java
│       │   ├── BasicAttack.java
│       │   ├── BattleOutcome.java
│       │   ├── BattleStats.java
│       │   ├── Buff.java
│       │   ├── Debuff.java
│       │   ├── Difficulty.java
│       │   ├── GameCharacter.java
│       │   ├── Heal.java
│       │   ├── InvalidAnswerException.java
│       │   ├── MathProblem.java
│       │   ├── Move.java
│       │   ├── OperationType.java
│       │   ├── Opponent.java
│       │   ├── Player.java
│       │   ├── Ronin.java
│       │   ├── Shield.java
│       │   ├── Shogun.java
│       │   ├── SpecialAttack.java
│       │   └── UserProfile.java
│       ├── engine/                  # Battle & math rules — ZERO Swing, ZERO File I/O
│       │   ├── BattleEngine.java
│       │   └── ProblemGenerator.java
│       ├── storage/                 # Plain-text accounts & battle history in data/
│       │   ├── BattleHistoryRepository.java
│       │   └── UserRepository.java
│       └── gui/                     # Swing screens & rendering (matches Page 8 wireframes)
│           ├── BattlePanel.java
│           ├── DashboardPanel.java
│           ├── GameWindow.java
│           ├── HomePanel.java
│           ├── LeaderboardPanel.java
│           ├── LoginPanel.java
│           ├── ResultPanel.java
│           └── ThemeManager.java
└── tests/
    ├── com/sumurai/diagnostics/
    │   └── EngineDiagnosticsTest.java
    ├── com/sumurai/gui/
    │   └── GuiSmokeTest.java
    ├── com/sumurai/tools/
    │   └── BalanceSimulatorTool.java
    └── negative/                    # Must fail to compile; kept out of main build
        └── BoundaryViolationTest.java

```
