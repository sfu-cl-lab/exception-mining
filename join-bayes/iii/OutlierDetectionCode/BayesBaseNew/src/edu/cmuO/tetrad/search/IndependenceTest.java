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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.data.RectangularDataSet;

import java.util.List;

/**
 * Interface implemented by classes that do conditional independence testing.
 * These classes are capable of serving as conditional independence "oracles"
 * for constraint-based searches.
 *
 * @author Don Crimbchin (djc2@andrew.cmu.edu)
 * @author Joseph Ramsey
 * @version $Revision: 6413 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public interface IndependenceTest {

    /**
     * Returns an Independence test for a subset of the variables.
     */
    IndependenceTest indTestSubset(List<Node> vars);

    /**
     * Returns true if the given independence question is judged true, false if
     * not. The independence question is of the form x _||_ y | z, z =
     * <z1,...,zn>, where x, y, z1,...,zn are variables in the list returned by
     * getVariableNames().
     */
    boolean isIndependent(Node x, Node y, List<Node> z);

    /**
     * Returns true if the given independence question is judged false, true if
     * not. The independence question is of the form x _||_ y | z, z =
     * <z1,...,zn>, where x, y, z1,...,zn are variables in the list returned by
     * getVariableNames().
     */
    boolean isDependent(Node x, Node y, List<Node> z);

    /**
     * Returns the probability associated with the most recently executed
     * independence test, of Double.NaN if p value is not meaningful for tis
     * test.
     */
    double getPValue();

    /**
     * Returns the list of variables over which this independence checker is
     * capable of determinining independence relations.
     */
    List<Node> getVariables();

    /**
     * Returns the variable by the given name.
     */
    Node getVariable(String name);

    /**
     * Returns the list of names for the variables in getNodesInEvidence.
     */
    List<String> getVariableNames();

    /**
     * Returns true if x or y is determined by
     */
    boolean determines(List<Node> z, Node x1);

    /**
     * Returns true if x or y is determined by
     */
    boolean splitDetermines(List<Node> z, Node x, Node y);

    /**
     * Returns the significance level of the independence test.
     * @throws UnsupportedOperationException if there is no significance level.
     */
    double getAlpha();

    /**
     * Sets the significance level.
     */
    void setAlpha(double alpha);

    RectangularDataSet getData();
}


