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

package edu.cmu.tetrad.data;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.List;

/**
 * Interface implemented by classes, instantiations of which serve as columns in
 * a DataSet. This contains raw data of some type (int, double, String, for
 * example; this can change from column to column) and a variable specifying how
 * that data is to be interpreted. The column extends List, allowing all List
 * operations to be used on the column, albeit with objectified data values
 * (Integers for ints, for example). The underlying column of raw data may be
 * longer than the size() methods reports; in this case, only the first size()
 * elements of the data are used. The raw data is returned as an array of some
 * type and must be cast to that type of array. See implementation for details.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 * @see java.util.List
 */
public interface Column extends List, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the raw data array for this columns.  Must be cast to the type of
     * array which supports the particular column implementation being used. The
     * array will typically contain more elements than are actually being used;
     * to obtain the true number of data points, use the size() method. This
     * method should be used by algorithms to retrieve data in columns without
     * incurring the cost of unnecessarruy object creations (e.g. array
     * allocations and generation of primitive data wrappers). </p> Example of
     * proper casting for ContinuousColumn:
     * <pre>    double[] data = (double[])column.getRawData(); </pre>
     *
     * @return the raw data array.
     */
    Object getRawData();

    /**
     * Returns the variable which governs the type of data stored in this
     * column.
     *
     * @return the variable specification.
     * @see edu.cmu.tetrad.data.Variable
     */
    Node getVariable();
}


