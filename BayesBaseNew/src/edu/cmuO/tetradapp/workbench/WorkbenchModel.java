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

package edu.cmu.tetradapp.workbench;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;

import java.awt.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jun 7, 2004 Time: 5:09:49 PM
 * To change this template use File | Settings | File Templates.
 */
public interface WorkbenchModel {
    /**
     * Returns a new model node of type GraphNode.
     */
    Node getNewModelNode();

    /**
     * Returns a new display node of type AbstractGraphNode given a model node
     * of type modelNode.
     */
    DisplayNode getNewDisplayNode(Node modelNode);

    /**
     * Returns a new display edge for the given model edge.
     */
    IDisplayEdge getNewDisplayEdge(Edge modelEdge);

    /**
     * Returns a new model edge connecting the given nodes.
     */
    Edge getNewModelEdge(Node node1, Node node2);

    /**
     * Returns a new tracking edge for the given display node at the given
     * location.
     */
    IDisplayEdge getNewTrackingEdge(DisplayNode displayNode, Point mouseLoc);
}


