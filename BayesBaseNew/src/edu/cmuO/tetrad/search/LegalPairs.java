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

import java.util.List;

/**
 * Determines whether nodes indexed as (n1, center, n2) form a legal pair of
 * edges in a graph for purposes of some algorithm that uses this information.
 * The pair would be n1---center---n2.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-10 16:46:53 -0500 (Tue, 10 Jan
 *          2006) $
 */
public interface LegalPairs {

    /**
     * Returns true iff x*-*y is a legal first edge for the base case.
     */
    boolean isLegalFirstEdge(Node x, Node y);

    /**
     * Returns true iff n1---center---n2 is a legal pair.
     */
    boolean isLegalPair(Node x, Node y, Node z, List<Node> c, List<Node> d);
}

