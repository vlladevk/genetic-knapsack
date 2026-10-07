package pl.pwr.tio.mkp.knapsack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Reader of multidimensional knapsack instances in the OR-Library format.
 *
 * <p>Format: number of problems, then for every problem: {@code n m optimum},
 * {@code n} values, {@code m} rows of {@code n} weights, {@code m} capacities.
 * Optimum {@code 0} means that it is unknown.</p>
 */
public final class OrLibraryReader {

    /**
     * Prefix of comment lines in files with best known values.
     */
    private static final String COMMENT = "#";

    /**
     * Reads all instances from a file.
     * Instances are named {@code <file name>-<number>}, numbered from 0.
     *
     * @param file File in the OR-Library format
     * @return Instances in the order they appear in the file
     * @throws IOException If the file can not be read
     */
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

    /**
     * Reads instances and sets their best known values from a second file.
     * The second file has one value per line, in the same order as the instances;
     * lines starting with {@code #} are ignored.
     *
     * @param file File in the OR-Library format
     * @param best File with best known values
     * @return Instances with best known values
     * @throws IOException If a file can not be read
     */
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

    /**
     * Reads one instance.
     *
     * @param tokens Tokens positioned at the start of the instance
     * @param name Name of the instance
     * @return Instance
     */
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

    /**
     * Whitespace separated tokens of a file.
     */
    private static final class Tokens {

        /**
         * All tokens.
         */
        private final String[] all;

        /**
         * Position of the next token.
         */
        private int position;

        /**
         * Ctor.
         *
         * @param text Text to split
         */
        Tokens(final String text) {
            this.all = text.trim().split("\\s+");
        }

        /**
         * Next token as an integer.
         *
         * @return Integer
         */
        int nextInt() {
            return Integer.parseInt(this.next());
        }

        /**
         * Next token as a number.
         *
         * @return Number
         */
        double nextDouble() {
            return Double.parseDouble(this.next());
        }

        /**
         * Next tokens as numbers.
         *
         * @param count How many numbers to read
         * @return Numbers
         */
        double[] nextDoubles(final int count) {
            final double[] numbers = new double[count];
            for (int idx = 0; idx < count; ++idx) {
                numbers[idx] = this.nextDouble();
            }
            return numbers;
        }

        /**
         * Next token.
         *
         * @return Token
         */
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
