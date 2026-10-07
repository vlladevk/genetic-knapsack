package pl.pwr.tio.mkp.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

/**
 * Series of the comparative study (badania porównawcze).
 *
 * <p>Every series changes one parameter and keeps the others at the base values:
 * P = 30, G = 1000, tournament of 2, pc = 0.9, pm = 1/n, linear penalty.</p>
 * <ul>
 *   <li>A: probability of crossover pc,</li>
 *   <li>B: probability of mutation pm,</li>
 *   <li>C: population size P,</li>
 *   <li>D: penalty function (linear penalty against death penalty).</li>
 * </ul>
 */
public final class ExperimentPlan {

    /**
     * Base population size, small as required by the task.
     */
    public static final int POPULATION = 30;

    /**
     * Number of generations after the initial one.
     */
    public static final int GENERATIONS = 1000;

    /**
     * Tournament size.
     */
    public static final int TOURNAMENT = 2;

    /**
     * Base probability of crossover.
     */
    public static final double CROSSOVER = 0.9;

    /**
     * Probabilities of crossover compared in series A.
     */
    private static final double[] CROSSOVERS = {0.0, 0.5, 0.7, 0.9, 1.0};

    /**
     * Probabilities of mutation compared in series B, as multiples of 1/n.
     */
    private static final double[] MUTATIONS_PER_N = {0.0, 0.5, 1.0, 2.0, 5.0, 10.0};

    /**
     * Population sizes compared in series C.
     */
    private static final int[] POPULATIONS = {10, 30, 50};

    /**
     * All configurations for an instance.
     *
     * @param instance Instance, its size sets the base probability of mutation 1/n
     * @return Configurations of all series
     */
    public List<RunConfig> configs(final KnapsackInstance instance) {
        final double mutation = 1.0 / instance.size();
        final List<RunConfig> configs = new ArrayList<>();
        for (final double crossover : ExperimentPlan.CROSSOVERS) {
            configs.add(ExperimentPlan.config("A-pc", "pc=" + crossover, ExperimentPlan.POPULATION,
                crossover, mutation, PenaltyType.LINEAR));
        }
        for (final double multiple : ExperimentPlan.MUTATIONS_PER_N) {
            configs.add(ExperimentPlan.config("B-pm",
                String.format(Locale.ROOT, "pm=%s/n", ExperimentPlan.trim(multiple)),
                ExperimentPlan.POPULATION, ExperimentPlan.CROSSOVER, multiple * mutation,
                PenaltyType.LINEAR));
        }
        for (final int population : ExperimentPlan.POPULATIONS) {
            configs.add(ExperimentPlan.config("C-population", "P=" + population, population,
                ExperimentPlan.CROSSOVER, mutation, PenaltyType.LINEAR));
        }
        for (final PenaltyType penalty : PenaltyType.values()) {
            configs.add(ExperimentPlan.config("D-penalty",
                "penalty=" + penalty.name().toLowerCase(Locale.ROOT), ExperimentPlan.POPULATION,
                ExperimentPlan.CROSSOVER, mutation, penalty));
        }
        return configs;
    }

    /**
     * Configuration with the base number of generations and tournament size.
     *
     * @param series Series
     * @param label Label
     * @param population Population size
     * @param crossover Probability of crossover
     * @param mutation Probability of mutation
     * @param penalty Penalty function
     * @return Configuration
     */
    private static RunConfig config(final String series, final String label,
        final int population, final double crossover, final double mutation,
        final PenaltyType penalty) {
        return new RunConfig(series, label, population, ExperimentPlan.GENERATIONS,
            ExperimentPlan.TOURNAMENT, crossover, mutation, penalty);
    }

    /**
     * Number without a trailing ".0", for labels.
     *
     * @param number Number
     * @return Text
     */
    private static String trim(final double number) {
        final String text = String.valueOf(number);
        return text.replaceFirst("\\.0$", "");
    }
}
