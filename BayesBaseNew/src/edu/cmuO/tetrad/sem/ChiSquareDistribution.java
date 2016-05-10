package edu.cmu.tetrad.sem;

import cern.jet.random.ChiSquare;
import cern.jet.random.engine.MersenneTwister;
import edu.cmu.tetrad.util.Distribution;
import edu.cmu.tetrad.util.PersistentRandomUtil;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a chi squarew distribution, allowing the values of its parameters to
 * be modified and generating random data.
 *
 * @author Joseph Ramsey
 */
public class ChiSquareDistribution implements Distribution {
    static final long serialVersionUID = 23L;

    /**
     * The stored degees of freedom. Needed because the wrapped distribution
     * does not provide getters for its parameters.
     */
    private double df = 5.0;

    /**
     * The wrapped distribution. For some reason, COLT's ChiSquare class
     * is not serializable, so we have to compensate using readObject.
     */
    private transient ChiSquare distribution;

    /**
     * Constructs a new Chi Square distribution.
     */
    public ChiSquareDistribution(double df) {
        distribution = initializeDistribution(df);
        this.df = df;
    }

    private ChiSquare initializeDistribution(double df) {
        return new ChiSquare(df, PersistentRandomUtil.getInstance().getRandomEngine());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ChiSquareDistribution serializableInstance() {
        return new ChiSquareDistribution(5.0);
    }

    @Override
	public double nextRandom() {
        return distribution.nextDouble();
    }

    @Override
	public void setParameter(int index, double value) {
        if (index == 0 && value >= 0.0) {
            df = value;
            distribution = new ChiSquare(5.0, new MersenneTwister());
        }
        else {
            throw new IllegalArgumentException("Illegal value: " + index + " = " + value);
        }
    }

    @Override
	public double getParameter(int index) {
        if (index == 0) {
            return df;
        }
        else {
            throw new IllegalArgumentException("Illegal index: " + index);
        }
    }

    @Override
	public String getParameterName(int index) {
        if (index == 0) {
            return "DF";
        }
        else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    @Override
	public int getNumParameters() {
        return 1;
    }


    @Override
	public String getName() {
        return "Chi Square";
    }

    @Override
	public String toString() {
        return "ChiSquare(" + df + ")";
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        distribution = initializeDistribution(df);
    }
}
