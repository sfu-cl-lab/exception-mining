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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Frank Wimberly
 * @version $Revision: 6039 $ $Date: 2006-01-09 00:19:55 -0500 (Mon, 09 Jan
 *          2006) $
 */
public final class TestBDeMetric extends TestCase {

    public TestBDeMetric(String name) {
        super(name);
    }

    public static void testMetric() {
        try {
            String fileName = "test_data/testbdemetricshort.dat";
            File file = new File(fileName);

//            ds = DataLoaders.loadDiscreteData(file, DelimiterType.TAB, "#",
//                    null);

            DataParser parser = new DataParser();
            RectangularDataSet ds = parser.parseTabular(file);

            Node x1 = new GraphNode("X1");
            Node x2 = new GraphNode("X2");
            Node x3 = new GraphNode("X3");
            Node x4 = new GraphNode("X4");
            Node x5 = new GraphNode("X5");
            //        graph = new EdgeListGraph();
            Dag graph = new Dag();


            graph.clear();

            // Add and remove some nodes.
            graph.addNode(x1);
            graph.addNode(x2);
            graph.addNode(x3);
            graph.addNode(x4);
            graph.addNode(x5);

            graph.addDirectedEdge(x1, x2);
            graph.addDirectedEdge(x2, x3);
            graph.addDirectedEdge(x3, x4);


            BayesPm bayesPm = new BayesPm(graph);
            bayesPm.setNumCategories(x1, 2);
            bayesPm.setNumCategories(x2, 2);
            bayesPm.setNumCategories(x3, 2);
            bayesPm.setNumCategories(x4, 2);
            bayesPm.setNumCategories(x5, 2);

            BdeMetric bdem = new BdeMetric(ds, bayesPm);
            double scoreOrig = bdem.score();
            System.out.println("Score of generating PM = " + scoreOrig);

            List<Graph> variants1 = ModelGenerator.generate(graph);

            //System.out.println("Size of list = " + variants1.size());
            //assertEquals(28, variants1.size());


            for (Graph aVariants1 : variants1) {
                Dag d = new Dag(aVariants1);
                BayesPm bpm = new BayesPm(d);

                BdeMetric bdemr = new BdeMetric(ds, bpm);
                double scorer = bdemr.score();
                //System.out.println(r);
                System.out.println("Score for above graph = " + scorer);
            }
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
        return new TestSuite(TestBDeMetric.class);
    }
}


