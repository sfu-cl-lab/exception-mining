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
import edu.cmu.tetrad.graph.Pag;

import java.util.List;

/**
 * Specifies what counts as a legal pair of edges X---Y---Z for purposes of
 * calculating possible d-separation sets for the FCI algorithm. In this case,
 * legal initial edges are those adjacent to initial nodes, and legal pairs of
 * edges are those for which either X-->Y<--Z or X is adjacent to Z--i.e. X, Y,
 * and Z form a triangle. (It is assumed (and checked) that is adjacent to Y and
 * Y is adjacent to Z.)
 *
 * @author Tyler Gibson
 * @version $Revision: 6549 $; Mar 20, 2002
 */
class IonLegalPairs implements LegalPairs {

    /**
     * Graph with respect to which graph properties are tested.
     */
    private Pag graph;

    /**
     * Constructs a new legal pairs object. See class level doc.
     *
     * @param graph The graph with respect to which legal pairs will be tested.
     */
    public IonLegalPairs(Pag graph) {
        if (graph == null) {
            throw new NullPointerException();
        }

        this.graph = graph;
    }

    /**
     * Returns true iff x is adjacent to y.
     */
    @Override
	public boolean isLegalFirstEdge(Node x, Node y) {
        return this.graph.isAdjacentTo(x, y);
    }

    /**
     *
     *
     * @throws IllegalArgumentException if x is not adjacent to y or y is not
     *                                  adjacent to z.
     */
    @Override
	public boolean isLegalPair(Node x, Node y, Node z, List<Node> c, List<Node> d) {
        if (!(graph.isAdjacentTo(x, y)) || !(graph.isAdjacentTo(y, z))) {
            throw new IllegalArgumentException();
        }
        //noinspection SimplifiableIfStatement                
        if(graph.isDefiniteCollider(x,y,x) || graph.isAdjacentTo(x,z)){
            return true;
        }

        return this.graph.isUnderlineTriple(x,y,z);
    }
}


