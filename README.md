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
javac --release 25 -encoding UTF-8 -d out $sources
java -cp out com.sumurai.Main
```

## Run the focused leaderboard checks

After compiling the project, run:

```powershell
javac --release 25 -encoding UTF-8 -cp out -d out tests\com\sumurai\leaderboard\LeaderboardServiceTest.java
java -cp out com.sumurai.leaderboard.LeaderboardServiceTest
```

Choose **Play as Guest** to start immediately, or **Sign Up** before logging in.
Runtime account and battle-history files are created under `data/`.