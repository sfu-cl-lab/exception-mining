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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.SplitDistribution;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6578 $ $Date: 2005-12-21 12:23:40 -0500 (Wed, 21 Dec
 *          2005) $
 */
public class TestLingamPattern extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestLingamPattern(String name) {
        super(name);
    }


    public void test1() {
        int sampleSize = 1000;

        Graph graph = new EdgeListGraph();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node w = new GraphNode("W");

        graph.addNode(x);
        graph.addNode(y);
        graph.addNode(w);

        graph.addDirectedEdge(x, y);
        graph.addDirectedEdge(y, w);
        graph.addDirectedEdge(x, w);

        List<SamplingDistribution> variableDistributions = new ArrayList<SamplingDistribution>();

        variableDistributions.add(new Uniform(-1, 1));
        variableDistributions.add(new Gaussian(0, 1));
        variableDistributions.add(new Gaussian(0, 1));


        SemPm semPm = new SemPm(graph);

        semPm.setCoefDistribution(new SplitDistribution(1.0, 2.0));
        SemIm semIm = new SemIm(semPm);

        System.out.println(semIm);

        RectangularDataSet dataSet = simulateDataNonNormal(semIm, sampleSize, variableDistributions);
        IndependenceTest test = new IndTestFisherZ(dataSet, 0.05);

        LingamPattern lingamPattern = new LingamPattern();
        lingamPattern.setAlpha(test.getAlpha());
        lingamPattern.setNumSamples(200);

        Graph estPattern = new Cpc(test, new Knowledge()).search();
//        Graph estPattern = new PcSearch(test1, new Knowledge()).search();
//        Graph estPattern = new GesSearch(dataSet).search();

        List<Dag> dags = SearchGraphUtils.getDagsInPatternMeek(estPattern, new Knowledge());

        LingamPattern.Result result = lingamPattern.search(dags, dataSet);

        for (int i = 0; i < result.getDags().size(); i++) {
            System.out.println("\n\nModel # " + (i + 1) + " # votes = " + result.getCounts().get(i));
            System.out.println(result.getDags().get(i));
        }

        System.out.println("Lingam:");

        Lingam lingam = new Lingam(0.05);
        GraphWithParameters _graph = lingam.lingam(dataSet);
        System.out.println(_graph.getGraph());
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Fast for large simulations but hangs for cyclic models.
     *
     * @param sampleSize    > 0.
     * @param distributions
     * @return the simulated data set.
     */
    private RectangularDataSet simulateDataNonNormal(SemIm semIm, int sampleSize,
                                                     List<SamplingDistribution> distributions) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = semIm.getSemPm().getVariableNodes();

        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            variables.add(var);
        }

        RectangularDataSet dataSet = new ColtDataSet(sampleSize, variables);

        // Create some index arrays to hopefully speed up the simulation.
        SemGraph graph = semIm.getSemPm().getGraph();
        List<Node> tierOrdering = graph.getTierOrdering();

        int[] tierIndices = new int[variableNodes.size()];

        for (int i = 0; i < tierIndices.length; i++) {
            tierIndices[i] = variableNodes.indexOf(tierOrdering.get(i));
        }

        int[][] _parents = new int[variables.size()][];

        for (int i = 0; i < variableNodes.size(); i++) {
            Node node = variableNodes.get(i);
            List<Node> parents = graph.getParents(node);

            for (Iterator<Node> j = parents.iterator(); j.hasNext();) {
                Node _node = j.next();

                if (_node.getNodeType() == NodeType.ERROR) {
                    j.remove();
                }
            }

            _parents[i] = new int[parents.size()];

            for (int j = 0; j < parents.size(); j++) {
                Node _parent = parents.get(j);
                _parents[i][j] = variableNodes.indexOf(_parent);
            }
        }

        // Do the simulation.
        for (int row = 0; row < sampleSize; row++) {
//            System.out.println(row);

            for (int i = 0; i < tierOrdering.size(); i++) {
                int col = tierIndices[i];
                SamplingDistribution distribution = distributions.get(col);

//                System.out.println(distribution);

                double value = distribution.drawSample();

                for (int j = 0; j < _parents[col].length; j++) {
                    int parent = _parents[col][j];
                    value += dataSet.getDouble(row, parent) *
                            semIm.getEdgeCoef().get(parent, col);
                }

                value += semIm.getMeans()[col];
                dataSet.setDouble(row, col, value);
            }
        }

        return dataSet;
    }


    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestPcSearch.class);
    }
}