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

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.EventObject;

/**
 * Edits a cell in a data table.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
class DataCellEditor extends DefaultCellEditor {
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
    private JTextField textField;

    /**
     * Constructs a new number cell editor.
     */
    public DataCellEditor() {
        super(new JTextField());

        textField = (JTextField) editorComponent;
        textField.setHorizontalAlignment(SwingConstants.LEFT);
        textField.setBorder(new LineBorder(Color.BLACK));

        delegate = new EditorDelegate() {
            @Override
			public void setValue(Object value) {
                if (value == null) {
                    textField.setText("");
                }
                else if (value instanceof String) {
                    textField.setText((String) value);
                }
                else if (value instanceof Integer) {
                    textField.setText(value.toString());
                }
                else if (value instanceof Double) {
                    double doubleValue = (Double) value;

                    if (Double.isNaN(doubleValue)) {
                        textField.setText("");
                    }
                    else {
                        textField.setText(nf.format(doubleValue));
                    }
                }

                textField.selectAll();
            }

            /**
             * Overrides delegate; gets the text value from the cell to send
             * back to the model.
             *
             * @return this text getValue.
             */
            @Override
			public Object getCellEditorValue() {
                return textField.getText();
            }
        };

        textField.addActionListener(delegate);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see javax.swing.DefaultCellEditor.EditorDelegate#shouldSelectCell(java.util.EventObject)
     */
    @Override
	public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
}
