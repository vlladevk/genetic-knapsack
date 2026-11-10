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
//   I  parameters of the winner of H (tournament + 1 elite, two-point, bit-flip):
//      number of elites, pm, pc - one at a time (table only, no plot)
//   J  three ways to choose the best combination, each with a reason, at the budget of
//      the final series:
//        A  winner of each of E, F, G taken one by one (n = 100)
//        B  winner of the heatmap H on n = 100
//        C  winner of the heatmap H on n = 50
//   Z  final: the best approach of J vs task 1 with the same, larger budget of about
//      50 000 evaluations (task 1 had its best result with P = 50, G = 1000)
public final class TaskTwoPlan {

    // budget of the final series: P = 50, G = 1000 -> 50 050 evaluations;
    // P = 30 gets G = 1667 -> 50 040
    private static final int FINAL_GENERATIONS_30 = 1667;

    private static final int FINAL_GENERATIONS_50 = 1000;

    private static final int FINAL_POPULATION_50 = 50;

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
        new Row("G-mutation",  "bit-flip+inv p=0.3",  T, 0, ONE, 0.9, BOTH, 1, 0.3),

        new Row("I-tuning",    "winner (e=1)",        T, 1, TWO, 0.9, BIT,  1, 0),
        new Row("I-tuning",    "e=2",                 T, 2, TWO, 0.9, BIT,  1, 0),
        new Row("I-tuning",    "e=5",                 T, 5, TWO, 0.9, BIT,  1, 0),
        new Row("I-tuning",    "pm=0.5/n",            T, 1, TWO, 0.9, BIT,  0.5, 0),
        new Row("I-tuning",    "pm=2/n",              T, 1, TWO, 0.9, BIT,  2, 0),
        new Row("I-tuning",    "pc=0.6",              T, 1, TWO, 0.6, BIT,  1, 0),
        new Row("I-tuning",    "pc=1.0",              T, 1, TWO, 1.0, BIT,  1, 0)
    );

    // series J and Z: label, population, generations, row with the methods
    private static final List<Final> FINAL = List.of(
        new Final("A: tournament+elite; one-point; inversion 0.3", 30,
            TaskTwoPlan.FINAL_GENERATIONS_30,
            new Row("J-approaches", "", T, 1, ONE, 0.9, INV, 0, 0.3)),
        new Final("B: tournament+elite; two-point; bit-flip 1/n", 30,
            TaskTwoPlan.FINAL_GENERATIONS_30,
            new Row("J-approaches", "", T, 1, TWO, 0.9, BIT, 1, 0)),
        new Final("C: rank+elite; uniform; bit-flip 1/n", 30,
            TaskTwoPlan.FINAL_GENERATIONS_30,
            new Row("J-approaches", "", K, 1, UNI, 0.9, BIT, 1, 0)),
        new Final("task 1: tournament; one-point; bit-flip 1/n; P=30", 30,
            TaskTwoPlan.FINAL_GENERATIONS_30,
            new Row("Z-final", "", T, 0, ONE, 0.9, BIT, 1, 0)),
        new Final("task 1: tournament; one-point; bit-flip 1/n; P=50",
            TaskTwoPlan.FINAL_POPULATION_50, TaskTwoPlan.FINAL_GENERATIONS_50,
            new Row("Z-final", "", T, 0, ONE, 0.9, BIT, 1, 0)),
        new Final("task 1: tournament; one-point; bit-flip 0.5/n; P=30", 30,
            TaskTwoPlan.FINAL_GENERATIONS_30,
            new Row("Z-final", "", T, 0, ONE, 0.9, BIT, 0.5, 0)),
        new Final("task 2: tournament+elite; two-point; bit-flip 1/n; P=30", 30,
            TaskTwoPlan.FINAL_GENERATIONS_30,
            new Row("Z-final", "", T, 1, TWO, 0.9, BIT, 1, 0)),
        new Final("task 2: tournament+elite; two-point; bit-flip 1/n; P=50",
            TaskTwoPlan.FINAL_POPULATION_50, TaskTwoPlan.FINAL_GENERATIONS_50,
            new Row("Z-final", "", T, 1, TWO, 0.9, BIT, 1, 0))
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
        final List<RunConfig> configs = new ArrayList<>();
        for (final Row row : rows) {
            configs.add(row.config(row.label(), TaskTwoPlan.POPULATION, TaskTwoPlan.GENERATIONS,
                instance));
        }
        for (final Final last : TaskTwoPlan.FINAL) {
            configs.add(last.row().config(last.label(), last.population(), last.generations(),
                instance));
        }
        return configs;
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

        RunConfig config(final String name, final int population, final int generations,
            final KnapsackInstance instance) {
            return new RunConfig(this.series, name, population, generations, this.selection,
                this.elites, this.crossoverType, this.crossover, this.mutationType,
                this.mutationPerN / instance.size(), this.inversion, PenaltyType.LINEAR);
        }
    }

    private record Final(String label, int population, int generations, Row row) {
    }
}
