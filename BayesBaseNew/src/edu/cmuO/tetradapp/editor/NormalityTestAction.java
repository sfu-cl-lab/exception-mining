package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.editor.QQPlot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Displays a Q-Q plot for a random variable.
 *
 * A lot of the code borrows heavily from HistogramAction
 *
 * @author Michael Freenor
 */

public class NormalityTestAction extends AbstractAction {


    /**
     * The data edtitor that action is attached to.
     */
    private DataEditor dataEditor;


    /**
     * Constructs the <code>QQPlotAction</code> given the <code>DataEditor</code>
     * that its attached to.
     *
     * @param editor
     */
    public NormalityTestAction(DataEditor editor) {
        super("Run Normality Tests");
        this.dataEditor = editor;
    }






    @Override
	public void actionPerformed(ActionEvent e) {
        RectangularDataSet dataSet = (RectangularDataSet) dataEditor.getSelectedDataModel();
        if(dataSet == null || dataSet.getNumColumns() == 0){
            JOptionPane.showMessageDialog(findOwner(), "Cannot run normality tests on an empty data set.");
            return;
        }
        // if there are missing values warn and don't display q-q plot.
        if(DataUtils.containsMissingValue(dataSet)){
            JOptionPane.showMessageDialog(findOwner(), new JLabel("<html>Data has missing values, " +
                    "remove all missing values before<br>" +
                    "running normality tests.</html>"));
            return;
        }

        JDialog dialog = createNormalityTestDialog(null);
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);

    }

    //============================== Private methods ============================//





    /**
     * Sets the location on the given dialog for the given index.
     */
    private void setLocation(JDialog dialog, int index) {
        Rectangle bounds = dialog.getBounds();
        JFrame frame = findOwner();
        Dimension dim;
        if (frame == null) {
            dim = Toolkit.getDefaultToolkit().getScreenSize();
        } else {
            dim = frame.getSize();
        }

        int x = (int) (150 * Math.cos(index * 15 * (Math.PI / 180)));
        int y = (int) (150 * Math.sin(index * 15 * (Math.PI / 180)));
        x += (dim.width - bounds.width)/2;
        y += (dim.height - bounds.height)/2;
        dialog.setLocation(x, y);
    }


    /**
     * Creates a dialog that is showing the histogram for the given node (if null
     * one is selected for you)
     */
    private JDialog createNormalityTestDialog(Node selected) {
        String dialogTitle = "Normality Tests";
        JDialog dialog = new JDialog(findOwner(), dialogTitle, false);
        dialog.setResizable(false);
        dialog.getContentPane().setLayout(new BorderLayout());
        RectangularDataSet dataSet = (RectangularDataSet) dataEditor.getSelectedDataModel();

        QQPlot qqPlot = new QQPlot(dataSet, selected);
        NormalityTestEditorPanel editorPanel = new NormalityTestEditorPanel(qqPlot, dataSet);
        //QQPlotDisplayPanel display = new QQPlotDisplayPanel(qqPlot);
        JTextArea display = new JTextArea(NormalityTests.runNormalityTests(dataSet, (ContinuousVariable)qqPlot.getSelectedVariable()), 20, 65);
        display.setEditable(false);
        editorPanel.addPropertyChangeListener(new NormalityTestListener(display));

        /*
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(new JMenuItem(new SaveComponentImage(display, "Save Q-Q Plot")));
        bar.add(menu);
        */

        Box box = Box.createHorizontalBox();
        box.add(display);

        box.add(Box.createHorizontalStrut(3));
        box.add(editorPanel);
        box.add(Box.createHorizontalStrut(5));
        box.add(Box.createHorizontalGlue());

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(15));
        vBox.add(box);
        vBox.add(Box.createVerticalStrut(5));

        //dialog.getContentPane().add(bar, BorderLayout.NORTH);
        dialog.getContentPane().add(vBox, BorderLayout.CENTER);
        return dialog;
    }


    private JFrame findOwner() {
        return (JFrame) SwingUtilities.getAncestorOfClass(
                JFrame.class, dataEditor);
    }

    //================================= Inner Class ======================================//



    /**
     * Glue between the editor and the display.
     */
    private static class NormalityTestListener implements PropertyChangeListener {

        private JTextArea display;


        public NormalityTestListener(JTextArea display) {
            this.display = display;
        }


        @Override
		public void propertyChange(PropertyChangeEvent evt) {
            if ("histogramChange".equals(evt.getPropertyName())) {
                this.display.setText((String) evt.getNewValue());
            }
        }
    }


}