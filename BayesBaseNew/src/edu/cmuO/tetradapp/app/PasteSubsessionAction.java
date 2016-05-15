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

import edu.cmu.tetradapp.model.EditorUtils;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.InternalClipboard;
import edu.cmu.tetradapp.util.SessionEditorIndirectRef;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Copies a parentally closed selection of session nodes in the frontmost
 * session editor to the clipboard.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
final class PasteSubsessionAction extends AbstractAction
        implements ClipboardOwner {

    /**
     * Constucts an action for loading the session in the given '.tet' file into
     * the desktop.
     */
    public PasteSubsessionAction() {
        super("Paste");
    }

    /**
     * Copies a parentally closed selection of session nodes in the frontmost
     * session editor to the clipboard.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Transferable transferable = InternalClipboard.getInstance()
                .getContents(null);

        if (!(transferable instanceof SubsessionSelection)) {
            return;
        }

        SubsessionSelection selection = (SubsessionSelection) transferable;
        DataFlavor flavor = new DataFlavor(SubsessionSelection.class,
                "Subsession Selection");

        try {
            List modelList = (List) selection.getTransferData(flavor);

            if (modelList != null) {
                SessionEditorIndirectRef sessionEditorRef =
                        DesktopController.getInstance().getFrontmostSessionEditor();
                SessionEditor sessionEditor = (SessionEditor) sessionEditorRef;
                Point point = EditorUtils.getTopLeftPoint(modelList);
                int numPastes = selection.getNumPastes();
                point.translate(50 * numPastes, 50 * numPastes);

//                Point point = sessionEditor.getSessionWorkbench().getCurrentMouseLocation();
//
                sessionEditor.pasteSubsession(modelList, point);
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


