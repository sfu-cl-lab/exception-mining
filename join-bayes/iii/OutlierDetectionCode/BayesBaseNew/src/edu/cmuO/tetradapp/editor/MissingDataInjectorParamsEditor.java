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
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.MissingDataInjectorParams;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;

/**
 * Edits the parameters for simulating data from Bayes nets.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @author Frank Wimberly based on similar classes by Joe Ramsey
 * @version $Revision: 6042 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public class MissingDataInjectorParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameters object being edited.
     */
    private MissingDataInjectorParams params = null;

    //========================= Public Methods =======================================//

    @Override
	public void setup() {
        buildGui();
    }

    @Override
	public void setParams(Params params) {
        this.params = (MissingDataInjectorParams)params;
    }

    @Override
	public void setParentModels(Object[] parentModels) {

    }

    @Override
	public boolean mustBeShown() {
        return true;
    }

    //======================== Private Methods ====================================//

    /**
     * Constructs the Gui used to edit properties; called from each constructor.
     * Constructs labels and text fields for editing each property and adds
     * appropriate listeners.
     */
    private void buildGui() {
        setLayout(new BorderLayout());

        final DoubleTextField probField =
                new DoubleTextField(params.getProb(), 6, NumberFormatUtil.getInstance().getNumberFormat());
        probField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    params.setProb(value);
                    return value;
                }
                catch (IllegalArgumentException e) {
                    return oldValue;
                }
            }
        });

        // continue workbench construction.
        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("<html>" +
                "The input dataset will have missing data values inserted " +
                "<br>independently for each variable in each case with the" +
                "<br>probability specified." + "</html>"));

        Box b7 = Box.createHorizontalBox();
        b7.add(Box.createHorizontalGlue());
        b7.add(new JLabel("<html>" + "<i>Probability:  </i>" + "</html>"));
        b7.add(probField);

        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));
        b1.add(b7);
        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

}


