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

public final class CsvWriter implements Closeable {

    private static final String SEPARATOR = ",";

    private final BufferedWriter writer;

    private final int columns;

    public CsvWriter(final Path file, final String... header) throws IOException {
        this.writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        this.columns = header.length;
        this.line((Object[]) header);
    }

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

    private void line(final Object... cells) throws IOException {
        final StringJoiner joiner = new StringJoiner(CsvWriter.SEPARATOR);
        for (final Object cell : cells) {
            joiner.add(CsvWriter.format(cell));
        }
        this.writer.write(joiner.toString());
        this.writer.newLine();
    }

    // plain numbers with a dot (no 1.0E-4), empty OptionalDouble = empty cell
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
