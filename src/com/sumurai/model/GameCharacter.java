// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public abstract class GameCharacter {
    private final String name;
    private final int maxHealth;
    private int health;
    private int shieldPoints;

    protected GameCharacter(String name, int maxHealth) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Character name is required.");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Maximum health must be positive.");
        }
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getShieldPoints() {
        return shieldPoints;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        int remaining = amount;
        int shieldDamage = Math.min(shieldPoints, remaining);
        shieldPoints -= shieldDamage;
        remaining -= shieldDamage;
        int oldHealth = health;
        health = Math.max(0, health - remaining);
        return oldHealth - health;
    }

    public int heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing cannot be negative.");
        }
        int oldHealth = health;
        health = Math.min(maxHealth, health + amount);
        return health - oldHealth;
    }

    public void addShield(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Shield cannot be negative.");
        }
        shieldPoints += amount;
    }
}
