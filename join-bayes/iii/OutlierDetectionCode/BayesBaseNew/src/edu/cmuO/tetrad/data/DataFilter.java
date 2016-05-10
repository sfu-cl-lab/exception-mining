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


/**
 * Interface implemented by classes whose purpose is to generate modifications
 * of data sets. See implementations for details.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-07-04 19:58:14 -0400 (Mon, 04 Jul
 *          2005) $
 */
public interface DataFilter {

    /**
     * Interpolates the given data set, producing a data set with no missing
     * values.
     */
    RectangularDataSet filter(RectangularDataSet dataSet);
}


