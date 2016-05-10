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
import edu.cmu.tetrad.search.CcdSearch;
import edu.cmu.tetrad.search.IndTestType;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.SearchGraphUtils;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the CCD algorithm.
 *
 * @author Frank Wimberly after Shane Harwood's PcRunner
 * @version $Revision: 5940 $ $Date: 2006-01-06 23:02:37 -0500 (Fri, 06 Jan
 *          2006) $
 */

public class CcdRunner extends AbstractAlgorithmRunner
        implements IndTestProducer, GraphSource {
    static final long serialVersionUID = 23L;

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public CcdRunner(DataWrapper dataWrapper, BasicSearchParams params) {
        super(dataWrapper, params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CcdRunner(GraphWrapper graphWrapper, BasicSearchParams params) {
        super(graphWrapper.getGraph(), params);
    }

    /**
     * Constucts a wrapper for the given EdgeListGraph.
     */
    public CcdRunner(DagWrapper dagWrapper, BasicSearchParams params) {
        super(dagWrapper.getDag(), params);
    }

    public CcdRunner(SemGraphWrapper dagWrapper, BasicSearchParams params) {
        super(dagWrapper.getGraph(), params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CcdRunner serializableInstance() {
        return new CcdRunner(DataWrapper.serializableInstance(),
                BasicSearchParams.serializableInstance());
    }

    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    @Override
	public void execute() {
        Knowledge knowledge = getParams().getKnowledge();
        CcdSearch ccdSearch = new CcdSearch(getIndependenceTest(), knowledge);
        ccdSearch.setDepth(getParams().getIndTestParams().getDepth());
        Graph searchGraph = ccdSearch.search();

        if (knowledge.isDefaultToKnowledgeLayout()) {
            SearchGraphUtils.arrangeByKnowledgeTiers(searchGraph, knowledge);
        }

        setResultGraph(searchGraph);
        GraphUtils.arrangeBySourceGraph(getResultGraph(), getSourceGraph());
    }

    @Override
	public IndependenceTest getIndependenceTest() {
        Object dataModel = getDataSet();

        if (dataModel == null) {
            dataModel = getSourceGraph();
        }

        BasicSearchParams params = (BasicSearchParams) getParams();
        IndTestType testType = params.getIndTestType();
        return new IndTestFactory().getTest(dataModel, params, testType);
    }

    @Override
	public Graph getGraph() {
        return getResultGraph();
    }
}


