// OWNER: Ron Xyver Atienza
package com.sumurai.model;

import java.util.Objects;

public final class UserProfile {
    private final String username;
    private final String passwordHash;
    private final BattleStats stats;
    private final boolean guest;
    private int level;
    private long experience;

    public UserProfile(String username, String passwordHash, int level, long experience, BattleStats stats) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (level < 1 || experience < 0) {
            throw new IllegalArgumentException("Level and experience are invalid.");
        }
        this.username = username;
        this.passwordHash = passwordHash == null ? "" : passwordHash;
        this.level = level;
        this.experience = experience;
        this.stats = Objects.requireNonNull(stats, "stats");
        this.guest = false;
    }

    private UserProfile() {
        this.username = "Guest";
        this.passwordHash = "";
        this.level = 1;
        this.experience = 0;
        this.stats = new BattleStats();
        this.guest = true;
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

    public void addExperience(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Experience cannot be negative.");
        }
        experience += amount;
        while (experience >= level * 100L) {
            level++;
        }
    }
}
