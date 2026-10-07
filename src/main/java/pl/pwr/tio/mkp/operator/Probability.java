package pl.pwr.tio.mkp.operator;

/**
 * Validation of probabilities given to operators.
 */
final class Probability {

    /**
     * Utility class.
     */
    private Probability() {
    }

    /**
     * Checks that a number is a probability.
     *
     * @param value Number to check
     * @return The same number
     */
    static double check(final double value) {
        if (!(value >= 0 && value <= 1)) {
            throw new IllegalArgumentException("Probability must be in [0, 1]: " + value);
        }
        return value;
    }
}
