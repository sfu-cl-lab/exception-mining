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
import edu.cmu.tetradapp.model.StructEmBayesSearchParams;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;

/**
 * Edits the parameters for simulating data from Bayes nets.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @author Frank Wimberly based on similar classes by Joe Ramsey
 * @version $Revision: 6042 $ $Date: 2006-01-05 16:37:15 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class StructEMBayesSearchParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameters object being edited.
     */
    private StructEmBayesSearchParams params = null;

    @Override
	public void setParams(Params params) {
        this.params = (StructEmBayesSearchParams) params;
    }

    @Override
	public void setParentModels(Object[] parentModels) {
        // ignore.
    }

    @Override
	public boolean mustBeShown() {
        return false;
    }

    /**
     * Constructs the Gui used to edit properties; called from each constructor.
     * Constructs labels and text fields for editing each property and adds
     * appropriate listeners.
     */
    @Override
	public void setup() {
        setLayout(new BorderLayout());

        final DoubleTextField toleranceField =
                new DoubleTextField(params.getTolerance(), 8, NumberFormatUtil.getInstance().getNumberFormat());
        toleranceField.setFilter(new DoubleTextField.Filter() {
            @Override
			public double filter(double value, double oldValue) {
                try {
                    params.setTolerance(value);
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
                "The dataset will be used to iteratively estmate models (Bayes nets) " +
                "<br>using the BDe metric until the model does not change from one " +
                "<br>iteration to the next.  " + "</html>"));

        Box b7 = Box.createHorizontalBox();
        b7.add(Box.createHorizontalGlue());
        b7.add(new JLabel("<html>" + "<i>The default value is 0.0001</i>" +
                "</html>"));
        b7.add(toleranceField);

        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));
        b1.add(b7);
        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    protected synchronized StructEmBayesSearchParams getParams() {
        return this.params;
    }
}


