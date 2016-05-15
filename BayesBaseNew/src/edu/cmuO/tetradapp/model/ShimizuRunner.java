package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.*;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the PC algorithm.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5940 $ 
 */
public class ShimizuRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public ShimizuRunner(DataWrapper dataWrapper, PcSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ShimizuRunner serializableInstance() {
        return new ShimizuRunner(DataWrapper.serializableInstance(),
                PcSearchParams.serializableInstance());
    }

    @Override
	public ImpliedOrientation getMeekRules() {
        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.isAggressivelyPreventCycles());
        rules.setKnowledge(getParams().getKnowledge());
        return rules;
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    @Override
	public void execute() {
        Object source = getDataSet();

        if (!(source instanceof RectangularDataSet)) {
            throw new IllegalArgumentException("Must be a rectangular data set.");
        }

        RectangularDataSet dataSet = (RectangularDataSet) source;

        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Must be continuous.");
        }

        GraphWithParameters result = Shimizu2006SearchOld.lingamDiscovery_DAG(dataSet, 0.05);
        System.out.println(result);

        Graph graph = result.getGraph();

        setResultGraph(graph);
    }

    @Override
	public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataSet();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        IndTestType testType = (getParams()).getIndTestType();
        return new IndTestFactory().getTest(dataModel, getParams(), testType);
    }

    @Override
	public Graph getGraph() {
        return getResultGraph();
    }

    @Override
	public boolean supportsKnowledge() {
        return true;
    }

    //========================== Private Methods ===============================//

    private boolean isAggressivelyPreventCycles(){
        SearchParams params = getParams();
        if(params instanceof MeekSearchParams){
           return ((MeekSearchParams)params).isAggressivelyPreventCycles();
        }
        return false;
    }

}
