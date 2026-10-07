package pl.pwr.tio.mkp.engine;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import pl.pwr.tio.mkp.genetic.Individual;

/**
 * Result of one generation (wynik pokolenia): the population and its statistics.
 *
 * @param generation Number of the generation, the initial population is generation 0
 * @param population Evaluated population, not empty
 */
public record EvolutionResult(int generation, List<Individual> population) {

    /**
     * Order of individuals by fitness.
     */
    private static final Comparator<Individual> BY_FITNESS =
        Comparator.comparingDouble(Individual::fitness);

    /**
     * Validates the result and copies the population.
     *
     * @param generation Number of the generation
     * @param population Evaluated population
     */
    public EvolutionResult {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Population must not be empty");
        }
        population = List.copyOf(population);
    }

    /**
     * Individual with the highest fitness, feasible or not.
     *
     * @return Best individual
     */
    public Individual best() {
        return this.population.stream().max(EvolutionResult.BY_FITNESS).orElseThrow();
    }

    /**
     * Individual with the lowest fitness.
     *
     * @return Worst individual
     */
    public Individual worst() {
        return this.population.stream().min(EvolutionResult.BY_FITNESS).orElseThrow();
    }

    /**
     * Average fitness of the population.
     *
     * @return Average fitness
     */
    public double averageFitness() {
        return this.population.stream().mapToDouble(Individual::fitness).average().orElseThrow();
    }

    /**
     * Feasible individual with the highest fitness. Only feasible solutions are valid
     * answers of the algorithm, infeasible ones may live in the population only as
     * steps towards good solutions.
     *
     * @return Best feasible individual, empty when no individual is feasible
     */
    public Optional<Individual> bestFeasible() {
        return this.population.stream()
            .filter(Individual::feasible)
            .max(EvolutionResult.BY_FITNESS);
    }

    /**
     * Share of feasible individuals in the population.
     *
     * @return Number from 0 to 1
     */
    public double feasibleShare() {
        final long feasible = this.population.stream().filter(Individual::feasible).count();
        return (double) feasible / this.population.size();
    }
}
