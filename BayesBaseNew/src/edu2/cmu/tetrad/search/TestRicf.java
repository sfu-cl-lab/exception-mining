///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.PlusMult;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestRicf extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestRicf(String name) {
        super(name);
    }


    public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
    }


    public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    /**
     * <pre>
     * > ## A covariance matrix
     * <p/>
     * > "S" <- structure(c(2.93, -1.7, 0.76, -0.06,
     * +                   -1.7, 1.64, -0.78, 0.1,
     * +                    0.76, -0.78, 1.66, -0.78,
     * +                   -0.06, 0.1, -0.78, 0.81), .Dim = c(4,4),
     * +                  .Dimnames = list(c("y", "x", "z", "u"), c("y", "x",
     * "z", "u")))
     * <p/>
     * > ## The following should give the same fit.
     * <p/>
     * > ## Fit an ancestral graph y -> x <-> z <- u
     * <p/>
     * > fitAncestralGraph(ag1 <- makeAG(dag=DAG(x~y,z~u), bg = UG(~x*z)), S,
     * n=100)
     * $Shat
     * y          x          z          u
     * y  2.930000 -1.4344254  0.0000000  0.0000000
     * x -1.434425  1.3799680 -0.3430373  0.0000000
     * z  0.000000 -0.3430373  1.5943070 -0.7442518
     * u  0.000000  0.0000000 -0.7442518  0.8100000
     * <p/>
     * $Lhat
     * y x z    u
     * y 2.93 0 0 0.00
     * x 0.00 0 0 0.00
     * z 0.00 0 0 0.00
     * u 0.00 0 0 0.81
     * <p/>
     * $Bhat
     * y x z         u
     * y 1.000000 0 0 0.0000000
     * x 0.489565 1 0 0.0000000
     * z 0.000000 0 1 0.9188294
     * u 0.000000 0 0 1.0000000
     * <p/>
     * $Ohat
     * y          x          z u
     * y 0  0.0000000  0.0000000 0
     * x 0  0.6777235 -0.3430373 0
     * z 0 -0.3430373  0.9104666 0
     * u 0  0.0000000  0.0000000 0
     * <p/>
     * $dev
     * [1] 21.57711
     * <p/>
     * $df
     * [1] 3
     * <p/>
     * $it
     * [1] 4
     * <p/>
     * <p/>
     * <p/>
     * </pre>
     */
    public void testRicf1() {
        String[] varNames = new String[]{"y", "x", "z", "u"};
        int numVars = varNames.length;

        double[] values = {2.93, -1.7, 0.76, -0.06, -1.7, 1.64, -0.78, 0.1,
                0.76, -0.78, 1.66, -0.78, -0.06, 0.1, -0.78, 0.81};
        DoubleMatrix2D m = matrix(values, numVars, numVars);

        ICovarianceMatrix s = new CovarianceMatrix(DataUtils.createContinuousVariables(varNames), m, 30);

        System.out.println(s);

        Graph mag = new EdgeListGraph();
        Node x = new ContinuousVariable("x");
        Node y = new ContinuousVariable("y");
        Node z = new ContinuousVariable("z");
        Node u = new ContinuousVariable("u");
        mag.addNode(x);
        mag.addNode(y);
        mag.addNode(z);
        mag.addNode(u);

        mag.addDirectedEdge(y, x);
//        mag.addDirectedEdge(u, x);
        mag.addBidirectedEdge(x, z);
        mag.addDirectedEdge(u, z);

        System.out.println(mag);

        int n = 100;
        double tol = 1e-06;

        Ricf ricf = new Ricf();
        Ricf.RicfResult ricfResult = ricf.ricf(mag, s, n, tol);

        System.out.println(ricfResult);

        // Test shat at least.
        double[] shatValues = {2.93, -1.434425, 0, 0,
                -1.434425, 1.379968, -0.343037, 0,
                0, -0.343037, 1.594307, -0.744252,
                0, 0, -0.744252, 0.81};

        double norm = normdiff(ricfResult, shatValues, numVars, numVars);
        assertTrue(norm < 0.0001);

        // sHat should be the same for the bidirected model.

        mag.removeEdges(mag.getEdges());
        mag.addBidirectedEdge(y, x);
//        mag.addDirectedEdge(u, x);
        mag.addBidirectedEdge(x, z);
        mag.addBidirectedEdge(u, z);

        System.out.println(mag);

        Ricf.RicfResult ricfResult2 = ricf.ricf(mag, s, n, tol);

        System.out.println(ricfResult2);

        norm = normdiff(ricfResult, shatValues, numVars, numVars);
        assertTrue(norm < 0.0001);
    }

    private double normdiff(Ricf.RicfResult ricfResult, double[] shatValues,
                            int rows, int cols) {
        DoubleMatrix2D shat = matrix(shatValues, rows, cols);
        DoubleMatrix2D diff = shat.copy();
        diff.assign(ricfResult.getShat(), PlusMult.plusMult(-1));
        return new Algebra().norm1(diff);
    }

    private DoubleMatrix2D matrix(double[] values, int rows, int cols) {
        DoubleMatrix2D m = new DenseDoubleMatrix2D(rows, cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, values[i + cols * j]);
            }
        }

        return m;
    }

    public void testCliques() {
        Graph graph = new EdgeListGraph();

        ContinuousVariable x0 = new ContinuousVariable("X0");
        ContinuousVariable x1 = new ContinuousVariable("X1");
        ContinuousVariable x2 = new ContinuousVariable("X2");
        ContinuousVariable x3 = new ContinuousVariable("X3");
        ContinuousVariable x4 = new ContinuousVariable("X4");
        ContinuousVariable x5 = new ContinuousVariable("X5");

        graph.addNode(x0);
        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);

        graph.addUndirectedEdge(x0, x1);
        graph.addUndirectedEdge(x0, x2);
        graph.addUndirectedEdge(x0, x3);
        graph.addUndirectedEdge(x1, x2);
        graph.addUndirectedEdge(x1, x3);
        graph.addUndirectedEdge(x1, x4);
        graph.addUndirectedEdge(x2, x4);

        graph.addUndirectedEdge(x5, x0);
        graph.addUndirectedEdge(x5, x1);
        graph.addUndirectedEdge(x5, x2);

        List<List<Node>> cliques = new Ricf().cliques(graph);

        System.out.println(cliques);
    }

    public void testCliques2() {
        Graph graph = GraphUtils.randomDag(8, 0, 20, 5, 5, 5, false);
        List<List<Node>> cliques = new Ricf().cliques(graph);
        System.out.println(graph);
        System.out.println(cliques);
    }

    /**
     * Whittaker p. 59.
     */
    public void testCliques3() {
        Graph graph = new EdgeListGraph();

        ContinuousVariable x1 = new ContinuousVariable("X1");
        ContinuousVariable x2 = new ContinuousVariable("X2");
        ContinuousVariable x3 = new ContinuousVariable("X3");
        ContinuousVariable x4 = new ContinuousVariable("X4");
        ContinuousVariable x5 = new ContinuousVariable("X5");
        ContinuousVariable x6 = new ContinuousVariable("X6");
        ContinuousVariable x7 = new ContinuousVariable("X7");

        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);
        graph.addNode(x6);
        graph.addNode(x7);

        graph.addUndirectedEdge(x1, x2);
        graph.addUndirectedEdge(x1, x4);
        graph.addUndirectedEdge(x2, x3);
        graph.addUndirectedEdge(x2, x5);
        graph.addUndirectedEdge(x3, x5);
        graph.addUndirectedEdge(x4, x5);
        graph.addUndirectedEdge(x5, x6);

        List<List<Node>> cliques = new Ricf().cliques(graph);

        System.out.println(cliques);
    }

