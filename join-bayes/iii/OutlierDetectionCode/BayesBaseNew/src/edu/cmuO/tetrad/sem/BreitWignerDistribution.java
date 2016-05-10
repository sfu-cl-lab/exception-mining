package edu.cmu.tetrad.sem;

import cern.jet.random.BreitWigner;
import cern.jet.random.engine.MersenneTwister;
import edu.cmu.tetrad.util.Distribution;
import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Wraps a breitWigner distribution, allowing the values of its parameters to
 * be modified, generating random data.
 *
 * @author Joseph Ramsey
 */
public class BreitWignerDistribution implements Distribution {
    static final long serialVersionUID = 23L;

    private double mean = 0;
    private double gamma = 1;
    private double cut = 2;

    BreitWigner breitWigner = new BreitWigner(mean, gamma, cut,
            PersistentRandomUtil.getInstance().getRandomEngine());

    public BreitWignerDistribution(double mean, double gamma, double cut) {
        breitWigner = new BreitWigner(mean, gamma, cut,
                PersistentRandomUtil.getInstance().getRandomEngine());
        this.mean = mean;
        this.gamma = gamma;
        this.cut = cut;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BreitWignerDistribution serializableInstance() {
        return new BreitWignerDistribution(0, 1, 2);
    }

    @Override
	public double nextRandom() {
        return breitWigner.nextDouble();
    }

    @Override
	public void setParameter(int index, double value) {
        if (index == 0) {
            mean = value;
        }
        else if (index == 1) {
            gamma = value;
        }
        else if (index == 2) {
            cut = value;
        }
        else {
            throw new IllegalArgumentException("Illegal value: " + value);
        }

        breitWigner = new BreitWigner(mean, gamma, cut, new MersenneTwister());
    }

    @Override
	public double getParameter(int index) {
        if (index == 0) {
            return mean;
        }
        else if (index == 1) {
            return gamma;
        }
        else if (index == 2) {
            return cut;
        }
        else {
            throw new IllegalArgumentException("Illegal index: " + index);
        }
    }

    @Override
	public String getParameterName(int index) {
        if (index == 0) {
            return "Mean";
        }
        else if (index == 1) {
            return "Gamma";
        }
        else if (index == 2) {
            return "Cut";
        }
        else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    @Override
	public int getNumParameters() {
        return 2;
    }


    @Override
	public String getName() {
        return "Breit Wigner";
    }
}
