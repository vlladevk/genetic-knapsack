package pl.pwr.tio.mkp.genetic;

import java.util.Random;

// Encoding (kodowanie): chromosome <-> solution of the problem (T).
public interface Codec<T> {

    T decode(Chromosome chromosome);

    // for the initial population
    Chromosome random(Random random);
}
