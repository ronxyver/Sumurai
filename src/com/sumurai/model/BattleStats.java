// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public final class BattleStats {
    private int battlesPlayed;
    private int wins;
    private int losses;
    private int correctAnswers;

    public BattleStats() {
    }

    public BattleStats(int battlesPlayed, int wins, int losses, int correctAnswers) {
        if (battlesPlayed < 0 || wins < 0 || losses < 0 || correctAnswers < 0
                || (long) wins + losses > battlesPlayed) {
            throw new IllegalArgumentException("Invalid battle statistics.");
        }
        this.battlesPlayed = battlesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.correctAnswers = correctAnswers;
    }

    public void recordAnswer(boolean correct) {
        if (correct) {
            correctAnswers++;
        }
    }

    public void recordBattle(BattleOutcome outcome) {
        if (outcome == null || outcome == BattleOutcome.IN_PROGRESS) {
            throw new IllegalArgumentException("A completed battle outcome is required.");
        }
        battlesPlayed++;
        if (outcome == BattleOutcome.PLAYER_WON) {
            wins++;
        } else {
            losses++;
        }
    }

    public int getBattlesPlayed() {
        return battlesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }
}
