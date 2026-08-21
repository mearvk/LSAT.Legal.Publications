package lsat.ui;

import lsat.engine.*;
import lsat.scoring.*;
import lsat.config.TestConfig;
import lsat.i18n.LanguagePack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LSAT Test-Taker GUI — Swing-based interface for moral/ethical assessment quizzes.
 * Features adaptive pacing (running start: easy→hard), morality bit tracking,
 * morale boost with streak bonuses, midpoint prediction with dynamic color theming,
 * and configurable time allotments per question.
 */
public class TestGUI extends JFrame {

    // Card layout panel names
    private static final String CARD_WELCOME = "welcome";
    private static final String CARD_ADMIN = "admin";
    private static final String CARD_QUIZ = "quiz";
    private static final String CARD_RESULTS = "results";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    // Moral curve filter (admin-adjustable)
    private final MoralCurveFilter curveFilter;

    // Configuration and language
    private final TestConfig config;
    private LanguagePack langPack;

    // Adaptive test engine
    private AdaptiveTestEngine engine;

    // Live IQ estimator (updates every question)
    private LiveIQEstimator liveIQ;

    // Lie scale detector (social desirability / acquiescence bias)
    private LieScaleDetector lieDetector;

    // All questions flattened + ordered by difficulty for running start
    private String[] orderedQuestions;
    private int currentQuestionIndex;
    private int score;
    private boolean[] answers;

    // Quiz panel components
    private JPanel quizPanel;
    private JLabel lblQuizTitle;
    private JLabel lblProgress;
    private JLabel lblDifficulty;
    private JLabel lblTimer;
    private JLabel lblMorale;
    private JLabel lblStreak;
    private JLabel lblIntellect;
    private JLabel lblLiveIQ;
    private JLabel lblWeighted;
    private JTextArea txtQuestion;
    private JButton btnYes;
    private JButton btnNo;
    private JButton btnBreak;
    private JLabel lblBonusTime;
    private JPanel quizTopBar;
    private JPanel quizBtnPanel;

    // Timer
    private Timer questionTimer;
    private int timeRemaining;

    // Intellect classifier (computed live and at end)
    private IntellectClassifier classifier;

    // Results panel components
    private JLabel lblResultTitle;
    private JLabel lblResultScore;
    private JLabel lblResultPercent;
    private JLabel lblResultCurved;
    private JLabel lblResultMorale;
    private JLabel lblResultTier;
    private JLabel lblResultIntellect;
    private JTextArea txtResultReasoning;

    // ─── Fidelity-inspired color palette (white, grey, green) ───
    // Backgrounds
    private static final Color COLOR_WHITE = new Color(255, 255, 255);
    private static final Color COLOR_OFF_WHITE = new Color(251, 250, 243);   // #FBFAF3
    private static final Color COLOR_LIGHT_GREY = new Color(240, 240, 240);  // #F0F0F0
    private static final Color COLOR_MID_GREY = new Color(82, 81, 80);       // #525150
    private static final Color COLOR_DARK_GREY = new Color(64, 63, 62);      // #403F3E
    private static final Color COLOR_TEXT_DARK = new Color(20, 20, 20);      // #141414

    // Greens
    private static final Color COLOR_PRIMARY_GREEN = new Color(54, 135, 39);   // #368727
    private static final Color COLOR_DARK_GREEN = new Color(30, 111, 29);      // #1E6F1D
    private static final Color COLOR_FOREST_GREEN = new Color(15, 83, 25);     // #0F5319
    private static final Color COLOR_SUCCESS_GREEN = new Color(72, 172, 54);   // #48AC36

    // Accent greens / teals
    private static final Color COLOR_TEAL = new Color(0, 150, 129);            // #009681
    private static final Color COLOR_DARK_TEAL = new Color(5, 128, 112);       // #058070

    // Utility
    private static final Color COLOR_ERROR_RED = new Color(250, 57, 57);       // #FA3939
    private static final Color COLOR_WARNING_ORANGE = new Color(255, 130, 16); // #FF8210
    private static final Color COLOR_GOLD = new Color(255, 202, 31);           // #FFCA1F

    // Color themes for performance tiers (light theme)
    private static final Color COLOR_NEUTRAL_BG = COLOR_WHITE;
    private static final Color COLOR_GREEN_BG = new Color(235, 248, 234);      // very light green
    private static final Color COLOR_SILVER_BG = new Color(242, 244, 247);     // light blue-grey
    private static final Color COLOR_GOLD_BG = new Color(255, 250, 230);       // warm cream

    private static final Color COLOR_NEUTRAL_ACCENT = COLOR_PRIMARY_GREEN;
    private static final Color COLOR_GREEN_ACCENT = COLOR_SUCCESS_GREEN;
    private static final Color COLOR_SILVER_ACCENT = COLOR_MID_GREY;
    private static final Color COLOR_GOLD_ACCENT = COLOR_GOLD;

    // ─── Branding font (Barlow Condensed Bold Italic) ────────────────────────
    private static final Font BRANDING_FONT = loadBrandingFont();

