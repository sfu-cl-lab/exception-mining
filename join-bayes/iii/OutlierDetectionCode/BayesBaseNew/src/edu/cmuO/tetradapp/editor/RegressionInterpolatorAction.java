package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.search.RegressionInterpolator;
import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Splits continuous data sets by collinear columns.
 *
 * @author Ricardo Silva
 * @version $Revision: 4524 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public final class RegressionInterpolatorAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new action to split by collinear columns.
     */
    public RegressionInterpolatorAction(DataEditor editor) {
        super("Replace Missing Values with Regression Predictions");

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

        if (dataModel instanceof RectangularDataSet) {
            RectangularDataSet dataSet = (RectangularDataSet) dataModel;

            DataFilter interpolator = new RegressionInterpolator();
            RectangularDataSet newDataSet = null;
            try {
                newDataSet = interpolator.filter(dataSet);
            } catch (IllegalArgumentException e1) {
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Error: " + e1.getMessage());
                throw e1;
            }

            DataModelList list = new DataModelList();
            list.add(newDataSet);
            getDataEditor().reset(list);
            getDataEditor().selectLastTab();
        }
        else if (dataModel instanceof CovarianceMatrix) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    "Must be a tabular data set.");
        }
    }

    private DataEditor getDataEditor() {
        return dataEditor;
    }
}
