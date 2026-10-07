package pl.pwr.tio.mkp.knapsack;

// Death penalty (kara śmierci): every infeasible solution gets the same worst fitness.
// Only for comparison with LinearPenalty.
public final class DeathPenalty implements Penalty {

    // below the value of any feasible solution
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
