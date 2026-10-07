package pl.pwr.tio.mkp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import pl.pwr.tio.mkp.engine.EvolutionResult;
import pl.pwr.tio.mkp.experiment.ExperimentPlan;
import pl.pwr.tio.mkp.experiment.ExperimentRunner;
import pl.pwr.tio.mkp.experiment.PenaltyType;
import pl.pwr.tio.mkp.experiment.Plots;
import pl.pwr.tio.mkp.experiment.RunConfig;
import pl.pwr.tio.mkp.experiment.RunOutcome;
import pl.pwr.tio.mkp.experiment.SummaryTable;
import pl.pwr.tio.mkp.genetic.Individual;
import pl.pwr.tio.mkp.knapsack.CsvInstanceReader;
import pl.pwr.tio.mkp.knapsack.KnapsackCodec;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;

// Runs the toy example and all experiments.
// Args: --no-plots skips Python; -Dpython=python3 changes the Python command.
public final class Main {

    private static final Path DATA = Path.of("data");

    private static final Path RESULTS = Path.of("results");

    // GA is random, one run proves nothing: every configuration runs 10 times,
    // seeds 1..10, and the summary shows mean, std, min, max
    private static final int RUNS = 10;

    // Optima: 110100 = tent + food + torch for the toy; the other two come from
    // OR-Library. All three are proven by an exact solver: scripts/exact.py
    private static final double TOY_OPTIMUM = 23;

    private static final double MKNAP1_6_OPTIMUM = 16537;

    private static final double MKNAPCB1_0_OPTIMUM = 24381;

    private static final int TOY_POPULATION = 30;

    private static final int TOY_GENERATIONS = 50;

    private static final int TOY_TOURNAMENT = 2;

    private static final double TOY_CROSSOVER = 0.9;

    private static final String NO_PLOTS = "--no-plots";

    private Main() {
    }

    public static void main(final String[] args) throws IOException {
        final CsvInstanceReader reader = new CsvInstanceReader();
        final KnapsackInstance toy = reader.read(Main.DATA.resolve("toy.csv"), Main.TOY_OPTIMUM);
        Main.toy(toy);
        final List<KnapsackInstance> instances = List.of(
            reader.read(Main.DATA.resolve("mknap1-6.csv"), Main.MKNAP1_6_OPTIMUM),
            reader.read(Main.DATA.resolve("mknapcb1-0.csv"), Main.MKNAPCB1_0_OPTIMUM)
        );
        final ExperimentRunner runner = new ExperimentRunner(Main.RESULTS, Main.RUNS);
        final ExperimentPlan plan = new ExperimentPlan();
        for (final KnapsackInstance instance : instances) {
            final long start = System.nanoTime();
            final List<RunOutcome> outcomes = runner.run(instance, plan.configs(instance));
            new SummaryTable().print(instance, outcomes, System.out);
            System.out.printf("(%d runs in %.1f s)%n", outcomes.size(),
                (System.nanoTime() - start) / 1e9);
        }
        if (!List.of(args).contains(Main.NO_PLOTS)) {
            new Plots(System.getProperty("python", "python")).draw(Main.RESULTS);
        }
    }

    // solves the toy example and prints the best feasible chromosome and its items
    private static void toy(final KnapsackInstance toy) {
        // base configuration of ExperimentPlan, only fewer generations
        final RunConfig config = new RunConfig("toy", "base", Main.TOY_POPULATION,
            Main.TOY_GENERATIONS, Main.TOY_TOURNAMENT, Main.TOY_CROSSOVER,
            1.0 / toy.size(), PenaltyType.LINEAR);
        final Optional<Individual> best = config.engine(toy, 1).stream()
            .limit(Main.TOY_GENERATIONS + 1L)
            .map(EvolutionResult::bestFeasible)
            .flatMap(Optional::stream)
            .max(Comparator.comparingDouble(Individual::fitness));
        System.out.println("Toy example " + toy + ", optimum " + toy.optimum());
        best.ifPresent(ind -> System.out.println("  best " + ind.chromosome() + " value "
            + ind.fitness() + " items " + new KnapsackCodec(toy).decode(ind.chromosome())
            .stream().map(item -> "#" + item.index()).toList()));
    }
}
