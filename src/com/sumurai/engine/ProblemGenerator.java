// OWNER: Ron Xyver Atienza
package com.sumurai.engine;

import com.sumurai.model.Difficulty;
import com.sumurai.model.MathProblem;
import com.sumurai.model.OperationType;
import com.sumurai.model.UserProfile;

import java.util.Objects;
import java.util.Random;

public final class ProblemGenerator {
    private final Random random;

    public ProblemGenerator() {
        this(new Random());
    }

    public ProblemGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public MathProblem generate(Difficulty difficulty) {
        Objects.requireNonNull(difficulty, "difficulty");
        return generateWithMaximum(difficulty.getMaxOperand());
    }

    public MathProblem generateForLevel(int level) {
        if (level < 1 || level > UserProfile.MAX_JOURNEY_STAGE) {
            throw new IllegalArgumentException("Level must be between 1 and "
                    + UserProfile.MAX_JOURNEY_STAGE + ".");
        }
        return generateWithMaximum(level * 10);
    }

    private MathProblem generateWithMaximum(int max) {
        OperationType operation = OperationType.values()[random.nextInt(OperationType.values().length)];
        int left;
        int right;

        switch (operation) {
            case ADD:
                left = random.nextInt(max) + 1;
                right = random.nextInt(max) + 1;
                break;
            case SUBTRACT:
                left = random.nextInt(max) + 1;
                right = random.nextInt(left + 1);
                break;
            case MULTIPLY:
                left = random.nextInt(max) + 1;
                right = random.nextInt(max) + 1;
                break;
            case DIVIDE:
                right = random.nextInt(max) + 1;
                int quotient = random.nextInt(max / right) + 1;
                left = right * quotient;
                break;
            default:
                throw new AssertionError(operation);
        }

        return new MathProblem(left, right, operation);
    }
}
