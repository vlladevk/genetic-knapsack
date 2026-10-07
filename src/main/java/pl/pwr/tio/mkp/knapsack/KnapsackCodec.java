package pl.pwr.tio.mkp.knapsack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Codec;

/**
 * Binary encoding (kodowanie binarne) of the knapsack problem.
 * Gene {@code j} set to {@code true} means that item {@code j} is in the knapsack.
 */
public final class KnapsackCodec implements Codec<List<Item>> {

    /**
     * Instance whose items the genes refer to.
     */
    private final KnapsackInstance instance;

    /**
     * Ctor.
     *
     * @param instance Instance whose items the genes refer to
     */
    public KnapsackCodec(final KnapsackInstance instance) {
        this.instance = instance;
    }

    /**
     * Items whose genes are set.
     *
     * @param chromosome Chromosome of length equal to the number of items
     * @return Chosen items
     */
    @Override
    public List<Item> decode(final Chromosome chromosome) {
        if (chromosome.length() != this.instance.size()) {
            throw new IllegalArgumentException("Chromosome has " + chromosome.length()
                + " genes, instance has " + this.instance.size() + " items");
        }
        final List<Item> chosen = new ArrayList<>(chromosome.length());
        for (int idx = 0; idx < chromosome.length(); ++idx) {
            if (chromosome.gene(idx)) {
                chosen.add(this.instance.item(idx));
            }
        }
        return chosen;
    }

    /**
     * Random chromosome: every item is taken with probability 1/2.
     *
     * @param random Random number generator of the run
     * @return New random chromosome
     */
    @Override
    public Chromosome random(final Random random) {
        final boolean[] genes = new boolean[this.instance.size()];
        for (int idx = 0; idx < genes.length; ++idx) {
            genes[idx] = random.nextBoolean();
        }
        return new Chromosome(genes);
    }
}
