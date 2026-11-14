// OWNER: Ron Xyver Atienza
package com.sumurai.model;

public enum OperationType {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/");

    private final String symbol;

    OperationType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public int calculate(int left, int right) {
        switch (this) {
            case ADD:
                return left + right;
            case SUBTRACT:
                return left - right;
            case MULTIPLY:
                return left * right;
            case DIVIDE:
                if (right == 0 || left % right != 0) {
                    throw new ArithmeticException("Division must have a nonzero exact divisor.");
                }
                return left / right;
            default:
                throw new AssertionError(this);
        }
    }
}
