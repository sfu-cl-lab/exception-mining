package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:46 PM
* To change this template use File | Settings | File Templates.
*/
public class BreitWigner implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.BreitWigner distribution;
    private double mean;
    private double gamma;
    private double cut;

    public BreitWigner(double mean, double gamma, double cut) {
        distribution = new cern.jet.random.BreitWigner(mean, gamma, cut, engine);
        this.mean = mean;
        this.gamma = gamma;
        this.cut = cut;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "BreitWigner(" + mean + ", " + gamma + ", " + cut + ")";
    }
}
