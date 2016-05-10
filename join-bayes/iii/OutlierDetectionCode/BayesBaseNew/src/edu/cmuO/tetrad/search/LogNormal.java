package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import cern.jet.random.Normal;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:07 PM
* To change this template use File | Settings | File Templates.
*/
public class LogNormal implements SamplingDistribution {
    private RandomEngine distribution = PersistentRandomUtil.getInstance().getRandomEngine();
    private Normal normal = new Normal(0, 1, distribution);

    @Override
	public double drawSample() {
        return Math.exp(normal.nextDouble());
    }

    @Override
	public String toString() {
        return "LogNormal";
    }
}
