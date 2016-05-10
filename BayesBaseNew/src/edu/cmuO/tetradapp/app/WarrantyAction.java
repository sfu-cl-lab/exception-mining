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

package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.util.LicenseUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Closes the frontmost session of the given desktop.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-06 15:57:49 -0500 (Fri, 06 Jan
 *          2006) $
 */
final class WarrantyAction extends AbstractAction {

    /**
     * Creates a new close session action for the given desktop.
     */
    public WarrantyAction() {
        super("Warranty");
    }

    /**
     * Closes the frontmost session of this action's desktop.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        String license = LicenseUtils.license();

        int index = license.indexOf("SUCH DAMAGES");

        JTextArea textArea = new JTextArea(license);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(600, 400));
        textArea.setCaretPosition(index);

        Box b = Box.createVerticalBox();
        b.add(scroll);

        JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), b,
                "Warranty", JOptionPane.PLAIN_MESSAGE);
    }
}
