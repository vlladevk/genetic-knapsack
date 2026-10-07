"""Exact optimum of a knapsack instance, to check the optimum values used in Main.

Usage: python scripts/exact.py data/mknap1-6.csv [more files ...]

Solves the problem as an integer program with the CBC solver (branch and bound):
    max  sum value(j) * x(j)
    s.t. sum weight(i, j) * x(j) <= capacity(i)   for every constraint i
         x(j) in {0, 1}
CBC proves optimality, unlike the GA. Needs: pip install pulp
"""

import sys
import time
from pathlib import Path

import pandas as pd
import pulp


def solve(path):
    table = pd.read_csv(path)
    capacity = table.iloc[0, 2:].astype(float)
    items = table.iloc[1:].reset_index(drop=True)
    weights = items.iloc[:, 2:].astype(float)
    values = items["value"].astype(float)
    problem = pulp.LpProblem(path.stem, pulp.LpMaximize)
    x = [pulp.LpVariable(f"x{j}", cat="Binary") for j in range(len(items))]
    problem += pulp.lpSum(values[j] * x[j] for j in range(len(items)))
    for i, column in enumerate(weights.columns):
        problem += pulp.lpSum(weights[column][j] * x[j] for j in range(len(items))) \
            <= capacity.iloc[i]
    start = time.time()
    problem.solve(pulp.PULP_CBC_CMD(msg=False))
    chosen = "".join(str(round(var.value())) for var in x)
    print(f"{path.stem}: {pulp.LpStatus[problem.status]}, optimum "
          f"{pulp.value(problem.objective):g} in {time.time() - start:.1f} s\n  {chosen}")


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    for name in sys.argv[1:]:
        solve(Path(name))


if __name__ == "__main__":
    main()
