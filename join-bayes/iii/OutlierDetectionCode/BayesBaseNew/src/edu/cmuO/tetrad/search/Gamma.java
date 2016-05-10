package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:27 PM
* To change this template use File | Settings | File Templates.
*/
public class Gamma implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Gamma distribution;
    private double alpha;
    private double lambda;

    public Gamma(double alpha, double lambda) {
        distribution = new cern.jet.random.Gamma(alpha, lambda, engine);
        this.alpha = alpha;
        this.lambda = lambda;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Gamma(" + alpha + ", " + lambda + ")";
    }
}
