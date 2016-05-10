package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemIm;

import java.util.List;

/**
 * @author Joseph Ramsey
 */
public class TestECast extends TestCase {
    public TestECast(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest23() {
        int n = 30;
        int p = 10;

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(data);

        ECast algorithm = new ECast();
        algorithm.cluster(data);

        System.out.println(algorithm.getClusters());
    }

    public void rtest3() {
        int n = 100;
        int p = 20;

        Graph graph = GraphUtils.createRandomDag(p, 0, 10, 4, 4, 4, false);

        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        DoubleMatrix2D data = semIm.simulateData(n, false).getDoubleData();

        ECast algorithm = new ECast();
        algorithm.cluster(data);
        System.out.println(algorithm.getClusters());
    }

    public void rtest4() {
        double[] data1 = new double[]{1, 1.2, .99, .89, 1.3, 5, 5.3, 4.9, 5.1, 4.8};
        DoubleMatrix1D data2 = new DenseDoubleMatrix1D(data1);
        DoubleMatrix2D data3 = new DenseDoubleMatrix2D(data2.size(), 1);
        data3.viewColumn(0).assign(data2);

        ECast algorithm = new ECast();
        algorithm.cluster(data3);
        System.out.println(algorithm.getClusters());
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
            DoubleMatrix2D timeSeries = data.getTimeSeries();

            data.restrictToTopFractionScores(timeSeries.viewColumn(0), .10);
            DoubleMatrix2D thresholdedXyz = data.getXyzData();

            System.out.println(thresholdedXyz.rows() + " points");

            ECast algorithm = new ECast();
            algorithm.cluster(thresholdedXyz);

            List<List<Integer>> indices = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/points7.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestECast.class);
    }
}
