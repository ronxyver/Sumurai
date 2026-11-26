// OWNER: Ron Xyver Atienza
package com.sumurai.engine;

import com.sumurai.model.BattleOutcome;
import com.sumurai.model.BasicAttack;
import com.sumurai.model.Buff;
import com.sumurai.model.Debuff;
import com.sumurai.model.GameCharacter;
import com.sumurai.model.Heal;
import com.sumurai.model.InvalidAnswerException;
import com.sumurai.model.MathProblem;
import com.sumurai.model.Move;
import com.sumurai.model.Opponent;
import com.sumurai.model.Player;
import com.sumurai.model.Shield;
import com.sumurai.model.SpecialAttack;

import java.util.Objects;

public final class BattleEngine {
    private final ProblemGenerator problemGenerator;
    private Player player;
    private Opponent opponent;
    private MathProblem currentProblem;
    private BattleOutcome outcome = BattleOutcome.IN_PROGRESS;
    private int turns;
    private int bonusDamage;
    private int bonusDamageTurns;
    private int reducedOpponentDamage;
    private int reducedOpponentDamageTurns;
    private boolean battleRecorded;
    private long problemStartedAtNanos;

    public BattleEngine(ProblemGenerator problemGenerator) {
        this.problemGenerator = Objects.requireNonNull(problemGenerator, "problemGenerator");
    }

    public MathProblem startBattle(Player player, Opponent opponent) {
        this.player = Objects.requireNonNull(player, "player");
        this.opponent = Objects.requireNonNull(opponent, "opponent");
        this.turns = 0;
        this.bonusDamage = 0;
        this.bonusDamageTurns = 0;
        this.reducedOpponentDamage = 0;
        this.reducedOpponentDamageTurns = 0;
        this.battleRecorded = false;
        this.outcome = BattleOutcome.IN_PROGRESS;
        this.currentProblem = problemGenerator.generate(opponent.getDifficulty());
        this.problemStartedAtNanos = System.nanoTime();
        return currentProblem;
    }

    public BattleOutcome submitAnswer(String answer, Move move) throws InvalidAnswerException {
        ensureBattleActive();
        Objects.requireNonNull(move, "move");
        final int numericAnswer;
        try {
            numericAnswer = Integer.parseInt(answer == null ? "" : answer.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidAnswerException("Enter a whole-number answer.", exception);
        }

        turns++;
        boolean correct = currentProblem.isCorrect(numericAnswer);
        long answerTimeMillis = Math.max(1,
                (System.nanoTime() - problemStartedAtNanos) / 1_000_000L);
        player.getProfile().getStats().recordAnswer(correct, answerTimeMillis);
        if (correct) {
            applyMove(move);
        } else {
            attack(player, opponent.getDamagePerTurn());
            reduceDebuffDuration();
        }

        if (!player.isAlive()) {
            finish(BattleOutcome.OPPONENT_WON);
        } else if (!opponent.isAlive()) {
            finish(BattleOutcome.PLAYER_WON);
        } else {
            currentProblem = problemGenerator.generate(opponent.getDifficulty());
            problemStartedAtNanos = System.nanoTime();
        }
        return outcome;
    }

    public BattleOutcome endBattleByTimeout() {
        ensureBattleActive();
        finish(BattleOutcome.OPPONENT_WON);
        return outcome;
    }

    public MathProblem getCurrentProblem() {
        return currentProblem;
    }

    public Player getPlayer() {
        return player;
    }

    public Opponent getOpponent() {
        return opponent;
    }

    public BattleOutcome getOutcome() {
        return outcome;
    }

    public int getTurns() {
        return turns;
    }

    private void applyMove(Move move) {
        if (move instanceof BasicAttack || move instanceof SpecialAttack) {
            int damage = move.getPower() + (bonusDamageTurns > 0 ? bonusDamage : 0);
            opponent.takeDamage(damage);
            reduceBonusDuration();
        } else if (move instanceof Heal) {
            player.heal(move.getPower());
        } else if (move instanceof Shield) {
            player.addShield(move.getPower());
        } else if (move instanceof Buff) {
            Buff buff = (Buff) move;
            bonusDamage = buff.getMagnitude();
            bonusDamageTurns = buff.getRemainingTurns();
        } else if (move instanceof Debuff) {
            Debuff debuff = (Debuff) move;
            reducedOpponentDamage = debuff.getMagnitude();
            reducedOpponentDamageTurns = debuff.getRemainingTurns();
        } else {
            throw new IllegalArgumentException("Unsupported move type: " + move.getClass().getName());
        }
    }

    private void attack(GameCharacter target, int baseDamage) {
        int damage = baseDamage;
        if (reducedOpponentDamageTurns > 0) {
            damage = Math.max(1, damage - reducedOpponentDamage);
        }
        target.takeDamage(damage);
    }

    private void reduceBonusDuration() {
        if (bonusDamageTurns > 0 && --bonusDamageTurns == 0) {
            bonusDamage = 0;
        }
    }

    private void reduceDebuffDuration() {
        if (reducedOpponentDamageTurns > 0 && --reducedOpponentDamageTurns == 0) {
            reducedOpponentDamage = 0;
        }
    }

    private void finish(BattleOutcome finalOutcome) {
        outcome = finalOutcome;
        if (!battleRecorded) {
            player.getProfile().getStats().recordBattle(finalOutcome);
            if (finalOutcome == BattleOutcome.PLAYER_WON) {
                player.getProfile().addExperience(25);
            }
            battleRecorded = true;
        }
    }

    private void ensureBattleActive() {
        if (player == null || opponent == null || outcome != BattleOutcome.IN_PROGRESS) {
            throw new IllegalStateException("There is no active battle.");
        }
    }
}
