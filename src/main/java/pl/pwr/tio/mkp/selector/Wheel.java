package pl.pwr.tio.mkp.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Individual;

// Roulette wheel (koło ruletki): individual i gets a sector of size weights[i],
// so it is picked with probability weights[i] / sum. Roulette and rank selection
// differ only in how they compute the weights.
final class Wheel {

    private final List<Individual> population;

    // cumulative[i] = weights[0] + ... + weights[i]
    private final double[] cumulative;

    Wheel(final List<Individual> population, final double[] weights) {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Population must not be empty");
        }
        this.population = population;
        this.cumulative = new double[weights.length];
        double sum = 0;
        for (int idx = 0; idx < weights.length; ++idx) {
            if (weights[idx] < 0) {
                throw new IllegalArgumentException("Weight must not be negative: " + weights[idx]);
            }
            sum += weights[idx];
            this.cumulative[idx] = sum;
        }
    }

    // count spins of the wheel; all weights 0 = everyone has the same chance
    List<Individual> spin(final int count, final Random random) {
        final double total = this.cumulative[this.cumulative.length - 1];
        final List<Individual> picked = new ArrayList<>(count);
        for (int idx = 0; idx < count; ++idx) {
            if (total == 0) {
                picked.add(this.population.get(random.nextInt(this.population.size())));
            } else {
                picked.add(this.population.get(this.sector(random.nextDouble() * total)));
            }
        }
        return picked;
    }

    // first sector whose cumulative weight is above point (binary search)
    private int sector(final double point) {
        int found = Arrays.binarySearch(this.cumulative, point);
        if (found < 0) {
            found = -found - 1;
        } else {
            ++found;
        }
        // skip empty sectors (weight 0) that end exactly at point
        while (this.cumulative[found] <= point) {
            ++found;
        }
        return found;
    }
}
