package pl.pwr.tio.mkp.experiment;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalDouble;
import java.util.StringJoiner;

/**
 * Minimal writer of comma separated values. Numbers use a dot as the decimal separator,
 * an empty {@link OptionalDouble} is written as an empty cell.
 */
public final class CsvWriter implements Closeable {

    /**
     * Separator of cells.
     */
    private static final String SEPARATOR = ",";

    /**
     * Underlying writer.
     */
    private final BufferedWriter writer;

    /**
     * Number of cells in every row.
     */
    private final int columns;

    /**
     * Creates the file (replacing an existing one) and writes the header.
     *
     * @param file File to create
     * @param header Names of the columns
     * @throws IOException If the file can not be written
     */
    public CsvWriter(final Path file, final String... header) throws IOException {
        this.writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        this.columns = header.length;
        this.line((Object[]) header);
    }

    /**
     * Writes one row.
     *
     * @param cells Cells, as many as columns in the header
     * @throws IOException If the file can not be written
     */
    public void row(final Object... cells) throws IOException {
        if (cells.length != this.columns) {
            throw new IllegalArgumentException("Expected " + this.columns + " cells, got "
                + cells.length);
        }
        this.line(cells);
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    /**
     * Writes one line.
     *
     * @param cells Cells
     * @throws IOException If the file can not be written
     */
    private void line(final Object... cells) throws IOException {
        final StringJoiner joiner = new StringJoiner(CsvWriter.SEPARATOR);
        for (final Object cell : cells) {
            joiner.add(CsvWriter.format(cell));
        }
        this.writer.write(joiner.toString());
        this.writer.newLine();
    }

    /**
     * Text of one cell.
     *
     * @param cell Cell
     * @return Text
     */
    private static String format(final Object cell) {
        final String text;
        if (cell instanceof OptionalDouble optional) {
            if (optional.isPresent()) {
                text = CsvWriter.format(optional.getAsDouble());
            } else {
                text = "";
            }
        } else if (cell instanceof Double number && Double.isFinite(number)) {
            text = BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
        } else {
            text = String.valueOf(cell);
        }
        return text;
    }
}
