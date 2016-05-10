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
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;

/**
 * Edits the parameters for generating random graphs.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6042 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
class RowTotalEditor extends JPanel {

    /**
     * Lets the user edit the number of nodes.
     */
    private DoubleTextField rowTotalField;

    /**
     * Constructs a dialog to input a row total for randomly generating
     * Dirichlet Bayes Im rows.
     */
    public RowTotalEditor(final double initialRowTotal) {
        rowTotalField = new DoubleTextField(initialRowTotal, 4, NumberFormatUtil.getInstance().getNumberFormat());
        buildGui();
    }

    public double getRowTotal() {
        return rowTotalField.getValue();
    }

    private void buildGui() {
        setLayout(new BorderLayout());

        // continue workbench construction.
        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalStrut(25));
        b2.add(new JLabel("Enter row total:"));
        b2.add(Box.createRigidArea(new Dimension(10, 0)));
        b2.add(Box.createHorizontalGlue());
        b2.add(rowTotalField);

        b1.add(b2);

        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }
}


