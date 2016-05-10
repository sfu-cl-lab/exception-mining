package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.data.*;

import java.io.*;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestIcc extends TestCase {
    public TestIcc(String name) {
        super(name);
    }

    public void test1() {
        try {
            DataParser parser = new DataParser();
            RectangularDataSet data = parser.parseTabular(new File("test_data/TestIonR.txt"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/cars.dat"));

            Icc cluster = new Icc(data.getDoubleData());
            cluster.cluster();

//            for (int k = 0; k < cluster.getNumClusters(); k++) {
//                System.out.println(k + ": " + cluster.getCluster(k));
//            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test1C() {
        try {
            DoubleMatrix2D X = readFromFile("test_data/icaTestX.txt", 5, 5);
            DoubleMatrix2D wInit = readFromFile("test_data/icaTestWInit.txt", 3, 3);

            System.out.println(X);
            System.out.println(wInit);

            FastIca fastIca = new FastIca(X, 3);
            fastIca.setWInit(wInit);
            fastIca.setVerbose(true);

            FastIca.IcaResult w = fastIca.findComponents();

            System.out.println(w.getS());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DoubleMatrix2D readFromFile(String s, int rows, int columns)
            throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(s));
        DoubleMatrix2D m = new DenseDoubleMatrix2D(rows, columns);

        for (int i = 0; i < rows; i++) {
            String line = in.readLine();
            RegexTokenizer tokenizer = new RegexTokenizer(line,
                    DelimiterType.WHITESPACE.getPattern(), '\"');

            for (int j = 0; j < columns; j++) {
                m.set(i, j, Double.parseDouble(tokenizer.nextToken()));
            }
        }

        return m;
    }

    public void rtest2c() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();

            DoubleMatrix2D timeSeries = data.getTimeSeries();

            KMeans cluster = KMeans.randomClusters(15);
            cluster.setMaxIterations(-1);
            cluster.cluster(timeSeries);
            System.out.println(cluster);

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

            Icc cluster = new Icc(timeSeries);
//            cluster.setMaxIterations(-1);
            cluster.cluster();
            System.out.println(cluster);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rtest2e() {
        try {
            FmriData data = FmriData.loadDataSetHead("../../other/r1/r1_63.txt", 400);
            data.imposeTimeSeriesMinimum(400);
            data.convertToMeanCentered();
            data.printXyzExtents();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Test suite() {
        return new TestSuite(TestIcc.class);
    }
}
