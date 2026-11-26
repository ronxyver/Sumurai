package com.sumurai.leaderboard;

import com.sumurai.model.BattleStats;
import com.sumurai.model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LeaderboardService {
    private final List<LeaderboardEntry> entries;

    public LeaderboardService(List<LeaderboardEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        this.entries = new ArrayList<>(entries.size());
        for (LeaderboardEntry entry : entries) {
            this.entries.add(Objects.requireNonNull(entry, "entry"));
        }
    }

    public static LeaderboardService fromProfiles(List<UserProfile> profiles) {
        Objects.requireNonNull(profiles, "profiles");
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (UserProfile profile : profiles) {
            Objects.requireNonNull(profile, "profile");
            BattleStats stats = profile.getStats();
            boolean metricsAvailable = stats.hasAnswerMetrics();
            entries.add(new LeaderboardEntry(profile.getUsername(), profile.getLevel(),
                    stats.getAccuracy(), stats.getAverageTime(),
                    metricsAvailable, metricsAvailable));
        }
        return new LeaderboardService(entries);
    }

    public List<LeaderboardEntry> getRanked(LeaderboardCategory category) {
        Objects.requireNonNull(category, "category");
        List<LeaderboardEntry> ranked = new ArrayList<>(entries);
        ranked.sort(category.getComparator());
        return ranked;
    }

    public List<LeaderboardEntry> getTop(LeaderboardCategory category, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Leaderboard count cannot be negative.");
        }
        List<LeaderboardEntry> ranked = getRanked(category);
        return new ArrayList<>(ranked.subList(0, Math.min(count, ranked.size())));
    }

    public int getRankOf(LeaderboardCategory category, String username) {
        if (username == null) {
            return 0;
        }
        List<LeaderboardEntry> ranked = getRanked(category);
        for (int index = 0; index < ranked.size(); index++) {
            if (ranked.get(index).getUsername().equalsIgnoreCase(username)) {
                return index + 1;
            }
        }
        return 0;
    }

}
