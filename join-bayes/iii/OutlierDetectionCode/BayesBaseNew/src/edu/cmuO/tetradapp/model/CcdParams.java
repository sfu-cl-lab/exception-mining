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
import edu.cmu.tetrad.search.IndTestType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Stores the parameters needed for the Ccd search and wizard.
 *
 * @author Raul Salinas (modified for Ccd by Frank Wimberly)
 * @version $Revision: 4524 $ $Date: 2005-11-14 18:32:18 -0500 (Mon, 14 Nov
 *          2005) $
 */
public final class CcdParams implements SearchParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Range >= -1.
     */
    private int depth;

    /**
     * @serial Can be null.
     */
    private IndTestParams indTestParams;

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private IndTestType testType;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    //================================CONSTRUCTORS========================//

    /**
     * Constructs a new parameter object. Must have a blank constructor.
     */
    public CcdParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CcdParams serializableInstance() {
        return new CcdParams();
    }

    //===============================PUBLIC METHODS=======================//

    @Override
	public IndTestParams getIndTestParams() {
        return indTestParams;
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

    @Override
	public void setIndTestType(IndTestType testType) {
        this.testType = testType;
    }

    @Override
	public IndTestType getIndTestType() {
        return this.testType;
    }

    @Override
	public void setIndTestParams2(IndTestParams indTestParams) {
        this.indTestParams = indTestParams;
    }

    @Override
	public Knowledge getKnowledge() {
        if (knowledge == null) {
            throw new NullPointerException();
        }
        return new Knowledge(knowledge);
    }

    @Override
	public void setKnowledge(Knowledge k) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
    }

    /**
     * Sets the depth of the associated PC search.
     *
     * @param depth
     */
    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException("Depth < -1: " + depth);
        }
        this.depth = depth;
    }

    /**
     * @return the int containing the depth of the associated PC search.
     */
    public int getDepth() {
        return depth;
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

        if (depth < -1) {
            throw new IllegalStateException("Depth < -1: " + depth);
        }
    }
}


