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

package edu.cmu.shuttle;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.RectangularDataSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedSet;

/**
 * Tests the shuttle data converter methods.
 *
 * @author Joseph Ramsey
 * @version $Date: 2006-10-28 12:33:05 -0400 (Sat, 28 Oct 2006) $
 */
public final class TestShuttleDataConverter extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestShuttleDataConverter(String name) {
        super(name);
    }

    public static void testCountVariables() {
        String fileD = "test_data/110_subset.test_subset.txt";
        File file = new File(fileD);

        try {
            SortedSet<String> variables =
                    ShuttleDataConverter.getVariables(file);
            int numTotal = variables.size();
            System.out.println("# variables = " + numTotal);

            SortedSet<String> discreteVariables =
                    ShuttleDataConverter.getDiscreteVariables(file, variables);
            int numDiscrete = discreteVariables.size();
            System.out.println("# discrete variables = " + numDiscrete);

            SortedSet<String> continuousVariables =
                    ShuttleDataConverter.getContinuousVariables(file,
                            variables);
            int numContinuous = continuousVariables.size();
            System.out.println("# continuous variables = " + numContinuous);

            // There is one variable showing launch time.
            assertEquals(numTotal, numDiscrete + numContinuous + 1);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtestWriteContinuousVariables() {
        try {
            String infile =
                    "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
//            String infile = "test_data/110_subset.test_subset.txt";
            String outFile = "test_data/110_subset.contvars.txt";
            ShuttleDataConverter.writeContinuousVariableNames(new File(infile),
                    new File(outFile));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtestWriteDiscreteVariables() {                              
        try {
            String infile =
                    "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
//            String infile = "test_data/110_subset.test_subset.txt";
            String outFile = "test_data/110_subset.discvars.txt";
            ShuttleDataConverter.writeDiscreteVariableNames(new File(infile),
                    new File(outFile));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtestReadVariableNames() {
        try {
            String file = "test_data/110_subset.contvars.txt";
            SortedSet<String> variables =
                    ShuttleDataConverter.readVariableNames(new File(file));

            System.out.println(variables);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtestReadContinuousData() {
        try {
//            String dataFile = "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            String dataFile =
                    "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
//            String dataFile = "test_data/110_subset.test_subset.txt";
            String varFile = "test_data/110_subset.contvars.120.txt";
            SortedSet<String> variables =
                    ShuttleDataConverter.readVariableNames(new File(varFile));

            long beginTime = ConvertUtils.parseYearTime("98:20:30:32/00");
            long endTime = ConvertUtils.parseYearTime("98:20:49:19/999");
//            long endTime = ConvertUtils.parseYearTime("98:20:48:0/0");
            long interval = 1000L;


            RectangularDataSet data = ShuttleDataConverter.readContinuousData(
                    new File(dataFile), variables, beginTime, endTime,
                    interval);

//            System.out.println(data);

//            PrintWriter out = new PrintWriter(new FileWriter("/home/jdramsey/shuttleout.txt"));
            String outFile = "test_data/110_subset.contvars.120.data.txt";
            PrintWriter out = new PrintWriter(new FileWriter(outFile));
            out.println(data);
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtestSimpleInversion() {
        DoubleMatrix2D m = new DenseDoubleMatrix2D(3, 3);

        m.set(0, 0, 1.00);
        m.set(0, 1, .8269);
        m.set(0, 2, .0631);
        m.set(1, 0, .8269);
        m.set(1, 1, 1.0);
        m.set(1, 2, 0.6128);
        m.set(2, 0, .0631);
        m.set(2, 1, 0.6128);
        m.set(2, 2, 1.0);

        DoubleMatrix2D n = new Algebra().inverse(m);

        System.out.println(m);
        System.out.println(n);
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestShuttleDataConverter.class);
    }
}

