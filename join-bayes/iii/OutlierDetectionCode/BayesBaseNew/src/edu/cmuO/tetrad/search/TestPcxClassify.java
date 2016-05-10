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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TestPcxClassify extends TestCase {
    //static int[][] testCrosstabs = {{496, 53},
    //                               {93, 358}};

    //NOTE:  the values below replaced those above when a bug was fixed in PcxSearch
    // in the Ramsey procedure.
    // We assume that the values below are correct.
    static int[][] testCrosstabs = {{492, 57}, {78, 373}};

    static int[][] testCrosstabsNew = {{38, 7, 9}, {14, 9, 16}, {4, 3, 60}};

    public TestPcxClassify(String name) {
        super(name);
    }

    @Override
	public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    @Override
	public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    public void testBlank() {

    }

    public static void rtestCrosstabs() {

        try {
            //Test with discrete data.
            String filenameD1 = "test_data/markovBlanketTestDisc.dat";
            String filenameD2 = "test_data/markovBlanketTestDisc.dat";
            String filenameD3 = "test_data/sampledata.txt";
            String filenameD4 = "test_data/sampledata.txt";

            double alpha = 0.05;
            int depth = -1;

            DataParser parser = new DataParser();
            RectangularDataSet dds1 = parser.parseTabular(new File(filenameD1));
            RectangularDataSet dds2 = parser.parseTabular(new File(filenameD2));

            String targetVariableString = "A6";

            PcxClassifier pcxc = new PcxClassifier(dds1, dds2, targetVariableString,
                    alpha, depth);

            int[][] crossTabs = pcxc.crossTabulation();

            DiscreteVariable targetVariable = pcxc.getTargetVariable();
            int nvalues = targetVariable.getNumCategories();

            System.out.println("Target Variable " + targetVariableString);
            System.out.println("\t\t\tEstimated\t");
            System.out.print("Observed\t");
            for (int i = 0; i < nvalues; i++) {
                System.out.print(targetVariable.getCategory(i) + "\t");
            }
            System.out.println();
            for (int i = 0; i < nvalues; i++) {
                System.out.print(targetVariable.getCategory(i) + "\t");
                for (int j = 0; j < nvalues; j++) {
                    System.out.print(crossTabs[i][j] + "\t\t");
                }
                System.out.println();
            }

            //Test with discrete data found in Xue's sampledata.txt.
            RectangularDataSet dds3 = parser.parseTabular(new File(filenameD3));
            RectangularDataSet dds4 = parser.parseTabular(new File(filenameD4));

            String targetVariableStringNew = "col2";

            PcxClassifier pcxcNew = new PcxClassifier(dds3, dds4,
                    targetVariableStringNew, alpha, depth);

            int[][] crossTabsNew = pcxcNew.crossTabulation();
            DiscreteVariable targetVariableNew = pcxcNew.getTargetVariable();
            int nvaluesNew = targetVariableNew.getNumCategories();

            System.out.println("Target Variable " + targetVariableStringNew);
            System.out.println("\t\t\tEstimated\t");
            System.out.print("Observed\t");
            for (int i = 0; i < nvaluesNew; i++) {
                System.out.print(targetVariableNew.getCategory(i) + "\t");
            }
            System.out.println();
            for (int i = 0; i < nvaluesNew; i++) {
                System.out.print(targetVariableNew.getCategory(i) + "\t\t");
                for (int j = 0; j < nvaluesNew; j++) {
                    System.out.print(crossTabsNew[i][j] + "\t");
                }
                System.out.println();
            }

            assertTrue(Arrays.equals(crossTabs[0], testCrosstabs[0]));
            assertTrue(Arrays.equals(crossTabs[1], testCrosstabs[1]));

            assertTrue(Arrays.equals(crossTabsNew[0], testCrosstabsNew[0]));
            assertTrue(Arrays.equals(crossTabsNew[1], testCrosstabsNew[1]));
            assertTrue(Arrays.equals(crossTabsNew[2], testCrosstabsNew[2]));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestPcxClassify.class);
    }
}


