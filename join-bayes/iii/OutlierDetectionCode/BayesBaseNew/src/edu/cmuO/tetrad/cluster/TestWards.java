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
public class TestWards extends TestCase {
    public TestWards(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest1() {
        try {
            DataParser parser = new DataParser();
            RectangularDataSet data = parser.parseTabular(new File("test_data/TestIonR.txt"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/cars.dat"));

            System.out.println(data);

            Wards cluster = Wards.initialize();
            cluster.setVerbose(true);
            cluster.cluster(data.getDoubleData());
            System.out.println(cluster);
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

            Wards algorithm = Wards.initialize();
//            cluster.setMetric(new CorrelationMetric());

            algorithm.cluster(data2);
            System.out.println(algorithm);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
            data.imposeTimeSeriesMinimum(700);
            data.convertToMeanCentered();

            System.out.println("Number of points = " + data.getNumPoints());

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            DoubleMatrix1D scores = timeSeries.viewColumn(0);

            data.restrictToAboveThreshold(scores, 450);
            DoubleMatrix2D thresholdedXyz = data.getXyzData();
//
//            System.out.println(thresholdedData.rows() + " points");

            Wards algorithm = Wards.initialize();
            algorithm.setVerbose(true);
            algorithm.cluster(thresholdedXyz);
            System.out.println(algorithm);

            algorithm.setDepth(4);
            List<List<Integer>> clusters = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/points10.txt", clusters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestWards.class);
    }
}
