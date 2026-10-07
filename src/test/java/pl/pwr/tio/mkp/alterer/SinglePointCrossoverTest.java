package pl.pwr.tio.mkp.alterer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;

final class SinglePointCrossoverTest {

    private static final Chromosome ONES = Chromosome.parse("111111");

    private static final Chromosome ZEROS = Chromosome.parse("000000");

    @Test
    void childrenAreHeadOfOneParentAndTailOfTheOther() {
        final Random random = new Random(1);
        for (int run = 0; run < 100; ++run) {
            final List<Chromosome> children = new SinglePointCrossover(1.0)
                .alter(List.of(ONES, ZEROS), random);
            final String first = children.get(0).toString();
            final String second = children.get(1).toString();
            assertTrue(first.matches("1+0+"), first);
            assertTrue(second.matches("0+1+"), second);
            assertEquals(first.indexOf('0'), second.indexOf('1'));
        }
    }

    @Test
    void cutPointCoversWholeRangeButNeverTheEnds() {
        final Random random = new Random(2);
        final boolean[] seen = new boolean[7];
        for (int run = 0; run < 1000; ++run) {
            final Chromosome child = new SinglePointCrossover(1.0)
                .alter(List.of(ONES, ZEROS), random).get(0);
            seen[child.toString().indexOf('0')] = true;
        }
        for (int cut = 1; cut <= 5; ++cut) {
            assertTrue(seen[cut], "cut " + cut + " never used");
        }
        assertEquals(false, seen[0]);
    }

    @Test
    void zeroProbabilityCopiesParents() {
        final List<Chromosome> parents = List.of(ONES, ZEROS, ONES);
        assertEquals(parents, new SinglePointCrossover(0.0).alter(parents, new Random(3)));
    }

    @Test
    void oddPopulationKeepsItsSize() {
        final List<Chromosome> children = new SinglePointCrossover(1.0)
            .alter(List.of(ONES, ZEROS, ONES), new Random(4));
        assertEquals(3, children.size());
        assertEquals(ONES, children.get(2));
    }

    @Test
    void doesNotChangeParents() {
        final List<Chromosome> parents = List.of(ONES, ZEROS);
        final List<Chromosome> children = new SinglePointCrossover(1.0)
            .alter(parents, new Random(5));
        assertNotEquals(parents, children);
        assertEquals("111111", ONES.toString());
        assertEquals("000000", ZEROS.toString());
    }

    @Test
    void rejectsInvalidProbability() {
        assertThrows(IllegalArgumentException.class, () -> new SinglePointCrossover(1.5));
        assertThrows(IllegalArgumentException.class, () -> new SinglePointCrossover(-0.1));
    }
}
