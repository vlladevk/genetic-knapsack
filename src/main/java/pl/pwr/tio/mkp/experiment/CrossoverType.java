package pl.pwr.tio.mkp.experiment;

import pl.pwr.tio.mkp.operator.Crossover;
import pl.pwr.tio.mkp.operator.SinglePointCrossover;
import pl.pwr.tio.mkp.operator.TwoPointCrossover;
import pl.pwr.tio.mkp.operator.UniformCrossover;

// Crossover operators compared in the experiments.
public enum CrossoverType {
    SINGLE_POINT,
    TWO_POINT,
    UNIFORM;

    public Crossover create(final double probability) {
        return switch (this) {
            case SINGLE_POINT -> new SinglePointCrossover(probability);
            case TWO_POINT -> new TwoPointCrossover(probability);
            case UNIFORM -> new UniformCrossover(probability);
        };
    }
}
