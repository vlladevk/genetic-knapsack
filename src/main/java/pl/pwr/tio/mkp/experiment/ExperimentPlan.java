package pl.pwr.tio.mkp.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

// Experiments: each series changes one parameter.
// Base: P = 30, G = 1000, tournament 2, pc = 0.9, pm = 1/n, linear penalty.
// A: pc, B: pm, C: P, D: linear vs death penalty.
public final class ExperimentPlan {

    // small, as the task requires
    public static final int POPULATION = 30;

    // without generation 0
    // with 500 the best value was still improving at the end
    public static final int GENERATIONS = 1000;

    // k = 2: the weakest pressure that still prefers better individuals. With a small
    // population a stronger one quickly fills it with copies of one individual.
    public static final int TOURNAMENT = 2;

    // typical pc is 0.6..1.0: most pairs are crossed, a few parents pass unchanged
    public static final double CROSSOVER = 0.9;

    private static final double[] CROSSOVERS = {0.0, 0.5, 0.7, 0.9, 1.0};

    // multiples of 1/n (= 1 flipped gene per child on average), so "pm=2/n" means
    // the same for n = 50 and n = 100
    private static final double[] MUTATIONS_PER_N = {0.0, 0.5, 1.0, 2.0, 5.0, 10.0};

    private static final int[] POPULATIONS = {10, 30, 50};

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

    private static RunConfig config(final String series, final String label,
        final int population, final double crossover, final double mutation,
        final PenaltyType penalty) {
        return new RunConfig(series, label, population, ExperimentPlan.GENERATIONS,
            ExperimentPlan.TOURNAMENT, crossover, mutation, penalty);
    }

    private static String trim(final double number) {
        final String text = String.valueOf(number);
        return text.replaceFirst("\\.0$", "");
    }
}
