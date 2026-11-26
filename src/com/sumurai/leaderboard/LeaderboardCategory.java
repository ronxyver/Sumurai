package com.sumurai.leaderboard;

import java.util.Comparator;

public enum LeaderboardCategory {
    HIGHEST_LEVEL("Highest Level",
            "Ranked by level, then accuracy",
            Comparator.comparingInt(LeaderboardEntry::getLevel).reversed()
                    .thenComparing(Comparator.comparing(LeaderboardEntry::hasAccuracyData).reversed())
                    .thenComparing(Comparator.comparingDouble(LeaderboardEntry::getAccuracy).reversed())
                    .thenComparing(LeaderboardEntry::getUsername, String.CASE_INSENSITIVE_ORDER)),
    BEST_ACCURACY("Best Accuracy",
            "Ranked by measured accuracy, then level",
            Comparator.comparing(LeaderboardEntry::hasAccuracyData).reversed()
                    .thenComparing(Comparator.comparingDouble(LeaderboardEntry::getAccuracy).reversed())
                    .thenComparing(Comparator.comparingInt(LeaderboardEntry::getLevel).reversed())
                    .thenComparing(LeaderboardEntry::getUsername, String.CASE_INSENSITIVE_ORDER)),
    FASTEST_AVERAGE("Fastest Average",
            "Ranked by average answer time, then accuracy",
            Comparator.comparing(LeaderboardEntry::hasTimingData).reversed()
                    .thenComparingDouble(LeaderboardEntry::getSortTime)
                    .thenComparing(Comparator.comparingDouble(LeaderboardEntry::getAccuracy).reversed())
                    .thenComparing(LeaderboardEntry::getUsername, String.CASE_INSENSITIVE_ORDER));

    private final String label;
    private final String description;
    private final Comparator<LeaderboardEntry> comparator;

    LeaderboardCategory(String label, String description, Comparator<LeaderboardEntry> comparator) {
        this.label = label;
        this.description = description;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public Comparator<LeaderboardEntry> getComparator() {
        return comparator;
    }
}
