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

import java.io.ObjectStreamException;

/**
 * A typesafe enumeration of the types of endpoints that are permitted in
 * Tetrad-style graphs: null (-), arrow (->), and circle (-o).
 *
 * @author Joseph Ramsey
 * @version $Revision: 4975 $ $Date: 2005-10-06 11:05:09 -0400 (Thu, 06 Oct
 *          2005) $
 */
public final class Endpoint implements TetradSerializable {
    static final long serialVersionUID = 23L;

    public static final Endpoint TAIL = new Endpoint("Tail");
    public static final Endpoint ARROW = new Endpoint("Arrow");
    public static final Endpoint CIRCLE = new Endpoint("Circle");

    /**
     * The name of this type.
     */
    private final transient String name;

    /**
     * Protected constructor for the types; this allows for extension in case
     * anyone wants to add formula types.
     */
    private Endpoint(String name) {
        this.name = name;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Endpoint serializableInstance() {
        return Endpoint.TAIL;
    }

    /**
     * Prints out the name of the type.
     */
    @Override
	public String toString() {
        return name;
    }

    // Declarations required for serialization.
    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;
    private static final Endpoint[] TYPES = {TAIL, ARROW, CIRCLE};

    Object readResolve() throws ObjectStreamException {
        return TYPES[ordinal]; // Canonicalize.
    }
}


