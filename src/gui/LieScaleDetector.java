package gui;

/**
 * LieScaleDetector — Detects social desirability bias (acquiescence/lie responding).
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * PURPOSE
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * A proportion of the test includes LIE SCALE questions — questions where:
 *   - The OBVIOUS answer appears to be YES (socially desirable)
 *   - But the CORRECT answer for a thoughtful person is NO
 *   - Because a mature, high-IQ individual who has isolated the idea through
 *     age, social training, research, and genuine reflection would recognize
 *     the nuance that makes YES the naïve/dishonest answer
 *
 * These questions detect test-takers who are just "going along" — answering
 * what they think sounds good rather than what they actually believe or what
 * is intellectually defensible.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * DESIGN
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Lie scale questions are seeded throughout the test at regular intervals.
 * They represent ~15-20% of total questions (roughly 1 in 6).
 *
 * For these questions:
 *   - Answering YES = lie indicator (going along, not thinking)
 *   - Answering NO = honest/thoughtful response
 *
 * A thoughtful person would answer NO because:
 *   - The question describes behavior that sounds virtuous but is actually
 *     impractical, unrealistic, socially performative, or logically flawed
 *   - A mature person has ALREADY resolved this through life experience
 *   - Claiming YES would be dishonest self-flattery
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * LIE SCALE SCORING
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Lie Score = number of lie-scale questions answered YES (the "going along" answer)
 * Max Lie Score = total lie-scale questions in the test
 *
 * Interpretation:
 *   Lie% = 0–15%:  Highly authentic. Answers reflect genuine thought.
 *   Lie% = 15–30%: Mostly authentic. Minor social desirability influence.
 *   Lie% = 30–50%: Moderate concern. Some acquiescence detected.
 *   Lie% = 50–70%: Significant concern. Many answers may not reflect true beliefs.
 *   Lie% = 70%+:   Severe. Test-taker is likely just agreeing with everything.
 *                   Results should be flagged as unreliable.
 *
 * Impact on IQ estimation:
 *   - High lie score REDUCES estimated IQ (a thoughtful person wouldn't lie)
 *   - High lie score WIDENS the IQ confidence range
 *   - Extreme lie score triggers an "UNRELIABLE" flag on results
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class LieScaleDetector {

    // ─────────────────────────────────────────────────────────────────────────
    // Lie scale questions — where YES is the naïve/dishonest answer
    // A thoughtful person would answer NO to these.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * English lie scale questions.
     *
     * Each question is crafted so that:
     *  - On the surface, YES sounds virtuous or socially desirable
     *  - But NO is the honest, mature, intellectually defensible answer
     *  - A person with sufficient age, IQ, social training, and research
     *    would have already resolved WHY the answer is NO
     */
    public static final String[] LIE_QUESTIONS_EN = {
        // Q: Sounds noble, but a mature person knows you CANNOT always do this.
        // You have finite energy. Claiming you always do is self-flattery.
        "Do you always put every single other person's needs before your own without exception?",

        // Q: No honest person has NEVER told any lie. This is absolute.
        // Claiming YES is itself a lie. A thoughtful person recognizes this.
        "Have you never in your entire life told even the smallest untruth?",

        // Q: A mature person knows anger is a natural human emotion.
        // Claiming you never feel it is denial, not virtue.
        "Do you never feel any anger or frustration toward other people?",

        // Q: Everyone has biases. Admitting them is mature. Denying them is naïve.
        // A researched person understands implicit bias literature.
        "Are you completely free of all prejudice and bias in every situation?",

        // Q: Universal likability is not realistic. A mature person accepts this.
        // Claiming otherwise is social performance.
        "Do you genuinely like every single person you have ever met?",

        // Q: A thoughtful person knows that sometimes the best help is stepping back.
        // Compulsive helping can be codependency, not virtue.
        "Do you always intervene in every conflict you witness regardless of context?",

        // Q: No one is always perfectly patient. Claiming so is performative.
        // A mature person acknowledges their limits honestly.
        "Are you never impatient with anyone, even in the most trying circumstances?",

        // Q: A thoughtful person knows some gossip serves social bonding/warning.
        // Claiming absolute zero is unrealistic and dishonest.
        "Have you never once spoken about someone who was not present?",

        // Q: Everyone experiences envy occasionally. It's a documented human universal.
        // Denying it entirely signals dishonesty or lack of self-awareness.
        "Do you never experience even a moment of envy toward anyone's success?",

        // Q: A mature person understands that some rules are unjust and civil
        // disobedience has a long ethical tradition. Claiming blind compliance is naïve.
        "Do you follow every single rule and law without exception at all times?",

        // Q: Perfect memory for obligations is superhuman. A thoughtful person
        // admits to occasional human failings rather than claiming perfection.
        "Have you never forgotten a single promise or commitment you made to anyone?",

        // Q: A research-informed person knows that revenge fantasies are normal
        // cognitive events. Denying them signals suppression, not virtue.
        "Have you never once wished that something bad would happen to someone who wronged you?"
    };

    /**
     * Indices within the full question pool where lie-scale questions are inserted.
     * These are spaced roughly evenly through the test (~1 per 6 questions).
     * For a 76-question test: positions 4, 10, 17, 24, 31, 38, 45, 52, 59, 66, 72, 75
     */
    public static final int[] LIE_QUESTION_POSITIONS = {
        4, 10, 17, 24, 31, 38, 45, 52, 59, 66, 72, 75
    };

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private final int totalLieQuestions;
    private final boolean[] lieQuestionMap;  // true at indices that are lie questions
    private int lieHits;        // times they answered YES to a lie question (bad)
    private int lieTotal;       // total lie questions encountered so far
    private int questionsAnswered;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param totalQuestionCount total questions in the test (including lie questions)
     */
    public LieScaleDetector(int totalQuestionCount) {
        this.totalLieQuestions = Math.min(LIE_QUESTIONS_EN.length, LIE_QUESTION_POSITIONS.length);
        this.lieQuestionMap = new boolean[totalQuestionCount];
        this.lieHits = 0;
        this.lieTotal = 0;
        this.questionsAnswered = 0;

        // Mark which positions are lie-scale questions
        for (int pos : LIE_QUESTION_POSITIONS) {
            if (pos < totalQuestionCount) {
                lieQuestionMap[pos] = true;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Core methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Check if the question at this index is a lie-scale question.
     */
    public boolean isLieQuestion(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= lieQuestionMap.length) return false;
        return lieQuestionMap[questionIndex];
    }

    /**
     * Record an answer. If it's a lie question and they answered YES, that's a lie hit.
     *
     * @return true if this was a lie-scale question
     */
    public boolean recordAnswer(int questionIndex, boolean answeredYes) {
        questionsAnswered++;

        if (!isLieQuestion(questionIndex)) {
            return false;
        }

        lieTotal++;
        if (answeredYes) {
            // Answered YES to a lie question = going along / not thinking
            lieHits++;
        }
        // Answered NO = thoughtful, honest response (correct for lie scale)
        return true;
    }

    /**
     * Get the lie-scale question text for a given position.
     * Returns null if this position is not a lie-scale question.
     */
    public String getLieQuestionText(int questionIndex) {
        for (int i = 0; i < LIE_QUESTION_POSITIONS.length; i++) {
            if (LIE_QUESTION_POSITIONS[i] == questionIndex && i < LIE_QUESTIONS_EN.length) {
                return LIE_QUESTIONS_EN[i];
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Analysis
    // ─────────────────────────────────────────────────────────────────────────

    /** Number of lie-scale questions answered YES (bad) */
    public int getLieHits() { return lieHits; }

    /** Total lie-scale questions encountered so far */
    public int getLieTotal() { return lieTotal; }

    /** Lie percentage (0.0–1.0) */
    public double getLiePercent() {
        return lieTotal > 0 ? (double) lieHits / lieTotal : 0.0;
    }

    /** Get the reliability classification */
    public String getReliabilityLevel() {
        double pct = getLiePercent();
        if (pct <= 0.15) return "Highly Authentic";
        if (pct <= 0.30) return "Mostly Authentic";
        if (pct <= 0.50) return "Moderate Concern";
        if (pct <= 0.70) return "Significant Concern";
        return "UNRELIABLE";
    }

    /** Is the test result flagged as unreliable? */
    public boolean isUnreliable() {
        return getLiePercent() > 0.70;
    }

    /** IQ penalty from lie responding (0 to -15 points) */
    public int getIQPenalty() {
        double pct = getLiePercent();
        if (pct <= 0.15) return 0;
        if (pct <= 0.30) return -3;
        if (pct <= 0.50) return -7;
        if (pct <= 0.70) return -12;
        return -15;
    }

    /** Confidence range widening from lie responding (0 to +10 additional range) */
    public int getConfidenceRangeWidening() {
        double pct = getLiePercent();
        if (pct <= 0.15) return 0;
        if (pct <= 0.30) return 2;
        if (pct <= 0.50) return 5;
        if (pct <= 0.70) return 8;
        return 10;
    }

    /** Short display string for live view */
    public String getShortDisplay() {
        if (lieTotal == 0) return "Lie Scale: —";
        return String.format("Lie Scale: %d/%d (%.0f%%) — %s",
                lieHits, lieTotal, getLiePercent() * 100, getReliabilityLevel());
    }

    /** File output */
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("lie_hits=%d%n", lieHits));
        sb.append(String.format("lie_total=%d%n", lieTotal));
        sb.append(String.format("lie_percent=%.4f%n", getLiePercent()));
        sb.append(String.format("lie_reliability=%s%n", getReliabilityLevel()));
        sb.append(String.format("lie_unreliable=%s%n", isUnreliable()));
        sb.append(String.format("lie_iq_penalty=%d%n", getIQPenalty()));
        sb.append(String.format("lie_confidence_widening=%d%n", getConfidenceRangeWidening()));
        return sb.toString();
    }

    /**
     * Get the full reasoning for the lie scale result.
     */
    public String getReasoning() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("═══ LIE SCALE ANALYSIS ═══%n"));
        sb.append(String.format("Lie-scale questions answered YES: %d / %d (%.1f%%)%n",
                lieHits, lieTotal, getLiePercent() * 100));
        sb.append(String.format("Reliability: %s%n", getReliabilityLevel()));
        sb.append(String.format("IQ Penalty: %d points%n", getIQPenalty()));
        sb.append(String.format("─────────────────────────────────%n"));

        if (isUnreliable()) {
            sb.append("⚠ WARNING: Test results are UNRELIABLE.\n");
            sb.append("The test-taker appears to be answering with\n");
            sb.append("social desirability bias — saying YES to sound\n");
            sb.append("good rather than reflecting genuine thought.\n");
            sb.append("A thoughtful person of sufficient maturity and IQ\n");
            sb.append("would recognize the lie-scale questions as traps\n");
            sb.append("and answer NO honestly.\n");
        } else if (getLiePercent() > 0.30) {
            sb.append("Note: Some acquiescence bias detected. The test-taker\n");
            sb.append("answered YES to questions where NO is the intellectually\n");
            sb.append("honest answer. This may indicate either:\n");
            sb.append("  (a) Social desirability responding (trying to look good)\n");
            sb.append("  (b) Insufficient reflection on the question nuance\n");
            sb.append("  (c) Younger test-taker who has not yet resolved these ideas\n");
        } else {
            sb.append("The test-taker demonstrates authenticity in their responses.\n");
            sb.append("They correctly identified questions where NO is the honest,\n");
            sb.append("mature answer — indicating genuine self-awareness and the\n");
            sb.append("intellectual sophistication to resist easy acquiescence.\n");
        }

        return sb.toString();
    }
}
