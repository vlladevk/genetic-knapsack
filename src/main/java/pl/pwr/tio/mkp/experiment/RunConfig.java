package pl.pwr.tio.mkp.experiment;

import java.util.List;
import pl.pwr.tio.mkp.engine.Engine;
import pl.pwr.tio.mkp.knapsack.Item;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.KnapsackProblem;
import pl.pwr.tio.mkp.operator.BitFlipMutator;
import pl.pwr.tio.mkp.operator.SinglePointCrossover;
import pl.pwr.tio.mkp.selector.TournamentSelector;

/**
 * Parameters of the genetic algorithm used in one configuration of an experiment.
 *
 * @param series Experiment series the configuration belongs to, for example {@code A-pc}
 * @param label Short description of what differs in this configuration, for example {@code pc=0.7}
 * @param populationSize Number of individuals in a generation (P)
 * @param generations Number of generations after the initial one (stop condition)
 * @param tournamentSize Tournament size (k)
 * @param crossover Probability of crossover (pc)
 * @param mutation Probability of mutation of one gene (pm)
 * @param penalty Penalty function
 */
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

    /**
     * Engine for one run of this configuration.
     *
     * @param instance Instance to solve
     * @param seed Seed of the run
     * @return Engine
     */
    public Engine<List<Item>> engine(final KnapsackInstance instance, final long seed) {
        return new Engine.Builder<>(new KnapsackProblem(instance, this.penalty.create(instance)))
            .populationSize(this.populationSize)
            .selector(new TournamentSelector(this.tournamentSize))
            .operators(new SinglePointCrossover(this.crossover), new BitFlipMutator(this.mutation))
            .seed(seed)
            .build();
    }
}
