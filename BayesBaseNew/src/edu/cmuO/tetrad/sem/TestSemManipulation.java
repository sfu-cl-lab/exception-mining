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

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the MeasurementSimulator class using diagnostics devised by Richard
 * Scheines. The diagnostics are described in the Javadocs, below.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-07-18 13:23:23 -0400 (Mon, 18 Jul
 *          2005) $
 */
public class TestSemManipulation extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestSemManipulation(String name) {
        super(name);
    }

    public void testEvidence() {
        Graph graph = constructGraph1();
        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);
        SemManipulation manipulation = new SemManipulation(semIm);
        System.out.println(manipulation);
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestSemManipulation.class);
    }

    private Graph constructGraph1() {
        Graph graph = new EdgeListGraph();

        Node x1 = new GraphNode("X1");
        Node x2 = new GraphNode("X2");
        Node x3 = new GraphNode("X3");
        Node x4 = new GraphNode("X4");
        Node x5 = new GraphNode("X5");

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
}


