package pl.pwr.tio.mkp.selector;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Individual;

/**
 * Selection method (metoda selekcji): chooses parents from the population.
 *
 * <p>Contract: returns exactly {@code count} individuals, the same individual may be
 * chosen more than once, the population is not modified. Better individuals
 * (higher fitness) should be chosen more often.</p>
 */
public interface Selector {

    /**
     * Chooses parents.
     *
     * @param population Current population, not empty
     * @param count How many individuals to choose
     * @param random Random number generator of the run
     * @return Chosen individuals, in random order
     */
    List<Individual> select(List<Individual> population, int count, Random random);
}
