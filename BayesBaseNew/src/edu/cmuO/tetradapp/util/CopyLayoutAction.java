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

package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.graph.Graph;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

/**
 * Copies the current graph in a given container, storing it so that another
 * container can be laid out the same way.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-09 16:30:41 -0500 (Mon, 09 Jan
 *          2006) $
 */
public class CopyLayoutAction extends AbstractAction implements ClipboardOwner {

    /**
     * The LayoutEditable containing the target session editor.
     */
    private LayoutEditable layoutEditable;

    /**
     * Creates a new copy subsession action for the given LayoutEditable and
     * clipboard.
     */
    public CopyLayoutAction(LayoutEditable layoutEditable) {
        super("Copy Layout");

        if (layoutEditable == null) {
            throw new NullPointerException();
        }

        this.layoutEditable = layoutEditable;
    }

    /**
     * Copies a parentally closed selection of session nodes in the frontmost
     * session editor to the clipboard.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Graph layoutGraph = layoutEditable.getGraph();
        LayoutSelection selection = new LayoutSelection(layoutGraph);
        InternalClipboard.getInstance().setContents(selection, this);
    }

    /**
     * Required by the AbstractAction interface; does nothing.
     */
    @Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}


