package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.graph.Graph;

/**
 * This has to be left here, due to a mistake in serialization. Sorry.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5940 $
 * @deprecated Use ShimuzuRunner instead.
 */
@Deprecated
public class ShimizuRunner2 extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public ShimizuRunner2(DataWrapper dataWrapper, PcSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ShimizuRunner2 serializableInstance() {
        return new ShimizuRunner2(DataWrapper.serializableInstance(),
                PcSearchParams.serializableInstance());
    }

    @Override
	public void execute() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
	public IndependenceTest getIndependenceTest() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
	public Graph getGraph() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
