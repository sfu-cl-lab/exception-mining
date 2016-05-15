package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Add description
 *
 * @author Tyler Gibson
 * @version $Revision: 1 $ $Date: Jan 29, 2007 2:41:06 AM $
 */
public class RemoveMissingValueCasesWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * Constructs the missing value cases wrapper.
     *
     * @param data
     */
    public RemoveMissingValueCasesWrapper(DataWrapper data){
        if(data == null){
            throw new NullPointerException("The givan data must not be null");
        }
        DataModel model = data.getSelectedDataModel();
        if((!(model instanceof RectangularDataSet))){
            throw new IllegalArgumentException("Data must be tabular");
        }
        RemoveMissingCasesDataFilter filter = new RemoveMissingCasesDataFilter();
        this.setDataModel(filter.filter((RectangularDataSet)model));
        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static RemoveMissingValueCasesWrapper serializableInstance() {
        return new RemoveMissingValueCasesWrapper(DataWrapper.serializableInstance());
    }


}
