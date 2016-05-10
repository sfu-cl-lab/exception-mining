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

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestPcxSearch extends TestCase {
    static Graph testGraphSub;
    static Graph testGraphSubCorrect;

    public TestPcxSearch(String name) {
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

    public static void testSubgraph() {

        try {
            //Test new methods in SearchGraphMatrix:
            List<Node> V = new ArrayList<Node>();
            V.add(new ContinuousVariable("X1"));
            V.add(new ContinuousVariable("X2"));
            V.add(new ContinuousVariable("X3"));
            V.add(new ContinuousVariable("X4"));
            V.add(new ContinuousVariable("X5"));
            V.add(new ContinuousVariable("X6"));
            V.add(new ContinuousVariable("X7"));
            V.add(new ContinuousVariable("X8"));
            V.add(new ContinuousVariable("X9"));
            V.add(new ContinuousVariable("X10"));
            V.add(new ContinuousVariable("X11"));
            V.add(new ContinuousVariable("X12"));
            V.add(new ContinuousVariable("X13"));
            V.add(new ContinuousVariable("X14"));
            V.add(new ContinuousVariable("X15"));
            V.add(new ContinuousVariable("X16"));

            Graph testGraph = new EndpointMatrixGraph(V);

            testGraph.setEndpoint(V.get(0), V.get(4), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(4), V.get(0), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(1), V.get(4), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(4), V.get(1), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(1), V.get(5), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(5), V.get(1), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(2), V.get(5), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(5), V.get(2), Endpoint.TAIL);

            assertTrue(testGraph.setEndpoint(V.get(2), V.get(6), Endpoint.ARROW));
            assertTrue(testGraph.setEndpoint(V.get(6), V.get(2), Endpoint.TAIL));

            assertTrue(testGraph.setEndpoint(V.get(3), V.get(6), Endpoint.ARROW));
            assertTrue(testGraph.setEndpoint(V.get(6), V.get(3), Endpoint.TAIL));

            assertTrue(testGraph.setEndpoint(V.get(7), V.get(11), Endpoint.ARROW));
            testGraph.setEndpoint(V.get(11), V.get(7), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(8), V.get(11), Endpoint.ARROW);
            assertTrue(testGraph.setEndpoint(V.get(11), V.get(8), Endpoint.TAIL));

            testGraph.setEndpoint(V.get(8), V.get(12), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(12), V.get(8), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(5), V.get(12), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(12), V.get(5), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(5), V.get(13), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(13), V.get(5), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(5), V.get(14), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(14), V.get(5), Endpoint.TAIL);

            testGraph.setEndpoint(V.get(9), V.get(14), Endpoint.ARROW);
            testGraph.setEndpoint(V.get(14), V.get(9), Endpoint.TAIL);

            assertTrue(testGraph.setEndpoint(V.get(9), V.get(15), Endpoint.ARROW));
            assertTrue(testGraph.setEndpoint(V.get(15), V.get(9), Endpoint.TAIL));

            assertTrue(testGraph.setEndpoint(V.get(10), V.get(15), Endpoint.ARROW));
            assertTrue(testGraph.setEndpoint(V.get(15), V.get(10), Endpoint.TAIL));

            System.out.println("Original Graph");
            System.out.println(testGraph);

            List<Node> subSet = new ArrayList<Node>();
            subSet.add(V.get(1));
            subSet.add(V.get(2));
            subSet.add(V.get(5));
            subSet.add(V.get(12));
            subSet.add(V.get(13));
            subSet.add(V.get(14));

            testGraphSub = testGraph.subgraph(subSet);

            testGraphSubCorrect = GraphConverter.convert(
                    "X2-->X6,X3-->X6,X6-->X13,X6-->X14,X6-->X15");

            //Was subgraph computed correctly?
            assertTrue(testGraphSub.equals(testGraphSubCorrect));

            System.out.println("The following graph should be that subgraph of");
            System.out.println(" the original graph containing the variables X2,");
            System.out.println("X3,X6,X13,X14,X15 and the edges between them.");
            System.out.println("Subgraph");
            System.out.println(testGraphSub);
            System.out.println("\n\n");

            String filename = "test_data/markovBlanketTest.dat";
            File file = new File(filename);

            DataParser parser = new DataParser();
            RectangularDataSet dataSet = parser.parseTabular(file);

            IndependenceTest test;
            PcxSearch pcx;
            double alpha = 0.05;
            test = new IndTestCramerT(dataSet, alpha);

            pcx = new PcxSearch(test, -1);
            Graph mb = pcx.search("X6");

            Graph mbCorrect = GraphConverter.convert("X6-->X13,X6-->X14,X6-->X15," +
                    "X2-->X6,X3-->X6,X9-->X13,X10-->X15");

            System.out.println("Mb = " + mb);

            System.out.println("Mbcorrect = " + mbCorrect);

            assertTrue(mb.equals(mbCorrect));

            System.out.println("Markov Blanket with target = X6:  ");
            System.out.println(mb);
            System.out.println("Test search completed for continuous data.");

            //Test with discrete data.
            String filenameD = "test_data/markovBlanketTestDisc.dat";
            File fileD = new File(filenameD);

            List<Node> knownVariables = null;

            RectangularDataSet dds = parser.parseTabular(fileD);

            IndependenceTest indTestD;
            PcxSearch pcxD;
            indTestD = new IndTestChiSquare(dds, alpha);

            pcxD = new PcxSearch(indTestD, -1);
            Graph mbD = pcxD.search("A6");

            //NOTE:  the last two edges were added after a bug was fixed in the Ramsey procedure.
            // The fix caused the result of the search for this case to change.
            Graph mbDCorrect = GraphConverter.convert("A6-->A13,A6-->A14," +
                    "A9-->A13,A7-->A3,A6-->A3,A10-->A15,A6-->A15");

            System.out.println("mdD = " + mbD);
            System.out.println("mdDCorrect = " + mbDCorrect);

            assertTrue(mbD.equals(mbDCorrect));


            System.out.println("Markov Blanket for X6 in discrete dataset");
            System.out.println(mbD);
            System.out.println("Test search completed for discrete data.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        testSubgraph();
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestPcxSearch.class);
    }

}


