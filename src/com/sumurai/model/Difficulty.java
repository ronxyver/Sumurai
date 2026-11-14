// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public enum Difficulty {
    EASY(10),
    MEDIUM(20),
    HARD(50);

    private final int maxOperand;

    Difficulty(int maxOperand) {
        this.maxOperand = maxOperand;
    }

    public int getMaxOperand() {
        return maxOperand;
    }
}
