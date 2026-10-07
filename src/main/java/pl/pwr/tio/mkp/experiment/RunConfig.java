package pl.pwr.tio.mkp.experiment;

import pl.pwr.tio.mkp.engine.Engine;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.KnapsackProblem;
import pl.pwr.tio.mkp.operator.BitFlipMutator;
import pl.pwr.tio.mkp.operator.SinglePointCrossover;
import pl.pwr.tio.mkp.selector.TournamentSelector;

// Parameters of one configuration; generations = stop condition (warunek stopu).
public record RunConfig(
    String series,
    String label,
    int populationSize,
    int generations,
    int tournamentSize,
    double crossover,
    double mutation,
    PenaltyType penalty
) {

    public Engine engine(final KnapsackInstance instance, final long seed) {
        return new Engine.Builder(new KnapsackProblem(instance, this.penalty.create(instance)))
            .populationSize(this.populationSize)
            .selector(new TournamentSelector(this.tournamentSize))
            .operators(new SinglePointCrossover(this.crossover), new BitFlipMutator(this.mutation))
            .seed(seed)
            .build();
    }
}
