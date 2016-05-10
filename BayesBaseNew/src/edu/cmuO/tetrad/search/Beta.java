package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:38 PM
* To change this template use File | Settings | File Templates.
*/
public class Beta implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Beta distribution;
    private double a;
    private double b;

    public Beta(double a, double b) {
        distribution = new cern.jet.random.Beta(a, b, engine);
        this.a = a;
        this.b = b;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Beta(" + a + ", " + b + ")";
    }
}
