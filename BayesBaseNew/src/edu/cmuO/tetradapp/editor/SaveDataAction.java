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

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSavers;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.EditorUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;

/**
 * Saves data to disk.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6337 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public final class SaveDataAction extends AbstractAction {

    /**
     * The dataEditor.                          -
     */
    private DataEditor dataEditor;


    /***
     * Search editor to get data from.
     */
    private MarkovBlanketSearchEditor searchEditor;


    /**
     * Formats all numbers.
     */
    private final NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * Creates a new action to save data.
     */
    public SaveDataAction(DataEditor editor) {
        super("Save Data...");

        if (editor == null) {
            throw new NullPointerException("Data Editor must not be null.");
        }

        this.setDataEditor(editor);
    }


    public SaveDataAction(MarkovBlanketSearchEditor editor){
        super("Save Data...");
        if(editor == null){
            throw new NullPointerException("Editor must not be null");
        }
        this.searchEditor = editor;
    }





    /**
     * Performs the action of loading a session from a file.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        try {
            saveData();
        }
        catch (IOException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Error in saving: " + e1.getMessage());
        }
    }

    /**
     * Saves data in the selected data set to a file.
     */
    private void saveData() throws IOException {
        File file =
                EditorUtils.getSaveFile("data", "dat", getDataEditor(), false);

        if (file == null) {
            return;
        }

        char delimiter = '\t';

        if (file.getName().endsWith(".csv")) {
            delimiter = ',';
        }

        PrintWriter out;

        try {
            out = new PrintWriter(new FileOutputStream(file));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(
                    "Output file could not be opened: " + file);
        }

        DataModel dataModel;
        if(this.dataEditor != null){
            dataModel = getDataEditor().getSelectedDataModel();
        } else {
            dataModel = searchEditor.getDataModel();
        }

        if (dataModel == null) {
            return;
        }

        if (dataModel instanceof RectangularDataSet) {
            RectangularDataSet dataSet = (RectangularDataSet) dataModel;

            if (dataSet.isContinuous()) {
                DataSavers.saveRectangularData(dataSet, out, delimiter);
            }
            else if (dataSet.isDiscrete()) {
                DataSavers.saveRectangularData(dataSet, out, delimiter);
            }
            else {
                DataSavers.saveRectangularData(dataSet, out, delimiter);
            }
        }
        else if (dataModel instanceof CovarianceMatrix) {
            DataSavers.saveCovMatrix((CovarianceMatrix) dataModel, out, nf);
        }
        else {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Sorry, don't know how to save that.");
        }

        out.close();
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }

    private void setDataEditor(DataEditor dataEditor) {
        this.dataEditor = dataEditor;
    }
}


