package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:54 PM
* To change this template use File | Settings | File Templates.
*/
public class ChiSquare implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.ChiSquare distribution;
    private double df;

    public ChiSquare(double df) {
        distribution = new cern.jet.random.ChiSquare(df, engine);
        this.df = df;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "ChiSquare(" + df + ")";
    }
}
