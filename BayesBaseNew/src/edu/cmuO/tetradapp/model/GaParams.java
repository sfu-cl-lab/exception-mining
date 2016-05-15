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
 * Stores the parameters needed for the PC search and wizard.
 *
 * @author Shane Harwood
 * @author Raul Salinas
 * @version 1.0
 */
public final class GaParams implements SearchParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @serial Cannot be null.
     */
    private IndTestParams indTestParams = new BasicIndTestParams();

    /**
     * @serial Range [0.0, 1.0].
     */
    private double alpha = 0.05;

    /**
     * @serial Range (0.0, +inf).
     */
    private double bias = 1.0;

    /**
     * @serial Range [0.0, +inf).
     */
    private double timeLimit = 0.0;

    /**
     * @serial Can be null.
     */
    private IndTestType indTestType;

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private Graph sourceGraph;

    //=============================CONSTRUCTORS============================//

    /**
     * Constructs a new parameter object. Must have a blank constructor.
     */
    public GaParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GaParams serializableInstance() {
        return new GaParams();
    }

    //=============================PUBLIC METHODS==========================//

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double newValue) {
        if (newValue < 0.0 || newValue > 1.0) {
            throw new IllegalArgumentException(
                    "Alpha value out of range: " + newValue);
        }

        this.alpha = newValue;
    }

    public void setBias(double newValue) {
        if (newValue <= 0.0) {
            throw new IllegalArgumentException(
                    "bias value out of range: " + newValue);
        }

        this.bias = newValue;
    }

    public void setTimeLimit(double newValue) {
        if (newValue < 0.0) {
            throw new IllegalArgumentException(
                    "bias value out of range: " + newValue);
        }

        System.out.println("Time limit set to " + newValue);
        timeLimit = newValue;
    }

    public double getTimeLimit() {
        return timeLimit;
    }

    public double getBias() {
        return bias;
    }

    @Override
	public IndTestParams getIndTestParams() {
        return indTestParams;
    }

    @Override
	public void setIndTestParams2(IndTestParams indTestParams) {
        if (indTestParams == null) {
            throw new NullPointerException();
        }
        this.indTestParams = indTestParams;
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
	public void setIndTestType(IndTestType indTestType) {
        this.indTestType = indTestType;
    }

    @Override
	public IndTestType getIndTestType() {
        return this.indTestType;
    }

    @Override
	public Knowledge getKnowledge() {
        return new Knowledge(knowledge);
    }

    @Override
	public void setKnowledge(Knowledge k) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set a null knowledge.");
        }

        this.knowledge = new Knowledge(knowledge);
    }

    public void verboseDescription() {
        System.out.println("Verbose GaParam description " + toString());
        System.out.println(knowledge.toString());

        /* Field */
        System.out.println(alpha);
        System.out.println(bias);
        System.out.println(timeLimit);
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

        if (!(alpha >= 0.0 && alpha <= 1.0)) {
            throw new IllegalStateException("Alpha out of range: " + alpha);
        }

        if (!(bias > 0.0)) {
            throw new IllegalStateException("Bias out of range: " + bias);
        }

        if (!(timeLimit >= 0.0)) {
            throw new IllegalStateException("Time limit out of range: " + bias);
        }
    }
}


