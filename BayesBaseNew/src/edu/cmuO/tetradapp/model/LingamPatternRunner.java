package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Dag;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class LingamPatternRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;

    //============================CONSTRUCTORS============================//

    public LingamPatternRunner(DataWrapper dataWrapper, PcSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LingamStructureRunner serializableInstance() {
        return new LingamStructureRunner(DataWrapper.serializableInstance(),
                PcSearchParams.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    @Override
	public void execute() {
        DataModel source = getDataSet();

        if (!(source instanceof RectangularDataSet)) {
            throw new IllegalArgumentException("Expecting a rectangular data set.");
        }

        RectangularDataSet dataSet = (RectangularDataSet) source;

        if (!dataSet.isContinuous()) {
            throw new IllegalArgumentException("Expecting a continuous data set.");
        }

        LingamPattern lingamPattern = new LingamPattern();
        lingamPattern.setAlpha(getIndependenceTest().getAlpha());
        lingamPattern.setNumSamples(200);

        Cpc search = new Cpc(getIndependenceTest(), getParams().getKnowledge());
//        PcdSearch search = new PcdSearch(getIndependenceTest(), new Knowledge());
        Graph estPattern = search.search();

//        Graph estPattern = new PcSearch(test1, new Knowledge()).search();
//        Graph estPattern = new GesSearch(dataSet).search();
        List<Dag> dags = SearchGraphUtils.getDagsInPatternMeek(estPattern, getParams().getKnowledge());

        LingamPattern.Result result = lingamPattern.search(dags, dataSet);
        setResultGraph(result.getDags().get(0));
        GraphUtils.arrangeBySourceGraph(getResultGraph(), getSourceGraph());


        for (int i = 0; i < result.getDags().size(); i++) {
            System.out.println("\n\nModel # " + (i + 1) + " # votes = " + result.getCounts().get(i));
            System.out.println(result.getDags().get(i));
        }
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

    private boolean isAggressivelyPreventCycles() {
        SearchParams params = getParams();
        if (params instanceof MeekSearchParams) {
            return ((MeekSearchParams) params).isAggressivelyPreventCycles();
        }
        return false;
    }

    @Override
	public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt);
    }

    private void firePropertyChange(PropertyChangeEvent evt) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(evt);
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (!getListeners().contains(l)) getListeners().add(l);
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