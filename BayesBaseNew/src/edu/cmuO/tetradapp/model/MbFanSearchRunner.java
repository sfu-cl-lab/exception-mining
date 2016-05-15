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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.MbFanSearch;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the MB Fan Search
 * algorithm.
 *
 * @author Frank Wimberly after Joe Ramsey's PcRunner
 * @version $Revision: 6039 $ $Date: 2006-01-06 23:02:37 -0500 (Fri, 06 Jan
 *          2006) $
 */
public class MbFanSearchRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * Stores a reference to the most recent MB fan search, in case the list of
     * compatible DAGs is needed for display.
     */
    private transient MbFanSearch mbFanSearch;

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public MbFanSearchRunner(DataWrapper dataWrapper, MbSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public MbFanSearchRunner(Graph graph, MbSearchParams params) {
        super(graph, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public MbFanSearchRunner(GraphWrapper dagWrapper, MbSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public MbFanSearchRunner(DagWrapper dagWrapper, MbSearchParams params) {
        super(dagWrapper.getDag(), params);
    }

    public MbFanSearchRunner(SemGraphWrapper dagWrapper,
                             BasicSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MbFanSearchRunner serializableInstance() {
        return new MbFanSearchRunner(DataWrapper.serializableInstance(),
                MbSearchParams.serializableInstance());
    }

    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    @Override
	public void execute() {
        int pcDepth = ((MbSearchParams) getParams()).getDepth();
        MbFanSearch mbfs =
                new MbFanSearch(getIndependenceTest(), pcDepth);
        SearchParams params = getParams();
        if (params instanceof MeekSearchParams) {
            mbfs.setAggressivelyPreventCycles(((MeekSearchParams) params).isAggressivelyPreventCycles());
        }
        Knowledge knowledge = getParams().getKnowledge();
        mbfs.setKnowledge(knowledge);
        String targetName = ((MbSearchParams) getParams()).getTargetName();
        Graph searchGraph = mbfs.search(targetName);
        setResultGraph(searchGraph);
        boolean arrangedAll = GraphUtils.arrangeBySourceGraph(getResultGraph(),
                getSourceGraph());

        if (!arrangedAll) {
            GraphUtils.arrangeInCircle(getResultGraph(), 200, 200, 150);
        }

        this.mbFanSearch = mbfs;
    }

    @Override
	public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataSet();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        MbSearchParams params = (MbSearchParams) getParams();
        IndTestType testType = params.getIndTestType();
        return new IndTestFactory().getTest(dataModel, params, testType);
    }

    public MbFanSearch getMbFanSearch() {
        if (mbFanSearch == null) {
            execute();
        }

        return mbFanSearch;
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
    }

    @Override
	public Graph getGraph() {
        return getResultGraph();
    }

    @Override
	public boolean supportsKnowledge() {
        return true;
    }
}


