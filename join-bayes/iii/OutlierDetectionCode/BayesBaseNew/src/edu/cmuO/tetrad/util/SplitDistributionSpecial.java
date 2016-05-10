package edu.cmu.tetrad.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;

/**
 * For given a, b (0 <= a < b), returns a point chosen uniformly from [-b, -a] U
 * [a, b].
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-10-26 12:43:45 -0400 (Wed, 26 Oct
 *          2005) $
 */

public class SplitDistributionSpecial implements Distribution {
    static final long serialVersionUID = 23L;

    /**
     * @serial
     */
    private double a;

    /**
     * @serial
     */
    private double b;

    //===========================CONSTRUCTORS===========================//

    /**
     * Creates a new split distribution.
     */
    public SplitDistributionSpecial(double a, double b) {
        if (a < 0) {
            throw new IllegalArgumentException(
                    "The value of a must be >= 0: " + a);
        }

        if (b <= a) {
            throw new IllegalArgumentException(
                    "The value of be must be > a: " + b);
        }

        this.a = a;
        this.b = b;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SplitDistributionSpecial serializableInstance() {
        return new SplitDistributionSpecial(0.5, 1.5);
    }

    //==========================PUBLIC METHODS==========================//

    /**
     * Returns a random value from [-b, -a] U [a, b].
     */
    @Override
	public double nextRandom() {
        if (PersistentRandomUtil.getInstance().nextDouble() < 0.05) {
            return -0.001 + PersistentRandomUtil.getInstance().nextDouble() *
                    0.002;
        }
        else {
            double c = PersistentRandomUtil.getInstance().nextDouble();
            double value = getA() + c * (getB() - getA());

            if (PersistentRandomUtil.getInstance().nextDouble() < 0.5) {
                value *= -1.0;
            }

            return value;
        }
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    @Override
	public String toString() {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        return "Split(" + nf.format(a) + ", " + nf.format(b) + ", " + ")";
    }


    @Override
	public void setParameter(int index, double value) {
        if (index == 0 && value < b) {
            a = value;
        }
        else if (index == 1 && value > a) {
            b = value;
        }
        else {
            throw new IllegalArgumentException("Cannot set value of " + index + " to " + value);
        }
    }

    @Override
	public double getParameter(int index) {
        if (index == 0) {
            return a;
        }
        else if (index == 1) {
            return b;
        }
        else {
            throw new IllegalArgumentException("There is no parameter " + index);
        }
    }

    @Override
	public String getParameterName(int index) {
        if (index == 0) {
            return "Lower bound (> 0)";
        }
        else if (index == 1) {
            return "Upper bound (> 0)";
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
        return "Split Distribution Special";
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

        if (a < 0) {
            throw new IllegalStateException();
        }

        if (b <= a) {
            throw new IllegalStateException();
        }
    }
}
