package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:06:35 PM
* To change this template use File | Settings | File Templates.
*/
public class Hyperbolic implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private cern.jet.random.Hyperbolic distribution;
    private double alpha;
    private double gamma;

    public Hyperbolic(double alpha, double gamma) {
        distribution = new cern.jet.random.Hyperbolic(alpha, gamma, engine);
        this.alpha = alpha;
        this.gamma = gamma;
    }

    @Override
	public double drawSample() {
        return distribution.nextDouble();
    }

    @Override
	public String toString() {
        return "Hyperbolic(" + alpha + ", " + gamma + ")";
    }
}
