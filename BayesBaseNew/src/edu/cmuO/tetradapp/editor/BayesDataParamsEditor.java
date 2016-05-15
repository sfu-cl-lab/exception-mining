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

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetradapp.model.BayesDataParams;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * Edits the parameters for simulating new datasets from a Bayes net.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6059 $
 */
public final class BayesDataParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The objects storing values of parameters needed to simulate data.
     */
    private BayesDataParams params;

    /**
     * Blank constructor.
     */
    public BayesDataParamsEditor() {
    }

    /**
     * Sets the parameter-storing object. This is a separate method because
     * a blank constructor is needed.
     */
    @Override
	public void setParams(Params params) {
        this.params = (BayesDataParams) params;
    }

    /**
     * A method required by the interface that does nothing.
     */
    @Override
	public void setParentModels(Object[] parentModels) {
        // Do nothing.
    }

    /**
     * Constructs the GUI.
     */
    @Override
	public void setup() {
        // set up text and ties them to the parameters object being edited.
        IntTextField sampleSizeField = new IntTextField(getParams().getSampleSize(), 4);
        sampleSizeField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                try {
                    getParams().setSampleSize(value);
                    return value;
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        JCheckBox latentDataSaved = new JCheckBox("Save Latent Data",
                Preferences.userRoot().getBoolean("latentDataSaved", false));

        latentDataSaved.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                params.setLatentDataSaved(checkBox.isSelected());
                Preferences.userRoot().putBoolean("latentDataSaved",
                        checkBox.isSelected());
            }
        });

        // construct the workbench.

        setLayout(new BorderLayout());

        // continue workbench construction.
        Box b = Box.createVerticalBox();

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Sample size:  "));
        b1.add(Box.createHorizontalGlue());
        b1.add(sampleSizeField);
        b.add(b1);

        Box b2 = Box.createHorizontalBox();
        b2.add(latentDataSaved);
        b.add(b2);

        add(b, BorderLayout.CENTER);
    }

    @Override
	public boolean mustBeShown() {
        return true;
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    private synchronized BayesDataParams getParams() {

        // Unused.
        //        String ret = (this.getMappings == null)
        //                ? "null"
        //                : this.getMappings.toString();

        return this.params;
    }
}


