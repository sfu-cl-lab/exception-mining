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
 * Stores the parameters needed to generate a new lag workbench, whether
 * randomized or manually constructed.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4804 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public class LagGraphParams implements Params {
    static final long serialVersionUID = 23L;

    /**
     * Indicates a constant indegree. Every node will be initialized with the
     * same number of parents.
     */
    public final static int CONSTANT = 0;

    /**
     * Indicates a maximum indegree. When nodes are initialized, their maximum
     * indegree will be this.
     */
    public final static int MAX = 1;

    /**
     * Indicates the mean indegree. When nodes are initialized, their mean
     * indegree will be this.
     */
    public final static int MEAN = 2;

    /**
     * The stored indegree type.
     *
     * @serial Of type CONSTANT, MAX or MEAN.
     */
    private int indegreeType = CONSTANT;

    /**
     * The number of variables per individual.
     *
     * @serial Range >= 1.
     */
    private int varsPerInd = 5;

    /**
     * The maximum lag of the lag workbench.
     *
     * @serial Range > 0.
     */
    private int mlag = 1;

    /**
     * The indegree. (This is variously interpreted.)
     *
     * @serial Range > 1.
     */
    private int indegree = 2;

    /**
     * Percent housekeeping genes; these genes have no parents.
     *
     * @serial Range (0, 100).
     */
    private double percentUnregulated = 10.0;

    //=================================CONSTRUCTOR========================//

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public LagGraphParams() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LagGraphParams serializableInstance() {
        return new LagGraphParams();
    }

    //==============================PUBLIC METHODS========================//

    /**
     * Returns the number of variables per individual.
     */
    public int getVarsPerInd() {
        return this.varsPerInd;
    }

    /**
     * Sets the number of variables per individual.
     */
    public void setVarsPerInd(int varsPerInd) {
        // TODO Make this throw an exception! (Need to adjust calling code
        // first so as not to break things.) jdramsey 2/22/2005

        if (varsPerInd > 0) {
            this.varsPerInd = varsPerInd;
        }
    }

    /**
     * Returns the maximum lag.
     */
    public int getMlag() {
        return this.mlag;
    }

    /**
     * Sets the maximum lag.
     */
    public void setMlag(int mlag) {
        // TODO Make this throw an exception! (Need to adjust calling code
        // first so as not to break things.) jdramsey 2/22/2005

        if (mlag > 0) {
            this.mlag = mlag;
        }
    }

    /**
     * Returns the indegree.
     */
    public int getIndegree() {
        return this.indegree;
    }

    /**
     * Sets the indegree. Must be at least 2.
     */
    public void setIndegree(int indegree) {
        // TODO Make this throw an exception! (Need to adjust calling code
        // first so as not to break things.) jdramsey 2/22/2005

        if (indegree > 1) {
            this.indegree = indegree;
        }
    }

    /**
     * Returns the indegree type.
     */
    public int getIndegreeType() {
        return indegreeType;
    }

    /**
     * Sets the indegree type.
     *
     * @param indegreeType one of CONSTANT, MAX, OR MEAN.
     */
    public void setIndegreeType(int indegreeType) {

        switch (indegreeType) {
            case CONSTANT:

                //Falls through!
            case MAX:

                //Falls through!
            case MEAN:
                this.indegreeType = indegreeType;
                break;

            default :
                throw new IllegalArgumentException();
        }
    }

    /**
     * Gets the approximate percent of unregulated genes.
     */
    public double getPercentUnregulated() {
        return percentUnregulated;
    }

    /**
     * Sets the approximate percent of unregulated genes.
     */
    public void setPercentUnregulated(double percentUnregulated) {
        if (percentUnregulated < 0.0 || percentUnregulated > 100.0) {
            throw new IllegalArgumentException();
        }

        this.percentUnregulated = percentUnregulated;
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

        switch (indegreeType) {
            case CONSTANT:
                // Falls through.
            case MAX:
                // Falls through.
            case MEAN:
                break;
            default:
                throw new IllegalStateException(
                        "Illegal indegree type: " + indegreeType);
        }

        if (!(varsPerInd >= 1)) {
            throw new IllegalStateException(
                    "VarsPerInd out of range: " + varsPerInd);
        }

        if (!(mlag > 0)) {
            throw new IllegalStateException("Mlag out of range: " + mlag);
        }

        if (!(varsPerInd > 1)) {
            throw new IllegalStateException(
                    "VarsPerInd out of range: " + varsPerInd);
        }

        if (!(percentUnregulated > 0.0 && percentUnregulated < 100.0)) {
            throw new IllegalStateException(
                    "PercentUnregulated out of range: " + percentUnregulated);
        }
    }
}


