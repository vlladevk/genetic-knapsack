package pl.pwr.tio.mkp.knapsack;

import java.util.List;
import pl.pwr.tio.mkp.genetic.Codec;
import pl.pwr.tio.mkp.genetic.Problem;

// Multidimensional knapsack problem (wielowymiarowy problem plecakowy):
// max total value, for every constraint i total weight <= capacity i.
// fitness = value reduced by Penalty for the overload.
public final class KnapsackProblem implements Problem<List<Item>> {

    // rounding error of decimal weights
    private static final double TOLERANCE = 1e-9;

    private final KnapsackInstance instance;

    private final Penalty penalty;

    private final KnapsackCodec codec;

    public KnapsackProblem(final KnapsackInstance instance, final Penalty penalty) {
        this.instance = instance;
        this.penalty = penalty;
        this.codec = new KnapsackCodec(instance);
    }

    @Override
    public Codec<List<Item>> codec() {
        return this.codec;
    }

    // objective function (funkcja celu)
    public double value(final List<Item> solution) {
        double total = 0;
        for (final Item item : solution) {
            total += item.value();
        }
        return total;
    }

    @Override
    // sum over constraints of max(0, load - capacity); 0 = feasible
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
