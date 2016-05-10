package edu.cmu.tetradapp.model.calculator;

import edu.cmu.tetrad.model.Params;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Params for the calculator feature.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Jan 28, 2007 10:43:24 PM $
 */
public class CalculatorParams implements Params {

    static final long serialVersionUID = 23L;


    /**
     * String representation of the equations used by the calculator.
     */
    private List<String> equations = new ArrayList<String>(5);


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CalculatorParams serializableInstance() {
        return new CalculatorParams();
    }

    //=============== Public Methods ===================//


    /**
     * Returns the equations used in the calculator.
     */
    public List<String> getEquations(){
        return this.equations;
    }


    /**
     * Adds the given equation to the params
     */
    public void addEquation(String eq){
        this.equations.add(eq);
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
        if(equations == null){
            throw new NullPointerException("equations was null.");
        }

    }
}
