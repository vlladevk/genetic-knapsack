package pl.pwr.tio.mkp.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

// Experiments of task 2. Base = the best of task 1 that does not need more fitness
// evaluations: P = 30, G = 1000, tournament k = 2, no elites, one-point crossover
// pc = 0.9, bit-flip pm = 1/n, linear penalty. Every configuration gets the same budget
// of P * (G + 1) evaluations.
//   E  selection x elitism        (each method with and without one elite)
//   F  crossover                  (type, and pc for uniform: does pc matter now?)
//   G  mutation                   (bit-flip, inversion, both; p of inversion)
//   H  all combinations           3 selections x 2 elitism x 3 crossovers x 3 mutations
public final class TaskTwoPlan {

    private static final int POPULATION = 30;

    private static final int GENERATIONS = 1000;

    private static final double PC = 0.9;

    // pm of bit-flip as a multiple of 1/n
    private static final double PM = 1;

    // p of inversion per chromosome when it is used together with other factors
    private static final double INVERSION = 0.3;

    private static final SelectionType T = SelectionType.TOURNAMENT;

    private static final SelectionType R = SelectionType.ROULETTE;

    private static final SelectionType K = SelectionType.RANK;

    private static final CrossoverType ONE = CrossoverType.SINGLE_POINT;

    private static final CrossoverType TWO = CrossoverType.TWO_POINT;

    private static final CrossoverType UNI = CrossoverType.UNIFORM;

    private static final MutationType BIT = MutationType.BIT_FLIP;

    private static final MutationType INV = MutationType.INVERSION;

    private static final MutationType BOTH = MutationType.BOTH;

    private static final List<Row> ROWS = List.of(
        //      series         label                  sel e  crossover pc   mutation pm*n p_inv
        new Row("E-selection", "tournament",          T, 0, ONE, 0.9, BIT,  1, 0),
        new Row("E-selection", "tournament+elite",    T, 1, ONE, 0.9, BIT,  1, 0),
        new Row("E-selection", "roulette",            R, 0, ONE, 0.9, BIT,  1, 0),
        new Row("E-selection", "roulette+elite",      R, 1, ONE, 0.9, BIT,  1, 0),
        new Row("E-selection", "rank",                K, 0, ONE, 0.9, BIT,  1, 0),
        new Row("E-selection", "rank+elite",          K, 1, ONE, 0.9, BIT,  1, 0),

        new Row("F-crossover", "none (pc=0)",         T, 0, ONE, 0.0, BIT,  1, 0),
        new Row("F-crossover", "one-point",           T, 0, ONE, 0.9, BIT,  1, 0),
        new Row("F-crossover", "two-point",           T, 0, TWO, 0.9, BIT,  1, 0),
        new Row("F-crossover", "uniform",             T, 0, UNI, 0.9, BIT,  1, 0),
        new Row("F-crossover", "uniform pc=0.5",      T, 0, UNI, 0.5, BIT,  1, 0),
        new Row("F-crossover", "uniform pc=1.0",      T, 0, UNI, 1.0, BIT,  1, 0),

        new Row("G-mutation",  "bit-flip",            T, 0, ONE, 0.9, BIT,  1, 0),
        new Row("G-mutation",  "inversion p=0.1",     T, 0, ONE, 0.9, INV,  0, 0.1),
        new Row("G-mutation",  "inversion p=0.3",     T, 0, ONE, 0.9, INV,  0, 0.3),
        new Row("G-mutation",  "inversion p=0.6",     T, 0, ONE, 0.9, INV,  0, 0.6),
        new Row("G-mutation",  "bit-flip+inv p=0.1",  T, 0, ONE, 0.9, BOTH, 1, 0.1),
        new Row("G-mutation",  "bit-flip+inv p=0.3",  T, 0, ONE, 0.9, BOTH, 1, 0.3)
    );

    // series H is generated: every combination of the factors, the rest as in the base
    public List<RunConfig> configs(final KnapsackInstance instance) {
        final List<Row> rows = new ArrayList<>(TaskTwoPlan.ROWS);
        for (final SelectionType selection : SelectionType.values()) {
            for (final int elites : new int[] {0, 1}) {
                for (final CrossoverType crossover : CrossoverType.values()) {
                    for (final MutationType mutation : MutationType.values()) {
                        rows.add(new Row("H-all", TaskTwoPlan.name(selection, elites, crossover,
                            mutation), selection, elites, crossover, TaskTwoPlan.PC, mutation,
                            TaskTwoPlan.PM, TaskTwoPlan.INVERSION));
                    }
                }
            }
        }
        final double perGene = 1.0 / instance.size();
        return rows.stream()
            .map(row -> new RunConfig(row.series(), row.label(), TaskTwoPlan.POPULATION,
                TaskTwoPlan.GENERATIONS, row.selection(), row.elites(), row.crossoverType(),
                row.crossover(), row.mutationType(), row.mutationPerN() * perGene,
                row.inversion(), PenaltyType.LINEAR))
            .toList();
    }

    // e.g. "rank+e1 uniform bit-flip+inv"
    private static String name(final SelectionType selection, final int elites,
        final CrossoverType crossover, final MutationType mutation) {
        final String crossing = switch (crossover) {
            case SINGLE_POINT -> "one-point";
            case TWO_POINT -> "two-point";
            case UNIFORM -> "uniform";
        };
        final String mutating = switch (mutation) {
            case BIT_FLIP -> "bit-flip";
            case INVERSION -> "inversion";
            case BOTH -> "bit-flip+inv";
        };
        return selection.name().toLowerCase(Locale.ROOT) + "+e" + elites + " "
            + crossing + " " + mutating;
    }

    private record Row(
        String series,
        String label,
        SelectionType selection,
        int elites,
        CrossoverType crossoverType,
        double crossover,
        MutationType mutationType,
        double mutationPerN,
        double inversion
    ) {
    }
}
