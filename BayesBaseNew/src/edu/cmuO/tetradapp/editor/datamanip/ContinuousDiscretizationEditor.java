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

package edu.cmu.tetradapp.editor.datamanip;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.util.IntSpinner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Edits discretization parameters for a continuous variable.
 *
 * @author Joseph Ramsey
 * @author Tyler Gibson
 * @version $Revision: 6039 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
class ContinuousDiscretizationEditor extends JPanel {


    /**
     * States which method should be used to discretize matters by default.
     */
    public enum Method {
        EQUAL_SIZE_BUCKETS, EVENLY_DIVIDED_INTERNVALS;
    }


    /**
     * The variable to be discretized.
     */
    private ContinuousVariable variable;


    /**
     * The decimal format to use.
     */
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * The min value of the data.
     */
    private double min;

    /**
     * The max value of the data.
     */
    private double max;

    /**
     * The panel that contains the range editor.
     */
    private JPanel rangeEditorPanel;


    /**
     * The selection buttons box.
     */
    private Box selectionButtonsBox;


    /**
     * An editor that allows a user to edit the ranges of each category
     */
    private RangeEditor rangeEditor;

    /**
     * A spinner used to select the number of categories.
     */
    private IntSpinner categorySpinner;


    /**
     * The default number of categories to use.
     */
    private int numberOfCategories = 3;


    /**
     * The data that is being discretized.
     */
    private double[] data;


    /**
     * The method being used to discretize the data.
     */
    private Method method;


    /**
     * Constructs an editor that allows the user to discretize continuous variables. There are two
     * options for the default discretization <code>Method.EQUAL_SIZE_BUCKETS</code> which will
     * discretize the continuous data into categories by trying to fit an equal number of values in
     * each category. On the other hand <code>Method.EVENLY_DIVIDED_INTERNVALS</code> will just
     * spent the interval up in equal segments which may or may not include an even distribution of
     * values.
     *
     * @param dataSet       The dataset containing the data for the variable.
     * @param variable      The variable to be edited.
     * @param numCategories The number of categories for the variable.
     * @param method        - The method used to find a default discretization.
     */
    public ContinuousDiscretizationEditor(RectangularDataSet dataSet,
                                          ContinuousVariable variable,
                                          int numCategories, Method method) {
        if (method == null) {
            throw new NullPointerException();
        }

        if (variable == null) {
            throw new NullPointerException();
        }

        if (numCategories < 2) {
            throw new IllegalArgumentException(
                    "Number of categories must be >= 2.");
        }

        this.method = method;
        this.variable = variable;
        this.data = new double[dataSet.getNumRows()];
        int col = dataSet.getColumn(variable);

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            this.data[i] = dataSet.getDouble(i, col);
        }

        this.min = Descriptive.min(new DoubleArrayList(data));
        this.max = Descriptive.max(new DoubleArrayList(data));
        this.numberOfCategories = numCategories;

