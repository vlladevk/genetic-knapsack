package pl.pwr.tio.mkp.genetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ChromosomeTest {

    @Test
    void parsesAndPrintsBits() {
        final Chromosome chromosome = Chromosome.parse("110100");
        assertEquals(6, chromosome.length());
        assertTrue(chromosome.gene(0));
        assertFalse(chromosome.gene(2));
        assertEquals("110100", chromosome.toString());
    }

    @Test
    void isNotChangedThroughInputArray() {
        final boolean[] genes = {true, false};
        final Chromosome chromosome = new Chromosome(genes);
        genes[0] = false;
        assertTrue(chromosome.gene(0));
    }

    @Test
    void isNotChangedThroughReturnedArray() {
        final Chromosome chromosome = Chromosome.parse("10");
        chromosome.genes()[0] = false;
        assertTrue(chromosome.gene(0));
    }

    @Test
    void rejectsEmptyAndNonBinaryInput() {
        assertThrows(IllegalArgumentException.class, Chromosome::new);
        assertThrows(IllegalArgumentException.class, () -> Chromosome.parse("102"));
    }
}
