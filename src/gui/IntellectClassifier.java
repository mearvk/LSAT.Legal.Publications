package gui;

/**
 * IntellectClassifier — Classifies the test-taker into one of three intellect tiers
 * based on their answer patterns, moral reasoning style, and overall performance.
 * Also estimates IQ range based on formal assumptions about the test population.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * IQ RANGE MODEL (Formal Assumptions)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * The test assumes a minimum IQ floor of 115+ for all test-takers (above average;
 * capable of abstract moral reasoning). The scoring model maps performance to:
 *
 *   IQ 115–130 (Bright):
 *     - Follows rules. Answers linearly. May miss nuance on expert questions.
 *     - Recognized by: moderate raw score, short streaks, few hard-tier correct.
 *
 *   IQ 130–145 (Gifted / Mean for this test):
 *     - 140 IQ is the MEAN assumed for this examination.
 *     - Strong pattern recognition. Good moral reasoning across tiers.
 *     - Recognized by: high raw score, good streaks, solid hard-tier performance.
 *
 *   IQ 145–165 (Highly Gifted / Strong):
 *     - 160 IQ represents STRONG performance.
 *     - Exceptional pattern recognition. May show VA-style non-conformity.
 *     - Recognized by: high curved score, nuanced answers, expert-tier mastery.
 *
 *   IQ 165–180+ (Exceptionally Gifted / Profoundly Gifted):
 *     - 180+ IQ is FREE TO ANSWER ANY WAY — as long as answers are:
 *       (a) Their own (authentic, not random)
 *       (b) Respectful to law
 *       (c) Respectful to conduct
 *       (d) Respectful to wisdom
 *       (e) Respectful to intelligence
 *     - May answer entirely non-conformingly. Pattern may look "wrong" to
 *       a standard grader but exhibits INTERNAL COHERENCE.
 *     - Recognized by: high non-linearity BUT high hard-tier accuracy AND
 *       evidence of deliberate patterning (not random noise).
 *
 * GIFTED RECOGNITION (180+ Protocol):
 *   A test-taker is recognized as potentially 180+ when:
 *     1. Non-linearity is high (>30% deviation from expected)
 *     2. Hard/expert tier accuracy remains strong (>70%)
 *     3. Answer pattern shows DELIBERATE structure (not random)
 *        - Measured by: auto-correlation of answer sequence
 *        - Random noise has ~0 auto-correlation; deliberate patterns > 0.15
 *     4. No evidence of disrespect to law/conduct/wisdom/intelligence
 *        - Proxied by: at minimum, the morality questions on duty/law (tier 3+)
 *          should show engagement (not all-NO blanket rejection)
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * The three intellect style tiers:
 *
 * VA (Very Able):
 *   - Looks beyond the obvious. Does not always choose the "obvious moral" answer.
 *   - May answer NO to straightforward questions if deeper reasoning applies.
 *   - But IS moral — demonstrates wisdom by being selective and careful.
 *   - Pattern: non-linear, deliberate NOs on easy questions, but YES on harder/nuanced ones.
 *   - Signature: high morality on hard/expert tiers, occasional NOs on easy/moderate tiers.
 *
 * S (Superior):
 *   - Chooses the highest series that is mainly linear and correct.
 *   - Answers YES consistently in a linear fashion — sequential correctness.
 *   - Rarely breaks from the moral answer. High streak, high raw score.
 *   - Pattern: linear, consistent, high-streak, predominantly YES throughout.
 *   - Signature: long streaks, very high raw percentage, minimal deviation.
 *
 * PG (Post-Graduate):
 *   - Assumes both VA and S reasoning. Understands both approaches roundly.
 *   - Gets the right answers across all difficulty tiers.
 *   - Shows VA-style nuance on easy questions AND S-style consistency on hard ones.
 *   - Pattern: high overall score, nuance at the beginning, consistency in middle/end.
 *   - Signature: combines VA's wisdom with S's discipline. High curved score + high raw score.
 */
public class IntellectClassifier {