//    public void testFitConGraph() {
//        DataSet data = null;
//
//        try {
//            DataReader reader = new DataReader();
//            reader.setIdsSupplied(true);
//            data = reader.parseTabular(new File("test_data/marks.txt"));
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
////        System.out.println(data);
//
//        CovarianceMatrix s = new CovarianceMatrix(data);
//
////        System.out.println(s);
//
//        Graph graph = new EdgeListGraph();
//        Node mechanics = new ContinuousVariable("mechanics");
//        Node vectors = new ContinuousVariable("vectors");
//        Node algebra = new ContinuousVariable("algebra");
//        Node analysis = new ContinuousVariable("analysis");
//        Node statistics = new ContinuousVariable("statistics");
//        graph.addIndex(mechanics);
//        graph.addIndex(vectors);
//        graph.addIndex(algebra);
//        graph.addIndex(analysis);
//        graph.addIndex(statistics);
//
//        graph.addUndirectedEdge(mechanics, algebra);
//        graph.addUndirectedEdge(mechanics, vectors);
//        graph.addUndirectedEdge(algebra, vectors);
//        graph.addUndirectedEdge(algebra, analysis);
//        graph.addUndirectedEdge(algebra, statistics);
//        graph.addUndirectedEdge(analysis, statistics);
//
////        System.out.println(graph);
//
//        int n = 88;
//        double tol = 1e-06;
//
//        Ricf ricf = new Ricf();
////        Ricf.IcfMagResult icfMagResult = ricf.icfmag(graph, s, n, tol);
//        Ricf.FitConGraphResult result = ricf.fitConGraph(graph, s, n, tol);
//
//        System.out.println(result);
//    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestRicf.class);
    }
}

