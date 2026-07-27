package lsat.engine;

/**
 * AdaptiveTestEngine — Controls the pacing, difficulty progression, morality tracking,
 * morale boost system, mid-test prediction, and visual feedback for the test session.
 *
 * Design:
 * - Questions are ordered from easy (beginning) to hard (end) using a configurable
 *   "running start" — the first questions are straightforward, ramping up in difficulty.
 * - Each question has both a CORRECTNESS bit and a MORALITY bit:
 *   - Correctness: Did the test-taker answer affirmatively (Y)?
 *   - Morality: Does the answer reflect moral reasoning? (tracked per-question via difficulty tier)
 * - Morale boost accumulates for correct+moral answers, with streak bonuses.
 * - At the midpoint, the engine evaluates whether the test-taker is likely to succeed
 *   relative to the moral curve, and signals a performance tier:
 *   - GREEN  = doing well (on track)
 *   - SILVER = doing great (above expectation)
 *   - GOLD   = superb (exceptional performance)
 * - Time allotment per question increases with difficulty tier.
 */
public class AdaptiveTestEngine {

    // ─────────────────────────────────────────────────────────────────────────
    // Performance tiers
    // ─────────────────────────────────────────────────────────────────────────

    public enum PerformanceTier {
        /** Below threshold — no special feedback */
        NEUTRAL,
        /** On track to succeed */
        GREEN,
        /** Above expected performance */
        SILVER,
        /** Exceptional — superb moral + correctness */
        GOLD
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Difficulty configuration
    // ─────────────────────────────────────────────────────────────────────────

    /** Difficulty tiers: 1 (easy) to 5 (hard). Each question is assigned a tier. */
    public static final int TIER_EASY = 1;
    public static final int TIER_MODERATE = 2;
    public static final int TIER_MEDIUM = 3;
    public static final int TIER_HARD = 4;
    public static final int TIER_EXPERT = 5;

    /** Base time per question in seconds — most questions require a careful read */
    private int baseTimeSec = 21;

    /** Maximum base time (varies 21–24 depending on tier complexity) */
    private int maxBaseTimeSec = 24;

    /** Additional seconds added per difficulty tier above 1 (smaller now since base is higher) */
    private int timePerTierSec = 1;

    /** Bonus seconds earned by a 7-correct streak (2–7 per question, for next 45 questions) */
    private int bonusTimeSec = 0;

    /** How many questions remain with the bonus time active */
    private int bonusQuestionsRemaining = 0;

    /** Whether the 7-in-a-row streak bonus has been triggered */
    private boolean streakBonusEarned = false;

    /** Break eligibility: test-taker may request one 2-minute break after 45 questions */
    private boolean breakAvailable = false;

    /** Whether the break has already been used */
    private boolean breakUsed = false;

    /** Total questions answered/addressed so far (for break eligibility) */
    private int questionsAddressed = 0;

    /** Running start configuration: what % of questions are easy at the beginning */
    private double easyStartPercent = 0.25;   // first 25% are easy
    private double middlePercent = 0.50;       // middle 50% ramp up
    // remaining 25% are hardest

    // ─────────────────────────────────────────────────────────────────────────
    // State tracking
    // ─────────────────────────────────────────────────────────────────────────

    private final int totalQuestions;
    private final int[] difficultyTiers;       // difficulty per question index
    private final boolean[] correctness;       // did they answer correctly (Y)
    private final boolean[] morality;          // did the answer reflect moral reasoning
    private final double[] moralBoost;         // accumulated morale boost per question

    private int currentIndex;
    private int streak;                        // consecutive correct+moral answers
    private double totalMorale;               // running morale total
    private PerformanceTier currentTier;
    private boolean midpointEvaluated;

    private final MoralCurveFilter curveFilter;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param totalQuestions number of questions in this test session
     * @param curveFilter   the moral curve filter (for midpoint evaluation)
     */
    public AdaptiveTestEngine(int totalQuestions, MoralCurveFilter curveFilter) {
        this.totalQuestions = totalQuestions;
        this.curveFilter = curveFilter;
        this.difficultyTiers = new int[totalQuestions];
        this.correctness = new boolean[totalQuestions];
        this.morality = new boolean[totalQuestions];
        this.moralBoost = new double[totalQuestions];
        this.currentIndex = 0;
        this.streak = 0;
        this.totalMorale = 0.0;
        this.currentTier = PerformanceTier.NEUTRAL;
        this.midpointEvaluated = false;

        assignDifficultyTiers();
    }

