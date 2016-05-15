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

package edu.cmu.tetrad.graph;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Joseph Ramsey
 */
public final class TestSemGraph extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestSemGraph(String name) {
        super(name);
    }

    public void test1() {
        SemGraph graph = new SemGraph();

        Node a = new GraphNode("a");
        Node b = new GraphNode("b");
        Node c = new GraphNode("c");
        Node d = new GraphNode("d");
        Node e = new GraphNode("e");

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        graph.addDirectedEdge(a, b);
        graph.addDirectedEdge(c, d);
        graph.addBidirectedEdge(a, c);
        graph.addBidirectedEdge(b, d);
        System.out.println(graph);

        graph.removeEdge(a, b);
        System.out.println(graph);

        graph.addDirectedEdge(e, a);
        System.out.println(graph);

        graph.addBidirectedEdge(c, d);
        System.out.println(graph);

        SemGraph graph2 = new SemGraph(graph);
        System.out.println(graph2);

        SemGraph copy = new SemGraph(graph2);
        Graph stripErrors = new EdgeListGraph(copy);
        System.out.println(stripErrors);
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestSemGraph.class);
    }
}



