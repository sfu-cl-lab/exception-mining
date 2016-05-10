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
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Ricardo Silva
 * @version $Revision: 5533 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class SplitByCollinearColumnsAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new action to split by collinear columns.
     */
    public SplitByCollinearColumnsAction(DataEditor editor) {
        super("Split Data by Collinear Columns");

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
        DataModel dataModel = getDataEditor().getSelectedDataModel();
        CorrelationMatrix corrMatrix;

        if (dataModel instanceof RectangularDataSet) {
            try {
                RectangularDataSet dataSet = (RectangularDataSet) dataModel;
                CovarianceMatrix covMatrix = new CovarianceMatrix(dataSet);
                corrMatrix = new CorrelationMatrix(covMatrix);
            }
            catch (Exception e1) {
                String message = e1.getMessage();
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        message);
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }
        }
        else if (dataModel instanceof CovarianceMatrix) {
            corrMatrix = new CorrelationMatrix((CovarianceMatrix) dataModel);
        }
        else {
            String message =
                    "Operation not supported for this kind of data set.";
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    message);
            throw new RuntimeException(message);
        }

        CollinearityChooser collinearityChooser =
                new CollinearityChooser(dataEditor);
        int selection = JOptionPane.showOptionDialog(
                JOptionUtils.centeringComp(), collinearityChooser,
                "Degree of collinearity", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Done", "Cancel"}, "Done");

        if (selection == 0 && confirmSplit(corrMatrix, dataModel,
                collinearityChooser.getCorrelationSize() / 100.)) {
            DataModelList splitData = new DataModelList();
            DataModelList splitData1 = getSplitData(corrMatrix, dataModel,
                    collinearityChooser.getCorrelationSize() / 100.,
                    splitData);
            getDataEditor().reset(splitData1);
        }
    }

    private boolean confirmSplit(CorrelationMatrix corrMatrix,
            DataModel dataModel, double correlation) {
        int count = 0;
        for (int i = 0; i < dataModel.getVariableNames().size() - 1; i++) {
            int index1 = getMatrixIndex(corrMatrix,
                    dataModel.getVariableNames().get(i));
            for (int j = i + 1; j < dataModel.getVariableNames().size(); j++) {
                int index2 = getMatrixIndex(corrMatrix,
                        dataModel.getVariableNames().get(j));
                if (Math.abs(corrMatrix.getValue(index1, index2)) > correlation)
                {
                    count++;
                }
            }
        }
        if (count == 0) {
            return false;
        }
        int total = (int) Math.pow(2, count);
        int selection = JOptionPane.showConfirmDialog(
                JOptionUtils.centeringComp(), "This option will generate " +
                total + " extra data sets. Continue?", "Confirmation",
                JOptionPane.YES_NO_OPTION);
        return (selection == javax.swing.JOptionPane.YES_OPTION);
    }

    private int getMatrixIndex(CorrelationMatrix corrMatrix, String key) {
        for (int i = 0; i < corrMatrix.getVariableNames().size(); i++) {
            if (corrMatrix.getVariableNames().get(i).equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private DataModelList getSplitData(CorrelationMatrix corrMatrix,
            DataModel dataModel, double correlation, DataModelList modelList) {
        int index1 = -1, index2 = -1;
        boolean found = false;

        for (int i = 0;
                i < dataModel.getVariableNames().size() - 1 && !found; i++) {
            int index_i = getMatrixIndex(corrMatrix,
                    dataModel.getVariableNames().get(i));
            index1 = i;
            for (int j = i + 1;
                    j < dataModel.getVariableNames().size() && !found; j++) {
                int index_j = getMatrixIndex(corrMatrix,
                        dataModel.getVariableNames().get(j));
                index2 = j;
                if (Math.abs(corrMatrix.getValue(index_i, index_j)) >
                        correlation) {
                    found = true;
                }
            }
        }
        if (found) {
            if (dataModel instanceof RectangularDataSet) {
                ((RectangularDataSet) dataModel).removeColumn(index2);
                ((RectangularDataSet) dataModel).removeColumn(index1);
                getSplitData(corrMatrix, dataModel, correlation, modelList);
                getSplitData(corrMatrix, dataModel, correlation, modelList);
            }
            else { //CovarianceMatrix
                String subVarNames1[] = new String[dataModel.getVariableNames()
                        .size() - 1];
                String subVarNames2[] = new String[dataModel.getVariableNames()
                        .size() - 1];
                int count1 = 0, count2 = 0;
                for (int i = 0; i < dataModel.getVariableNames().size(); i++) {
                    if (i != index2) {
                        subVarNames1[count1++] =
                                dataModel.getVariableNames().get(i);
                    }
                    if (i != index1) {
                        subVarNames2[count2++] =
                                dataModel.getVariableNames().get(i);
                    }
                }
                CovarianceMatrix newDataModel1 =
                        ((CovarianceMatrix) dataModel).getSubmatrix(
                                subVarNames1);
                CovarianceMatrix newDataModel2 =
                        ((CovarianceMatrix) dataModel).getSubmatrix(
                                subVarNames2);
                getSplitData(corrMatrix, newDataModel1, correlation, modelList);
                getSplitData(corrMatrix, newDataModel2, correlation, modelList);
            }
        }
        else {
            modelList.add(dataModel);
        }

        return modelList;
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }

    /**
     * Allows the user to select a model type and indicate whether this model
     * type should be remembered.
     */
    private static final class CollinearityChooser extends JComponent {
        private int correlationSize;

        /**
         * Constructs a new component with the given model classes.
         */
        public CollinearityChooser(final DataEditor editor) {

            correlationSize = 95;

            setLayout(new BorderLayout());

            Box b1 = Box.createVerticalBox();
            Box b2 = Box.createHorizontalBox();
            JLabel l1 = new JLabel("Correlation threshold (x 100):");

            l1.setForeground(Color.black);
            b2.add(l1);
            b2.add(Box.createGlue());

            IntTextField correlationSizeField = new IntTextField(95, 3);
            correlationSizeField.setFilter(
                    new IntTextField.Filter() {
                        @Override
						public int filter(int value, int oldValue) {
                            if (value < 80 || value > 100) {
                                JOptionPane.showMessageDialog(
                                        JOptionUtils.centeringComp(),
                                        "Correlation threshold should be in " +
                                                "the [80, 100] interval",
                                        "Alert", JOptionPane.ERROR_MESSAGE);
                                return oldValue;
                            }

                            correlationSize = value;
                            return value;
                        }
                    });

            b2.add(correlationSizeField);
            b1.add(b2);
            add(b1, BorderLayout.CENTER);
        }

        /**
         * Returns the correlation level.
         */
        public int getCorrelationSize() {
            return correlationSize;
        }
    }

}


