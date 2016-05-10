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

import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * Stores basic independence test parameters.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class GraphIndTestParams implements IndTestParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Range >= -1.
     */
    private int depth = -1;

    //===============================CONSTRUCTORS=========================//

    public GraphIndTestParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GraphIndTestParams serializableInstance() {
        return new GraphIndTestParams();
    }

    //===============================PUBLIC METHODS=======================//

    @Override
	public double getAlpha() {
        return Double.NaN;
    }

    @Override
	public void setAlpha(double alpha) {
        throw new UnsupportedOperationException("Cannot set alpha when using " +
                "d-separation to calculate conditional independence.");
    }

    /**
     * Sets the depth for search algorithms that require it.
     */
    @Override
	public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0.");
        }

        this.depth = depth;
    }

    /**
     * Returns the depth of the search.
     */
    @Override
	public int getDepth() {
        return this.depth;
    }

    public int getNumLags() {
        throw new UnsupportedOperationException();
    }

    public void setNumLags(int numLags) {
        throw new UnsupportedOperationException();
    }

    public int getNumTimePoints() {
        throw new UnsupportedOperationException();
    }

    public void setNumTimePoints(int numTimePoints) {
        throw new UnsupportedOperationException();
    }

    public boolean isTimeSeries() {
        return false;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (depth < -1) {
            throw new NullPointerException();
        }
    }
}                                         


