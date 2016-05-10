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

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.EventObject;

/**
 * Displays a DataSet object as a JTable.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public class TabularDataJTable extends JTable implements DataModelContainer,
        PropertyChangeListener {

    /**
     * The number of initial "special" columns not used to display the data
     * set.
     */
    private int numLeadingCols = 2;


    /**
     * States whether edits are allowed.
     */
    private boolean editable = true;

    /**
     * Constructor. Takes a DataSet as a model.
     */
    public TabularDataJTable(RectangularDataSet model) {
        TabularDataTable dataModel = new TabularDataTable(model);
        dataModel.addPropertyChangeListener(this);
        setModel(dataModel);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int rowCount = this.dataModel.getRowCount();
        int max = 0;

        while (rowCount > 0) {
            rowCount /= 10;
            max++;
        }

        addMouseListener(new MouseAdapter() {
            @Override
			public void mousePressed(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());

                if (row == 0) {
                    setRowSelectionAllowed(false);
                    setColumnSelectionAllowed(true);
                } else if (col == 0) {
                    setRowSelectionAllowed(true);
                    setColumnSelectionAllowed(false);
                } else {
                    setRowSelectionAllowed(true);
                    setColumnSelectionAllowed(true);
                }
            }
        });

        FontMetrics metrics = getFontMetrics(getFont());

        getColumnModel().getColumn(0).setMaxWidth(9 * max);
        getColumnModel().getColumn(1).setMaxWidth(9 * 4);
        setRowHeight(metrics.getHeight() + 3);

        setRowSelectionAllowed(true);
        getColumnModel().setColumnSelectionAllowed(true);

        for (int i = 0; i < model.getNumColumns(); i++) {
            if (model.isSelected(model.getVariable(i))) {
                setRowSelectionAllowed(false);
                addColumnSelectionInterval(i + 2, i + 2);
            }
        }

        getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
			public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
			public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
			public void columnMoved(TableColumnModelEvent e) {
            }

            @Override
			public void columnMarginChanged(ChangeEvent e) {
            }

            /**
             * Sets the selection of columns in the model to what's in the
             * display.
             */
            @Override
			public void columnSelectionChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel selectionModel =
                        (ListSelectionModel) e.getSource();
                RectangularDataSet dataSet = getDataSet();
                dataSet.clearSelection();

                if (!getRowSelectionAllowed()) {
                    for (int i = 0; i < dataSet.getNumColumns(); i++) {
                        if (selectionModel.isSelectedIndex(i + 2)) {
                            dataSet.setSelected(dataSet.getVariable(i), true);
                        }
                    }
                }
            }
        });

        setTransferHandler(new TabularDataTransferHandler());

        addFocusListener(new FocusAdapter() {
            @Override
			public void focusLost(FocusEvent e) {
                JTable table = (JTable) e.getSource();
                TableCellEditor editor = table.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }
            }
        });
    }


    public void setEditable(boolean editable){
        this.editable = editable;
    }

    @Override
	public void setValueAt(Object aValue, int row, int column) {
        try {
            super.setValueAt(aValue, row, column);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(JOptionUtils.centeringComp(),
                    e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
	public TableCellEditor getCellEditor(int row, int column) {
        if(!this.editable){
            return new DoNothingEditor();
        }
        if (row == 1) {
            return new VariableNameEditor();
        } else if (column == 1 && row >= 2) {
            return new MultiplierEditor();
        } else if (row > 1) {
            return new DataCellEditor();
        }

        return null;
    }

    @Override
	public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0) {
            return new RowNumberRenderer();
        } else if (column == 1 && row >= 1) {
            return new MultiplierRenderer();
        } else {
            if (row == 0 || row == 1) {
                return new VariableNameRenderer();
            }

            return new DataCellRenderer(this, getNumLeadingCols());
        }
    }

    /**
     * Returns the underlying DataSet model.
     *
     * @return this model.
     */
    public RectangularDataSet getDataSet() {
        TabularDataTable tableModelTabularData = (TabularDataTable) getModel();
        return tableModelTabularData.getDataSet();
    }


    public void setDataSet(RectangularDataSet data){
        TabularDataTable tableModelTabularData = (TabularDataTable) getModel();
        tableModelTabularData.setDataSet(data);
    }



    @Override
	public DataModel getDataModel() {
        return getDataSet();
    }

    public void deleteSelected() {
        TabularDataTable model = (TabularDataTable) getModel();
        RectangularDataSet dataSet = model.getDataSet();

        if (!getRowSelectionAllowed()) {
            int[] selectedCols = getSelectedColumns();
            TableCellEditor editor = getCellEditor();

            if (editor != null) {
                editor.stopCellEditing();
            }

            for (int i = 0; i < selectedCols.length; i++) {
                selectedCols[i] -= getNumLeadingCols();
            }

            dataSet.removeCols(selectedCols);
        } else if (!getColumnSelectionAllowed()) {
            int[] selectedRows = getSelectedRows();
            TableCellEditor editor = getCellEditor();

            if (editor != null) {
                editor.stopCellEditing();
            }

            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] -= 2;
            }

            dataSet.removeRows(selectedRows);
        } else {
            throw new IllegalStateException(
                    "Only row deletion and column " + "deltion supported.");
        }

        firePropertyChange("modelChanged", null, null);
        model.fireTableDataChanged();
    }

    public void clearSelected() {
        TabularDataTable model = (TabularDataTable) getModel();
        RectangularDataSet dataSet = model.getDataSet();

        if (!getRowSelectionAllowed()) {
            int[] selectedCols = getSelectedColumns();
            TableCellEditor editor = getCellEditor();

            if (editor != null) {
                editor.stopCellEditing();
            }

            for (int i = selectedCols.length - 1; i >= 0; i--) {
                if (selectedCols[i] < getNumLeadingCols()) {
                    continue;
                }

                int dataCol = selectedCols[i] - getNumLeadingCols();

                if (dataCol >= dataSet.getNumColumns()) {
                    continue;
                }

                Node variable = dataSet.getVariable(dataCol);
                Object missingValue =
                        ((Variable) variable).getMissingValueMarker();

                for (int j = 0; j < dataSet.getNumRows(); j++) {
                    dataSet.setObject(j, dataCol, missingValue);
                }
            }
        } else if (!getColumnSelectionAllowed()) {
            int[] selectedRows = getSelectedRows();

            TableCellEditor editor = getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }

            for (int i = getColumnCount() - 1; i >= 0; i--) {
                if (i < getNumLeadingCols()) {
                    continue;
                }

                String colName = (String) (getValueAt(1, i));

                if (colName == null) {
                    continue;
                }

                int dataCol = i - getNumLeadingCols();

                Node variable = dataSet.getVariable(dataCol);
                Object missingValue =
                        ((Variable) variable).getMissingValueMarker();

                for (int j = selectedRows.length - 1; j >= 0; j--) {
                    if (selectedRows[j] < 2) {
                        continue;
                    }

                    if (selectedRows[j] > dataSet.getNumRows() + 1) {
                        continue;
                    }

                    dataSet.setObject(selectedRows[j] - 2, dataCol,
                            missingValue);
                }
            }
        } else {
            int[] selectedRows = getSelectedRows();
            int[] selectedCols = getSelectedColumns();

            TableCellEditor editor = getCellEditor();

            if (editor != null) {
                editor.stopCellEditing();
            }

            for (int i = selectedCols.length - 1; i >= 0; i--) {
                if (selectedCols[i] < getNumLeadingCols()) {
                    continue;
                }

                int dataCol = selectedCols[i] - getNumLeadingCols();

                if (dataCol >= dataSet.getNumColumns()) {
                    continue;
                }

                String colName = (String) (getValueAt(1, selectedCols[i]));

                if (colName == null) {
                    continue;
                }

                Node variable = dataSet.getVariable(dataCol);
                Object missingValue =
                        ((Variable) variable).getMissingValueMarker();

                for (int j = selectedRows.length - 1; j >= 0; j--) {
                    if (selectedRows[j] < 2) {
                        continue;
                    }

                    if (selectedRows[j] > dataSet.getNumRows() + 1) {
                        continue;
                    }

                    dataSet.setObject(selectedRows[j] - 2, dataCol,
                            missingValue);
                }
            }
        }

        firePropertyChange("modelChanged", null, null);
        model.fireTableDataChanged();
    }

    private int getNumLeadingCols() {
        return numLeadingCols;
    }

    /**
     * Returns true iff the given token is a legitimate value for the cell at
     * (row, col) in the table.
     */
    public boolean checkValueAt(String token, int col) {
        if (col < getNumLeadingCols()) {
            throw new IllegalArgumentException();
        }

        RectangularDataSet dataSet = getDataSet();
        int dataCol = col - getNumLeadingCols();
//        int dataCol = col;

        if (dataCol < dataSet.getNumColumns()) {
            Node variable = dataSet.getVariable(dataCol);
            return ((Variable) variable).checkValue(token);
        } else {
            return true;
        }
    }

    public void setShowCategoryNames(boolean selected) {
        TabularDataTable table = (TabularDataTable) getModel();
        table.setCategoryNamesShown(selected);
    }

    public boolean isShowCategoryNames() {
        TabularDataTable table = (TabularDataTable) getModel();
        return table.isCategoryNamesShown();
    }

    @Override
	public void propertyChange(PropertyChangeEvent evt) {
        if ("modelChanged".equals(evt.getPropertyName())) {
            firePropertyChange("modelChanged", null, null);
        }
    }
                
}





