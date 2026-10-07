package pl.pwr.tio.mkp.experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import pl.pwr.tio.mkp.engine.EvolutionResult;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

/**
 * Runs every configuration several times (with seeds 1, 2, ..., runs) on one instance
 * and writes the results to {@code <output>/<instance>/}:
 * <ul>
 *   <li>{@code history.csv}: statistics of every generation of every run,</li>
 *   <li>{@code runs.csv}: one row per run with the best feasible value found.</li>
 * </ul>
 */
public final class ExperimentRunner {

    /**
     * Columns of {@code history.csv}.
     */
    private static final String[] HISTORY = {
        "series", "label", "seed", "generation", "best", "average", "median", "worst",
        "best_feasible", "best_ever", "feasible_share",
    };

    /**
     * Columns of {@code runs.csv}.
     */
    private static final String[] RUNS = {
        "instance", "series", "label", "population", "generations", "tournament", "pc", "pm",
        "penalty", "seed", "best", "found_at", "optimum", "feasible_share",
    };

    /**
     * Directory for the results.
     */
    private final Path output;

    /**
     * Number of runs of every configuration.
     */
    private final int runs;

    /**
     * Ctor.
     *
     * @param output Directory for the results
     * @param runs Number of runs of every configuration, at least 1
     */
    public ExperimentRunner(final Path output, final int runs) {
        if (runs < 1) {
            throw new IllegalArgumentException("Number of runs must be at least 1: " + runs);
        }
        this.output = output;
        this.runs = runs;
    }

    /**
     * Runs all configurations on an instance and writes the CSV files.
     *
     * @param instance Instance
     * @param configs Configurations
     * @return Outcomes of all runs
     * @throws IOException If the results can not be written
     */
    public List<RunOutcome> run(final KnapsackInstance instance, final List<RunConfig> configs)
        throws IOException {
        final Path dir = this.output.resolve(instance.name());
        Files.createDirectories(dir);
        final List<RunOutcome> outcomes = new ArrayList<>(configs.size() * this.runs);
        try (CsvWriter history =
                new CsvWriter(dir.resolve("history.csv"), ExperimentRunner.HISTORY);
            CsvWriter summary = new CsvWriter(dir.resolve("runs.csv"), ExperimentRunner.RUNS)) {
            for (final RunConfig config : configs) {
                for (long seed = 1; seed <= this.runs; ++seed) {
                    final RunOutcome outcome = ExperimentRunner.single(instance, config, seed,
                        history);
                    summary.row(instance.name(), config.series(), config.label(),
                        config.populationSize(), config.generations(), config.tournamentSize(),
                        config.crossover(), config.mutation(), config.penalty().name(), seed,
                        outcome.best(), outcome.foundAt(), instance.optimum(),
                        outcome.feasibleShare());
                    outcomes.add(outcome);
                }
            }
        }
        return outcomes;
    }

    /**
     * One run: generations 0..G, the stop condition is the number of generations.
     *
     * @param instance Instance
     * @param config Configuration
     * @param seed Seed
     * @param history Where to write statistics of every generation
     * @return Outcome of the run
     * @throws IOException If the history can not be written
     */
    private static RunOutcome single(final KnapsackInstance instance, final RunConfig config,
        final long seed, final CsvWriter history) throws IOException {
        final Iterator<EvolutionResult> generations = config.engine(instance, seed).stream()
            .limit(config.generations() + 1L)
            .iterator();
        OptionalDouble best = OptionalDouble.empty();
        int found = -1;
        double share = 0;
        while (generations.hasNext()) {
            final EvolutionResult result = generations.next();
            final Optional<Individual> feasible = result.bestFeasible();
            if (feasible.isPresent()
                && (best.isEmpty() || feasible.get().fitness() > best.getAsDouble())) {
                best = OptionalDouble.of(feasible.get().fitness());
                found = result.generation();
            }
            share = result.feasibleShare();
            history.row(config.series(), config.label(), seed, result.generation(),
                result.best().fitness(), result.averageFitness(), result.medianFitness(),
                result.worst().fitness(),
                ExperimentRunner.fitness(feasible), best, share);
        }
        return new RunOutcome(config, seed, best, found, share);
    }

    /**
     * Fitness of an optional individual.
     *
     * @param individual Individual
     * @return Its fitness, empty when there is no individual
     */
    private static OptionalDouble fitness(final Optional<Individual> individual) {
        return individual.map(ind -> OptionalDouble.of(ind.fitness()))
            .orElseGet(OptionalDouble::empty);
    }
}
