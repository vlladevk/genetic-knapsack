"""Plots and summary tables for the GA experiments.

Usage: python scripts/plots.py <results-dir>

For every instance directory with history.csv and runs.csv writes:
  summary.csv, summary.md          statistics of every configuration
  plots/convergence_<series>.png   mean best-so-far feasible value per generation
  plots/boxplot_<series>.png       best values of all runs per configuration
  plots/population_base.png        best and median fitness of the population (base config)
  plots/feasible_share.png         share of feasible individuals (penalty series)
"""

import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt  # noqa: E402
import pandas as pd  # noqa: E402

SERIES_COLORS = [
    "#2a78d6", "#eb6834", "#1baf7a", "#eda100",
    "#e87ba4", "#008300", "#4a3aa7", "#e34948",
]
INK = "#0b0b0b"
INK_SECONDARY = "#52514e"
GRID = "#e4e3df"
SURFACE = "#fcfcfb"
REFERENCE = "#8a8984"

SERIES_TITLES = {
    "A-pc": "Series A: probability of crossover pc",
    "B-pm": "Series B: probability of mutation pm",
    "C-population": "Series C: population size P",
    "D-penalty": "Series D: penalty function",
}

BASE_SERIES = "B-pm"
BASE_LABEL = "pm=1/n"
SMOOTHING = 20
WIDE = (10.5, 5.2)
OUTSIDE = {"loc": "upper left", "bbox_to_anchor": (1.01, 1)}
TOLERANCE = 1e-6


def style(ax, title, xlabel, ylabel):
    ax.set_facecolor(SURFACE)
    ax.set_title(title, color=INK, fontsize=12, loc="left")
    ax.set_xlabel(xlabel, color=INK_SECONDARY)
    ax.set_ylabel(ylabel, color=INK_SECONDARY)
    ax.grid(True, color=GRID, linewidth=0.8)
    ax.set_axisbelow(True)
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color(REFERENCE)
    ax.tick_params(colors=INK_SECONDARY)


def optimum_line(ax, optimum):
    if pd.notna(optimum):
        ax.axhline(optimum, color=REFERENCE, linestyle="--", linewidth=1.2)
        ax.annotate(f"optimum {optimum:g}", xy=(1, optimum), xycoords=("axes fraction", "data"),
                    xytext=(-4, 4), textcoords="offset points", ha="right", va="bottom",
                    color=INK_SECONDARY, fontsize=9)


def summary(runs):
    rows = []
    for (series, label), group in runs.groupby(["series", "label"], sort=False):
        best = group["best"].dropna()
        optimum = group["optimum"].iloc[0]
        row = {
            "series": series,
            "label": label,
            "pc": group["pc"].iloc[0],
            "pm": group["pm"].iloc[0],
            "population": group["population"].iloc[0],
            "penalty": group["penalty"].iloc[0],
            "runs": len(group),
            "feasible_runs": len(best),
            "mean": best.mean(),
            "std": best.std(),
            "min": best.min(),
            "max": best.max(),
            "mean_found_at": group.loc[group["found_at"] >= 0, "found_at"].mean(),
        }
        if pd.notna(optimum):
            row["hits"] = int((best >= optimum - TOLERANCE).sum())
            row["gap_percent"] = 100 * (optimum - best.mean()) / optimum
        rows.append(row)
    return pd.DataFrame(rows)


def markdown(table, instance, optimum):
    columns = ["series", "label", "feasible_runs", "mean", "std", "min", "max",
               "hits", "gap_percent", "mean_found_at"]
    columns = [col for col in columns if col in table.columns]
    lines = [f"# {instance}", "", f"Optimum / best known: {optimum:g}" if pd.notna(optimum)
             else "Optimum unknown", "",
             "| " + " | ".join(columns) + " |",
             "|" + "---|" * len(columns)]
    for _, row in table.iterrows():
        cells = []
        for col in columns:
            value = row[col]
            if isinstance(value, float):
                cells.append("" if pd.isna(value) else f"{value:.2f}" if col == "gap_percent"
                             else f"{value:.1f}")
            else:
                cells.append(str(value))
        lines.append("| " + " | ".join(cells) + " |")
    return "\n".join(lines) + "\n"


def best_so_far(part, seeds):
    """Mean best-so-far value over the given runs.

    Starts at the first generation in which every one of these runs already has
    a feasible solution, so the mean is always taken over the same runs.
    """
    table = part[part["seed"].isin(seeds)].pivot(index="generation", columns="seed",
                                                  values="best_ever")
    complete = table.dropna()
    return complete.mean(axis=1)


