package lsat.config;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * TestConfig — External configuration file handler for the LSAT GUI.
 * Reads and writes settings from src/gui/config.properties.
 *
 * Configurable settings:
 *   - Font color (foreground text)
 *   - Font type (family name)
 *   - Font size
 *   - Test difficulty (easy, moderate, standard, hard, expert)
 *   - Language (english, spanish, french, american_prudent, german, greek)
 */
public class TestConfig {

    // ─────────────────────────────────────────────────────────────────────────
    // Defaults
    // ─────────────────────────────────────────────────────────────────────────

    private static final String DEFAULT_FONT_FAMILY = "SansSerif";
    private static final int DEFAULT_FONT_SIZE = 20;
    private static final String DEFAULT_FONT_COLOR = "#FFFFFF";
    private static final String DEFAULT_DIFFICULTY = "standard";
    private static final String DEFAULT_LANGUAGE = "english";

    private static final Path CONFIG_PATH = Path.of("src", "lsat", "config", "config.properties");

    // ─────────────────────────────────────────────────────────────────────────
    // Settings
    // ─────────────────────────────────────────────────────────────────────────

    private String fontFamily;
    private int fontSize;
    private Color fontColor;
    private String difficulty;  // easy, moderate, standard, hard, expert
    private String language;    // english, spanish, french, american_prudent, german, greek
    private String titlebarDoubleclick; // go_back, pause
    private String symbolDefinition;   // text shown when 𓈌 is clicked
    private String curveWeights;       // descriptor:integer pairs for curve & pacing
    private int frameCornerRadius;     // percentage (0–50) for bottom corner rounding
    private int[] buttonGloss;         // [topAlpha, glossPercent, shadowDepth, shadowAlpha, outlineAlpha, arcRadius]

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public TestConfig() {
        loadDefaults();
        load();
    }

    private static final String DEFAULT_CURVE_WEIGHTS =
        "Beginning:20,Early:45,Rising:72,Building:90,Approaching:98,Middle:100,Sustaining:98,Declining:90,Waning:72,End:45";

    private void loadDefaults() {
        fontFamily = DEFAULT_FONT_FAMILY;
        fontSize = DEFAULT_FONT_SIZE;
        fontColor = Color.WHITE;
        difficulty = DEFAULT_DIFFICULTY;
        language = DEFAULT_LANGUAGE;
        titlebarDoubleclick = "go_back";
        symbolDefinition = "The State has an interest in Human Domomy of at $300,000,000 per Second per Ant of Human Anatomy.";
        curveWeights = DEFAULT_CURVE_WEIGHTS;
        frameCornerRadius = 28;
        buttonGloss = new int[]{120, 25, 8, 50, 40, 12};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Load / Save
    // ─────────────────────────────────────────────────────────────────────────

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save(); // create default config
            return;
        }

