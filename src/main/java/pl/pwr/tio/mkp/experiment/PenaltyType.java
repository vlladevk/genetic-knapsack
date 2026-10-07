package pl.pwr.tio.mkp.experiment;

import pl.pwr.tio.mkp.knapsack.DeathPenalty;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.LinearPenalty;
import pl.pwr.tio.mkp.knapsack.Penalty;

// Penalty functions compared in the experiments (series D).
public enum PenaltyType {
    LINEAR,
    DEATH;

    // LinearPenalty needs the instance to compute rho
    public Penalty create(final KnapsackInstance instance) {
        return switch (this) {
            case LINEAR -> LinearPenalty.of(instance);
            case DEATH -> new DeathPenalty();
        };
    }
}
