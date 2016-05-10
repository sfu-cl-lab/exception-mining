package edu.cmu.tetradapp.model.calculator;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetradapp.model.DataWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.List;

/**
 * @author Tyler
 * @version $Revision: 1.1 $ $Date: Jan 24, 2007 10:23:36 PM $
 */
public class CalculatorWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    /**
     * Constructs the <code>DiscretizationWrapper</code> by discretizing the select
     * <code>DataModel</code>.
     *
     * @param data
     * @param params
     */
    public CalculatorWrapper(DataWrapper data, CalculatorParams params) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }
        if (params == null) {
            throw new NullPointerException("The given parameters must not be null");
        }
        RectangularDataSet copy = copy((RectangularDataSet) data.getSelectedDataModel());
        List<String> eqs = params.getEquations();
        if (!eqs.isEmpty()) {
            try {
                Transformation.transform(copy, eqs.toArray(new String[eqs.size()]));
            } catch (ParseException e) {
                throw new IllegalStateException("Was given unparsable expressions.");
            }
            setDataModel(copy);
        } else {
            setDataModel(copy);
        }
        setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CalculatorWrapper serializableInstance() {
        return new CalculatorWrapper(DataWrapper.serializableInstance(),
                CalculatorParams.serializableInstance());
    }

    //=============================== Private Methods =========================//


    private static RectangularDataSet copy(RectangularDataSet data) {
        if (data instanceof ColtDataSet) {
            return new ColtDataSet((ColtDataSet) data);
        }
        RectangularDataSet copy = new ColtDataSet(data.getNumRows(), data.getVariables());
        int cols = data.getNumColumns();
        int rows = data.getNumRows();
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                copy.setDouble(row, col, data.getDouble(row, col));
            }
        }
        return copy;
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
    @SuppressWarnings({"MethodMayBeStatic"})
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

    }


}
