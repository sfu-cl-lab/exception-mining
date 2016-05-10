package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.util.Distribution;
import edu.cmu.tetrad.util.UniformDistribution;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Stores the parameters used to simulate data from a OldSem net.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5985 $ 
 */
public class Sem2DataParams implements Params {
    static final long serialVersionUID = 23L;

    private Distribution distribution = new UniformDistribution(-1, 1);

    /**
     * The sample size to generate.
     *
     * @serial Range > 0.
     */
    private int sampleSize = 1000;


    /**
     * States whether data should be gerenated for latent variables also.
     *
     * @serial
     */
    private boolean includeLatents = false;

    //==========================CONSTRUCTORS============================//

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public Sem2DataParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Sem2DataParams serializableInstance() {
        return new Sem2DataParams();
    }

    //==========================PUBLIC METHODS=========================//

    /**
     * Returns the number of samples to simulate.
     */
    public int getSampleSize() {
        return this.sampleSize;
    }

    /**
     * Sets the number of samples to generate.
     */
    public void setSampleSize(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException();
        }

        this.sampleSize = sampleSize;
    }


    public boolean getIncludeLatents(){
        return this.includeLatents;
    }


    public void setIncludeLatents(boolean include){
        this.includeLatents = include;
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

        if (sampleSize < 1) {
            throw new IllegalStateException("Sample size < 1: " + sampleSize);
        }
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }
}
