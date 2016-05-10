package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.util.List;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestKMeans extends TestCase {
    public TestKMeans(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest1() {
        try {
            DataParser parser = new DataParser();
            RectangularDataSet data = parser.parseTabular(new File("test_data/TestIonR.txt"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/cars.dat"));

            KMeans cluster = KMeans.randomPoints(4);

            cluster.cluster(data.getDoubleData());
            System.out.println(cluster);

            for (int k = 0; k < cluster.getNumClusters(); k++) {
                System.out.println(k + ": " + cluster.getCluster(k));
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            data.convertToStandardized();
            data.convertToTopFractionThresholdByTime(0.10);
//            data.imposeTimeSeriesMinimum(400);
//            data.subtractBaseline();

            DoubleMatrix2D timeSeries = data.getTimeSeries();

//            System.out.println(timeSeries);

//            KMeans algorithm = KMeans.randomClusters(20);
            TryThis algorithm = new TryThis();
            algorithm.setEpsilon(60);
            algorithm.cluster(timeSeries);
//            System.out.println(algorithm);

            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", algorithm.getClusters());
//            ClusterUtils.printXyzExtents(xyzData, algorithm.getClusters());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2c() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
//            DoubleMatrix2D xyzData = file.getXyzData();

            KMeans algorithm = KMeans.randomClusters(15);
            algorithm.setMaxIterations(-1);
            algorithm.cluster(timeSeries);
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
            DoubleMatrix2D xyzData = data.getXyzData();

            KMeans algorithm = KMeans.randomClusters(15);
            algorithm.setMaxIterations(100);
            algorithm.cluster(timeSeries);
            System.out.println(algorithm);

            ClusterUtils.printXyzExtents(xyzData, algorithm.getClusters());

            List<List<Integer>> clusters = algorithm.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", clusters);

            ClusterUtils.writeOutPrototypesVertically(algorithm.getPrototypes(),
                    "/home/jdramsey/proj/other/out/centroids.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2e() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
//            file.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            long time0 = System.currentTimeMillis();

            KMeans algorithm = KMeans.randomClusters(30);
            algorithm.setMaxIterations(-1);
            algorithm.cluster(data.getTimeSeries());
            System.out.println(algorithm);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> clusters = algorithm.getClusters();
            ClusterUtils.printXyzExtents(data.getXyzData(), clusters);
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", clusters);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2f() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
//            file.restrictToTopFractionScores(file.getTimeSeries().viewColumn(0), 0.05);

            DoubleMatrix2D xyzData = data.getXyzData();
            System.out.println(data.getNumPoints() + " points");

            long time0 = System.currentTimeMillis();

            KMeans algorithm = KMeans.randomClusters(5);
            algorithm.setMaxIterations(-1);
            algorithm.cluster(xyzData);
            System.out.println(algorithm);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> clusters = algorithm.getClusters();
            ClusterUtils.printXyzExtents(xyzData, clusters);
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", clusters);
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

            KMeans algorithm = KMeans.randomPoints(25);
            algorithm.cluster(timeSeries);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> indices = algorithm.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest3a() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
            data.convertToTopFractionThresholdByTime(0.05);
//            data.standardizeTimeSeries();

            System.out.println(data.getNumPoints() + " points");

            DoubleMatrix2D timeSeries = data.getTimeSeries();

//            DoubleMatrix2D standardizedTimeSeries = ClusterUtils.standardizeData(timeSeries);

            long time0 = System.currentTimeMillis();

            KMeans algorithm = KMeans.randomPoints(25);
            algorithm.cluster(timeSeries);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> indices = algorithm.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);
//            ClusterUtils.writeOutPrototypesVertically(algorithm.getPrototypes(),
//                    "/home/jdramsey/prototypes.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testLoadPoints() {
        try {
            String[] s = new String[]{
                    "1", "2", "3", "4", "8", "9"
            };

            for (int j = 0; j < s.length; j++) {

                FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_6" + s[j] + ".txt", 54266);
                DoubleMatrix2D xyzData = data.getXyzData();

                PrintWriter out = new PrintWriter(new File("/home/jdramsey/r" + s[j] + "allpoints.txt"));

                for (int i = 0; i < xyzData.rows(); i++) {
                    double x = xyzData.get(i, 0);
                    double y = xyzData.get(i, 1);
                    double z = xyzData.get(i, 2);

                    out.println((z) + " " + (x) + " " + (y));
                }

                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtestLoadPoints2() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
                DoubleMatrix2D xyzData = data.getXyzData();

                PrintWriter out = new PrintWriter(new File("/home/jdramsey/r9allpoints.txt"));

                for (int i = 0; i < xyzData.rows(); i++) {
                    double x = xyzData.get(i, 0);
                    double y = xyzData.get(i, 1);
                    double z = xyzData.get(i, 2);

                    out.println((z) + " " + (x) + " " + (y));
                }

                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtestLoadNifti() {
        try {

            BufferedInputStream bufIn = new BufferedInputStream(
                    new FileInputStream(new File("/home/jdramsey/imgfiles20070911/" +
                    "bet-az63_ep2d_bold_ADI_MCFLRT8245_R1.img")));


//            String line;
//
//            while ((line = bufIn.readLine()) != null) {
//                System.out.println(line);
//                Thread.sleep(1);
//            }


            DataInputStream dataIn = new DataInputStream(bufIn);

            int d = 0;

            while (true) {
                d = dataIn.readInt();
                System.out.println(d);
                Thread.sleep(1);
            }

//            for (int i = 0; i < 1000; i++) {
//                System.out.println(dataIn.readDouble());
//            }


//            byte[] buf = new byte[1000];
//
//            ByteArrayInputStream stream = new ByteArrayInputStream(buf);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest10() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.printXyzExtents();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestKMeans.class);
    }
}

