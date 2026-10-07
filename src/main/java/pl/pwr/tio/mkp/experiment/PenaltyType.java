package pl.pwr.tio.mkp.experiment;

import pl.pwr.tio.mkp.knapsack.DeathPenalty;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.LinearPenalty;
import pl.pwr.tio.mkp.knapsack.Penalty;

public enum PenaltyType {

    LINEAR {
        @Override
        public Penalty create(final KnapsackInstance instance) {
            return LinearPenalty.of(instance);
        }
    },

    DEATH {
        @Override
        public Penalty create(final KnapsackInstance instance) {
            return new DeathPenalty();
        }
    };

    public abstract Penalty create(KnapsackInstance instance);
}
