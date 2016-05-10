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

package edu.cmu.shuttle;

import edu.cmu.tetrad.util.TetradSerializable;

import java.io.ObjectStreamException;

/**
 * Types of data in Choh Man's reformatting of the shuttle data.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-09 00:12:03 -0500 (Mon, 09 Jan
 *          2006) $
 */
public final class DataType implements TetradSerializable {
    static final long serialVersionUID = 23L;

    public static final DataType CONTINUOUS = new DataType("Continuous");
    public static final DataType DISCRETE = new DataType("Discrete");
    public static final DataType LAUNCH_TIME = new DataType("LaunchTime");

    /**
     * The name of this type.
     */
    private transient final String name;

    /**
     * Protected constructor for the types; this allows for extension in case
     * anyone wants to add formula types.
     */
    private DataType(String name) {
        this.name = name;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataType serializableInstance() {
        return DataType.DISCRETE;
    }

    /**
     * Prints out the name of the type.
     */
    @Override
	public final String toString() {
        return name;
    }

    // Declarations required for serialization.
    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;
    private static final DataType[] TYPES = {CONTINUOUS, DISCRETE, LAUNCH_TIME};

    final Object readResolve() throws ObjectStreamException {
        return TYPES[ordinal]; // Canonicalize.
    }
}


