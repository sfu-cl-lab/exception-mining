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

package edu.cmu.tetrad.search.test;

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.search.PcxClassifier;

import java.io.File;
import java.io.IOException;

/**
 * @author Frank Wimberly
 * @version $Revision: 6039 $ $Date: 2005-07-07 17:17:27 -0400 (Thu, 07 Jul
 *          2005) $
 * @deprecated
 */
@Deprecated
public class PcxClassifyTester {
    public static void main(String[] args) {

        try {
            //Test with discrete data.
            String filenameD1 = args[0];
            String filenameD2 = args[1];
            //String fileD = "../../../test_data/markovBlanketTestDisc.dat";

            File fileD1 = new File(filenameD1);
            File fileD2 = new File(filenameD2);

            //double alpha = 0.05;
            double alpha = Double.parseDouble(args[2]);

            String targetVariableString = args[3];

            int depth = Integer.parseInt(args[4]);

            DataParser parser = new DataParser();
            RectangularDataSet dds1 = parser.parseTabular(fileD1);

            parser.setKnownVariables(dds1.getVariables());
            RectangularDataSet dds2 = parser.parseTabular(fileD2);


            PcxClassifier pcxc = new PcxClassifier(dds1, dds2, targetVariableString,
                    alpha, depth);

            int[][] crossTabs = pcxc.crossTabulation();
            DiscreteVariable targetVariable = pcxc.getTargetVariable();
            int nvalues = targetVariable.getNumCategories();

            //System.out.println("Number correct = " + numCorrect);
            System.out.println("Target Variable " + targetVariableString);
            System.out.println("\t\t\tEstimated\t");
            System.out.print("Observed\t");
            for (int i = 0; i < nvalues - 1; i++) {
                System.out.print(targetVariable.getCategory(i) + "\t");
            }
            System.out.print(targetVariable.getCategory(nvalues - 1));
            System.out.println();
            for (int i = 0; i < nvalues; i++) {
                System.out.print(targetVariable.getCategory(i) + "\t\t");
                for (int j = 0; j < nvalues - 1; j++) {
                    System.out.print(crossTabs[i][j] + "\t");
                }
                System.out.print(crossTabs[i][nvalues - 1]);
                System.out.println();
            }

            System.out.println("Percentage correctly classified:  ");
            System.out.println(pcxc.getPercentCorrect());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}


