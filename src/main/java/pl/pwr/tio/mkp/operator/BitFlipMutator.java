package pl.pwr.tio.mkp.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

/**
 * Bit-flip mutation (mutacja przez negację bitu).
 *
 * <p>Every gene of every chromosome is flipped independently with probability
 * {@code pm} (prawdopodobieństwo mutacji), so on average {@code pm * n} genes
 * change in a chromosome of length {@code n}.</p>
 */
public final class BitFlipMutator implements GeneticOperator {

    /**
     * Probability of flipping one gene (pm).
     */
    private final double probability;

    /**
     * Ctor.
     *
     * @param probability Probability of flipping one gene (pm), in [0, 1]
     */
    public BitFlipMutator(final double probability) {
        this.probability = Probability.check(probability);
    }

    /**
     * Probability of flipping one gene.
     *
     * @return pm
     */
    public double probability() {
        return this.probability;
    }

    @Override
    public List<Chromosome> apply(final List<Chromosome> population, final Random random) {
        final List<Chromosome> mutated = new ArrayList<>(population.size());
        for (final Chromosome chromosome : population) {
            mutated.add(this.mutate(chromosome, random));
        }
        return mutated;
    }

    /**
     * Mutates one chromosome.
     *
     * @param chromosome Chromosome
     * @param random Random number generator
     * @return New chromosome, or the same one when no gene was flipped
     */
    private Chromosome mutate(final Chromosome chromosome, final Random random) {
        final boolean[] genes = chromosome.genes();
        boolean changed = false;
        for (int gene = 0; gene < genes.length; ++gene) {
            if (random.nextDouble() < this.probability) {
                genes[gene] = !genes[gene];
                changed = true;
            }
        }
        final Chromosome result;
        if (changed) {
            result = new Chromosome(genes);
        } else {
            result = chromosome;
        }
        return result;
    }
}
