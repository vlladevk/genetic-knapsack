package pl.pwr.tio.mkp.selector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Individual;

final class TournamentSelectorTest {

    private static final List<Individual> POPULATION = List.of(
        new Individual(Chromosome.parse("00"), 3, 0),
        new Individual(Chromosome.parse("01"), 5, 0),
        new Individual(Chromosome.parse("10"), 2, 0),
        new Individual(Chromosome.parse("11"), 5, 0)
    );

    @Test
    void returnsRequestedNumberOfIndividuals() {
        assertEquals(7, new TournamentSelector(2).select(POPULATION, 7, new Random(1)).size());
    }

    @Test
    void betterIndividualsWinMoreOften() {
        final List<Individual> winners =
            new TournamentSelector(2).select(POPULATION, 16_000, new Random(2));
        final long worst = winners.stream().filter(ind -> ind.fitness() == 2).count();
        final long middle = winners.stream().filter(ind -> ind.fitness() == 3).count();
        final long best = winners.stream().filter(ind -> ind.fitness() == 5).count();
        assertTrue(worst < middle && middle < best, worst + " " + middle + " " + best);
        assertTrue(Math.abs(worst - 1000) < 200, "worst won " + worst + " of 16000, expected 1/16");
    }

    @Test
    void sizeOneIsRandomChoice() {
        final List<Individual> winners =
            new TournamentSelector(1).select(POPULATION, 16_000, new Random(3));
        final long worst = winners.stream().filter(ind -> ind.fitness() == 2).count();
        assertTrue(Math.abs(worst - 4000) < 300, "worst won " + worst + " of 16000, expected 1/4");
    }
}
