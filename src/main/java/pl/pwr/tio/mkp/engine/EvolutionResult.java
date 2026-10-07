package pl.pwr.tio.mkp.engine;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import pl.pwr.tio.mkp.genetic.Individual;

// One generation (pokolenie) and its statistics. Generation 0 is the initial population.
public record EvolutionResult(int generation, List<Individual> population) {

    private static final Comparator<Individual> BY_FITNESS =
        Comparator.comparingDouble(Individual::fitness);

    public EvolutionResult {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Population must not be empty");
        }
        population = List.copyOf(population);
    }

    // may be infeasible, see bestFeasible()
    public Individual best() {
        return this.population.stream().max(EvolutionResult.BY_FITNESS).orElseThrow();
    }

    public Individual worst() {
        return this.population.stream().min(EvolutionResult.BY_FITNESS).orElseThrow();
    }

    public double averageFitness() {
        return this.population.stream().mapToDouble(Individual::fitness).average().orElseThrow();
    }

    // median, because a few heavily penalized individuals pull the average far down
    public double medianFitness() {
        final double[] sorted = this.population.stream()
            .mapToDouble(Individual::fitness)
            .sorted()
            .toArray();
        final int middle = sorted.length / 2;
        final double median;
        if (sorted.length % 2 == 0) {
            median = (sorted[middle - 1] + sorted[middle]) / 2;
        } else {
            median = sorted[middle];
        }
        return median;
    }

    // Only a feasible solution can be the answer; infeasible ones stay in the population
    // only as a way towards good solutions. Empty if nobody is feasible.
    public Optional<Individual> bestFeasible() {
        return this.population.stream()
            .filter(Individual::feasible)
            .max(EvolutionResult.BY_FITNESS);
    }

    // 0..1
    public double feasibleShare() {
        final long feasible = this.population.stream().filter(Individual::feasible).count();
        return (double) feasible / this.population.size();
    }
}
