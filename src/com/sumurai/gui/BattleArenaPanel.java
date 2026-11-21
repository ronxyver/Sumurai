// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BasicAttack;
import com.sumurai.model.Buff;
import com.sumurai.model.Debuff;
import com.sumurai.model.Heal;
import com.sumurai.model.Move;
import com.sumurai.model.Shield;
import com.sumurai.model.SpecialAttack;

import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;

/** Renders the battle scene and controls temporary character sprite animations. */
public final class BattleArenaPanel extends JPanel {
    private final JLabel playerSprite = new JLabel();
    private final JLabel opponentSprite = new JLabel();
    private final Timer animationTimer;
    private Image backgroundImage;
    private String opponentFolderName;
    private int animationStep;

    public BattleArenaPanel(String opponentName) {
        String displayName = opponentName == null || opponentName.trim().isEmpty()
                ? "Apprentice"
                : opponentName.trim();
        this.opponentFolderName = displayName.toLowerCase(Locale.ROOT);
        setOpaque(true);
        setLayout(new BorderLayout());

        // === BACKGROUND START ===
        URL backgroundResource = getClass().getResource("/images/Ink-WashRoninInFallingLeaves.gif");
        if (backgroundResource != null) {
            backgroundImage = new ImageIcon(backgroundResource).getImage();
        } else {
            backgroundImage = new ImageIcon(
                    "assets/images/Ink-WashRoninInFallingLeaves.gif").getImage();
        }
        // === BACKGROUND END ===

        JLabel stageLabel = new JLabel("STAGE 1  /  " + displayName, SwingConstants.CENTER);
        stageLabel.setForeground(java.awt.Color.WHITE);
        stageLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        stageLabel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        add(stageLabel, BorderLayout.NORTH);

        playerSprite.setHorizontalAlignment(SwingConstants.CENTER);
        opponentSprite.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel fighters = new JPanel(new java.awt.GridLayout(1, 2));
        fighters.setOpaque(false);
        fighters.add(playerSprite);
        fighters.add(opponentSprite);
        add(fighters, BorderLayout.CENTER);

        animationTimer = new Timer(180, event -> advanceAnimation());
        animationTimer.setRepeats(true);
        setIdle();
    }

    public void playPlayerMove(Move move) {
        Objects.requireNonNull(move, "move");
        String animation = animationFor(move);
        boolean opponentShouldBeHurt =
                move instanceof BasicAttack || move instanceof SpecialAttack;
        startAnimation("player", animation,
                opponentShouldBeHurt ? "hurt" : null);
    }

    public ImageIcon moveIcon(Move move) {
        Objects.requireNonNull(move, "move");
        return loadSprite("player", animationFor(move), 1);
    }

    public void playPlayerHurt() {
        startAnimation("player", "hurt", null);
    }

    public void setIdle() {
        animationTimer.stop();
        animationStep = 0;
        setSprite(playerSprite, "player", "idle", 1);
        setSprite(opponentSprite, opponentFolderName, "idle", 1);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        // === BACKGROUND START ===
        if (backgroundImage != null) {
            graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        // === BACKGROUND END ===
    }

    private void startAnimation(String playerFolder, String playerAnimation,
                                String opponentAnimation) {
        animationTimer.stop();
        animationStep = 0;
        advanceAnimation(playerFolder, playerAnimation, opponentAnimation);
        animationTimer.start();
    }

    private String currentPlayerAnimation;
    private String currentOpponentAnimation;

    private void advanceAnimation(String playerFolder, String playerAnimation,
                                  String opponentAnimation) {
        currentPlayerAnimation = playerAnimation;
        currentOpponentAnimation = opponentAnimation;
        setSprite(playerSprite, playerFolder, playerAnimation, 1);
        if (opponentAnimation != null) {
            setSprite(opponentSprite, opponentFolderName, opponentAnimation, 1);
        } else {
            setSprite(opponentSprite, opponentFolderName, "idle", 1);
        }
    }

    private void advanceAnimation() {
        animationStep++;
        if (animationStep >= 6) {
            setIdle();
            return;
        }
        int frame = (animationStep % 2) + 1;
        setSprite(playerSprite, "player", currentPlayerAnimation, frame);
        if (currentOpponentAnimation != null) {
            setSprite(opponentSprite, opponentFolderName, currentOpponentAnimation, frame);
        }
    }

    private void setSprite(JLabel label, String character, String animation, int frame) {
        label.setIcon(loadSprite(character, animation, frame));
    }

    private ImageIcon loadSprite(String character, String animation, int frame) {
        String resourcePath = "/images/sprites/" + character + "/" + animation
                + "/" + animation + "_" + frame + ".png";
        URL resource = getClass().getResource(resourcePath);
        if (resource != null) {
            return new ImageIcon(resource);
        }
        return new ImageIcon("assets" + resourcePath);
    }

    private String animationFor(Move move) {
        if (move instanceof BasicAttack || move instanceof SpecialAttack) {
            return "attack";
        }
        if (move instanceof Heal) {
            return "heal";
        }
        if (move instanceof Shield) {
            return "shield";
        }
        if (move instanceof Buff) {
            return "buff";
        }
        if (move instanceof Debuff) {
            return "debuff";
        }
        throw new IllegalArgumentException("Unsupported move type: "
                + move.getClass().getName());
    }
}