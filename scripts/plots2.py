"""Plots and tables for the experiments of task 2.

Usage: python scripts/plots2.py <results-dir> [--no-show]

For every instance directory with history.csv and runs.csv writes:
  summary.csv, summary.md              statistics of every configuration, ranking of series H
  plots/convergence_<series>.png       left: mean gap to the optimum per fitness evaluation
                                       (log-log); right: gap of every run at the end
                                       (all series except H and I)
  plots/diversity_<series>.png         mean Hamming distance in the population (E, F, G, Z)
  plots/population_selection.png       best and median fitness in the population per selection
  plots/feasible_share_selection.png   share of feasible individuals per selection
  plots/heatmap_all.png                series H: gap of every combination

summary.md also has Mann-Whitney U tests: are the differences between the best
configurations larger than the random spread of 10 runs?

Then all plots are shown in one window (arrow keys switch between them).
--no-show only writes the files.
"""

import sys
import textwrap
import time
from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402

import matplotlib.ticker  # noqa: E402,F401
from scipy.stats import mannwhitneyu  # noqa: E402
from scipy.stats import t as t_dist  # noqa: E402

from plots import (  # noqa: E402
    GRID, INK, INK_SECONDARY, OUTSIDE, REFERENCE, SERIES_COLORS, SURFACE, WIDE,
    optimum_line, show, style,
)

SERIES_TITLES = {
    "E-selection": "Series E: selection method, with and without elitism",
    "F-crossover": "Series F: crossover operator",
    "G-mutation": "Series G: mutation operator",
    "I-tuning": "Series I: parameters of the best combination",
    "J-approaches": "Series J: three ways to choose the best combination (50 000 evaluations)",
    "Z-final": "Final: best of task 2 vs task 1 (equal budget of 50 000 evaluations)",
}
# series with a table in summary.md but no convergence plot: H has the heatmap,
# I (one parameter at a time) is summed up in the table
NO_CONVERGENCE = {"H-all", "I-tuning"}
# series with a diversity plot
DIVERSITY = {"E-selection", "F-crossover", "G-mutation", "Z-final"}
# plots that are not made any more; old copies are deleted
OBSOLETE = ["factors_all.png", "convergence_I-tuning.png", "diversity_I-tuning.png"]
WRAP = 24
FACTOR_SERIES = "H-all"
SELECTION_SERIES = "E-selection"
SMOOTHING = 20
TOLERANCE = 1e-6


def save(fig, path, **options):
    """Save a figure; on Windows an image viewer can lock the file for a moment while it
    reloads it, so a failed write is retried a few times."""
    for attempt in range(10):
        try:
            fig.savefig(path, **options)
            break
        except OSError:
            if attempt == 9:
                raise
            time.sleep(0.5)
    plt.close(fig)


def gap(best, optimum):
    return 100 * (optimum - best) / optimum


def summary(runs, history):
    last = history.loc[history.groupby(["series", "label", "seed"])["generation"].idxmax()]
    diversity = last.groupby(["series", "label"])["diversity"].mean()
    rows = []
    for (series, label), group in runs.groupby(["series", "label"], sort=False):
        best = group["best"].dropna()
        optimum = group["optimum"].iloc[0]
        rows.append({
            "series": series,
            "label": label,
            "selection": group["selection"].iloc[0],
            "elites": group["elites"].iloc[0],
            "crossover": group["crossover"].iloc[0],
            "pc": group["pc"].iloc[0],
            "mutation": group["mutation"].iloc[0],
            "pm": group["pm"].iloc[0],
            "p_inv": group["p_inv"].iloc[0],
            "population": group["population"].iloc[0],
            "generations": group["generations"].iloc[0],
            "runs": len(group),
            "feasible_runs": len(best),
            "mean": best.mean(),
            "std": best.std(),
            "min": best.min(),
            "max": best.max(),
            "hits": int((best >= optimum - TOLERANCE).sum()),
            "gap_percent": gap(best.mean(), optimum) if len(best) else np.nan,
            "best_gap_percent": gap(best.max(), optimum) if len(best) else np.nan,
            "mean_found_at": group.loc[group["found_at"] >= 0, "found_at"].mean(),
            "final_diversity": diversity.get((series, label), np.nan),
        })
    return pd.DataFrame(rows)


