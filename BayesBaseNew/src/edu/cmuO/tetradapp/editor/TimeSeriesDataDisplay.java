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

package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.TimeSeriesData;

import javax.swing.*;
import java.awt.*;

/**
 * Displays a DataSet object as a JTable.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-07-07 17:17:28 -0400 (Thu, 07 Jul
 *          2005) $
 */
public class TimeSeriesDataDisplay extends JPanel
        implements DataModelContainer {
    private TimeSeriesDataDisplayJTable timeSerieaDataDisplayJTable;

    /**
     * Constructor. Takes a DataSet as a model.
     */
    public TimeSeriesDataDisplay(TimeSeriesData model) {
        timeSerieaDataDisplayJTable = new TimeSeriesDataDisplayJTable(model);
        setLayout(new BorderLayout());
        add(new JScrollPane(timeSerieaDataDisplayJTable), BorderLayout.CENTER);
    }

    @Override
	public DataModel getDataModel() {
        return timeSerieaDataDisplayJTable.getDataModel();
    }
}

