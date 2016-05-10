package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;

import java.io.*;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestKMedoids extends TestCase {
    public TestKMedoids(String name) {
        super(name);
    }

    public void test1() {
        try {
            DataParser parser = new DataParser();
            RectangularDataSet data = parser.parseTabular(new File("test_data/TestIonR.txt"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/cars.dat"));

            KMedoids cluster = KMedoids.randomClusters(4);
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
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D xyzData = data.getXyzData();

            KMedoids cluster = KMedoids.randomClusters(20);
            cluster.cluster(xyzData);
            System.out.println(cluster);

            data.printClusters(cluster.getClusters());
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

            KMedoids cluster = KMedoids.randomClusters(30);
            cluster.setMaxIterations(-1);
            cluster.cluster(timeSeries);
            System.out.println(cluster);

            ClusterUtils.printXyzExtents(xyzData, cluster.getClusters());
            ClusterUtils.writeOutPrototypesVertically(cluster.getPrototypes(),
                    "/home/jdramsey/proj/other/out/centroids.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestKMedoids.class);
    }
}
