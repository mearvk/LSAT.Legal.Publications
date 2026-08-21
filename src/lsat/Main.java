package lsat;

import lsat.ui.TestGUI;
import lsat.ui.SplashScreen;
import lsat.ui.TerminalRunner;

import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.nio.file.*;

/**
 * Main — Entry point for the LSAT Moral Assessment application.
 * Loads configuration from XML, then launches either GUI or Terminal mode
 * based on user selection.
 *
 * Config file: src/gui/lsat-config.xml
 */
public class Main {

    private static final Path CONFIG_XML_PATH = Path.of("src", "lsat", "config", "lsat-config.xml");

    public static void main(String[] args) {
        // Ensure config XML exists (create default if missing)
        ensureConfigExists();

        // Load XML config
        AppConfig config = loadXmlConfig();

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // CLI flags override config
        boolean forceTerminal = false;
        boolean forceGui = false;
        for (String arg : args) {
            if ("--terminal".equals(arg) || "-t".equals(arg)) forceTerminal = true;
            if ("--gui".equals(arg) || "-g".equals(arg)) forceGui = true;
        }

        // Determine mode: CLI flags take precedence, then config, default is GUI
        boolean useTerminal;
        if (forceTerminal) {
            useTerminal = true;
        } else if (forceGui) {
            useTerminal = false;
        } else {
            useTerminal = "terminal".equalsIgnoreCase(config.launchMode);
        }

        if (useTerminal) {
            System.out.println("Starting in Terminal mode.");
            applyConfigToTerminal(config);
            TerminalRunner.run();
        } else {
            launchGUI(config);
        }
    }

