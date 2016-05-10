package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:10 PM
* To change this template use File | Settings | File Templates.
*/
public class ExponentialPower implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.ExponentialPower distribution;
    private double tau;

    public ExponentialPower(double tau) {
        distribution = new cern.jet.random.ExponentialPower(tau, engine);
        this.tau = tau;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "ExponentialPower(" + tau + ")";
    }
}
