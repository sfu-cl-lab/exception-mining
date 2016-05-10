package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:07:07 PM
* To change this template use File | Settings | File Templates.
*/
public class VonMises implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.VonMises distribution;
    private double freedom;

    public VonMises(double freedom) {
        distribution = new cern.jet.random.VonMises(freedom, engine);
        this.freedom = freedom;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "vonMises(" + freedom + ")";
    }
}
