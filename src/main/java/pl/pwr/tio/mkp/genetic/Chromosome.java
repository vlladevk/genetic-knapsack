package pl.pwr.tio.mkp.genetic;

import java.util.Arrays;

/**
 * Binary chromosome (chromosom): an immutable sequence of genes.
 *
 * <p>Immutability matters: selection may pick the same individual twice,
 * so operators must create new chromosomes instead of changing existing ones.</p>
 */
public final class Chromosome {

    /**
     * Character used for a gene set to {@code true}.
     */
    private static final char ONE = '1';

    /**
     * Character used for a gene set to {@code false}.
     */
    private static final char ZERO = '0';

    /**
     * Genes.
     */
    private final boolean[] genes;

    /**
     * Ctor.
     *
     * @param genes Genes, copied defensively
     */
    public Chromosome(final boolean... genes) {
        if (genes.length == 0) {
            throw new IllegalArgumentException("Chromosome must have at least one gene");
        }
        this.genes = genes.clone();
    }

    /**
     * Creates a chromosome from a string of zeros and ones, for example {@code "110100"}.
     *
     * @param bits Zeros and ones
     * @return Chromosome
     */
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

    /**
     * Number of genes.
     *
     * @return Length of the chromosome
     */
    public int length() {
        return this.genes.length;
    }

    /**
     * Value of one gene.
     *
     * @param index Index of the gene
     * @return Value of the gene
     */
    public boolean gene(final int index) {
        return this.genes[index];
    }

    /**
     * All genes.
     *
     * @return Copy of the genes, changing it does not change the chromosome
     */
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
