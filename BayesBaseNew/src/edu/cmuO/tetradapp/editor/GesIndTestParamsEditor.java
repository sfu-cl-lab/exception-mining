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

import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.GesIndTestParams;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

/**
 * Edits the properties of a GesSearch.
 *
 * @author Ricardo Silva
 * @version $Revision: 6042 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */

class GesIndTestParamsEditor extends JComponent {

    private GesIndTestParams params;
    private DoubleTextField cellPriorField, structurePriorField;
    private JButton defaultStructurePrior;
    private JButton uniformStructurePrior;
    private boolean discreteData;

    public GesIndTestParamsEditor(GesIndTestParams simulator,
            boolean discreteData) {
        this.params = simulator;
        this.discreteData = discreteData;

        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        this.cellPriorField = new DoubleTextField(
                getGesIndTestParams().getCellPrior(), 5, nf);

        this.cellPriorField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    getGesIndTestParams().setCellPrior(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        this.structurePriorField = new DoubleTextField(
                getGesIndTestParams().getStructurePrior(), 5, nf);
        this.structurePriorField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    getGesIndTestParams().setStructurePrior(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        this.defaultStructurePrior =
                new JButton("Default structure prior = 0.05");
        Font font = new Font("Dialog", Font.BOLD, 10);
        this.defaultStructurePrior.setFont(font);
        this.defaultStructurePrior.setBorder(null);
        this.defaultStructurePrior.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                structurePriorField.setValue(0.05);
            }
        });

        this.uniformStructurePrior =
                new JButton("Uniform structure prior = 1.0");
        this.uniformStructurePrior.setFont(font);
        this.uniformStructurePrior.setBorder(null);
        this.uniformStructurePrior.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                structurePriorField.setValue(1.0);
            }
        });

        buildGui();
    }

    private void buildGui() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (discreteData) {
            Box b0 = Box.createHorizontalBox();
            b0.add(new JLabel("Discrete (BDeu):"));
            b0.add(Box.createHorizontalGlue());
            add(b0);
            add(Box.createVerticalStrut(5));

            Box b2 = Box.createHorizontalBox();
            b2.add(Box.createHorizontalStrut(5));
            b2.add(new JLabel("Sample prior:"));
            b2.add(Box.createHorizontalGlue());
            b2.add(this.cellPriorField);
            add(b2);
            add(Box.createVerticalStrut(5));

            Box b3 = Box.createHorizontalBox();
            b3.add(Box.createHorizontalStrut(5));
            b3.add(new JLabel("Structure prior:"));
            b3.add(Box.createHorizontalGlue());
            b3.add(this.structurePriorField);
            add(b3);

            Box b4 = Box.createHorizontalBox();
            b4.add(Box.createHorizontalGlue());
            b4.add(this.defaultStructurePrior);
            add(b4);

            Box b5 = Box.createHorizontalBox();
            b5.add(Box.createHorizontalGlue());
            b5.add(this.uniformStructurePrior);
            add(b5);
            add(Box.createVerticalStrut(10));

            Box b6 = Box.createHorizontalBox();
            b6.add(new JLabel("Continuous (SEM Score): No parameters."));
            b6.add(Box.createHorizontalGlue());
            add(b6);
        }
        else {
            Box b1 = Box.createHorizontalBox();
            b1.add(new JLabel("No parameters to set"));
            add(b1);
            add(Box.createHorizontalGlue());
        }

    }

    private GesIndTestParams getGesIndTestParams() {
        return params;
    }

}


