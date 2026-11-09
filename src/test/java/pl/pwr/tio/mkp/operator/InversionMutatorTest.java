package pl.pwr.tio.mkp.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;

final class InversionMutatorTest {

    private static final Chromosome SAMPLE = Chromosome.parse("1011000110");

    @Test
    void zeroProbabilityKeepsChromosome() {
        assertSame(InversionMutatorTest.SAMPLE, new InversionMutator(0.0)
            .apply(List.of(InversionMutatorTest.SAMPLE), new Random(1)).get(0));
    }

    // the number of taken items never changes
    @Test
    void keepsNumberOfOnes() {
        final Random random = new Random(2);
        for (int run = 0; run < 500; ++run) {
            final String mutated = new InversionMutator(1.0)
                .apply(List.of(InversionMutatorTest.SAMPLE), random).get(0).toString();
            assertEquals(5, mutated.chars().filter(bit -> bit == '1').count(), mutated);
        }
    }

    // the result is the original with one segment reversed
    @Test
    void reversesOneSegment() {
        final Random random = new Random(3);
        final String original = InversionMutatorTest.SAMPLE.toString();
        for (int run = 0; run < 500; ++run) {
            final String mutated = new InversionMutator(1.0)
                .apply(List.of(InversionMutatorTest.SAMPLE), random).get(0).toString();
            assertTrue(InversionMutatorTest.isOneReversal(original, mutated), mutated);
        }
    }

    @Test
    void probabilityIsPerChromosome() {
        final Chromosome sorted = Chromosome.parse("0000011111");
        final List<Chromosome> population = Collections.nCopies(10_000, sorted);
        final long changed = new InversionMutator(0.3).apply(population, new Random(4))
            .stream()
            .filter(chromosome -> !chromosome.equals(sorted))
            .count();
        // every segment crossing the middle changes the chromosome, the others do not
        assertTrue(changed > 1000 && changed < 3000, "changed " + changed);
    }

    @Test
    void rejectsInvalidProbability() {
        assertThrows(IllegalArgumentException.class, () -> new InversionMutator(1.5));
    }

    private static boolean isOneReversal(final String original, final String mutated) {
        boolean found = false;
        for (int from = 0; from < original.length() && !found; ++from) {
            for (int to = from + 2; to <= original.length() && !found; ++to) {
                final String reversed = original.substring(0, from)
                    + new StringBuilder(original.substring(from, to)).reverse()
                    + original.substring(to);
                found = reversed.equals(mutated);
            }
        }
        return found;
    }
}
