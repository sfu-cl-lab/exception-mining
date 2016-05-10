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


/**
 * Stores a (x, y) point without having to use awt classes. Immutable.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-08 12:28:12 -0500 (Tue, 08 Mar
 *          2005) $
 */
public class Point implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The x coordinate.
     *
     * @serial
     */
    private int x;

    /**
     * The y coordinate.
     *
     * @serial
     */
    private int y;

    //=============================CONSTRUCTORS=======================//

    /**
     * Constructs a new point with the given coordinates.
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor.
     */
    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Point serializableInstance() {
        return new Point(1, 2);
    }

    //=============================PUBLIC METHODS====================//

    /**
     * Returns the x coordinate of the point.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate of the point.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns true just in case o is a Point with the same x and y
     * coordinates.
     */
    @Override
	public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        Point c = (Point) o;
        return c.x == this.x && c.y == this.y;
    }

    /**
     * Returns a string representation of a point.
     */
    @Override
	public String toString() {
        return "Point<" + x + "," + y + ">";
    }
}


