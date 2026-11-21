// OWNER: Franchesca Denise B. Manila
package com.sumurai.gui;

import com.sumurai.model.UserProfile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Objects;
import java.util.function.IntConsumer;

/** Renders the 100-stage journey path with scrollbar, wheel, and mouse-drag navigation. */
public final class LevelPathPanel extends JPanel {
    private static final int STAGE_COUNT = UserProfile.MAX_JOURNEY_STAGE;
    private static final int STAGE_SPACING = 132;
    private static final Color CARD_COLOR = new Color(39, 42, 63);
    private static final Color NODE_COLOR = new Color(86, 126, 234);
    private static final Color LOCKED_COLOR = new Color(95, 98, 116);

    public LevelPathPanel(UserProfile profile, IntConsumer onLevelSelected) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(onLevelSelected, "onLevelSelected");
        setOpaque(false);
        setLayout(new BorderLayout(0, 10));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Level Path");
        title.setForeground(ThemeManager.textColor());
        title.setFont(ThemeManager.headingFont(22));
        JLabel progress = new JLabel(profile.getHighestUnlockedStage()
                + " / " + STAGE_COUNT + " stages unlocked");
        progress.setForeground(new Color(185, 190, 210));
        heading.add(title, BorderLayout.WEST);
        heading.add(progress, BorderLayout.EAST);
        add(heading, BorderLayout.NORTH);

        PathTrack track = new PathTrack();
        DragScroller dragScroller = new DragScroller(track);
        track.addMouseListener(dragScroller);
        track.addMouseMotionListener(dragScroller);
        track.addMouseWheelListener(dragScroller);
        for (int stage = 1; stage <= STAGE_COUNT; stage++) {
            final int selectedStage = stage;
            boolean unlocked = stage <= profile.getHighestUnlockedStage();
            JButton node = new JButton(Integer.toString(stage));
            node.setBounds(24 + (stage - 1) * STAGE_SPACING, 44, 64, 64);
            node.setFont(ThemeManager.headingFont(unlocked ? 22 : 12));
            node.setForeground(Color.WHITE);
            node.setBackground(unlocked ? NODE_COLOR : LOCKED_COLOR);
            node.setBorder(BorderFactory.createLineBorder(
                    unlocked ? new Color(159, 183, 255) : new Color(130, 132, 145), 2));
            node.setFocusPainted(false);
            node.setEnabled(unlocked);
            node.setToolTipText(unlocked
                    ? "Start level " + stage
                    : "Win the previous level to unlock this stage.");
            node.addActionListener(event -> {
                if (!dragScroller.wasDragged()) {
                    onLevelSelected.accept(selectedStage);
                }
            });
            node.addMouseListener(dragScroller);
            node.addMouseMotionListener(dragScroller);
            node.addMouseWheelListener(dragScroller);
            track.add(node);

            JLabel label = new JLabel("Level " + stage);
            label.setForeground(unlocked ? ThemeManager.textColor() : LOCKED_COLOR);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setBounds(8 + (stage - 1) * STAGE_SPACING, 114, 96, 24);
            label.addMouseWheelListener(dragScroller);
            track.add(label);
        }

        JScrollPane scrollPane = new JScrollPane(track,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(390, 175));
        scrollPane.addMouseWheelListener(dragScroller);
        add(scrollPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            int stageCenter = 24 + (profile.getHighestUnlockedStage() - 1) * STAGE_SPACING + 32;
            int maxViewX = Math.max(0, track.getWidth() - viewport.getExtentSize().width);
            int viewX = Math.max(0, Math.min(maxViewX,
                    stageCenter - viewport.getExtentSize().width / 2));
            viewport.setViewPosition(new Point(viewX, 0));
        });
    }

    private static final class PathTrack extends JPanel {
        private PathTrack() {
            setLayout(null);
            setOpaque(true);
            setBackground(CARD_COLOR);
            setPreferredSize(new Dimension(24 + STAGE_COUNT * STAGE_SPACING, 164));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(110, 125, 176));
            g.setStroke(new BasicStroke(3));
            int centerY = 76;
            for (int stage = 0; stage < STAGE_COUNT - 1; stage++) {
                int startX = 24 + stage * STAGE_SPACING + 64;
                int endX = 24 + (stage + 1) * STAGE_SPACING;
                g.drawLine(startX, centerY, endX, centerY);
            }
            g.dispose();
        }
    }

    private static final class DragScroller extends MouseAdapter {
        private final JPanel track;
        private JViewport viewport;
        private int pressX;
        private int initialViewX;
        private boolean dragging;

        private DragScroller(JPanel track) {
            this.track = track;
        }

        @Override
        public void mousePressed(MouseEvent event) {
            viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, track);
            pressX = SwingUtilities.convertPoint(
                    (Component) event.getSource(), event.getPoint(), track).x;
            initialViewX = viewport == null ? 0 : viewport.getViewPosition().x;
            dragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            if (viewport == null) {
                return;
            }
            int currentX = SwingUtilities.convertPoint(
                    (Component) event.getSource(), event.getPoint(), track).x;
            int delta = pressX - currentX;
            if (Math.abs(delta) > 4) {
                dragging = true;
            }
            if (dragging) {
                int maxViewX = Math.max(0, viewport.getView().getWidth()
                        - viewport.getExtentSize().width);
                int nextX = Math.max(0, Math.min(maxViewX, initialViewX + delta));
                viewport.setViewPosition(new Point(nextX, 0));
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            if (viewport == null) {
                viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, track);
            }
            if (viewport == null) {
                return;
            }
            int delta = event.getWheelRotation() * 40;
            int maxViewX = Math.max(0, viewport.getView().getWidth()
                    - viewport.getExtentSize().width);
            int nextX = Math.max(0, Math.min(maxViewX,
                    viewport.getViewPosition().x + delta));
            viewport.setViewPosition(new Point(nextX, 0));
            event.consume();
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            if (dragging) {
                SwingUtilities.invokeLater(() -> dragging = false);
            }
        }

        private boolean wasDragged() {
            return dragging;
        }
    }
}
