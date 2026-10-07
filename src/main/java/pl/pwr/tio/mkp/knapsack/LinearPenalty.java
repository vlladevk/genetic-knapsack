package pl.pwr.tio.mkp.knapsack;

// Linear penalty (liniowa funkcja kary):
//   fitness = value - rho * violation
// rho (ρ, współczynnik kary) = how much fitness one unit of overload costs.
// The more overload, the lower the fitness, so selection still sees the difference
// between "almost fits" and "far too heavy" (death penalty gives both -1).
//
// Toy example, rho = 5 (checked in LinearPenaltyTest):
//   110100  value 23, violation 0          -> fitness 23 (optimum)
//   111100  value 27, violation 1 + 2 = 3  -> fitness 27 - 5 * 3 = 12
// With rho = 1 the second one would get 24 and beat the optimum.
public final class LinearPenalty implements Penalty {

    // when no item has a positive weight
    private static final double DEFAULT_COEFFICIENT = 1.0;

    // rho
    private final double coefficient;

    public LinearPenalty(final double coefficient) {
        if (!(coefficient > 0)) {
            throw new IllegalArgumentException("Coefficient must be positive: " + coefficient);
        }
        this.coefficient = coefficient;
    }

    // rho = the largest value per one unit of weight, over all items and constraints:
    //   rho = max value(j) / weight(i, j)
    // Toy: torch gives 5 value per 1 kg (and per 1 l), more than any other item -> rho = 5.
    // Why: one extra unit of space can hold at most rho value, so a fine of rho per unit
    // makes overloading not pay off (like a luggage fee that is higher than what fits
    // into one extra kg). Michalewicz uses the same rho for the 0/1 knapsack
    // ("Genetic Algorithms + Data Structures = Evolution Programs"); here the max is
    // also taken over all m constraints.
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
