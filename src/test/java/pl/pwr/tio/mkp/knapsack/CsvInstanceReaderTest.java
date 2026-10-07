package pl.pwr.tio.mkp.knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class CsvInstanceReaderTest {

    private static final double DELTA = 1e-9;

    @Test
    void readsToyExample() throws IOException {
        final KnapsackInstance toy =
            new CsvInstanceReader().read(Path.of("data", "toy.csv"), 23);
        assertEquals("toy", toy.name());
        assertEquals(6, toy.size());
        assertEquals(2, toy.dimensions());
        assertEquals(10, toy.capacity(0), DELTA);
        assertEquals(10, toy.capacity(1), DELTA);
        assertEquals(10, toy.item(0).value(), DELTA);
        assertEquals(5, toy.item(0).weight(0), DELTA);
        assertEquals(6, toy.item(0).weight(1), DELTA);
        assertEquals(7, toy.item(5).weight(1), DELTA);
        assertEquals(23, toy.optimum(), DELTA);
    }

    @Test
    void readsLargeInstance() throws IOException {
        final KnapsackInstance large =
            new CsvInstanceReader().read(Path.of("data", "mknapcb1-0.csv"), 24381);
        assertEquals(100, large.size());
        assertEquals(5, large.dimensions());
    }
}
