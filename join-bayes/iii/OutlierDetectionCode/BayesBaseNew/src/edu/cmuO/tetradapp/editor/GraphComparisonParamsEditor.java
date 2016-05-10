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
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetradapp.model.GraphComparisonParams;
import edu.cmu.tetradapp.model.GraphSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Edits the parameters for generating random graphs.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public class GraphComparisonParamsEditor extends JPanel implements ParameterEditor {

    /**
     * The parameters object being edited.
     */
    private GraphComparisonParams params = null;

    /**
     * The first graph source.
     */
    private SessionModel model1;

    /**
     * The second graph source.
     */
    private SessionModel model2;
    private Object[] parentModels;

    /**
     *
     */
    public GraphComparisonParamsEditor() {
    }

    @Override
	public void setParams(Params params) {
        if (params == null) {
            throw new NullPointerException();
        }

        this.params = (GraphComparisonParams) params;
    }

    @Override
	public void setParentModels(Object[] parentModels) {
        this.parentModels = parentModels;
    }

    @Override
	public boolean mustBeShown() {
        return false; 
    }

    @Override
	public void setup() {
        List graphSources = new LinkedList();

        for (Object parentModel : parentModels) {
            if (parentModel instanceof GraphSource) {
                graphSources.add(parentModel);
            }
        }

        if (graphSources.size() != 2) {
            throw new IllegalArgumentException(
                    "Expecting exactly two graph " + "sources.");
        }

        model1 = (SessionModel) graphSources.get(0);
        model2 = (SessionModel) graphSources.get(1);

        setLayout(new BorderLayout());

        // Reset?
        JRadioButton resetOnExecute = new JRadioButton("Reset");
        JRadioButton dontResetOnExecute = new JRadioButton("Appended to");
        ButtonGroup group1 = new ButtonGroup();
        group1.add(resetOnExecute);
        group1.add(dontResetOnExecute);

        resetOnExecute.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setResetTableOnExecute(true);
            }
        });

        dontResetOnExecute.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setResetTableOnExecute(false);
            }
        });

        if (getParams().isResetTableOnExecute()) {
            resetOnExecute.setSelected(true);
        }
        else {
            dontResetOnExecute.setSelected(true);
        }

        // Latents?
        JRadioButton latents = new JRadioButton("Yes");
        JRadioButton noLatents = new JRadioButton("No");
        ButtonGroup group2 = new ButtonGroup();
        group2.add(latents);
        group2.add(noLatents);

        latents.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setKeepLatents(true);
            }
        });

        latents.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setKeepLatents(false);
            }
        });

        if (getParams().isKeepLatents()) {
            latents.setSelected(true);
        }
        else {
            noLatents.setSelected(true);
        }

        // True graph?
        JRadioButton graph1 = new JRadioButton(model1.getName());
        JRadioButton graph2 = new JRadioButton(model2.getName());
        ButtonGroup group3 = new ButtonGroup();
        group3.add(graph1);
        group3.add(graph2);

        graph1.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setReferenceGraphName(model1.getName());
            }
        });

        graph2.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                getParams().setReferenceGraphName(model2.getName());
            }
        });

        if (getParams().getReferenceGraphName() == null) {
            getParams().setReferenceGraphName(model1.getName());
            graph1.setSelected(true);
        }
        else {
            if (getParams().getReferenceGraphName().equals(model1.getName())) {
                graph1.setSelected(true);
            }
            else
            if (getParams().getReferenceGraphName().equals(model2.getName())) {
                graph2.setSelected(true);
            }
        }

        // continue workbench construction.
        Box b1 = Box.createVerticalBox();

        Box b2 = Box.createHorizontalBox();
        b2.add(new JLabel(
                "Should the counts table be reset or appended to with each " +
                        "simulation?"));
        b2.add(Box.createHorizontalGlue());
        b1.add(b2);
        b1.add(Box.createVerticalStrut(5));

        Box b3 = Box.createHorizontalBox();
        b3.add(resetOnExecute);
        b3.add(Box.createHorizontalGlue());
        b1.add(b3);

        Box b4 = Box.createHorizontalBox();
        b4.add(dontResetOnExecute);
        b4.add(Box.createHorizontalGlue());
        b1.add(b4);
        b1.add(Box.createVerticalStrut(20));

        Box b5 = Box.createHorizontalBox();
        b5.add(new JLabel(
                "Will the results graph contain latents? (Requires a different algorithm.)"));
        b5.add(Box.createHorizontalGlue());
        b1.add(b5);

        Box b6 = Box.createHorizontalBox();
        b6.add(latents);
        b6.add(Box.createHorizontalGlue());
        b1.add(b6);

        Box b7 = Box.createHorizontalBox();
        b7.add(noLatents);
        b7.add(Box.createHorizontalGlue());
        b1.add(b7);
        b1.add(Box.createVerticalStrut(20));

        Box b8 = Box.createHorizontalBox();
        b8.add(new JLabel("Which of the two input graphs is the true graph?"));
        b8.add(Box.createHorizontalGlue());
        b1.add(b8);

        Box b9 = Box.createHorizontalBox();
        b9.add(graph1);
        b9.add(Box.createHorizontalGlue());
        b1.add(b9);

        Box b10 = Box.createHorizontalBox();
        b10.add(graph2);
        b10.add(Box.createHorizontalGlue());
        b1.add(b10);

        b1.add(Box.createHorizontalGlue());
        add(b1, BorderLayout.CENTER);
    }

    /**
     * Returns the getMappings object being edited. (This probably should not be
     * public, but it is needed so that the textfields can edit the model.)
     *
     * @return the stored simulation parameters model.
     */
    private synchronized GraphComparisonParams getParams() {
        return this.params;
    }
}


