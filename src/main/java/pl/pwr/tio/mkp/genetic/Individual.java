package pl.pwr.tio.mkp.genetic;

// Individual (osobnik): chromosome + fitness (penalty included) + violation (0 = feasible).
public record Individual(Chromosome chromosome, double fitness, double violation) {

    public Individual {
        if (violation < 0) {
            throw new IllegalArgumentException("Violation must not be negative: " + violation);
        }
    }

    public boolean feasible() {
        return this.violation == 0;
    }
}
