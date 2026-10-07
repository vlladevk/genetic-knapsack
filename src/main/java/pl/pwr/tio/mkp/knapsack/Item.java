package pl.pwr.tio.mkp.knapsack;

import java.util.Arrays;
import java.util.Objects;

// Item: value and one weight per constraint.
// index = position in the instance = gene index.
public record Item(int index, double value, double[] weights) {

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

    public double weight(final int constraint) {
        return this.weights[constraint];
    }

    public int dimensions() {
        return this.weights.length;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Item(int index1, double value1, double[] weights1)
            && this.index == index1
            && Double.compare(this.value, value1) == 0
            && Arrays.equals(this.weights, weights1);
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
