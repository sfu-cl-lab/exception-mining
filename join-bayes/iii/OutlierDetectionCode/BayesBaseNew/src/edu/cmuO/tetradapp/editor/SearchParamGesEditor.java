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
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.model.Params;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.knowledge_editor.KnowledgeEditor;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.GesParams;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Edits parameters for GES.
 *
 * @author Ricardo Silva
 */
public final class SearchParamGesEditor extends JPanel implements
        ParameterEditor {

    /**
     * The parameter object being edited.
     */
    private GesParams params;

    /**
     * The name of the algorithm.
     */
    private String name;

    /**
     * The parent models of this search object; should contain a DataModel.
     */
    private Object[] parentModels;

    /**
     * The variable names from the object being searched over (usually data).
     */
    private List<String> varNames;

    /**
     * Opens up an editor to let the user view the given PcRunner.
     */

    public SearchParamGesEditor() {
    }

    @Override
	public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (GesParams) params;
    }

    @Override
	public void setParentModels(Object[] parentModels) {
        if (parentModels == null) {
            throw new NullPointerException();
        }

        this.parentModels = parentModels;
    }

    @Override
	public void setup() {
        this.varNames = params.getVarNames();
        this.name = "GES Algorithm";

        DoubleTextField cellPriorField =
                new DoubleTextField(params.getCellPrior(), 3, NumberFormatUtil.getInstance().getNumberFormat());
        cellPriorField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    getParams().setCellPrior(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        DoubleTextField structurePriorField = new DoubleTextField(
                params.getStructurePrior(), 5, NumberFormatUtil.getInstance().getNumberFormat());
        structurePriorField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    getParams().setStructurePrior(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        DataModel dataModel = null;

        for (Object parentModel : parentModels) {
            if (parentModel instanceof DataWrapper) {
                DataWrapper dataWrapper = (DataWrapper) parentModel;
                dataModel = dataWrapper.getSelectedDataModel();
            }
        }

        if (dataModel != null) {
            varNames = new ArrayList<String>(dataModel.getVariableNames());
        }
        else {
            throw new RuntimeException("Null Data Model");
        }

        boolean isDiscreteModel;
        if (dataModel instanceof CovarianceMatrix) {
            isDiscreteModel = false;
        }
        else {
            RectangularDataSet dataSet = (RectangularDataSet) dataModel;
            isDiscreteModel = dataSet.isDiscrete();
        }

        getParams().setVarNames(varNames);

        setBorder(new MatteBorder(10, 10, 10, 10, super.getBackground()));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel[] rows = new JPanel[5];

        for (int i = 0; i < 5; i++) {
            rows[i] = new JPanel();
            rows[i].setBackground(super.getBackground());
        }

        if (isDiscreteModel) {
            // --First Row
            String msg0 = "Cell prior:";
            rows[0].setLayout(new FlowLayout(FlowLayout.LEFT));
            rows[0].setMaximumSize(new Dimension(1000, 30));
            rows[0].add(new JLabel(msg0));
            rows[0].add(cellPriorField);
            add(rows[0]);

            // --Second Row
            String msg1 = "Structure prior:";
            rows[1].setLayout(new FlowLayout(FlowLayout.LEFT));
            rows[1].setMaximumSize(new Dimension(1000, 30));
            rows[1].add(new JLabel(msg1));
            rows[1].add(structurePriorField);
            add(rows[1]);
        }
        else {
            // --First Row (continuous data)
            String msg0 = "BIC score will be used. No parameters to set.";
            rows[0].setLayout(new FlowLayout(FlowLayout.LEFT));
            rows[0].setMaximumSize(new Dimension(1000, 30));
            rows[0].add(new JLabel(msg0));
            add(rows[0]);
        }

        // --Fourth Row
        JButton knowledgeButton = new JButton("View Background Knowledge");
        rows[3].setLayout(new FlowLayout(FlowLayout.LEFT));
        rows[3].setMaximumSize(new Dimension(1000, 30));
        rows[3].add(knowledgeButton);
        add(rows[3]);

        knowledgeButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                openKnowledgeEditor();
            }
        });
    }

    @Override
	public boolean mustBeShown() {
        return false;
    }

    /**
     * Must pass knowledge from getMappings. If null, creates new knowledge
     * object.
     */
    private void openKnowledgeEditor() {

        if (this.getParams() == null) {
            throw new NullPointerException("Parameter object must not be " +
                    "null if you want to launch a OldKnowledgeEditor.");

        }

        Knowledge knowledge = this.getParams().getKnowledge();

        KnowledgeEditor knowledgeEditor = new KnowledgeEditor(knowledge,
                varNames, getParams().getSourceGraph());
        EditorWindow window = new EditorWindow(knowledgeEditor,
                knowledgeEditor.getName(), "Save", false);
        DesktopController.getInstance().addEditorWindow(window);
        window.setVisible(true);
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        String oldName = getName();
        this.name = name;
        super.setName(name);
        firePropertyChange("name", oldName, getName());
    }

    private GesParams getParams() {
        return params;
    }

    private void setParams(GesParams params) {
        this.params = params;
    }
}


