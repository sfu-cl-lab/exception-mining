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

package edu.cmu.tetrad.gene.history;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Instantiations of this interface know how to randomize update graphs in
 * particular ways.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public interface GraphRandomizer extends TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Randomizes the given lag graph--in other words, chooses random edges for
     * the graph according to a particlar scheme (see instantiations). May or
     * may not modify the factors in the graph, depending on the particular
     * scheme. (Whether the factors are modified should be clearly documented.)
     *
     * @param graph an lag graph.
     */
    void randomizeGraph(LagGraph graph);
}


