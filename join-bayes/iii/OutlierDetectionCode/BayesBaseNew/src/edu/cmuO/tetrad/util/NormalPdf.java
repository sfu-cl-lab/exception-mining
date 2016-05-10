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
 * Density function of a Gaussian distribution
 *
 * @author Frank Wimberly based on a similar class by Joseph Ramsey
 */
public class NormalPdf implements Function, TetradSerializable {
    static final long serialVersionUID = 23L;

    //=========================CONSTRUCTORS============================//

    /**
     * Constructs a new density function of a Gaussian.
     */
    public NormalPdf() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static NormalPdf serializableInstance() {
        return new NormalPdf();
    }

    //========================PUBLIC METHODS===========================//

    /**
     * Calculates the value of the function at the given domain point.
     *
     * @param x the domain point.
     * @return the value of the function at x.
     */
    @Override
	public double valueAt(double x) {
        return ProbUtils.normalPdf(x);
    }

    /**
     * Returns a description of the function.
     */
    @Override
	public String toString() {
        return "Gaussian density function \n\n";
    }
}