        try {
            Properties props = new Properties();
            props.load(Files.newBufferedReader(CONFIG_PATH));

            fontFamily = props.getProperty("font.family", DEFAULT_FONT_FAMILY);
            fontSize = Integer.parseInt(props.getProperty("font.size", String.valueOf(DEFAULT_FONT_SIZE)));
            fontColor = parseColor(props.getProperty("font.color", DEFAULT_FONT_COLOR));
            difficulty = props.getProperty("test.difficulty", DEFAULT_DIFFICULTY).toLowerCase();
            language = props.getProperty("test.language", DEFAULT_LANGUAGE).toLowerCase();
            titlebarDoubleclick = props.getProperty("titlebar.doubleclick", "go_back").toLowerCase();
            symbolDefinition = props.getProperty("symbol.definition",
                "The State has an interest in Human Domomy of at $300,000,000 per Second per Ant of Human Anatomy.");
            curveWeights = props.getProperty("curve.weights", DEFAULT_CURVE_WEIGHTS);
            frameCornerRadius = Math.max(0, Math.min(50,
                Integer.parseInt(props.getProperty("frame.corner.radius", "28"))));
            buttonGloss = parseButtonGloss(props.getProperty("button.gloss", "120,25,8,50,40,12"));

            // Validate
            if (!isValidDifficulty(difficulty)) difficulty = DEFAULT_DIFFICULTY;
            if (!isValidLanguage(language)) language = DEFAULT_LANGUAGE;
            if (!titlebarDoubleclick.equals("go_back") && !titlebarDoubleclick.equals("pause")) {
                titlebarDoubleclick = "go_back";
            }

        } catch (Exception e) {
            System.err.println("Error loading config, using defaults: " + e.getMessage());
            loadDefaults();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            StringBuilder sb = new StringBuilder();
            sb.append("# LSAT Moral Assessment — Configuration\n");
            sb.append("# ──────────────────────────────────────────────\n");
            sb.append("#\n");
            sb.append("# Font Settings\n");
            sb.append("#   font.family: SansSerif, Serif, Monospaced, Dialog, or any installed font\n");
            sb.append("#   font.size: integer (12–36 recommended)\n");
            sb.append("#   font.color: hex color (#RRGGBB) or named: white, black, green, gold, cyan\n");
            sb.append("#\n");
            sb.append("# Test Difficulty\n");
            sb.append("#   test.difficulty: easy, moderate, standard, hard, expert\n");
            sb.append("#     easy     — more time, relaxed scoring\n");
            sb.append("#     moderate — slightly more time than standard\n");
            sb.append("#     standard — default (21–24s per question)\n");
            sb.append("#     hard     — less time, stricter curve\n");
            sb.append("#     expert   — minimal time, strict curve, no bonus time\n");
            sb.append("#\n");
            sb.append("# Language\n");
            sb.append("#   test.language: english, spanish, french, american_prudent, german, greek\n");
            sb.append("#\n");
            sb.append("# ──────────────────────────────────────────────\n\n");
            sb.append(String.format("font.family=%s%n", fontFamily));
            sb.append(String.format("font.size=%d%n", fontSize));
            sb.append(String.format("font.color=%s%n", colorToHex(fontColor)));
            sb.append(String.format("test.difficulty=%s%n", difficulty));
            sb.append(String.format("test.language=%s%n", language));

            Files.writeString(CONFIG_PATH, sb.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────

    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String family) { this.fontFamily = family; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int size) { this.fontSize = Math.max(10, Math.min(48, size)); }

    public Color getFontColor() { return fontColor; }
    public void setFontColor(Color color) { this.fontColor = color; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String diff) {
        if (isValidDifficulty(diff)) this.difficulty = diff.toLowerCase();
    }

    public String getLanguage() { return language; }
    public void setLanguage(String lang) {
        if (isValidLanguage(lang)) this.language = lang.toLowerCase();
    }

    public String getTitlebarDoubleclick() { return titlebarDoubleclick; }
    public void setTitlebarDoubleclick(String action) {
        if ("go_back".equals(action) || "pause".equals(action)) this.titlebarDoubleclick = action;
    }

    public String getSymbolDefinition() { return symbolDefinition; }
    public void setSymbolDefinition(String def) {
        if (def != null && !def.isEmpty()) this.symbolDefinition = def;
    }

    public String getCurveWeights() { return curveWeights; }
    public void setCurveWeights(String weights) {
        if (weights != null && !weights.isEmpty()) this.curveWeights = weights;
    }

    public int getFrameCornerRadius() { return frameCornerRadius; }
    public void setFrameCornerRadius(int radius) {
        this.frameCornerRadius = Math.max(0, Math.min(50, radius));
    }

    public int[] getButtonGloss() { return buttonGloss; }
    public void setButtonGloss(int[] gloss) {
        if (gloss != null && gloss.length == 6) this.buttonGloss = gloss;
    }

    /** topAlpha */
    public int getGlossTopAlpha() { return buttonGloss[0]; }
    /** glossPercent (height of gloss as % of button) */
    public int getGlossPercent() { return buttonGloss[1]; }
    /** shadowDepth in pixels */
    public int getGlossShadowDepth() { return buttonGloss[2]; }
    /** shadowAlpha */
    public int getGlossShadowAlpha() { return buttonGloss[3]; }
    /** outlineAlpha */
    public int getGlossOutlineAlpha() { return buttonGloss[4]; }
    /** arcRadius in pixels */
    public int getGlossArcRadius() { return buttonGloss[5]; }

    private static int[] parseButtonGloss(String s) {
        int[] defaults = {120, 25, 8, 50, 40, 12};
        if (s == null || s.isEmpty()) return defaults;
        String[] parts = s.split(",");
        if (parts.length != 6) return defaults;
        try {
            int[] result = new int[6];
            result[0] = Math.max(0, Math.min(255, Integer.parseInt(parts[0].trim()))); // topAlpha
            result[1] = Math.max(0, Math.min(100, Integer.parseInt(parts[1].trim()))); // glossPercent
            result[2] = Math.max(0, Math.min(20, Integer.parseInt(parts[2].trim())));  // shadowDepth
            result[3] = Math.max(0, Math.min(255, Integer.parseInt(parts[3].trim()))); // shadowAlpha
            result[4] = Math.max(0, Math.min(255, Integer.parseInt(parts[4].trim()))); // outlineAlpha
            result[5] = Math.max(0, Math.min(30, Integer.parseInt(parts[5].trim())));  // arcRadius
            return result;
        } catch (NumberFormatException e) {
            return defaults;
        }
    }

    /**
     * Parse curve.weights into an array of 10 double values (0.0–2.0).
     * Format: "Descriptor:integer,Descriptor:integer,..."
     * Integer is 0–200, divided by 100 to get the weight.
     * Returns null if parsing fails.
     */
    public double[] parseCurveWeights() {
        if (curveWeights == null || curveWeights.isEmpty()) return null;
        String[] entries = curveWeights.split(",");
        if (entries.length != 10) return null;

        double[] weights = new double[10];
        for (int i = 0; i < 10; i++) {
            String[] parts = entries[i].trim().split(":");
            if (parts.length != 2) return null;
            try {
                int val = Integer.parseInt(parts[1].trim());
                weights[i] = Math.max(0.0, Math.min(2.0, val / 100.0));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return weights;
    }

    /**
     * Parse curve.weights into an array of descriptor labels.
     */
    public String[] parseCurveLabels() {
        if (curveWeights == null || curveWeights.isEmpty()) return null;
        String[] entries = curveWeights.split(",");
        if (entries.length != 10) return null;

        String[] labels = new String[10];
        for (int i = 0; i < 10; i++) {
            String[] parts = entries[i].trim().split(":");
            if (parts.length != 2) return null;
            labels[i] = parts[0].trim();
        }
        return labels;
    }

    /** Get a Font object from current config */
    public Font getQuestionFont() {
        return new Font(fontFamily, Font.PLAIN, fontSize);
    }

    /** Get a bold variant */
    public Font getTitleFont() {
        return new Font(fontFamily, Font.BOLD, fontSize + 4);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Difficulty → engine parameters
    // ─────────────────────────────────────────────────────────────────────────

    /** Get base time adjustment for difficulty */
    public int getBaseTimeForDifficulty() {
        switch (difficulty) {
            case "easy": return 28;
            case "moderate": return 25;
            case "standard": return 21;
            case "hard": return 17;
            case "expert": return 14;
            default: return 21;
        }
    }

    /** Get max base time for difficulty */
    public int getMaxBaseTimeForDifficulty() {
        switch (difficulty) {
            case "easy": return 32;
            case "moderate": return 28;
            case "standard": return 24;
            case "hard": return 20;
            case "expert": return 16;
            default: return 24;
        }
    }

    /** Whether streak bonus is allowed at this difficulty */
    public boolean isStreakBonusAllowed() {
        return !difficulty.equals("expert");
    }

    /** Whether break is allowed at this difficulty */
    public boolean isBreakAllowed() {
        return !difficulty.equals("expert");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isValidDifficulty(String d) {
        if (d == null) return false;
        switch (d.toLowerCase()) {
            case "easy": case "moderate": case "standard": case "hard": case "expert":
                return true;
            default: return false;
        }
    }

    public static boolean isValidLanguage(String l) {
        if (l == null) return false;
        switch (l.toLowerCase()) {
            case "english": case "spanish": case "french":
            case "american_prudent": case "german": case "greek":
                return true;
            default: return false;
        }
    }

    public static String[] getAvailableLanguages() {
        return new String[]{"english", "spanish", "french", "american_prudent", "german", "greek"};
    }

    public static String[] getAvailableDifficulties() {
        return new String[]{"easy", "moderate", "standard", "hard", "expert"};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Color utilities
    // ─────────────────────────────────────────────────────────────────────────

    private static Color parseColor(String s) {
        if (s == null || s.isEmpty()) return Color.WHITE;
        s = s.trim().toLowerCase();

        // Named colors
        switch (s) {
            case "white": return Color.WHITE;
            case "black": return Color.BLACK;
            case "green": return new Color(100, 220, 100);
            case "gold": return new Color(255, 215, 0);
            case "cyan": return new Color(0, 200, 200);
            case "silver": return new Color(192, 192, 192);
            case "red": return new Color(255, 80, 80);
            case "blue": return new Color(100, 150, 255);
        }

        // Hex color
        try {
            if (s.startsWith("#")) s = s.substring(1);
            int rgb = Integer.parseInt(s, 16);
            return new Color(rgb);
        } catch (NumberFormatException e) {
            return Color.WHITE;
        }
    }

    private static String colorToHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
