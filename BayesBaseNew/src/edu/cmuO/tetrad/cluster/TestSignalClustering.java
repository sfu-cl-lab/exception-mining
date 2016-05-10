package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * @author Joseph Ramsey
 */
public class TestSignalClustering extends TestCase {
    public TestSignalClustering(String name) {
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void rtest2b() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            data.convertToMeanCentered();

            System.out.println(data.getTimeSeries().rows() + " points");

            SignalClustering algorithm = new SignalClustering(data.getXyzData());
            algorithm.setThreshold(0.5);
            algorithm.setClusterMin(10);
            algorithm.cluster(data.getTimeSeries());

            List<List<Integer>> indices = algorithm.getClusters();

            data.writeClustersToGnuPlotFile("/home/jdramsey/signalclustering.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestSignalClustering.class);
    }
}
                    