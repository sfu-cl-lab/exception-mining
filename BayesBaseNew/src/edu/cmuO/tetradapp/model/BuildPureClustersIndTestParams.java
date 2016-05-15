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
import java.util.List;

/**
 * @author Ricardo Silva
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class BuildPureClustersIndTestParams implements MimIndTestParams {
    static final long serialVersionUID = 23L;

    /**
     * @serial Range [0, 1].
     */
    private double alpha = 0.05;

    /**
     * @serial Can be null.
     */
    private List varNames;

    /**
     * @serial Can be null.
     */
    private int tetradTestType;

    /**
     * These are public static ints in BuildPureClusters (see);
     *
     * @serial Range unchecked.
     */
    private int purifyTestType;

    //=============================CONSTRUCTORS==========================//

    public BuildPureClustersIndTestParams(double alpha, int type,
            int purifyType) {
        setAlpha(alpha);
        setTetradTestType(type);
        setPurifyTestType(purifyType);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BuildPureClustersIndTestParams serializableInstance() {
        return new BuildPureClustersIndTestParams(0.05, 1, 1);
    }

    //============================PUBLIC METHODS=========================//

    public int getTetradTestType() {
        return tetradTestType;
    }

    public void setTetradTestType(int type) {
        tetradTestType = type;
    }

    public int getPurifyTestType() {
        return purifyTestType;
    }

    public void setPurifyTestType(int type) {
        purifyTestType = type;
    }

    @Override
	public double getAlpha() {
        return alpha;
    }

    @Override
	public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha out of range: " + alpha);
        }
        this.alpha = alpha;
    }

    @Override
	public List getVarNames() {
        return this.varNames;
    }

    @Override
	public void setVarNames(List varNames) {
        this.varNames = varNames;
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

        if (alpha < 0.0 && alpha > 1.0) {
            throw new IllegalArgumentException(
                    "Alpha should be in [0, 1]: " + alpha);
        }
    }
}


