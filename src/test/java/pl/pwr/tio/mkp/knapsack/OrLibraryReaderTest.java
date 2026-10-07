package pl.pwr.tio.mkp.knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

final class OrLibraryReaderTest {

    private static final double DELTA = 1e-9;

    @Test
    void readsAllProblemsOfMknap1() throws IOException {
        final List<KnapsackInstance> all = new OrLibraryReader().read(Path.of("data", "mknap1.txt"));
        assertEquals(7, all.size());
        assertEquals(List.of(6, 10, 15, 20, 28, 39, 50),
            all.stream().map(KnapsackInstance::size).toList());
        final KnapsackInstance first = all.get(0);
        assertEquals("mknap1-0", first.name());
        assertEquals(10, first.dimensions());
        assertEquals(3800, first.optimum().orElseThrow(), DELTA);
        assertEquals(100, first.item(0).value(), DELTA);
        assertEquals(8, first.item(0).weight(0), DELTA);
        assertEquals(80, first.capacity(0), DELTA);
        assertEquals(24, first.capacity(9), DELTA);
    }

    @Test
    void readsBestKnownValues() throws IOException {
        final List<KnapsackInstance> all = new OrLibraryReader().read(
            Path.of("data", "mknapcb1.txt"), Path.of("data", "mknapcb1-best.txt")
        );
        assertEquals(30, all.size());
        assertEquals(100, all.get(0).size());
        assertEquals(5, all.get(0).dimensions());
        assertEquals(24381, all.get(0).optimum().orElseThrow(), DELTA);
        assertTrue(all.stream().allMatch(instance -> instance.optimum().isPresent()));
    }
}
