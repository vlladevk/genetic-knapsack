package pl.pwr.tio.mkp.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Individual;

// Tournament selection (selekcja turniejowa): draw size individuals with
// replacement, the best wins (tie: the first drawn).
// size = selection pressure (nacisk selekcyjny), 1 = random choice.
public final class TournamentSelector implements Selector {

    private final int size;

    public TournamentSelector(final int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Tournament size must be at least 1: " + size);
        }
        this.size = size;
    }

    @Override
    public List<Individual> select(final List<Individual> population, final int count,
        final Random random) {
        if (population.isEmpty()) {
            throw new IllegalArgumentException("Population must not be empty");
        }
        final List<Individual> winners = new ArrayList<>(count);
        for (int idx = 0; idx < count; ++idx) {
            winners.add(this.tournament(population, random));
        }
        return winners;
    }

    private Individual tournament(final List<Individual> population, final Random random) {
        Individual winner = population.get(random.nextInt(population.size()));
        for (int round = 1; round < this.size; ++round) {
            final Individual rival = population.get(random.nextInt(population.size()));
            if (rival.fitness() > winner.fitness()) {
                winner = rival;
            }
        }
        return winner;
    }
}
