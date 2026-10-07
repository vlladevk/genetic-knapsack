package pl.pwr.tio.mkp.knapsack;

/**
 * Linear penalty (liniowa funkcja kary): {@code fitness = value - rho * violation}.
 *
 * <p>The coefficient rho (współczynnik kary) works as an exchange rate between
 * an excess unit of weight and lost value. A solution that is only slightly too heavy
 * keeps a reasonable fitness, so selection can still tell "almost fits" from
 * "far too heavy".</p>
 */
public final class LinearPenalty implements Penalty {

    /**
     * Coefficient used when no item has a positive weight.
     */
    private static final double DEFAULT_COEFFICIENT = 1.0;

    /**
     * Penalty per unit of violation (rho).
     */
    private final double coefficient;

    /**
     * Ctor.
     *
     * @param coefficient Penalty per unit of violation (rho), positive
     */
    public LinearPenalty(final double coefficient) {
        if (!(coefficient > 0)) {
            throw new IllegalArgumentException("Coefficient must be positive: " + coefficient);
        }
        this.coefficient = coefficient;
    }

    /**
     * Penalty with rho equal to the largest value per unit of weight,
     * {@code max over items j and constraints i of value(j) / weight(i, j)}.
     * One excess unit of weight can not bring more value than rho,
     * so this penalty makes overloading unprofitable in most cases.
     *
     * @param instance Instance
     * @return Penalty adjusted to the instance
     */
    public static LinearPenalty of(final KnapsackInstance instance) {
        double rho = 0;
        for (final Item item : instance.items()) {
            for (int constraint = 0; constraint < item.dimensions(); ++constraint) {
                final double weight = item.weight(constraint);
                if (weight > 0) {
                    rho = Math.max(rho, item.value() / weight);
                }
            }
        }
        if (rho == 0) {
            rho = LinearPenalty.DEFAULT_COEFFICIENT;
        }
        return new LinearPenalty(rho);
    }

    /**
     * Penalty per unit of violation.
     *
     * @return Coefficient rho
     */
    public double coefficient() {
        return this.coefficient;
    }

    @Override
    public double apply(final double value, final double violation) {
        return value - this.coefficient * violation;
    }
}