        this.categorySpinner = new IntSpinner(numCategories, 1, 2);
        categorySpinner.setMin(2);
        categorySpinner.setMaximumSize(categorySpinner.getPreferredSize());
        categorySpinner.setFilter(new MyFilter());
        categorySpinner.addChangeListener(new ChangeListener() {
            @Override
			public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                if (!spinner.getValue().equals(numberOfCategories)) {
                    setNumCategories((Integer) spinner.getValue());
                }
            }
        });


        rangeEditorPanel = new JPanel();
        rangeEditorPanel.setLayout(new BorderLayout());
        setNumCategories(numCategories);

        Box b1 = Box.createVerticalBox();
        b1.add(Box.createVerticalStrut(10));

        Box b3 = Box.createHorizontalBox();
        b3.add(Box.createRigidArea(new Dimension(10, 0)));
        String label = "Min = " + nf.format(min) + " , Max = " + nf.format(max);
        b3.add(new JLabel(label));
        b3.add(Box.createHorizontalGlue());
        b1.add(b3);

        b1.add(Box.createVerticalStrut(5));
        this.selectionButtonsBox = Box.createHorizontalBox();
        this.buildSelectionBox();
        b1.add(this.selectionButtonsBox);
        b1.add(Box.createVerticalStrut(5));

        Box b5 = Box.createHorizontalBox();
        b5.add(Box.createRigidArea(new Dimension(10, 0)));
        b5.add(new JLabel("Use "));
        b5.add(categorySpinner);
        b5.add(new JLabel(" categories to discretize."));
        b5.add(Box.createHorizontalGlue());
        b1.add(b5);

        b1.add(Box.createVerticalStrut(10));

        Box b6 = Box.createHorizontalBox();
        b6.add(Box.createRigidArea(new Dimension(10, 0)));
        b6.add(new JLabel("Edit category names and breakpoints:"));
        b6.add(Box.createHorizontalGlue());
        b1.add(b6);

        b1.add(Box.createVerticalStrut(10));

        b1.add(rangeEditorPanel);
        b1.add(Box.createVerticalGlue());

        setLayout(new BorderLayout());
        add(b1, BorderLayout.CENTER);
    }

    //================================PUBLIC METHODS=======================//


    /**
     * Returns the number of categories.
     *
     * @return - number of categories.
     */
    public int getNumCategories() {
        return this.numberOfCategories;
    }


    /**
     * Returns the variable that is being discretized.
     *
     * @return - variable being discretized.
     */
    public ContinuousVariable getVariable() {
        return this.variable;
    }


    /**
     * Returns the current discretization spec created by the user.
     *
     * @return <code>ContinuousDiscretizationSpec</code>.
     */
    public ContinuousDiscretizationSpec getDiscretizationSpec() {
        ContinuousDiscretizationSpec spec = this.rangeEditor.getDiscretizationSpec();
        if (method == Method.EQUAL_SIZE_BUCKETS) {
            spec.setMethod(ContinuousDiscretizationSpec.EVENLY_DISTRIBUTED_VALUES);
        } else if (method == Method.EVENLY_DIVIDED_INTERNVALS) {
            spec.setMethod(ContinuousDiscretizationSpec.EVENLY_DISTRIBUTED_INTERVALS);
        }
        return spec;
    }


    /**
     * Changes the method.
     *
     * @param method
     */
    public void setMethod(Method method) {
        this.method = method;
        this.buildSelectionBox();
        this.setNumCategories(this.numberOfCategories);
    }


    /**
     * Returns the method.
     *
     * @return
     */
    public Method getMethod(){
        return this.method;
    }


    /**
     * Sets the discretization spec that should be used by the editor.
     *
     * @param spec
     */
    public void setDiscretizationSpec(ContinuousDiscretizationSpec spec) {
        rangeEditorPanel.removeAll();
        if (spec.getMethod() == ContinuousDiscretizationSpec.EVENLY_DISTRIBUTED_INTERVALS) {
            this.method = Method.EVENLY_DIVIDED_INTERNVALS;
        } else if (spec.getMethod() == ContinuousDiscretizationSpec.EVENLY_DISTRIBUTED_VALUES) {
            this.method = Method.EQUAL_SIZE_BUCKETS;
        }
        this.buildSelectionBox();
        rangeEditor = createRangeEditor(spec);
        this.numberOfCategories = spec.getCategories().size();
        this.categorySpinner.setValue(this.numberOfCategories);
        rangeEditorPanel.add(rangeEditor, BorderLayout.CENTER);
        rangeEditorPanel.revalidate();
        rangeEditorPanel.repaint();
    }


    /**
     * Sets the number of categories to use.
     *
     * @param numCategories
     */
    public void setNumCategories(int numCategories) {
        if (numCategories < 2) {
            throw new IllegalArgumentException();
        }
        this.numberOfCategories = numCategories;
        rangeEditorPanel.removeAll();
        ContinuousDiscretizationSpec defaultDiscretizationSpec;
        if (this.method == Method.EVENLY_DIVIDED_INTERNVALS) {
            defaultDiscretizationSpec = getDefaultDiscretizationSpec(numCategories);
        } else if (this.method == Method.EQUAL_SIZE_BUCKETS) {
            defaultDiscretizationSpec = getEqualFreqDiscretizationSpec(numCategories);
        } else {
            throw new IllegalStateException("Unknown method " + method);
        }
        rangeEditor = createRangeEditor(defaultDiscretizationSpec);
        rangeEditorPanel.add(rangeEditor, BorderLayout.CENTER);
        rangeEditorPanel.revalidate();
        rangeEditorPanel.repaint();

        this.categorySpinner.setValue(numCategories);

        Preferences.userRoot().putInt("latestNumCategories", numCategories);
    }

    //==============================PRIVATE METHODS=======================//


    private void buildSelectionBox() {
        this.selectionButtonsBox.removeAll();
        Box vBox = Box.createVerticalBox();

        vBox.add(new JLabel("Discretization Method: "));

        JRadioButton equalInterval = new JRadioButton("Evenly Distributed Intervals",
                method == Method.EVENLY_DIVIDED_INTERNVALS);
        JRadioButton equalBuckets = new JRadioButton("Evenly Distributed Values",
                method == Method.EQUAL_SIZE_BUCKETS);
        equalInterval.setHorizontalTextPosition(SwingConstants.RIGHT);
        equalBuckets.setHorizontalTextPosition(SwingConstants.RIGHT);

        equalInterval.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                method = Method.EVENLY_DIVIDED_INTERNVALS;
                setNumCategories(numberOfCategories);
            }
        });

        equalBuckets.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                method = Method.EQUAL_SIZE_BUCKETS;
                setNumCategories(numberOfCategories);
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(equalInterval);
        group.add(equalBuckets);

        vBox.add(equalInterval);
        vBox.add(equalBuckets);

        this.selectionButtonsBox.add(Box.createHorizontalStrut(10));
        this.selectionButtonsBox.add(vBox);
        this.selectionButtonsBox.add(Box.createHorizontalGlue());
    }


    private RangeEditor createRangeEditor(
            ContinuousDiscretizationSpec discretizationSpec) {
        return new RangeEditor(variable, discretizationSpec, true);
    }

    /**
     * Calculates the equal freq discretization spec
     */
    private ContinuousDiscretizationSpec getEqualFreqDiscretizationSpec(int numCategories) {
        double[] breakpoints = Discretization.getEqualFrequencyBreakPoints(this.data, numCategories);
        List<String> cats = defaultCategories(numCategories);
        return new ContinuousDiscretizationSpec(breakpoints, cats);
    }


    /**
     * Calculates the default discretization spec.
     */
    private ContinuousDiscretizationSpec getDefaultDiscretizationSpec(
            int numCategories) {
        double[] breakpoints =
                defaultBreakpoints(max, min, numCategories);
        List<String> categories = defaultCategories(numCategories);
        return new ContinuousDiscretizationSpec(breakpoints, categories);
    }

    /**
     * Calcultes the default break points.
     */
    private static double[] defaultBreakpoints(double max, double min,
                                               int numCategories) {
        double interval = (max - min) / numCategories;
        double[] breakpoints = new double[numCategories - 1];
        for (int i = 0; i < breakpoints.length; i++) {
            breakpoints[i] = min + (i + 1) * interval;
        }
        return breakpoints;
    }


    private static List<String> defaultCategories(int numCategories) {
        List<String> categories = new LinkedList<String>();
        for (int i = 0; i < numCategories; i++) {
            categories.add(DataUtils.defaultCategory(i));
        }
        return categories;
    }

    //=========================== Inner class ====================================//


    private static class MyFilter implements IntSpinner.Filter {


        @Override
		public int filter(int oldValue, int newValue) {
            if (newValue < 2) {
                newValue = oldValue;
            }
            return newValue;
        }

    }
}


