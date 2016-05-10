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

import edu.cmu.tetrad.data.ContinuousColumn;
import edu.cmu.tetrad.data.ContinuousVariable;

/**
 * Wraps a column of a simulated data set for presentation. The intention of
 * this is to allow the form of the simulated data set to change. In the current
 * implementation, it just passes an array of data to the superclass. The form
 * of the simulated data may change to have more dimensions, with each current
 * column of data being broken up into multiple columns. In that case, more of
 * the superclass will need to be overridden, and the constructor will need to
 * change of course, though the function should stay exactly the same.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 * @deprecated Unused anywhere in the code. Can't delete because doing so breaks
 *             serialization.
 */
@Deprecated
public class WrapperContinuousColumn extends ContinuousColumn {
    static final long serialVersionUID = 23L;

    public WrapperContinuousColumn(ContinuousVariable var, double[] data) {
        super(var, data, data.length);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ContinuousColumn serializableInstance() {
        return new WrapperContinuousColumn(
                ContinuousVariable.serializableInstance(), new double[0]);
    }
}