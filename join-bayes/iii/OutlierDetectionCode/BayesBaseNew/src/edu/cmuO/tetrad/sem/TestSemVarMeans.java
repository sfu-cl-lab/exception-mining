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

package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Tests the use of variable means in the SEM classes.  Instantiates a SemPm and
 * SemIm from it. Values of the means of the variables are set in the IM. Then
 * the simulateData is used to create a continuous dataset.  The dataset is used
 * to SemEstimator and, from that, a new SemIm.  The means of the two SemIm's
 * are compared to see if they fall within a tolerance.
 *
 * @author Frank Wimberly
 */
public class TestSemVarMeans extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestSemVarMeans(String name) {
        super(name);
    }

    public void testMeans() {
        Graph graph = constructGraph1();
        SemPm semPm1 = new SemPm(graph);

        List<Parameter> parameters = semPm1.getParameters();

        for (Parameter p : parameters) {
            p.setInitializedRandomly(false);
        }

        SemIm semIm1 = new SemIm(semPm1);

        double[] means = {5.0, 4.0, 3.0, 2.0, 1.0};
        long seed = -379467L;

        for (int i = 0; i < semIm1.getVariableNodes().size(); i++) {
            Node node = semIm1.getVariableNodes().get(i);
            semIm1.setMean(node, means[i]);
        }

        RectangularDataSet dataSet = semIm1.simulateData(1000, false, seed);

        SemEstimator semEst = new SemEstimator(dataSet, semPm1);
        semEst.estimate();
        SemIm estSemIm = semEst.getEstimatedSem();
        List<Node> nodes = semPm1.getVariableNodes();

        System.out.println("Original means   Simulated means:");

        for (Node node : nodes) {
            System.out.println("For node " + node);
            assertEquals(semIm1.getMean(node), estSemIm.getMean(node), 0.06);
            System.out.println(semIm1.getMean(node) + "          " +
                    estSemIm.getMean(node));
        }
    }

    private Graph constructGraph1() {
        Graph graph = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");

        //x1.setNodeType(NodeType.LATENT);
        //x2.setNodeType(NodeType.LATENT);

        graph.addNode(x1);
        graph.addNode(x2);
        graph.addNode(x3);
        graph.addNode(x4);
        graph.addNode(x5);

        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);
        graph.addDirectedEdge(x3, x4);
        graph.addDirectedEdge(x1, x4);
        graph.addDirectedEdge(x4, x5);

        return graph;
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestSemVarMeans.class);
    }
}