    private static Font loadBrandingFont() {
        // Attempt to load Barlow Condensed Bold Italic from fonts/ directory
        String[] candidates = {
            "fonts/BarlowCondensed-BoldItalic.ttf",
            "fonts/BarlowCondensed-BoldItalic.otf",
            "fonts/BarlowCondensed-Italic.ttf",
            "fonts/BarlowCondensed-Italic.otf",
            "fonts/BarlowCondensed-Bold.ttf",
            "fonts/BarlowCondensed-Bold.otf",
            "fonts/BarlowCondensed-Regular.ttf",
            "fonts/BarlowCondensed-Regular.otf"
        };
        for (String path : candidates) {
            File fontFile = new File(path);
            if (fontFile.exists()) {
                try {
                    Font base = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                    return base.deriveFont(Font.BOLD | Font.ITALIC, 22f);
                } catch (FontFormatException | IOException ignored) { }
            }
        }
        // Also try loading from classpath resources
        for (String path : candidates) {
            try (InputStream is = TestGUI.class.getResourceAsStream("/" + path)) {
                if (is != null) {
                    Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                    return base.deriveFont(Font.BOLD | Font.ITALIC, 22f);
                }
            } catch (FontFormatException | IOException | NullPointerException ignored) { }
        }
        // Fallback: system sans-serif bold italic
        return new Font("SansSerif", Font.BOLD | Font.ITALIC, 22);
    }

    // ─── Heading font (Francois One Italic) ─────────────────────────────────
    private static final Font HEADING_FONT_BASE = loadHeadingFont();

    private static Font loadHeadingFont() {
        String[] candidates = {
            "fonts/FrancoisOne-Regular.ttf",
            "fonts/FrancoisOne-Regular.otf",
            "fonts/FrancoisOne.ttf",
            "fonts/FrancoisOne.otf"
        };
        for (String path : candidates) {
            File fontFile = new File(path);
            if (fontFile.exists()) {
                try {
                    Font base = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                    return base.deriveFont(Font.ITALIC, 24f);
                } catch (FontFormatException | IOException ignored) { }
            }
        }
        for (String path : candidates) {
            try (InputStream is = TestGUI.class.getResourceAsStream("/" + path)) {
                if (is != null) {
                    Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                    return base.deriveFont(Font.ITALIC, 24f);
                }
            } catch (FontFormatException | IOException | NullPointerException ignored) { }
        }
        return new Font("SansSerif", Font.BOLD | Font.ITALIC, 24);
    }

    private static Font headingFont(float size) {
        return HEADING_FONT_BASE.deriveFont(Font.ITALIC, size);
    }

    // Difficulty: 1=easy (basic ethics), 2=moderate, 3=medium, 4=hard, 5=expert
    // We order them easy→hard for the running start method.

    private static final String[] EASY_QUESTIONS = {
        "Do you correct a cashier who gives you too much change back?",
        "Do you hold the elevator or door for someone walking far behind you?",
        "Do you clean up after yourself in public spaces and parks?",
        "Do you arrive on time for appointments out of respect for others?",
        "Do you keep your music down in residential areas late at night?",
        "Do you give people physical space in public lines and crowds?",
        "Do you let another driver merge into your lane during heavy traffic?",
        "Do you pause your day to help someone pick up dropped items?",
        "Do you offer your seat on public transit to someone who needs it?",
        "Do you pick up litter on public sidewalks that is not yours?",
        "Do you knock and wait for permission before entering private rooms?",
        "Do you ask before borrowing items from family or roommates?",
        "Do you return lost items or money to the rightful owner immediately?",
        "Do you show up to events you promised to attend, rain or shine?",
        "Do you express gratitude frequently to those who provide you service?"
    };

    private static final String[] MODERATE_QUESTIONS = {
        "Do you refuse to lie even if a small white lie makes a conversation smoother?",
        "Do you admit your mistakes immediately to your boss or peers?",
        "Do you tell a friend the truth when they ask for honest, difficult feedback?",
        "Do you refuse to spread rumors or unverified gossip?",
        "Do you admit when you do not know the answer to a question?",
        "Do you avoid exaggerating your accomplishments on your resume?",
        "Do you listen to others without interrupting or judging their experiences?",
        "Do you show patience to customer service workers who make mistakes?",
        "Do you forgive people who genuinely apologize for hurting you?",
        "Do you refrain from mocking people for their flaws or insecurities?",
        "Do you treat service workers with the same respect as executives?",
        "Do you manage your personal debts and pay them back promptly?",
        "Do you accept constructive feedback without becoming defensive?",
        "Do you follow through on group projects so you do not let partners down?",
        "Do you value other people's time as much as you value your own?"
    };

    private static final String[] MEDIUM_QUESTIONS = {
        "Do you report your exact income on your taxes without cutting corners?",
        "Do you refuse to take credit for work done by a colleague?",
        "Do you keep promises even when a better opportunity arises later?",
        "Do you speak up when someone misrepresents facts in a meeting?",
        "Do you honor verbal agreements even if no contract is signed?",
        "Do you reveal product flaws if you are selling a used item?",
        "Do you defend an absent person when false statements are made about them?",
        "Do you actively try to understand the perspective of people you dislike?",
        "Do you judge people solely on their character rather than their background?",
        "Do you call out systemic bias or discrimination when you witness it?",
        "Do you vote for policies that benefit society even if they raise your taxes?",
        "Do you refuse to use nepotism to get ahead in your career?",
        "Do you support criminal justice reforms aimed at rehabilitating people?",
        "Do you uphold professional ethics even if it risks your employment?",
        "Do you protect the privacy and confidential secrets of your friends?",
        "Do you keep secrets that your friends trusted you to hold?",
        "Do you refuse bribes intended to make you betray your team?",
        "Do you remain loyal to your core values when tempted by fast money?"
    };

