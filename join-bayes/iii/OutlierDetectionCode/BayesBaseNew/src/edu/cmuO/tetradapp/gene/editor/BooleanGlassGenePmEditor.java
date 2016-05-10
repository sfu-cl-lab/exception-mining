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

package edu.cmu.tetradapp.gene.editor;

import edu.cmu.tetradapp.model.BooleanGlassGenePm;

import javax.swing.*;

/**
 * Holds place for an editor that is not yet needed. At present, the Boolean
 * Blass Gene PM has no parameters in addition to the editor itself.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class BooleanGlassGenePmEditor extends JPanel {

    /**
     * Constructs a new Boolean Glass GenePM Editor.
     */
    public BooleanGlassGenePmEditor() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel(
                "There are no parameters in the Boolean Glass Gene PM."));
    }

    public BooleanGlassGenePmEditor(BooleanGlassGenePm pm) {
        this();
    }
}


