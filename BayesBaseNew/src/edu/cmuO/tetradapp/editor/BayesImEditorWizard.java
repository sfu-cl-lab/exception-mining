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

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.util.SortingComboBox;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Allows the user to choose a variable in a Bayes net and edit the parameters
 * associated with that variable.  Parameters are of the form
 * P(Node=value1|Parent1=value2, Parent2=value2,...); values for these
 * parameters are probabilities ranging from 0.0 to 1.0. For a given combination
 * of parent values for node N, the probabilities for the values of N
 * conditional on that combination of parent values must sum to 1.0
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4926 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public final class BayesImEditorWizard extends JPanel {
    private BayesIm bayesIm;
    private JComboBox varNamesComboBox;
    private GraphWorkbench workbench;
    private BayesImNodeEditingTable editingTable;
    private JPanel tablePanel;

    public BayesImEditorWizard(BayesIm bayesIm, GraphWorkbench workbench) {
        if (bayesIm == null) {
            throw new NullPointerException();
        }

        if (workbench == null) {
            throw new NullPointerException();
        }

        workbench.setAllowDoubleClickActions(false);
        setBorder(new MatteBorder(10, 10, 10, 10, getBackground()));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setFont(new Font("SanSerif", Font.BOLD, 12));

        // Set up components.
        this.varNamesComboBox = createVarNamesComboBox(bayesIm);
        workbench.scrollWorkbenchToNode(
                (Node) (varNamesComboBox.getSelectedItem()));

        JButton nextButton = new JButton("Next");
        nextButton.setMnemonic('N');

        Node node = (Node) (varNamesComboBox.getSelectedItem());
        editingTable = new BayesImNodeEditingTable(node, bayesIm);
        editingTable.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {
                    firePropertyChange("modelChanged", null, null);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(editingTable);
        scroll.setPreferredSize(new Dimension(0, 150));
        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(scroll, BorderLayout.CENTER);
        editingTable.grabFocus();

        // Do layout.
        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("1. Choose the next variable to edit:  "));
        b1.add(varNamesComboBox);
        b1.add(nextButton);
        b1.add(Box.createHorizontalGlue());

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("2. Scroll to a row (that is, combination of " +
                "parent values) in the table below."));
        b2.add(Box.createHorizontalGlue());

        Box b3 = Box.createHorizontalBox();
        b3.add(new JLabel("3. Click in the appropriate box and assign a probability"
                + " to each value of the chosen"));
        b3.add(Box.createHorizontalGlue());

        Box b3a = Box.createHorizontalBox();
        b3a.add(new JLabel("    variable in that row."));
        b3a.add(Box.createHorizontalGlue());

        Box b4 = Box.createHorizontalBox();
        b4.add(tablePanel, BorderLayout.CENTER);

        Box b5 = Box.createHorizontalBox();
        b5.add(new JLabel("Right click in table to randomize."));
        b5.add(Box.createHorizontalGlue());

        add(b1);
        add(Box.createVerticalStrut(1));
        add(b2);
        add(Box.createVerticalStrut(5));
        add(b3);
        add(b3a);
        add(b4);
        add(b5);

        // Add listeners.
        varNamesComboBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                Node node = (Node) (varNamesComboBox.getSelectedItem());
                getWorkbench().scrollWorkbenchToNode(node);
                setCurrentNode(node);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                int current = varNamesComboBox.getSelectedIndex();
                int max = varNamesComboBox.getItemCount();

                ++current;

                if (current == max) {
                    JOptionPane.showMessageDialog(BayesImEditorWizard.this,
                            "There are no more variables.");
                }

                int set = (current < max) ? current : 0;

                varNamesComboBox.setSelectedIndex(set);
            }
        });

        workbench.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("selectedNodes")) {
                    List selection = (List) (e.getNewValue());

                    if (selection.size() == 1) {
                        Node node = (Node) (selection.get(0));
                        varNamesComboBox.setSelectedItem(node);
                        
                    }
                }
            }
        });

        this.bayesIm = bayesIm;
        this.workbench = workbench;
    }

    private JComboBox createVarNamesComboBox(BayesIm bayesIm) {
        JComboBox varNamesComboBox = new SortingComboBox() {
            @Override
			public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };

        varNamesComboBox.setBackground(Color.white);

        Graph graph = bayesIm.getBayesPm().getDag();

        for (Object o : graph.getNodes()) {
            varNamesComboBox.addItem(o);
        }

        if (graph.getNodes().size() > 0) {
            varNamesComboBox.setSelectedIndex(0);
        }

        return varNamesComboBox;
    }

    /**
     * Sets the current display to reflect the stored values of the current
     * node.
     */
    private void setCurrentNode(Node node) {
        TableCellEditor cellEditor = editingTable.getCellEditor();

        if (cellEditor != null) {
            cellEditor.cancelCellEditing();
        }

        editingTable = new BayesImNodeEditingTable(node, getBayesIm());
        editingTable.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                if ("modelChanged".equals(evt.getPropertyName())) {
                    firePropertyChange("modelChanged", null, null);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(editingTable);
        scroll.setPreferredSize(new Dimension(0, 150));

        tablePanel.removeAll();
        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();

        editingTable.grabFocus();
    }

    public BayesIm getBayesIm() {
        return bayesIm;
    }

    private GraphWorkbench getWorkbench() {
        return workbench;
    }
}



