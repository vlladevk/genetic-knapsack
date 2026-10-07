package pl.pwr.tio.mkp.genetic;

import java.util.Arrays;

// Binary chromosome (chromosom). Immutable: selection can pick the same parent twice,
// so operators create new chromosomes instead of changing existing ones.
public final class Chromosome {

    private static final char ONE = '1';

    private static final char ZERO = '0';

    private final boolean[] genes;

    public Chromosome(final boolean... genes) {
        if (genes.length == 0) {
            throw new IllegalArgumentException("Chromosome must have at least one gene");
        }
        this.genes = genes.clone();
    }

    // e.g. parse("110100")
    public static Chromosome parse(final String bits) {
        final boolean[] genes = new boolean[bits.length()];
        for (int idx = 0; idx < bits.length(); ++idx) {
            final char bit = bits.charAt(idx);
            if (bit != Chromosome.ONE && bit != Chromosome.ZERO) {
                throw new IllegalArgumentException("Not a binary string: " + bits);
            }
            genes[idx] = bit == Chromosome.ONE;
        }
        return new Chromosome(genes);
    }

    public int length() {
        return this.genes.length;
    }

    public boolean gene(final int index) {
        return this.genes[index];
    }

    // returns a copy
    public boolean[] genes() {
        return this.genes.clone();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Chromosome chromosome
            && Arrays.equals(this.genes, chromosome.genes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.genes);
    }

    @Override
    public String toString() {
        final StringBuilder bits = new StringBuilder(this.genes.length);
        for (final boolean gene : this.genes) {
            if (gene) {
                bits.append(Chromosome.ONE);
            } else {
                bits.append(Chromosome.ZERO);
            }
        }
        return bits.toString();
    }
}
