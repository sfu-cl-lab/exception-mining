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

import edu.cmu.tetrad.sem.SemEvidence;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Edits evidence for a Bayes net--that is, which categories (perhaps
 * disjunctive) are known for which variables, and which variables are
 * manipulated.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6042 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
class SemEvidenceEditor extends JPanel {
    private SemEvidence evidence;
    private Map checkBoxesToVariables = new HashMap();
    private Map variablesToCheckboxes = new HashMap();

    public SemEvidenceEditor(SemEvidence evidence) {
        if (evidence == null) {
            throw new NullPointerException();
        }

        this.evidence = evidence;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Box d = Box.createHorizontalBox();
        d.add(new JLabel("Variable = value"));
        d.add(Box.createHorizontalGlue());
        d.add(new JLabel("Manipulated"));
        add(d);

        for (int i = 0; i < evidence.getNumNodes(); i++) {
            Box c = Box.createHorizontalBox();
            SemIm semIm = evidence.getSemIm();
            String name = (semIm.getVariableNodes().get(i)).getName();
            JLabel label = new JLabel(name + " =  ") {
                @Override
				public Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            c.add(label);

            DoubleTextField field = new DoubleTextField(
                    evidence.getProposition().getValue(i), 5, NumberFormatUtil.getInstance().getNumberFormat());

            c.add(field);
            c.add(Box.createHorizontalStrut(2));
            c.add(Box.createHorizontalGlue());

            JCheckBox checkbox = new JCheckBox() {
                @Override
				public Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            checkbox.setSelected(getEvidence().isManipulated(i));
            checkBoxesToVariables.put(checkbox, i);
            variablesToCheckboxes.put(i, checkbox);
            checkbox.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent e) {
                    JCheckBox checkbox = (JCheckBox) e.getSource();
                    boolean selected = checkbox.isSelected();
                    Object o = checkBoxesToVariables.get(checkbox);
                    int variable = (Integer) o;

                    getEvidence().setManipulated(variable, selected);
                }
            });
            checkbox.setBackground(Color.WHITE);
            checkbox.setBorder(null);
            c.add(checkbox);
            c.setMaximumSize(new Dimension(1000, 30));
            add(c);
        }
    }

    public SemEvidence getEvidence() {
        return evidence;
    }
}


