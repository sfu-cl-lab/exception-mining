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

import edu.cmu.tetrad.model.Params;
import edu.cmu.tetradapp.model.GraphParams;
import edu.cmu.tetradapp.util.IntTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Edits the parameters for generating random graphs.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5052 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public class GraphParamsEditor extends JPanel implements ParameterEditor {
    private GraphParams params;

    /**
     * Constructs a dialog to edit the given workbench randomization
     * parameters.
     */
    public GraphParamsEditor() {
    }

    @Override
	public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (GraphParams) params;
    }

    @Override
	public void setParentModels(Object[] parentModels) {
        // Do nothing.
    }

    @Override
	public void setup() {
        // set up text and ties them to the parameters object being edited.
        final IntTextField numNodesField =
                new IntTextField(getParams().getNumNodes(), 4);
        final IntTextField numLatentsField =
                new IntTextField(getParams().getNumLatents(), 4);
        final IntTextField maxEdgesField =
                new IntTextField(getParams().getMaxEdges(), 4);
        final IntTextField maxIndegreeField =
                new IntTextField(getParams().getMaxIndegree(), 4);
        final IntTextField maxOutdegreeField =
                new IntTextField(getParams().getMaxOutdegree(), 4);
        final IntTextField maxDegreeField =
                new IntTextField(getParams().getMaxDegree(), 4);
        final JComboBox connectedBox = new JComboBox(new String[]{"No", "Yes"});


        numNodesField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                try {
                    getParams().setNumNodes(value);
                    maxEdgesField.setValue(getParams().getMaxEdges());
                    return getParams().getNumNodes();
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        numLatentsField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                try {
                    getParams().setNumLatents(value);
                    maxEdgesField.setValue(getParams().getMaxEdges());
                    return getParams().getNumLatents();
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });

        maxEdgesField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                try {
                    getParams().setMaxEdges(value);
                    return getParams().getMaxEdges();
                }
                catch (Exception e) {
                    return oldValue;
                }
            }
        });


        maxIndegreeField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                getParams().setMaxIndegree(value);
                maxOutdegreeField.setValue(getParams().getMaxOutdegree());
                maxDegreeField.setValue(getParams().getMaxDegree());
                return getParams().getMaxIndegree();
            }
        });


        maxOutdegreeField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                getParams().setMaxOutdegree(value);
                maxIndegreeField.setValue(getParams().getMaxIndegree());
                maxDegreeField.setValue(getParams().getMaxDegree());
                return getParams().getMaxOutdegree();
            }
        });


        maxDegreeField.setFilter(new IntTextField.Filter() {
            @Override
			public int filter(int value, int oldValue) {
                getParams().setMaxDegree(value);
                maxIndegreeField.setValue(getParams().getMaxIndegree());
                maxOutdegreeField.setValue(getParams().getMaxOutdegree());
                return getParams().getMaxDegree();
            }
        });

        connectedBox.setMaximumSize(connectedBox.getPreferredSize());

        if (getParams().isConnected()) {
            connectedBox.setSelectedItem("Yes");
        }
        else {
            connectedBox.setSelectedItem("No");
        }

        connectedBox.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                if ("Yes".equals(box.getSelectedItem())) {
                    getParams().setConnected(true);
                }
                else if ("No".equals(box.getSelectedItem())) {
                    getParams().setConnected(false);
                }
                else {
                    throw new IllegalArgumentException();
                }

                maxIndegreeField.setValue(getParams().getMaxIndegree());
                maxOutdegreeField.setValue(getParams().getMaxOutdegree());
                maxDegreeField.setValue(getParams().getMaxDegree());
                maxEdgesField.setValue(getParams().getMaxEdges());
            }
        });

        // construct the workbench.
        setLayout(new BorderLayout());

        JRadioButton manual = new JRadioButton(
                "An empty graph (to be constructed manually).");
        JRadioButton random = new JRadioButton("A random DAG.");
        ButtonGroup group = new ButtonGroup();
        group.add(manual);
        group.add(random);

        if (getParams().getInitializationMode() == GraphParams.MANUAL) {
            manual.setSelected(true);
            numNodesField.setEnabled(false);
            maxEdgesField.setEnabled(false);
            numLatentsField.setEnabled(false);
            maxDegreeField.setEnabled(false);
            maxIndegreeField.setEnabled(false);
            maxOutdegreeField.setEnabled(false);
            connectedBox.setEnabled(false);
        }
        else {
            random.setSelected(true);
            numNodesField.setEnabled(true);
            maxEdgesField.setEnabled(true);
            numLatentsField.setEnabled(true);
            maxDegreeField.setEnabled(true);
            maxIndegreeField.setEnabled(true);
            maxOutdegreeField.setEnabled(true);
            connectedBox.setEnabled(true);
        }

        manual.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setInitializationMode(GraphParams.MANUAL);
                numNodesField.setEnabled(false);
                maxEdgesField.setEnabled(false);
                numLatentsField.setEnabled(false);
                maxDegreeField.setEnabled(false);
                maxIndegreeField.setEnabled(false);
                maxOutdegreeField.setEnabled(false);
                connectedBox.setEnabled(false);
            }
        });

        random.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setInitializationMode(GraphParams.RANDOM);
                numNodesField.setEnabled(true);
                maxEdgesField.setEnabled(true);
                numLatentsField.setEnabled(true);
                maxDegreeField.setEnabled(true);
                maxIndegreeField.setEnabled(true);
                maxOutdegreeField.setEnabled(true);
                connectedBox.setEnabled(true);
            }
        });

        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel("Make new graph:"));
        b2.add(Box.createHorizontalGlue());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));

        Box b8 = Box.createHorizontalBox();
        b8.add(manual);
        b8.add(Box.createHorizontalGlue());

        b1.add(b8);
        Box b9 = Box.createHorizontalBox();
        b9.add(random);
        b9.add(Box.createHorizontalGlue());
        b1.add(b9);

        Box b10 = Box.createHorizontalBox();
        b10.add(Box.createHorizontalStrut(25));
        b10.add(new JLabel("Number of nodes:"));
        b10.add(Box.createRigidArea(new Dimension(10, 0)));
        b10.add(Box.createHorizontalGlue());
        b10.add(numNodesField);
        b1.add(b10);

        Box b11 = Box.createHorizontalBox();
        b11.add(Box.createHorizontalStrut(25));
        b11.add(new JLabel("Number of latent nodes:"));
        b11.add(Box.createHorizontalStrut(25));
        b11.add(Box.createHorizontalGlue());
        b11.add(numLatentsField);
        b1.add(b11);
        b1.add(Box.createVerticalStrut(5));

        Box b12 = Box.createHorizontalBox();
        b12.add(Box.createHorizontalStrut(25));
        b12.add(new JLabel("Maximum number of edges:"));
        b12.add(Box.createHorizontalGlue());
        b12.add(maxEdgesField);
        b1.add(b12);

        Box b14 = Box.createHorizontalBox();
        b14.add(Box.createHorizontalStrut(25));
        b14.add(new JLabel("Maximum indegree:"));
        b14.add(Box.createHorizontalGlue());
        b14.add(maxIndegreeField);
        b1.add(b14);

        Box b15 = Box.createHorizontalBox();
        b15.add(Box.createHorizontalStrut(25));
        b15.add(new JLabel("Maximum outdegree:"));
        b15.add(Box.createHorizontalGlue());
        b15.add(maxOutdegreeField);
        b1.add(b15);

        Box b13 = Box.createHorizontalBox();
        b13.add(Box.createHorizontalStrut(25));
        b13.add(new JLabel("Maximum degree:"));
        b13.add(Box.createHorizontalGlue());
        b13.add(maxDegreeField);
        b1.add(b13);
        b1.add(Box.createVerticalStrut(5));

        Box b16 = Box.createHorizontalBox();
        b16.add(Box.createHorizontalStrut(25));
        b16.add(new JLabel("Connected:"));
        b16.add(Box.createHorizontalGlue());
        b16.add(connectedBox);
        b1.add(b16);

        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

    @Override
	public boolean mustBeShown() {
        return false;
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    private synchronized GraphParams getParams() {
        return this.params;
    }
}


