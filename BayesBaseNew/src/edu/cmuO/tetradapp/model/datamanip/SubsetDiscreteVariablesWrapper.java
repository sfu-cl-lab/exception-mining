package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Add description
 *
 * @author Tyler Gibson
 * @version $Revision: 1 $ $Date: Jan 29, 2007 4:14:07 AM $
 */
public class SubsetDiscreteVariablesWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;


    public SubsetDiscreteVariablesWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The givan data must not be null");
        }
        DataModel model = data.getSelectedDataModel();
        if (!(model instanceof RectangularDataSet)) {
            throw new IllegalArgumentException("The given dataset must be tabular");
        }
        this.setDataModel(createModel((RectangularDataSet) model));
        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new SubsetDiscreteVariablesWrapper(new DataWrapper(DataUtils.continuousSerializableInstance()));
    }

    //=========================== Private Methods =================================//


    private static DataModel createModel(RectangularDataSet data) {
        for (int i = data.getNumColumns() -1; i >= 0; i--) {
            if (data.getVariable(i) instanceof DiscreteVariable) {
                data.removeColumn(i);
            }
        }
        return data;
    }


}
