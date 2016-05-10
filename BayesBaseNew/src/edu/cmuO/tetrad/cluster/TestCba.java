package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import cern.colt.matrix.DoubleMatrix2D;

import java.util.List;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestCba extends TestCase {
    public TestCba(String name) {
        super(name);
    }

    public void test1() {
        
    }

    public void rtest1() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
//            file.subtractBaseline();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            Cba cba = new Cba(xyzData);
            cba.cluster(timeSeries);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            Cba algorithm = new Cba(xyzData);
            algorithm.cluster(timeSeries);

            List<List<Integer>> clusters = algorithm.getClusters();
            int min = Integer.MAX_VALUE;
            int max = 0;

            for (List<Integer> cluster : clusters) {
                int size = cluster.size();
                if (size > max) max = size;
                if (size < min) min = size;
            }

            System.out.println("Max size = " + max);
            System.out.println("Min size = " + min);

            System.out.println("Num clusters = " + clusters.size());

            data.writeClustersToGnuPlotFile("/home/jdramsey/cba.txt", clusters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void rtest3() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
//            file.subtractBaseline();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            Cba2 cba = new Cba2(xyzData);
            cba.findNeighbors(timeSeries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestCba.class);
    }
}
