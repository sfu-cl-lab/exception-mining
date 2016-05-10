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

import edu.cmu.tetrad.data.RectangularDataSet;

/**
 * Creates a data set from the set of selected columns in the given data set.
 * (Makes a copy of the selected data.)
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6240 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class VariableSubsetterWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    public VariableSubsetterWrapper(DataWrapper wrapper) {
        RectangularDataSet dataSet =
                (RectangularDataSet) wrapper.getSelectedDataModel();
        RectangularDataSet selection =
                dataSet.subsetColumns(dataSet.getSelectedIndices());

        RectangularDataSet selectionCopy;

        if (selection.isDiscrete()) {
            selectionCopy = selection;
        }
        else if (selection.isContinuous()) {
            selectionCopy = selection;
        }
        else {
            selectionCopy = selection;
        }

        setDataModel(selectionCopy);
        setSourceGraph(wrapper.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new VariableSubsetterWrapper(DataWrapper.serializableInstance());
    }
}


