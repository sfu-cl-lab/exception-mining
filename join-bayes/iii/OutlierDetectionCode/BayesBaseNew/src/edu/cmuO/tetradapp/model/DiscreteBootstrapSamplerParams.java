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

import edu.cmu.tetrad.model.Params;

import java.io.IOException;
import java.io.ObjectInputStream;


/** 
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5718 $ $Date: 2006-01-05 17:04:44 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class DiscreteBootstrapSamplerParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     *
     * @serial Range > 0.
     */
    private int sampleSize = 100;

    //==========================CONSTRUCTORS===========================//

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public DiscreteBootstrapSamplerParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DiscreteBootstrapSamplerParams serializableInstance() {
        return new DiscreteBootstrapSamplerParams();
    }

    //==========================PUBLIC METHODS=========================//

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        if (sampleSize < 1) {
            throw new IllegalArgumentException(
                    "Sample size must be > 0: " + sampleSize);
        }

        this.sampleSize = sampleSize;
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

        if (sampleSize <= 0) {
            throw new IllegalArgumentException(
                    "Sample size out of range: " + sampleSize);
        }
    }
}


