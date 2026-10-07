package pl.pwr.tio.mkp.knapsack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

// Reads OR-Library files: count, then per problem: n m optimum, n values,
// m rows of n weights, m capacities. Optimum 0 = unknown.
public final class OrLibraryReader {

    private static final String COMMENT = "#";

    // instances are named <file>-<number>, from 0
    public List<KnapsackInstance> read(final Path file) throws IOException {
        final Tokens tokens = new Tokens(Files.readString(file, StandardCharsets.UTF_8));
        final String prefix = file.getFileName().toString().replaceFirst("\\.[^.]*$", "");
        final int count = tokens.nextInt();
        final List<KnapsackInstance> instances = new ArrayList<>(count);
        for (int number = 0; number < count; ++number) {
            instances.add(OrLibraryReader.instance(tokens, prefix + "-" + number));
        }
        return instances;
    }

    // best: one value per line in the same order, lines with # are skipped
    public List<KnapsackInstance> read(final Path file, final Path best) throws IOException {
        final List<KnapsackInstance> instances = this.read(file);
        final List<String> values = Files.readAllLines(best, StandardCharsets.UTF_8).stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty() && !line.startsWith(OrLibraryReader.COMMENT))
            .toList();
        if (values.size() != instances.size()) {
            throw new IllegalArgumentException("Expected " + instances.size()
                + " best known values in " + best + ", found " + values.size());
        }
        final List<KnapsackInstance> result = new ArrayList<>(instances.size());
        for (int idx = 0; idx < instances.size(); ++idx) {
            result.add(instances.get(idx).withOptimum(Double.parseDouble(values.get(idx))));
        }
        return result;
    }

    private static KnapsackInstance instance(final Tokens tokens, final String name) {
        final int size = tokens.nextInt();
        final int dimensions = tokens.nextInt();
        final double optimum = tokens.nextDouble();
        final double[] values = tokens.nextDoubles(size);
        final double[][] weights = new double[size][dimensions];
        for (int constraint = 0; constraint < dimensions; ++constraint) {
            for (int item = 0; item < size; ++item) {
                weights[item][constraint] = tokens.nextDouble();
            }
        }
        final double[] capacities = tokens.nextDoubles(dimensions);
        final List<Item> items = new ArrayList<>(size);
        for (int item = 0; item < size; ++item) {
            items.add(new Item(item, values[item], weights[item]));
        }
        final OptionalDouble known;
        if (optimum > 0) {
            known = OptionalDouble.of(optimum);
        } else {
            known = OptionalDouble.empty();
        }
        return new KnapsackInstance(name, items, capacities, known);
    }

    private static final class Tokens {

        private final String[] all;

        private int position;

        Tokens(final String text) {
            this.all = text.trim().split("\\s+");
        }

        int nextInt() {
            return Integer.parseInt(this.next());
        }

        double nextDouble() {
            return Double.parseDouble(this.next());
        }

        double[] nextDoubles(final int count) {
            final double[] numbers = new double[count];
            for (int idx = 0; idx < count; ++idx) {
                numbers[idx] = this.nextDouble();
            }
            return numbers;
        }

        private String next() {
            if (this.position >= this.all.length) {
                throw new IllegalStateException("Unexpected end of file");
            }
            final String token = this.all[this.position];
            this.position += 1;
            return token;
        }
    }
}
