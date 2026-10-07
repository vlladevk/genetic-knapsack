package pl.pwr.tio.mkp.genetic;

/**
 * Individual (osobnik): a chromosome together with its evaluation.
 *
 * @param chromosome Chromosome
 * @param fitness Fitness (wartość przystosowania), already including the penalty
 * @param violation Total constraint violation (naruszenie ograniczeń), {@code 0} when feasible
 */
public record Individual(Chromosome chromosome, double fitness, double violation) {

    /**
     * Validates the individual.
     *
     * @param chromosome Chromosome
     * @param fitness Fitness
     * @param violation Total constraint violation
     */
    public Individual {
        if (violation < 0) {
            throw new IllegalArgumentException("Violation must not be negative: " + violation);
        }
    }

    /**
     * Whether the individual satisfies all constraints.
     *
     * @return True if the solution is feasible (rozwiązanie dopuszczalne)
     */
    public boolean feasible() {
        return this.violation == 0;
    }
}
