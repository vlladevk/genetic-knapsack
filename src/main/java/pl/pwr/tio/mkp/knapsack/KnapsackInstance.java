package pl.pwr.tio.mkp.knapsack;

import java.util.List;
import java.util.OptionalDouble;

/**
 * Instance of the multidimensional knapsack problem (instancja problemu).
 * Holds the items, the capacity of every constraint and, if known, the optimal value.
 */
public final class KnapsackInstance {

    /**
     * Name of the instance, used in results and plots.
     */
    private final String name;

    /**
     * Items, the item at position {@code j} has index {@code j}.
     */
    private final List<Item> items;

    /**
     * Capacity of every constraint (pojemność).
     */
    private final double[] capacities;

    /**
     * Optimal or best known value, empty when unknown.
     */
    private final OptionalDouble optimum;

    /**
     * Ctor.
     *
     * @param name Name of the instance
     * @param items Items, the item at position {@code j} must have index {@code j}
     * @param capacities Capacity of every constraint
     * @param optimum Optimal or best known value, empty when unknown
     */
    public KnapsackInstance(final String name, final List<Item> items,
        final double[] capacities, final OptionalDouble optimum) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Instance must have at least one item");
        }
        for (int idx = 0; idx < items.size(); ++idx) {
            final Item item = items.get(idx);
            if (item.index() != idx) {
                throw new IllegalArgumentException("Item at position " + idx + " has index "
                    + item.index());
            }
            if (item.dimensions() != capacities.length) {
                throw new IllegalArgumentException("Item " + idx + " has " + item.dimensions()
                    + " weights, expected " + capacities.length);
            }
        }
        this.name = name;
        this.items = List.copyOf(items);
        this.capacities = capacities.clone();
        this.optimum = optimum;
    }

    /**
     * Same instance with another optimal or best known value.
     *
     * @param value Optimal or best known value
     * @return New instance
     */
    public KnapsackInstance withOptimum(final double value) {
        return new KnapsackInstance(this.name, this.items, this.capacities,
            OptionalDouble.of(value));
    }

    /**
     * Name of the instance.
     *
     * @return Name
     */
    public String name() {
        return this.name;
    }

    /**
     * Number of items, equal to the length of the chromosome.
     *
     * @return Number of items (n)
     */
    public int size() {
        return this.items.size();
    }

    /**
     * Number of constraints.
     *
     * @return Number of constraints (m)
     */
    public int dimensions() {
        return this.capacities.length;
    }

    /**
     * All items.
     *
     * @return Unmodifiable list of items
     */
    public List<Item> items() {
        return this.items;
    }

    /**
     * One item.
     *
     * @param index Index of the item
     * @return Item
     */
    public Item item(final int index) {
        return this.items.get(index);
    }

    /**
     * Capacity of one constraint.
     *
     * @param constraint Index of the constraint
     * @return Capacity
     */
    public double capacity(final int constraint) {
        return this.capacities[constraint];
    }

    /**
     * Optimal or best known value.
     *
     * @return Value, empty when unknown
     */
    public OptionalDouble optimum() {
        return this.optimum;
    }

    @Override
    public String toString() {
        return String.format("%s (n=%d, m=%d)", this.name, this.size(), this.dimensions());
    }
}
