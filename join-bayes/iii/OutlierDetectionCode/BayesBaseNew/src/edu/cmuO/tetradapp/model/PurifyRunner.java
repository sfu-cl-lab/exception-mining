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

import edu.cmu.tetrad.data.Clusters;
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.MimUtils;
import edu.cmu.tetrad.search.Purify;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the Purify
 * algorithm.
 *
 * @author Ricardo Silva
 * @version $Revision: 4524 $ $Date: 2006-01-06 23:02:37 -0500 (Fri, 06 Jan
 *          2006) $
 */
public class PurifyRunner extends AbstractMimRunner implements GraphSource {
    static final long serialVersionUID = 23L;

    //============================CONSTRUCTORS============================//

    public PurifyRunner(DataWrapper dataWrapper, PurifyParams params) {
        super(dataWrapper, params);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static PurifyRunner serializableInstance() {
        return new PurifyRunner(DataWrapper.serializableInstance(),
                PurifyParams.serializableInstance());
    }

    //===================PUBLIC METHODS OVERRIDING ABSTRACT================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    @Override
	public void execute() {
        Object source = getData();
        Purify purify;

        if (source instanceof CovarianceMatrix) {
            CovarianceMatrix covMatrix = (CovarianceMatrix) source;
            CorrelationMatrix corrMatrix = new CorrelationMatrix(covMatrix);
            purify = new Purify(corrMatrix, getParams().getAlpha(),
                    getParams().getTetradTestType(), getParams().getClusters());
        }
        else if (source instanceof RectangularDataSet) {
            purify = new Purify((RectangularDataSet) source,
                    getParams().getAlpha(), getParams().getTetradTestType(),
                    getParams().getClusters());
        }
        else {
            throw new RuntimeException(
                    "Data source for Purify of invalid type!");
        }
        Graph searchGraph = purify.search();
        setResultGraph(searchGraph);
        GraphUtils.arrangeClustersInLine(getResultGraph(), true);

        setClusters(MimUtils.convertToClusters(searchGraph));
    }

    @Override
	public Clusters getClusters() {
        return super.getClusters();
    }

    @Override
	public void setClusters(Clusters clusters) {
        super.setClusters(clusters);
    }

    @Override
	public Graph getGraph() {
        return getResultGraph();
    }
}

