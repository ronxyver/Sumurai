// OWNER: Ron Xyver Atienza
package com.sumurai.model;

import java.util.Objects;

public final class UserProfile {
    public static final int MAX_PLAYER_LEVEL = 100;
    public static final int MAX_JOURNEY_STAGE = 100;

    private final String username;
    private final String passwordHash;
    private final BattleStats stats;
    private final boolean guest;
    private int level;
    private long experience;
    private int highestUnlockedStage;

    public UserProfile(String username, String passwordHash, int level, long experience, BattleStats stats) {
        this(username, passwordHash, level, experience, stats, 1);
    }

    public UserProfile(String username, String passwordHash, int level, long experience,
                       BattleStats stats, int highestUnlockedStage) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (level < 1 || level > MAX_PLAYER_LEVEL || experience < 0
                || highestUnlockedStage < 1 || highestUnlockedStage > MAX_JOURNEY_STAGE) {
            throw new IllegalArgumentException("Profile progression is invalid.");
        }
        this.username = username;
        this.passwordHash = passwordHash == null ? "" : passwordHash;
        this.level = level;
        this.experience = experience;
        this.stats = Objects.requireNonNull(stats, "stats");
        this.guest = false;
        this.highestUnlockedStage = highestUnlockedStage;
    }

    private UserProfile() {
        this.username = "Guest";
        this.passwordHash = "";
        this.level = 1;
        this.experience = 0;
        this.stats = new BattleStats();
        this.guest = true;
        this.highestUnlockedStage = 1;
    }

    public static UserProfile guest() {
        return new UserProfile();
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getLevel() {
        return level;
    }

    public long getExperience() {
        return experience;
    }

    public BattleStats getStats() {
        return stats;
    }

    public boolean isGuest() {
        return guest;
    }

    public int getHighestUnlockedStage() {
        return highestUnlockedStage;
    }

    public void recordStageVictory(int stage) {
        if (stage < 1 || stage > MAX_JOURNEY_STAGE) {
            throw new IllegalArgumentException("Stage must be between 1 and "
                    + MAX_JOURNEY_STAGE + ".");
        }
        if (stage == highestUnlockedStage && highestUnlockedStage < MAX_JOURNEY_STAGE) {
            highestUnlockedStage++;
        }
    }

    public void addExperience(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience cannot be negative.");
        }
        if (amount > Long.MAX_VALUE - experience) {
            throw new IllegalArgumentException("Experience overflow.");
        }
        experience += amount;
        while (level < MAX_PLAYER_LEVEL && experience >= (long) level * 100L) {
            level++;
        }
    }
}
