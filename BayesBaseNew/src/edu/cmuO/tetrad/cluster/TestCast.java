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
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;

import java.util.List;

/**
 * @author Joseph Ramsey
 */
public class TestCast extends TestCase {
    public TestCast(String name) {
        super(name);
    }

    public void test2() {
        int n = 30;
        int p = 10;

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(data);

        Cast algorithm = new Cast();
        algorithm.setThreshold(2.0);
        algorithm.cluster(data);

        System.out.println(algorithm.getClusters());
    }

    public void test3() {
        int n = 100;
        int p = 20;

        Graph graph = GraphUtils.createRandomDag(p, 0, 10, 4, 4, 4, false);

        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        DoubleMatrix2D data = semIm.simulateData(n, false).getDoubleData();

        Cast algorithm = new Cast();
        algorithm.setThreshold(10);
        algorithm.cluster(data);
        System.out.println(algorithm.getClusters());
    }

    public void test4() {
        double[] data1 = new double[]{1, 1.2, .99, .89, 1.3, 5, 5.3, 4.9, 5.1, 4.8};
        DoubleMatrix1D data2 = new DenseDoubleMatrix1D(data1);
        DoubleMatrix2D data3 = new DenseDoubleMatrix2D(data2.size(), 1);
        data3.viewColumn(0).assign(data2);

        Cast algorithm = new Cast();
        algorithm.setThreshold(.7);
        algorithm.cluster(data3);
        System.out.println(algorithm.getClusters());
    }

    public void test2b() {
        try {
//            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
//            FmriData data = FmriData.loadDataSetHead("test_data/sub001_run001_bold.csv", 54266);
//            FmriData data = FmriData.loadNifti1Dataset("test_data/sub001_run001_bold.nii.gz", 200);
            FmriData data = FmriData.loadNifti1Dataset("test_data/cluster_mask_zstat1.nii.gz", 1);

            data.convertToStandardized();

            DoubleMatrix2D timeSeries = data.getTimeSeries();

            System.out.println(timeSeries.rows() + " points");

            Cast algorithm = new Cast();
            algorithm.setDissimilarity(new SquaredErrorLoss());
            algorithm.setThreshold(0.5);
            algorithm.cluster(timeSeries);

            List<List<Integer>> indices = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestCast.class);
    }
}
