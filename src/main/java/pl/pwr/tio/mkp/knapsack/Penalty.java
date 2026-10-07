package pl.pwr.tio.mkp.knapsack;

/**
 * Penalty function (funkcja kary): turns the value of a solution and its constraint
 * violation into fitness. Constraints are handled here, inside the fitness function,
 * so the algorithm needs no extra operator (for example repair).
 */
public interface Penalty {

    /**
     * Fitness of a solution.
     *
     * @param value Total value of the chosen items
     * @param violation Total constraint violation, {@code 0} when feasible
     * @return Fitness, equal to {@code value} for feasible solutions
     */
    double apply(double value, double violation);
}