def table(frame, columns, digits=None):
    digits = digits or {}
    lines = ["| " + " | ".join(columns) + " |", "|" + "---|" * len(columns)]
    for _, row in frame.iterrows():
        cells = []
        for col in columns:
            value = row[col]
            if isinstance(value, (float, np.floating)):
                cells.append("" if pd.isna(value) else f"{value:.{digits.get(col, 1)}f}")
            else:
                cells.append(str(value))
        lines.append("| " + " | ".join(cells) + " |")
    return lines


def compare(runs, pairs):
    """Mann-Whitney U test (two-sided) on the best values of 10 runs of two configurations.

    p < 0.05: the difference is unlikely to come only from the randomness of the runs.
    """
    lines = ["| configuration A | configuration B | mean A | mean B | p-value | significant |",
             "|---|---|---|---|---|---|"]
    for (series_a, label_a), (series_b, label_b) in pairs:
        first = runs[(runs["series"] == series_a) & (runs["label"] == label_a)]["best"].dropna()
        second = runs[(runs["series"] == series_b) & (runs["label"] == label_b)]["best"].dropna()
        if first.empty or second.empty:
            continue
        p_value = mannwhitneyu(first, second, alternative="two-sided").pvalue
        lines.append(f"| {series_a} {label_a} | {series_b} {label_b} | {first.mean():.1f} | "
                     f"{second.mean():.1f} | {p_value:.4f} | {'yes' if p_value < 0.05 else 'no'} |")
    return lines


def tests(runs, stats):
    pairs = []
    final = stats[stats["series"] == "Z-final"]
    if not final.empty:
        best = final[final["label"].str.startswith("task 2")]["label"].iloc[0]
        pairs += [(("Z-final", best), ("Z-final", label))
                  for label in final["label"] if label.startswith("task 1")]
    approaches = stats[stats["series"] == "J-approaches"]
    if not approaches.empty:
        labels = list(approaches["label"])
        pairs += [(("J-approaches", labels[1]), ("J-approaches", label))
                  for label in labels if label != labels[1]]
    ranked = stats[stats["series"] == FACTOR_SERIES].sort_values("gap_percent")
    if len(ranked) >= 2:
        pairs.append(((FACTOR_SERIES, ranked["label"].iloc[0]),
                      (FACTOR_SERIES, ranked["label"].iloc[1])))
    selection = stats[stats["series"] == SELECTION_SERIES]
    if not selection.empty:
        pairs += [((SELECTION_SERIES, f"{name}+elite"), (SELECTION_SERIES, name))
                  for name in ("tournament", "roulette", "rank")]
        pairs.append(((SELECTION_SERIES, "tournament"), (SELECTION_SERIES, "rank")))
    if not pairs:
        return []
    return ["## Mann-Whitney U tests (p < 0.05 = significant)", ""] + compare(runs, pairs) + [""]


def markdown(stats, instance, optimum, runs):
    digits = {"gap_percent": 2, "best_gap_percent": 2, "final_diversity": 3}
    columns = ["label", "feasible_runs", "mean", "std", "max", "gap_percent",
               "best_gap_percent", "mean_found_at", "final_diversity"]
    lines = [f"# {instance}", "", f"Optimum: {optimum:g}. gap = (optimum - mean) / optimum.", ""]
    for series in dict.fromkeys(stats["series"]):
        part = stats[stats["series"] == series]
        if series == FACTOR_SERIES:
            ranked = part.sort_values("gap_percent")
            lines += [f"## {series}: ranking of all {len(part)} combinations", ""]
            ranked = ranked.assign(rank=range(1, len(ranked) + 1))
            lines += table(ranked, ["rank"] + columns, digits)
        else:
            lines += [f"## {SERIES_TITLES.get(series, series)}", ""]
            lines += table(part, columns, digits)
        lines.append("")
    lines += tests(runs, stats)
    return "\n".join(lines)


def curves(part, value):
    """Mean and std over seeds of a column, per generation, as a function of evaluations."""
    table_ = part.pivot(index="evaluations", columns="seed", values=value)
    return table_


def line_styles(labels):
    """Colour and dash of every label. In series E a method with and without elitism
    shares a colour: solid line with the elite, dashed without."""
    styles = {}
    bases = []
    for label in labels:
        base = label.removesuffix("+elite")
        if base not in bases:
            bases.append(base)
    paired = any(label.endswith("+elite") for label in labels)
    for idx, label in enumerate(labels):
        if paired:
            base = label.removesuffix("+elite")
            color = SERIES_COLORS[bases.index(base) % len(SERIES_COLORS)]
            dash = "-" if label.endswith("+elite") else "--"
        else:
            color = SERIES_COLORS[idx % len(SERIES_COLORS)]
            dash = "-"
        styles[label] = (color, dash)
    return styles


