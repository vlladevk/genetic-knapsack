package pl.pwr.tio.mkp.operator;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// One-point crossover (krzyżowanie jednopunktowe): cut both parents at the same
// point and swap the tails.
//   mother: 1 1 | 0 1 0 0      child 1: 1 1 | 1 1 1 0
//   father: 0 1 | 1 1 1 0  ->  child 2: 0 1 | 0 1 0 0
public final class SinglePointCrossover extends Crossover {

    public SinglePointCrossover(final double probability) {
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
        final List<Chromosome> children;
        if (length < 2) {
            children = List.of(mother, father);
        } else {
            // cut in [1, n - 1]: at 0 or n the children would be copies
            final int cut = 1 + random.nextInt(length - 1);
            final boolean[] first = mother.genes();
            final boolean[] second = father.genes();
            for (int gene = cut; gene < length; ++gene) {
                final boolean tail = first[gene];
                first[gene] = second[gene];
                second[gene] = tail;
            }
            children = List.of(new Chromosome(first), new Chromosome(second));
        }
        return children;
    }
}
