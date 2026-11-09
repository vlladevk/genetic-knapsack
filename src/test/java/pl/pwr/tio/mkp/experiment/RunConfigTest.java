package pl.pwr.tio.mkp.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.knapsack.CsvInstanceReader;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

final class RunConfigTest {

    // every selection, crossover and mutation can be built and run
    @Test
    void everyCombinationRuns() throws IOException {
        final KnapsackInstance toy = new CsvInstanceReader().read(Path.of("data", "toy.csv"), 23);
        for (final SelectionType selection : SelectionType.values()) {
            for (final CrossoverType crossover : CrossoverType.values()) {
                for (final MutationType mutation : MutationType.values()) {
                    final RunConfig config = new RunConfig("test", "all", 10, 5, selection, 1,
                        crossover, 0.9, mutation, 0.1, 0.3, PenaltyType.LINEAR);
                    assertEquals(6, config.engine(toy, 1).stream().limit(6).count());
                }
            }
        }
    }

    @Test
    void countsEvaluationsFromGenerationZero() {
        final RunConfig config = RunConfig.taskOne("test", "base", 30, 1000, 0.9, 0.01,
            PenaltyType.LINEAR);
        assertEquals(30, config.evaluations(0));
        assertEquals(30_030, config.evaluations(1000));
    }
}
