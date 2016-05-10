package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestIca extends TestCase {
    public TestIca(String name) {
        super(name);
    }

    public void test() {
        
    }

    public void rtest1() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};
            int n = 54266;
            int c = 40;

            for (int s : subjects) {
                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + s + ".txt", n);

                String path = "/home/jdramsey/proj/other/fMRI/experiments/experiment20071022/r1_6" + s + "_ica/S.txt";
                DoubleMatrix2D S = ClusterUtils.loadMatrix(path, n, c, true, true);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();

                for (int _c = 0; _c < 40; _c++) {
                    List<Integer> cluster = new ArrayList<Integer>();

                    for (int i = 0; i < n; i++) {
                        if (S.get(i, _c) > 3) {
                            cluster.add(i);
                        }
                    }

                    clusters.add(cluster);
                }

                data.writeClustersToGnuPlotFile("/home/jdramsey/proj/other/fMRI/experiments/" +
                        "experiment20071022/points" + s + ".txt", clusters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest1a() {
        try {
            int[] subjects = new int[]{1};
            int n = 54266;
            int c = 40;

            for (int s : subjects) {
//                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + s + ".txt", n);

                String path = "/home/jdramsey/proj/other/fMRI/experiments/experiment20071022/r1_6" + s + "_ica/S.txt";
                DoubleMatrix2D S = ClusterUtils.loadMatrix(path, n, c, true, true);

                System.out.println(new Algebra().mult(S.viewDice(), S));

//                List<List<Integer>> clusters = new ArrayList<List<Integer>>();
//
//                for (int _c = 0; _c < 40; _c++) {
//                    List<Integer> cluster = new ArrayList<Integer>();
//
//                    for (int i = 0; i < n; i++) {
//                        if (S.get(i, _c) > 3) {
//                            cluster.add(i);
//                        }
//                    }
//
//                    clusters.add(cluster);
//                }

//                data.writeClustersToGnuPlotFile("/home/jdramsey/proj/other/fMRI/experiments/" +
//                        "experiment20071022/points" + s + ".txt", clusters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2() {
        try {
            int[] subjects = new int[]{1, 2, 3, 4, 8, 9};
            int n = 54266;
            int c = 40;

            for (int s : subjects) {
                FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_6" + s + ".txt", n);

                String path = "/home/jdramsey/proj/other/fMRI/experiments/experiment20071022/r1_6" + s + "_ica/";

                DoubleMatrix2D X = ClusterUtils.loadMatrix(path + "X.txt", n, c, true, true);
                DoubleMatrix2D W = ClusterUtils.loadMatrix(path + "W.txt", c, c, true, true);

                for (int i = 0; i < W.rows(); i++) {
                    for (int j = 0; j < W.rows(); j++) {
                        if (W.get(i, j) < .3) {
                            W.set(i, j, 0);
                        }
                    }
                }

                DoubleMatrix2D S = new Algebra().mult(X, W);

                List<List<Integer>> clusters = new ArrayList<List<Integer>>();

                for (int _c = 0; _c < 40; _c++) {
                    List<Integer> cluster = new ArrayList<Integer>();

                    for (int i = 0; i < n; i++) {
                        if (S.get(i, _c) > 3) {
                            cluster.add(i);
                        }
                    }

                    clusters.add(cluster);
                }

                data.writeClustersToGnuPlotFile("/home/jdramsey/proj/other/fMRI/experiments/" +
                        "experiment20071022/pointsb" + s + ".txt", clusters);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static Test suite() {
        return new TestSuite(TestIca.class);
    }
}
