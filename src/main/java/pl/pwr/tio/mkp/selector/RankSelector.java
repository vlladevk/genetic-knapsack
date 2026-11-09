package pl.pwr.tio.mkp.selector;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import pl.pwr.tio.mkp.genetic.Individual;

// Rank selection (selekcja rankingowa), linear ranking: sort the population, the
// worst gets rank 0, the best rank P - 1; the chance depends only on the rank:
//   p(rank) = (2 - s) / P + 2 * rank * (s - 1) / (P * (P - 1))
// Only the order matters, not the values, so negative fitness and big gaps between
// individuals change nothing (like in tournament selection).
// Equal fitness: the one earlier in the population gets the lower rank.
public final class RankSelector implements Selector {

    // s, selection pressure (nacisk selekcyjny), 1..2: the best is picked s times more
    // often than the average. 1 = random choice, 2 = the worst is never picked.
    private final double pressure;

    public RankSelector(final double pressure) {
        if (pressure < 1 || pressure > 2) {
            throw new IllegalArgumentException("Pressure must be in [1, 2]: " + pressure);
        }
        this.pressure = pressure;
    }

    @Override
    public List<Individual> select(final List<Individual> population, final int count,
        final Random random) {
        final int size = population.size();
        if (size == 1) {
            return new Wheel(population, new double[] {1}).spin(count, random);
        }
        // order[rank] = index in the population
        final int[] order = IntStream.range(0, size)
            .boxed()
            .sorted(Comparator.comparingDouble(idx -> population.get(idx).fitness()))
            .mapToInt(Integer::intValue)
            .toArray();
        final double[] weights = new double[size];
        for (int rank = 0; rank < size; ++rank) {
            weights[order[rank]] = (2 - this.pressure) / size
                + 2 * rank * (this.pressure - 1) / ((double) size * (size - 1));
        }
        return new Wheel(population, weights).spin(count, random);
    }
}
