package edu.cmu.tetrad.search.fastica;

/**
 * This function is useful, when the independent components are highly
 * super-Gaussian, or when robustness is very important.
 *
 * @author Michael Lambertz
 */

public class GaussCFunction implements ContrastFunction {

    private double a;

    public GaussCFunction(double a) {
        this.a = a;
    }

    @Override
	public double function(double x) {
        return (x * Math.exp(-a * x * x / 2));
    }

    @Override
	public double derivative(double x) {
        return ((1 - a * x * x) * Math.exp(-a * x * x / 2));
    }

}
