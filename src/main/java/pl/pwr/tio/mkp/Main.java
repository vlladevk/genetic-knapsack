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
import pl.pwr.tio.mkp.knapsack.KnapsackCodec;
import pl.pwr.tio.mkp.knapsack.KnapsackInstance;
import pl.pwr.tio.mkp.knapsack.OrLibraryReader;

// Runs the toy example and all experiments.
// Args: --no-plots skips Python; -Dpython=python3 changes the Python command.
public final class Main {

    private static final Path DATA = Path.of("data");

    private static final Path RESULTS = Path.of("results");

    // seeds 1..RUNS
    private static final int RUNS = 10;

    // mknap1-6: n = 50, m = 5
    private static final int MKNAP1_LARGEST = 6;

    private static final int TOY_GENERATIONS = 50;

    private static final String NO_PLOTS = "--no-plots";

    private Main() {
    }

    public static void main(final String[] args) throws IOException {
        final OrLibraryReader reader = new OrLibraryReader();
        Main.toy(reader.read(Main.DATA.resolve("toy.txt")).getFirst());
        final List<KnapsackInstance> instances = List.of(
            reader.read(Main.DATA.resolve("mknap1.txt")).get(Main.MKNAP1_LARGEST),
            reader.read(Main.DATA.resolve("mknapcb1.txt"), Main.DATA.resolve("mknapcb1-best.txt"))
                .getFirst()
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

    private static void toy(final KnapsackInstance toy) {
        final RunConfig config = new RunConfig("toy", "base", ExperimentPlan.POPULATION,
            Main.TOY_GENERATIONS, ExperimentPlan.TOURNAMENT, ExperimentPlan.CROSSOVER,
            1.0 / toy.size(), PenaltyType.LINEAR);
        final Optional<Individual> best = config.engine(toy, 1).stream()
            .limit(Main.TOY_GENERATIONS + 1L)
            .map(EvolutionResult::bestFeasible)
            .flatMap(Optional::stream)
            .max(Comparator.comparingDouble(Individual::fitness));
        System.out.println("Toy example " + toy + ", optimum " + toy.optimum().orElse(0));
        best.ifPresent(ind -> System.out.println("  best " + ind.chromosome() + " value "
            + ind.fitness() + " items " + new KnapsackCodec(toy).decode(ind.chromosome())
            .stream().map(item -> "#" + item.index()).toList()));
    }
}
