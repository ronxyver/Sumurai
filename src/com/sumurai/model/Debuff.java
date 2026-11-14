// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public final class Debuff extends Move {
    private final int remainingTurns;

    public Debuff(int magnitude, int remainingTurns) {
        super("Debuff", magnitude);
        if (remainingTurns <= 0) {
            throw new IllegalArgumentException("Debuff duration must be positive.");
        }
        this.remainingTurns = remainingTurns;
    }

    public int getMagnitude() {
        return getPower();
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }
}
