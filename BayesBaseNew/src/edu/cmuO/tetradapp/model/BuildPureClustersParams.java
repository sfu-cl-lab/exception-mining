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
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.BuildPureClusters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Stores the parameters needed for the BuildPureClusters search and wizard.
 *
 * @author Ricardo Silva rbas@cs.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class BuildPureClustersParams implements MimParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Cannot be null.
     */
    private Clusters clusters = new Clusters();

    /**
     * @serial Cannot be null.
     */
    private BuildPureClustersIndTestParams pureClustersIndTestParams;

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    //=================================CONSTRUCTORS========================//

    /**
     * Constructs a new parameter object. Must have a blank constructor.
     */
    public BuildPureClustersParams() {
        pureClustersIndTestParams = new BuildPureClustersIndTestParams(0.05,
                BuildPureClusters.TEST_TETRAD_WISHART,
                BuildPureClusters.PURIFY_TEST_GAUSSIAN_SCORE);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BuildPureClustersParams serializableInstance() {
        return new BuildPureClustersParams();
    }

    //=================================PUBLIC METHODS======================//

    /**
     * pureClustersIndTestParams is not in use yet
     */
    @Override
	public MimIndTestParams getMimIndTestParams() {
        return pureClustersIndTestParams;
    }

    @Override
	public List getVarNames() {
        return this.varNames;
    }

    @Override
	public void setVarNames(List varNames) {
        this.varNames = varNames;
    }

    @Override
	public Graph getSourceGraph() {
        return this.sourceGraph;
    }

    @Override
	public void setSourceGraph(Graph graph) {
        this.sourceGraph = graph;
    }

    /**
     * Sets a new knowledge for the algorithm. Not in use yet.
     */
    @Override
	public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
    }

    @Override
	public Knowledge getKnowledge() {
        return knowledge;
    }

    @Override
	public Clusters getClusters() {
        return this.clusters;
    }

    @Override
	public void setClusters(Clusters clusters) {
        if (clusters == null) {
            throw new NullPointerException();
        }
        this.clusters = clusters;
    }

    /**
     * Gets the significance level for the search.
     */
    @Override
	public double getAlpha() {
        return pureClustersIndTestParams.getAlpha();
    }

    /**
     * Sets the significance level for the search.
     */
    @Override
	public void setAlpha(double alpha) {
        pureClustersIndTestParams.setAlpha(alpha);
    }

    /**
     * Gets the type of significance test.
     */
    @Override
	public int getTetradTestType() {
        return pureClustersIndTestParams.getTetradTestType();
    }

    /**
     * Sets the type of significance test.
     */
    @Override
	public void setTetradTestType(int tetradTestType) {
        pureClustersIndTestParams.setTetradTestType(tetradTestType);
    }

    /**
     * Gets the type of significance test.
     */
    @Override
	public int getPurifyTestType() {
        return pureClustersIndTestParams.getPurifyTestType();
    }

    /**
     * Sets the type of significance test.
     */
    @Override
	public void setPurifyTestType(int purifyTestType) {
        pureClustersIndTestParams.setPurifyTestType(purifyTestType);
    }

    @Override
	public int getAlgorithmType() {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setAlgorithmType(int tt) {
        throw new UnsupportedOperationException();
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

        if (knowledge == null) {
            throw new NullPointerException();
        }

        if (clusters == null) {
            throw new NullPointerException();
        }

        if (pureClustersIndTestParams == null) {
            throw new NullPointerException();
        }
    }
}


