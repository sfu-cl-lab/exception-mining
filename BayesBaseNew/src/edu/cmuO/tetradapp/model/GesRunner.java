///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.GesSearch;
import edu.cmu.tetrad.search.ImpliedOrientation;
import edu.cmu.tetrad.search.MeekRules;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the GES algorithm.
 *
 * @author Ricardo Silva
 */

public class GesRunner extends AbstractAlgorithmRunner implements GraphSource,
        PropertyChangeListener {
    static final long serialVersionUID = 23L;
    private transient List<PropertyChangeListener> listeners;

    //============================CONSTRUCTORS============================//

    public GesRunner(DataWrapper dataWrapper, GesParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GesRunner serializableInstance() {
        return new GesRunner(DataWrapper.serializableInstance(),
                GesParams.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */

    @Override
	public void execute() {
        GesSearch gesSearch;
        Object source = getDataSet();

        if (source instanceof CovarianceMatrix) {
            gesSearch = new GesSearch((CovarianceMatrix) source);
            gesSearch.addPropertyChangeListener(this);
            gesSearch.setAggressivelyPreventCycles(this.isAggressivelyPreventCycles());
            gesSearch.setKnowledge(getParams().getKnowledge());
            gesSearch.setSamplePrior(((GesParams) getParams()).getCellPrior());
            gesSearch.setStructurePrior(
                    ((GesParams) getParams()).getStructurePrior());
        } else if (source instanceof RectangularDataSet) {
            gesSearch = new GesSearch((RectangularDataSet) source);
            gesSearch.addPropertyChangeListener(this);
            gesSearch.setAggressivelyPreventCycles(this.isAggressivelyPreventCycles());
            gesSearch.setKnowledge(getParams().getKnowledge());
            gesSearch.setSamplePrior(((GesParams) getParams()).getCellPrior());
            gesSearch.setStructurePrior(
                    ((GesParams) getParams()).getStructurePrior());
        } else {
            throw new RuntimeException(
                    "GES does not accept this type of data set.");
        }

        Graph searchGraph = gesSearch.search();
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


