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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;

import java.util.List;

/**
 * Interface to indicate a class whose knowledge is capable of being edited by
 * the knowledge editor.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-07 00:27:28 -0500 (Sat, 07 Jan
 *          2006) $
 */
public interface KnowledgeEditable {

    /**
     * Returns a copy of the knowledge for this class.
     */
    Knowledge getKnowledge();

    /**
     * Sets knowledge to a copy of the given object.
     */
    void setKnowledge(Knowledge knowledge);

    /**
     * Returns the source graph. This will be used to arrange the graph in the
     * knowledge editor in a recognizable way.
     */
    Graph getSourceGraph();

    /**
     * Returns the variable names that the knowledge editor may use.
     */
    List<String> getVarNames();
}


