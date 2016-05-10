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
 * Stores the parameters needed to initialize a BayesPm.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @author Frank Wimberly based on DiscreteBootstrapSamplerParams by Ramsey
 */
public class MissingDataInjectorParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     * 0.02 is the default probability that a given variable will be missing for
     * a given case.
     *
     * @serial Range [0, 1].
     */
    private double prob = 0.02;

    //===========================CONSTRUCTORS=============================//

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public MissingDataInjectorParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static MissingDataInjectorParams serializableInstance() {
        return new MissingDataInjectorParams();
    }

    //============================PUBLIC METHODS===========================//

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        if (prob < 0.0 || prob > 1.0) {
            throw new IllegalArgumentException(
                    "Probability must be between 0.0 and 1.0: " + prob);
        }

        this.prob = prob;
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

        if (prob < 0.0 || prob > 1.0) {
            throw new IllegalThreadStateException("Prob out of range: " + prob);
        }
    }
}


