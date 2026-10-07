package pl.pwr.tio.mkp.genetic;

import java.util.Random;

/**
 * Encoding (kodowanie): translation between chromosomes and solutions of a problem.
 *
 * <p>The meaning of the genes lives here, once per problem, not in every chromosome.</p>
 *
 * @param <T> Type of a solution, for example a list of chosen items
 */
public interface Codec<T> {

    /**
     * Translates a chromosome into a solution of the problem.
     *
     * @param chromosome Chromosome
     * @return Solution
     */
    T decode(Chromosome chromosome);

    /**
     * Creates a random chromosome, used to build the initial population.
     *
     * @param random Random number generator of the run
     * @return New random chromosome
     */
    Chromosome random(Random random);
}
