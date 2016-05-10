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

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Silly shell for GaRunner to keep the serialization test happy without having
 * to have all the GA code in the repository.
 *
 * @author Shane Harwood
 * @version $Revision: 4524 $ $Date: 2006-01-06 23:02:37 -0500 (Fri, 06 Jan
 *          2006) $
 */
public class GaRunner implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     * @deprecated
     */
    @Deprecated
	private int[] standardIndex;

    /**
     * @serial Can be null.
     * @deprecated
     */
    @Deprecated
	private int currentIndex;

    /**
     * @serial Can be null.
     * @deprecated
     */
    @Deprecated
	private GaParams[] searchGraphs;

    /**
     * @serial Can be null.
     * @deprecated
     */
    @Deprecated
	private GaParams gaParams;

    //=========================CONSTRUCTORS==============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public GaRunner(DataWrapper dataWrapper, GaParams params) {
    }

    public static GaRunner serializableInstance() {
        return new GaRunner(DataWrapper.serializableInstance(),
                GaParams.serializableInstance());
    }
}


