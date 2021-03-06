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

import edu.cmu.tetradapp.util.DesktopController;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Exits the Tetrad application.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-09 15:55:02 -0500 (Mon, 09 Jan
 *          2006) $
 */
final class ExitAction extends AbstractAction {

    /**
     * Creates an exit action.
     */
    public ExitAction() {
        super("Exit");
    }

    /**
     * Performs the action of loading a session from a file.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        DesktopController.getInstance().exitProgram();
    }
}


