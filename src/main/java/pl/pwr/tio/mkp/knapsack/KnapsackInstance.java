package pl.pwr.tio.mkp.knapsack;

import java.util.List;

// One problem: items, capacity of every
// constraint and the optimal (or best known) value.
public final class KnapsackInstance {

    private final String name;

    // item at position j has index j
    private final List<Item> items;

    private final double[] capacities;

    private final double optimum;

    public KnapsackInstance(final String name, final List<Item> items,
        final double[] capacities, final double optimum) {
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

    public String name() {
        return this.name;
    }

    // n = chromosome length
    public int size() {
        return this.items.size();
    }

    // m = number of constraints
    public int dimensions() {
        return this.capacities.length;
    }

    public List<Item> items() {
        return this.items;
    }

    public Item item(final int index) {
        return this.items.get(index);
    }

    public double capacity(final int constraint) {
        return this.capacities[constraint];
    }

    public double optimum() {
        return this.optimum;
    }

    @Override
    public String toString() {
        return String.format("%s (n=%d, m=%d)", this.name, this.size(), this.dimensions());
    }
}
