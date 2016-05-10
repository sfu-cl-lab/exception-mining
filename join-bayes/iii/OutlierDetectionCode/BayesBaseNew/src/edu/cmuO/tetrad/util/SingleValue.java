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
 * A pretend distribution that always returns the given value when
 * nextRandom() is called.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6018 $ $Date: 2005-07-07 17:17:28 -0400 (Thu, 07 Jul
 *          2005) $
 */
public class SingleValue implements Distribution {
    static final long serialVersionUID = 23L;

    /**
     * @serial
     */
    private double value;

    //===========================CONSTRUCTORS===========================//

    /**
     * Constructs single value "distribution" using the given value.
     */
    public SingleValue(double value) {
        this.value = value;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SingleValue serializableInstance() {
        return new SingleValue(0.5);
    }

    @Override
	public void setParameter(int index, double value) {
        if (index == 0) {
            this.value = value;
        }
        else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    @Override
	public double getParameter(int index) {
        if (index == 0) {
            return this.value;
        }
        else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    @Override
	public String getParameterName(int index) {
        if (index == 0) {
            return "Value";
        }
        else {
            throw new IllegalArgumentException("Not a parameter index: " + index);
        }
    }

    @Override
	public int getNumParameters() {
        return 1;
    }

    //============================PUBLIC METHODS========================//

    /**
     * Returns the value that was set.
     */
    @Override
	public double nextRandom() {
        return getValue();
    }

    @Override
	public String toString() {
        return "[" + getValue() + "]";
    }

    public double getValue() {
        return value;
    }


    @Override
	public String getName() {
        return "Single Value";
    }
}


