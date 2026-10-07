package pl.pwr.tio.mkp.knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.pwr.tio.mkp.genetic.Chromosome;
import pl.pwr.tio.mkp.genetic.Individual;

// Toy example: tent, food, pot, torch, book, guitar; capacity 10 kg and 10 l;
// optimum 23 = tent + food + torch.
final class KnapsackProblemTest {

    private static final double DELTA = 1e-9;

    private static KnapsackInstance toy;

    @BeforeAll
    static void readToy() throws IOException {
        toy = new OrLibraryReader().read(Path.of("data", "toy.txt")).get(0);
    }

    @Test
    void feasibleSolutionHasItsValueAsFitness() {
        final Individual best = new KnapsackProblem(toy, LinearPenalty.of(toy))
            .evaluate(Chromosome.parse("110100"));
        assertEquals(23, best.fitness(), DELTA);
        assertEquals(0, best.violation(), DELTA);
        assertTrue(best.feasible());
    }

    @Test
    void overloadedSolutionIsPenalized() {
        final Individual heavy = new KnapsackProblem(toy, LinearPenalty.of(toy))
            .evaluate(Chromosome.parse("111110"));
        assertEquals(6, heavy.violation(), DELTA);
        assertEquals(29 - 5 * 6, heavy.fitness(), DELTA);
        assertFalse(heavy.feasible());
    }

    @Test
    void rhoIsLargestValuePerUnitOfWeight() {
        assertEquals(5, LinearPenalty.of(toy).coefficient(), DELTA);
    }

    @Test
    void deathPenaltyRejectsEveryInfeasibleSolution() {
        final KnapsackProblem problem = new KnapsackProblem(toy, new DeathPenalty());
        assertEquals(-1, problem.evaluate(Chromosome.parse("111110")).fitness(), DELTA);
        assertEquals(-1, problem.evaluate(Chromosome.parse("111111")).fitness(), DELTA);
        assertEquals(23, problem.evaluate(Chromosome.parse("110100")).fitness(), DELTA);
    }

    @Test
    void decodesChosenItems() {
        assertEquals(
            java.util.List.of(toy.item(0), toy.item(1), toy.item(3)),
            new KnapsackCodec(toy).decode(Chromosome.parse("110100"))
        );
    }
}
