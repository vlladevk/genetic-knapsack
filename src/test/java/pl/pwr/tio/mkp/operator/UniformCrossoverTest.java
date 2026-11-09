package pl.pwr.tio.mkp.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;

final class UniformCrossoverTest {

    private static final int GENES = 1000;

    @Test
    void childrenAreComplementary() {
        final List<Chromosome> children = new UniformCrossover(1.0).apply(
            List.of(Chromosome.parse("1111111111"), Chromosome.parse("0000000000")),
            new Random(1));
        for (int gene = 0; gene < 10; ++gene) {
            assertTrue(children.get(0).gene(gene) != children.get(1).gene(gene));
        }
    }

    // about half of the genes come from each parent, not in blocks
    @Test
    void takesAboutHalfOfGenesFromEachParent() {
        final Chromosome ones = Chromosome.parse("1".repeat(UniformCrossoverTest.GENES));
        final Chromosome zeros = Chromosome.parse("0".repeat(UniformCrossoverTest.GENES));
        final String child = new UniformCrossover(1.0)
            .apply(List.of(ones, zeros), new Random(2)).get(0).toString();
        final long fromMother = child.chars().filter(bit -> bit == '1').count();
        assertEquals(500, fromMother, 50);
        assertTrue(child.split("10|01").length > 300, "genes should be mixed, not in blocks");
    }

    @Test
    void keepsGenesOnWhichParentsAgree() {
        final Chromosome mother = Chromosome.parse("1100101");
        final Chromosome father = Chromosome.parse("1011100");
        final Random random = new Random(3);
        for (int run = 0; run < 100; ++run) {
            for (final Chromosome child : new UniformCrossover(1.0)
                .apply(List.of(mother, father), random)) {
                assertTrue(child.gene(0) && child.gene(4) && !child.gene(5), child.toString());
            }
        }
    }

    @Test
    void zeroProbabilityCopiesParents() {
        final List<Chromosome> parents =
            List.of(Chromosome.parse("1111"), Chromosome.parse("0000"));
        assertEquals(parents, new UniformCrossover(0.0).apply(parents, new Random(4)));
    }
}
