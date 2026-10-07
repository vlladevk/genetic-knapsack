package pl.pwr.tio.mkp.operator;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

/**
 * Genetic operator (operator genetyczny) applied to a whole population,
 * for example crossover or mutation. Operators are applied one after another.
 *
 * <p>Contract: returns a population of the same size, never modifies the input
 * (chromosomes are immutable, changed ones are new objects). Every operator applies
 * its own probability, because its meaning differs between operators
 * (per pair of parents for crossover, per gene for bit-flip mutation).</p>
 */
public interface GeneticOperator {

    /**
     * Applies the operator.
     *
     * @param population Chromosomes of the parents
     * @param random Random number generator of the run
     * @return Changed population of the same size
     */
    List<Chromosome> apply(List<Chromosome> population, Random random);
}
