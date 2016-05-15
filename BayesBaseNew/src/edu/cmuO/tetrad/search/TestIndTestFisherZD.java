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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.PersistentRandomUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests the IndTestTimeSeries class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6061 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public class TestIndTestFisherZD extends TestCase {
    public TestIndTestFisherZD(String name) {
        super(name);
    }

    public void testIsIndependent() {
        Graph graph = constructGraph();
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        System.out.println("Original SemIm: " + semIm);

        RectangularDataSet dataSet = semIm.simulateData(400, false);
//        double[][] data = dataSet.getDoubleData();

//        System.out.println("Data = ");
//        System.out.println(MatrixUtils.toString(data));

        IndTestFisherZD test = new IndTestFisherZD(dataSet, 0.05);
        List<Node> v = test.getVariables();

        Node xVar = v.get(0);
        Node yVar = v.get(2);
        List<Node> zList = new ArrayList<Node>();
//        zList.add(v.get(1));
//        zList.add(v.get(2));

        System.out.println(test.isIndependent(xVar, yVar, zList));
//        System.out.println(test.isDetermined(xVar, yVar, zList));
    }

    private Graph constructGraph() {
        Graph graph = new EdgeListGraph();

        Node x0 = new GraphNode("X0");
        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");

        graph.addNode(x0);
        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);

        graph.addDirectedEdge(x0, x1);
        graph.addDirectedEdge(x1, x2);
//        graph.addDirectedEdge(x2, x3);
//        graph.addDirectedEdge(x1, x4);
//        graph.addDirectedEdge(x4, x5);

        return graph;
    }

    public void testGInverse() {
        DoubleMatrix2D X = new DenseDoubleMatrix2D(
                new double[][]{{2, 2}, {4, 6}, {3, -5}, {3, -5}});

        DoubleMatrix2D G = MatrixUtils.ginverse(X);
        DoubleMatrix2D prod = new Algebra().mult(X, new Algebra().mult(G, X));
        System.out.println("Prod = " + prod);
    }

    public void testSplitDetermination() {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        System.out.println("trial\ta1\ta2\ta3\ta4\tcases\tp\tind");

        for (int i = 0; i < 200; i++) {

            double limit = 20;
            double a1 = PersistentRandomUtil.getInstance().nextDouble() * 2 *
                    limit - limit;

            if (Math.abs(a1) < 0.4) {
                i--;
                continue;
            }

            double a2 = PersistentRandomUtil.getInstance().nextDouble() * 2 *
                    30 - 30;
//            double a2 = 0;
            double limit2 = 0.00001;
            double a3 = PersistentRandomUtil.getInstance().nextDouble() * 2 *
                    limit2 - limit2;

//            if (Math.abs(a3) < 0.4) {
//                i--;
//                continue;
//            }

            double a4 = PersistentRandomUtil.getInstance().nextDouble() * 2 *
                    30 - 30;
//            double a4 = 0;
            int sampleSize = 1000;

            Node x2 = new ContinuousVariable("X2");
            Node x4 = new ContinuousVariable("X4");
            Node x6 = new ContinuousVariable("X6");

            List<Node> variables = new LinkedList<Node>();
            variables.add(x2);
            variables.add(x4);
            variables.add(x6);

            RectangularDataSet dataSet = new ColtDataSet(sampleSize, variables);

            double[] x2Data = new double[sampleSize];
            double[] x4Data = new double[sampleSize];
            double[] x6Data = new double[sampleSize];

            for (int j = 0; j < sampleSize; j++) {
                double d1 = (PersistentRandomUtil.getInstance().nextDouble() -
                        0.5) * 200.0;
                double d2 = (PersistentRandomUtil.getInstance().nextDouble() -
                        0.5) * 200.0;

                if (PersistentRandomUtil.getInstance().nextDouble() >= 0.5) {
                    x2Data[j] = a1 * d1 + a2;
                    x4Data[j] = d1;
                    x6Data[j] = d2;
                }
                else {
                    x2Data[j] = d2;
                    x4Data[j] = d1;
                    x6Data[j] = a3 * d1 + a4;
                }
            }

            int col = dataSet.getVariables().indexOf(x2);

            for (int i1 = 0; i1 < x2Data.length; i1++) {
                dataSet.setDouble(i1, col, x2Data[i1]);
            }
            int col1 = dataSet.getVariables().indexOf(x4);

            for (int i2 = 0; i2 < x4Data.length; i2++) {
                dataSet.setDouble(i2, col1, x4Data[i2]);
            }
            int col2 = dataSet.getVariables().indexOf(x6);

            for (int i3 = 0; i3 < x6Data.length; i3++) {
                dataSet.setDouble(i3, col2, x6Data[i3]);
            }

            IndTestFisherZD test = new IndTestFisherZD(dataSet, 0.05);
            boolean independent =
                    test.isIndependent(x2, x6, Collections.singletonList(x4));


            System.out.print((i + 1) + "\t");
            System.out.print(nf.format(a1) + "\t");
            System.out.print(nf.format(a2) + "\t");
            System.out.print(nf.format(a3) + "\t");
            System.out.print(nf.format(a4) + "\t");
            System.out.print(sampleSize + "\t");
            System.out.print(nf.format(test.getPValue()) + "\t");
            System.out.print(independent);
            System.out.println();
        }
    }

    public static Test suite() {
        return new TestSuite(TestIndTestFisherZD.class);
    }
}


