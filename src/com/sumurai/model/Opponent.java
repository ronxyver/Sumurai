// OWNER: Ron Xyver Atienza
package com.sumurai.model;

import java.util.Objects;

public abstract class Opponent extends GameCharacter {
    private final Difficulty difficulty;
    private final int damagePerTurn;

    protected Opponent(String name, int maxHealth, Difficulty difficulty, int damagePerTurn) {
        super(name, maxHealth);
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        if (damagePerTurn <= 0) {
            throw new IllegalArgumentException("Opponent damage must be positive.");
        }
        this.damagePerTurn = damagePerTurn;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getDamagePerTurn() {
        return damagePerTurn;
    }
}
