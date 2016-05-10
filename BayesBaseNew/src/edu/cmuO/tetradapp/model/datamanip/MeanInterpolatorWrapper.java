package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.bayes.MeanInterpolator;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetradapp.model.DataWrapper;

/**
 * Add description
 *
 * @author Tyler Gibson
 * @version $Revision: 1 $ $Date: Jan 29, 2007 3:16:02 AM $
 */
public class MeanInterpolatorWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;



    public MeanInterpolatorWrapper(DataWrapper data) {
        if (data == null) {
            throw new NullPointerException("The givan data must not be null");
        }
        DataModel model = data.getSelectedDataModel();
        if ((!(model instanceof RectangularDataSet))) {
            throw new IllegalArgumentException("Data must be tabular");
        }
        DataFilter interpolator = new MeanInterpolator();
        this.setDataModel(interpolator.filter((RectangularDataSet)model));
        this.setSourceGraph(data.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MeanInterpolatorWrapper serializableInstance() {
        return new MeanInterpolatorWrapper(DataWrapper.serializableInstance());
    }


}
