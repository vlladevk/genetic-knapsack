package pl.pwr.tio.mkp.knapsack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Reads one problem from CSV:
//   item,value,weight,volume    <- header
//   capacity,,10,10             <- capacity of every constraint
//   tent,10,5,6                 <- one row per item
// The instance is named after the file: data/toy.csv -> toy.
public final class CsvInstanceReader {

    // columns: 0 = item name (only for people), 1 = value, 2.. = weights
    private static final int FIRST_WEIGHT = 2;

    public KnapsackInstance read(final Path file, final double optimum) throws IOException {
        final List<String[]> rows = Files.readAllLines(file, StandardCharsets.UTF_8).stream()
            .skip(1)
            .map(line -> line.split(","))
            .toList();
        final double[] capacities = CsvInstanceReader.weights(rows.getFirst());
        final List<Item> items = new ArrayList<>(rows.size() - 1);
        for (final String[] row : rows.subList(1, rows.size())) {
            items.add(new Item(items.size(), Double.parseDouble(row[1]),
                CsvInstanceReader.weights(row)));
        }
        final String name = file.getFileName().toString().replace(".csv", "");
        return new KnapsackInstance(name, items, capacities, optimum);
    }

    private static double[] weights(final String[] row) {
        return Arrays.stream(row, CsvInstanceReader.FIRST_WEIGHT, row.length)
            .mapToDouble(Double::parseDouble)
            .toArray();
    }
}