class RowNumberRenderer extends DefaultTableCellRenderer {
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        if (row > 1) {
            setText(Integer.toString(row - 1));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
        }

        return label;
    }
}

class MultiplierRenderer extends DefaultTableCellRenderer {
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        if (value == null) {
            setText("");
        } else {
            setText(value.toString());
        }

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(Color.DARK_GRAY);

        return label;
    }
}





class MultiplierEditor extends DefaultCellEditor {
    private JTextField textField;

    /**
     * Constructs a new number cell editor.
     */
    public MultiplierEditor() {
        super(new JTextField());

        textField = (JTextField) editorComponent;
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setFont(new Font("SansSerif", Font.BOLD, 10));
        textField.setForeground(Color.DARK_GRAY);
        textField.setBorder(new LineBorder(Color.black));

        delegate = new EditorDelegate() {
            @Override
			public void setValue(Object value) {
                if (value instanceof Integer) {
                    textField.setText(value.toString());
                }

                textField.selectAll();
            }

            /**
             * Overrides delegate; gets the text value from the cell to send
             * back to the model.
             *
             * @return this text value.
             */
            @Override
			public Object getCellEditorValue() {
                return textField.getText();
            }
        };

        textField.addActionListener(delegate);
    }
}

class VariableNameRenderer extends DefaultTableCellRenderer {
    @Override
	public void setValue(Object value) {
        if (!(value instanceof String)) {
            value = "";
        }

        setText((String) value);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setHorizontalAlignment(SwingConstants.CENTER);
    }
}

