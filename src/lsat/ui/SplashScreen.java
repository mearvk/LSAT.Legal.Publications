package lsat.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * SplashScreen — Displays the LSAT logo prominently on a teal-gradient
 * background for a few seconds before the main application loads.
 */
public class SplashScreen extends JWindow {

    private static final int SPLASH_WIDTH = 520;
    private static final int SPLASH_HEIGHT = 440;
    private static final int DISPLAY_DURATION_MS = 3000; // 3 seconds

    private static final Color COLOR_TEAL_DARK = new Color(20, 90, 85);
    private static final Color COLOR_TEAL_LIGHT = new Color(45, 160, 145);
    private static final Color COLOR_TEXT = new Color(230, 245, 240);
    private static final Color COLOR_SUBTITLE = new Color(180, 215, 210);

    private BufferedImage logoImage;
    private float opacity = 0f;
    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private Timer holdTimer;
    private Runnable onComplete;

    public SplashScreen() {
        setSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        setLocationRelativeTo(null);

        // Load the logo
        try {
            logoImage = ImageIO.read(new File("images/logo-orb.png"));
        } catch (IOException e) {
            System.err.println("SplashScreen: Could not load logo-orb.png: " + e.getMessage());
        }

        // Make the window rounded on supported platforms
        try {
            setShape(new RoundRectangle2D.Double(0, 0, SPLASH_WIDTH, SPLASH_HEIGHT, 30, 30));
        } catch (UnsupportedOperationException ignored) {
            // Shaped windows not supported; proceed with rectangular
        }

        // Custom painted content
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                int w = getWidth();
                int h = getHeight();

                // Radial gradient background: teal center, darker edges
                GradientPaint gp = new GradientPaint(
                        w / 2f, h * 0.3f, COLOR_TEAL_LIGHT,
                        w / 2f, h, COLOR_TEAL_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 30, 30);

                // Subtle dark border
                g2.setColor(new Color(10, 50, 50));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 28, 28);

                // Draw logo centered, large
                if (logoImage != null) {
                    int logoSize = 200;
                    int lx = (w - logoSize) / 2;
                    int ly = 50;
                    g2.drawImage(logoImage, lx, ly, logoSize, logoSize, null);
                }

                // Title text
                g2.setColor(COLOR_TEXT);
                g2.setFont(new Font("SansSerif", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                String title = "LSAT Legal Publications";
                int tx = (w - fm.stringWidth(title)) / 2;
                g2.drawString(title, tx, 290);

                // Subtitle
                g2.setColor(COLOR_SUBTITLE);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 15));
                fm = g2.getFontMetrics();
                String subtitle = "Adaptive Ethical Character Evaluation";
                int sx = (w - fm.stringWidth(subtitle)) / 2;
                g2.drawString(subtitle, sx, 320);

                // Copyright
                g2.setColor(new Color(150, 190, 185));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                fm = g2.getFontMetrics();
                String copyright = "\u00A9 2026 Max Rupplin \u2014 All Rights Reserved";
                int cx = (w - fm.stringWidth(copyright)) / 2;
                g2.drawString(copyright, cx, h - 30);

                // Loading dots
                g2.setColor(COLOR_TEXT);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                fm = g2.getFontMetrics();
                String loading = "Loading\u2026";
                int loadx = (w - fm.stringWidth(loading)) / 2;
                g2.drawString(loading, loadx, 360);

                g2.dispose();
            }
        };
        content.setPreferredSize(new Dimension(SPLASH_WIDTH, SPLASH_HEIGHT));
        setContentPane(content);
    }

    /**
     * Displays the splash screen with a fade-in effect, holds, then fades out
     * and calls the onComplete callback.
     */
    public void showSplash(Runnable onComplete) {
        this.onComplete = onComplete;
        this.opacity = 0f;

        setOpacity(0f);
        setVisible(true);

        // Fade in over 500ms
        fadeInTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= 1f) {
                    opacity = 1f;
                    setOpacity(1f);
                    fadeInTimer.stop();
                    startHold();
                } else {
                    setOpacity(opacity);
                }
            }
        });
        fadeInTimer.start();
    }

    private void startHold() {
        // Hold for the display duration
        holdTimer = new Timer(DISPLAY_DURATION_MS, e -> {
            holdTimer.stop();
            startFadeOut();
        });
        holdTimer.setRepeats(false);
        holdTimer.start();
    }

    private void startFadeOut() {
        // Fade out over 400ms
        fadeOutTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.06f;
                if (opacity <= 0f) {
                    opacity = 0f;
                    setOpacity(0f);
                    fadeOutTimer.stop();
                    setVisible(false);
                    dispose();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    setOpacity(opacity);
                }
            }
        });
        fadeOutTimer.start();
    }
}
