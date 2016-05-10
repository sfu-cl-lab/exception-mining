package edu.cmu.tetrad.search;

import junit.framework.TestCase;
import junit.framework.Test;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.ColtDataSet;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 *          2005) $
 * @version $Revision: 4609 $ $Date: 2005-12-21 12:23:40 -0500 (Wed, 21 Dec
 */
public class TestFiddlingWithPatternResiduals extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestFiddlingWithPatternResiduals(String name) {
        super(name);
    }

    public void test1() {
        Dag dag = GraphUtils.createRandomDag(8, 0, 8, 3, 3, 3, false);

        System.out.println("DAG = " + dag);

        SemPm pm = new SemPm(dag);
        SemIm im = new SemIm(pm);

        RectangularDataSet data = im.simulateData(500, false);
//        RectangularDataSet data = simulateDataNonNormal(im, 500, new LogNormal());

        FiddlingWithPatternResiduals picker = new FiddlingWithPatternResiduals();
//        picker.search2(data);
        picker.search3(dag, data);
//        picker.search4();
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Fast for large simulations but hangs for cyclic models.
     *
     * @param sampleSize   > 0.
     * @param distribution
     * @return the simulated data set.
     */
    private RectangularDataSet simulateDataNonNormal(SemIm semIm, int sampleSize,
                                                     SamplingDistribution distribution) {
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
            for (int i = 0; i < tierOrdering.size(); i++) {
                int col = tierIndices[i];
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
        return null;//new TestSuite(ExploreLingamPattern.class);
    }
}