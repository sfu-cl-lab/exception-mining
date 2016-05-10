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
public class TestTightCorrelations extends TestCase {
    public TestTightCorrelations(String name) {
        super(name);
    }
                     
    public void test() {

    }

    public void rtest4() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
//            file.subtractBaseline();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            TightCorrelations cba = new TightCorrelations(xyzData);
            cba.findTightCorrelations(timeSeries);

            List<List<Integer>> indices = cba.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest5() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            data.convertToStandardized();

            DoubleMatrix2D timeSeries = data.getTimeSeries();
            DoubleMatrix2D xyzData = data.getXyzData();

            TightCorrelations cba = new TightCorrelations(xyzData);
            cba.findTightCorrelations2(timeSeries);

            List<List<Integer>> indices = cba.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/points6.txt", indices);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Test suite() {
        return new TestSuite(TestTightCorrelations.class);
    }
}
