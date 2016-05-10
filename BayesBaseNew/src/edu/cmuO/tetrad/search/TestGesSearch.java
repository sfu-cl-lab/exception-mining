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

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphConverter;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6059 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public class TestGesSearch extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGesSearch(String name) {
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
        // Blank to keep the automatic JUnit runner happy.
    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3
     * --> X4. Should produce X1 -- X2, X1 -- X3, X2 --> X4, X3 --> X4.
     */
    public void rtestSearch1() {
        checkSearch("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void rtestSearch2() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void rtestSearch3() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        knowledge.setEdgeForbidden("C", "B", true);

        checkWithKnowledge("A-->B,C-->B,B-->D", "A---B,C---A,B-->C,C-->D,A-->D",
                knowledge);
    }

    public void rtestSearch4() {
        int numVars = 30;
        int numEdges = 30;
        int sampleSize = 500;
        boolean latentDataSaved = false;

        Dag trueGraph = GraphUtils.createRandomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

//        SemPm bayesPm = new SemPm(trueGraph);
//        SemIm bayesIm = new SemIm(bayesPm);
//        RectangularDataSet dataSet = bayesIm.simulateData(sampleSize);

        BayesPm bayesPm = new BayesPm(trueGraph);
        MlBayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
        RectangularDataSet dataSet = bayesIm.simulateData(sampleSize, latentDataSaved);

        GesSearch gesSearch = new GesSearch(dataSet, trueGraph);
        gesSearch.setStructurePrior(0.1);
        gesSearch.setSamplePrior(10);

        // Run search
        Graph resultGraph = gesSearch.search();

        // PrintUtil out problem and graphs.
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
    }

    public void rtestSearch5() {
        int numVars = 20;
        int numEdges = 40;
//        int sampleSize = 1000;

        Dag trueGraph = GraphUtils.createRandomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

//        SemPm bayesPm = new SemPm(trueGraph);
//        SemIm bayesIm = new SemIm(bayesPm);

        for (int sampleSize = 500; sampleSize < 15500; sampleSize += 500) {
            System.out.println("********** SAMPLE SIZE = " + sampleSize);

//            RectangularDataSet dataSet = bayesIm.simulateData(sampleSize);

            BayesPm bayesPm = new BayesPm(trueGraph);
            BayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
            RectangularDataSet dataSet = bayesIm.simulateData(sampleSize, false);

            GesSearch gesSearch = new GesSearch(dataSet, trueGraph);

            // Run search
            Graph resultGraph = gesSearch.search();

            // PrintUtil out problem and graphs.
            System.out.println("\nResult graph:");
            System.out.println(resultGraph);
        }
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkSearch(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        RectangularDataSet dataSet = semIM.simulateData(500, false);

        // Set up search.
        GesSearch gesSearch = new GesSearch(dataSet, graph);
//        gesSearch.setMessageOutputted(true);

        // Run search
        Graph resultGraph = gesSearch.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkWithKnowledge(String inputGraph, String outputGraph,
                                    Knowledge knowledge) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        RectangularDataSet dataSet = semIM.simulateData(1000, false);

        // Set up search.
        GesSearch gesSearch = new GesSearch(dataSet);
        gesSearch.setKnowledge(knowledge);
//        gesSearch.setMessageOutputted(true);

        // Run search
        Graph resultGraph = gesSearch.search();

        // PrintUtil out problem and graphs.
        System.out.println(knowledge);
        System.out.println("Input graph:");
        System.out.println(graph);
        System.out.println("Result graph:");
        System.out.println(resultGraph);

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestGesSearch.class);
    }
}


