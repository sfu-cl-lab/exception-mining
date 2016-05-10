package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.model.Params;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Jan 27, 2007 11:09:53 PM $
 */
public class TimeSeriesParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     * The number of time lags
     */
    private int numOfTimeLags = 2;


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static TimeSeriesParams serializableInstance() {
        return new TimeSeriesParams();
    }

    //====================== Public Methods ===============================//


    public void setNumOfTimeLags(int timeLags) {
        if (timeLags < 2 || 20 < timeLags) {
            throw new IllegalArgumentException("Must be between 2 and 20");
        }
        this.numOfTimeLags = timeLags;
    }


    public int getNumOfTimeLags() {
        return this.numOfTimeLags;
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

        if (this.numOfTimeLags < 2 || 20 < this.numOfTimeLags) {
            throw new IllegalStateException("Time of lags must be between 2 and 20");
        }
    }


}
