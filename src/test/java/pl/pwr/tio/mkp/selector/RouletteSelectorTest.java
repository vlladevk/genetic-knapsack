package pl.pwr.tio.mkp.selector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Individual;

final class RouletteSelectorTest {

    private static final int SPINS = 32_000;

    @Test
    void returnsRequestedNumberOfIndividuals() {
        assertEquals(7, new RouletteSelector().select(
            RouletteSelectorTest.population(10, 8, 5, -3), 7, new Random(1)).size());
    }

    // weights after the shift: 13, 11, 8, 0 of 32
    @Test
    void chanceIsProportionalToShiftedFitness() {
        final List<Individual> picked = new RouletteSelector().select(
            RouletteSelectorTest.population(10, 8, 5, -3), RouletteSelectorTest.SPINS,
            new Random(2));
        RouletteSelectorTest.assertShare(picked, 10, 13.0 / 32);
        RouletteSelectorTest.assertShare(picked, 8, 11.0 / 32);
        RouletteSelectorTest.assertShare(picked, 5, 8.0 / 32);
        RouletteSelectorTest.assertShare(picked, -3, 0);
    }

    // one far better individual takes almost the whole wheel
    @Test
    void outlierTakesAlmostEverything() {
        final List<Individual> picked = new RouletteSelector().select(
            RouletteSelectorTest.population(1000, 100, 99, 98), RouletteSelectorTest.SPINS,
            new Random(3));
        RouletteSelectorTest.assertShare(picked, 1000, 902.0 / 905);
    }

    @Test
    void equalFitnessIsRandomChoice() {
        final List<Individual> picked = new RouletteSelector().select(
            RouletteSelectorTest.population(4, 4, 4, 4), RouletteSelectorTest.SPINS,
            new Random(4));
        final long first = picked.stream()
            .filter(ind -> ind.chromosome().equals(Chromosome.parse("00")))
            .count();
        assertEquals(0.25, (double) first / RouletteSelectorTest.SPINS, 0.01);
    }

    static List<Individual> population(final double... fitness) {
        final String[] genes = {"00", "01", "10", "11"};
        final Individual[] result = new Individual[fitness.length];
        for (int idx = 0; idx < fitness.length; ++idx) {
            result[idx] = new Individual(Chromosome.parse(genes[idx]), fitness[idx], 0);
        }
        return List.of(result);
    }

    static void assertShare(final List<Individual> picked, final double fitness,
        final double expected) {
        final long count = picked.stream().filter(ind -> ind.fitness() == fitness).count();
        final double share = (double) count / picked.size();
        assertTrue(Math.abs(share - expected) < 0.01,
            "fitness " + fitness + ": share " + share + ", expected " + expected);
    }
}
