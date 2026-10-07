package pl.pwr.tio.mkp.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Crossover (krzyżowanie). Parents go in pairs (0, 1), (2, 3), ... - order is
// already random after selection. A pair is crossed with probability pc,
// otherwise copied. Odd population: the last one is copied.
public abstract class Crossover implements GeneticOperator {

    private static final int PARENTS = 2;

    // pc, crossover probability (prawdopodobieństwo krzyżowania): chance that a pair
    // of parents is crossed, otherwise both are copied unchanged
    private final double probability;

    protected Crossover(final double probability) {
        this.probability = Probability.check(probability);
    }

    @Override
    public final List<Chromosome> apply(final List<Chromosome> population, final Random random) {
        final List<Chromosome> offspring = new ArrayList<>(population.size());
        int first = 0;
        while (first + 1 < population.size()) {
            final Chromosome mother = population.get(first);
            final Chromosome father = population.get(first + 1);
            if (random.nextDouble() < this.probability) {
                offspring.addAll(this.cross(mother, father, random));
            } else {
                offspring.add(mother);
                offspring.add(father);
            }
            first += Crossover.PARENTS;
        }
        if (first < population.size()) {
            offspring.add(population.get(first));
        }
        return offspring;
    }

    protected abstract List<Chromosome> cross(Chromosome mother, Chromosome father,
        Random random);
}
