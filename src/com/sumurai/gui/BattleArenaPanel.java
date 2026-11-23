// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.BasicAttack;
import com.sumurai.model.Buff;
import com.sumurai.model.Debuff;
import com.sumurai.model.Heal;
import com.sumurai.model.Move;
import com.sumurai.model.Shield;
import com.sumurai.model.SpecialAttack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/** Renders the battle scene and controls temporary character sprite animations. */
public final class BattleArenaPanel extends JPanel {
    private static final int ANIMATION_FRAME_DELAY = 180;
    private static final int SPECIAL_ATTACK_FRAME_DELAY = 45;
    private static final int SPECIAL_ATTACK_FRAME_COUNT = 81;
    private static final int SPECIAL_ATTACK_FRAME_WIDTH = 400;
    private static final int SPECIAL_ATTACK_FRAME_HEIGHT = 226;

    private final JLabel playerSprite = new JLabel();
    private final JLabel opponentSprite = new JLabel();
    private final JPanel fighters = new JPanel(new java.awt.GridLayout(1, 2));
    private final SpecialAttackOverlay specialAttackOverlay = new SpecialAttackOverlay();
    private final List<BufferedImage> specialAttackFrames = new ArrayList<>();
    private final JLayeredPane fighterLayers = new JLayeredPane() {
        @Override
        public void doLayout() {
            fighters.setBounds(0, 0, getWidth(), getHeight());
            specialAttackOverlay.setBounds(0, 0, getWidth(), getHeight());
        }
    };
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
        fighters.setOpaque(false);
        fighters.add(playerSprite);
        fighters.add(opponentSprite);

