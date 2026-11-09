package pl.pwr.tio.mkp.selector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Individual;

final class RankSelectorTest {

    private static final int SPINS = 32_000;

    @Test
    void returnsRequestedNumberOfIndividuals() {
        assertEquals(7, new RankSelector(2).select(
            RouletteSelectorTest.population(10, 8, 5, -3), 7, new Random(1)).size());
    }

    // s = 2, P = 4: chances 0, 1/6, 2/6, 3/6 from the worst to the best
    @Test
    void chanceDependsOnRank() {
        final List<Individual> picked = new RankSelector(2).select(
            RouletteSelectorTest.population(10, 8, 5, -3), RankSelectorTest.SPINS,
            new Random(2));
        RouletteSelectorTest.assertShare(picked, 10, 3.0 / 6);
        RouletteSelectorTest.assertShare(picked, 8, 2.0 / 6);
        RouletteSelectorTest.assertShare(picked, 5, 1.0 / 6);
        RouletteSelectorTest.assertShare(picked, -3, 0);
    }

    // the same order gives the same chances, whatever the values
    @Test
    void ignoresDistanceBetweenValues() {
        final List<Individual> picked = new RankSelector(2).select(
            RouletteSelectorTest.population(98, 99, 100, 1000), RankSelectorTest.SPINS,
            new Random(3));
        RouletteSelectorTest.assertShare(picked, 1000, 3.0 / 6);
        RouletteSelectorTest.assertShare(picked, 98, 0);
    }

    @Test
    void pressureOneIsRandomChoice() {
        final List<Individual> picked = new RankSelector(1).select(
            RouletteSelectorTest.population(10, 8, 5, -3), RankSelectorTest.SPINS,
            new Random(4));
        RouletteSelectorTest.assertShare(picked, -3, 0.25);
        RouletteSelectorTest.assertShare(picked, 10, 0.25);
    }

    @Test
    void rejectsPressureOutsideRange() {
        assertThrows(IllegalArgumentException.class, () -> new RankSelector(2.5));
    }
}
