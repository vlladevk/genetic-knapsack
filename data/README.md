# Data

All files use the OR-Library format for the multidimensional knapsack problem:

```
number of problems K
for each problem:
    n m optimum            (optimum = 0 when unknown)
    p(1) ... p(n)          values of the items
    r(i,1) ... r(i,n)      weights of the items for constraint i, one row per constraint
    b(1) ... b(m)          capacities
```

| File | Content | Source |
|---|---|---|
| `toy.txt` | 6 items, 2 constraints (weight, volume), optimum 23 | hand-made example |
| `mknap1.txt` | 7 problems, n = 6..50, known optima | OR-Library (Petersen, 1967) |
| `mknapcb1.txt` | 30 problems, n = 100, m = 5 | OR-Library (Chu & Beasley, 1998) |
| `mknapcb1-best.txt` | best known values for `mknapcb1.txt` | OR-Library, `mkcbres.txt` |

OR-Library: https://people.brunel.ac.uk/~mastjjb/jeb/orlib/mknapinfo.html

Toy example items: 0 tent (10; 5 kg; 6 l), 1 food (8; 3; 2), 2 pot (4; 2; 3),
3 torch (5; 1; 1), 4 book (2; 2; 1), 5 guitar (3; 4; 7). Capacity: 10 kg, 10 l.
