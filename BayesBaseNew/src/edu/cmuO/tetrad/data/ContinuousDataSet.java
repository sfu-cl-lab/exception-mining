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
 * This is just a ghost of a previous data container. Don't even think of trying
 * to use it; all of the method implementations have been deleted. It must be
 * kept in this ghost form in order to preserve backwards compatibility for
 * serialization.
 *
 * @deprecated Use ColDataSet instead.
 */
@Deprecated
final class ContinuousDataSet extends DataSet {
    static final long serialVersionUID = 23L;

    /**
     * @serial DO NOT ERASE THIS! IT'S NEEDED SO SERIALIZATION WILL WORK!
     */
    private int fixedRowSize;

    /**
     * @serial DO NOT ERASE THIS! IT'S NEEDED SO SERIALIZATION WILL WORK!
     */
    private boolean checkingRowSize;
}


