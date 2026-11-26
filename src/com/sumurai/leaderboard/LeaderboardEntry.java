package com.sumurai.leaderboard;

public final class LeaderboardEntry {
    private final String username;
    private final int level;
    private final double accuracy;
    private final double averageTime;
    private final boolean accuracyAvailable;
    private final boolean timingAvailable;

    public LeaderboardEntry(String username, int level, double accuracy, double averageTime,
                            boolean accuracyAvailable, boolean timingAvailable) {
        if (username == null || username.trim().isEmpty() || level < 1
                || !Double.isFinite(accuracy) || accuracy < 0 || accuracy > 100
                || !Double.isFinite(averageTime) || averageTime < 0) {
            throw new IllegalArgumentException("Invalid leaderboard entry.");
        }
        this.username = username;
        this.level = level;
        this.accuracy = accuracy;
        this.averageTime = averageTime;
        this.accuracyAvailable = accuracyAvailable;
        this.timingAvailable = timingAvailable;
    }

    public String getUsername() {
        return username;
    }

    public int getLevel() {
        return level;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getAverageTime() {
        return averageTime;
    }

    public boolean hasAccuracyData() {
        return accuracyAvailable;
    }

    public boolean hasTimingData() {
        return timingAvailable;
    }

    public int getStars() {
        if (!accuracyAvailable) {
            return 0;
        }
        if (accuracy >= 97) {
            return 5;
        }
        if (accuracy >= 94) {
            return 4;
        }
        if (accuracy >= 90) {
            return 3;
        }
        if (accuracy >= 80) {
            return 2;
        }
        return 1;
    }

    public double getSortTime() {
        return timingAvailable ? averageTime : Double.POSITIVE_INFINITY;
    }
}