    private static void launchGUI(AppConfig config) {
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.showSplash(() -> {
                SwingUtilities.invokeLater(() -> {
                    TestGUI gui = new TestGUI();
                    gui.setVisible(true);
                });
            });
        });
    }

    private static void applyConfigToTerminal(AppConfig config) {
        System.out.println("Config loaded: language=" + config.language
                + ", difficulty=" + config.difficulty
                + ", font=" + config.fontFamily + "/" + config.fontSize);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // XML Config
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads configuration from the XML file.
     */
    private static AppConfig loadXmlConfig() {
        AppConfig config = new AppConfig();

        if (!Files.exists(CONFIG_XML_PATH)) {
            return config; // defaults
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(CONFIG_XML_PATH.toFile());
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement(); // <lsat-config>

            // Font settings
            NodeList fontNodes = root.getElementsByTagName("font");
            if (fontNodes.getLength() > 0) {
                Element font = (Element) fontNodes.item(0);
                config.fontFamily = getChildText(font, "family", config.fontFamily);
                config.fontSize = getChildInt(font, "size", config.fontSize);
                config.fontColor = getChildText(font, "color", config.fontColor);
            }

            // Test settings
            NodeList testNodes = root.getElementsByTagName("test");
            if (testNodes.getLength() > 0) {
                Element test = (Element) testNodes.item(0);
                config.difficulty = getChildText(test, "difficulty", config.difficulty);
                config.language = getChildText(test, "language", config.language);
                config.lieScaleEnabled = getChildBoolean(test, "lie-scale-enabled", config.lieScaleEnabled);
                config.adaptivePacing = getChildBoolean(test, "adaptive-pacing", config.adaptivePacing);
                config.streakBonusEnabled = getChildBoolean(test, "streak-bonus", config.streakBonusEnabled);
                config.breakEnabled = getChildBoolean(test, "break-enabled", config.breakEnabled);
            }

            // Timing settings
            NodeList timingNodes = root.getElementsByTagName("timing");
            if (timingNodes.getLength() > 0) {
                Element timing = (Element) timingNodes.item(0);
                config.baseTimeSec = getChildInt(timing, "base-seconds", config.baseTimeSec);
                config.maxTimeSec = getChildInt(timing, "max-seconds", config.maxTimeSec);
                config.breakDurationSec = getChildInt(timing, "break-duration-seconds", config.breakDurationSec);
            }

            // IQ settings
            NodeList iqNodes = root.getElementsByTagName("iq");
            if (iqNodes.getLength() > 0) {
                Element iq = (Element) iqNodes.item(0);
                config.iqFloor = getChildInt(iq, "floor", config.iqFloor);
                config.iqMean = getChildInt(iq, "mean", config.iqMean);
                config.iqPassingThreshold = getChildInt(iq, "passing-threshold", config.iqPassingThreshold);
                config.iqWeight = getChildDouble(iq, "weight-percent", config.iqWeight);
            }

            // Display settings
            NodeList displayNodes = root.getElementsByTagName("display");
            if (displayNodes.getLength() > 0) {
                Element display = (Element) displayNodes.item(0);
                config.launchMode = getChildText(display, "mode", config.launchMode);
                config.windowWidth = getChildInt(display, "window-width", config.windowWidth);
                config.windowHeight = getChildInt(display, "window-height", config.windowHeight);
                config.showLiveIQ = getChildBoolean(display, "show-live-iq", config.showLiveIQ);
                config.showLieScale = getChildBoolean(display, "show-lie-scale", config.showLieScale);
                config.showIntellectTier = getChildBoolean(display, "show-intellect-tier", config.showIntellectTier);
            }

            System.out.println("Config loaded from: " + CONFIG_XML_PATH);

        } catch (Exception e) {
            System.err.println("Warning: Could not parse XML config, using defaults: " + e.getMessage());
        }

        return config;
    }

    /**
     * Creates a default XML config file if one doesn't exist.
     */
    private static void ensureConfigExists() {
        if (Files.exists(CONFIG_XML_PATH)) return;

        String defaultXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!--\n"
                + "  LSAT Moral Assessment — Application Configuration\n"
                + "  Edit this file to customize the test experience.\n"
                + "-->\n"
                + "<lsat-config>\n"
                + "\n"
                + "    <!-- Font settings for question display -->\n"
                + "    <font>\n"
                + "        <family>SansSerif</family>\n"
                + "        <size>20</size>\n"
                + "        <!-- Hex (#RRGGBB) or named: white, black, green, gold, cyan, silver -->\n"
                + "        <color>#FFFFFF</color>\n"
                + "    </font>\n"
                + "\n"
                + "    <!-- Test behavior settings -->\n"
                + "    <test>\n"
                + "        <!-- easy, moderate, standard, hard, expert -->\n"
                + "        <difficulty>standard</difficulty>\n"
                + "        <!-- english, spanish, french, american_prudent, german, greek -->\n"
                + "        <language>english</language>\n"
                + "        <!-- Enable lie-scale trap questions (true/false) -->\n"
                + "        <lie-scale-enabled>true</lie-scale-enabled>\n"
                + "        <!-- Enable adaptive pacing / running start (true/false) -->\n"
                + "        <adaptive-pacing>true</adaptive-pacing>\n"
                + "        <!-- Enable streak bonus time (true/false) -->\n"
                + "        <streak-bonus>true</streak-bonus>\n"
                + "        <!-- Enable 2-minute break after 45 questions (true/false) -->\n"
                + "        <break-enabled>true</break-enabled>\n"
                + "    </test>\n"
                + "\n"
                + "    <!-- Timing configuration -->\n"
                + "    <timing>\n"
                + "        <!-- Base seconds per question (21-24 for standard) -->\n"
                + "        <base-seconds>21</base-seconds>\n"
                + "        <!-- Maximum seconds per question -->\n"
                + "        <max-seconds>24</max-seconds>\n"
                + "        <!-- Break duration in seconds -->\n"
                + "        <break-duration-seconds>120</break-duration-seconds>\n"
                + "    </timing>\n"
                + "\n"
                + "    <!-- IQ estimation parameters -->\n"
                + "    <iq>\n"
                + "        <!-- Minimum IQ floor assumption -->\n"
                + "        <floor>115</floor>\n"
                + "        <!-- Mean IQ assumption for test population -->\n"
                + "        <mean>140</mean>\n"
                + "        <!-- Passing IQ threshold -->\n"
                + "        <passing-threshold>125</passing-threshold>\n"
                + "        <!-- IQ weight in overall score (0.30-0.50) -->\n"
                + "        <weight-percent>0.40</weight-percent>\n"
                + "    </iq>\n"
                + "\n"
                + "    <!-- Display/UI settings -->\n"
                + "    <display>\n"
                + "        <!-- Launch mode: gui or terminal -->\n"
                + "        <mode>gui</mode>\n"
                + "        <window-width>950</window-width>\n"
                + "        <window-height>700</window-height>\n"
                + "        <!-- Show live IQ estimator during test -->\n"
                + "        <show-live-iq>true</show-live-iq>\n"
                + "        <!-- Show lie scale indicator during test -->\n"
                + "        <show-lie-scale>true</show-lie-scale>\n"
                + "        <!-- Show intellect tier (VA/S/PG) during test -->\n"
                + "        <show-intellect-tier>true</show-intellect-tier>\n"
                + "    </display>\n"
                + "\n"
                + "</lsat-config>\n";

        try {
            Files.createDirectories(CONFIG_XML_PATH.getParent());
            Files.writeString(CONFIG_XML_PATH, defaultXml,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Created default config: " + CONFIG_XML_PATH);
        } catch (IOException e) {
            System.err.println("Could not create default config: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // XML helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static String getChildText(Element parent, String tagName, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent().trim();
            if (!text.isEmpty()) return text;
        }
        return defaultValue;
    }

    private static int getChildInt(Element parent, String tagName, int defaultValue) {
        try {
            return Integer.parseInt(getChildText(parent, tagName, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double getChildDouble(Element parent, String tagName, double defaultValue) {
        try {
            return Double.parseDouble(getChildText(parent, tagName, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getChildBoolean(Element parent, String tagName, boolean defaultValue) {
        String text = getChildText(parent, tagName, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text) || "1".equals(text);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AppConfig data class
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Holds all parsed configuration values from the XML.
     * Used to pass settings to the GUI and Terminal runners.
     */
    public static class AppConfig {
        // Font
        public String fontFamily = "SansSerif";
        public int fontSize = 20;
        public String fontColor = "#FFFFFF";

        // Test
        public String difficulty = "standard";
        public String language = "english";
        public boolean lieScaleEnabled = true;
        public boolean adaptivePacing = true;
        public boolean streakBonusEnabled = true;
        public boolean breakEnabled = true;

        // Timing
        public int baseTimeSec = 21;
        public int maxTimeSec = 24;
        public int breakDurationSec = 120;

        // IQ
        public int iqFloor = 115;
        public int iqMean = 140;
        public int iqPassingThreshold = 125;
        public double iqWeight = 0.40;

        // Display
        public String launchMode = "gui"; // "gui" or "terminal"
        public int windowWidth = 950;
        public int windowHeight = 700;
        public boolean showLiveIQ = true;
        public boolean showLieScale = true;
        public boolean showIntellectTier = true;

        @Override
        public String toString() {
            return String.format("AppConfig{lang=%s, diff=%s, font=%s/%d, iq=%d/%d/%d}",
                    language, difficulty, fontFamily, fontSize, iqFloor, iqMean, iqPassingThreshold);
        }
    }
}
