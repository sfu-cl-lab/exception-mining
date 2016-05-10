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

package edu.cmu.tetrad.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores a node arrangment as a map from node names to
 * edu.cmu.tetrad.util.Points.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-05 17:09:26 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class NodeArrangement {

    /**
     * Map from names to point.
     */
    private Map<String, Point> arrangement = new HashMap<String, Point>();

    /**
     * Blank constructor.
     */
    public NodeArrangement() {
    }

    /**
     * Copy constructor.
     */
    public NodeArrangement(NodeArrangement nodeArrangement) {
        this.arrangement =
                new HashMap<String, Point>(nodeArrangement.arrangement);
    }

    /**
     * Returns the point for the given node name, or null if no such point has
     * been recorded.
     */
    public Point get(String nodeName) {
        return arrangement.get(nodeName);
    }

    /**
     * Sets the point for the given node name.
     *
     * @throws NullPointerException if either nodeName or point is null.
     */
    public void put(String nodeName, Point point) {
        if (nodeName == null || point == null) {
            throw new NullPointerException();
        }

        arrangement.put(nodeName, point);
    }

    @Override
	public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        NodeArrangement c = (NodeArrangement) o;
        return c.arrangement.equals(this.arrangement);
    }

    /**
     * Prints out the nodes and their points.
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("\nNode Arrangement:");

        Set<String> keys = arrangement.keySet();

        for (String key : keys) {
            Point value = arrangement.get(key);
            buf.append("\n\t").append(key).append("-->").append(value);
        }

        buf.append("\n");
        return buf.toString();
    }
}