class DoNothingEditor extends DefaultCellEditor {

    public DoNothingEditor() {
        super(new JTextField());
    }

     @Override
	public boolean isCellEditable(EventObject anEvent){
         return false;
     }
}



class VariableNameEditor extends DefaultCellEditor {
    private JTextField textField;

    /**
     * Constructs a new number cell editor.
     */
    public VariableNameEditor() {
        super(new JTextField());

        textField = (JTextField) editorComponent;

        delegate = new EditorDelegate() {

            /**
             * Overrides delegate; sets the value of the textfield to the value
             * of the datum.
             *
             * @param value this value.
             */
            @Override
			public void setValue(Object value) {
                if (!(value instanceof String)) {
                    value = "";
                }

                textField.setText((String) value);
                textField.setFont(new Font("SansSerif", Font.BOLD, 12));
                textField.setHorizontalAlignment(SwingConstants.CENTER);
                textField.selectAll();
            }

            /**
             * Overrides delegate; gets the text value from the cell to send
             * back to the model.
             *
             * @return this text value.
             */
            @Override
			public Object getCellEditorValue() {
                return textField.getText();
            }
        };

        textField.addActionListener(delegate);
    }
}

class DataCellRenderer extends DefaultTableCellRenderer {
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
    //    private NumberFormat nf = new DecimalFormat("0.0000E0");
    private RectangularDataSet dataSet;
    private int numLeadingCols;

    public DataCellRenderer(TabularDataJTable tableTabular,
                            int numLeadingCols) {
        this.dataSet =
                ((TabularDataTable) tableTabular.getModel()).getDataSet();
        this.numLeadingCols = numLeadingCols;
    }

    @Override
	public void setValue(Object value) {
        if (value instanceof String) {
            setText((String) value);
        } else if (value instanceof Integer) {
            setText(value.toString());
        } else if (value instanceof Double) {
            double doubleValue = (Double) value;
            setText(nf.format(doubleValue));
        } else {
            setText("");
        }
    }

    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int col) {

        // Have to set the alignment here, since this is the only place the col
        // index of the component is available...
        Component c = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, col);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) c;

        if (dataSet.getNumColumns() > 0 && col >= getNumLeadingCols() &&
                col < dataSet.getNumColumns() + getNumLeadingCols()) {
            int dataCol = col - getNumLeadingCols();
            Node variable = dataSet.getVariable(dataCol);

            if (variable instanceof DiscreteVariable) {
                renderer.setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                renderer.setHorizontalAlignment(SwingConstants.RIGHT);
            }
        }

        return renderer;
    }

    private int getNumLeadingCols() {
        return numLeadingCols;
    }
}





