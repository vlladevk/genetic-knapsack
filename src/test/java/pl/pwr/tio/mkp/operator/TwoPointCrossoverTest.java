package pl.pwr.tio.mkp.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;

final class TwoPointCrossoverTest {

    private static final Chromosome ONES = Chromosome.parse("111111");

    private static final Chromosome ZEROS = Chromosome.parse("000000");

    @Test
    void childrenSwapOneMiddlePart() {
        final Random random = new Random(1);
        for (int run = 0; run < 200; ++run) {
            final List<Chromosome> children = new TwoPointCrossover(1.0)
                .apply(List.of(ONES, ZEROS), random);
            final String first = children.get(0).toString();
            final String second = children.get(1).toString();
            assertTrue(first.matches("1+0+1*"), first);
            assertEquals(first.replace('0', 'x').replace('1', '0').replace('x', '1'), second);
        }
    }

    // a middle part that ends before the last gene must happen, otherwise it is one-point
    @Test
    void lastGeneSometimesStaysWithFirst() {
        final Random random = new Random(2);
        boolean seen = false;
        for (int run = 0; run < 200 && !seen; ++run) {
            seen = new TwoPointCrossover(1.0).apply(List.of(ONES, ZEROS), random)
                .get(0).toString().matches("1+0+1+");
        }
        assertTrue(seen);
    }

    @Test
    void keepsGenesOnWhichParentsAgree() {
        final Chromosome mother = Chromosome.parse("1100101");
        final Chromosome father = Chromosome.parse("1011100");
        final Random random = new Random(3);
        for (int run = 0; run < 100; ++run) {
            for (final Chromosome child : new TwoPointCrossover(1.0)
                .apply(List.of(mother, father), random)) {
                assertTrue(child.gene(0) && child.gene(4), child.toString());
            }
        }
    }

    @Test
    void zeroProbabilityCopiesParents() {
        final List<Chromosome> parents = List.of(ONES, ZEROS);
        assertEquals(parents, new TwoPointCrossover(0.0).apply(parents, new Random(4)));
    }
}