def convergence(history, runs, series, optimum, out):
    data = history[history["series"] == series]
    labels = list(dict.fromkeys(data["label"]))
    fig, ax = plt.subplots(figsize=WIDE)
    for idx, label in enumerate(labels):
        part = data[data["label"] == label]
        config = runs[(runs["series"] == series) & (runs["label"] == label)]
        seeds = config.loc[config["best"].notna(), "seed"]
        if seeds.empty:
            ax.plot([], [], color=SERIES_COLORS[idx % len(SERIES_COLORS)], linewidth=2,
                    label=f"{label} (no feasible solution)")
            continue
        curve = best_so_far(part, seeds)
        name = label
        if len(seeds) < len(config):
            name = f"{label} ({len(seeds)}/{len(config)} feasible)"
        ax.plot(curve.index, curve.values, color=SERIES_COLORS[idx % len(SERIES_COLORS)],
                linewidth=2, label=name)
    optimum_line(ax, optimum)
    style(ax, SERIES_TITLES.get(series, series), "generation",
          "best feasible value so far (mean of runs)")
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    fig.savefig(out / f"convergence_{series}.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def boxplot(runs, series, optimum, out):
    data = runs[runs["series"] == series]
    labels = list(dict.fromkeys(data["label"]))
    values = [data[data["label"] == label]["best"].dropna().values for label in labels]
    fig, ax = plt.subplots(figsize=(9, 5.2))
    positions = range(1, len(labels) + 1)
    kept = [(pos, vals) for pos, vals in zip(positions, values) if len(vals) > 0]
    if kept:
        box = ax.boxplot([vals for _, vals in kept], positions=[pos for pos, _ in kept],
                         widths=0.5, patch_artist=True, medianprops={"color": INK})
        for patch in box["boxes"]:
            patch.set_facecolor(SERIES_COLORS[0] + "55")
            patch.set_edgecolor(SERIES_COLORS[0])
    for pos, vals in zip(positions, values):
        if len(vals) < len(data[data["label"] == labels[pos - 1]]):
            ax.annotate(f"{len(vals)}/{len(data[data['label'] == labels[pos - 1]])} feasible",
                        xy=(pos, 0), xycoords=("data", "axes fraction"), xytext=(0, 6),
                        textcoords="offset points", ha="center", color=INK_SECONDARY, fontsize=9)
    ax.set_xticks(list(positions), labels)
    optimum_line(ax, optimum)
    style(ax, SERIES_TITLES.get(series, series) + " - best value of each run", "",
          "best feasible value")
    fig.tight_layout()
    fig.savefig(out / f"boxplot_{series}.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def population(history, optimum, out):
    data = history[(history["series"] == BASE_SERIES) & (history["label"] == BASE_LABEL)]
    if data.empty:
        return
    mean = data.groupby("generation")[["best", "median", "best_ever"]].mean()
    fig, ax = plt.subplots(figsize=WIDE)
    ax.plot(mean.index, mean["best_ever"], color=SERIES_COLORS[0], linewidth=2,
            label="best feasible so far")
    ax.plot(mean.index, mean["best"], color=SERIES_COLORS[1], linewidth=2,
            label="best fitness in population")
    median = mean["median"].rolling(SMOOTHING, min_periods=1).mean()
    ax.plot(mean.index, median, color=SERIES_COLORS[2], linewidth=2,
            label=f"median fitness in population\n(rolling mean of {SMOOTHING} generations)")
    optimum_line(ax, optimum)
    top = optimum if pd.notna(optimum) else mean["best"].max()
    ax.set_ylim(0, top * 1.05)
    style(ax, "Population in the base configuration (pc=0.9, pm=1/n, P=30)", "generation",
          "fitness (mean of runs; values below 0 cut off)")
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    fig.savefig(out / "population_base.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def feasible_share(history, out):
    data = history[history["series"] == "D-penalty"]
    if data.empty:
        return
    fig, ax = plt.subplots(figsize=WIDE)
    for idx, label in enumerate(dict.fromkeys(data["label"])):
        curve = data[data["label"] == label].groupby("generation")["feasible_share"].mean()
        ax.plot(curve.index, 100 * curve.values, color=SERIES_COLORS[idx], linewidth=2,
                label=label)
    style(ax, "Share of feasible individuals in the population", "generation",
          "feasible individuals, % (mean of runs)")
    ax.set_ylim(-3, 105)
    ax.legend(frameon=False, labelcolor=INK, **OUTSIDE)
    fig.tight_layout()
    fig.savefig(out / "feasible_share.png", dpi=150, facecolor=SURFACE)
    plt.close(fig)


def instance(directory):
    history = pd.read_csv(directory / "history.csv")
    runs = pd.read_csv(directory / "runs.csv")
    optimum = runs["optimum"].iloc[0]
    out = directory / "plots"
    out.mkdir(exist_ok=True)
    table = summary(runs)
    table.to_csv(directory / "summary.csv", index=False)
    (directory / "summary.md").write_text(markdown(table, directory.name, optimum),
                                          encoding="utf-8")
    for series in dict.fromkeys(runs["series"]):
        convergence(history, runs, series, optimum, out)
        boxplot(runs, series, optimum, out)
    population(history, optimum, out)
    feasible_share(history, out)
    print(f"{directory.name}: summary and {len(list(out.glob('*.png')))} plots in {out}")


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    root = Path(sys.argv[1])
    dirs = sorted(path.parent for path in root.glob("*/runs.csv"))
    if not dirs:
        print(f"No results in {root}")
        sys.exit(1)
    for directory in dirs:
        instance(directory)


if __name__ == "__main__":
    main()
