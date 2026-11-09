package pl.pwr.tio.mkp.operator;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Uniform crossover (krzyżowanie równomierne, jednorodne): for every gene toss a coin,
// child 1 takes it from the mother or from the father, child 2 from the other one.
//   mother: 1 1 0 1 0 0      coin:    M F F M F M
//   father: 0 1 1 1 1 0  ->  child 1: 1 1 1 1 1 0, child 2: 0 1 0 1 0 0
// The position of a gene does not matter: neighbours are not kept together. In the
// knapsack the order of items is arbitrary, so neighbouring genes are not related anyway.
// Genes on which the parents agree are always kept.
public final class UniformCrossover extends Crossover {

    // chance that a gene comes from the other parent; 0.5 = a fair coin
    private static final double SWAP = 0.5;

    public UniformCrossover(final double probability) {
        super(probability);
    }

    @Override
    protected List<Chromosome> cross(final Chromosome mother, final Chromosome father,
        final Random random) {
        final int length = mother.length();
        if (father.length() != length) {
            throw new IllegalArgumentException("Parents have different lengths: " + length
                + " and " + father.length());
        }
        final boolean[] one = mother.genes();
        final boolean[] two = father.genes();
        for (int gene = 0; gene < length; ++gene) {
            if (random.nextDouble() < UniformCrossover.SWAP) {
                final boolean taken = one[gene];
                one[gene] = two[gene];
                two[gene] = taken;
            }
        }
        return List.of(new Chromosome(one), new Chromosome(two));
    }
}
