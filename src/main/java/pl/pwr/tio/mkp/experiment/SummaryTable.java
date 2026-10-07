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

// Summary per configuration: feasible runs, then over them mean, std, min, max,
// hits of the optimum and gap.
// gap = how far the mean result is from the optimum, in % of the optimum:
//   gap = (optimum - mean) / optimum * 100,  e.g. (16537 - 16387) / 16537 = 0.91%
public final class SummaryTable {

    // closer than this = equal to the optimum
    private static final double TOLERANCE = 1e-6;

    private static final double PERCENT = 100.0;

    public void print(final KnapsackInstance instance, final List<RunOutcome> outcomes,
        final PrintStream out) {
        final Map<String, List<RunOutcome>> groups = new LinkedHashMap<>();
        for (final RunOutcome outcome : outcomes) {
            groups.computeIfAbsent(outcome.config().series() + " " + outcome.config().label(),
                key -> new ArrayList<>()).add(outcome);
        }
        final double optimum = instance.optimum();
        out.printf(Locale.ROOT, "%n%s, optimum %s%n", instance, optimum);
        out.printf(Locale.ROOT, "%-26s %8s %10s %9s %10s %10s %6s %7s%n",
            "configuration", "feasible", "mean", "std", "min", "max", "hits", "gap %");
        for (final Map.Entry<String, List<RunOutcome>> group : groups.entrySet()) {
            out.println(SummaryTable.line(group.getKey(), group.getValue(), optimum));
        }
    }

    private static String line(final String name, final List<RunOutcome> runs,
        final double optimum) {
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
            final String hits = Arrays.stream(values)
                .filter(value -> value >= optimum - SummaryTable.TOLERANCE)
                .count() + "/" + runs.size();
            final String gap = String.format(Locale.ROOT, "%.2f",
                SummaryTable.PERCENT * (optimum - mean) / optimum);
            line = String.format(Locale.ROOT, "%-26s %8s %10.1f %9.1f %10.1f %10.1f %6s %7s",
                name, feasible, mean, SummaryTable.deviation(values, mean), min, max, hits, gap);
        }
        return line;
    }

    // sample standard deviation (divide by n - 1): 10 runs are only a sample
    // of all possible runs
    private static double deviation(final double[] values, final double mean) {
        double squares = 0;
        for (final double value : values) {
            squares += (value - mean) * (value - mean);
        }
        return Math.sqrt(squares / Math.max(1, values.length - 1));
    }
}