def log_ticks(ax, low, high):
    """Readable ticks on a logarithmic gap axis (plain numbers, not 10^x)."""
    candidates = [0.05, 0.1, 0.2, 0.3, 0.5, 0.7, 1, 1.5, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20,
                  30, 40, 50, 70, 100]
    ticks = [tick for tick in candidates if low <= tick <= high]
    ax.set_yticks(ticks, [f"{tick:g}" for tick in ticks])
    ax.yaxis.set_minor_locator(matplotlib.ticker.NullLocator())


def log_x_ticks(ax):
    """Plain numbers on a logarithmic axis of fitness evaluations."""
    low, high = ax.get_xlim()
    ticks = [t for t in (100, 300, 1000, 3000, 10_000, 30_000, 100_000) if low <= t <= high]
    ax.set_xticks(ticks, [f"{t:,}".replace(",", " ") for t in ticks])
    ax.xaxis.set_minor_formatter(matplotlib.ticker.NullFormatter())


def end_labels(ax, ends, styles, x_end):
    """Final gap written right of every line, moved apart so that they do not overlap."""
    if not ends:
        return
    low, high = ax.get_ylim()
    span = np.log10(high) - np.log10(low)
    step = span * 0.045
    order = sorted(ends, key=lambda item: item[1])
    placed = []
    for label, value in order:
        position = np.log10(value)
        if placed and position < placed[-1] + step:
            position = placed[-1] + step
        placed.append(position)
        ax.annotate(f"{value:.2f}", xy=(x_end, value), xytext=(x_end * 1.08, 10 ** position),
                    textcoords="data", color=styles[label][0], fontsize=9, va="center",
                    annotation_clip=False,
                    arrowprops={"arrowstyle": "-", "color": styles[label][0], "lw": 0.6,
                                "alpha": 0.6})


def tick_label(label):
    """A long label "task 2: tournament+elite; two-point; ..." with one part per line."""
    head, _, rest = label.rpartition(": ")
    parts = ([head + ":"] if head else []) + rest.split("; ")
    return "\n".join(textwrap.fill(part, 16) for part in parts)


def final_runs(ax, runs, labels, styles, optimum):
    """Gap of every run at the end (dots) and the mean with its 95% confidence interval."""
    for idx, label in enumerate(labels):
        values = gap(runs[runs["label"] == label]["best"].dropna(), optimum)
        if values.empty:
            continue
        color = styles[label][0]
        jitter = np.random.default_rng(idx).uniform(-0.16, 0.16, len(values))
        ax.scatter(np.full(len(values), idx) + jitter, values, color=color, alpha=0.5, s=20,
                   edgecolors="none")
        half = 0.0
        if len(values) > 1:
            half = t_dist.ppf(0.975, len(values) - 1) * values.std() / np.sqrt(len(values))
        ax.errorbar(idx, values.mean(), yerr=half, fmt="o", color=INK, capsize=5,
                    markersize=6, elinewidth=1.4)
    if max(len(label) for label in labels) > WRAP:
        ax.set_xticks(range(len(labels)), [tick_label(label) for label in labels],
                      fontsize=8)
    else:
        ax.set_xticks(range(len(labels)), labels, rotation=35, ha="right", fontsize=9)
    ax.set_xlim(-0.6, len(labels) - 0.4)
    style(ax, "Gap at the end", "", "gap, % (dots = runs, black = mean ± 95% CI)")


