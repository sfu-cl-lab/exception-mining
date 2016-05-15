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

package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;

import java.awt.*;

/**
 * Interface to indicate a class that has a graph in it that can be laid out.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public interface LayoutEditable {

    /**
     * Returns the current graph. (Not necessarily a copy.)
     */
    Graph getGraph();

    /**
     * Returns the current knowledge.
     */
    Knowledge getKnowledge();

    /**
     * Returns the source graph.
     */
    Graph getSourceGraph();

    /**
     * Sets the graph according to which the given graph should be laid out.
     */
    void layoutByGraph(Graph graph);

    /**
     * Lays out the graph in tiers according to knowledge.
     */
    void layoutByKnowledge();

    /**
     * Returns the preferred size of the layout.
     */
    Rectangle getVisibleRect();
}


