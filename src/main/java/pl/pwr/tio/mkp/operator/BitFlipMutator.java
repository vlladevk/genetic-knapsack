package pl.pwr.tio.mkp.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Bit-flip mutation (mutacja przez negację bitu): every gene flips with
// probability pm, on average pm * n genes per chromosome.
public final class BitFlipMutator implements GeneticOperator {

    private final double probability;

    public BitFlipMutator(final double probability) {
        this.probability = Probability.check(probability);
    }

    @Override
    public List<Chromosome> apply(final List<Chromosome> population, final Random random) {
        final List<Chromosome> mutated = new ArrayList<>(population.size());
        for (final Chromosome chromosome : population) {
            mutated.add(this.mutate(chromosome, random));
        }
        return mutated;
    }

    // same object if nothing was flipped
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
