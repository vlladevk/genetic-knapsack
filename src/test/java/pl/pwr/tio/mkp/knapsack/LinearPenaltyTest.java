package pl.pwr.tio.mkp.knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pl.pwr.tio.mkp.genetic.Chromosome;

// Why rho = max value / weight, on the toy example (capacity 10 kg and 10 l).
final class LinearPenaltyTest {

    private static final double DELTA = 1e-9;

    private static final double OPTIMUM = 23;

    private static KnapsackInstance toy;

    @BeforeAll
    static void readToy() throws IOException {
        toy = new CsvInstanceReader().read(Path.of("data", "toy.csv"), OPTIMUM);
    }

    // value / weight:  tent 2 | 1.67, food 2.67 | 4, pot 2 | 1.33,
    //                  torch 5 | 5, book 1 | 2, guitar 0.75 | 0.43  -> max = 5
    @Test
    void rhoIsLargestValuePerUnitOfWeight() {
        assertEquals(5, LinearPenalty.of(toy).coefficient(), DELTA);
    }

    @ParameterizedTest(name = "{0}: value {1}, violation {2} -> fitness {3}")
    @CsvSource(textBlock = """
        # chromosome, value, violation (kg + l over 10), fitness = value - 5 * violation
        110100,       23,    0,                          23
        111100,       27,    3,                          12
        111110,       29,    6,                          -1
        111111,       32,    17,                         -53
        """)
    void fitnessTable(final String bits, final double value, final double violation,
        final double fitness) {
        final KnapsackProblem problem = new KnapsackProblem(toy, LinearPenalty.of(toy));
        final Chromosome chromosome = Chromosome.parse(bits);
        assertEquals(value, problem.value(problem.codec().decode(chromosome)), DELTA);
        assertEquals(violation, problem.evaluate(chromosome).violation(), DELTA);
        assertEquals(fitness, problem.evaluate(chromosome).fitness(), DELTA);
    }

    // rho = 1 is too small: 111100 does not fit but gets 27 - 1 * 3 = 24 > 23
    @Test
    void smallRhoLetsOverloadedSolutionBeatOptimum() {
        final KnapsackProblem problem = new KnapsackProblem(toy, new LinearPenalty(1));
        assertTrue(problem.evaluate(Chromosome.parse("111100")).fitness() > OPTIMUM);
    }

    // rho = 5: no overloaded solution is better than the optimum
    @Test
    void rhoOfInstanceKeepsOptimumBest() {
        final KnapsackProblem problem = new KnapsackProblem(toy, LinearPenalty.of(toy));
        for (int mask = 0; mask < 1 << toy.size(); ++mask) {
            final StringBuilder bits = new StringBuilder();
            for (int gene = toy.size() - 1; gene >= 0; --gene) {
                bits.append((mask >> gene) & 1);
            }
            assertTrue(problem.evaluate(Chromosome.parse(bits.toString())).fitness()
                <= OPTIMUM, bits.toString());
        }
    }
}
