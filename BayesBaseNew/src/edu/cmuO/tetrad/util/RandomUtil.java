package edu.cmu.tetrad.util;

import cern.jet.random.engine.RandomEngine;

/**
 * A source of random numbers of various sorts used in Tetrad.
 *
 * @author Joseph Ramsey
 */
public interface RandomUtil {

    /**
     * Returns the next random integer from 0 up to n - 1.
     */
    int nextInt(int n);

    /**
     * Returns the next random long from 0 up to n - 1.
     */
    long nextLong(long n);

    /**
     * Returns the next random double in [0.0, 1.0].
     */
    double nextDouble();

    /**
     * Returns the next random Gaussian in N(0.0, 1.0).
     */
    double nextGaussian();

    /**
     * Get the default COLT generator.
     * @return the default COLT generator.
     */
    RandomEngine getRandomEngine();
}
