///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

import java.beans.PropertyChangeListener;

/**
 * Represents an object with a name, node type, and position that can serve as a
 * node in a graph.
 *
 * @author Joseph Ramsey
 * @see NodeType
 */
public interface Node extends TetradSerializable, Comparable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the name of the node.
     */
    String getName();

    /**
     * Sets the name of this node.
     */
    void setName(String name);

    /**
     * Returns the node type for this node.
     */
    NodeType getNodeType();

    /**
     * Sets the node type for this node.
     */
    void setNodeType(NodeType nodeType);

    /**
     * Returns a string representation of the node.
     */
    String toString();

    /**
     * Returns the x coordinate of the center of the node.
     */
    int getCenterX();

    /**
     * Sets the x coordinate of the center of this node.
     */
    void setCenterX(int centerX);

    /**
     * Returns the y coordinate of the center of the node.
     */
    int getCenterY();

    /**
     * Sets the y coordinate of the center of this node.
     */
    void setCenterY(int centerY);

    /**
     * Sets the (x, y) coordinates of the center of this node.
     */
    void setCenter(int centerX, int centerY);

    /**
     * Adds a property change listener.
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Returns a hashcode for this variable.
     */
    int hashCode();

    /**
     * Returns true iff this variable is equal to the given variable.
     */
    boolean equals(Object o);

    /**
     * Creates a new node of the same type as this one with the given name.
     */
    Node like(String name);

    /**
     * Alphabetical order.
     */
    int compareTo(Object o);
}



