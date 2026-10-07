package pl.pwr.tio.mkp.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.knapsack.CsvInstanceReader;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

final class ExperimentPlanTest {

    private static final double DELTA = 1e-12;

    @Test
    void keepsSeriesAndLabelsInOrder() throws IOException {
        final List<RunConfig> configs = new ExperimentPlan()
            .configs(ExperimentPlanTest.instance("mknap1-6.csv", 16537));
        assertEquals(
            List.of(
                "A-pc pc=0.0", "A-pc pc=0.5", "A-pc pc=0.7", "A-pc pc=0.9", "A-pc pc=1.0",
                "B-pm pm=0/n", "B-pm pm=0.5/n", "B-pm pm=1/n", "B-pm pm=2/n", "B-pm pm=5/n",
                "B-pm pm=10/n", "C-population P=10", "C-population P=30",
                "C-population P=50", "D-penalty penalty=linear", "D-penalty penalty=death"
            ),
            configs.stream().map(config -> config.series() + " " + config.label()).toList()
        );
    }

    @Test
    void usesBaseParameters() throws IOException {
        final RunConfig base = new ExperimentPlan()
            .configs(ExperimentPlanTest.instance("mknap1-6.csv", 16537)).get(3);
        assertEquals("pc=0.9", base.label());
        assertEquals(30, base.populationSize());
        assertEquals(1000, base.generations());
        assertEquals(2, base.tournamentSize());
        assertEquals(0.9, base.crossover(), ExperimentPlanTest.DELTA);
        assertEquals(1.0 / 50, base.mutation(), ExperimentPlanTest.DELTA);
        assertEquals(PenaltyType.LINEAR, base.penalty());
    }

    @Test
    void scalesMutationWithInstanceSize() throws IOException {
        final RunConfig small = new ExperimentPlan()
            .configs(ExperimentPlanTest.instance("mknap1-6.csv", 16537)).get(8);
        final RunConfig large = new ExperimentPlan()
            .configs(ExperimentPlanTest.instance("mknapcb1-0.csv", 24381)).get(8);
        assertEquals("pm=2/n", small.label());
        assertEquals(2.0 / 50, small.mutation(), ExperimentPlanTest.DELTA);
        assertEquals(2.0 / 100, large.mutation(), ExperimentPlanTest.DELTA);
    }

    private static KnapsackInstance instance(final String file, final double optimum)
        throws IOException {
        return new CsvInstanceReader().read(Path.of("data", file), optimum);
    }
}
