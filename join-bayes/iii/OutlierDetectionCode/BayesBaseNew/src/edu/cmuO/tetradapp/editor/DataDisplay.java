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
import edu.cmu.tetrad.data.RectangularDataSet;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Displays a DataSet object as a JTable.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2005-07-06 13:24:32 -0400 (Wed, 06 Jul
 *          2005) $
 */
public class DataDisplay extends JPanel implements DataModelContainer,
        PropertyChangeListener {
    private TabularDataJTable tabularDataJTable;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Constructor. Takes a DataSet as a model.
     */
    public DataDisplay(RectangularDataSet model) {
        tabularDataJTable = new TabularDataJTable(model);
        tabularDataJTable.addPropertyChangeListener(this);
        setLayout(new BorderLayout());
        add(new JScrollPane(getDataDisplayJTable()), BorderLayout.CENTER);
    }

    @Override
	public DataModel getDataModel() {
        return getDataDisplayJTable().getDataModel();
    }

    public TabularDataJTable getDataDisplayJTable() {
        return tabularDataJTable;
    }

    @Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
	public void propertyChange(PropertyChangeEvent evt) {
//        System.out.println("DataDisplay: " + evt.getPropertyName());
        pcs.firePropertyChange(evt);
    }
}