package pl.pwr.tio.mkp.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.knapsack.CsvInstanceReader;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

final class TaskTwoPlanTest {

    @Test
    void seriesHasEveryCombinationOnce() throws IOException {
        final List<RunConfig> all = new TaskTwoPlan().configs(TaskTwoPlanTest.instance());
        final List<RunConfig> combinations = all.stream()
            .filter(config -> "H-all".equals(config.series()))
            .toList();
        assertEquals(3 * 2 * 3 * 3, combinations.size());
        assertEquals(combinations.size(), new HashSet<>(combinations.stream()
            .map(config -> List.of(config.selection(), config.elites(),
                config.crossoverType(), config.mutationType()))
            .toList()).size());
    }

    @Test
    void labelsAreUniqueWithinSeries() throws IOException {
        final List<RunConfig> all = new TaskTwoPlan().configs(TaskTwoPlanTest.instance());
        assertEquals(all.size(), new HashSet<>(all.stream()
            .map(config -> config.series() + " " + config.label()).toList()).size());
    }

    // the same budget of fitness evaluations within a series: 30 030 in E-I,
    // about 50 000 in series J and the final series
    @Test
    void everySeriesHasEqualBudget() throws IOException {
        final List<RunConfig> all = new TaskTwoPlan().configs(TaskTwoPlanTest.instance());
        assertTrue(all.stream().filter(config -> !TaskTwoPlanTest.large(config))
            .allMatch(config -> config.evaluations(config.generations()) == 30L * 1001));
        assertTrue(all.stream().filter(TaskTwoPlanTest::large)
            .allMatch(config -> Math.abs(config.evaluations(config.generations()) - 50_050)
                <= 10));
    }

    // first configuration of series E = base of task 1
    @Test
    void startsFromBaseOfTaskOne() throws IOException {
        final RunConfig base = new TaskTwoPlan().configs(TaskTwoPlanTest.instance()).get(0);
        assertEquals(RunConfig.taskOne("E-selection", "tournament", 30, 1000, 0.9, 1.0 / 50,
            PenaltyType.LINEAR), base);
    }

    private static boolean large(final RunConfig config) {
        return "Z-final".equals(config.series()) || "J-approaches".equals(config.series());
    }

    private static KnapsackInstance instance() throws IOException {
        return new CsvInstanceReader().read(Path.of("data", "mknap1-6.csv"), 16537);
    }
}
