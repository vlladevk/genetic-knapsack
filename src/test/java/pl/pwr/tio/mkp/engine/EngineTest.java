package pl.pwr.tio.mkp.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.KnapsackProblem;
import pl.pwr.tio.mkp.knapsack.LinearPenalty;
import pl.pwr.tio.mkp.knapsack.OrLibraryReader;
import pl.pwr.tio.mkp.operator.BitFlipMutator;
import pl.pwr.tio.mkp.operator.SinglePointCrossover;
import pl.pwr.tio.mkp.selector.TournamentSelector;

final class EngineTest {

    @Test
    void findsOptimumOfToyExample() throws IOException {
        final KnapsackInstance toy = new OrLibraryReader().read(Path.of("data", "toy.txt")).get(0);
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
            new OrLibraryReader().read(Path.of("data", "mknap1.txt")).get(6);
        final List<Double> first = this.engine(instance, 3).stream().limit(30)
            .map(result -> result.best().fitness()).toList();
        final List<Double> second = this.engine(instance, 3).stream().limit(30)
            .map(result -> result.best().fitness()).toList();
        assertEquals(first, second);
    }

    @Test
    void keepsPopulationSizeAndCountsGenerations() throws IOException {
        final KnapsackInstance toy = new OrLibraryReader().read(Path.of("data", "toy.txt")).get(0);
        final List<EvolutionResult> results = this.engine(toy, 1).stream().limit(5).toList();
        assertEquals(List.of(0, 1, 2, 3, 4),
            results.stream().map(EvolutionResult::generation).toList());
        assertTrue(results.stream().allMatch(result -> result.population().size() == 10));
    }

    private Engine engine(final KnapsackInstance instance, final long seed) {
        return new Engine.Builder(new KnapsackProblem(instance, LinearPenalty.of(instance)))
            .populationSize(10)
            .selector(new TournamentSelector(2))
            .operators(new SinglePointCrossover(0.9), new BitFlipMutator(1.0 / instance.size()))
            .seed(seed)
            .build();
    }
}
