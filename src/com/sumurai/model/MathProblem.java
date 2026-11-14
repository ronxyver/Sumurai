// OWNER: Ron Xyver Atienza
package com.sumurai.model;

import java.util.Objects;

public final class MathProblem {
    private final int leftOperand;
    private final int rightOperand;
    private final OperationType operation;
    private final int correctAnswer;

    public MathProblem(int leftOperand, int rightOperand, OperationType operation) {
        this.operation = Objects.requireNonNull(operation, "operation");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.correctAnswer = operation.calculate(leftOperand, rightOperand);
    }

    public int getLeftOperand() {
        return leftOperand;
    }

    public int getRightOperand() {
        return rightOperand;
    }

    public OperationType getOperation() {
        return operation;
    }

    public String getPrompt() {
        return leftOperand + " " + operation.getSymbol() + " " + rightOperand + " = ?";
    }

    public boolean isCorrect(int answer) {
        return answer == correctAnswer;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }
}
