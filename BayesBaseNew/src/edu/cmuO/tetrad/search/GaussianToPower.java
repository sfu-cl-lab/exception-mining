package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import cern.jet.random.Normal;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:19 PM
* To change this template use File | Settings | File Templates.
*/
public class GaussianToPower implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private Normal normal = new Normal(0, 1, engine);
    private double power;
    private double variance;

    public GaussianToPower(double variance, double power) {
        if (power <= 0) {
            throw new IllegalArgumentException();
        }

        this.variance = variance;
        this.power = power;
    }

    @Override
	public double drawSample() {
        return Math.pow(normal.nextDouble(), power);
    }

    @Override
	public String toString() {
        return "GaussianToPower(" + variance + ", " + power + ")";
    }
}
