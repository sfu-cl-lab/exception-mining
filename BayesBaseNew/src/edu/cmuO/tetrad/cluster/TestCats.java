package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemIm;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import java.util.List;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestCats extends TestCase {
    public TestCats(String name) {
        super(name);
    }

    public void test() {
        
    }

    public void rtest2() {
        int n = 30;
        int p = 10;

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(data);

        Cats cats = new Cats();
        cats.setClusteringAlgorithm(KMeans.randomPoints(15));
        cats.cluster(data);
    }

    public void rtest2a() {
        int n = 30;
        int p = 10;

        DoubleMatrix2D data = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(data);

        DoubleMatrix2D standardizedData = ClusterUtils.convertToStandardized(data);

        System.out.println(standardizedData);

        KMeans algorithm = KMeans.randomPoints(15);
        algorithm.cluster(standardizedData);

//        Cats cats = new Cats();
//        cats.setClusteringAlgorithm(KMeans.randomPoints(15));
//        cats.cluster(data);
    }

    public void rtest3() {
        int n = 500;
        int m = 400;

        Graph graph = GraphUtils.createRandomDag(n, 0, n, 4, 4, 4, false);

        SemPm semPm = new SemPm(graph);
        SemIm semIm = new SemIm(semPm);

        DoubleMatrix2D data = semIm.simulateData(n, false).getDoubleData().viewDice();

        Cats cats = new Cats();
        cats.setClusteringAlgorithm(KMeans.randomPoints(15));
        cats.cluster(data);
    }

    public void rtest4() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/fMRI/data/r1/r1_63.txt", 54266);
            System.out.println(data.getNumPoints() + " points");

            DoubleMatrix2D timeSeries = data.getTimeSeries();

            long time0 = System.currentTimeMillis();

            Cats algorithm = new Cats();

            KMeans kmeans = KMeans.randomPoints(25);
            algorithm.setClusteringAlgorithm(kmeans);

            algorithm.cluster(timeSeries);

            long time1 = System.currentTimeMillis();

            System.out.println((time1 - time0) / 1000 + "s");

            List<List<Integer>> indices = algorithm.getClusters();
            data.writeClustersToGnuPlotFile("/home/jdramsey/cats.txt", indices);

//            FmriFile.writeOutPrototypesVertically(algorithm.getPrototypes(),
//                    "C:/work/proj/other/out/centroids.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2d() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.convertToMeanCentered();
            DoubleMatrix2D timeSeries = data.getTimeSeries();

            Cats cats = new Cats();
            cats.setClusteringAlgorithm(KMeans.randomPoints(15));
            cats.cluster(timeSeries);
            System.out.println(cats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest5() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 54266);
            NumberFormat nf = new DecimalFormat("0.000000");

            PrintWriter out = new PrintWriter("/home/jdramsey/annotated63.txt");

            for (int i = 0; i < 400; i++) {
                out.println("@attribute t" + i + " real");
            }

            DoubleMatrix2D timeSeriesData = data.getTimeSeries();

            for (int i = 0; i < timeSeriesData.rows(); i++) {
                if (i % 1000 == 0) System.out.println(i);

                for (int j = 0; j < timeSeriesData.columns(); j++) {
                    out.print(nf.format(timeSeriesData.get(i, j)));

                    if (j < timeSeriesData.columns() - 1) {
                        out.print(",");
                    }
                }

                out.println();
            }

            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        return new TestSuite(TestCats.class);
    }
}
