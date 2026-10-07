package pl.pwr.tio.mkp.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;

final class BitFlipMutatorTest {

    @Test
    void zeroProbabilityKeepsChromosome() {
        final Chromosome chromosome = Chromosome.parse("1010");
        assertSame(chromosome,
            new BitFlipMutator(0.0).apply(List.of(chromosome), new Random(1)).get(0));
    }

    @Test
    void probabilityOneFlipsEveryGene() {
        final Chromosome chromosome = Chromosome.parse("1010");
        assertEquals(Chromosome.parse("0101"),
            new BitFlipMutator(1.0).apply(List.of(chromosome), new Random(1)).get(0));
    }

    @Test
    void flipsAboutPmOfAllGenes() {
        final int length = 10_000;
        final Chromosome zeros = new Chromosome(new boolean[length]);
        final String mutated = new BitFlipMutator(0.1)
            .apply(List.of(zeros), new Random(2)).get(0).toString();
        final long ones = mutated.chars().filter(bit -> bit == '1').count();
        assertTrue(ones > 900 && ones < 1100, "flipped " + ones + " of " + length);
    }
}
