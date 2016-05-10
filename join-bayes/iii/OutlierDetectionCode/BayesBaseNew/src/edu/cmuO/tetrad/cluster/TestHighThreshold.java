package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestHighThreshold extends TestCase {
    public TestHighThreshold(String name) {
        super(name);
    }

    public void test() {
        
    }

    public void rtest1() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};

            for (int n : subjects) {
                FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_6" + n + ".txt", 54266);
                data.convertToStandardized();

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();

                for (int i = 0; i < 400; i++) {
                    DoubleMatrix2D timeSeries = data.getTimeSeries();
                    clusters.add(ClusterUtils.getTopFractionScoreRows(
                            timeSeries.viewColumn(i), 0.10, timeSeries));
                }

                data.writeClustersToGnuPlotFile("/home/jdramsey/fMRI.output/experiments/" +
                        "experiment20071013/highthresholds" + n + ".txt",
                        clusters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void rtest2() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};

            for (int n : subjects) {
                System.out.println("Subject " + n);

                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + n + ".txt", 54266);
//                data.convertToMeanCentered();
                data.convertToStandardized();

                DoubleMatrix2D timeSeries = data.getTimeSeries();
//                double threshold
//                        = ClusterUtils.getTopFactionThresholdOverall(timeSeries, 0.10);
                double threshold = 1.6449;

                System.out.println("threshold = " + threshold);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();

                for (int i = 0; i < 400; i++) {
                    clusters.add(ClusterUtils.getAboveThresholdRows(
                            timeSeries.viewColumn(i), threshold, timeSeries));
                }

//                data.writeClustersToGnuPlotFile("/home/jdramsey/thresholds" + n + ".txt",
//                        clusters);
                data.writeClustersToGnuPlotFile("../../other/fMRI/experiments/" +
                        "experiment20071018/highthresholds" + n + ".txt",
                        clusters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest3() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};

            for (int n : subjects) {
                System.out.println("Subject " + n);

                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + n + ".txt", 54266);
                data.convertToSeriesZScores();

                DoubleMatrix2D timeSeries = data.getTimeSeries();
//                double threshold
//                        = ClusterUtils.getTopFactionThresholdOverall(timeSeries, 0.10);
                double threshold = 3.0;

                System.out.println("threshold = " + threshold);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();

                for (int i = 1; i < 400; i++) {
                    clusters.add(ClusterUtils.getSignificantlyChangingRows(
                            timeSeries, i, threshold));
                }

//                data.writeClustersToGnuPlotFile("/home/jdramsey/thresholds" + n + ".txt",
//                        clusters);
                data.writeClustersToGnuPlotFile("../../other/fMRI/experiments/" +
                        "experiment20071018a/zstatthresholddiff3.0." + n + ".txt",
                        clusters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trying to color upward shifts as red, downward shifts as blue. OK,
     * this doesn't work, because gnuplot (despite the docs) apparently doesn't
     * have turned on the ability to read colors from the 4th column of the
     * data.
     */
    public void rtest4() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};

            for (int n : subjects) {
                System.out.println("Subject " + n);

                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + n + ".txt", 54266);
                data.convertToSeriesZScores();

                DoubleMatrix2D timeSeries = data.getTimeSeries();

                double threshold = 1.96;
                System.out.println("threshold = " + threshold);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();
                List<List<Integer>> colors = new ArrayList<List<Integer>>();

                for (int j = 1; j < 400; j++) {
                    List<Integer> cluster = new ArrayList<Integer>();
                    List<Integer> clusterColors = new ArrayList<Integer>();

                    for (int i = 0; i < 54266; i++) {
                        if (ClusterUtils.isSignificantlyChangingUp(
                                timeSeries, i, j, threshold)) {
                            cluster.add(i);
                            clusterColors.add(0xFF0000);
                        }

                        if (ClusterUtils.isSignificantlyChangingDown(
                                timeSeries, i, j, threshold)) {
                            cluster.add(i);
                            clusterColors.add(0x0000FF);
                        }
                    }

                    clusters.add(cluster);
                    colors.add(clusterColors);
                }

                data.writeClustersToGnuPlotFile("../../other/fMRI/experiments/" +
                        "experiment20071023/zstatupdowndiffs." + n + ".txt",
                        clusters, colors);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest5() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};

            for (int n : subjects) {
                System.out.println("Subject " + n);

                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + n + ".txt", 54266);
                data.convertToSeriesZScores();

                DoubleMatrix2D timeSeries = data.getTimeSeries();

                double threshold = 1.96;
                System.out.println("threshold = " + threshold);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();
                List<List<Integer>> colors = new ArrayList<List<Integer>>();

                for (int j = 0; j < 400; j++) {
                    List<Integer> cluster = new ArrayList<Integer>();
                    List<Integer> clusterColors = new ArrayList<Integer>();

                    for (int i = 0; i < 54266; i++) {
                        if (timeSeries.get(i, j) > threshold) {
                            cluster.add(i);
                            clusterColors.add(0xFF0000);
                        }

//                        if (Math.abs(timeSeries.get(i, j)) < 0.1) {
//                            cluster.add(i);
//                            clusterColors.add(0x00FF00);
//                        }

                        if (timeSeries.get(i, j) < -threshold) {
                            cluster.add(i);
                            clusterColors.add(0x0000FF);
                        }
                    }

                    clusters.add(cluster);
                    colors.add(clusterColors);
                }

                data.writeClustersToGnuPlotFile("../../other/fMRI/experiments/" +
                        "experiment20071023b/zstatextremes." + n + ".txt",
                        clusters, colors);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest6() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};

            for (int n : subjects) {
                System.out.println("Subject " + n);

                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + n + ".txt", 54266);
                data.convertToMeanCentered();

                DoubleMatrix2D timeSeries = data.getTimeSeries();

                double threshold = 3;
                System.out.println("threshold = " + threshold);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();
                List<List<Integer>> colors = new ArrayList<List<Integer>>();

                for (int j = 1; j < 400; j++) {
                    List<Integer> cluster = new ArrayList<Integer>();
                    List<Integer> clusterColors = new ArrayList<Integer>();

                    for (int i = 0; i < 54266; i++) {
                        if (ClusterUtils.isSignificantlyChangingUp(
                                timeSeries, i, j, threshold)) {
                            cluster.add(i);
                            clusterColors.add(0xFF0000);
                        }

                        if (ClusterUtils.isSignificantlyChangingDown(
                                timeSeries, i, j, threshold)) {
                            cluster.add(i);
                            clusterColors.add(0x0000FF);
                        }
                    }

                    clusters.add(cluster);
                    colors.add(clusterColors);
                }

                data.writeClustersToGnuPlotFile("../../other/fMRI/experiments/" +
                        "experiment20071024/meandiffupdowndiffs/meandiffupdowndiffs." + n + ".txt",
                        clusters, colors);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Test suite() {
        return new TestSuite(TestHighThreshold.class);
    }
}
