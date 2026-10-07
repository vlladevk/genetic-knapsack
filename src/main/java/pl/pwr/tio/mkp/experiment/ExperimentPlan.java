package pl.pwr.tio.mkp.experiment;

import java.util.List;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

// Table of all experiments: one line = one configuration.
public final class ExperimentPlan {

    // without generation 0
    // with 500 the best value was still improving at the end
    private static final int GENERATIONS = 1000;

    // k = 2: the weakest pressure that still prefers better individuals. With a small
    // population a stronger one quickly fills it with copies of one individual.
    private static final int TOURNAMENT = 2;

    // Base: P = 30 (small, as the task requires), pc = 0.9 (typical pc is 0.6..1.0),
    // pm = 1/n, linear penalty. Each series changes one of them.
    // pm is written as a multiple of 1/n (1/n = 1 flipped gene per child on average),
    // so "pm=2/n" means the same for n = 50 and n = 100.
    private static final List<Row> ROWS = List.of(
        //      series          label             P   pc   pm*n  penalty
        new Row("A-pc",         "pc=0.0",         30, 0.0, 1,    PenaltyType.LINEAR),
        new Row("A-pc",         "pc=0.5",         30, 0.5, 1,    PenaltyType.LINEAR),
        new Row("A-pc",         "pc=0.7",         30, 0.7, 1,    PenaltyType.LINEAR),
        new Row("A-pc",         "pc=0.9",         30, 0.9, 1,    PenaltyType.LINEAR),
        new Row("A-pc",         "pc=1.0",         30, 1.0, 1,    PenaltyType.LINEAR),

        new Row("B-pm",         "pm=0/n",         30, 0.9, 0,    PenaltyType.LINEAR),
        new Row("B-pm",         "pm=0.5/n",       30, 0.9, 0.5,  PenaltyType.LINEAR),
        new Row("B-pm",         "pm=1/n",         30, 0.9, 1,    PenaltyType.LINEAR),
        new Row("B-pm",         "pm=2/n",         30, 0.9, 2,    PenaltyType.LINEAR),
        new Row("B-pm",         "pm=5/n",         30, 0.9, 5,    PenaltyType.LINEAR),
        new Row("B-pm",         "pm=10/n",        30, 0.9, 10,   PenaltyType.LINEAR),

        new Row("C-population", "P=10",           10, 0.9, 1,    PenaltyType.LINEAR),
        new Row("C-population", "P=30",           30, 0.9, 1,    PenaltyType.LINEAR),
        new Row("C-population", "P=50",           50, 0.9, 1,    PenaltyType.LINEAR),

        new Row("D-penalty",    "penalty=linear", 30, 0.9, 1,    PenaltyType.LINEAR),
        new Row("D-penalty",    "penalty=death",  30, 0.9, 1,    PenaltyType.DEATH)
    );

    // pm becomes a real probability only here, when n of the instance is known
    public List<RunConfig> configs(final KnapsackInstance instance) {
        final double perGene = 1.0 / instance.size();
        return ExperimentPlan.ROWS.stream()
            .map(row -> new RunConfig(row.series(), row.label(), row.population(),
                ExperimentPlan.GENERATIONS, ExperimentPlan.TOURNAMENT, row.crossover(),
                row.mutationPerN() * perGene, row.penalty()))
            .toList();
    }

    private record Row(
        String series,
        String label,
        int population,
        double crossover,
        double mutationPerN,
        PenaltyType penalty
    ) {
    }
}
