package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:07:01 PM
* To change this template use File | Settings | File Templates.
*/
public class Uniform implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Uniform distribution;
    private double min;
    private double max;

    public Uniform(double min, double max) {
        distribution = new cern.jet.random.Uniform(min, max, engine);
        this.min = min;
        this.max = max;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Uniform(" + min + ", " + max + ")";
    }
}
