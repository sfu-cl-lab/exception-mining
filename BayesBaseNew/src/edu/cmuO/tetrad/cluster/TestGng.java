package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;

import java.io.*;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestGng extends TestCase {
    public TestGng(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest1() {
        try {
            DataParser parser = new DataParser();
            RectangularDataSet data = parser.parseTabular(new File("test_data/TestIonR.txt"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/cars.dat"));

            Gng algorithm = Gng.init();
            algorithm.setAgeMax(88);
            algorithm.setAlpha(0.5);
            algorithm.setBeta(0.0005);
            algorithm.setLambda(1000);
            algorithm.setMaxUnits(50);
            algorithm.cluster(data.getDoubleData());
            System.out.println(algorithm.getClusters());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest1a() {
        try {
            DataParser parser = new DataParser();
            DoubleMatrix2D data = parser.parseTabular(new File("test_data/TestIonR.txt")).getDoubleData();

            DoubleMatrix2D data2 = new DenseDoubleMatrix2D(data.rows() * 2, data.columns());

            for (int i = 0; i < data.rows(); i++) {
                for (int j = 0; j < data.columns(); j++) {
                    data2.set(i, j, data.get(i, j));
                    data2.set(i + data.rows(), j, data.get(i, j) + data.rows() + 5);
                }
            }

//            data2 = FmriDataLoader.subtractBaseline(data2);

            Gng algorithm = Gng.init();
            algorithm.setAgeMax(88);
            algorithm.setAlpha(0.5);
            algorithm.setBeta(0.0005);
            algorithm.setLambda(1000);
            algorithm.setMaxUnits(50);
//            cluster.setMetric(new CorrelationMetric());

            algorithm.cluster(data2);
            System.out.println(algorithm.getClusters());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2d() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D timeSeries = data.getTimeSeries();

            Gng cluster = Gng.init();
            cluster.setAgeMax(10);
            cluster.setAlpha(0.3);
            cluster.setBeta(0.03);
            cluster.setEpsilonB(0.01);
            cluster.setEpsilonN(0.001);
            cluster.setLambda(10000);
            cluster.setMaxUnits(50);
            cluster.setVerbose(true);
            cluster.cluster(timeSeries);
            System.out.println(cluster);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2e() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            System.out.println("Entire data set");
            data.printXyzExtents();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void rtest3() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
            data.imposeTimeSeriesMinimum(400);
            data.convertToStandardized();

            System.out.println(data.getNumPoints() + " points");

            DoubleMatrix2D timeSeries = data.getTimeSeries();

//            DoubleMatrix2D standardizedTimeSeries = ClusterUtils.standardizeData(timeSeries);

            long time0 = System.currentTimeMillis();

            Gng algorithm = Gng.init();
            algorithm.cluster(timeSeries);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            System.out.println(algorithm);


//            List<List<Integer>> indices = algorithm.getClusters();
//            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest4() {
         try {
             FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
//             data.convertToZStat();
             data.convertToStandardized();

             DoubleMatrix2D timeSeries = data.getTimeSeries();
             DoubleMatrix1D scores = timeSeries.viewColumn(0);

//            data.restrictToAboveThreshold(scores, 650);
             data.restrictToTopFractionScores(scores, 0.10);
             DoubleMatrix2D thresholdedXyz = data.getXyzData();

             System.out.println(thresholdedXyz.rows() + " points");

             Gng algorithm = Gng.init();
             algorithm.setAgeMax(10);
             algorithm.setAlpha(0.3);
             algorithm.setBeta(0.03);
             algorithm.setEpsilonB(0.01);
             algorithm.setEpsilonN(0.001);
             algorithm.setLambda(10000);
             algorithm.setMaxUnits(50);
             algorithm.cluster(thresholdedXyz);

             System.out.println(algorithm);

             List<List<Integer>> clusters = algorithm.getClusters();
//
             data.writeClustersToGnuPlotFile("/home/jdramsey/points7.txt", clusters);

         } catch (Exception e) {
             e.printStackTrace();
         }
     }

    public static Test suite() {
        return new TestSuite(TestGng.class);
    }
}
