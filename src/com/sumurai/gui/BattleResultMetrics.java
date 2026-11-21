// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

/** Immutable, current-battle metrics used by the results screen. */
public final class BattleResultMetrics {
    private final int secondsRemaining;
    private final int answeredTurns;
    private final int correctAnswers;
    private final long totalAnswerTimeMillis;
    private final int longestStreak;

    public BattleResultMetrics(int secondsRemaining, int answeredTurns, int correctAnswers,
                               long totalAnswerTimeMillis, int longestStreak) {
        if (secondsRemaining < 0 || answeredTurns < 0 || correctAnswers < 0
                || correctAnswers > answeredTurns || totalAnswerTimeMillis < 0
                || longestStreak < 0 || longestStreak > correctAnswers) {
            throw new IllegalArgumentException("Invalid battle result metrics.");
        }
        this.secondsRemaining = secondsRemaining;
        this.answeredTurns = answeredTurns;
        this.correctAnswers = correctAnswers;
        this.totalAnswerTimeMillis = totalAnswerTimeMillis;
        this.longestStreak = longestStreak;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public int getAnsweredTurns() {
        return answeredTurns;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getAccuracyPercent() {
        return answeredTurns == 0 ? 0
                : (int) Math.round(correctAnswers * 100.0 / answeredTurns);
    }

    public long getAverageAnswerTimeMillis() {
        return answeredTurns == 0 ? 0 : totalAnswerTimeMillis / answeredTurns;
    }

    public int getLongestStreak() {
        return longestStreak;
    }
}