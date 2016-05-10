package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.cluster.metrics.CorrelationLoss;

import java.io.*;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestNg extends TestCase {
    public TestNg(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest1() {
        try {
            DataParser parser = new DataParser();
            RectangularDataSet data = parser.parseTabular(new File("test_data/TestIonR.txt"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/cars.dat"));

            Ng algorithm = Ng.randomUnitsFromData(40);
            algorithm.setTMax(10000);
            algorithm.setMetric(new CorrelationLoss());
            algorithm.cluster(data.getDoubleData());
            System.out.println(algorithm.getClusters());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D xyzData = data.getXyzData();

            Ng algorithm = Ng.randomUnitsFromData(40);
            algorithm.cluster(xyzData);

            List<List<Integer>> clusters = algorithm.getClusters();
            ClusterUtils.printXyzExtents(xyzData, clusters);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2c() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            Ng algorithm = Ng.randomUnitsFromData(50);
            algorithm.setTMax(20000);
            algorithm.cluster(timeSeries);

            List<List<Integer>> clusters = algorithm.getClusters();
            ClusterUtils.printXyzExtents(xyzData, clusters);
            System.out.println(algorithm);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2d() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D timeSeries = data.getTimeSeries();

            Ng cluster = Ng.randomUnitsFromData(50);
            cluster.setTMax(20000);
            cluster.cluster(timeSeries);
            System.out.println(cluster);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2e() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            long time0 = System.currentTimeMillis();

            Ng algorithm = Ng.randomUnitsFromData(30);
            algorithm.cluster(timeSeries);

            long time1 = System.currentTimeMillis();
            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> clusters = algorithm.getClusters();
            ClusterUtils.printXyzExtents(xyzData, clusters);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest3() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            data.convertToStandardized();

            System.out.println(data.getNumPoints() + " points");

            DoubleMatrix2D timeSeries = data.getTimeSeries();

//            DoubleMatrix2D standardizedTimeSeries = ClusterUtils.standardizeData(timeSeries);

            long time0 = System.currentTimeMillis();

            Ng algorithm = Ng.randomUnitsFromData(25);
            algorithm.cluster(timeSeries);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> indices = algorithm.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestNg.class);
    }
}
