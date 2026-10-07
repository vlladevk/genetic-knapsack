package pl.pwr.tio.mkp.knapsack;

// Penalty function (funkcja kary): constraints are handled inside fitness,
// so no repair operator is needed. Must return value when violation is 0.
public interface Penalty {

    double apply(double value, double violation);
}
