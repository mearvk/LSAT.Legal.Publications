package lsat.ui;

import lsat.engine.MoralCurveFilter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * AdminCurvePanel — Pre-test admin interface for adjusting the moral curve.
 * Provides 10 sliders (one per control point) and a live graph preview
 * showing the normal distribution curve shape. Admin adjusts before the
 * test-taker begins.
 */
public class AdminCurvePanel extends JPanel {

    private final MoralCurveFilter curveFilter;
    private final JSlider[] sliders;
    private final JLabel[] valueLabels;
    private final CurveGraphPanel graphPanel;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    /**
     * @param curveFilter the shared MoralCurveFilter instance to adjust
     * @param onConfirm   called when admin clicks "Apply & Continue"
     * @param onCancel    called when admin clicks "Cancel"
     */
    public AdminCurvePanel(MoralCurveFilter curveFilter, Runnable onConfirm, Runnable onCancel) {
        this.curveFilter = curveFilter;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.sliders = new JSlider[MoralCurveFilter.NUM_POINTS];
        this.valueLabels = new JLabel[MoralCurveFilter.NUM_POINTS];

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(255, 255, 255));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // Header
        JLabel header = new JLabel("Admin: Moral Curve Adjustment");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(new Color(15, 83, 25));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel subheader = new JLabel("Adjust the 10 control points of the scoring distribution (modulus filter)");
        subheader.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subheader.setForeground(new Color(82, 81, 80));
        subheader.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        subheader.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(header);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subheader);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Graph panel (live curve visualization)
        graphPanel = new CurveGraphPanel(curveFilter);
        graphPanel.setPreferredSize(new Dimension(0, 180));

        // Sliders panel
        JPanel slidersPanel = createSlidersPanel();
        JScrollPane scrollPane = new JScrollPane(slidersPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();

        // Layout
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(graphPanel, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JPanel createSlidersPanel() {
        JPanel panel = new JPanel(new GridLayout(MoralCurveFilter.NUM_POINTS, 1, 0, 12));
        panel.setOpaque(false);

        for (int i = 0; i < MoralCurveFilter.NUM_POINTS; i++) {
            panel.add(createSliderRow(i));
        }

        return panel;
    }

    private JPanel createSliderRow(int index) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 12, 6, 12));

        // Label
        JLabel label = new JLabel(MoralCurveFilter.POINT_LABELS[index]);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(64, 63, 62));
        label.setPreferredSize(new Dimension(172, 36));

        // Slider: 0 to 200 (represents 0.00 to 2.00)
        int initialValue = (int) (curveFilter.getWeight(index) * 100);
        JSlider slider = new JSlider(0, 200, initialValue);
        slider.setOpaque(false);
        slider.setForeground(new Color(54, 135, 39));
        slider.setPreferredSize(new Dimension(300, 36));
        sliders[index] = slider;

        // Value label
        JLabel valLabel = new JLabel(String.format("%.2f", curveFilter.getWeight(index)));
        valLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        valLabel.setForeground(new Color(54, 135, 39));
        valLabel.setPreferredSize(new Dimension(72, 36));
        valLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabels[index] = valLabel;

        final int idx = index;
        slider.addChangeListener(e -> {
            double value = slider.getValue() / 100.0;
            curveFilter.setWeight(idx, value);
            valLabel.setText(String.format("%.2f", value));
            graphPanel.repaint();
        });

        row.add(label, BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        row.add(valLabel, BorderLayout.EAST);

        return row;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);

        JButton btnReset = new JButton("Reset to Normal Distribution");
        btnReset.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnReset.setBackground(new Color(240, 240, 240));
        btnReset.setForeground(new Color(64, 63, 62));
        btnReset.setFocusPainted(false);
        btnReset.setBorderPainted(false);
        btnReset.setOpaque(true);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            curveFilter.resetToNormalDistribution();
            refreshSliders();
            graphPanel.repaint();
        });

        JButton btnFlat = new JButton("Flat (All Equal)");
        btnFlat.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnFlat.setBackground(new Color(240, 240, 240));
        btnFlat.setForeground(new Color(64, 63, 62));
        btnFlat.setFocusPainted(false);
        btnFlat.setBorderPainted(false);
        btnFlat.setOpaque(true);
        btnFlat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFlat.addActionListener(e -> {
            for (int i = 0; i < MoralCurveFilter.NUM_POINTS; i++) {
                curveFilter.setWeight(i, 1.0);
            }
            refreshSliders();
            graphPanel.repaint();
        });

        JButton btnApply = new JButton("Apply & Continue →");
        btnApply.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnApply.setBackground(new Color(54, 135, 39));
        btnApply.setForeground(Color.WHITE);
        btnApply.setFocusPainted(false);
        btnApply.setBorderPainted(false);
        btnApply.setOpaque(true);
        btnApply.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApply.addActionListener(e -> onConfirm.run());

        JButton btnCancel = new JButton("← Cancel");
        btnCancel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnCancel.setForeground(new Color(82, 81, 80));
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> onCancel.run());

        panel.add(btnCancel);
        panel.add(btnReset);
        panel.add(btnFlat);
        panel.add(btnApply);

        return panel;
    }

    private void refreshSliders() {
        for (int i = 0; i < MoralCurveFilter.NUM_POINTS; i++) {
            int val = (int) (curveFilter.getWeight(i) * 100);
            sliders[i].setValue(val);
            valueLabels[i].setText(String.format("%.2f", curveFilter.getWeight(i)));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner class: Graph panel to visualize the curve
    // ─────────────────────────────────────────────────────────────────────────

    private static class CurveGraphPanel extends JPanel {
        private final MoralCurveFilter filter;

        CurveGraphPanel(MoralCurveFilter filter) {
            this.filter = filter;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int pad = 40;
            int graphW = w - 2 * pad;
            int graphH = h - 2 * pad;

            // Background
            g2.setColor(new Color(44, 50, 59));
            g2.fillRoundRect(pad - 10, pad - 10, graphW + 20, graphH + 20, 10, 10);

            // Grid lines
            g2.setColor(new Color(60, 66, 75));
            for (int i = 0; i <= 4; i++) {
                int y = pad + (int) (graphH * (1.0 - i / 4.0));
                g2.drawLine(pad, y, pad + graphW, y);
            }

            // Draw curve
            double[] weights = filter.getWeights();
            double maxWeight = 2.0; // max slider value

            // Fill area under curve
            Path2D.Double area = new Path2D.Double();
            area.moveTo(pad, pad + graphH);
            for (int i = 0; i < MoralCurveFilter.NUM_POINTS; i++) {
                double x = pad + (double) i / (MoralCurveFilter.NUM_POINTS - 1) * graphW;
                double y = pad + graphH * (1.0 - weights[i] / maxWeight);
                area.lineTo(x, y);
            }
            area.lineTo(pad + graphW, pad + graphH);
            area.closePath();

            g2.setColor(new Color(0, 200, 180, 40));
            g2.fill(area);

            // Draw curve line
            g2.setColor(new Color(0, 200, 180));
            g2.setStroke(new BasicStroke(2.5f));
            for (int i = 0; i < MoralCurveFilter.NUM_POINTS - 1; i++) {
                double x1 = pad + (double) i / (MoralCurveFilter.NUM_POINTS - 1) * graphW;
                double y1 = pad + graphH * (1.0 - weights[i] / maxWeight);
                double x2 = pad + (double) (i + 1) / (MoralCurveFilter.NUM_POINTS - 1) * graphW;
                double y2 = pad + graphH * (1.0 - weights[i + 1] / maxWeight);
                g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            }

            // Draw control points
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i < MoralCurveFilter.NUM_POINTS; i++) {
                double x = pad + (double) i / (MoralCurveFilter.NUM_POINTS - 1) * graphW;
                double y = pad + graphH * (1.0 - weights[i] / maxWeight);
                g2.setColor(Color.WHITE);
                g2.fillOval((int) x - 5, (int) y - 5, 10, 10);
                g2.setColor(new Color(0, 200, 180));
                g2.drawOval((int) x - 5, (int) y - 5, 10, 10);
            }

            // Axis labels
            g2.setColor(new Color(140, 140, 140));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("Beginning", pad - 5, pad + graphH + 15);
            g2.drawString("Middle", pad + graphW / 2 - 15, pad + graphH + 15);
            g2.drawString("End", pad + graphW - 15, pad + graphH + 15);
            g2.drawString("2.0", pad - 30, pad + 5);
            g2.drawString("1.0", pad - 30, pad + graphH / 2 + 3);
            g2.drawString("0.0", pad - 30, pad + graphH + 5);

            // Title
            g2.setColor(new Color(200, 200, 200));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Moral Curve Distribution", pad, pad - 15);

            g2.dispose();
        }
    }
}