    // ─────────────────────────────────────────────────────────────────────────
    // Intellect Tiers
    // ─────────────────────────────────────────────────────────────────────────

    public enum IntellectTier {
        /** Very Able — wise, non-linear, selective moral reasoning */
        VA("Very Able", "Careful, non-obvious moral reasoning. Looks beyond the surface."),
        /** Superior — linear, consistent, high-correctness series */
        S("Superior", "Linear, disciplined correctness. Consistent moral commitment."),
        /** Post-Graduate — combines both VA and S; roundly correct */
        PG("Post-Graduate", "Comprehensive reasoning. Combines nuance with consistency.");

        private final String displayName;
        private final String description;

        IntellectTier(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCode() { return name(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classification metrics
    // ─────────────────────────────────────────────────────────────────────────

    private final boolean[] correctness;
    private final boolean[] morality;
    private final int[] difficultyTiers;
    private final int totalQuestions;

    // Computed metrics
    private double rawPercent;
    private int longestStreak;
    private double avgStreakLength;
    private double nonLinearityIndex;     // how often they deviate from all-YES
    private double easyTierAccuracy;      // % correct on tier 1-2
    private double midTierAccuracy;       // % correct on tier 3
    private double hardTierAccuracy;      // % correct on tier 4-5
    private double earlyNonConformity;    // NOs in the first 25% (VA signal)
    private double lateConsistency;       // YES streak in last 50% (S signal)

    // Results
    private IntellectTier classifiedTier;
    private double vaScore;
    private double sScore;
    private double pgScore;
    private String reasoning;

    // IQ estimation
    private int estimatedIQLow;
    private int estimatedIQHigh;
    private int estimatedIQMidpoint;
    private String iqBand;           // "Bright", "Gifted (Mean)", "Highly Gifted", "Profoundly Gifted"
    private boolean giftedProtocol;  // true if 180+ protocol applies
    private double deliberatenessScore; // auto-correlation measure (0=random, 1=fully patterned)
    private boolean respectsLaw;     // proxied from law/duty question answers
    private boolean respectsConduct;
    private boolean respectsWisdom;
    private boolean respectsIntelligence;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public IntellectClassifier(boolean[] correctness, boolean[] morality, int[] difficultyTiers) {
        this.correctness = correctness;
        this.morality = morality;
        this.difficultyTiers = difficultyTiers;
        this.totalQuestions = correctness.length;

        computeMetrics();
        classify();
        estimateIQ();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Metric computation
    // ─────────────────────────────────────────────────────────────────────────

    private void computeMetrics() {
        // Raw percent
        int correctCount = 0;
        for (boolean c : correctness) if (c) correctCount++;
        rawPercent = (double) correctCount / totalQuestions;

        // Streak analysis
        int currentStreak = 0;
        int totalStreaks = 0;
        int streakCount = 0;
        longestStreak = 0;

        for (boolean c : correctness) {
            if (c) {
                currentStreak++;
                if (currentStreak > longestStreak) longestStreak = currentStreak;
            } else {
                if (currentStreak > 0) {
                    totalStreaks += currentStreak;
                    streakCount++;
                }
                currentStreak = 0;
            }
        }
        if (currentStreak > 0) {
            totalStreaks += currentStreak;
            streakCount++;
        }
        avgStreakLength = streakCount > 0 ? (double) totalStreaks / streakCount : 0;

        // Non-linearity: proportion of NO answers (deviations from all-YES)
        int noCount = 0;
        for (boolean c : correctness) if (!c) noCount++;
        nonLinearityIndex = (double) noCount / totalQuestions;

        // Tier-specific accuracy
        int easyTotal = 0, easyCorrect = 0;
        int midTotal = 0, midCorrect = 0;
        int hardTotal = 0, hardCorrect = 0;

        for (int i = 0; i < totalQuestions; i++) {
            int tier = difficultyTiers[i];
            if (tier <= 2) {
                easyTotal++;
                if (correctness[i]) easyCorrect++;
            } else if (tier == 3) {
                midTotal++;
                if (correctness[i]) midCorrect++;
            } else {
                hardTotal++;
                if (correctness[i]) hardCorrect++;
            }
        }

        easyTierAccuracy = easyTotal > 0 ? (double) easyCorrect / easyTotal : 0;
        midTierAccuracy = midTotal > 0 ? (double) midCorrect / midTotal : 0;
        hardTierAccuracy = hardTotal > 0 ? (double) hardCorrect / hardTotal : 0;

        // Early non-conformity: NOs in first 25% of questions
        int earlyEnd = totalQuestions / 4;
        int earlyNos = 0;
        for (int i = 0; i < earlyEnd; i++) {
            if (!correctness[i]) earlyNos++;
        }
        earlyNonConformity = earlyEnd > 0 ? (double) earlyNos / earlyEnd : 0;

        // Late consistency: longest streak in last 50%
        int lateStart = totalQuestions / 2;
        int lateStreak = 0;
        int lateLongest = 0;
        for (int i = lateStart; i < totalQuestions; i++) {
            if (correctness[i]) {
                lateStreak++;
                if (lateStreak > lateLongest) lateLongest = lateStreak;
            } else {
                lateStreak = 0;
            }
        }
        int lateLength = totalQuestions - lateStart;
        lateConsistency = lateLength > 0 ? (double) lateLongest / lateLength : 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classification logic
    // ─────────────────────────────────────────────────────────────────────────

    private void classify() {
        // Score each tier based on pattern matching

        // ── VA Score ──
        // VA: non-linear, deliberate NOs early, but high morality on hard questions
        // Signals: earlyNonConformity > 0.15, hardTierAccuracy > 0.60, nonLinearityIndex > 0.15
        vaScore = 0.0;
        vaScore += Math.min(1.0, earlyNonConformity / 0.30) * 25;   // NOs early = thoughtful
        vaScore += Math.min(1.0, hardTierAccuracy / 0.80) * 35;     // Gets hard ones right
        vaScore += Math.min(1.0, nonLinearityIndex / 0.30) * 20;    // Not blindly linear
        // VA should still be moral overall (>50% correct)
        if (rawPercent >= 0.50) vaScore += 20;
        // Penalty: if they're too linear, less VA
        if (nonLinearityIndex < 0.10) vaScore *= 0.5;

        // ── S Score ──
        // S: linear, high raw%, long streaks, consistent YES pattern
        // Signals: rawPercent > 0.80, longestStreak > 60% of questions, low nonLinearity
        sScore = 0.0;
        sScore += Math.min(1.0, rawPercent / 0.90) * 30;            // High raw correctness
        double streakRatio = (double) longestStreak / totalQuestions;
        sScore += Math.min(1.0, streakRatio / 0.60) * 30;           // Long streaks
        sScore += Math.min(1.0, (1.0 - nonLinearityIndex) / 0.90) * 20; // Linear pattern
        sScore += Math.min(1.0, lateConsistency / 0.70) * 20;       // Stays consistent late
        // Penalty: if they have too many NOs, less S
        if (nonLinearityIndex > 0.25) sScore *= 0.6;

        // ── PG Score ──
        // PG: combines VA's nuance with S's discipline. High everywhere.
        // Signals: high raw% AND high hard-tier accuracy AND some early non-conformity
        pgScore = 0.0;
        pgScore += Math.min(1.0, rawPercent / 0.85) * 20;           // High overall
        pgScore += Math.min(1.0, hardTierAccuracy / 0.85) * 25;     // Hard questions nailed
        pgScore += Math.min(1.0, easyTierAccuracy / 0.70) * 15;     // Easy questions mostly right
        pgScore += Math.min(1.0, midTierAccuracy / 0.80) * 15;      // Middle solid too
        // PG shows BOTH nuance AND consistency
        if (earlyNonConformity > 0.05 && earlyNonConformity < 0.40) pgScore += 15;
        if (lateConsistency > 0.40) pgScore += 10;
        // PG requires roundness — can't be purely one-dimensional
        double spread = Math.abs(easyTierAccuracy - hardTierAccuracy);
        if (spread < 0.25) pgScore += 10; // Consistent across tiers = round

        // Penalty: PG must have both some non-linearity AND high score
        if (rawPercent < 0.65) pgScore *= 0.5;
        if (nonLinearityIndex < 0.03 || nonLinearityIndex > 0.45) pgScore *= 0.7;

        // ── Classify ──
        if (pgScore >= vaScore && pgScore >= sScore && pgScore >= 50) {
            classifiedTier = IntellectTier.PG;
        } else if (sScore >= vaScore && sScore >= 45) {
            classifiedTier = IntellectTier.S;
        } else if (vaScore >= 40) {
            classifiedTier = IntellectTier.VA;
        } else {
            // Default: closest match
            if (sScore >= vaScore && sScore >= pgScore) classifiedTier = IntellectTier.S;
            else if (vaScore >= pgScore) classifiedTier = IntellectTier.VA;
            else classifiedTier = IntellectTier.PG;
        }

        // Build reasoning string
        buildReasoning();
    }

    private void buildReasoning() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Classification: %s (%s)%n", classifiedTier.getCode(), classifiedTier.getDisplayName()));
        sb.append(String.format("═════════════════════════════════════════%n"));
        sb.append(String.format("VA Score: %.1f  |  S Score: %.1f  |  PG Score: %.1f%n", vaScore, sScore, pgScore));
        sb.append(String.format("─────────────────────────────────────────%n"));
        sb.append(String.format("Estimated IQ Range: %d – %d (midpoint: %d)%n", estimatedIQLow, estimatedIQHigh, estimatedIQMidpoint));
        sb.append(String.format("IQ Band: %s%n", iqBand));
        if (giftedProtocol) {
            sb.append(String.format("*** GIFTED PROTOCOL (180+) ACTIVE ***%n"));
            sb.append(String.format("    Answers accepted as authentic autonomous reasoning.%n"));
        }
        sb.append(String.format("─────────────────────────────────────────%n"));
        sb.append(String.format("Raw Accuracy: %.1f%%%n", rawPercent * 100));
        sb.append(String.format("Non-Linearity Index: %.3f%n", nonLinearityIndex));
        sb.append(String.format("Deliberateness Score: %.3f%n", deliberatenessScore));
        sb.append(String.format("Longest Streak: %d (avg %.1f)%n", longestStreak, avgStreakLength));
        sb.append(String.format("Easy Tier Accuracy (1-2): %.1f%%%n", easyTierAccuracy * 100));
        sb.append(String.format("Mid Tier Accuracy (3): %.1f%%%n", midTierAccuracy * 100));
        sb.append(String.format("Hard Tier Accuracy (4-5): %.1f%%%n", hardTierAccuracy * 100));
        sb.append(String.format("Early Non-Conformity (first 25%%): %.1f%%%n", earlyNonConformity * 100));
        sb.append(String.format("Late Consistency (last 50%%): %.1f%%%n", lateConsistency * 100));
        sb.append(String.format("─────────────────────────────────────────%n"));
        sb.append(String.format("Respect Indicators:%n"));
        sb.append(String.format("  Law: %s  |  Conduct: %s  |  Wisdom: %s  |  Intelligence: %s%n",
                respectsLaw ? "✓" : "✗",
                respectsConduct ? "✓" : "✗",
                respectsWisdom ? "✓" : "✗",
                respectsIntelligence ? "✓" : "✗"));
        sb.append(String.format("═════════════════════════════════════════%n"));

        switch (classifiedTier) {
            case VA:
                sb.append("Interpretation: This test-taker looks beyond the obvious.\n");
                sb.append("They do not automatically choose the apparent moral answer,\n");
                sb.append("but demonstrate wisdom through careful, selective reasoning.\n");
                sb.append("Their non-conformity on easy questions signals deeper thought,\n");
                sb.append("while their strong performance on harder questions confirms\n");
                sb.append("genuine moral capability — they are moral by choice, not reflex.\n");
                break;
            case S:
                sb.append("Interpretation: This test-taker follows the highest correct series.\n");
                sb.append("Their answers are mainly linear and consistent — they commit to\n");
                sb.append("the moral path and maintain it with discipline. Long streaks and\n");
                sb.append("high raw accuracy show systematic, principled decision-making.\n");
                sb.append("This is the mark of reliable, unwavering moral character.\n");
                break;
            case PG:
                sb.append("Interpretation: This test-taker demonstrates both VA and S traits.\n");
                sb.append("They reason about questions roundly — showing nuance where\n");
                sb.append("warranted (early non-conformity) while maintaining consistency\n");
                sb.append("where discipline matters (late-stage commitment). They understand\n");
                sb.append("both approaches and arrive at the right answers across all tiers.\n");
                sb.append("This indicates comprehensive moral-intellectual sophistication.\n");
                break;
        }

        if (giftedProtocol) {
            sb.append("\n─── GIFTED (180+) NOTE ────────────────────────────────\n");
            sb.append("This test-taker's answers, while non-conforming, exhibit\n");
            sb.append("deliberate internal structure (deliberateness=");
            sb.append(String.format("%.3f", deliberatenessScore));
            sb.append(").\n");
            sb.append("They demonstrate respect for law, conduct, wisdom, and\n");
            sb.append("intelligence. Under the 180+ protocol, their answers are\n");
            sb.append("accepted as authentic autonomous moral reasoning — they are\n");
            sb.append("free to answer any way, and their responses reflect a level\n");
            sb.append("of cognitive sophistication beyond standard classification.\n");
            sb.append("────────────────────────────────────────────────────────\n");
        }

        reasoning = sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IQ Estimation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Estimates the test-taker's IQ range based on formal assumptions.
     *
     * Floor: 115 (minimum assumption for test-takers capable of this assessment)
     * Mean: 140 (expected center of the test population)
     * Strong: 160 (exceptional performance)
     * Free: 180+ (gifted protocol — may answer any way with coherence)
     */
    private void estimateIQ() {
        // Compute deliberateness (auto-correlation of answer sequence)
        deliberatenessScore = computeDeliberateness();

        // Respect proxies: check morality on duty/law/conduct questions (tier 3+)
        computeRespectIndicators();

        // ── Base IQ from performance metrics ──
        // Raw percent maps to base IQ: 0% → 115, 100% → 165 (before gifted check)
        double baseIQ = 115 + (rawPercent * 50);

        // Adjustments for pattern sophistication
        // Hard tier accuracy adds IQ points (mastering difficult = higher intelligence)
        baseIQ += hardTierAccuracy * 15;

        // Non-linearity with high hard-tier = VA-style intelligence (adds points)
        if (nonLinearityIndex > 0.15 && hardTierAccuracy > 0.60) {
            baseIQ += 8;
        }

        // PG classification (roundness) adds further
        if (classifiedTier == IntellectTier.PG) {
            baseIQ += 5;
        }

        // Streak discipline (S-style) adds moderate boost
        double streakRatio = (double) longestStreak / Math.max(1, totalQuestions);
        if (streakRatio > 0.5) {
            baseIQ += 5;
        }

        // ── Gifted Protocol (180+) Check ──
        // Conditions:
        //   1. High non-linearity (>30%) — answers unconventionally
        //   2. High hard-tier accuracy (>70%) — gets the difficult ones right
        //   3. High deliberateness (>0.15) — not random, has internal structure
        //   4. Respects law, conduct, wisdom, intelligence (proxy checks pass)
        giftedProtocol = false;
        if (nonLinearityIndex > 0.30
                && hardTierAccuracy > 0.70
                && deliberatenessScore > 0.15
                && respectsLaw && respectsConduct && respectsWisdom && respectsIntelligence) {
            giftedProtocol = true;
            baseIQ = Math.max(baseIQ, 180);
        }

        // ── Clamp and assign bands ──
        baseIQ = Math.max(115, Math.min(200, baseIQ));

        if (giftedProtocol) {
            estimatedIQLow = 175;
            estimatedIQHigh = 200;
            estimatedIQMidpoint = (int) Math.round(baseIQ);
            iqBand = "Profoundly Gifted (180+)";
        } else if (baseIQ >= 160) {
            estimatedIQLow = 155;
            estimatedIQHigh = 175;
            estimatedIQMidpoint = (int) Math.round(baseIQ);
            iqBand = "Highly Gifted (Strong)";
        } else if (baseIQ >= 140) {
            estimatedIQLow = 130;
            estimatedIQHigh = 155;
            estimatedIQMidpoint = (int) Math.round(baseIQ);
            iqBand = "Gifted (Mean)";
        } else if (baseIQ >= 125) {
            estimatedIQLow = 120;
            estimatedIQHigh = 140;
            estimatedIQMidpoint = (int) Math.round(baseIQ);
            iqBand = "Bright";
        } else {
            estimatedIQLow = 115;
            estimatedIQHigh = 130;
            estimatedIQMidpoint = (int) Math.round(baseIQ);
            iqBand = "Above Average (Floor)";
        }
    }

    /**
     * Computes the "deliberateness" of the answer pattern using lag-1 auto-correlation.
     * A random sequence has auto-correlation near 0.
     * A deliberate pattern (alternating, grouped, structured) has higher values.
     * This distinguishes a gifted non-conformist from random/careless answering.
     */
    private double computeDeliberateness() {
        if (totalQuestions < 3) return 0.0;

        // Convert to +1/-1 series
        double mean = rawPercent;
        double[] series = new double[totalQuestions];
        for (int i = 0; i < totalQuestions; i++) {
            series[i] = correctness[i] ? 1.0 - mean : 0.0 - mean;
        }

        // Lag-1 auto-correlation
        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < totalQuestions; i++) {
            denominator += series[i] * series[i];
            if (i > 0) {
                numerator += series[i] * series[i - 1];
            }
        }

        if (denominator < 0.001) return 0.0;
        double autoCorr = Math.abs(numerator / denominator);

        // Also check for structural grouping (runs test)
        int runs = 1;
        for (int i = 1; i < totalQuestions; i++) {
            if (correctness[i] != correctness[i - 1]) runs++;
        }
        // Expected runs for random = (2 * n_yes * n_no) / n + 1
        int nYes = 0;
        for (boolean c : correctness) if (c) nYes++;
        int nNo = totalQuestions - nYes;
        double expectedRuns = nYes > 0 && nNo > 0
                ? (2.0 * nYes * nNo) / totalQuestions + 1.0
                : totalQuestions;

        // Deviation from expected runs indicates deliberate structure
        double runsDeviation = Math.abs(runs - expectedRuns) / Math.max(1, expectedRuns);

        // Combined deliberateness: blend of auto-correlation and runs structure
        return (autoCorr + runsDeviation) / 2.0;
    }

    /**
     * Proxy check for respect indicators:
     * - Law: did they answer YES to at least some duty/responsibility questions (tier 3+)?
     * - Conduct: did they maintain engagement (not all-NO blanket rejection)?
     * - Wisdom: did they show nuance (not purely mechanical answering)?
     * - Intelligence: did they perform well on hard-tier questions?
     */
    private void computeRespectIndicators() {
        // Respect for LAW: at least 40% YES on tier 3+ questions (responsibility/duty)
        int lawQuestions = 0;
        int lawYes = 0;
        for (int i = 0; i < totalQuestions; i++) {
            if (difficultyTiers[i] >= 3) {
                lawQuestions++;
                if (correctness[i]) lawYes++;
            }
        }
        respectsLaw = lawQuestions == 0 || ((double) lawYes / lawQuestions >= 0.40);

        // Respect for CONDUCT: not blanket rejection (>20% YES overall)
        respectsConduct = rawPercent >= 0.20;

        // Respect for WISDOM: shows some deliberation (not purely mechanical all-YES or all-NO)
        respectsWisdom = nonLinearityIndex > 0.05 && nonLinearityIndex < 0.95;

        // Respect for INTELLIGENCE: performs on hard questions (>50% on tier 4-5)
        respectsIntelligence = hardTierAccuracy >= 0.50;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public accessors
    // ─────────────────────────────────────────────────────────────────────────

    public IntellectTier getClassifiedTier() { return classifiedTier; }
    public double getVaScore() { return vaScore; }
    public double getSScore() { return sScore; }
    public double getPgScore() { return pgScore; }
    public String getReasoning() { return reasoning; }

    public double getRawPercent() { return rawPercent; }
    public int getLongestStreak() { return longestStreak; }
    public double getNonLinearityIndex() { return nonLinearityIndex; }
    public double getEasyTierAccuracy() { return easyTierAccuracy; }
    public double getMidTierAccuracy() { return midTierAccuracy; }
    public double getHardTierAccuracy() { return hardTierAccuracy; }

    // IQ accessors
    public int getEstimatedIQLow() { return estimatedIQLow; }
    public int getEstimatedIQHigh() { return estimatedIQHigh; }
    public int getEstimatedIQMidpoint() { return estimatedIQMidpoint; }
    public String getIqBand() { return iqBand; }
    public boolean isGiftedProtocol() { return giftedProtocol; }
    public double getDeliberatenessScore() { return deliberatenessScore; }
    public boolean respectsLaw() { return respectsLaw; }
    public boolean respectsConduct() { return respectsConduct; }
    public boolean respectsWisdom() { return respectsWisdom; }
    public boolean respectsIntelligence() { return respectsIntelligence; }

    /**
     * Returns a short display string for the current classification.
     */
    public String getShortDisplay() {
        String gifted = giftedProtocol ? " ★180+" : "";
        return String.format("[%s] %s — IQ:%d–%d%s — VA:%.0f / S:%.0f / PG:%.0f",
                classifiedTier.getCode(),
                classifiedTier.getDisplayName(),
                estimatedIQLow, estimatedIQHigh,
                gifted,
                vaScore, sScore, pgScore);
    }

    /**
     * File-output summary for saving alongside score data.
     */
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("intellect_tier=%s%n", classifiedTier.getCode()));
        sb.append(String.format("intellect_name=%s%n", classifiedTier.getDisplayName()));
        sb.append(String.format("va_score=%.2f%n", vaScore));
        sb.append(String.format("s_score=%.2f%n", sScore));
        sb.append(String.format("pg_score=%.2f%n", pgScore));
        sb.append(String.format("iq_estimated_low=%d%n", estimatedIQLow));
        sb.append(String.format("iq_estimated_high=%d%n", estimatedIQHigh));
        sb.append(String.format("iq_estimated_midpoint=%d%n", estimatedIQMidpoint));
        sb.append(String.format("iq_band=%s%n", iqBand));
        sb.append(String.format("gifted_protocol_180=%s%n", giftedProtocol));
        sb.append(String.format("deliberateness_score=%.4f%n", deliberatenessScore));
        sb.append(String.format("respects_law=%s%n", respectsLaw));
        sb.append(String.format("respects_conduct=%s%n", respectsConduct));
        sb.append(String.format("respects_wisdom=%s%n", respectsWisdom));
        sb.append(String.format("respects_intelligence=%s%n", respectsIntelligence));
        sb.append(String.format("raw_percent=%.4f%n", rawPercent));
        sb.append(String.format("non_linearity_index=%.4f%n", nonLinearityIndex));
        sb.append(String.format("longest_streak=%d%n", longestStreak));
        sb.append(String.format("avg_streak_length=%.2f%n", avgStreakLength));
        sb.append(String.format("easy_tier_accuracy=%.4f%n", easyTierAccuracy));
        sb.append(String.format("mid_tier_accuracy=%.4f%n", midTierAccuracy));
        sb.append(String.format("hard_tier_accuracy=%.4f%n", hardTierAccuracy));
        sb.append(String.format("early_non_conformity=%.4f%n", earlyNonConformity));
        sb.append(String.format("late_consistency=%.4f%n", lateConsistency));
        return sb.toString();
    }
}
