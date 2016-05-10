package edu.cmu.tetrad.cluster;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cmu.tetrad.data.RegexTokenizer;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.search.fastica.FastICA;
import edu.cmu.tetrad.search.fastica.FastICAException;

import java.io.*;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * JUnit test for the regression classes.
 *
 * @author Frank Wimberly
 */
public class TestFastIca2 extends TestCase {
    public TestFastIca2(String name) {
        super(name);
    }

    public void test1C() {
        try {
            DoubleMatrix2D X = readFromFile("test_data/icaTestX.txt", 10, 5);
            DoubleMatrix2D wInit = readFromFile("test_data/icaTestWInit.txt", 3, 3);

            System.out.println("X");
            System.out.println("w.init " + wInit);

            FastIca fastIca = new FastIca(X.viewDice(), 3);
            fastIca.setWInit(wInit);
            fastIca.setAlgorithmType(FastIca.PARALLEL);
            fastIca.setFunction(FastIca.EXP);
            fastIca.setVerbose(true);

            FastIca.IcaResult r = fastIca.findComponents();

            System.out.println(r);

            DoubleMatrix2D s = r.getS();

            System.out.println(new Algebra().mult(s.viewDice(), s));

//            System.out.println("SA " + new Algebra().mult(r.getS(), r.getA()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rtest2() {
        int n = 54000;
        int p = 400;
        int m = 40;

        DoubleMatrix2D X = new DenseDoubleMatrix2D(n, p);
        ClusterUtils.initRandomly(X);

//        System.out.println("X" + X);

        FastIca fastIca = new FastIca(X, m);
        fastIca.setAlgorithmType(FastIca.PARALLEL);
        fastIca.setFunction(FastIca.EXP);
        fastIca.setVerbose(true);

        FastIca.IcaResult r = fastIca.findComponents();

//        System.out.println(r);

//        System.out.println("SA " + new Algebra().mult(r.getS(), r.getA()));
    }

    public void rtest3() {
        try {
            int n = 1000;
            int p = 400;
            int m = 40;

            DoubleMatrix2D X = new DenseDoubleMatrix2D(n, p);
            ClusterUtils.initRandomly(X);

//            System.out.println("X" + X);

            FastICA fastIca2 = new FastICA(X.viewDice().toArray(), m);

            DoubleMatrix2D S = new DenseDoubleMatrix2D(fastIca2.getICVectors());
            DoubleMatrix2D A = new DenseDoubleMatrix2D(fastIca2.getMixingMatrix());

//            System.out.println("SA " + new Algebra().mult(S.viewDice(), A.viewDice()));

        } catch (FastICAException e) {
            e.printStackTrace();
        }
    }

    public void test1D() {
        try {
            DoubleMatrix2D X = readFromFile("test_data/icaTestX.txt", 10, 5);
            DoubleMatrix2D wInit = readFromFile("test_data/icaTestWInit.txt", 3, 3);

            System.out.println(X);
            System.out.println(wInit);

            FastICA fastIca = new FastICA(X.viewDice().toArray(), 3);

            DoubleMatrix2D mixingMatrix = new DenseDoubleMatrix2D(fastIca.getMixingMatrix());

            System.out.println(mixingMatrix);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FastICAException e) {
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

    public static Test suite() {
        return new TestSuite(TestFastIca2.class);
    }
}
