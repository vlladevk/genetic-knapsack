package pl.pwr.tio.mkp.genetic;

/**
 * Optimization problem (problem optymalizacyjny) solved by the genetic algorithm.
 *
 * <p>Provides the encoding and the fitness function (funkcja przystosowania).
 * Fitness is computed on a decoded solution, in the language of the problem,
 * not on raw genes. Higher fitness is better.</p>
 *
 * @param <T> Type of a solution
 */
public interface Problem<T> {

    /**
     * Encoding of the problem.
     *
     * @return Codec
     */
    Codec<T> codec();

    /**
     * Fitness of a solution, including the penalty for violated constraints.
     *
     * @param solution Solution
     * @return Fitness, higher is better
     */
    double fitness(T solution);

    /**
     * How much a solution violates the constraints.
     *
     * @param solution Solution
     * @return Total violation, {@code 0} when the solution is feasible
     */
    double violation(T solution);

    /**
     * Decodes a chromosome and evaluates it.
     *
     * @param chromosome Chromosome
     * @return Evaluated individual
     */
    default Individual evaluate(final Chromosome chromosome) {
        final T solution = this.codec().decode(chromosome);
        return new Individual(chromosome, this.fitness(solution), this.violation(solution));
    }
}
