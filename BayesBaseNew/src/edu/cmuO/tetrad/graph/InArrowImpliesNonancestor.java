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
 * <p>A graph constraint that checks to make sure that in-arrows imply
 * non-ancestry-- that is, when adding an edge A *--> B, where * is any
 * endpoint, B is not an ancestor of A.</p> <p>This can be used, e.g., to guard
 * against cycles in a DAG.  If we may assume that graph G is already acyclic,
 * then we can use this constraint to check whether adding a new directed edge
 * will create a new cycle.</p>
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class InArrowImpliesNonancestor implements GraphConstraint {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS===========================//

    public InArrowImpliesNonancestor() {

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static InArrowImpliesNonancestor serializableInstance() {
        return new InArrowImpliesNonancestor();
    }

    //=============================PUBLIC METHODS=========================//

    /**
     * Returns true for edge A *-> B iff B is not an ancestor of A.
     */
    @Override
	public boolean isEdgeAddable(Edge edge, Graph graph) {
        if (edge.getEndpoint1() == Endpoint.ARROW) {
            if (graph.isProperAncestorOf(edge.getNode1(), edge.getNode2())) {
                return false;
            }
        }

        if (edge.getEndpoint2() == Endpoint.ARROW) {
            if (graph.isProperAncestorOf(edge.getNode2(), edge.getNode1())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true.
     */
    @Override
	public boolean isNodeAddable(Node node, Graph graph) {
        return true;
    }

    /**
     * Returns true.
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
     */
    @Override
	public String toString() {
        return "<Arrow implies non-ancestor.>";
    }
}