    /**
     * Assign difficulty tiers using the running start method:
     * - First easyStartPercent% → tiers 1-2 (easy warmup)
     * - Middle middlePercent% → tiers 2-4 (progressive ramp)
     * - Final remainder% → tiers 4-5 (hardest)
     */
    private void assignDifficultyTiers() {
        int easyEnd = (int) (totalQuestions * easyStartPercent);
        int middleEnd = (int) (totalQuestions * (easyStartPercent + middlePercent));

        for (int i = 0; i < totalQuestions; i++) {
            if (i < easyEnd) {
                // Beginning: easy (tier 1-2)
                double progress = (double) i / Math.max(1, easyEnd);
                difficultyTiers[i] = progress < 0.5 ? TIER_EASY : TIER_MODERATE;
            } else if (i < middleEnd) {
                // Middle: progressive ramp (tier 2-4)
                double progress = (double) (i - easyEnd) / Math.max(1, middleEnd - easyEnd);
                if (progress < 0.33) {
                    difficultyTiers[i] = TIER_MODERATE;
                } else if (progress < 0.66) {
                    difficultyTiers[i] = TIER_MEDIUM;
                } else {
                    difficultyTiers[i] = TIER_HARD;
                }
            } else {
                // End: hardest (tier 4-5)
                double progress = (double) (i - middleEnd) / Math.max(1, totalQuestions - middleEnd);
                difficultyTiers[i] = progress < 0.5 ? TIER_HARD : TIER_EXPERT;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configuration (admin can adjust before test starts)
    // ─────────────────────────────────────────────────────────────────────────

    public void setBaseTimeSec(int seconds) { this.baseTimeSec = seconds; }
    public void setTimePerTierSec(int seconds) { this.timePerTierSec = seconds; }
    public void setEasyStartPercent(double pct) {
        this.easyStartPercent = Math.max(0.05, Math.min(0.50, pct));
        assignDifficultyTiers();
    }
    public void setMiddlePercent(double pct) {
        this.middlePercent = Math.max(0.20, Math.min(0.70, pct));
        assignDifficultyTiers();
    }

    public int getBaseTimeSec() { return baseTimeSec; }
    public int getTimePerTierSec() { return timePerTierSec; }
    public double getEasyStartPercent() { return easyStartPercent; }
    public double getMiddlePercent() { return middlePercent; }

    // ─────────────────────────────────────────────────────────────────────────
    // Query methods
    // ─────────────────────────────────────────────────────────────────────────

    /** Get the allowed time (seconds) for the current question */
    public int getAllowedTimeForQuestion(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= totalQuestions) return baseTimeSec;
        int tier = difficultyTiers[questionIndex];

        // Base time: 21–24 seconds depending on tier (most questions = careful read)
        // Tier 1 (easy) = 21s, Tier 2 = 22s, Tier 3 = 23s, Tier 4-5 = 24s
        int time = baseTimeSec + Math.min(tier - 1, maxBaseTimeSec - baseTimeSec);

        // Apply streak bonus if active (2–7 extra seconds per question)
        if (bonusQuestionsRemaining > 0) {
            time += bonusTimeSec;
        }

        return time;
    }

    /** Check if the 2-minute break is available (after 45 questions, not yet used) */
    public boolean isBreakAvailable() {
        return breakAvailable && !breakUsed;
    }

    /** Use the break (call when test-taker requests it) */
    public void useBreak() {
        if (breakAvailable && !breakUsed) {
            breakUsed = true;
        }
    }

    /** Has the break been used? */
    public boolean isBreakUsed() { return breakUsed; }

    /** Get bonus time seconds currently active */
    public int getBonusTimeSec() { return bonusQuestionsRemaining > 0 ? bonusTimeSec : 0; }

    /** Get remaining questions with bonus time */
    public int getBonusQuestionsRemaining() { return bonusQuestionsRemaining; }

    /** Get difficulty tier for a question (1–5) */
    public int getDifficultyTier(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= totalQuestions) return TIER_EASY;
        return difficultyTiers[questionIndex];
    }

    /** Get difficulty label */
    public String getDifficultyLabel(int questionIndex) {
        int tier = getDifficultyTier(questionIndex);
        switch (tier) {
            case TIER_EASY: return "Easy";
            case TIER_MODERATE: return "Moderate";
            case TIER_MEDIUM: return "Medium";
            case TIER_HARD: return "Hard";
            case TIER_EXPERT: return "Expert";
            default: return "Unknown";
        }
    }

    /** Current streak of correct+moral answers in a row */
    public int getStreak() { return streak; }

    /** Total accumulated morale boost */
    public double getTotalMorale() { return totalMorale; }

    /** Current performance tier (updated at midpoint and beyond) */
    public PerformanceTier getPerformanceTier() { return currentTier; }

    /** Has the midpoint been evaluated? */
    public boolean isMidpointEvaluated() { return midpointEvaluated; }

    /** Get the morality bit array */
    public boolean[] getMoralityBits() { return morality.clone(); }

    /** Get the correctness bit array */
    public boolean[] getCorrectnessBits() { return correctness.clone(); }

    /** Get morale boost per question */
    public double[] getMoraleBoosts() { return moralBoost.clone(); }

    /** Get difficulty tiers array */
    public int[] getDifficultyTiers() { return difficultyTiers.clone(); }

    public int getTotalQuestions() { return totalQuestions; }

    // ─────────────────────────────────────────────────────────────────────────
    // Core logic: record an answer
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Record an answer for the current question.
     *
     * Morality determination:
     * - For moral questions (the Y/N ethical questions), answering Y = moral
     * - The difficulty tier modulates the morality weight:
     *   higher-tier questions where the test-taker answers morally carry more weight
     *
     * Morale boost calculation:
     * - Base boost: 1.0 per correct answer
     * - Tier multiplier: × (tier / 3.0) — harder questions boost more
     * - Streak bonus: +0.5 for each consecutive correct+moral answer (capped at +5.0)
     *
     * @param answeredYes true if the test-taker answered Y
     * @return the morale boost earned for this question
     */
    public double recordAnswer(int questionIndex, boolean answeredYes) {
        if (questionIndex < 0 || questionIndex >= totalQuestions) return 0.0;

        correctness[questionIndex] = answeredYes;

        // Morality: answering "yes" to these ethical questions = moral response
        // (the question bank is designed so Y = the moral/virtuous answer)
        morality[questionIndex] = answeredYes;

        int tier = difficultyTiers[questionIndex];

        if (answeredYes) {
            // Correct + moral answer
            streak++;
            double tierMultiplier = tier / 3.0;
            double streakBonus = Math.min(5.0, streak * 0.5);
            double boost = (1.0 + streakBonus) * tierMultiplier;
            moralBoost[questionIndex] = boost;
            totalMorale += boost;

            // ── Streak bonus timer: 7 correct in a row earns 2–7 extra seconds ──
            if (streak == 7 && !streakBonusEarned) {
                streakBonusEarned = true;
                // Bonus scales with how far into the test (harder = more bonus)
                // Early streak (first 20 questions): +2s
                // Mid streak: +4s
                // Late streak: +7s
                double progress = (double) questionIndex / totalQuestions;
                if (progress < 0.25) {
                    bonusTimeSec = 2;
                } else if (progress < 0.50) {
                    bonusTimeSec = 4;
                } else if (progress < 0.75) {
                    bonusTimeSec = 5;
                } else {
                    bonusTimeSec = 7;
                }
                bonusQuestionsRemaining = 45;
            }
        } else {
            // Not moral — break streak
            streak = 0;
            moralBoost[questionIndex] = 0.0;
        }

        // Decrement bonus questions remaining
        if (bonusQuestionsRemaining > 0) {
            bonusQuestionsRemaining--;
        }

        // Track questions addressed for break eligibility
        questionsAddressed = questionIndex + 1;
        if (questionsAddressed >= 45 && !breakUsed) {
            breakAvailable = true;
        }

        currentIndex = questionIndex + 1;

        // Check midpoint
        int midpoint = totalQuestions / 2;
        if (!midpointEvaluated && currentIndex >= midpoint) {
            evaluateMidpoint();
        }
        // Continue evaluating after midpoint for progressive tier updates
        if (midpointEvaluated && currentIndex > midpoint) {
            updateTierProgressive();
        }

        return moralBoost[questionIndex];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Midpoint evaluation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * At the midpoint, evaluate whether the test-taker is on track to succeed.
     * Uses the moral curve filter to compute a projected curved score.
     *
     * Thresholds (relative to curve-adjusted expectation):
     * - >= 80% of max possible at midpoint → GOLD (superb)
     * - >= 65% → SILVER (great)
     * - >= 50% → GREEN (well)
     * - < 50% → NEUTRAL
     */
    private void evaluateMidpoint() {
        midpointEvaluated = true;

        int midpoint = currentIndex;

        // Build a partial answers array up to current point
        boolean[] partialAnswers = new boolean[midpoint];
        System.arraycopy(correctness, 0, partialAnswers, 0, midpoint);

        double curvedScore = curveFilter.applyCurve(partialAnswers);
        double maxAtMid = curveFilter.getMaxScore(midpoint);

        if (maxAtMid <= 0) {
            currentTier = PerformanceTier.NEUTRAL;
            return;
        }

        double ratio = curvedScore / maxAtMid;

        if (ratio >= 0.80) {
            currentTier = PerformanceTier.GOLD;
        } else if (ratio >= 0.65) {
            currentTier = PerformanceTier.SILVER;
        } else if (ratio >= 0.50) {
            currentTier = PerformanceTier.GREEN;
        } else {
            currentTier = PerformanceTier.NEUTRAL;
        }
    }

    /**
     * After midpoint, continue to update tier based on running performance.
     * The tier can go up if the test-taker improves, but does not drop below
     * the midpoint evaluation (graceful behavior).
     */
    private void updateTierProgressive() {
        boolean[] partialAnswers = new boolean[currentIndex];
        System.arraycopy(correctness, 0, partialAnswers, 0, currentIndex);

        double curvedScore = curveFilter.applyCurve(partialAnswers);
        double maxSoFar = curveFilter.getMaxScore(currentIndex);

        if (maxSoFar <= 0) return;

        double ratio = curvedScore / maxSoFar;

        // Only upgrade, never downgrade after midpoint (graceful)
        PerformanceTier newTier;
        if (ratio >= 0.80) {
            newTier = PerformanceTier.GOLD;
        } else if (ratio >= 0.65) {
            newTier = PerformanceTier.SILVER;
        } else if (ratio >= 0.50) {
            newTier = PerformanceTier.GREEN;
        } else {
            newTier = PerformanceTier.NEUTRAL;
        }

        // Only upgrade
        if (newTier.ordinal() > currentTier.ordinal()) {
            currentTier = newTier;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Summary data for saving
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generate a summary string for file output.
     */
    public String getSummary() {
        int correctCount = 0;
        int moralCount = 0;
        for (int i = 0; i < totalQuestions; i++) {
            if (correctness[i]) correctCount++;
            if (morality[i]) moralCount++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("total_questions=%d%n", totalQuestions));
        sb.append(String.format("correct=%d%n", correctCount));
        sb.append(String.format("moral=%d%n", moralCount));
        sb.append(String.format("total_morale_boost=%.2f%n", totalMorale));
        sb.append(String.format("max_streak=%d%n", streak)); // final streak (may not be max, but close)
        sb.append(String.format("performance_tier=%s%n", currentTier.name()));
        sb.append(String.format("morality_bits=%s%n", bitsToString(morality)));
        sb.append(String.format("correctness_bits=%s%n", bitsToString(correctness)));
        sb.append(String.format("difficulty_tiers=%s%n", java.util.Arrays.toString(difficultyTiers)));
        return sb.toString();
    }

    private static String bitsToString(boolean[] bits) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : bits) sb.append(b ? '1' : '0');
        return sb.toString();
    }

    private static String Arrays_toString(int[] arr) {
        return java.util.Arrays.toString(arr);
    }
}