    private static final String[] HARD_QUESTIONS = {
        "Do you state your true intentions early in a new relationship?",
        "Do you refuse to ghost people to avoid an awkward conversation?",
        "Do you sacrifice your personal free time to help a friend move?",
        "Do you regularly donate money or resources to causes helping the poor?",
        "Do you give food or money directly to homeless individuals?",
        "Do you advocate for equal pay for equal work in your workplace?",
        "Do you call out double standards when applied to different genders?",
        "Do you reject privileges gained at the direct expense of others?",
        "Do you challenge unfair policies implemented by your own government?",
        "Do you step down from leadership if you are no longer fit to serve?",
        "Do you support a person's right to bodily autonomy and medical choice?",
        "Do you support your long-term friends when they experience poverty?",
        "Do you support your partner through long periods of chronic illness?",
        "Do you stand by a friend even when they are socially unpopular?",
        "Do you honor non-disclosure agreements even after leaving a company?"
    };

    private static final String[] EXPERT_QUESTIONS = {
        "Do you give anonymous donations where you receive absolutely no credit?",
        "Do you pull over to help a stranded motorist on a dark highway?",
        "Do you step in to break up a fight or defend a victim of bullying?",
        "Do you give up your weekend to assist with disaster relief efforts?",
        "Do you pass up a promotion if a colleague needs it far more urgently?",
        "Do you dive into danger to pull someone out of harm's way?",
        "Do you host or shelter someone who has nowhere else to go safely?",
        "Do you put the safety of others ahead of your own during a crisis?",
        "Do you fulfill promises made to people who have passed away?",
        "Do you protect vulnerable whistleblowers who trust you with info?",
        "Do you return to help communities that raised or supported you?",
        "Do you keep your word to your enemies or rivals without fail?",
        "Do you recuse yourself from decisions where you have a conflict of interest?"
    };

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public TestGUI() {
        super("LSAT Moral Assessment — Adaptive Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(750, 550));

        curveFilter = new MoralCurveFilter();

        // Load external config and language pack
        config = new TestConfig();
        langPack = LanguagePack.load(config.getLanguage());

        // ─── Menu Bar: Mayors & Sheriffs ─────────────────────────────────────
        setJMenuBar(createMenuBar());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createWelcomePanel(), CARD_WELCOME);
        cardPanel.add(new AdminCurvePanel(curveFilter,
                () -> cardLayout.show(cardPanel, CARD_WELCOME),
                () -> cardLayout.show(cardPanel, CARD_WELCOME)
        ), CARD_ADMIN);
        cardPanel.add(createQuizPanel(), CARD_QUIZ);
        cardPanel.add(createResultsPanel(), CARD_RESULTS);

        // ─── Persistent branding header: upper-left "LSAT Legal Publications" ───
        JPanel brandingHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        brandingHeader.setBackground(COLOR_WHITE);
        JLabel brandingLabel = new JLabel("LSAT Legal Publications");
        brandingLabel.setFont(BRANDING_FONT);
        brandingLabel.setForeground(COLOR_PRIMARY_GREEN);
        brandingHeader.add(brandingLabel);

        setLayout(new BorderLayout());
        add(brandingHeader, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, CARD_WELCOME);

        // Global key bindings for Y/N during quiz
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            if (!isQuizActive()) return false;

            if (e.getKeyCode() == KeyEvent.VK_Y) {
                answerQuestion(true);
                return true;
            } else if (e.getKeyCode() == KeyEvent.VK_N) {
                answerQuestion(false);
                return true;
            }
            return false;
        });
    }

    private boolean isQuizActive() {
        for (Component comp : cardPanel.getComponents()) {
            if (comp.isVisible() && comp.getName() != null && comp.getName().equals(CARD_QUIZ)) {
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu Bar — Mayors & Sheriffs
    // ─────────────────────────────────────────────────────────────────────────

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu oneMenu = new JMenu("One");
        JMenuItem voteItem = new JMenuItem("Vote");
        oneMenu.add(voteItem);

        menuBar.add(oneMenu);

        return menuBar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Welcome Panel
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new EmptyBorder(50, 60, 50, 60));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        // ─── Title row: logo orb (left) + "LSAT Moral Assessment" (right) ───
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoLabel = new JLabel();
        logoLabel.setOpaque(false);
        try {
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(new File("images/logo-orb.png"));
            if (original != null) {
                // Scale preserving aspect ratio, height matches title line (~64px)
                int targetHeight = 64;
                int targetWidth = (int) ((double) original.getWidth() / original.getHeight() * targetHeight);
                Image scaled = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (IOException ignored) { }

        JLabel title = new JLabel("LSAT Moral Assessment");
        title.setFont(headingFont(36f));
        title.setForeground(COLOR_FOREST_GREEN);

        titleRow.add(logoLabel);
        titleRow.add(title);

        JLabel subtitle = new JLabel("Adaptive Ethical Character Evaluation");
        subtitle.setFont(headingFont(18f));
        subtitle.setForeground(COLOR_MID_GREY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><center>Questions progress from easy to hard with adaptive pacing.<br>"
                + "Your morality and streak are tracked throughout.</center></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(COLOR_DARK_GREY);
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel owner = new JLabel("© 2026 Max Rupplin — All Rights Reserved");
        owner.setFont(new Font("SansSerif", Font.ITALIC, 12));
        owner.setForeground(COLOR_MID_GREY);
        owner.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleRow);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitle);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        titlePanel.add(desc);
        titlePanel.add(Box.createVerticalGlue());

        // Buttons — primary green CTA, secondary grey, same size, Francois One italic
        JButton btnStart = new JButton("Begin Adaptive Test");
        styleButton(btnStart, COLOR_PRIMARY_GREEN, 18, true);
        btnStart.setFont(headingFont(18f));
        btnStart.setPreferredSize(new Dimension(300, 50));
        btnStart.setMaximumSize(new Dimension(300, 50));
        btnStart.addActionListener(e -> startAdaptiveTest());

        JButton btnAdmin = new JButton("Admin: Adjust Curve & Pacing");
        styleButton(btnAdmin, COLOR_MID_GREY, 14, false);
        btnAdmin.setFont(headingFont(14f));
        btnAdmin.setPreferredSize(new Dimension(300, 50));
        btnAdmin.setMaximumSize(new Dimension(300, 50));
        btnAdmin.addActionListener(e -> cardLayout.show(cardPanel, CARD_ADMIN));

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(btnStart);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        btnPanel.add(btnAdmin);
        btnPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        owner.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(owner);

        panel.add(titlePanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Quiz Panel
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createQuizPanel() {
        quizPanel = new JPanel(new BorderLayout());
        quizPanel.setName(CARD_QUIZ);
        quizPanel.setBackground(COLOR_WHITE);
        quizPanel.setBorder(new EmptyBorder(5, 40, 20, 10));

        // Top bar: title + progress + difficulty + timer
        quizTopBar = new JPanel(new BorderLayout());
        quizTopBar.setOpaque(false);
        quizTopBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
        topLeft.setOpaque(false);

        lblQuizTitle = new JLabel("Adaptive Moral Assessment");
        lblQuizTitle.setFont(headingFont(18f));
        lblQuizTitle.setForeground(COLOR_PRIMARY_GREEN);

        lblDifficulty = new JLabel("Difficulty: Easy");
        lblDifficulty.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblDifficulty.setForeground(COLOR_MID_GREY);

        topLeft.add(lblQuizTitle);
        topLeft.add(lblDifficulty);

        JPanel topRight = new JPanel();
        topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
        topRight.setOpaque(false);

        lblTimer = new JLabel("⏱ 15s");
        lblTimer.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTimer.setForeground(COLOR_DARK_GREEN);
        lblTimer.setAlignmentX(Component.RIGHT_ALIGNMENT);

        btnBreak = new JButton("☕ Break (2 min)");
        btnBreak.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnBreak.setPreferredSize(new Dimension(130, 30));
        btnBreak.setMaximumSize(new Dimension(130, 30));
        btnBreak.setBackground(COLOR_LIGHT_GREY);
        btnBreak.setForeground(COLOR_MID_GREY);
        btnBreak.setFocusPainted(false);
        btnBreak.setBorderPainted(false);
        btnBreak.setOpaque(true);
        btnBreak.setEnabled(false);
        btnBreak.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBreak.setToolTipText("Available after 45 questions. One 2-minute break allowed.");
        btnBreak.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnBreak.addActionListener(e -> takeBreak());

        lblProgress = new JLabel("Question 1 of 76");
        lblProgress.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblProgress.setForeground(COLOR_MID_GREY);
        lblProgress.setAlignmentX(Component.RIGHT_ALIGNMENT);

        topRight.add(lblTimer);
        topRight.add(Box.createRigidArea(new Dimension(0, 4)));
        topRight.add(btnBreak);
        topRight.add(Box.createRigidArea(new Dimension(0, 4)));
        topRight.add(lblProgress);

        quizTopBar.add(topLeft, BorderLayout.WEST);
        quizTopBar.add(topRight, BorderLayout.EAST);

        // Morale/streak bar
        JPanel moraleBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        moraleBar.setOpaque(false);

        lblMorale = new JLabel("Morale: 0.0");
        lblMorale.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblMorale.setForeground(COLOR_PRIMARY_GREEN);

        lblStreak = new JLabel("Streak: 0");
        lblStreak.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStreak.setForeground(COLOR_WARNING_ORANGE);

        moraleBar.add(lblMorale);
        moraleBar.add(lblStreak);

        lblIntellect = new JLabel("Intellect: —");
        lblIntellect.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIntellect.setForeground(COLOR_DARK_TEAL);
        moraleBar.add(lblIntellect);

        // IQ estimation bar
        JPanel iqBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 3));
        iqBar.setOpaque(false);

        lblLiveIQ = new JLabel("IQ ~140 (120–160)");
        lblLiveIQ.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblLiveIQ.setForeground(COLOR_DARK_GREEN);

        lblWeighted = new JLabel("Overall: — [IQ×40% + Test×60%]");
        lblWeighted.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblWeighted.setForeground(COLOR_MID_GREY);

        iqBar.add(lblLiveIQ);
        iqBar.add(lblWeighted);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.add(moraleBar);
        statsPanel.add(iqBar);

        topSection.add(quizTopBar, BorderLayout.NORTH);
        topSection.add(statsPanel, BorderLayout.SOUTH);

        // Question area — white card, text color based on language
        txtQuestion = new JTextArea();
        txtQuestion.setFont(new Font("SansSerif", Font.PLAIN, 20));
        txtQuestion.setForeground(COLOR_TEXT_DARK);
        txtQuestion.setBackground(COLOR_WHITE);
        txtQuestion.setLineWrap(true);
        txtQuestion.setWrapStyleWord(true);
        txtQuestion.setEditable(false);
        txtQuestion.setFocusable(false);
        txtQuestion.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Answer buttons + break button
        quizBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        quizBtnPanel.setOpaque(false);

        btnYes = createAnswerButton("YES (Y)", COLOR_PRIMARY_GREEN);
        btnNo = createAnswerButton("NO (N)", COLOR_WHITE);

        lblBonusTime = new JLabel("");
        lblBonusTime.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblBonusTime.setForeground(COLOR_TEAL);

        btnYes.addActionListener(e -> answerQuestion(true));
        btnNo.addActionListener(e -> answerQuestion(false));

        quizBtnPanel.add(btnYes);
        quizBtnPanel.add(btnNo);
        quizBtnPanel.add(lblBonusTime);

        quizPanel.add(topSection, BorderLayout.NORTH);
        quizPanel.add(txtQuestion, BorderLayout.CENTER);
        quizPanel.add(quizBtnPanel, BorderLayout.SOUTH);

        return quizPanel;
    }

    private JButton createAnswerButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = Math.min(getWidth(), getHeight()) / 4; // 25% radius
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                // Paint text on top
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = Math.min(getWidth(), getHeight()) / 4; // 25% radius
                g2.setColor(getBackground().equals(Color.WHITE) || getBackground().equals(COLOR_WHITE)
                        ? COLOR_PRIMARY_GREEN : getBackground().darker());
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(180, 60));
        btn.setBackground(bg);
        btn.setForeground(bg.equals(Color.WHITE) || bg.equals(COLOR_WHITE) ? COLOR_PRIMARY_GREEN : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Results Panel
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        lblResultTitle = new JLabel("Assessment Complete");
        lblResultTitle.setFont(headingFont(24f));
        lblResultTitle.setForeground(COLOR_FOREST_GREEN);
        lblResultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultScore = new JLabel("0 / 0");
        lblResultScore.setFont(headingFont(38f));
        lblResultScore.setForeground(COLOR_PRIMARY_GREEN);
        lblResultScore.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultPercent = new JLabel("Raw: 0%");
        lblResultPercent.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblResultPercent.setForeground(COLOR_MID_GREY);
        lblResultPercent.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultCurved = new JLabel("Curved: 0%");
        lblResultCurved.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblResultCurved.setForeground(COLOR_DARK_TEAL);
        lblResultCurved.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultMorale = new JLabel("Total Morale Boost: 0.0");
        lblResultMorale.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblResultMorale.setForeground(COLOR_WARNING_ORANGE);
        lblResultMorale.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultTier = new JLabel("Performance: —");
        lblResultTier.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblResultTier.setForeground(COLOR_TEXT_DARK);
        lblResultTier.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultIntellect = new JLabel("Intellect: —");
        lblResultIntellect.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblResultIntellect.setForeground(COLOR_DARK_GREEN);
        lblResultIntellect.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(lblResultTitle);
        center.add(Box.createRigidArea(new Dimension(0, 15)));
        center.add(lblResultScore);
        center.add(Box.createRigidArea(new Dimension(0, 6)));
        center.add(lblResultPercent);
        center.add(Box.createRigidArea(new Dimension(0, 6)));
        center.add(lblResultCurved);
        center.add(Box.createRigidArea(new Dimension(0, 10)));
        center.add(lblResultMorale);
        center.add(Box.createRigidArea(new Dimension(0, 8)));
        center.add(lblResultTier);
        center.add(Box.createRigidArea(new Dimension(0, 12)));
        center.add(lblResultIntellect);

        // Reasoning text area (grader interpretation) — light grey card
        txtResultReasoning = new JTextArea(8, 50);
        txtResultReasoning.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtResultReasoning.setForeground(COLOR_DARK_GREY);
        txtResultReasoning.setBackground(COLOR_LIGHT_GREY);
        txtResultReasoning.setEditable(false);
        txtResultReasoning.setLineWrap(true);
        txtResultReasoning.setWrapStyleWord(true);
        txtResultReasoning.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane reasoningScroll = new JScrollPane(txtResultReasoning);
        reasoningScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_LIGHT_GREY),
                "Grader Interpretation",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.PLAIN, 11),
                COLOR_MID_GREY));
        reasoningScroll.setOpaque(false);
        reasoningScroll.getViewport().setOpaque(false);
        reasoningScroll.setPreferredSize(new Dimension(0, 180));

        JPanel mainCenter = new JPanel(new BorderLayout(0, 10));
        mainCenter.setOpaque(false);
        mainCenter.add(center, BorderLayout.NORTH);
        mainCenter.add(reasoningScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton btnRetry = new JButton("Retake Test");
        styleButton(btnRetry, COLOR_PRIMARY_GREEN, 14, false);
        btnRetry.addActionListener(e -> startAdaptiveTest());

        JButton btnMenu = new JButton("Back to Welcome");
        styleButton(btnMenu, COLOR_MID_GREY, 14, false);
        btnMenu.addActionListener(e -> {
            resetQuizColors();
            cardLayout.show(cardPanel, CARD_WELCOME);
        });

        btnPanel.add(btnRetry);
        btnPanel.add(btnMenu);

        panel.add(mainCenter, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Adaptive Test Logic
    // ─────────────────────────────────────────────────────────────────────────

    private void startAdaptiveTest() {
        // Reload language in case config changed
        langPack = LanguagePack.load(config.getLanguage());

        // Build the ordered question pool from language pack: easy → expert
        List<String> pool = new ArrayList<>();
        Collections.addAll(pool, langPack.getEasyQuestions());
        Collections.addAll(pool, langPack.getModerateQuestions());
        Collections.addAll(pool, langPack.getMediumQuestions());
        Collections.addAll(pool, langPack.getHardQuestions());
        Collections.addAll(pool, langPack.getExpertQuestions());

        // Insert lie-scale questions at designated positions
        // These replace the question at that index with a lie-scale trap question
        for (int i = 0; i < LieScaleDetector.LIE_QUESTION_POSITIONS.length; i++) {
            int pos = LieScaleDetector.LIE_QUESTION_POSITIONS[i];
            if (pos < pool.size() && i < LieScaleDetector.LIE_QUESTIONS_EN.length) {
                pool.set(pos, LieScaleDetector.LIE_QUESTIONS_EN[i]);
            }
        }

        orderedQuestions = pool.toArray(new String[0]);
        int total = orderedQuestions.length;

        engine = new AdaptiveTestEngine(total, curveFilter);
        lieDetector = new LieScaleDetector(total);

        // Apply difficulty settings from config
        engine.setBaseTimeSec(config.getBaseTimeForDifficulty());

        liveIQ = new LiveIQEstimator();
        currentQuestionIndex = 0;
        score = 0;
        answers = new boolean[total];

        // Apply font settings from config
        txtQuestion.setFont(config.getQuestionFont());

        // Text color: dark grey for English, Fidelity green for other languages
        String lang = config.getLanguage();
        if (lang == null || lang.toLowerCase().startsWith("en")) {
            txtQuestion.setForeground(COLOR_DARK_GREY);
        } else {
            txtQuestion.setForeground(COLOR_PRIMARY_GREEN);
        }

        resetQuizColors();
        showCurrentQuestion();
        cardLayout.show(cardPanel, CARD_QUIZ);
    }

    private void showCurrentQuestion() {
        int total = orderedQuestions.length;
        lblQuizTitle.setText("Adaptive Moral Assessment");
        lblProgress.setText(String.format("Question %d of %d", currentQuestionIndex + 1, total));
        lblDifficulty.setText(String.format("Difficulty: %s (Tier %d/5)",
                engine.getDifficultyLabel(currentQuestionIndex),
                engine.getDifficultyTier(currentQuestionIndex)));
        txtQuestion.setText(orderedQuestions[currentQuestionIndex]);

        // Update morale/streak display
        lblMorale.setText(String.format("Morale: %.1f", engine.getTotalMorale()));
        lblStreak.setText(String.format("Streak: %d 🔥", engine.getStreak()));

        // Timer for this question (21–24s base + bonus if earned)
        timeRemaining = engine.getAllowedTimeForQuestion(currentQuestionIndex);
        lblTimer.setText(String.format("⏱ %ds", timeRemaining));

        // Show bonus time indicator if active
        int bonus = engine.getBonusTimeSec();
        int remaining = engine.getBonusQuestionsRemaining();
        if (bonus > 0 && remaining > 0) {
            lblBonusTime.setText(String.format("+%ds bonus (%d questions left)", bonus, remaining));
        } else {
            lblBonusTime.setText("");
        }

        // Break button state
        if (engine.isBreakAvailable()) {
            btnBreak.setEnabled(true);
            btnBreak.setForeground(COLOR_TEAL);
            btnBreak.setText("☕ Break (2 min)");
        } else if (engine.isBreakUsed()) {
            btnBreak.setEnabled(false);
            btnBreak.setText("☕ Break used");
            btnBreak.setForeground(COLOR_MID_GREY);
        } else {
            btnBreak.setEnabled(false);
            int questionsUntilBreak = 45 - (currentQuestionIndex + 1);
            if (questionsUntilBreak > 0) {
                btnBreak.setText(String.format("☕ in %d Qs", questionsUntilBreak));
            }
        }

        btnYes.setEnabled(true);
        btnNo.setEnabled(true);

        // Start countdown timer
        if (questionTimer != null) questionTimer.stop();
        questionTimer = new Timer(1000, e -> {
            timeRemaining--;
            lblTimer.setText(String.format("⏱ %ds", timeRemaining));

            if (timeRemaining <= 5) {
                lblTimer.setForeground(COLOR_ERROR_RED);
            } else {
                lblTimer.setForeground(COLOR_DARK_GREEN);
            }

            if (timeRemaining <= 0) {
                questionTimer.stop();
                // Time expired — auto-answer NO (not moral under pressure)
                answerQuestion(false);
            }
        });
        questionTimer.start();

        // Apply performance tier colors if midpoint evaluated
        if (engine.isMidpointEvaluated()) {
            applyTierColors(engine.getPerformanceTier());
        }
    }

    private void answerQuestion(boolean yes) {
        if (orderedQuestions == null) return;
        if (currentQuestionIndex >= orderedQuestions.length) return;

        if (questionTimer != null) questionTimer.stop();

        btnYes.setEnabled(false);
        btnNo.setEnabled(false);

        answers[currentQuestionIndex] = yes;
        if (yes) score++;

        // Record in adaptive engine
        double boost = engine.recordAnswer(currentQuestionIndex, yes);

        // Record in lie-scale detector
        lieDetector.recordAnswer(currentQuestionIndex, yes);

        // Record in live IQ estimator (updates after EVERY question)
        Boolean prevAnswer = currentQuestionIndex > 0 ? answers[currentQuestionIndex - 1] : null;
        int tier = engine.getDifficultyTier(currentQuestionIndex);
        liveIQ.recordAnswer(yes, tier, prevAnswer);

        // Update IQ display (with lie-scale penalty applied)
        int iqPenalty = lieDetector.getIQPenalty();
        int adjustedMid = liveIQ.getIQMidpoint() + iqPenalty;
        if (iqPenalty < 0) {
            lblLiveIQ.setText(String.format("%s [%d lie adj]", liveIQ.getDisplayString(), iqPenalty));
        } else {
            lblLiveIQ.setText(liveIQ.getDisplayString());
        }
        lblWeighted.setText(String.format("%s | %s", liveIQ.getWeightedDisplay(), lieDetector.getShortDisplay()));

        // Color the IQ label based on passing status and lie reliability
        if (lieDetector.isUnreliable()) {
            lblLiveIQ.setForeground(COLOR_ERROR_RED); // red — unreliable
        } else if (liveIQ.isPassing()) {
            int mid = liveIQ.getIQMidpoint();
            if (mid >= 160) {
                lblLiveIQ.setForeground(COLOR_GOLD);            // gold for strong
            } else if (mid >= 140) {
                lblLiveIQ.setForeground(COLOR_PRIMARY_GREEN);   // green for gifted mean
            } else {
                lblLiveIQ.setForeground(COLOR_SUCCESS_GREEN);   // light green for passing
            }
        } else {
            lblLiveIQ.setForeground(COLOR_ERROR_RED); // red for at-risk
        }

        // Update display
        lblMorale.setText(String.format("Morale: %.1f", engine.getTotalMorale()));
        lblStreak.setText(String.format("Streak: %d 🔥", engine.getStreak()));

        // Live intellect classification (after 5+ answers for meaningful signal)
        if (currentQuestionIndex >= 4) {
            updateLiveIntellectDisplay();
        }

        // Check if tier changed (color feedback)
        if (engine.isMidpointEvaluated()) {
            applyTierColors(engine.getPerformanceTier());
        }

        currentQuestionIndex++;

        if (currentQuestionIndex >= orderedQuestions.length) {
            showResults();
        } else {
            Timer timer = new Timer(200, e -> {
                showCurrentQuestion();
                ((Timer) e.getSource()).stop();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Break system
    // ─────────────────────────────────────────────────────────────────────────

    private void takeBreak() {
        if (engine == null || !engine.isBreakAvailable()) return;

        engine.useBreak();
        btnBreak.setEnabled(false);
        btnBreak.setText("☕ Break used");
        btnBreak.setForeground(new Color(80, 80, 80));

        // Stop the question timer during break
        if (questionTimer != null) questionTimer.stop();

        // Disable answer buttons during break
        btnYes.setEnabled(false);
        btnNo.setEnabled(false);

        // Show break countdown (2 minutes = 120 seconds)
        final int[] breakTime = {120};
        lblTimer.setText("☕ BREAK: 2:00");
        lblTimer.setForeground(COLOR_TEAL);

        Timer breakTimer = new Timer(1000, null);
        breakTimer.addActionListener(e -> {
            breakTime[0]--;
            int mins = breakTime[0] / 60;
            int secs = breakTime[0] % 60;
            lblTimer.setText(String.format("☕ BREAK: %d:%02d", mins, secs));

            if (breakTime[0] <= 10) {
                lblTimer.setForeground(COLOR_WARNING_ORANGE); // warning
            }

            if (breakTime[0] <= 0) {
                breakTimer.stop();
                // Resume: restart the question timer
                lblTimer.setForeground(COLOR_DARK_GREEN);
                btnYes.setEnabled(true);
                btnNo.setEnabled(true);
                // Restart question timer with full time for current question
                timeRemaining = engine.getAllowedTimeForQuestion(currentQuestionIndex);
                lblTimer.setText(String.format("⏱ %ds", timeRemaining));
                questionTimer.start();
            }
        });
        breakTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Performance tier color theming
    // ─────────────────────────────────────────────────────────────────────────

    private void applyTierColors(AdaptiveTestEngine.PerformanceTier tier) {
        Color bg;
        Color accent;
        String tierLabel;

        switch (tier) {
            case GREEN:
                bg = COLOR_GREEN_BG;
                accent = COLOR_GREEN_ACCENT;
                tierLabel = "● Doing Well";
                break;
            case SILVER:
                bg = COLOR_SILVER_BG;
                accent = COLOR_SILVER_ACCENT;
                tierLabel = "● Doing Great";
                break;
            case GOLD:
                bg = COLOR_GOLD_BG;
                accent = COLOR_GOLD_ACCENT;
                tierLabel = "★ Superb!";
                break;
            default:
                bg = COLOR_NEUTRAL_BG;
                accent = COLOR_NEUTRAL_ACCENT;
                tierLabel = "";
                break;
        }

        quizPanel.setBackground(bg);
        lblQuizTitle.setForeground(accent);
        lblMorale.setForeground(accent);

        if (!tierLabel.isEmpty()) {
            lblQuizTitle.setText("Adaptive Moral Assessment — " + tierLabel);
        }
    }

    private void resetQuizColors() {
        quizPanel.setBackground(COLOR_WHITE);
        lblQuizTitle.setForeground(COLOR_PRIMARY_GREEN);
        lblMorale.setForeground(COLOR_PRIMARY_GREEN);
        lblIntellect.setText("Intellect: —");
        lblIntellect.setForeground(COLOR_DARK_TEAL);
    }

    /**
     * Runs the intellect classifier on current partial answers and updates the
     * live display so the grader can see which tier the test-taker is maintaining.
     */
    private void updateLiveIntellectDisplay() {
        int answered = currentQuestionIndex + 1;
        boolean[] partialCorrectness = new boolean[answered];
        boolean[] partialMorality = new boolean[answered];
        int[] partialTiers = new int[answered];

        System.arraycopy(engine.getCorrectnessBits(), 0, partialCorrectness, 0, answered);
        System.arraycopy(engine.getMoralityBits(), 0, partialMorality, 0, answered);
        System.arraycopy(engine.getDifficultyTiers(), 0, partialTiers, 0, answered);

        classifier = new IntellectClassifier(partialCorrectness, partialMorality, partialTiers);

        IntellectClassifier.IntellectTier tier = classifier.getClassifiedTier();
        Color tierColor;
        switch (tier) {
            case VA:
                tierColor = COLOR_TEAL;               // teal for thoughtful
                break;
            case S:
                tierColor = COLOR_GOLD;               // gold for disciplined
                break;
            case PG:
                tierColor = COLOR_DARK_GREEN;         // dark green for comprehensive
                break;
            default:
                tierColor = COLOR_DARK_TEAL;
                break;
        }

        // Show tier + IQ estimate live
        String iqNote = classifier.isGiftedProtocol()
                ? String.format("[%s] %s ★IQ:180+", tier.getCode(), tier.getDisplayName())
                : String.format("[%s] %s ~IQ:%d", tier.getCode(), tier.getDisplayName(), classifier.getEstimatedIQMidpoint());

        lblIntellect.setText("Intellect: " + iqNote);
        lblIntellect.setForeground(tierColor);

        // Special glow for gifted protocol
        if (classifier.isGiftedProtocol()) {
            lblIntellect.setForeground(COLOR_FOREST_GREEN); // deep green for 180+
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Results
    // ─────────────────────────────────────────────────────────────────────────

    private void showResults() {
        int total = orderedQuestions.length;

        lblResultTitle.setText("Adaptive Moral Assessment — Complete");
        lblResultScore.setText(String.format("%d / %d", score, total));

        double rawPct = (double) score / total * 100.0;
        lblResultPercent.setText(String.format("Raw: %.1f%%", rawPct));

        double curvedPct = curveFilter.getCurvedPercentage(answers);
        double curvedScore = curveFilter.applyCurve(answers);
        double maxCurved = curveFilter.getMaxScore(total);
        lblResultCurved.setText(String.format("Curved: %.1f%% (%.1f / %.1f weighted)",
                curvedPct, curvedScore, maxCurved));

        lblResultMorale.setText(String.format("Morale: %.1f  |  %s  |  %s  |  %s",
                engine.getTotalMorale(),
                liveIQ.getPassDisplay(),
                liveIQ.getWeightedDisplay(),
                lieDetector.getShortDisplay()));

        // Performance tier
        AdaptiveTestEngine.PerformanceTier tier = engine.getPerformanceTier();
        Color tierColor;
        String tierText;
        switch (tier) {
            case GREEN:
                tierColor = COLOR_SUCCESS_GREEN;
                tierText = "Performance: ● WELL";
                break;
            case SILVER:
                tierColor = COLOR_MID_GREY;
                tierText = "Performance: ● GREAT";
                break;
            case GOLD:
                tierColor = COLOR_GOLD;
                tierText = "Performance: ★ SUPERB";
                break;
            default:
                tierColor = COLOR_MID_GREY;
                tierText = "Performance: Needs Improvement";
                break;
        }
        lblResultTier.setText(tierText);
        lblResultTier.setForeground(tierColor);
        lblResultScore.setForeground(tierColor);

        // Final intellect classification
        classifier = new IntellectClassifier(
                engine.getCorrectnessBits(),
                engine.getMoralityBits(),
                engine.getDifficultyTiers());

        IntellectClassifier.IntellectTier intellectTier = classifier.getClassifiedTier();
        Color intellectColor;
        switch (intellectTier) {
            case VA:
                intellectColor = COLOR_TEAL;
                break;
            case S:
                intellectColor = COLOR_GOLD;
                break;
            case PG:
                intellectColor = COLOR_DARK_GREEN;
                break;
            default:
                intellectColor = COLOR_TEXT_DARK;
                break;
        }

        // Override color for gifted protocol
        if (classifier.isGiftedProtocol()) {
            intellectColor = COLOR_FOREST_GREEN; // deep green for 180+
        }

        String iqDisplay = classifier.isGiftedProtocol()
                ? String.format("Intellect: [%s] %s — ★ IQ 180+ (Gifted Protocol)",
                        intellectTier.getCode(), intellectTier.getDisplayName())
                : String.format("Intellect: [%s] %s — IQ %d–%d (%s)",
                        intellectTier.getCode(), intellectTier.getDisplayName(),
                        classifier.getEstimatedIQLow(), classifier.getEstimatedIQHigh(),
                        classifier.getIqBand());

        lblResultIntellect.setText(iqDisplay);
        lblResultIntellect.setForeground(intellectColor);

        // Show reasoning in the text area (intellect + lie scale analysis)
        String fullReasoning = classifier.getReasoning() + "\n" + lieDetector.getReasoning();
        txtResultReasoning.setText(fullReasoning);

        saveScore();
        cardLayout.show(cardPanel, CARD_RESULTS);
    }

    private void saveScore() {
        int total = orderedQuestions.length;
        double curvedPct = curveFilter.getCurvedPercentage(answers);

        Path outFile = Path.of("src", "questions", "AdaptiveTest_score.txt");
        StringBuilder content = new StringBuilder();
        content.append(String.format("score=%d%n", score));
        content.append(String.format("total=%d%n", total));
        content.append(String.format("curved_percent=%.2f%n", curvedPct));
        content.append(String.format("curve_config=%s%n", Arrays.toString(curveFilter.getWeights())));
        content.append(engine.getSummary());
        content.append(String.format("─────────────────────────────────%n"));
        if (liveIQ != null) {
            content.append(liveIQ.toFileString());
            content.append(String.format("─────────────────────────────────%n"));
        }
        if (lieDetector != null) {
            content.append(lieDetector.toFileString());
            content.append(String.format("─────────────────────────────────%n"));
            content.append(lieDetector.getReasoning());
            content.append(String.format("─────────────────────────────────%n"));
        }
        if (classifier != null) {
            content.append(classifier.toFileString());
            content.append(String.format("─────────────────────────────────%n"));
            content.append(classifier.getReasoning());
        }

        try {
            Files.createDirectories(outFile.getParent());
            Files.writeString(outFile, content.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save score: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────

    private void styleButton(JButton btn, Color bg, int fontSize, boolean bold) {
        btn.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, fontSize));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main (legacy — use gui.Main as primary entry point)
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Delegate to Main class
        lsat.Main.main(args);
    }
}