def convergence(history, runs, series, optimum, out):
    """Left: mean gap to the optimum during the run, both axes logarithmic.
    Right: gap of every run at the end."""
    data = history[history["series"] == series]
    if data.empty:
        return
    config_runs = runs[runs["series"] == series]
    labels = list(dict.fromkeys(data["label"]))
    styles = line_styles(labels)
    long_labels = max(len(label) for label in labels) > WRAP
    fig, (ax, side) = plt.subplots(1, 2, figsize=(17, 6.4) if long_labels else (15, 5.6),
                                   gridspec_kw={"width_ratios": [2, 1] if long_labels
                                                else [2.3, 1]})
    ends = []
    lowest, highest = np.inf, 0.0
    x_end = data["evaluations"].max()
    for label in labels:
        color, dash = styles[label]
        config = config_runs[config_runs["label"] == label]
        seeds = config.loc[config["best"].notna(), "seed"]
        if seeds.empty:
            ax.plot([], [], color=color, linestyle=dash, linewidth=2,
                    label=f"{label} (no feasible solution)")
            continue
        part = data[(data["label"] == label) & (data["seed"].isin(seeds))]
        mean = curves(part, "best_ever").dropna().mean(axis=1)
        values = gap(mean, optimum)
        name = label if len(seeds) == len(config) else f"{label} ({len(seeds)}/{len(config)})"
        ax.plot(values.index, values.values, color=color, linestyle=dash, linewidth=2,
                label=name)
        ends.append((label, values.iloc[-1]))
        lowest = min(lowest, values.min())
        highest = max(highest, values.max())
    ax.set_xscale("log")
    ax.set_yscale("log")
    if ends:
        low, high = lowest * 0.85, min(highest * 1.1, 100)
        ax.set_ylim(low, high)
        log_ticks(ax, low, high)
    log_x_ticks(ax)
    end_labels(ax, ends, styles, x_end)
    style(ax, SERIES_TITLES.get(series, series), "fitness evaluations (log scale)",
          "gap to optimum, % (mean of 10 runs, log scale)")
    ax.grid(True, which="major", color=GRID, linewidth=0.8)
    ax.legend(frameon=False, labelcolor=INK, loc="lower left", fontsize=9)
    final_runs(side, config_runs, labels, styles, optimum)
    fig.tight_layout()
    save(fig, out / f"convergence_{series}.png", dpi=150, facecolor=SURFACE)


def diversity(history, series, out):
    data = history[history["series"] == series]
    if data.empty:
        return
    labels = list(dict.fromkeys(data["label"]))
    styles = line_styles(labels)
    fig, ax = plt.subplots(figsize=WIDE)
    for label in labels:
        color, dash = styles[label]
        mean = curves(data[data["label"] == label], "diversity").mean(axis=1)
        smooth = mean.rolling(SMOOTHING, min_periods=1).mean()
        ax.plot(smooth.index, 100 * smooth.values, color=color, linestyle=dash, linewidth=2,
                label=label)
    ax.set_xscale("log")
    log_x_ticks(ax)
    style(ax, f"{SERIES_TITLES.get(series, series)} - diversity of the population",
          "fitness evaluations (log scale)",
          f"mean Hamming distance, % of genes (mean of runs, rolling {SMOOTHING})")
    ax.set_ylim(bottom=0)
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    save(fig, out / f"diversity_{series}.png", dpi=150, facecolor=SURFACE)


def population(history, optimum, out):
    """Best and median of the population for every selection (without elites)."""
    data = history[history["series"] == SELECTION_SERIES]
    labels = [label for label in dict.fromkeys(data["label"]) if "elite" not in label]
    if not labels:
        return
    fig, axes = plt.subplots(1, len(labels), figsize=(13, 4.6), sharey=True)
    for ax, label in zip(np.atleast_1d(axes), labels):
        mean = data[data["label"] == label].groupby("evaluations")[
            ["best", "median", "best_ever"]].mean()
        ax.plot(mean.index, mean["best_ever"], color=SERIES_COLORS[0], linewidth=2,
                label="best feasible so far")
        ax.plot(mean.index, mean["best"], color=SERIES_COLORS[1], linewidth=1.5,
                label="best in population")
        median = mean["median"].rolling(SMOOTHING, min_periods=1).mean()
        ax.plot(mean.index, median, color=SERIES_COLORS[2], linewidth=1.5,
                label="median of population")
        optimum_line(ax, optimum)
        ax.set_ylim(0, optimum * 1.05)
        style(ax, label, "fitness evaluations", "fitness (mean of runs)")
    np.atleast_1d(axes)[-1].legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.suptitle("Best and median individual under each selection (no elite): the lower "
                 "the median, the more bad individuals survive",
                 color=INK, x=0.01, ha="left")
    fig.tight_layout()
    save(fig, out / "population_selection.png", dpi=150, facecolor=SURFACE)


