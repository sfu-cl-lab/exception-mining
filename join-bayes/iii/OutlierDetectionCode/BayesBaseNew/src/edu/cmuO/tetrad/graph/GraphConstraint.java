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

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * An interface representing a syntactic constraint that certain graphs need to
 * obey when adding or removing nodes or edges. See instantiations for more
 * details.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public interface GraphConstraint extends TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Checks to make sure that adding the specified edge will comply with the
     * graph constraint.
     *
     * @param edge  the edge to check.
     * @param graph the graph to check.
     * @return true if adding the edge would satisfy the constraint, false if
     *         not.
     */
    boolean isEdgeAddable(Edge edge, Graph graph);

    /**
     * Checks to make sure that adding the specified node will comply with the
     * graph constraint.
     *
     * @param node  the node to check.
     * @param graph the graph to check.
     * @return true if adding the node would satisfy the constraint, false if
     *         not.
     */
    boolean isNodeAddable(Node node, Graph graph);

    /**
     * Checks to make sure that removing the specified edge will comply with the
     * graph constraint.
     *
     * @param edge  the edge to check.
     * @param graph the graph to check.
     * @return true if removing the edge would satisfy the constraint, false if
     *         not.
     */
    boolean isEdgeRemovable(Edge edge, Graph graph);

    /**
     * Checks to make sure that removing the specified node will comply with the
     * graph constraint.
     *
     * @param node  the node to check.
     * @param graph the graph to check.
     * @return true if removing the node would satisfy the constraint, false, if
     *         not.
     */
    boolean isNodeRemovable(Node node, Graph graph);

    /**
     * Returns a string representation of the graph constraint, of the form
     * "<[Some sentence.]>.
     *
     * @return this string.
     */
    @Override
	String toString();
}


