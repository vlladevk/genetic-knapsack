package pl.pwr.tio.mkp.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Individual;

/**
 * Tournament selection (selekcja turniejowa).
 *
 * <p>Each parent is the winner of a tournament: {@code size} individuals are drawn
 * at random (with replacement) and the one with the highest fitness wins; a tie is won
 * by the individual drawn first. The size of the tournament controls selection pressure
 * (nacisk selekcyjny): size 1 is random choice, bigger sizes favour the best more.</p>
 */
public final class TournamentSelector implements Selector {

    /**
     * Number of individuals in one tournament (rozmiar turnieju).
     */
    private final int size;

    /**
     * Ctor.
     *
     * @param size Number of individuals in one tournament, at least 1
     */
    public TournamentSelector(final int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Tournament size must be at least 1: " + size);
        }
        this.size = size;
    }

    /**
     * Number of individuals in one tournament.
     *
     * @return Tournament size
     */
    public int size() {
        return this.size;
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

    /**
     * Plays one tournament.
     *
     * @param population Population
     * @param random Random number generator
     * @return Winner
     */
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