        animationTimer = new Timer(180, event -> advanceAnimation());
        animationTimer.setRepeats(true);
        fighterLayers.setLayout(null);
        specialAttackOverlay.setOpaque(false);
        specialAttackOverlay.setVisible(false);
        fighterLayers.add(fighters, JLayeredPane.DEFAULT_LAYER);
        fighterLayers.add(specialAttackOverlay, JLayeredPane.PALETTE_LAYER);
        add(fighterLayers, BorderLayout.CENTER);
        setIdle();
        preloadSpecialAttackFrames();
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
        String animation = animationFor(move);
        return loadSprite("player",
                "specialAttack".equals(animation) ? "attack" : animation, 1);
    }

    public void playPlayerHurt() {
        startAnimation("player", "hurt", null);
    }

    public void setIdle() {
        animationTimer.stop();
        animationTimer.setDelay(ANIMATION_FRAME_DELAY);
        animationStep = 0;
        specialAttackOverlay.setVisible(false);
        playerSprite.setVisible(true);
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
        if ("specialAttack".equals(playerAnimation)) {
            if (!specialAttackFrames.isEmpty()) {
                animationTimer.setDelay(SPECIAL_ATTACK_FRAME_DELAY);
                animationTimer.start();
            }
        } else {
            animationTimer.setDelay(ANIMATION_FRAME_DELAY);
            animationTimer.start();
        }
    }

    private String currentPlayerAnimation;
    private String currentOpponentAnimation;

    private void advanceAnimation(String playerFolder, String playerAnimation,
                                  String opponentAnimation) {
        currentPlayerAnimation = playerAnimation;
        currentOpponentAnimation = opponentAnimation;
        if ("specialAttack".equals(playerAnimation)) {
            if (specialAttackFrames.isEmpty()) {
                specialAttackOverlay.setVisible(false);
                playerSprite.setVisible(true);
                playerSprite.setIcon(loadSprite("player", "attack", 1));
            } else {
                playerSprite.setVisible(false);
                specialAttackOverlay.setFrame(specialAttackFrames.get(0));
                specialAttackOverlay.setVisible(true);
            }
            if (opponentAnimation != null) {
                setSprite(opponentSprite, opponentFolderName, opponentAnimation, 1);
            } else {
                setSprite(opponentSprite, opponentFolderName, "idle", 1);
            }
            return;
        }
        specialAttackOverlay.setVisible(false);
        playerSprite.setVisible(true);
        setSprite(playerSprite, playerFolder, playerAnimation, 1);
        if (opponentAnimation != null) {
            setSprite(opponentSprite, opponentFolderName, opponentAnimation, 1);
        } else {
            setSprite(opponentSprite, opponentFolderName, "idle", 1);
        }
    }

    private void advanceAnimation() {
        animationStep++;
        if ("specialAttack".equals(currentPlayerAnimation)) {
            if (animationStep >= specialAttackFrames.size()) {
                setIdle();
                return;
            }
            specialAttackOverlay.setFrame(specialAttackFrames.get(animationStep));
            return;
        }
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
        if (move instanceof BasicAttack) {
            return "attack";
        }
        if (move instanceof SpecialAttack) {
            return "specialAttack";
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

    private void preloadSpecialAttackFrames() {
        SwingWorker<List<BufferedImage>, Void> loader =
                new SwingWorker<List<BufferedImage>, Void>() {
                    @Override
                    protected List<BufferedImage> doInBackground() throws IOException {
                        return loadSpecialAttackFrames();
                    }

                    @Override
                    protected void done() {
                        try {
                            specialAttackFrames.addAll(get());
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(
                                    "Interrupted while loading special attack frames.",
                                    exception);
                        } catch (ExecutionException exception) {
                            throw new IllegalStateException(
                                    "Unable to load the special attack animation.",
                                    exception.getCause());
                        }
                        if ("specialAttack".equals(currentPlayerAnimation)) {
                            animationStep = 0;
                            playerSprite.setVisible(false);
                            specialAttackOverlay.setFrame(specialAttackFrames.get(0));
                            specialAttackOverlay.setVisible(true);
                            animationTimer.setDelay(SPECIAL_ATTACK_FRAME_DELAY);
                            animationTimer.start();
                        }
                    }
                };
        loader.execute();
    }

    private List<BufferedImage> loadSpecialAttackFrames() throws IOException {
        List<BufferedImage> frames = new ArrayList<>(SPECIAL_ATTACK_FRAME_COUNT);
        for (int frame = 1; frame <= SPECIAL_ATTACK_FRAME_COUNT; frame++) {
            String resourcePath = String.format(Locale.ROOT,
                    "/images/sprites/player/specialAttack/ezgif-frame-%03d.png", frame);
            URL resource = getClass().getResource(resourcePath);
            BufferedImage source = resource == null
                    ? ImageIO.read(new File("assets" + resourcePath))
                    : ImageIO.read(resource);
            if (source == null) {
                throw new IOException("Unable to decode special attack frame " + frame + ".");
            }
            BufferedImage transparentFrame = removeLightBackground(source);
            BufferedImage scaledFrame = new BufferedImage(
                    SPECIAL_ATTACK_FRAME_WIDTH, SPECIAL_ATTACK_FRAME_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaledFrame.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.drawImage(transparentFrame, 0, 0,
                        SPECIAL_ATTACK_FRAME_WIDTH, SPECIAL_ATTACK_FRAME_HEIGHT, null);
            } finally {
                graphics.dispose();
                source.flush();
                transparentFrame.flush();
            }
            frames.add(scaledFrame);
        }
        return frames;
    }

    private BufferedImage removeLightBackground(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = source.getRGB(0, 0, width, height, null, 0, width);
        int[] backgroundQueue = new int[pixels.length];
        int queueEnd = 0;

        for (int x = 0; x < width; x++) {
            queueEnd = addBackgroundPixel(x, pixels, backgroundQueue, queueEnd);
            queueEnd = addBackgroundPixel((height - 1) * width + x,
                    pixels, backgroundQueue, queueEnd);
        }
        for (int y = 1; y < height - 1; y++) {
            queueEnd = addBackgroundPixel(y * width, pixels, backgroundQueue, queueEnd);
            queueEnd = addBackgroundPixel(y * width + width - 1,
                    pixels, backgroundQueue, queueEnd);
        }

        int queueStart = 0;
        while (queueStart < queueEnd) {
            int index = backgroundQueue[queueStart++];
            int x = index % width;
            int y = index / width;
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                int neighborY = y + offsetY;
                if (neighborY < 0 || neighborY >= height) {
                    continue;
                }
                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    int neighborX = x + offsetX;
                    if ((offsetX == 0 && offsetY == 0)
                            || neighborX < 0 || neighborX >= width) {
                        continue;
                    }
                    queueEnd = addBackgroundPixel(
                            neighborY * width + neighborX,
                            pixels, backgroundQueue, queueEnd);
                }
            }
        }

        BufferedImage result = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, pixels, 0, width);
        return result;
    }

    private int addBackgroundPixel(int index, int[] pixels, int[] queue, int queueEnd) {
        int pixel = pixels[index];
        if ((pixel >>> 24) == 0 || !isLightBackground(pixel)) {
            return queueEnd;
        }
        pixels[index] = pixel & 0x00ffffff;
        queue[queueEnd] = index;
        return queueEnd + 1;
    }

    private boolean isLightBackground(int pixel) {
        int red = (pixel >>> 16) & 0xff;
        int green = (pixel >>> 8) & 0xff;
        int blue = pixel & 0xff;
        int minimum = Math.min(red, Math.min(green, blue));
        int maximum = Math.max(red, Math.max(green, blue));
        return minimum >= 230 && maximum - minimum <= 28;
    }

    private final class SpecialAttackOverlay extends JPanel {
        private BufferedImage frame;

        private void setFrame(BufferedImage frame) {
            this.frame = frame;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (frame == null || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            double scale = Math.min(getWidth() / (double) frame.getWidth(),
                    getHeight() / (double) frame.getHeight());
            int width = (int) Math.round(frame.getWidth() * scale);
            int height = (int) Math.round(frame.getHeight() * scale);
            int x = (getWidth() - width) / 2 - width / 4;
            int y = (getHeight() - height) / 2;
            graphics.drawImage(frame, x, y, width, height, this);
        }
    }
}
