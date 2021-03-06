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

package edu.cmu.tetrad.graph;


/**
 * A graph constraint prohibitting more than one edge per node pair from being
 * added to the graph.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class AtMostOneEdgePerPair implements GraphConstraint {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS===========================//

    public AtMostOneEdgePerPair() {

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static AtMostOneEdgePerPair serializableInstance() {
        return new AtMostOneEdgePerPair();
    }

    //=============================PUBLIC METHODS=========================//

    /**
     * Returns true iff the new edge may be added.
     */
    @Override
	public boolean isEdgeAddable(Edge edge, Graph graph) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();

        return graph.getEdges(node1, node2).isEmpty();
    }

    /**
     * Returns true iff the node may be added.
     */
    @Override
	public boolean isNodeAddable(Node node, Graph graph) {
        return true;
    }

    /**
     * Returns true;
     */
    @Override
	public boolean isEdgeRemovable(Edge edge, Graph graph) {
        return true;
    }

    /**
     * Returns true.
     */
    @Override
	public boolean isNodeRemovable(Node node, Graph graph) {
        return true;
    }

    /**
     * Returns a string representation of the constraint.
     *
     * @return this representation.
     */
    @Override
	public String toString() {
        return "<At most one edge per node pair.>";
    }
}


