package pl.pwr.tio.mkp.selector;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Individual;

// Selection (selekcja): picks count parents, the same one may be picked many
// times. Better fitness = picked more often.
public interface Selector {

    List<Individual> select(List<Individual> population, int count, Random random);
}
