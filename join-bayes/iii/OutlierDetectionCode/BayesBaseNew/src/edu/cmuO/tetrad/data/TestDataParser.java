package edu.cmu.tetrad.data;

import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class TestDataParser extends TestCase {

    /**
     * Change the name of this constructor to match the name of the test class.
     */
    public TestDataParser(String name) {
        super(name);
    }

    public void test1() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("/local-scratch/BayesBase/src/sample_data/cheese.txt");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.WHITESPACE);
        parser.setIdsSupplied(true);
        parser.setIdLabel("Case");

        DataModel data = parser.parseTabular(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

    // Without the var names.
    public void test1b() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("sample_data/cheese2.txt");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.WHITESPACE);
        parser.setVarNamesSupplied(false);
        parser.setIdsSupplied(true);
        parser.setIdLabel(null);

        DataModel data = parser.parseTabular(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

    public void test2() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("test_data/g1set.txt");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.WHITESPACE);
        parser.setVarNamesSupplied(true);

        DataModel data = parser.parseTabular(chars);
        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

    // big
    public void rtest3() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("test_data/ad14.txt");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.COMMA);
        parser.setMissingValueMarker("*");

        DataModel data = parser.parseTabular(chars);
        TetradLogger.getInstance().removeOutputStream(System.out);
//        System.out.println(data);
    }

    public void test4() {
        TetradLogger.getInstance().addOutputStream(System.out);
        File file = new File("test_data/soybean.data");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setCommentMarker("//");
        parser.setDelimiter(DelimiterType.COMMA);
        parser.setIdsSupplied(true);
        parser.setIdLabel(null);
        parser.setMissingValueMarker("*");
        parser.setMaxIntegralDiscrete(10);

        DataModel data = parser.parseTabular(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

    public void test5() {
        TetradLogger.getInstance().addOutputStream(System.out);
        File file = new File("sample_data/pub.txt");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setCommentMarker("//");
        parser.setDelimiter(DelimiterType.WHITESPACE);
        parser.setIdsSupplied(true);
        parser.setIdLabel(null);
        parser.setMissingValueMarker("*");
        parser.setMaxIntegralDiscrete(10);

        DataModel data = parser.parseCovariance(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

    public void rtest6() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("test_data/dataparser/runSimulation.dat");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.WHITESPACE);

        DataModel data = parser.parseTabular(chars);

        System.out.println(data);

        TetradLogger.getInstance().removeOutputStream(System.out);

    }

    public void test7() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("test_data/dataparser/test2.dat");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.COMMA);
        parser.setCommentMarker("@");
        parser.setIdsSupplied(true);
        parser.setIdLabel("ID");
        parser.setQuoteChar('\'');

        DataModel data = parser.parseTabular(chars);

        System.out.println(data);

        TetradLogger.getInstance().removeOutputStream(System.out);

    }

    public void test8() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("test_data/dataparser/test3.dat");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.COMMA);
        parser.setCommentMarker("@");
        parser.setIdsSupplied(true);
        parser.setIdLabel(null);
        parser.setMissingValueMarker("Missing");

        DataModel data = parser.parseTabular(chars);

        System.out.println(data);

        TetradLogger.getInstance().removeOutputStream(System.out);

    }

    public void test9() {
        TetradLogger.getInstance().addOutputStream(System.out);

        File file = new File("test_data/dataparser/test4.dat");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setDelimiter(DelimiterType.WHITESPACE);

        DataModel data = parser.parseTabular(chars);

        System.out.println(data);

        TetradLogger.getInstance().removeOutputStream(System.out);

    }

    public void test10() {
        TetradLogger.getInstance().addOutputStream(System.out);
        File file = new File("sample_data/bollen.txt");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
//        DataModel data = parser.parseCovariance(chars);
        DataModel data = parser.parseTabular(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

    public void rtest11() {
        TetradLogger.getInstance().addOutputStream(System.out);
        File file = new File("test_data/covartest.dat");
        char[] chars = fileToCharArray(file);

        DataParser parser = new DataParser();
        parser.setCommentMarker("//");
        parser.setDelimiter(DelimiterType.WHITESPACE);
        parser.setIdsSupplied(true);
        parser.setIdLabel(null);
        parser.setMissingValueMarker("*");
        parser.setMaxIntegralDiscrete(10);

        DataModel data = parser.parseCovariance(chars);

        TetradLogger.getInstance().removeOutputStream(System.out);

        System.out.println(data);
    }

//    public void rtest12() {
//        try {
//            DataParser parser = new DataParser();
//            parser.setIdsSupplied(true);
//            parser.setIdLabel("ID");
//
//            RectangularDataSet data1 = parser.parseTabular(new File("/home/jdramsey/Desktop/temp/dataset_012.csv"));
//            RectangularDataSet data2 = parser.parseTabular(new File("/home/jdramsey/Desktop/temp/dataset_ABC.csv"));
//            RectangularDataSet data3 = parser.parseTabular(new File("/home/jdramsey/Desktop/temp/dataset_m10p1.csv"));
//
//
////            System.out.println(data3);
//
//            if (data1.getNumColumns() != data2.getNumColumns()) {
//                throw new IllegalArgumentException();
//            }
//
//            if (data1.getNumRows() != data2.getNumRows()) {
//                throw new IllegalArgumentException();
//            }
//
//
//            for (int i = 0; i < data1.getNumRows(); i++) {
//                for (int j = 0; j < data1.getNumColumns(); j++) {
////                    DiscreteVariable var1 = (DiscreteVariable) data1.getVariable(j);
////                    DiscreteVariable var2 = (DiscreteVariable) data2.getVariable(j);
//
//                    int i1 = data1.getInt(i, j);
//                    int i2 = data3.getInt(i, j);
//
//                    if (i1 != i2) {
//                        throw new IllegalArgumentException();
//                    }
//                }
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private char[] fileToCharArray(File file) {
        try {
            FileReader reader = new FileReader(file);
            CharArrayWriter writer = new CharArrayWriter();
            int c;

            while ((c = reader.read()) != -1) {
                writer.write(c);
            }

            return writer.toCharArray();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestDataParser.class);
    }
}
