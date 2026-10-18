# Data

One problem per CSV file:

```
item,value,weight,volume    <- header: item name, value, one column per constraint
capacity,,10,10             <- capacity of every constraint
tent,10,5,6                 <- one row per item
```

| File | Content | Optimum | Source |
|---|---|---|---|
| `toy.csv` | 6 items, 2 constraints (weight, volume) | 23 | hand-made example |
| `mknap1-6.csv` | 50 items, 5 constraints | 16537 | OR-Library `mknap1.txt`, problem 7 |
| `mknapcb1-0.csv` | 100 items, 5 constraints | 24381 | OR-Library `mknapcb1.txt`, problem 1 |

The two OR-Library problems were converted from the original files without changes
to the numbers. Optima are given in `Main`.

All three optima are proven by an exact solver (integer programming, CBC branch and
bound, under 1 s each):
`python scripts/exact.py data/toy.csv data/mknap1-6.csv data/mknapcb1-0.csv`.
For `mknapcb1` OR-Library lists 24381 only as the best known value.

OR-Library (J. E. Beasley): https://people.brunel.ac.uk/~mastjjb/jeb/orlib/mknapinfo.html
