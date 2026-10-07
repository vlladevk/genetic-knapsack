package pl.pwr.tio.mkp.operator;

import java.util.List;
import java.util.Random;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Genetic operator (operator genetyczny): crossover or mutation on the whole
// population. Returns the same number of chromosomes. Each operator uses its own
// probability: pc per pair of parents, pm per gene.
public interface GeneticOperator {

    List<Chromosome> apply(List<Chromosome> population, Random random);
}
