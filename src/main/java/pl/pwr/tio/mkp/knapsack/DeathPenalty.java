package pl.pwr.tio.mkp.knapsack;

/**
 * Death penalty (kara śmierci): every infeasible solution gets the same, worst fitness.
 * Used only for comparison with {@link LinearPenalty} in the experiments.
 */
public final class DeathPenalty implements Penalty {

    /**
     * Fitness of every infeasible solution, lower than the value of any feasible one.
     */
    private static final double REJECTED = -1;

    @Override
    public double apply(final double value, final double violation) {
        final double fitness;
        if (violation > 0) {
            fitness = DeathPenalty.REJECTED;
        } else {
            fitness = value;
        }
        return fitness;
    }
}
