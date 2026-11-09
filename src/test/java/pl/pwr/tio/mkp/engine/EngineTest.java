package pl.pwr.tio.mkp.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.knapsack.CsvInstanceReader;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.KnapsackProblem;
import pl.pwr.tio.mkp.knapsack.LinearPenalty;
import pl.pwr.tio.mkp.operator.BitFlipMutator;
import pl.pwr.tio.mkp.operator.SinglePointCrossover;
import pl.pwr.tio.mkp.selector.TournamentSelector;

final class EngineTest {

    @Test
    void findsOptimumOfToyExample() throws IOException {
        final KnapsackInstance toy = new CsvInstanceReader().read(Path.of("data", "toy.csv"), 23);
        final Engine engine = this.engine(toy, 7);
        final double best = engine.stream()
            .limit(51)
            .map(EvolutionResult::bestFeasible)
            .flatMap(Optional::stream)
            .mapToDouble(Individual::fitness)
            .max()
            .orElseThrow();
        assertEquals(23, best, 1e-9);
    }

    @Test
    void sameSeedGivesSameRun() throws IOException {
        final KnapsackInstance instance =
            new CsvInstanceReader().read(Path.of("data", "mknap1-6.csv"), 16537);
        final List<Double> first = this.engine(instance, 3).stream().limit(30)
            .map(result -> result.best().fitness()).toList();
        final List<Double> second = this.engine(instance, 3).stream().limit(30)
            .map(result -> result.best().fitness()).toList();
        assertEquals(first, second);
    }

    @Test
    void keepsPopulationSizeAndCountsGenerations() throws IOException {
        final KnapsackInstance toy = new CsvInstanceReader().read(Path.of("data", "toy.csv"), 23);
        final List<EvolutionResult> results = this.engine(toy, 1).stream().limit(5).toList();
        assertEquals(List.of(0, 1, 2, 3, 4),
            results.stream().map(EvolutionResult::generation).toList());
        assertTrue(results.stream().allMatch(result -> result.population().size() == 10));
    }

    // Strong mutation (5/n) often destroys the best individual. Without elitism the best
    // fitness of a generation sometimes drops, with one elite it never does.
    @Test
    void eliteKeepsBestFitnessFromDropping() throws IOException {
        final KnapsackInstance instance =
            new CsvInstanceReader().read(Path.of("data", "mknap1-6.csv"), 16537);
        assertTrue(this.drops(this.engine(instance, 5, 0, 5.0)) > 0);
        assertEquals(0, this.drops(this.engine(instance, 5, 1, 5.0)));
    }

    @Test
    void eliteIsBestParentUnchanged() throws IOException {
        final KnapsackInstance instance =
            new CsvInstanceReader().read(Path.of("data", "mknap1-6.csv"), 16537);
        final List<EvolutionResult> results =
            this.engine(instance, 9, 2, 5.0).stream().limit(20).toList();
        for (int gen = 1; gen < results.size(); ++gen) {
            final Individual parent = results.get(gen - 1).best();
            assertTrue(results.get(gen).population().contains(parent), "generation " + gen);
        }
    }

    @Test
    void rejectsAsManyElitesAsPopulation() throws IOException {
        final KnapsackInstance toy = new CsvInstanceReader().read(Path.of("data", "toy.csv"), 23);
        assertThrows(IllegalArgumentException.class, () -> this.engine(toy, 1, 10, 1.0));
    }

    private long drops(final Engine engine) {
        final List<Double> best = engine.stream().limit(200)
            .map(result -> result.best().fitness()).toList();
        long count = 0;
        for (int gen = 1; gen < best.size(); ++gen) {
            if (best.get(gen) < best.get(gen - 1)) {
                ++count;
            }
        }
        return count;
    }

    private Engine engine(final KnapsackInstance instance, final long seed) {
        return this.engine(instance, seed, 0, 1.0);
    }

    // mutation as a multiple of 1/n
    private Engine engine(final KnapsackInstance instance, final long seed, final int elites,
        final double mutationPerN) {
        return new Engine.Builder(new KnapsackProblem(instance, LinearPenalty.of(instance)))
            .populationSize(10)
            .elites(elites)
            .selector(new TournamentSelector(2))
            .operators(new SinglePointCrossover(0.9),
                new BitFlipMutator(mutationPerN / instance.size()))
            .seed(seed)
            .build();
    }
}
