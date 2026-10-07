package pl.pwr.tio.mkp.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import pl.pwr.tio.mkp.alterer.Alterer;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.genetic.Problem;
import pl.pwr.tio.mkp.selector.Selector;
import pl.pwr.tio.mkp.selector.TournamentSelector;

/**
 * Engine of the basic genetic algorithm (podstawowy algorytm genetyczny).
 *
 * <p>One generation: selection of {@code P} parents, then every operator in order
 * (crossover, then mutation), then evaluation. The children replace the whole
 * population (no elitism). Generations form an infinite stream, the stop condition
 * (warunek stopu) is applied by the caller, for example {@code stream().limit(G + 1)}.</p>
 *
 * <p>All randomness of a run comes from one generator created from the seed,
 * so a run with the same seed and parameters always gives the same result.</p>
 *
 * @param <T> Type of a solution of the problem
 */
public final class Engine<T> {

    /**
     * Problem to solve.
     */
    private final Problem<T> problem;

    /**
     * Number of individuals in every generation (P).
     */
    private final int populationSize;

    /**
     * Selection method.
     */
    private final Selector selector;

    /**
     * Operators applied in order to the selected parents.
     */
    private final List<Alterer> alterers;

    /**
     * Seed of the random number generator (ziarno generatora).
     */
    private final long seed;

    /**
     * Ctor.
     *
     * @param builder Builder with the parameters
     */
    private Engine(final Builder<T> builder) {
        this.problem = builder.problem;
        this.populationSize = builder.populationSize;
        this.selector = builder.selector;
        this.alterers = List.copyOf(builder.alterers);
        this.seed = builder.seed;
    }

    /**
     * Infinite stream of generations, starting with the initial population.
     * Every call starts a new, independent run with the same seed.
     *
     * @return Generations 0, 1, 2, ...
     */
    public Stream<EvolutionResult> stream() {
        final Random random = new Random(this.seed);
        return Stream.iterate(this.initial(random), previous -> this.evolve(previous, random));
    }

    /**
     * Random initial population.
     *
     * @param random Random number generator of the run
     * @return Generation 0
     */
    private EvolutionResult initial(final Random random) {
        final List<Individual> population = new ArrayList<>(this.populationSize);
        for (int idx = 0; idx < this.populationSize; ++idx) {
            population.add(this.problem.evaluate(this.problem.codec().random(random)));
        }
        return new EvolutionResult(0, population);
    }

    /**
     * Creates the next generation.
     *
     * @param previous Previous generation
     * @param random Random number generator of the run
     * @return Next generation
     */
    private EvolutionResult evolve(final EvolutionResult previous, final Random random) {
        final List<Individual> parents =
            this.selector.select(previous.population(), this.populationSize, random);
        List<Chromosome> chromosomes = new ArrayList<>(parents.size());
        for (final Individual parent : parents) {
            chromosomes.add(parent.chromosome());
        }
        for (final Alterer alterer : this.alterers) {
            chromosomes = alterer.alter(chromosomes, random);
        }
        final List<Individual> children = new ArrayList<>(chromosomes.size());
        for (final Chromosome chromosome : chromosomes) {
            children.add(this.problem.evaluate(chromosome));
        }
        return new EvolutionResult(previous.generation() + 1, children);
    }

    /**
     * Builder of the engine.
     *
     * @param <T> Type of a solution of the problem
     */
    public static final class Builder<T> {

        /**
         * Default number of individuals in a generation.
         */
        private static final int DEFAULT_POPULATION = 30;

        /**
         * Default tournament size.
         */
        private static final int DEFAULT_TOURNAMENT = 2;

        /**
         * Problem to solve.
         */
        private final Problem<T> problem;

        /**
         * Operators applied in order.
         */
        private final List<Alterer> alterers = new ArrayList<>();

        /**
         * Number of individuals in a generation.
         */
        private int populationSize = Builder.DEFAULT_POPULATION;

        /**
         * Selection method.
         */
        private Selector selector = new TournamentSelector(Builder.DEFAULT_TOURNAMENT);

        /**
         * Seed of the random number generator.
         */
        private long seed;

        /**
         * Ctor.
         *
         * @param problem Problem to solve
         */
        public Builder(final Problem<T> problem) {
            this.problem = problem;
        }

        /**
         * Sets the number of individuals in every generation.
         *
         * @param size Population size (P), at least 2
         * @return This builder
         */
        public Builder<T> populationSize(final int size) {
            if (size < 2) {
                throw new IllegalArgumentException("Population size must be at least 2: " + size);
            }
            this.populationSize = size;
            return this;
        }

        /**
         * Sets the selection method.
         *
         * @param method Selection method
         * @return This builder
         */
        public Builder<T> selector(final Selector method) {
            this.selector = method;
            return this;
        }

        /**
         * Sets the operators, applied in the given order.
         *
         * @param operators Operators, usually crossover and then mutation
         * @return This builder
         */
        public Builder<T> alterers(final Alterer... operators) {
            this.alterers.clear();
            this.alterers.addAll(List.of(operators));
            return this;
        }

        /**
         * Sets the seed of the random number generator.
         *
         * @param value Seed
         * @return This builder
         */
        public Builder<T> seed(final long value) {
            this.seed = value;
            return this;
        }

        /**
         * Creates the engine.
         *
         * @return Engine
         */
        public Engine<T> build() {
            return new Engine<>(this);
        }
    }
}
