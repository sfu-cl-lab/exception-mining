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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * Runs a test of logistic regression based on an example (South African heart
 * disease) from "Elements of Statistical Learning" by Hastie, Tibshirani and
 * Friedman.
 *
 * @author Frank Wimberly
 */
public class TestLogisticRegressionRunner extends TestCase {

    public TestLogisticRegressionRunner(String name) {
        super(name);
    }

    @Override
	public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    @Override
	public void tearDown(){
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    public static void testLogRegRunner() {

        //Test with discrete data.

        String filenameD1 = "test_data/SAHDMod.dat";
        File fileD1 = new File(filenameD1);

        //        FileReader frD1 = null;

        double alpha = 0.05;

        RectangularDataSet dds2;

        try {
            DataParser parser = new DataParser();
            parser.setDelimiter(DelimiterType.TAB);
            parser.setMaxIntegralDiscrete(0);
            dds2 = parser.parseTabular(fileD1);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        DataWrapper dds2Wrapper = new DataWrapper(dds2);

        LogisticRegressionParams params = new LogisticRegressionParams();
        params.setAlpha(alpha);
        params.setTargetName("chd");
        String[] regressorNames = {"sbp", "tobacco", "ldl", "famhist",
                "obesity", "alcohol", "age"};
        params.setRegressorNames(regressorNames);

        LogisticRegressionRunner runner =
                new LogisticRegressionRunner(dds2Wrapper, params);

        runner.execute();
        double[] coefficients = runner.getCoefficients();

        for (int i = 0; i < coefficients.length; i++) {

            System.out.println("Logistic Regression Coefficients Coefficients");
            System.out.println(i + " " + coefficients[i]);
        }

        //See the South African heart disease example in Hastie, Tibshirani and Friedman,
        //"Elements of Statistical Learning".
        double[] correctCoefs =
                {-4.13, 0.006, 0.080, 0.185, 0.939, -0.035, 0.001, 0.043};

        for (int i = 0; i < coefficients.length; i++) {
            assertEquals(correctCoefs[i], coefficients[i], 0.001);
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestLogisticRegressionRunner.class);
    }
}


