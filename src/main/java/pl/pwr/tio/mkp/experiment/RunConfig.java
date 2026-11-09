package pl.pwr.tio.mkp.experiment;

import java.util.ArrayList;
import java.util.List;
import pl.pwr.tio.mkp.engine.Engine;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.KnapsackProblem;
import pl.pwr.tio.mkp.operator.GeneticOperator;

// Parameters of one configuration; generations = stop condition (warunek stopu).
// crossover = pc, mutation = pm of bit-flip (per gene), inversion = p of inversion
// (per chromosome).
public record RunConfig(
    String series,
    String label,
    int populationSize,
    int generations,
    SelectionType selection,
    int elites,
    CrossoverType crossoverType,
    double crossover,
    MutationType mutationType,
    double mutation,
    double inversion,
    PenaltyType penalty
) {

    // configuration of task 1: tournament k = 2, no elites, one-point crossover, bit-flip
    public static RunConfig taskOne(final String series, final String label,
        final int population, final int generations, final double crossover,
        final double mutation, final PenaltyType penalty) {
        return new RunConfig(series, label, population, generations, SelectionType.TOURNAMENT,
            0, CrossoverType.SINGLE_POINT, crossover, MutationType.BIT_FLIP, mutation, 0,
            penalty);
    }

    // fitness evaluations up to the given generation: P for each generation 0..g
    public long evaluations(final int generation) {
        return (long) this.populationSize * (generation + 1);
    }

    public Engine engine(final KnapsackInstance instance, final long seed) {
        final List<GeneticOperator> operators = new ArrayList<>();
        operators.add(this.crossoverType.create(this.crossover));
        operators.addAll(this.mutationType.create(this.mutation, this.inversion));
        return new Engine.Builder(new KnapsackProblem(instance, this.penalty.create(instance)))
            .populationSize(this.populationSize)
            .elites(this.elites)
            .selector(this.selection.create())
            .operators(operators.toArray(GeneticOperator[]::new))
            .seed(seed)
            .build();
    }
}
