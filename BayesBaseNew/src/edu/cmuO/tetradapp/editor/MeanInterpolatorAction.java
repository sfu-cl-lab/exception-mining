package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.bayes.MeanInterpolator;
import edu.cmu.tetrad.data.*;
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
public final class MeanInterpolatorAction extends AbstractAction {

    /**
     * The data editor.                         -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new action to split by collinear columns.
     */
    public MeanInterpolatorAction(DataEditor editor) {
        super("Replace Missing Values with Column Mean");

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

            DataFilter interpolator = new MeanInterpolator();
            RectangularDataSet newDataSet = interpolator.filter(dataSet);

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
