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
 * A graph constraint prohibitting edges being added to a graph which connects
 * any given node to itself.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class NoEdgesToSelf implements GraphConstraint {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS===========================//

    public NoEdgesToSelf() {

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static NoEdgesToSelf serializableInstance() {
        return new NoEdgesToSelf();
    }

    //=============================PUBLIC METHODS=========================//


    /**
     * Checks to make sure that a proposed edge does not connect a node to
     * itself.
     *
     * @param edge  the edge to check.
     * @param graph the graph to check.
     * @return true if the edge does not connect a node to itself, false
     *         otherwise.
     */
    @Override
	public boolean isEdgeAddable(Edge edge, Graph graph) {
        return !edge.getNode1().equals(edge.getNode2());
    }

    /**
     * @param node  the node to check.
     * @param graph the graph to check.
     * @return true.
     */
    @Override
	public boolean isNodeAddable(Node node, Graph graph) {
        return true;
    }

    /**
     * @param edge  the edge to check.
     * @param graph the graph to check.
     * @return true.
     */
    @Override
	public boolean isEdgeRemovable(Edge edge, Graph graph) {
        return true;
    }

    /**
     * @param node
     * @param graph the graph to check.
     * @return true.
     */
    @Override
	public boolean isNodeRemovable(Node node, Graph graph) {
        return true;
    }

    /**
     * Returns a string representation of this constraint.
     *
     * @return this representation.
     */
    @Override
	public String toString() {
        return "<No edge connecting a node to itself.>";
    }
}


