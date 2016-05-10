package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataModelList;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.model.DataWrapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * New data loading action.
 *  
 *
 * @author Joseph Ramsey
 */
final class LoadDataAction extends AbstractAction {

    /**
     * The dataEditor into which data is loaded.                          -
     */
    private DataEditor dataEditor;

    /**
     * Creates a new load data action for the given dataEditor.
     */
    public LoadDataAction(DataEditor editor) {
        super("Load Data...");

        if (editor == null) {
            throw new NullPointerException("Data Editor must not be null.");
        }

        this.dataEditor = editor;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        // first warn user about other datasets being removed.
        if(!this.isDataEmpty()){
            String message = "Loading data from a file will remove all existing data in the data editor. " +
                    "Do you want to continue?";
            int option = JOptionPane.showOptionDialog(this.dataEditor, message, "Data Removal Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
            // if not yes, cancel action.
            if(option != JOptionPane.YES_OPTION){
                return;
            }
        }

        int ret = 1;
        while (ret == 1) {
            JFileChooser chooser = getJFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.showOpenDialog(this.dataEditor);

            final File file = chooser.getSelectedFile();

            if(file == null){
                return;
            }

            Preferences.userRoot().put("fileSaveLocation", file.getParent());
            final LoadDataDialog dialog = new LoadDataDialog(file);

            ret = JOptionPane.showOptionDialog(JOptionUtils.centeringComp(), dialog,
                    "Loading File " + file.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, new String[]{"Save", "Cancel"},
                    "Save");

            if (ret == 0) {
                DataModel dataModel = dialog.getDataModel();

                if (dataModel == null) {
                    return;
                }
                // relabel with file name if not already named.
                if(dataModel.getName() == null){
                    dataModel.setName(file.getName());
                }

                dataEditor.replace(dataModel);
                dataEditor.selectLastTab();
                firePropertyChange("modelChanged", null, null);
            }
        }
    }

    //======================= private methods =========================//

    /**
     * States whether the data is empty.
     */
    private boolean isDataEmpty(){
        DataWrapper wrapper = this.dataEditor.getDataWrapper();
        DataModelList dataModels = wrapper.getDataModelList();
        for(DataModel model : dataModels){
            if(model instanceof RectangularDataSet){
                return ((RectangularDataSet)model).getNumRows() == 0;
            } else {
                // how do you know in this case? Just say false
                return false;
            }
        }
        return true;
    }




    private static JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        String sessionSaveLocation =
                Preferences.userRoot().get("fileSaveLocation", "");
        chooser.setCurrentDirectory(new File(sessionSaveLocation));
        chooser.resetChoosableFileFilters();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return chooser;
    }
}
