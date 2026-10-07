package pl.pwr.tio.mkp.experiment;

import java.util.OptionalDouble;

/**
 * Outcome of one run (uruchomienie) of the genetic algorithm.
 *
 * @param config Configuration of the run
 * @param seed Seed of the run
 * @param best Value of the best feasible solution found in the whole run,
 *  empty when no feasible solution was found
 * @param foundAt Generation in which the best feasible solution was found, {@code -1} if none
 * @param feasibleShare Share of feasible individuals in the last generation
 */
public record RunOutcome(
    RunConfig config,
    long seed,
    OptionalDouble best,
    int foundAt,
    double feasibleShare
) {
}
