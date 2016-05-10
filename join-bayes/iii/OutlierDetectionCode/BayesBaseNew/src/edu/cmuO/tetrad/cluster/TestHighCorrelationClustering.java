package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.List;

/**
 * @author Joseph Ramsey
 */
public class TestHighCorrelationClustering extends TestCase {
    public TestHighCorrelationClustering(String name) {
        super(name);
    }

    public void test() {

    }

    public void rtest1() {
        String path = "/home/jdramsey/points6.txt";
        System.out.println(new File(path).getAbsolutePath());

        try {
            FileReader reader = new FileReader(new File(path));
            BufferedReader in = new BufferedReader(reader);
            String line;
            int emptyLines = 0;

            while ((line = in.readLine()) != null) {
                if (line.equals("")) {
                    emptyLines++;
                }
            }

            System.out.println(emptyLines / 2 + 1);

        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            data.convertToMeanCentered();

            System.out.println(data.getTimeSeries().rows() + " points");

            HighCorrelationClustering algorithm = new HighCorrelationClustering(data.getXyzData());
            algorithm.setThreshold(0.80);
            algorithm.setClusterMin(10);
            algorithm.cluster(data.getTimeSeries());

            List<List<Integer>> indices = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/highcorrelation.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestHighCorrelationClustering.class);
    }
}
