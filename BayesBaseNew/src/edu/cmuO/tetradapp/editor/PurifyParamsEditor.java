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
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.search.Purify;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MimParams;
import edu.cmu.tetradapp.model.PurifyParams;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class should access the getMappings mapped to it from the mapping to the
 * search classes. </p> This class is the parameter editor currently for Purify
 * parameters
 *
 * @author Ricardo Silva rbas@cs.cmu.edu
 * @version $Revision: 6210 $
 */
public class PurifyParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameter wrapper being viewed.
     */
    private PurifyParams params;
    private Object[] parentModels;

    /**
     * Opens up an editor to let the user view the given PurifyRunner.
     */
    public PurifyParamsEditor() {
    }

    @Override
	public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (PurifyParams) params;
    }

    @Override
	public void setParentModels(Object[] parentModels) {
        this.parentModels = parentModels;
    }

    @Override
	public void setup() {
        DoubleTextField alphaField = new DoubleTextField(params.getAlpha(), 4,
                NumberFormatUtil.getInstance().getNumberFormat());
        alphaField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    getParams().setAlpha(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        final String[] descriptions = Purify.getTestDescriptions();
        JComboBox testSelector = new JComboBox(descriptions);
        testSelector.setSelectedIndex(params.getTetradTestType());

        testSelector.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox) e.getSource();
                int index = combo.getSelectedIndex();
                getParams().setTetradTestType(index);
            }
        });

        boolean discreteModel = setVarNames(parentModels, params);

        JButton editClusters = new JButton("Edit");
        editClusters.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                openClusterEditor();
            }
        });

        Box b = Box.createVerticalBox();

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Alpha:"));
        b1.add(Box.createHorizontalGlue());
        b1.add(alphaField);
        b.add(b1);

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Cluster Assignments:"));
        b2.add(Box.createHorizontalGlue());
        b2.add(editClusters);
        b.add(b2);

        if (!discreteModel) {
            Box b3 = Box.createHorizontalBox();
            b3.add(new JLabel("Statistical Test:"));
            b3.add(Box.createHorizontalGlue());
            b3.add(testSelector);
            b.add(b3);
        }
        else {
            this.params.setTetradTestType(Purify.TEST_DISCRETE_LRT);
        }

        setLayout(new BorderLayout());
        add(b, BorderLayout.CENTER);
    }

    @Override
	public boolean mustBeShown() {
        return false;
    }

    private boolean setVarNames(Object[] parentModels, PurifyParams params) {
        DataModel dataModel = null;

        for (Object parentModel : parentModels) {
            if (parentModel instanceof DataWrapper) {
                DataWrapper dataWrapper = (DataWrapper) parentModel;
                dataModel = dataWrapper.getSelectedDataModel();
            }
        }

        boolean discreteModel;

        if (dataModel instanceof CovarianceMatrix) {
            discreteModel = false;
        }
        else {
            RectangularDataSet dataSet = (RectangularDataSet) dataModel;
            assert dataSet != null;
            discreteModel = dataSet.isDiscrete();

//            try {
//                new DataSet((DataSet) dataModel);
//                discreteModel = true;
//            }
//            catch (IllegalArgumentException e) {
//                discreteModel = false;
//            }
        }

        getParams().setVarNames(params.getVarNames());
        return discreteModel;
    }

    /**
     * Must pass knowledge from getMappings. If null, creates new knowledge
     * object.
     */
    private void openClusterEditor() {
        ClusterEditor clusterEditor = new ClusterEditor(
                getParams().getClusters(), getParams().getVarNames());

        EditorWindow window = new EditorWindow(clusterEditor,
                clusterEditor.getName(), "Save", false);
        DesktopController.getInstance().addEditorWindow(window);
        window.setVisible(true);
    }

    private MimParams getParams() {
        return this.params;
    }
}


