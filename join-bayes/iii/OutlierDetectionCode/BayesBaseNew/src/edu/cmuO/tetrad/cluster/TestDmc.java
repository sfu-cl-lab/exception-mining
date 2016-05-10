package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.*;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;
import edu.cmu.tetrad.cluster.metrics.Dissimilarity;


/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestDmc extends TestCase {
    public TestDmc(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest2() {
        int n = 30;
        int p = 10;

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(data);
        System.out.println(data);

//        DoubleMatrix2D zStat = ClusterUtils.convertToZStat(data);
//        System.out.println(zStat);
//        System.out.println(new Algebra().mult(zStat, zStat.viewDice()));

        data = ClusterUtils.convertToStandardized(data);
        System.out.println(data);
        System.out.println(new Algebra().mult(data, data.viewDice()));
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            data.convertToStandardized();

            DoubleMatrix2D timeSeries = data.getTimeSeries();

            List<Integer> thresholdedrows = ClusterUtils.getAboveThresholdRows(
                    timeSeries.viewColumn(0), 1.6449, timeSeries);

            DoubleMatrix2D xyzData = data.getXyzData();
            DoubleMatrix2D thresholdedXyz = ClusterUtils.restrictToRows(xyzData, thresholdedrows);

            System.out.println(thresholdedXyz.rows() + " points");

            Dmc algorithm = Dmc.rjSearch();
            algorithm.setVerbose(true);
            algorithm.setDensityTreshold(20);
            algorithm.setNeighborDistance(4);
            algorithm.cluster(thresholdedXyz);
//            System.out.println(cluster);

            List<List<Integer>> indices = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/points7.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2b2() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            DoubleMatrix1D scores = xyzData.viewColumn(0).like();
            Dissimilarity metric = new SquaredErrorLoss();

            for (int i = 0; i < timeSeries.rows(); i++) {
                scores.set(i, metric.dissimilarity(timeSeries.viewRow(i),
                        new DenseDoubleMatrix1D(timeSeries.columns())));
            }

//            file.restrictToTopFractionScores(scores, 0.05);
            data.restrictToAboveThreshold(scores, 2500);

            System.out.println(data.getNumPoints() + " points");

            DoubleMatrix2D thresholdedXyz = data.getXyzData();
            Dmc algorithm = Dmc.rjSearch();
            algorithm.setDensityTreshold(40);
            algorithm.setNeighborDistance(3);
            algorithm.cluster(thresholdedXyz);
            System.out.println(algorithm);
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

    public static Test suite() {
        return new TestSuite(TestDmc.class);
    }
}
