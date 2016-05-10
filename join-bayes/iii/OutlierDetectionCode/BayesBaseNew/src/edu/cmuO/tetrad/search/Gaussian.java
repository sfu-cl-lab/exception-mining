package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:19 PM
* To change this template use File | Settings | File Templates.
*/
public class Gaussian implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Normal distribution;
    private double mean;
    private double sd;

    public Gaussian(double mean, double sd) {
        distribution = new cern.jet.random.Normal(mean, sd, engine);
        this.mean = mean;
        this.sd = sd;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Gaussian(" + mean + ", " + sd + ")";
    }
}
