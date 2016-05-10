package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4609 $ $Date: 2005-12-21 12:23:40 -0500 (Wed, 21 Dec
 *          2005) $
 */
public class TestAlessiosMethodSimple extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestAlessiosMethodSimple(String name) {
        super(name);
    }

    public void test1() {

        try {
            RectangularDataSet data = new DataParser().parseTabular(
                    new File("test_data/clustermaxvalues.dat"));

            AlessiosMethodSimple search = new AlessiosMethodSimple();

            Graph graph = search.search(data, 5);

            System.out.println(graph);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test2() {

        try {
            RectangularDataSet data = new DataParser().parseTabular(
                    new File("test_data/clustermaxvalues.dat"));

            AlessiosMethodSimple search = new AlessiosMethodSimple();

            Graph graph = search.search2(data, 5);

            System.out.println(graph);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestCpc.class);
    }
}