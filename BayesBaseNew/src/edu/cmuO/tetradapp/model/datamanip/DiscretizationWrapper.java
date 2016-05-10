package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.model.DataWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tyler
 * @version $Revision: 1.1 $ $Date: Jan 24, 2007 10:23:36 PM $
 */
public class DiscretizationWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    /**
     * The discretized data set.
     *
     * @serial Not null.
     */
    private RectangularDataSet discretizedDataSet;


    /**
     * Constructs the <code>DiscretizationWrapper</code> by discretizing the select
     * <code>DataModel</code>.
     *
     * @param data
     * @param params
     */
    public DiscretizationWrapper(DataWrapper data, DiscretizationParams params) {
        if (data == null) {
            throw new NullPointerException("The given data must not be null");
        }
        if (params == null) {
            throw new NullPointerException("The given parameters must not be null");
        }
        RectangularDataSet originalData = (RectangularDataSet) data.getSelectedDataModel();
        this.discretizedDataSet = getDiscretizedDataSet(originalData, params);
        setDataModel(this.discretizedDataSet);
        setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DiscretizationWrapper serializableInstance() {
        return new DiscretizationWrapper(DataWrapper.serializableInstance(),
                DiscretizationParams.serializableInstance());
    }

    //=============================== Private Methods =========================//

    /**
     * Returns a new data set that has been discretized.
     *
     * @return - Discretized dataset.
     */
    private static RectangularDataSet getDiscretizedDataSet(RectangularDataSet sourceDataSet,
                                                            DiscretizationParams params) {
        // build list of variable.s
        List<Node> variables = new LinkedList<Node>();
        boolean copy = Preferences.userRoot().getBoolean("copyUnselectedColumns", false);
        Map<Node, ContinuousDiscretizationSpec> specsMap = params.getSpecs();
        Map<Node, Node> replacementMapping = new HashMap<Node, Node>();
        for (int i = 0; i < sourceDataSet.getNumColumns(); i++) {
            Node variable = sourceDataSet.getVariable(i);
            if (variable instanceof ContinuousVariable) {
                ContinuousDiscretizationSpec spec = specsMap.get(variable);
                if (spec != null) {
                    List<String> cats = spec.getCategories();
                    DiscreteVariable var = new DiscreteVariable(variable.getName(), cats);
                    replacementMapping.put(var, variable);
                    variables.add(var);
                } else if (copy) {
                    variables.add(variable);
                }
            } else if (copy) {
                variables.add(variable);
            }
        }

        // build new dataset.
       ColtDataSet newDataSet = new ColtDataSet(sourceDataSet.getNumRows(), variables);
        for (int i = 0; i < newDataSet.getNumColumns(); i++) {
            Node variable = newDataSet.getVariable(i);
            Node sourceVar = replacementMapping.get(variable);
            if (sourceVar != null && specsMap.containsKey(sourceVar)) {
                ContinuousDiscretizationSpec spec = specsMap.get(sourceVar);
                double[] breakpoints = spec.getBreakpoints();
                List<String> categories = spec.getCategories();
                String name = variable.getName();

                double[] trimmedData = new double[newDataSet.getNumRows()];
                int col = newDataSet.getColumn(variable);

                for (int j = 0; j < sourceDataSet.getNumRows(); j++) {
                    trimmedData[j] = sourceDataSet.getDouble(j, col);
                }
                Discretization discretization = Discretization.discretize(trimmedData,
                        breakpoints, name, categories);

                int _col = newDataSet.getColumn(variable);
                int[] _data = discretization.getData();
                for (int j = 0; j < _data.length; j++) {
                    newDataSet.setInt(j, _col, _data[j]);
                }
            } else {
                DataUtils.copyColumn(variable, sourceDataSet, newDataSet);
            }
        }
        return newDataSet;
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

        if (this.discretizedDataSet == null) {
            throw new NullPointerException();
        }
    }


}
