package pl.pwr.tio.mkp.experiment;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

/**
 * Prints a short summary of the experiments. For every configuration: in how many runs
 * a feasible solution was found, then over those runs the mean, standard deviation,
 * minimum and maximum of the best value, how many runs reached the optimum and the mean
 * relative gap to it (odchylenie od optimum).
 */
public final class SummaryTable {

    /**
     * Values closer to the optimum than this are treated as equal to it.
     */
    private static final double TOLERANCE = 1e-6;

    /**
     * Factor turning a fraction into percent.
     */
    private static final double PERCENT = 100.0;

    /**
     * Prints the summary.
     *
     * @param instance Instance the runs were made on
     * @param outcomes Outcomes of all runs, in the order of configurations
     * @param out Where to print
     */
    public void print(final KnapsackInstance instance, final List<RunOutcome> outcomes,
        final PrintStream out) {
        final Map<String, List<RunOutcome>> groups = new LinkedHashMap<>();
        for (final RunOutcome outcome : outcomes) {
            groups.computeIfAbsent(outcome.config().series() + " " + outcome.config().label(),
                key -> new ArrayList<>()).add(outcome);
        }
        final OptionalDouble optimum = instance.optimum();
        out.printf(Locale.ROOT, "%n%s, optimum %s%n", instance,
            optimum.isPresent() ? String.valueOf(optimum.getAsDouble()) : "unknown");
        out.printf(Locale.ROOT, "%-26s %8s %10s %9s %10s %10s %6s %7s%n",
            "configuration", "feasible", "mean", "std", "min", "max", "hits", "gap %");
        for (final Map.Entry<String, List<RunOutcome>> group : groups.entrySet()) {
            out.println(SummaryTable.line(group.getKey(), group.getValue(), optimum));
        }
    }

    /**
     * One line of the summary.
     *
     * @param name Name of the configuration
     * @param runs Its runs
     * @param optimum Optimal or best known value
     * @return Formatted line
     */
    private static String line(final String name, final List<RunOutcome> runs,
        final OptionalDouble optimum) {
        final double[] values = runs.stream()
            .map(RunOutcome::best)
            .filter(OptionalDouble::isPresent)
            .mapToDouble(OptionalDouble::getAsDouble)
            .toArray();
        final String feasible = values.length + "/" + runs.size();
        final String line;
        if (values.length == 0) {
            line = String.format(Locale.ROOT, "%-26s %8s", name, feasible);
        } else {
            final double mean = Arrays.stream(values).average().orElseThrow();
            final double min = Arrays.stream(values).min().orElseThrow();
            final double max = Arrays.stream(values).max().orElseThrow();
            String hits = "-";
            String gap = "-";
            if (optimum.isPresent()) {
                final double opt = optimum.getAsDouble();
                hits = Arrays.stream(values)
                    .filter(value -> value >= opt - SummaryTable.TOLERANCE)
                    .count() + "/" + runs.size();
                gap = String.format(Locale.ROOT, "%.2f",
                    SummaryTable.PERCENT * (opt - mean) / opt);
            }
            line = String.format(Locale.ROOT, "%-26s %8s %10.1f %9.1f %10.1f %10.1f %6s %7s",
                name, feasible, mean, SummaryTable.deviation(values, mean), min, max, hits, gap);
        }
        return line;
    }

    /**
     * Sample standard deviation.
     *
     * @param values Values
     * @param mean Their mean
     * @return Standard deviation, {@code 0} for a single value
     */
    private static double deviation(final double[] values, final double mean) {
        double squares = 0;
        for (final double value : values) {
            squares += (value - mean) * (value - mean);
        }
        return Math.sqrt(squares / Math.max(1, values.length - 1));
    }
}
