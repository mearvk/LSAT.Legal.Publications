package gui;

/**
 * MoralCurveFilter — A modulus-based scoring filter that applies a normal distribution
 * curve to raw quiz scores. The curve is defined by 10 control points spanning the
 * score range from 0% to 100%.
 *
 * The admin can adjust 10 control points at positions:
 *   Point 0: 0% (beginning)
 *   Point 1: ~11%
 *   Point 2: ~22%
 *   Point 3: ~33%
 *   Point 4: ~44%
 *   Point 5: ~55% (middle)
 *   Point 6: ~66%
 *   Point 7: ~77%
 *   Point 8: ~88%
 *   Point 9: 100% (end)
 *
 * Each control point has a weight (0.0 to 2.0) that scales the score contribution
 * for questions falling in that segment. Default weights follow a normal (Gaussian)
 * distribution centered at point 5 (the middle).
 *
 * The modulus operation determines which control point governs a given question:
 *   controlPointIndex = questionIndex % 10
 *
 * This means in a 20-question quiz, questions 0,10 use point 0; questions 1,11 use
 * point 1; etc. The weight at that control point determines the effective score
 * multiplier for a correct answer at that position.
 */
public class MoralCurveFilter {

    /** Number of control points on the moral curve */
    public static final int NUM_POINTS = 10;

    /** The 10 weight values — default is normal distribution */
    private final double[] weights;

    /** Labels for the 10 control points */
    public static final String[] POINT_LABELS = {
        "Beginning (0%)",
        "Point 2 (11%)",
        "Point 3 (22%)",
        "Point 4 (33%)",
        "Point 5 (44%)",
        "Middle (55%)",
        "Point 7 (66%)",
        "Point 8 (77%)",
        "Point 9 (88%)",
        "End (100%)"
    };

    /**
     * Creates a new MoralCurveFilter with default normal distribution weights.
     */
    public MoralCurveFilter() {
        this.weights = new double[NUM_POINTS];
        resetToNormalDistribution();
    }

    /**
     * Resets all 10 control points to a standard normal (Gaussian) distribution.
     * Mean = 4.5 (center of 0–9), sigma = 2.5
     * Values normalized so the peak is 1.0 and tails are ~0.1
     */
    public void resetToNormalDistribution() {
        double mean = 4.5;
        double sigma = 2.5;

        for (int i = 0; i < NUM_POINTS; i++) {
            double x = (double) i;
            double exponent = -0.5 * Math.pow((x - mean) / sigma, 2);
            weights[i] = Math.exp(exponent);
        }
        // The peak (at mean) will be 1.0 since exp(0) = 1.0
        // Tails at 0 and 9: exp(-0.5 * (4.5/2.5)^2) = exp(-1.62) ≈ 0.198
    }

    /**
     * Set the weight for a specific control point.
     * @param index 0–9
     * @param weight 0.0 to 2.0
     */
    public void setWeight(int index, double weight) {
        if (index < 0 || index >= NUM_POINTS) return;
        weights[index] = Math.max(0.0, Math.min(2.0, weight));
    }

    /**
     * Get the weight for a specific control point.
     * @param index 0–9
     * @return weight value
     */
    public double getWeight(int index) {
        if (index < 0 || index >= NUM_POINTS) return 0.0;
        return weights[index];
    }

    /**
     * Get a copy of all weights.
     */
    public double[] getWeights() {
        return weights.clone();
    }

    /**
     * Apply the modulus filter to a set of raw answers.
     * Each question's contribution is weighted by the control point at
     * (questionIndex % 10).
     *
     * @param answers boolean array where true = correct/yes answer
     * @return the curved score (weighted sum)
     */
    public double applyCurve(boolean[] answers) {
        double weightedScore = 0.0;
        double maxPossible = 0.0;

        for (int i = 0; i < answers.length; i++) {
            int controlPoint = i % NUM_POINTS;
            double w = weights[controlPoint];
            maxPossible += w;
            if (answers[i]) {
                weightedScore += w;
            }
        }

        return maxPossible > 0 ? weightedScore : 0.0;
    }

    /**
     * Get the maximum possible curved score for a quiz of the given length.
     */
    public double getMaxScore(int questionCount) {
        double max = 0.0;
        for (int i = 0; i < questionCount; i++) {
            max += weights[i % NUM_POINTS];
        }
        return max;
    }

    /**
     * Get the curved percentage for a set of answers.
     */
    public double getCurvedPercentage(boolean[] answers) {
        double max = getMaxScore(answers.length);
        if (max <= 0) return 0.0;
        return (applyCurve(answers) / max) * 100.0;
    }

    /**
     * Describe the current curve configuration as a formatted string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Moral Curve Filter [");
        for (int i = 0; i < NUM_POINTS; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.3f", weights[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
