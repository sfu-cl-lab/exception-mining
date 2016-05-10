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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Ricardo Silva
 * @version $Revision: 5303 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class RemoveMissingValueCasesAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new action to split by collinear columns.
     */
    public RemoveMissingValueCasesAction(DataEditor editor) {
        super("Remove Cases with Missing Values");

        if (editor == null) {
            throw new NullPointerException();
        }

        this.dataEditor = editor;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    @Override
	public void actionPerformed(ActionEvent e) {

        // TODO Rewrite this to to a data selection.
        DataModel dataModel = getDataEditor().getSelectedDataModel();
        RectangularDataSet dataSet = (RectangularDataSet) dataModel;
        List<Node> variables = new LinkedList<Node>();

        for (int j = 0; j < dataSet.getNumColumns(); j++) {
            variables.add(dataSet.getVariable(j));
        }

        RectangularDataSet newDataSet = new ColtDataSet(0, variables);

        int newRow = -1;

        ROWS:
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                Node variable = dataSet.getVariable(j);

                if (((Variable) variable).isMissingValue(
                        dataSet.getObject(i, j))) {
                    continue ROWS;
                }
            }

            newRow++;

            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                newDataSet.setObject(newRow, j, dataSet.getObject(i, j));
            }
        }

        DataModelList list = new DataModelList();
        list.add(newDataSet);
        getDataEditor().reset(list);
        getDataEditor().selectLastTab();
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }
}


