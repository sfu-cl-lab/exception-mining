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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.model.EditorUtils;
import edu.cmu.tetradapp.model.KnowledgeEditable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Loads knowledge.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
final class SaveKnowledgeAction extends AbstractAction {

    /**
     * The component that file choosers will be centered on.
     */
    private KnowledgeEditable knowledgeEditable;

    /**
     * Creates a new load data action for the given knowledgeEditable.
     *
     * @param knowledgeEditable The component to center the wizard on.
     */
    public SaveKnowledgeAction(KnowledgeEditable knowledgeEditable) {
        super("Save Knowledge...");

        if (knowledgeEditable == null) {
            throw new NullPointerException();
        }

        this.knowledgeEditable = knowledgeEditable;
    }

    /**
     * Performs the action of loading a session from a file.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Component comp =
                (this.knowledgeEditable instanceof Component) ? (Component) this.knowledgeEditable : null;
        File file = EditorUtils.getSaveFile("knowledge", "txt", comp, false);

        if (file != null) {
            try {
                FileWriter writer = new FileWriter(file);
                Knowledge knowledge = this.knowledgeEditable.getKnowledge();
                Knowledge.saveKnowledge(knowledge, writer);
                writer.close();
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        "Saved knowledge as " + file.getAbsoluteFile() + ".");
            }
            catch (IOException e1) {
                String message = e1.getMessage() ==
                        null ? e1.getClass().getName() : e1.getMessage();
                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                        message);
            }
        }
    }
}



