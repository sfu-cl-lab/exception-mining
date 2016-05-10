package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemIm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class PValueImproverWrapper extends AbstractAlgorithmRunner implements
        GraphSource {
    static final long serialVersionUID = 23L;
    private String name;
    private SemIm semIm;
    private transient List<PropertyChangeListener> listeners;
    private DataWrapper dataWrapper;
    private GesParams params;
    private SemIm estSem;
    private Graph trueDag;

    //============================CONSTRUCTORS============================//

    public PValueImproverWrapper(SemImWrapper semImWrapper,
                                DataWrapper dataWrapper,
                                GesParams params) {
        super(dataWrapper, params);
        this.dataWrapper = dataWrapper;
        this.params = params;
        this.semIm = semImWrapper.getSemIm();
    }

    public PValueImproverWrapper(SemEstimatorWrapper semEstimatorWrapper,
                                DataWrapper dataWrapper,
                                GesParams params) {
        super(dataWrapper, params);
        this.dataWrapper = dataWrapper;
        this.params = params;
        this.semIm = semEstimatorWrapper.getEstimatedSemIm();
    }

    public PValueImproverWrapper(DataWrapper dataWrapper,
                                GesParams params) {
        super(dataWrapper, params);
        this.dataWrapper = dataWrapper;
        this.params = params;
    }

    public PValueImproverWrapper(SemEstimatorWrapper semEstimatorWrapper,
                                DataWrapper dataWrapper,
                                GraphWrapper dagWrapper,
                                GesParams params) {
        super(dataWrapper, params);
        this.dataWrapper = dataWrapper;
        this.params = params;
        this.trueDag = dagWrapper.getGraph();
        this.semIm = semEstimatorWrapper.getEstimatedSemIm();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static PValueImproverWrapper serializableInstance() {
        return new PValueImproverWrapper(SemImWrapper.serializableInstance(),
                DataWrapper.serializableInstance(),
                GesParams.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//


    @Override
	public void setName(String name) {
        this.name = name;
    }

    @Override
	public String getName() {
        return this.name;
    }

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    @Override
	public void execute() {
        Object source = dataWrapper.getSelectedDataModel();

        if (!(source instanceof RectangularDataSet)) {
            throw new RuntimeException("Sem Score Search requires a rectangular " +
                    "dataset.");
        }

        RectangularDataSet dataSet = (RectangularDataSet) source;
        Knowledge knowledge = params.getKnowledge();
        PValueImprover2 search = null;
        Graph graph;

        if (semIm != null) {
            search = new PValueImprover2(semIm, dataSet, knowledge);
            search.setKnowledge(knowledge);
            fireGraphChange(semIm.getSemPm().getGraph());
            search.setTrueDag(trueDag);
            this.estSem = search.search();
            graph = estSem.getSemPm().getGraph();
        }
        else {
            search = new PValueImprover2(dataSet, knowledge);
            search.setKnowledge(knowledge);
            graph = search.search3();
        }


        GraphUtils.arrangeBySourceGraph(graph, getSourceGraph());
        setResultGraph(graph);
    }

    @Override
	public boolean supportsKnowledge() {
        return true;
    }

    @Override
	public ImpliedOrientation getMeekRules() {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(params.getKnowledge());
        return rules;
    }

    private boolean isAggressivelyPreventCycles() {
        return params.isAggressivelyPreventCycles();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (!getListeners().contains(l)) getListeners().add(l);
    }

    private void fireGraphChange(Graph graph) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(new PropertyChangeEvent(this, "graph", null, graph));
        }
    }

    public SemIm getSemIm() {
        return semIm;
    }

    public SemIm getEstSem() {
        return estSem;
    }

    @Override
	public Graph getGraph() {
        return getResultGraph();
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }
}
