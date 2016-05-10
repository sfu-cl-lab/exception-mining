package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:49 PM
* To change this template use File | Settings | File Templates.
*/
public class Poisson implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Poisson distribution;
    private double mean;

    public Poisson(double mean) {
        distribution = new cern.jet.random.Poisson(mean, engine);
        this.mean = mean;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Poisson(" + mean + ")";
    }
}
