package edu.cmu.tetrad.search;

import cern.jet.random.engine.RandomEngine;
import cern.jet.random.Normal;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 15, 2008 Time: 5:05:28 PM
* To change this template use File | Settings | File Templates.
*/
public class MixtureOfGaussians implements SamplingDistribution {
    private RandomEngine engine = PersistentRandomUtil.getInstance().getRandomEngine();
    private double a;
    private Normal normal1;
    private Normal normal2;
    private double mean1;
    private double mean2;
    private double var1;
    private double var2;

    public MixtureOfGaussians(double a, double mean1, double var1, double mean2, double var2) {
        if (a < 0 || a > 1) {
            throw new IllegalArgumentException();
        }

        if (var1 <= 0) {
            throw new IllegalArgumentException();
        }

        if (var2 <= 0) {
            throw new IllegalArgumentException();
        }

        this.a = a;
        this.mean1 = mean1;
        this.mean2 = mean2;
        this.var1 = var1;
        this.var2 = var2;
        normal1 = new Normal(mean1, var1, engine);
        normal2 = new Normal(mean2, var2, engine);
    }

    @Override
	public double drawSample() {
        double r = PersistentRandomUtil.getInstance().nextDouble();

        if (r < a) {
            return normal1.nextDouble();
        } else {
            return normal2.nextDouble();
        }
    }

    @Override
	public String toString() {
        return "MixtureOfGaussians(" + a + ", " + mean1 + ", " + var1 + ", " + mean2 + ", " + var2 + ")";
    }
}
