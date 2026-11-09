package pl.pwr.tio.mkp.selector;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Individual;

// Roulette selection (selekcja ruletkowa, proporcjonalna): chance of an individual
// is proportional to its fitness.
// The penalty makes fitness negative, and a sector cannot have negative size, so
// fitness is shifted by the worst one: weight = fitness - min. The worst individual
// gets 0, the others as much as they are better than the worst.
// Unlike tournament and rank, the pressure depends on the values: one individual far
// better than the rest takes almost the whole wheel, and one very bad individual makes
// all the others almost equal.
public final class RouletteSelector implements Selector {

    @Override
    public List<Individual> select(final List<Individual> population, final int count,
        final Random random) {
        final double min = population.stream()
            .mapToDouble(Individual::fitness)
            .min()
            .orElseThrow(() -> new IllegalArgumentException("Population must not be empty"));
        final double[] weights = population.stream()
            .mapToDouble(ind -> ind.fitness() - min)
            .toArray();
        return new Wheel(population, weights).spin(count, random);
    }
}
