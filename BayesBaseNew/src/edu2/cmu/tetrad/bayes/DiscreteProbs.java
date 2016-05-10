///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Interface containing methods to calculate probabilities for systems of
 * discrete variables. See instantiations for more details.
 *
 * @author Joseph Ramsey
 */
public interface DiscreteProbs {

    /**
     * Returns the variables over which probabilities and conditional
     * probabilities will be calculated.
     */
    public List<Node> getVariables();

    /**
     * Returns the probability in a given cell.
     */
    public double getCellProb(int[] cell);

    /**
     * Calculates the probability P(a), where a is a two dimensional boolean
     * array, with a[i] representing particular combinations of values for
     * bayesIm.getNode(i) as boolean arrays of length bayesim.getNumSplits(i),
     * with a[i][j] = true iff the condition includes the j'th value of
     * bayesIm.getNode(i).
     */
    double getProb(Proposition assertion);

    /**
     * <p>Calculates the conditional probability P(a|b), where a and b are two
     * dimensional boolean arrays, with a[i] and b[i] representing particular
     * combinations of values for bayesIm.getNode(i) as boolean arrays of length
     * bayesim.getNumSplits(i), with a[i][j] = true iff the condition
     * includes the j'th value of bayesIm.getNode(i), and similarly for
     * b[i][j].</p> </p> <p>This does not allow all possible conditional
     * probabilities to be calculated, since for instance the condition X1 = 1
     * or X2 = 2 cannot be represented this way directly, but it does capture
     * all conditions for which this type of method would generally be of
     * interest.</p>
     */
    double getConditionalProb(Proposition assertion, Proposition condition);

    /**
     * Returns true if a missing values case was encountered during the last
     * method call.
     */
    boolean isMissingValueCaseFound();
}



