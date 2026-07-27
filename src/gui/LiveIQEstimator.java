package gui;

/**
 * LiveIQEstimator — Computes a running IQ estimate after every single question.
 * Produces a tight range when confident (consistent performance) and a wider
 * range when the pattern is divergent or too early to tell.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * DESIGN PRINCIPLES
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * 1. PASSING THRESHOLD: A passing student has IQ ~125 or above.
 *    Below 125 = failing. At 125 = borderline pass. Above = pass with room.
 *
 * 2. IQ WEIGHT IN OVERALL SCORE:
 *    The test-taker's estimated IQ contributes at least 40% of the overall
 *    weighted score when the test itself evaluates as only slightly meritorious
 *    or very slightly meritorious. This prevents a "hard" test from unfairly
 *    penalizing a clearly intelligent test-taker.
 *
 *    Overall Weighted Score = (IQ_weight × IQ_component) + (Test_weight × Test_component)
 *    Where:
 *      - If test merit is LOW (slightly meritorious): IQ_weight = 0.50, Test_weight = 0.50
 *      - If test merit is MODERATE: IQ_weight = 0.40, Test_weight = 0.60
 *      - If test merit is HIGH (clearly meritorious): IQ_weight = 0.30, Test_weight = 0.70
 *
 * 3. RANGE TIGHTNESS:
 *    - After 1-3 questions: wide range (±20 IQ points)
 *    - After 4-10 questions: moderate range (±12 IQ points)
 *    - After 11-30 questions: narrowing range (±8 IQ points)
 *    - After 30+ questions: tight range (±5 IQ points) IF consistent
 *    - Divergent patterns widen the range by up to 2× regardless of count
 *
 * 4. QUESTION-APPROPRIATE ANSWERS:
 *    The expected "correct" answer for IQ purposes depends on question difficulty:
 *    - Easy (tier 1-2): YES is the expected answer (basic moral competence)
 *    - Moderate (tier 3): YES is expected but NO can signal deeper thought
 *    - Hard (tier 4-5): YES demonstrates conviction; NO with pattern = VA-thinking
 *    For IQ estimation, we credit BOTH straightforward YES answers AND
 *    patterned NO answers that show deliberate higher-order reasoning.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class LiveIQEstimator {

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Passing IQ threshold */
    public static final int PASSING_IQ = 125;

    /** Minimum possible IQ estimate (floor) */
    public static final int IQ_FLOOR = 115;

    /** Maximum possible IQ estimate (ceiling) */
    public static final int IQ_CEILING = 200;

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private int questionsAnswered;
    private int correctCount;    // straightforward correct (YES)
    private int iqCredits;       // IQ-creditworthy answers (includes patterned NOs)
    private int easyCorrect, easyTotal;
    private int midCorrect, midTotal;
    private int hardCorrect, hardTotal;

    // Running window for divergence detection
    private final boolean[] recentAnswers;  // circular buffer of last 10
    private int recentIndex;
    private int recentYes;

    // Pattern tracking
    private int currentStreak;
    private int longestStreak;
    private int patternChanges;  // how often answer flips Y→N or N→Y

    // IQ estimate state
    private double runningIQEstimate;
    private int iqLow;
    private int iqHigh;
    private int iqMidpoint;
    private double confidence;   // 0.0 = no confidence, 1.0 = full confidence
    private boolean isPassing;

    // Overall weighted score components
    private double iqComponent;      // 0.0–1.0 normalized IQ contribution
    private double testComponent;    // 0.0–1.0 raw test performance
    private double overallWeighted;  // combined weighted score
    private double iqWeight;         // actual weight used (0.40–0.50)
    private String meritLevel;       // "Slightly", "Very Slightly", "Moderate", "High"

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public LiveIQEstimator() {
        this.recentAnswers = new boolean[10];
        reset();
    }

    public void reset() {
        questionsAnswered = 0;
        correctCount = 0;
        iqCredits = 0;
        easyCorrect = easyTotal = 0;
        midCorrect = midTotal = 0;
        hardCorrect = hardTotal = 0;
        recentIndex = 0;
        recentYes = 0;
        currentStreak = 0;
        longestStreak = 0;
        patternChanges = 0;
        runningIQEstimate = 140.0; // start at assumed mean
        iqLow = 120;
        iqHigh = 160;
        iqMidpoint = 140;
        confidence = 0.0;
        isPassing = true;
        iqComponent = 0.5;
        testComponent = 0.5;
        overallWeighted = 0.5;
        iqWeight = 0.40;
        meritLevel = "Moderate";
        java.util.Arrays.fill(recentAnswers, false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core: record answer and update IQ estimate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Record one answer and recompute the live IQ range.
     *
     * @param answeredYes   did the test-taker answer YES
     * @param difficultyTier the question's difficulty tier (1–5)
     * @param previousAnswer the answer to the previous question (null if first)
     */
    public void recordAnswer(boolean answeredYes, int difficultyTier, Boolean previousAnswer) {
        questionsAnswered++;

        // Track per-tier accuracy
        if (difficultyTier <= 2) {
            easyTotal++;
            if (answeredYes) easyCorrect++;
        } else if (difficultyTier == 3) {
            midTotal++;
            if (answeredYes) midCorrect++;
        } else {
            hardTotal++;
            if (answeredYes) hardCorrect++;
        }

        // Straightforward correctness
        if (answeredYes) correctCount++;

        // IQ credit: YES always counts. NO counts if it shows pattern intelligence.
        // A NO on an easy question AFTER a YES streak = deliberate (VA-signal, IQ credit)
        // A NO that breaks a long streak = thoughtful pause (credit)
        // A random NO with no pattern = no credit
        boolean iqCredit = false;
        if (answeredYes) {
            iqCredit = true;
        } else {
            // NO answer — credit if it appears deliberate
            if (difficultyTier <= 2 && currentStreak >= 3) {
                // Said NO to easy question after a YES streak — deliberate thought
                iqCredit = true;
            } else if (difficultyTier >= 4 && previousAnswer != null && !previousAnswer) {
                // Consecutive NO on hard questions — philosophical stance, credit
                iqCredit = true;
            } else if (questionsAnswered > 5 && patternChanges > 0) {
                // Has shown pattern changes = engaged thinker, credit the NO
                double changeRate = (double) patternChanges / questionsAnswered;
                if (changeRate > 0.1 && changeRate < 0.5) {
                    iqCredit = true; // moderate pattern changes = deliberate
                }
            }
        }
        if (iqCredit) iqCredits++;

        // Streak tracking
        if (answeredYes) {
            currentStreak++;
            if (currentStreak > longestStreak) longestStreak = currentStreak;
        } else {
            currentStreak = 0;
        }

        // Pattern changes
        if (previousAnswer != null && answeredYes != previousAnswer) {
            patternChanges++;
        }

        // Recent window (circular buffer)
        if (questionsAnswered > 10) {
            if (recentAnswers[recentIndex % 10]) recentYes--;
        }
        recentAnswers[recentIndex % 10] = answeredYes;
        if (answeredYes) recentYes++;
        recentIndex++;

        // ── Compute IQ estimate ──
        computeIQEstimate(difficultyTier);
    }

    private void computeIQEstimate(int lastTier) {
        // Base IQ from IQ-credit ratio (not just raw YES count)
        double creditRatio = (double) iqCredits / questionsAnswered;

        // Credit ratio to IQ: 0.0 → 115, 1.0 → 175 (before bonuses)
        double baseIQ = IQ_FLOOR + (creditRatio * 60);

        // Tier-weighted bonus: getting hard questions right = smarter
        if (hardTotal > 0) {
            double hardAcc = (double) hardCorrect / hardTotal;
            baseIQ += hardAcc * 12;
        }

        // Pattern sophistication bonus
        if (questionsAnswered > 5) {
            double changeRate = (double) patternChanges / questionsAnswered;
            // Moderate change rate (0.1–0.35) = thoughtful = smart
            if (changeRate > 0.10 && changeRate < 0.35) {
                baseIQ += 5;
            }
            // Very high streak with high correct = disciplined intelligence
            double streakRatio = (double) longestStreak / questionsAnswered;
            if (streakRatio > 0.6 && creditRatio > 0.75) {
                baseIQ += 5;
            }
        }

        // Easy-tier check: if they get easy ones wrong without pattern = lower IQ
        if (easyTotal > 3) {
            double easyAcc = (double) easyCorrect / easyTotal;
            if (easyAcc < 0.5) {
                baseIQ -= 10; // Missing basic questions without compensating pattern
            }
        }

        // Apply exponential moving average for stability
        double alpha = questionsAnswered <= 3 ? 0.7 : 0.3; // more responsive early
        runningIQEstimate = runningIQEstimate * (1 - alpha) + baseIQ * alpha;

        // Clamp
        runningIQEstimate = Math.max(IQ_FLOOR, Math.min(IQ_CEILING, runningIQEstimate));

        // ── Compute range width based on confidence ──
        computeConfidenceAndRange();

        // ── Compute overall weighted score ──
        computeOverallWeighted();

        // ── Passing determination ──
        isPassing = iqMidpoint >= PASSING_IQ;
    }

    private void computeConfidenceAndRange() {
        // Confidence increases with questions answered
        double baseConfidence;
        if (questionsAnswered <= 3) {
            baseConfidence = 0.15;
        } else if (questionsAnswered <= 10) {
            baseConfidence = 0.15 + (questionsAnswered - 3) * 0.07; // up to ~0.64
        } else if (questionsAnswered <= 30) {
            baseConfidence = 0.64 + (questionsAnswered - 10) * 0.015; // up to ~0.94
        } else {
            baseConfidence = Math.min(0.98, 0.94 + (questionsAnswered - 30) * 0.002);
        }

        // Divergence detection: if recent answers are inconsistent with overall, widen range
        double divergencePenalty = 0.0;
        if (questionsAnswered > 5) {
            int windowSize = Math.min(10, questionsAnswered);
            double recentRate = (double) recentYes / windowSize;
            double overallRate = (double) correctCount / questionsAnswered;
            double divergence = Math.abs(recentRate - overallRate);
            divergencePenalty = divergence * 0.5; // up to 0.5 penalty
        }

        confidence = Math.max(0.10, baseConfidence - divergencePenalty);

        // Range width: inverse of confidence
        // At confidence 1.0 → ±3 points (very tight)
        // At confidence 0.1 → ±22 points (very wide)
        double halfRange = 3 + (1.0 - confidence) * 19;

        iqMidpoint = (int) Math.round(runningIQEstimate);
        iqLow = (int) Math.max(IQ_FLOOR, Math.round(runningIQEstimate - halfRange));
        iqHigh = (int) Math.min(IQ_CEILING, Math.round(runningIQEstimate + halfRange));

        // Ensure minimum range of 4 points (never claim exact IQ)
        if (iqHigh - iqLow < 4) {
            iqLow = iqMidpoint - 2;
            iqHigh = iqMidpoint + 2;
        }
    }

    /**
     * Compute the overall weighted score factoring in IQ weight.
     *
     * When the test itself evaluates as only slightly meritorious (raw test
     * performance is mediocre), the IQ weight increases to at least 40%,
     * preventing the test difficulty from unfairly penalizing an intelligent taker.
     */
    private void computeOverallWeighted() {
        // Test component: raw credit ratio (0–1)
        testComponent = (double) correctCount / Math.max(1, questionsAnswered);

        // IQ component: normalized IQ on 0–1 scale (115=0.0, 175=1.0)
        iqComponent = Math.max(0.0, Math.min(1.0, (runningIQEstimate - 115) / 60.0));

        // Determine test merit level (how "meritorious" the raw test is)
        // High merit = test performance clearly differentiates ability
        // Low merit = test performance alone is not very informative
        if (testComponent >= 0.75) {
            meritLevel = "High";
            iqWeight = 0.30;  // test is clearly meritorious, lean on it more
        } else if (testComponent >= 0.55) {
            meritLevel = "Moderate";
            iqWeight = 0.40;  // balanced
        } else if (testComponent >= 0.35) {
            meritLevel = "Slightly Meritorious";
            iqWeight = 0.45;  // test isn't saying much, lean on IQ more
        } else {
            meritLevel = "Very Slightly Meritorious";
            iqWeight = 0.50;  // test is barely informative, IQ dominates
        }

        double testWeight = 1.0 - iqWeight;
        overallWeighted = (iqWeight * iqComponent) + (testWeight * testComponent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public accessors
    // ─────────────────────────────────────────────────────────────────────────

    public int getIQLow() { return iqLow; }
    public int getIQHigh() { return iqHigh; }
    public int getIQMidpoint() { return iqMidpoint; }
    public double getConfidence() { return confidence; }
    public boolean isPassing() { return isPassing; }
    public int getQuestionsAnswered() { return questionsAnswered; }

    public double getIQComponent() { return iqComponent; }
    public double getTestComponent() { return testComponent; }
    public double getOverallWeighted() { return overallWeighted; }
    public double getIQWeight() { return iqWeight; }
    public String getMeritLevel() { return meritLevel; }

    /**
     * Get display string for the live IQ range.
     * Tight when confident, wide when divergent.
     */
    public String getDisplayString() {
        int range = iqHigh - iqLow;
        if (range <= 8) {
            // Tight: show as "~midpoint (low–high)"
            return String.format("IQ ~%d (%d–%d)", iqMidpoint, iqLow, iqHigh);
        } else {
            // Wide: show full range
            return String.format("IQ %d–%d", iqLow, iqHigh);
        }
    }

    /**
     * Get pass/fail indicator with IQ.
     */
    public String getPassDisplay() {
        if (isPassing) {
            return String.format("PASS (%s)", getDisplayString());
        } else {
            return String.format("AT RISK (%s) — need %d+", getDisplayString(), PASSING_IQ);
        }
    }

    /**
     * Get the weighted score display.
     */
    public String getWeightedDisplay() {
        return String.format("Overall: %.1f%% [IQ×%.0f%% + Test×%.0f%%] (%s)",
                overallWeighted * 100, iqWeight * 100, (1 - iqWeight) * 100, meritLevel);
    }

    /**
     * File output for score persistence.
     */
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("live_iq_low=%d%n", iqLow));
        sb.append(String.format("live_iq_high=%d%n", iqHigh));
        sb.append(String.format("live_iq_midpoint=%d%n", iqMidpoint));
        sb.append(String.format("live_iq_confidence=%.4f%n", confidence));
        sb.append(String.format("live_iq_passing=%s%n", isPassing));
        sb.append(String.format("live_iq_credits=%d%n", iqCredits));
        sb.append(String.format("live_iq_questions=%d%n", questionsAnswered));
        sb.append(String.format("overall_weighted=%.4f%n", overallWeighted));
        sb.append(String.format("iq_weight=%.2f%n", iqWeight));
        sb.append(String.format("test_merit_level=%s%n", meritLevel));
        sb.append(String.format("iq_component=%.4f%n", iqComponent));
        sb.append(String.format("test_component=%.4f%n", testComponent));
        return sb.toString();
    }
}
