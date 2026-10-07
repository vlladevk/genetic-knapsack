package pl.pwr.tio.mkp.alterer;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

/**
 * One-point crossover (krzyżowanie jednopunktowe).
 *
 * <p>Both parents are cut at the same random point {@code cut} from {@code 1}
 * to {@code n - 1} (punkt cięcia) and their tails are swapped:</p>
 * <pre>
 * mother: 1 1 | 0 1 0 0        child 1: 1 1 | 1 1 1 0
 * father: 0 1 | 1 1 1 0   -&gt;   child 2: 0 1 | 0 1 0 0
 * </pre>
 * <p>The cut is never at 0 or {@code n}, because then a child would be a copy of a parent.</p>
 */
public final class SinglePointCrossover extends Crossover {

    /**
     * Ctor.
     *
     * @param probability Probability of crossing a pair of parents (pc), in [0, 1]
     */
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
