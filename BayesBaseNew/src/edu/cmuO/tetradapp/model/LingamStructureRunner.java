package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.search.*;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class LingamStructureRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;

    //============================CONSTRUCTORS============================//

    public LingamStructureRunner(DataWrapper dataWrapper, PcSearchParams params) {
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

        RectangularDataSet data = (RectangularDataSet) source;

        if (!data.isContinuous()) {
            throw new IllegalArgumentException("Expecting a continuous data set.");
        }

        Lingam lingam = new Lingam(getParams().getIndTestParams().getAlpha());
        lingam.setPruningDone(true);
        lingam.setAlpha(getParams().getIndTestParams().getAlpha());
        GraphWithParameters result = lingam.lingam(data);
        setResultGraph(result.getGraph());
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
}
