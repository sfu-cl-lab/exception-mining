package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * @author Tyler
 * @version $Revision: 1.1 $ $Date: Jan 27, 2007 11:10:09 PM $
 */
public class TimeSeriesWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * Constructs a new time series dataset.
     *
     * @param data   - Previous data (from the parent node)
     * @param params - The parameters.
     */
    public TimeSeriesWrapper(DataWrapper data, TimeSeriesParams params) {
        DataModel model = data.getSelectedDataModel();
        if (!(model instanceof RectangularDataSet)) {
            throw new IllegalArgumentException("The data model must be a rectangular dataset");
        }
        model = DataUtils.createTimeSeriesData((RectangularDataSet) model, params.getNumOfTimeLags());
        this.setDataModel(model);
        this.setSourceGraph(data.getSourceGraph());
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new TimeSeriesWrapper(DataWrapper.serializableInstance(),
                TimeSeriesParams.serializableInstance());
    }

    //=============================== Private Methods =========================//


}
