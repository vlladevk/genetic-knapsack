package pl.pwr.tio.mkp.genetic;

// Problem solved by the GA: encoding + fitness function (funkcja przystosowania).
// Fitness is computed on the decoded solution T, not on raw bits. Higher is better.
public interface Problem<T> {

    Codec<T> codec();

    // includes the penalty for violated constraints
    double fitness(T solution);

    // 0 = feasible
    double violation(T solution);

    default Individual evaluate(final Chromosome chromosome) {
        final T solution = this.codec().decode(chromosome);
        return new Individual(chromosome, this.fitness(solution), this.violation(solution));
    }
}
