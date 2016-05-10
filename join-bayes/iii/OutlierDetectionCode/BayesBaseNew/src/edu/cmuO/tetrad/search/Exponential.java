package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:02 PM
* To change this template use File | Settings | File Templates.
*/
public class Exponential implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Exponential distribution;
    private double lambda;

    public Exponential(double lambda) {
        distribution = new cern.jet.random.Exponential(lambda, engine);
        this.lambda = lambda;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Exponential(" + lambda + ")";
    }
}
