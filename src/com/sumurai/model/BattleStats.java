// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public final class BattleStats {
    private int battlesPlayed;
    private int wins;
    private int losses;
    private int correctAnswers;
    private int measuredAnswers;
    private int measuredCorrectAnswers;
    private long totalAnswerTimeMillis;

    public BattleStats() {
    }

    public BattleStats(int battlesPlayed, int wins, int losses, int correctAnswers) {
        this(battlesPlayed, wins, losses, correctAnswers, 0, 0, 0);
    }

    public BattleStats(int battlesPlayed, int wins, int losses, int correctAnswers,
                       int measuredAnswers, int measuredCorrectAnswers, long totalAnswerTimeMillis) {
        if (battlesPlayed < 0 || wins < 0 || losses < 0 || correctAnswers < 0
                || (long) wins + losses > battlesPlayed
                || measuredAnswers < 0 || measuredCorrectAnswers < 0
                || measuredCorrectAnswers > measuredAnswers
                || measuredCorrectAnswers > correctAnswers || totalAnswerTimeMillis < 0) {
            throw new IllegalArgumentException("Invalid battle statistics.");
        }
        this.battlesPlayed = battlesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.correctAnswers = correctAnswers;
        this.measuredAnswers = measuredAnswers;
        this.measuredCorrectAnswers = measuredCorrectAnswers;
        this.totalAnswerTimeMillis = totalAnswerTimeMillis;
    }

    public void recordAnswer(boolean correct) {
        if (correct) {
            correctAnswers = Math.incrementExact(correctAnswers);
        }
    }

    public void recordAnswer(boolean correct, long answerTimeMillis) {
        if (answerTimeMillis < 0) {
            throw new IllegalArgumentException("Answer time cannot be negative.");
        }
        int newMeasuredAnswers = Math.incrementExact(measuredAnswers);
        long newTotalAnswerTimeMillis = Math.addExact(totalAnswerTimeMillis, answerTimeMillis);
        int newCorrectAnswers = correctAnswers;
        int newMeasuredCorrectAnswers = measuredCorrectAnswers;
        if (correct) {
            newCorrectAnswers = Math.incrementExact(correctAnswers);
            newMeasuredCorrectAnswers = Math.incrementExact(measuredCorrectAnswers);
        }
        measuredAnswers = newMeasuredAnswers;
        totalAnswerTimeMillis = newTotalAnswerTimeMillis;
        correctAnswers = newCorrectAnswers;
        measuredCorrectAnswers = newMeasuredCorrectAnswers;
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

    public int getMeasuredAnswers() {
        return measuredAnswers;
    }

    public int getMeasuredCorrectAnswers() {
        return measuredCorrectAnswers;
    }

    public long getTotalAnswerTimeMillis() {
        return totalAnswerTimeMillis;
    }

    public boolean hasAnswerMetrics() {
        return measuredAnswers > 0;
    }

    public double getAccuracy() {
        return measuredAnswers == 0 ? 0
                : measuredCorrectAnswers * 100.0 / measuredAnswers;
    }

    public double getAverageTime() {
        return measuredAnswers == 0 ? 0
                : totalAnswerTimeMillis / (1000.0 * measuredAnswers);
    }
}
