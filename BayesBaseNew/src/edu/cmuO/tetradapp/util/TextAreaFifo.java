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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 * Text area that removes lines from the top as the line limit is exceeded.
 *
 * @author http://forum.java.sun.com/thread.jspa?threadID=563827&tstart=90
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public class TextAreaFifo extends JTextArea implements DocumentListener {
    private int maxLines;

    public TextAreaFifo(int maxLines) {
        this.maxLines = maxLines;
        getDocument().addDocumentListener(this);
    }

    @Override
	public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                removeLines();
            }
        });
    }

    @Override
	public void removeUpdate(DocumentEvent e) {
    }

    @Override
	public void changedUpdate(DocumentEvent e) {
    }

    public void removeLines() {
        Element root = getDocument().getDefaultRootElement();

        while (root.getElementCount() > maxLines) {
            Element firstLine = root.getElement(0);

            try {
                getDocument().remove(0, firstLine.getEndOffset());
            }
            catch (BadLocationException ble) {
                System.out.println(ble);
            }
        }
    }
}


