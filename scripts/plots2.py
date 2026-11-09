"""Plots and tables for the experiments of task 2.

Usage: python scripts/plots2.py <results-dir> [--no-show]

For every instance directory with history.csv and runs.csv writes:
  summary.csv, summary.md              statistics of every configuration, ranking of series H
  plots/convergence_<series>.png       mean best-so-far feasible value per fitness evaluation
  plots/diversity_<series>.png         mean Hamming distance in the population
  plots/population_selection.png       best and median fitness in the population per selection
  plots/feasible_share_selection.png   share of feasible individuals per selection
  plots/heatmap_all.png                series H: gap of every combination
  plots/factors_all.png                series H: mean gap of every level of every factor

Then all plots are shown in one window (arrow keys switch between them).
--no-show only writes the files.
"""

import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402

from plots import (  # noqa: E402
    GRID, INK, INK_SECONDARY, OUTSIDE, REFERENCE, SERIES_COLORS, SURFACE, WIDE,
    optimum_line, show, style,
)

SERIES_TITLES = {
    "E-selection": "Series E: selection method, with and without elitism",
    "F-crossover": "Series F: crossover operator",
    "G-mutation": "Series G: mutation operator",
    "I-tuning": "Series I: parameters of the best combination",
    "Z-final": "Final: task 2 vs task 1 (equal budget of fitness evaluations)",
}
FACTOR_SERIES = "H-all"
SELECTION_SERIES = "E-selection"
SMOOTHING = 20
TOLERANCE = 1e-6


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


def markdown(stats, instance, optimum):
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
    return "\n".join(lines)


def curves(part, value):
    """Mean and std over seeds of a column, per generation, as a function of evaluations."""
    table_ = part.pivot(index="evaluations", columns="seed", values=value)
    return table_


def convergence(history, runs, series, optimum, out):
    data = history[history["series"] == series]
    if data.empty:
        return
    fig, ax = plt.subplots(figsize=WIDE)
    for idx, label in enumerate(dict.fromkeys(data["label"])):
        color = SERIES_COLORS[idx % len(SERIES_COLORS)]
        config = runs[(runs["series"] == series) & (runs["label"] == label)]
        seeds = config.loc[config["best"].notna(), "seed"]
        if seeds.empty:
            ax.plot([], [], color=color, linewidth=2, label=f"{label} (no feasible solution)")
            continue
        part = data[(data["label"] == label) & (data["seed"].isin(seeds))]
        values = curves(part, "best_ever").dropna()
        mean = values.mean(axis=1)
        spread = values.std(axis=1)
        name = label if len(seeds) == len(config) else f"{label} ({len(seeds)}/{len(config)})"
        ax.plot(mean.index, mean.values, color=color, linewidth=2, label=name)
        ax.fill_between(mean.index, mean - spread, mean + spread, color=color, alpha=0.12,
                        linewidth=0)
    optimum_line(ax, optimum)
    top = optimum * 1.01
    ax.set_ylim(optimum * 0.80, top)
    style(ax, SERIES_TITLES.get(series, series), "fitness evaluations",
          "best feasible value so far (mean ± std of 10 runs)")
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    fig.savefig(out / f"convergence_{series}.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def diversity(history, series, out):
    data = history[history["series"] == series]
    if data.empty:
        return
    fig, ax = plt.subplots(figsize=WIDE)
    for idx, label in enumerate(dict.fromkeys(data["label"])):
        mean = curves(data[data["label"] == label], "diversity").mean(axis=1)
        ax.plot(mean.index, 100 * mean.values, color=SERIES_COLORS[idx % len(SERIES_COLORS)],
                linewidth=2, label=label)
    style(ax, f"{SERIES_TITLES.get(series, series)} - diversity of the population",
          "fitness evaluations", "mean Hamming distance, % of genes (mean of runs)")
    ax.set_ylim(bottom=0)
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    fig.savefig(out / f"diversity_{series}.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


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
    fig.suptitle("Population under each selection: gap between best and median = pressure",
                 color=INK, x=0.01, ha="left")
    fig.tight_layout()
    fig.savefig(out / "population_selection.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


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
    fig.savefig(out / "feasible_share_selection.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


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
    fig.savefig(out / "heatmap_all.png", dpi=150, facecolor=SURFACE, bbox_inches="tight")
    plt.close(fig)


def factors(runs, out):
    """Main effects: mean gap of every level of a factor, averaged over all other factors."""
    data = runs[runs["series"] == FACTOR_SERIES].copy()
    if data.empty:
        return
    data["gap"] = gap(data["best"], data["optimum"])
    data["elitism"] = np.where(data["elites"] > 0, "1 elite", "none")
    names = [("selection", "selection"), ("elitism", "elitism"),
             ("crossover", "crossover"), ("mutation", "mutation")]
    fig, axes = plt.subplots(1, len(names), figsize=(14, 4.4), sharey=True)
    for ax, (column, title) in zip(axes, names):
        groups = data.groupby(column, sort=False)["gap"]
        mean = groups.mean()
        error = groups.std() / np.sqrt(groups.count())
        labels = [str(level).lower().replace("_", "-") for level in mean.index]
        ax.errorbar(range(len(mean)), mean.values, yerr=error.values, fmt="o",
                    color=SERIES_COLORS[0], ecolor=REFERENCE, capsize=4, markersize=7)
        ax.set_xticks(range(len(mean)), labels)
        ax.set_xlim(-0.5, len(mean) - 0.5)
        style(ax, title, "", "gap, % (mean ± std. error)" if ax is axes[0] else "")
    fig.suptitle("Series H: effect of each factor, averaged over all other factors "
                 "(lower = better)", color=INK, x=0.01, ha="left")
    fig.tight_layout()
    fig.savefig(out / "factors_all.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def instance(directory):
    history = pd.read_csv(directory / "history.csv")
    runs = pd.read_csv(directory / "runs.csv")
    optimum = runs["optimum"].iloc[0]
    out = directory / "plots"
    out.mkdir(exist_ok=True)
    stats = summary(runs, history)
    stats.to_csv(directory / "summary.csv", index=False)
    (directory / "summary.md").write_text(markdown(stats, directory.name, optimum),
                                          encoding="utf-8")
    for series in dict.fromkeys(runs["series"]):
        if series != FACTOR_SERIES:
            convergence(history, runs, series, optimum, out)
            diversity(history, series, out)
    population(history, optimum, out)
    feasible_share(history, out)
    heatmap(stats, out)
    factors(runs, out)
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
