package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4609 $ $Date: 2005-12-21 12:23:40 -0500 (Wed, 21 Dec
 *          2005) $
 */
public class TestVarianceTiebreaking extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestVarianceTiebreaking(String name) {
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

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3
     * --> X4. Should produce X1 -- X2, X1 -- X3, X2 --> X4, X3 --> X4.
     */
    public void testSearch1() {
        Dag dag = new Dag();

        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");

        dag.addNode(x);
        dag.addNode(y);
        dag.addNode(z);

        dag.addDirectedEdge(x, y);
        dag.addDirectedEdge(y, z);
        dag.addDirectedEdge(x, z);

        System.out.println(dag);


        SemPm semPm = new SemPm(dag);
        SemIm semIm = new SemIm(semPm);

        semIm.setParamValue(x, y, 1.0);
        semIm.setParamValue(y, z, -0.5);
        semIm.setParamValue(x, z, 0.5);

        System.out.println(semIm);

        RectangularDataSet data = semIm.simulateData(1000, false);
        CovarianceMatrix cov = new CovarianceMatrix(data);

//        System.out.println(variance(y, cov));

        System.out.println("x");
        System.out.println(variance(x, cov));
        System.out.println(conditionalVariance(x, y, cov));
        System.out.println(conditionalVariance(x, z, cov));

        System.out.println("y");
        System.out.println(variance(y, cov));
        System.out.println(conditionalVariance(y, x, cov));
        System.out.println(conditionalVariance(y, z, cov));

        System.out.println("z");
        System.out.println(variance(z, cov));
        System.out.println(conditionalVariance(z, x, cov));
        System.out.println(conditionalVariance(z, y, cov));

//        IndependenceTest test = new IndTestFisherZ(data, 0.5);
//
//        PcSearch pc = new PcSearch(test, new Knowledge());
//
//        Graph pattern = pc.search();
//
//        System.out.println("Pattern = " + pattern);

    }

    private double variance(Node x, CovarianceMatrix cov) {
        int index = cov.getVariableNames().indexOf(x.getName());
        return cov.getMatrix().get(index, index);
    }

    private double conditionalVariance(Node x, Node y, CovarianceMatrix cov) {
        int[] indices = new int[2];

        List<String> variableNames = cov.getVariableNames();
        indices[0] = variableNames.indexOf(x.getName());
        indices[1] = variableNames.indexOf(y.getName());

        // Extract submatrix of correlation cov using this index array.
        DoubleMatrix2D submatrix =
                cov.getMatrix().viewSelection(indices, indices);

        // Invert submatrix.
        if (new Algebra().rank(submatrix) != submatrix.rows()) {
            throw new IllegalArgumentException(
                    "Matrix singularity detected.");
        }

        submatrix = MatrixUtils.inverseC(submatrix);

        double d = submatrix.get(0, 0);

        return (d - 1) / d;
//
//        return d;

//        return 1./d;
    }

    private double conditionalVariance(Node x, Node y, Node z, CovarianceMatrix cov) {
        int[] indices = new int[3];

        List<String> variableNames = cov.getVariableNames();
        indices[0] = variableNames.indexOf(x.getName());
        indices[1] = variableNames.indexOf(y.getName());
        indices[2] = variableNames.indexOf(z.getName());

        // Extract submatrix of correlation cov using this index array.
        DoubleMatrix2D submatrix =
                cov.getMatrix().viewSelection(indices, indices);

        // Invert submatrix.
        if (new Algebra().rank(submatrix) != submatrix.rows()) {
            throw new IllegalArgumentException(
                    "Matrix singularity detected.");
        }

        submatrix = MatrixUtils.inverseC(submatrix);

        double d = submatrix.get(0, 0);

        return (d - 1) / d;

//        return d;

//        return 1./d;
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestVarianceTiebreaking.class);
    }
}
