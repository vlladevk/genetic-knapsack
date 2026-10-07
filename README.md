# mkp-metaheuristics

Basic genetic algorithm for the **multidimensional knapsack problem** (MKP,
*wielowymiarowy problem plecakowy*), written without external libraries.
Course: *Techniki inteligencji obliczeniowej*, PWr, task 1.

## Problem

Choose items with the highest total value so that for **every** constraint
(weight, volume, ...) the total weight does not exceed its capacity.
Data: OR-Library, see [`data/README.md`](data/README.md).

## Algorithm

| Element | Choice |
|---|---|
| Chromosome | binary, gene `j` = item `j` taken |
| Fitness | total value − ρ · total excess weight (linear penalty), ρ = max value / weight |
| Selection | tournament, size 2 |
| Crossover | one-point, probability `pc` per pair |
| Mutation | bit-flip, probability `pm` per gene |
| Replacement | whole population replaced by children (no elitism) |
| Stop condition | fixed number of generations |

The answer of a run is the best **feasible** solution found in any generation.

## Code

```
pl.pwr.tio.mkp
├── genetic/     Chromosome, Individual, Codec, Problem           (independent of the knapsack)
├── selector/    Selector, TournamentSelector
├── alterer/     Alterer, Crossover, SinglePointCrossover, BitFlipMutator
├── engine/      Engine (main loop, stream of generations), EvolutionResult
├── knapsack/    Item, KnapsackInstance, OrLibraryReader, KnapsackCodec,
│                KnapsackProblem, Penalty, LinearPenalty, DeathPenalty
├── experiment/  ExperimentPlan, RunConfig, ExperimentRunner, CsvWriter, SummaryTable, Plots
└── Main
```

## Running

Requirements: Java 21, Maven, Python 3 with `pip install -r scripts/requirements.txt`.

```
mvn package
java -cp target/classes pl.pwr.tio.mkp.Main
```

or run `Main` from the IDE (working directory = project root).

`Main` solves the toy example, runs all experiment series (10 runs each, seeds 1..10)
on `mknap1-6` (n = 50, m = 5) and `mknapcb1-0` (n = 100, m = 5), prints a summary and
calls `scripts/plots.py`. Results go to `results/<instance>/`:
`history.csv`, `runs.csv`, `summary.csv`, `summary.md`, `plots/*.png`.

Options: `--no-plots` skips Python; `-Dpython=python3` sets the Python command.

## Experiments

Base configuration: P = 30, G = 1000, tournament 2, pc = 0.9, pm = 1/n, linear penalty.

| Series | Changed parameter |
|---|---|
| A | pc ∈ {0, 0.5, 0.7, 0.9, 1} |
| B | pm ∈ {0, 0.5, 1, 2, 5, 10} / n |
| C | P ∈ {10, 30, 50} |
| D | linear penalty vs death penalty |

Tests: `mvn test`.
