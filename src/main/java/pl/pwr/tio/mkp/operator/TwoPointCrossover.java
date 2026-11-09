package pl.pwr.tio.mkp.operator;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Two-point crossover (krzyżowanie dwupunktowe): cut both parents at two points and
// swap the middle parts.
//   mother: 1 1 | 0 1 0 | 0      child 1: 1 1 | 1 1 1 | 0
//   father: 0 1 | 1 1 1 | 0  ->  child 2: 0 1 | 0 1 0 | 0
// Unlike one-point, the first and the last gene can stay together in a child.
public final class TwoPointCrossover extends Crossover {

    public TwoPointCrossover(final double probability) {
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
        if (length < 3) {
            children = List.of(mother, father);
        } else {
            // two different cuts in [1, n - 1], so the middle part is not empty
            // and not the whole chromosome
            final int first = 1 + random.nextInt(length - 1);
            int second = 1 + random.nextInt(length - 2);
            if (second >= first) {
                ++second;
            }
            final int from = Math.min(first, second);
            final int to = Math.max(first, second);
            final boolean[] one = mother.genes();
            final boolean[] two = father.genes();
            for (int gene = from; gene < to; ++gene) {
                final boolean middle = one[gene];
                one[gene] = two[gene];
                two[gene] = middle;
            }
            children = List.of(new Chromosome(one), new Chromosome(two));
        }
        return children;
    }
}
