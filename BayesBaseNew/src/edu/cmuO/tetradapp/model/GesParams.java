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
 * Stores the parameters needed for the GES search and wizard.
 *
 * @author Ricardo Silva
 * @version $Revision: 5621 $ $Date: 2006-01-06 23:02:37 -0500 (Fri, 06 Jan
 *          2006) $
 */
public final class GesParams implements MeekSearchParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can't be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Can't be null.
     */
    private GesIndTestParams indTestParams;

    /**
     * @serial Can be null.
     */
    private List<String> varNames;

    /**
     * @serial Can be null.
     */
    private IndTestType testType;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    /**
     * True if cycles are to be aggressively prevented. May be expensive
     * for large graphs (but also useful for large graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a new parameter object. Must have a blank constructor.
     */
    public GesParams() {
        this.indTestParams = new GesIndTestParams(10., 0.001, this);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GesParams serializableInstance() {
        return new GesParams();
    }

    //============================PUBLIC METHODS==========================//

    @Override
	public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    @Override
	public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    @Override
	public IndTestParams getIndTestParams() {
        return this.indTestParams;
    }

    @Override
	public void setIndTestParams2(IndTestParams indTestParams) {
        if (!(indTestParams instanceof GesIndTestParams)) {
            throw new IllegalArgumentException(
                    "Illegal IndTestParams " + "in GesIndTestParams");
        }
        this.indTestParams = (GesIndTestParams) indTestParams;
    }

    @Override
	public List<String> getVarNames() {
        return this.varNames;
    }

    @Override
	public void setVarNames(List<String> varNames) {
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

    //  Wrapping GesIndTestParams...

    public double getCellPrior() {
        return indTestParams.getCellPrior();
    }

    public void setCellPrior(double cellPrior) {
        indTestParams.setCellPrior(cellPrior);
    }

    public double getStructurePrior() {
        return indTestParams.getStructurePrior();
    }

    public void setStructurePrior(double structurePrior) {
        indTestParams.setStructurePrior(structurePrior);
    }

    @Override
	public Knowledge getKnowledge() {
        return new Knowledge(knowledge);
    }

    @Override
	public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
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

        if (indTestParams == null) {
            throw new NullPointerException();
        }
    }
}


