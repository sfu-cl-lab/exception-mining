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

import edu.cmu.tetrad.data.CaseExpander;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.session.SessionModel;

/**
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-07-04 16:52:58 -0400 (Mon, 04 Jul
 *          2005) $
 */
public class CaseExpanderWrapper extends DataWrapper implements SessionModel {
    static final long serialVersionUID = 23L;

    public CaseExpanderWrapper(DataWrapper wrapper) {
        DataFilter filter = new CaseExpander();
        RectangularDataSet columnDataModel =
                (RectangularDataSet) wrapper.getSelectedDataModel();
        setDataModel(filter.filter(columnDataModel));
        setSourceGraph(wrapper.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new CaseExpanderWrapper(DataWrapper.serializableInstance());
    }
}


