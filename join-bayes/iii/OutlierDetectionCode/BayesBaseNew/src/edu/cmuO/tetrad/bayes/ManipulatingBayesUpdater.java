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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.graph.Graph;

/**
 * Interface for a Bayes updating algorithm that's capable of doing
 * manipulation. In general, manipulating a variable X will eliminate edges into
 * X, so the updating operation on the manipulated model will produce different
 * results.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5100 $ $Date: 2005-07-06 13:39:56 -0400 (Wed, 06 Jul
 *          2005) $
 */
public interface ManipulatingBayesUpdater extends BayesUpdater {
    static final long serialVersionUID = 23L;

    /**
     * Returns the Bayes instantiated model after manipulations have been
     * applied.
     */
    BayesIm getManipulatedBayesIm();

    /**
     * Returns the graph for the manipulated BayesIm.
     */
    Graph getManipulatedGraph();

    /**
     * Returns a defensive copy of the evidence.
     */
    Evidence getEvidence();

    /**
     * Sets new evidence for the updater. Once this is called, old updating
     * results should not longer be available.
     */
    @Override
	void setEvidence(Evidence evidence);

    /**
     * Returns the updated Bayes IM--that is, the Bayes IM in which all
     * probabilities of variables conditional on their parents have been
     * updated.
     */
    BayesIm getUpdatedBayesIm();

    /**
     * Returns P(variable==category | evidence) where evience is getEvidence().
     */
    @Override
	double getMarginal(int variable, int category);

}


