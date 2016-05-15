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

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.search.LogisticRegressionResult;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetradapp.model.LogisticRegressionParams;
import edu.cmu.tetradapp.model.LogisticRegressionRunner;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

/**
 * Allows the user to execute a logistic regression in the GUI. Contains a panel
 * that lets the user specify a target variable (which must be binary) and a
 * list of continuous regressors, plus a tabbed pane that includes (a) a text
 * area to show the report of the logistic regression and (b) a graph workbench
 * to show the graph of the target with significant regressors from the
 * regression as parents.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @author Frank Wimberly - adapted for EM Bayes estimator and Strucural EM
 *         Bayes estimator
 * @version $Revision: 6039 $ $Date: 2006-01-05 13:54:07 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class LogisticRegressionEditor extends JPanel {

    /**
     * Text area for display output.
     */
    private JTextArea modelParameters;


    /**
     * The number formatter used for printing reports.
     */
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();


    public LogisticRegressionEditor(LogisticRegressionRunner regressionRunner) {
        final LogisticRegressionRunner regRunner = regressionRunner;
        final GraphWorkbench workbench = new GraphWorkbench();
        this.modelParameters = new JTextArea();
        final JButton executeButton = new JButton("Execute");

        //this.modelParameters.setFont(new Font("Monospaced", Font.PLAIN, 12));

        executeButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                regRunner.execute();
              //  modelParameters.setText(regRunner.getReport());
                print(regRunner.getResult(), regRunner.getAlpha());
                Graph outGraph = regRunner.getOutGraph();
                GraphUtils.arrangeInCircle(outGraph, 200, 200, 150);
                GraphUtils.fruchtermanReingoldLayout(outGraph);
                workbench.setGraph(outGraph);
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(600, 400));
        tabbedPane.add("Model", new JScrollPane(this.modelParameters));
        tabbedPane.add("Output Graph", new JScrollPane(workbench));

        LogisticRegressionParams params =
                (LogisticRegressionParams) regRunner.getParams();
        RegressionParamsEditorPanel paramsPanel = new RegressionParamsEditorPanel(params, regRunner.getDataSet());

        Box b = Box.createVerticalBox();
        Box b1 = Box.createHorizontalBox();
        b1.add(paramsPanel);
        b1.add(Box.createHorizontalStrut(5));
        b1.add(tabbedPane);
        b.add(b1);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(executeButton);
        b.add(buttonPanel);

        setLayout(new BorderLayout());
        add(b, BorderLayout.CENTER);
    }

    /**
     * Sets the name of this editor.
     */
    @Override
	public void setName(String name) {
        String oldName = getName();
        super.setName(name);
        this.firePropertyChange("name", oldName, getName());
    }


    //============================== Private Methods =====================================//

    /**
     * Prints the info in the result to the text area (doesn't use the results representation).
     */
    private void print(LogisticRegressionResult result, double alpha){
        if(result == null){
            return;
        }
        // print cases
        String text = result.getNy0() + " cases have " + result.getTarget()  + " = 0; ";
        text += result.getNy1() + " cases have " + result.getTarget() + " = 1.\n\n";
        // print avgs/SD
        text += "Var\tAvg\tSD\n";
        for(int i = 1; i<=result.getNumRegressors(); i++){
            text += result.getVariableNames()[i-1] + "\t";
            text += nf.format(result.getxMeans()[i]) + "\t";
            text += nf.format(result.getxStdDevs()[i]) + "\n";
        }
        text += "\nCoefficients and Standard Errors:\n";
        text += "Var\tCoeff.\tStdErr\tProb.\tSig.\n";
        for(int i = 1; i<=result.getNumRegressors(); i++){
            text += result.getVariableNames()[i-1] + "\t";
            text += nf.format(result.getCoefs()[i]) + "\t";
            text += nf.format(result.getStdErrs()[i]) + "\t";
            text += nf.format(result.getProbs()[i]) + "\t";
            if(result.getProbs()[i] < alpha){
                text += "*\n";
            } else {
                text += "\n";
            }
        }

        text+= "\n\nIntercept = " + nf.format(result.getIntercept()) + "\n";
        
        this.modelParameters.setText(text);
    }



}

