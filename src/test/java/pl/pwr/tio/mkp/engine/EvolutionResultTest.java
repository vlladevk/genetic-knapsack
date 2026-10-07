package pl.pwr.tio.mkp.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Individual;

final class EvolutionResultTest {

    private static final double DELTA = 1e-9;

    private static final EvolutionResult RESULT = new EvolutionResult(3, List.of(
        new Individual(Chromosome.parse("00"), 10, 0),
        new Individual(Chromosome.parse("01"), 40, 5),
        new Individual(Chromosome.parse("10"), 20, 0),
        new Individual(Chromosome.parse("11"), -1000, 9)
    ));

    @Test
    void computesStatistics() {
        assertEquals(40, RESULT.best().fitness(), DELTA);
        assertEquals(-1000, RESULT.worst().fitness(), DELTA);
        assertEquals(-232.5, RESULT.averageFitness(), DELTA);
        assertEquals(15, RESULT.medianFitness(), DELTA);
        assertEquals(0.5, RESULT.feasibleShare(), DELTA);
    }

    @Test
    void bestFeasibleIgnoresBetterInfeasibleIndividuals() {
        assertEquals(20, RESULT.bestFeasible().orElseThrow().fitness(), DELTA);
        assertTrue(new EvolutionResult(0, List.of(new Individual(Chromosome.parse("1"), 5, 1)))
            .bestFeasible().isEmpty());
    }
}
