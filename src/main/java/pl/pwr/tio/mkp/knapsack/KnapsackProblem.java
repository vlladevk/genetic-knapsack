package pl.pwr.tio.mkp.knapsack;

import java.util.List;
import pl.pwr.tio.mkp.genetic.Codec;
import pl.pwr.tio.mkp.genetic.Problem;

/**
 * Multidimensional knapsack problem (wielowymiarowy problem plecakowy).
 *
 * <p>Maximize the total value of chosen items so that, for every constraint {@code i},
 * the total weight does not exceed capacity {@code i}. Fitness (funkcja przystosowania)
 * is the total value reduced by a {@link Penalty} for the excess weight.</p>
 */
public final class KnapsackProblem implements Problem<List<Item>> {

    /**
     * Excess smaller than this is treated as rounding error of decimal weights.
     */
    private static final double TOLERANCE = 1e-9;

    /**
     * Data of the problem.
     */
    private final KnapsackInstance instance;

    /**
     * How constraint violations reduce fitness.
     */
    private final Penalty penalty;

    /**
     * Encoding.
     */
    private final KnapsackCodec codec;

    /**
     * Ctor.
     *
     * @param instance Data of the problem
     * @param penalty How constraint violations reduce fitness
     */
    public KnapsackProblem(final KnapsackInstance instance, final Penalty penalty) {
        this.instance = instance;
        this.penalty = penalty;
        this.codec = new KnapsackCodec(instance);
    }

    @Override
    public Codec<List<Item>> codec() {
        return this.codec;
    }

    /**
     * Total value of the chosen items (funkcja celu).
     *
     * @param solution Chosen items
     * @return Sum of their values
     */
    public double value(final List<Item> solution) {
        double total = 0;
        for (final Item item : solution) {
            total += item.value();
        }
        return total;
    }

    /**
     * Sum, over all constraints, of the weight above the capacity.
     *
     * @param solution Chosen items
     * @return Total excess weight, {@code 0} when every constraint is satisfied
     */
    @Override
    public double violation(final List<Item> solution) {
        double excess = 0;
        for (int constraint = 0; constraint < this.instance.dimensions(); ++constraint) {
            double load = 0;
            for (final Item item : solution) {
                load += item.weight(constraint);
            }
            final double over = load - this.instance.capacity(constraint);
            if (over > KnapsackProblem.TOLERANCE) {
                excess += over;
            }
        }
        return excess;
    }

    @Override
    public double fitness(final List<Item> solution) {
        return this.penalty.apply(this.value(solution), this.violation(solution));
    }
}
