package pl.pwr.tio.mkp.experiment;

import pl.pwr.tio.mkp.knapsack.DeathPenalty;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.LinearPenalty;
import pl.pwr.tio.mkp.knapsack.Penalty;

/**
 * Penalty functions compared in the experiments.
 */
public enum PenaltyType {

    /**
     * Linear penalty with rho computed from the instance.
     */
    LINEAR {
        @Override
        public Penalty create(final KnapsackInstance instance) {
            return LinearPenalty.of(instance);
        }
    },

    /**
     * Death penalty.
     */
    DEATH {
        @Override
        public Penalty create(final KnapsackInstance instance) {
            return new DeathPenalty();
        }
    };

    /**
     * Creates the penalty for an instance.
     *
     * @param instance Instance
     * @return Penalty
     */
    public abstract Penalty create(KnapsackInstance instance);
}
