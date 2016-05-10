package edu.cmu.tetrad.search.fastica;

/**
 * The x^3 function is useful for estimating sub-Gaussian independent components
 * when there are no outliers.
 *
 * @author Michael Lambertz
 */

public class Power3CFunction implements ContrastFunction {

    @Override
	public double function(double x) {
        return (x * x * x);
    }

    @Override
	public double derivative(double x) {
        return (3 * x * x);
    }

}
