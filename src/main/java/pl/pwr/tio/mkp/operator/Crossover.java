package pl.pwr.tio.mkp.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

/**
 * Base of crossover operators (krzyżowanie).
 *
 * <p>Takes parents in pairs: (0, 1), (2, 3), ... The order of parents is already random
 * because it comes from selection. Each pair is crossed with probability {@code pc}
 * (prawdopodobieństwo krzyżowania), otherwise both parents are copied unchanged.
 * With an odd population the last parent is copied. Subclasses only define how two
 * parents are combined.</p>
 */
public abstract class Crossover implements GeneticOperator {

    /**
     * Number of parents taking part in one crossover.
     */
    private static final int PARENTS = 2;

    /**
     * Probability of crossing a pair of parents (pc).
     */
    private final double probability;

    /**
     * Ctor.
     *
     * @param probability Probability of crossing a pair of parents (pc), in [0, 1]
     */
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

    /**
     * Combines two parents into two children.
     *
     * @param mother First parent
     * @param father Second parent, of the same length
     * @param random Random number generator of the run
     * @return Two children
     */
    protected abstract List<Chromosome> cross(Chromosome mother, Chromosome father,
        Random random);
}
