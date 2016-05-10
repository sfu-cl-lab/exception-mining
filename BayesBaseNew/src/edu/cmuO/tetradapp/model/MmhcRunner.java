package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.*;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class MmhcRunner extends AbstractAlgorithmRunner implements GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    public MmhcRunner(DataWrapper dataWrapper, BasicSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MmhcRunner serializableInstance() {
        return new MmhcRunner(DataWrapper.serializableInstance(),
                BasicSearchParams.serializableInstance());
    }                                                     

    //============================PUBLIC METHODS==========================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    @Override
	public void execute() {
        VanderbiltMmhc search;
        Object source = getDataSet();

        search = new VanderbiltMmhc(getIndependenceTest(), 3);
        search.setKnowledge(getParams().getKnowledge());

        Graph searchGraph = search.search();
        setResultGraph(searchGraph);
        GraphUtils.arrangeBySourceGraph(getResultGraph(), getSourceGraph());
    }

    @Override
	public Graph getGraph() {
        return getResultGraph();
    }

    @Override
	public boolean supportsKnowledge() {
        return true;
    }

    @Override
	public ImpliedOrientation getMeekRules() {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(getParams().getKnowledge());
        return rules;
    }

    public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataSet();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        IndTestType testType = (getParams()).getIndTestType();
        return new IndTestFactory().getTest(dataModel, getParams(), testType);
    }
}
