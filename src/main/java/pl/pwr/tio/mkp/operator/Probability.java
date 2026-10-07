package pl.pwr.tio.mkp.operator;

final class Probability {

    private Probability() {
    }

    static double check(final double value) {
        if (!(value >= 0 && value <= 1)) {
            throw new IllegalArgumentException("Probability must be in [0, 1]: " + value);
        }
        return value;
    }
}
