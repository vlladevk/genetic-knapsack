package pl.pwr.tio.mkp.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Inversion (inwersja): with probability p per chromosome take a random segment and
// reverse the order of its genes.
//   1 0 [1 1 0 0] 1  ->  1 0 [0 0 1 1] 1
// Not the same as bit-flip: no gene is negated. The number of ones in the segment
// stays the same, so in the knapsack some items are swapped for others - the number
// of taken items does not change, only which ones.
public final class InversionMutator implements GeneticOperator {

    // p, chance that a chromosome is inverted (per chromosome, not per gene as in bit-flip)
    private final double probability;

    public InversionMutator(final double probability) {
        this.probability = Probability.check(probability);
    }

    @Override
    public List<Chromosome> apply(final List<Chromosome> population, final Random random) {
        final List<Chromosome> mutated = new ArrayList<>(population.size());
        for (final Chromosome chromosome : population) {
            if (chromosome.length() >= 2 && random.nextDouble() < this.probability) {
                mutated.add(InversionMutator.invert(chromosome, random));
            } else {
                mutated.add(chromosome);
            }
        }
        return mutated;
    }

    // segment [from, to), at least 2 genes long: reversing one gene changes nothing
    private static Chromosome invert(final Chromosome chromosome, final Random random) {
        final int length = chromosome.length();
        final int first = random.nextInt(length + 1);
        int second = random.nextInt(length);
        if (second >= first) {
            ++second;
        }
        int from = Math.min(first, second);
        int to = Math.max(first, second);
        if (to - from < 2) {
            if (to < length) {
                ++to;
            } else {
                --from;
            }
        }
        final boolean[] genes = chromosome.genes();
        for (int left = from, right = to - 1; left < right; ++left, --right) {
            final boolean gene = genes[left];
            genes[left] = genes[right];
            genes[right] = gene;
        }
        return new Chromosome(genes);
    }
}
