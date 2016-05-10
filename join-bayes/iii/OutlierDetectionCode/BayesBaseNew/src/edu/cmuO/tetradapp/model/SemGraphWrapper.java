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

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Holds a tetrad dag with all of the constructors necessary for it to serve as
 * a model for the tetrad application.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5870 $ $Date: 2005-07-02 01:53:33 -0400 (Sat, 02 Jul
 *          2005) $
 */
public class SemGraphWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private SemGraph semGraph;

    //=============================CONSTRUCTORS==========================//

    public SemGraphWrapper(SemGraph graph) {
        if (graph == null) {
            throw new NullPointerException("MAG must not be null.");
        }
        this.semGraph = graph;
        this.semGraph.setShowErrorTerms(false);
        log();
    }

    public SemGraphWrapper(GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            semGraph = new SemGraph();
            semGraph.setShowErrorTerms(false);
        }
        else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public SemGraphWrapper(SemGraphWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.semGraph = new SemGraph(graphWrapper.getSemGraph());
            this.semGraph.setShowErrorTerms(false);
        }
        else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public SemGraphWrapper(DagWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.semGraph = new SemGraph(graphWrapper.getDag());
            this.semGraph.setShowErrorTerms(false);
        }
        else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public SemGraphWrapper(GraphWrapper graphWrapper, GraphParams params) {
        if (params.getInitializationMode() == GraphParams.MANUAL) {
            this.semGraph = new SemGraph(graphWrapper.getGraph());
            this.semGraph.setShowErrorTerms(false);
        }
        else if (params.getInitializationMode() == GraphParams.RANDOM) {
            createRandomDag(params);
        }
        log();
    }

    public SemGraphWrapper(AbstractAlgorithmRunner wrapper) {
        this(new SemGraph(wrapper.getResultGraph()));
    }

    public SemGraphWrapper(DataWrapper wrapper) {
        this(new SemGraph(new EdgeListGraph(wrapper.getVariables())));
    }

    public SemGraphWrapper(BayesPmWrapper wrapper) {
        this(new SemGraph(wrapper.getBayesPm().getDag()));
    }

    public SemGraphWrapper(BayesImWrapper wrapper) {
        this(new SemGraph(wrapper.getBayesIm().getBayesPm().getDag()));
    }

    public SemGraphWrapper(BayesEstimatorWrapper wrapper) {
        this(new SemGraph(wrapper.getEstimatedBayesIm().getBayesPm().getDag()));
    }

    public SemGraphWrapper(CptInvariantUpdaterWrapper wrapper) {
        this(new SemGraph(wrapper.getBayesUpdater().getManipulatedGraph()));
    }

    public SemGraphWrapper(SemPmWrapper wrapper) {
        this(new SemGraph(wrapper.getSemPm().getGraph()));
    }

    public SemGraphWrapper(SemImWrapper wrapper) {
        this(new SemGraph(wrapper.getSemIm().getSemPm().getGraph()));
    }

    public SemGraphWrapper(SemEstimatorWrapper wrapper) {
        this(new SemGraph(wrapper.getSemEstimator().getEstimatedSem().getSemPm()
                .getGraph()));
    }

    public SemGraphWrapper(RegressionRunner wrapper) {
        this(new SemGraph(wrapper.getResultGraph()));
    }

    public SemGraphWrapper(BuildPureClustersRunner wrapper) {
        this(new SemGraph(wrapper.getResultGraph()));
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemGraphWrapper serializableInstance() {
        return new SemGraphWrapper(SemGraph.serializableInstance());
    }

    //================================PUBLIC METHODS=======================//

    public SemGraph getSemGraph() {
        return semGraph;
    }

    public void setSemGraph(SemGraph graph) {
        this.semGraph = graph;
        this.semGraph.setShowErrorTerms(false);
    }

    //============================PRIVATE METHODS========================//


    private void log(){
        TetradLogger.getInstance().setTetradLoggerConfigForModel(this.getClass());
        TetradLogger.getInstance().info("Graph type = SEM Graph");
        TetradLogger.getInstance().log("graph", "Graph = " + semGraph);
        TetradLogger.getInstance().reset();
    }


    private void createRandomDag(GraphParams params) {
        Dag randomDagD = GraphUtils.createRandomDag(params.getNumNodes(),
                params.getNumLatents(), params.getMaxEdges(),
                params.getMaxDegree(), params.getMaxIndegree(),
                params.getMaxOutdegree(), params.isConnected());
        semGraph = new SemGraph(randomDagD);
        semGraph.setShowErrorTerms(false);
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (semGraph == null) {
            throw new NullPointerException();
        }
    }

    @Override
	public Graph getGraph() {
        return semGraph;
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }
}


