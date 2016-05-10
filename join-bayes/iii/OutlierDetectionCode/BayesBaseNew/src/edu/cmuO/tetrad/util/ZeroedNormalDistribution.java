package edu.cmu.tetrad.util;

import cern.jet.random.Normal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;

/**
 * A normal distribution that allows its parameters to be set and allows
 * random sampling. The parameters are 0 = mean, 1 = standard deviation.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5811 $
 */
public class ZeroedNormalDistribution implements Distribution {
    static final long serialVersionUID = 23L;

    /**
     * The standard devision of the distribution.
     * @serial
     */
    private double stDev;

    /**
     * The wrapped COLT distribution.
     */
    private Normal normal;

    //=========================CONSTRUCTORS===========================//

    public ZeroedNormalDistribution(double sd) {
        normal = new Normal(0, stDev, PersistentRandomUtil.getInstance().getRandomEngine());
        setParameter(0, sd);
    }

    //=========================PUBLIC METHODS=========================//

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ZeroedNormalDistribution serializableInstance() {
        return new ZeroedNormalDistribution(1);
    }

    @Override
	public void setParameter(int index, double value) {
        if (index == 0 && value >= 0) {
            stDev = value;
        }
        else {
            throw new IllegalArgumentException("Illegal value for parameter " +
                    index + ": " + value);
        }

        resetDistribution(0, stDev);
    }

    @Override
	public double getParameter(int index) {
        if (index == 0) {
            return stDev;
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
            return "Standard Deviation";
        }
        else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    /**
     * Returns the number of parameters = 2.
     */
    @Override
	public int getNumParameters() {
        return 2;
    }

    /**
     * Returns the next random sample from the distribution.
     */
    @Override
	public double nextRandom() {
        return normal.nextDouble();
    }


    @Override
	public String getName() {
        return "Zeroed Normal";
    }

    @Override
	public String toString() {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        return "Normal(" + 0 + ", " + nf.format(stDev) + ")";
    }

    //========================PRIVATE METHODS===========================//

    private void resetDistribution(double mean, double sd) {
        normal.setState(mean, sd);
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

        if (stDev <= 0) {
            throw new IllegalStateException();
        }

    }
}
