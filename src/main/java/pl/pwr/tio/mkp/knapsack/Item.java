package pl.pwr.tio.mkp.knapsack;

import java.util.Arrays;
import java.util.Objects;

/**
 * Item (przedmiot) of the multidimensional knapsack problem.
 *
 * @param index Position of the item in the instance, equal to the gene index
 * @param value Value of the item (wartość)
 * @param weights Weight of the item for every constraint (waga), one entry per constraint
 */
public record Item(int index, double value, double[] weights) {

    /**
     * Validates the item and copies the weights.
     *
     * @param index Position of the item in the instance
     * @param value Value of the item
     * @param weights Weight of the item for every constraint
     */
    public Item {
        if (index < 0) {
            throw new IllegalArgumentException("Index must not be negative: " + index);
        }
        if (value < 0) {
            throw new IllegalArgumentException("Value must not be negative: " + value);
        }
        if (weights.length == 0) {
            throw new IllegalArgumentException("Item must have at least one weight");
        }
        weights = weights.clone();
    }

    /**
     * Weights of the item.
     *
     * @return Copy of the weights, one entry per constraint
     */
    @Override
    public double[] weights() {
        return this.weights.clone();
    }

    /**
     * Weight of the item for one constraint.
     *
     * @param constraint Index of the constraint
     * @return Weight of the item for this constraint
     */
    public double weight(final int constraint) {
        return this.weights[constraint];
    }

    /**
     * Number of constraints the item has weights for.
     *
     * @return Number of constraints
     */
    public int dimensions() {
        return this.weights.length;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Item item
            && this.index == item.index
            && Double.compare(this.value, item.value) == 0
            && Arrays.equals(this.weights, item.weights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.index, this.value, Arrays.hashCode(this.weights));
    }

    @Override
    public String toString() {
        return String.format("#%d(value=%s, weights=%s)", this.index, this.value,
            Arrays.toString(this.weights));
    }
}
