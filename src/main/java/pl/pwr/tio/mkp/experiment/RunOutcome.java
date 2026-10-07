package pl.pwr.tio.mkp.experiment;

import java.util.OptionalDouble;

// One run (uruchomienie). best: best feasible value, empty if none;
// foundAt: its generation, -1 if none; feasibleShare: in the last generation.
public record RunOutcome(
    RunConfig config,
    long seed,
    OptionalDouble best,
    int foundAt,
    double feasibleShare
) {
}
