package pl.pwr.tio.mkp.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.genetic.Problem;
import pl.pwr.tio.mkp.operator.GeneticOperator;
import pl.pwr.tio.mkp.selector.Selector;
import pl.pwr.tio.mkp.selector.TournamentSelector;

// Genetic algorithm (algorytm genetyczny).
// One generation: select P parents -> crossover -> mutation -> evaluate.
// Children replace the whole population, except the e best parents (elitism).
public final class Engine {

    private static final Comparator<Individual> BEST_FIRST =
        Comparator.comparingDouble(Individual::fitness).reversed();

    private final Problem<?> problem;

    // P, population size (rozmiar populacji)
    private final int populationSize;

    // e, number of elites (elita): the e best individuals go to the next generation
    // unchanged, so the best one found so far is never lost. 0 = no elitism (task 1).
    private final int elites;

    private final Selector selector;

    // applied in this order, usually crossover then mutation
    private final List<GeneticOperator> operators;

    private final long seed;

    private Engine(final Builder builder) {
        this.problem = builder.problem;
        this.populationSize = builder.populationSize;
        this.elites = builder.elites;
        this.selector = builder.selector;
        this.operators = List.copyOf(builder.operators);
        this.seed = builder.seed;
    }

    // Infinite stream of generations 0, 1, 2, ...; the stop condition (warunek stopu)
    // is set by the caller with limit(G + 1).
    // One Random per run, so the same seed always gives the same run.
    public Stream<EvolutionResult> stream() {
        final Random random = new Random(this.seed);
        return Stream.iterate(this.initial(random), previous -> this.evolve(previous, random));
    }

    private EvolutionResult initial(final Random random) {
        final List<Individual> population = new ArrayList<>(this.populationSize);
        for (int idx = 0; idx < this.populationSize; ++idx) {
            population.add(this.problem.evaluate(this.problem.codec().random(random)));
        }
        return new EvolutionResult(0, population);
    }

    private EvolutionResult evolve(final EvolutionResult previous, final Random random) {
        final List<Individual> parents =
            this.selector.select(previous.population(), this.populationSize, random);
        List<Chromosome> chromosomes = parents.stream()
            .map(Individual::chromosome)
            .toList();
        for (final GeneticOperator operator : this.operators) {
            chromosomes = operator.apply(chromosomes, random);
        }
        final List<Individual> children = chromosomes.stream()
            .map(this.problem::evaluate)
            .toList();
        return new EvolutionResult(previous.generation() + 1,
            this.withElites(previous.population(), children));
    }

    // Elitism (selekcja elitarna): the e best parents take the place of the e worst
    // children. Ranked by fitness, like in selection, so an elite may be infeasible.
    private List<Individual> withElites(final List<Individual> parents,
        final List<Individual> children) {
        final List<Individual> next;
        if (this.elites == 0) {
            next = children;
        } else {
            next = new ArrayList<>(children.size());
            children.stream()
                .sorted(Engine.BEST_FIRST)
                .limit(children.size() - this.elites)
                .forEach(next::add);
            parents.stream()
                .sorted(Engine.BEST_FIRST)
                .limit(this.elites)
                .forEach(next::add);
        }
        return next;
    }

    public static final class Builder {

        private static final int DEFAULT_POPULATION = 30;

        private static final int DEFAULT_TOURNAMENT = 2;

        private final Problem<?> problem;

        private final List<GeneticOperator> operators = new ArrayList<>();

        private int populationSize = Builder.DEFAULT_POPULATION;

        private int elites;

        private Selector selector = new TournamentSelector(Builder.DEFAULT_TOURNAMENT);

        private long seed;

        public Builder(final Problem<?> problem) {
            this.problem = problem;
        }

        public Builder populationSize(final int size) {
            if (size < 2) {
                throw new IllegalArgumentException("Population size must be at least 2: " + size);
            }
            this.populationSize = size;
            return this;
        }

        public Builder elites(final int count) {
            if (count < 0) {
                throw new IllegalArgumentException(
                    "Number of elites must not be negative: " + count);
            }
            this.elites = count;
            return this;
        }

        public Builder selector(final Selector method) {
            this.selector = method;
            return this;
        }

        public Builder operators(final GeneticOperator... list) {
            this.operators.clear();
            this.operators.addAll(List.of(list));
            return this;
        }

        public Builder seed(final long value) {
            this.seed = value;
            return this;
        }

        public Engine build() {
            if (this.elites >= this.populationSize) {
                throw new IllegalArgumentException("Number of elites " + this.elites
                    + " must be smaller than population size " + this.populationSize);
            }
            return new Engine(this);
        }
    }
}