def feasible_share(history, out):
    data = history[history["series"] == SELECTION_SERIES]
    if data.empty:
        return
    fig, ax = plt.subplots(figsize=WIDE)
    for idx, label in enumerate(dict.fromkeys(data["label"])):
        curve = data[data["label"] == label].groupby("evaluations")["feasible_share"].mean()
        curve = curve.rolling(SMOOTHING, min_periods=1).mean()
        ax.plot(curve.index, 100 * curve.values, color=SERIES_COLORS[idx], linewidth=2,
                label=label)
    style(ax, "Share of feasible individuals per selection", "fitness evaluations",
          f"feasible individuals, % (mean of runs, rolling {SMOOTHING})")
    ax.set_ylim(-3, 105)
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    save(fig, out / "feasible_share_selection.png", dpi=150, facecolor=SURFACE)


def heatmap(stats, out):
    data = stats[stats["series"] == FACTOR_SERIES]
    if data.empty:
        return
    mutations = list(dict.fromkeys(data["mutation"]))
    crossovers = list(dict.fromkeys(data["crossover"]))
    rows = list(dict.fromkeys(zip(data["selection"], data["elites"])))
    vmin, vmax = data["gap_percent"].min(), data["gap_percent"].max()
    fig, axes = plt.subplots(1, len(mutations), figsize=(14, 5.2), sharey=True)
    for ax, mutation in zip(np.atleast_1d(axes), mutations):
        grid = np.full((len(rows), len(crossovers)), np.nan)
        for (sel, eli), (r) in zip(rows, range(len(rows))):
            for c, crossover in enumerate(crossovers):
                cell = data[(data["selection"] == sel) & (data["elites"] == eli)
                            & (data["crossover"] == crossover) & (data["mutation"] == mutation)]
                if not cell.empty:
                    grid[r, c] = cell["gap_percent"].iloc[0]
        image = ax.imshow(grid, cmap="viridis_r", vmin=vmin, vmax=vmax, aspect="auto")
        for r in range(len(rows)):
            for c in range(len(crossovers)):
                if not np.isnan(grid[r, c]):
                    shade = image.cmap(image.norm(grid[r, c]))
                    light = 0.299 * shade[0] + 0.587 * shade[1] + 0.114 * shade[2] > 0.55
                    ax.text(c, r, f"{grid[r, c]:.2f}", ha="center", va="center", fontsize=9,
                            color=INK if light else "white")
        ax.set_xticks(range(len(crossovers)), [c.lower().replace("_", "-") for c in crossovers])
        ax.set_yticks(range(len(rows)),
                      [f"{sel.lower()}{' + elite' if eli else ''}" for sel, eli in rows])
        ax.set_title(f"mutation: {mutation.lower().replace('_', '-')}", color=INK,
                     fontsize=11, loc="left")
        ax.tick_params(colors=INK_SECONDARY)
    fig.colorbar(image, ax=axes, label="gap from optimum, % (mean of 10 runs, lower = better)")
    fig.suptitle("Series H: every combination of selection, elitism, crossover and mutation",
                 color=INK, x=0.01, ha="left")
    save(fig, out / "heatmap_all.png", dpi=150, facecolor=SURFACE, bbox_inches="tight")


def instance(directory):
    history = pd.read_csv(directory / "history.csv")
    runs = pd.read_csv(directory / "runs.csv")
    optimum = runs["optimum"].iloc[0]
    out = directory / "plots"
    out.mkdir(exist_ok=True)
    stats = summary(runs, history)
    stats.to_csv(directory / "summary.csv", index=False)
    (directory / "summary.md").write_text(markdown(stats, directory.name, optimum, runs),
                                          encoding="utf-8")
    for name in OBSOLETE:
        (out / name).unlink(missing_ok=True)
    for series in dict.fromkeys(runs["series"]):
        if series not in NO_CONVERGENCE:
            convergence(history, runs, series, optimum, out)
        if series in DIVERSITY:
            diversity(history, series, out)
    population(history, optimum, out)
    feasible_share(history, out)
    heatmap(stats, out)
    print(f"{directory.name}: summary and {len(list(out.glob('*.png')))} plots in {out}")


def main():
    args = [arg for arg in sys.argv[1:] if arg != "--no-show"]
    if len(args) != 1:
        print(__doc__)
        sys.exit(2)
    root = Path(args[0])
    dirs = sorted(path.parent for path in root.glob("*/runs.csv"))
    if not dirs:
        print(f"No results in {root}")
        sys.exit(1)
    for directory in dirs:
        instance(directory)
    if "--no-show" not in sys.argv:
        show(sorted(path for directory in dirs for path in (directory / "plots").glob("*.png")))


if __name__ == "__main__":
    main()
