// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public abstract class Move {
    private final String name;
    private final int power;

    protected Move(String name, int power) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Move name is required.");
        }
        if (power <= 0) {
            throw new IllegalArgumentException("Move power must be positive.");
        }
        this.name = name;
        this.power = power;
    }

    public final String getName() {
        return name;
    }

    public final int getPower() {
        return power;
    }
}
