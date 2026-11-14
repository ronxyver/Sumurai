// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public final class Buff extends Move {
    private final int remainingTurns;

    public Buff(int magnitude, int remainingTurns) {
        super("Buff", magnitude);
        if (remainingTurns <= 0) {
            throw new IllegalArgumentException("Buff duration must be positive.");
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
