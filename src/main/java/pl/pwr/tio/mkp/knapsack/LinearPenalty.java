package pl.pwr.tio.mkp.knapsack;

// Linear penalty (liniowa funkcja kary): fitness = value - rho * violation.
// A slightly too heavy solution keeps a reasonable fitness, so selection can
// still tell "almost fits" from "far too heavy".
public final class LinearPenalty implements Penalty {

    // when no item has a positive weight
    private static final double DEFAULT_COEFFICIENT = 1.0;

    // rho, penalty coefficient (współczynnik kary)
    private final double coefficient;

    public LinearPenalty(final double coefficient) {
        if (!(coefficient > 0)) {
            throw new IllegalArgumentException("Coefficient must be positive: " + coefficient);
        }
        this.coefficient = coefficient;
    }

    // rho = max value(j) / weight(i, j): one unit of overload can not bring more
    // value than rho, so overloading does not pay off.
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

    public double coefficient() {
        return this.coefficient;
    }

    @Override
    public double apply(final double value, final double violation) {
        return value - this.coefficient * violation;
    }
}
