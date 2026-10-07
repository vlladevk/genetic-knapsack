package pl.pwr.tio.mkp.experiment;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import pl.pwr.tio.mkp.engine.EvolutionResult;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

// Runs every configuration with seeds 1..runs and writes <output>/<instance>/
// history.csv (every generation) and runs.csv (one row per run).
public final class ExperimentRunner {

    private static final String[] HISTORY = {
        "series", "label", "seed", "generation", "best", "average", "median", "worst",
        "best_feasible", "best_ever", "feasible_share",
    };

    private static final String[] RUNS = {
        "instance", "series", "label", "population", "generations", "tournament", "pc", "pm",
        "penalty", "seed", "best", "found_at", "optimum", "feasible_share",
    };

    private final Path output;

    private final int runs;

    public ExperimentRunner(final Path output, final int runs) {
        if (runs < 1) {
            throw new IllegalArgumentException("Number of runs must be at least 1: " + runs);
        }
        this.output = output;
        this.runs = runs;
    }

    public List<RunOutcome> run(final KnapsackInstance instance, final List<RunConfig> configs)
        throws IOException {
        final Path dir = this.output.resolve(instance.name());
        Files.createDirectories(dir);
        final List<RunOutcome> outcomes = new ArrayList<>(configs.size() * this.runs);
        try (PrintWriter history = ExperimentRunner.open(dir.resolve("history.csv"));
            PrintWriter summary = ExperimentRunner.open(dir.resolve("runs.csv"))) {
            history.println(String.join(",", ExperimentRunner.HISTORY));
            summary.println(String.join(",", ExperimentRunner.RUNS));
            for (final RunConfig config : configs) {
                for (long seed = 1; seed <= this.runs; ++seed) {
                    final RunOutcome outcome = ExperimentRunner.single(instance, config, seed,
                        history);
                    summary.println(ExperimentRunner.csv(instance.name(), config.series(),
                        config.label(), config.populationSize(), config.generations(),
                        config.tournamentSize(), config.crossover(), config.mutation(),
                        config.penalty().name(), seed,
                        outcome.best(), outcome.foundAt(), instance.optimum(),
                        outcome.feasibleShare()));
                    outcomes.add(outcome);
                }
            }
        }
        return outcomes;
    }

    private static RunOutcome single(final KnapsackInstance instance, final RunConfig config,
        final long seed, final PrintWriter history) {
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
            history.println(ExperimentRunner.csv(config.series(), config.label(), seed,
                result.generation(), result.best().fitness(), result.averageFitness(),
                result.medianFitness(), result.worst().fitness(),
                ExperimentRunner.fitness(feasible), best, share));
        }
        return new RunOutcome(config, seed, best, found, share);
    }

    private static PrintWriter open(final Path file) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(file));
    }

    private static String csv(final Object... cells) {
        return Arrays.stream(cells).map(ExperimentRunner::cell).collect(Collectors.joining(","));
    }

    // empty OptionalDouble (no feasible solution) = empty cell
    private static String cell(final Object value) {
        final String text;
        if (value instanceof OptionalDouble optional) {
            text = optional.isPresent() ? String.valueOf(optional.getAsDouble()) : "";
        } else {
            text = String.valueOf(value);
        }
        return text;
    }

    private static OptionalDouble fitness(final Optional<Individual> individual) {
        return individual.map(ind -> OptionalDouble.of(ind.fitness()))
            .orElseGet(OptionalDouble::empty);
    }
}
