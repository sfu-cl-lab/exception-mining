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
 * This graph constraint permitting only measured or latent nodes to be added to
 * the graph.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class MeasuredLatentOnly implements GraphConstraint {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS===========================//

    public MeasuredLatentOnly() {

    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MeasuredLatentOnly serializableInstance() {
        return new MeasuredLatentOnly();
    }

    //=============================PUBLIC METHODS=========================//

    /**
     * Returns true.
     */
    @Override
	public boolean isEdgeAddable(Edge edge, Graph graph) {
        return true;
    }

    /**
     * Returns true iff the given node is either an observed or a latent node.
     */
    @Override
	public boolean isNodeAddable(Node node, Graph graph) {
        NodeType type = node.getNodeType();
        return type == NodeType.MEASURED || type == NodeType.LATENT;
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
     */
    @Override
	public String toString() {
        return "<Measured and latent nodes only.>";
    }
}


