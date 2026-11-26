# Sumurai

Sumurai is a Java Swing math-combat prototype.

## Run in VS Code

Open the repository root as the workspace and press **F5**. The `Sumurai` launch
configuration compiles the Java sources into `out/` before starting
`com.sumurai.Main`.

## Run from PowerShell

From the repository root:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sources = Get-ChildItem -Path src\com\sumurai -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }
<<<<<<< HEAD
javac -encoding UTF-8 -d out $sources
New-Item -ItemType Directory -Force out\images | Out-Null
Copy-Item -Recurse -Force assets\images\* out\images
java -cp out com.sumurai.Main
```

## Run the battle tests

In VS Code, run the **Sumurai: test battle** task from **Terminal: Run Task**.
From PowerShell at the repository root, run:

```powershell
$testSources = Get-ChildItem -Path tests\com\sumurai -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp out -d out $testSources
java -cp out com.sumurai.Test
=======
javac --release 25 -encoding UTF-8 -d out $sources
java -cp out com.sumurai.Main
```

## Run the focused leaderboard checks

After compiling the project, run:

```powershell
javac --release 25 -encoding UTF-8 -cp out -d out tests\com\sumurai\leaderboard\LeaderboardServiceTest.java
java -cp out com.sumurai.leaderboard.LeaderboardServiceTest
>>>>>>> Leaderboards/added-UI
```

Choose **Play as Guest** to start immediately, or **Sign Up** before logging in.
Runtime account and battle-history files are created under `data/`.

The Journey has 100 stages. Winning a stage unlocks the next one; Practice
starts at the highest unlocked stage without unlocking progression. Question
operands scale up to 1000 at level 100. Enemy damage scales from 1.0x at level
1 to 2.98x at level 100. Navigate the level path with its scrollbar, mouse
wheel, or by dragging it horizontally. Player profile level is also capped at
100.