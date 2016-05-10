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
import edu.cmu.tetradapp.model.KnowledgeEditable;
import edu.cmu.tetradapp.util.InternalClipboard;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

/**
 * Copies a parentally closed selection of session nodes in the frontmost
 * session editor to the clipboard.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
class PasteKnowledgeAction extends AbstractAction implements ClipboardOwner {
    /**
     * The knowledgeEditable containing the target session editor.
     */
    private KnowledgeEditable knowledgeEditable;

    /**
     * Constucts an action for loading the session in the given '.tet' file into
     * the knowledgeEditable.
     */
    public PasteKnowledgeAction(KnowledgeEditable knowledgeEditable) {
        super("Paste Knowledge");

        if (knowledgeEditable == null) {
            throw new NullPointerException();
        }

        this.knowledgeEditable = knowledgeEditable;
    }

    /**
     * Copies a parentally closed selection of session nodes in the frontmost
     * session editor to the clipboard.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Transferable transferable = InternalClipboard.getInstance()
                .getContents(null);

        if (!(transferable instanceof KnowledgeSelection)) {
            return;
        }

        KnowledgeSelection selection = (KnowledgeSelection) transferable;
        DataFlavor flavor =
                new DataFlavor(KnowledgeSelection.class, "Knowledge");

        try {
            Knowledge knowledge = (Knowledge) selection.getTransferData(flavor);

            if (knowledge != null) {
                this.knowledgeEditable.setKnowledge(knowledge);
            }
        }
        catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * Notifies this object that it is no longer the owner of the contents of
     * the clipboard.
     *
     * @param clipboard the clipboard that is no longer owned
     * @param contents  the contents which this owner had placed on the
     *                  clipboard
     */
    @Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}


