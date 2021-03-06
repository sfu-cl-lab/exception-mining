package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemIm;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import java.util.List;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestAp extends TestCase {
    public TestAp(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest2() {
        int n = 30;
        int p = 10;

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(data);

        Ap cluster = new Ap();
        cluster.cluster(data);
    }

    public void rtest3() {
        int n = 100;
        int p = 20;

        Graph graph = GraphUtils.createRandomDag(p, 0, 10, 4, 4, 4, false);

        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        DoubleMatrix2D data = semIm.simulateData(n, false).getDoubleData();

        Ap algorithm = new Ap();
        algorithm.cluster(data);
    }

    public void rtest4() {
        double[] data1 = new double[]{1, 1.2, .99, .89, 1.3, 5, 5.3, 4.9, 5.1, 4.8};
        DoubleMatrix1D data2 = new DenseDoubleMatrix1D(data1);
        DoubleMatrix2D data3 = new DenseDoubleMatrix2D(data2.size(), 1);
        data3.viewColumn(0).assign(data2);

        Ap algorithm = new Ap();
        algorithm.cluster(data3);
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
            DoubleMatrix2D timeSeries = data.getTimeSeries();

            DoubleMatrix1D scores = timeSeries.viewColumn(0);

            data.restrictToTopFractionScores(scores, .05);
            DoubleMatrix2D thresholdedXyz = data.getXyzData();

            System.out.println(thresholdedXyz.rows() + " points");

            Ap algorithm = new Ap();
            algorithm.cluster(thresholdedXyz);

            List<List<Integer>> indices = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/points7.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestAp.class);
    }
}
