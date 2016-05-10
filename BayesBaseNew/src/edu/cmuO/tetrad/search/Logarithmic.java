package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:43 PM
* To change this template use File | Settings | File Templates.
*/
public class Logarithmic implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Logarithmic distribution;
    private double p;

    public Logarithmic(double p) {
        distribution = new cern.jet.random.Logarithmic(p, engine);
        this.p = p;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Logarithmic(" + p + ")";
    }
}
