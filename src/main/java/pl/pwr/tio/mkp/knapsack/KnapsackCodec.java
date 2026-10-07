package pl.pwr.tio.mkp.knapsack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Codec;

// Binary encoding (kodowanie binarne): gene j = 1 means item j is in the knapsack.
public final class KnapsackCodec implements Codec<List<Item>> {

    private final KnapsackInstance instance;

    public KnapsackCodec(final KnapsackInstance instance) {
        this.instance = instance;
    }

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

    // every item taken with probability 1/2
    @Override
    public Chromosome random(final Random random) {
        final boolean[] genes = new boolean[this.instance.size()];
        for (int idx = 0; idx < genes.length; ++idx) {
            genes[idx] = random.nextBoolean();
        }
        return new Chromosome(genes);
    }
}
