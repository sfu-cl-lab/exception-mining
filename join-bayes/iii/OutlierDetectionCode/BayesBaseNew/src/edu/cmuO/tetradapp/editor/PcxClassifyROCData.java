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

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.search.PcxRocScorer;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.RocCalculator;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * @author Frank Wimberly
 * @version $Revision: 6039 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
class PcxClassifyROCData {
    public static void main(String[] args) {

        try {
            //Test with discrete data.
            String filenameD1 = args[0];
            String filenameD2 = args[1];

            File fileD1 = new File(filenameD1);
            File fileD2 = new File(filenameD2);

            double alpha = Double.parseDouble(args[2]);

            String targetVariableString = args[3];

            int depth = Integer.parseInt(args[4]);

            DataParser parser = new DataParser();
            RectangularDataSet dds1 = parser.parseTabular(fileD1);

            parser.setKnownVariables(dds1.getVariables());
            RectangularDataSet columnDataSetD2 = parser.parseTabular(fileD2);

            int targetValue = 0;
            PcxRocScorer pcxc = new PcxRocScorer(dds1, columnDataSetD2,
                    targetVariableString, targetValue, alpha, depth);

            pcxc.classify();
            double[] scoresTemp = pcxc.getScores();
            boolean[] inCategoryTemp = pcxc.getCategoryFacts();
            int ncases = pcxc.getNumValidCases();

            //Since not all cases might have had valid marginals, the arrays returned from
            //pcxc accessors for scores and category facts may not be "full".
            double[] scores = new double[ncases];
            boolean[] inCategory = new boolean[ncases];
            for (int i = 0; i < ncases; i++) {
                scores[i] = scoresTemp[i];
                inCategory[i] = inCategoryTemp[i];
            }

            //Debug print:
            System.out.println(
                    "Scores and  categories for " + ncases + " cases:  ");
            for (int i = 0; i < ncases; i++) {
                System.out.println(i + " " + scores[i] + " " + inCategory[i]);
            }

            //Instantiate the ROC calculator.
            RocCalculator rocc = new RocCalculator(scores, inCategory, 0);

            //Create the ROC curve.
            System.out.println("The ROC Curve:  ");
            //int[][] plot = rocc.getUnscaledRocPlot();

            double[][] points = rocc.getScaledRocPlot();

            double area = rocc.getAuc();
            NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
            String info = "AUC = " + nf.format(area);
            System.out.println(info);

            RocPlot plotter = new RocPlot(points, "ROC Plot", info);

            JFrame plotterFrame = new JFrame("ROC Curve");
            plotterFrame.setSize(600, 600);
            plotterFrame.setContentPane(plotter);
            plotterFrame.pack();

            plotterFrame.setVisible(true);
            plotter.repaint();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    private static class RocPlotter extends Frame {

        int[][] points;

        public RocPlotter(String title, int[][] points) {
             this.points = points;
        }

        public void paint(Graphics g) {
            g.setColor(Color.black);
             for(int i = 0; i < points.length; i++) {
                  g.fillRect(points[i][0], points[i][1], 1, 1);
             }
        }
    }
    */
}


