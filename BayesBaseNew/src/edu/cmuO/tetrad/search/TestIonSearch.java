package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradLogger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4609 $ $Date: 2005-12-21 12:23:40 -0500 (Wed, 21 Dec
 *          2005) $
 */
public class TestIonSearch extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestIonSearch(String name) {
        super(name);
    }

    @Override
	public void setUp() throws Exception {
        TetradLogger.getInstance().addOutputStream(System.out);
        TetradLogger.getInstance().setForceLog(true);
        TetradLogger.getInstance().setLogging(true);
    }


    @Override
	public void tearDown() {
        TetradLogger.getInstance().setForceLog(false);
        TetradLogger.getInstance().removeOutputStream(System.out);
    }

    /**
     * Tests the example trace used in the grant write up for the Ion Algo.
     */


    public void testExampleTrace1(){
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");

        Pag G1 = new Pag(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G1.addEdge(new Edge(Y, Z, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G1.addUnderlineTriple(new Triple(X, Y, Z));

        Pag G2 = new Pag(Arrays.asList(X, W, Z));
        G2.addEdge(new Edge(X, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G2.addEdge(new Edge(W, Z, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G2.addUnderlineTriple(new Triple(X, W, Z));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph)G2));

        List<Graph> pags = search.search();
        Set<Graph> outputPags = new HashSet<Graph>();
        for (Graph graph : pags) {
            outputPags.add(graph);
        }

        Pag O1 = new Pag(Arrays.asList(X, Y, W, Z));
        O1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O1.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O1.addEdge(new Edge(Z, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O1.addUnderlineTriple(new Triple(Y, W, Z));
        O1.addUnderlineTriple(new Triple(X, Y, W));

        Pag O2 = new Pag(Arrays.asList(X, W, Y, Z));
        O2.addEdge(new Edge(X, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O2.addEdge(new Edge(Y, Z, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.CIRCLE));
        O2.addUnderlineTriple(new Triple(X, W, Y));
        O2.addUnderlineTriple(new Triple(W, Y, Z));

        Set<Pag> expectedPags = new HashSet<Pag>();
        expectedPags.add(O2);
        expectedPags.add(O1);

        assertTrue(expectedPags.equals(outputPags));


    }


    public void testExampleTrace2(){
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");

        Pag G1 = new Pag(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Z, Y, Endpoint.CIRCLE, Endpoint.ARROW));

        Pag G2 = new Pag(Arrays.asList(Y, W));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph)G2));

                search.search();

        }

    public void testExampleTrace3() {
        Node X = new ContinuousVariable("X");
        Node Y = new ContinuousVariable("Y");
        Node W = new ContinuousVariable("W");
        Node Z = new ContinuousVariable("Z");
        Node T = new ContinuousVariable("T");

        Pag G1 = new Pag(Arrays.asList(X, Y, Z));
        G1.addEdge(new Edge(X, Y, Endpoint.CIRCLE, Endpoint.ARROW));
        G1.addEdge(new Edge(Y, Z, Endpoint.ARROW, Endpoint.CIRCLE));

        Pag G2 = new Pag(Arrays.asList(Y, W, T));
        G2.addEdge(new Edge(Y, W, Endpoint.CIRCLE, Endpoint.ARROW));
        G2.addEdge(new Edge(W, T, Endpoint.ARROW, Endpoint.CIRCLE));

        IonSearch search = new IonSearch(Arrays.asList(G1, (Graph)G2));

        search.search();

    }

    public void testExampleTraceCycle(){
        Node A = new ContinuousVariable("X1");
        Node B = new ContinuousVariable("X2");
        Node C = new ContinuousVariable("X3");
        Node D = new ContinuousVariable("X4");

        Pag G1 = new Pag(Arrays.asList(A, C, D));
        G1.addEdge(new Edge(A, C, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G1.addEdge(new Edge(C, D, Endpoint.CIRCLE, Endpoint.CIRCLE));

        Pag G2 = new Pag(Arrays.asList(B, C, D));
        G2.addEdge(new Edge(B, D, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G2.addEdge(new Edge(D, C, Endpoint.CIRCLE, Endpoint.CIRCLE));

        Pag G3 = new Pag(Arrays.asList(A, B, C));
        G3.addEdge(new Edge(A, B, Endpoint.CIRCLE, Endpoint.CIRCLE));
        G3.addEdge(new Edge(A, C, Endpoint.CIRCLE, Endpoint.CIRCLE));

        List<Graph> graphs = new ArrayList<Graph>();
        graphs.add(G1);
        graphs.add(G2);
        graphs.add(G3);

        IonSearch search = new IonSearch(graphs);

        List<Graph> pags = search.search();
    }


    public void rtestIonSearchOnData(){
        RectangularDataSet dataSet1 = null;
        RectangularDataSet dataSet2 = null;
        try {
            DataParser parser = new DataParser();
            dataSet1 = parser.parseTabular(new File("test_data/TestIonL.txt"));
            dataSet2 = parser.parseTabular(new File("test_data/TestIonR.txt"));
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        List<Graph> pags = new LinkedList<Graph>();

        IndependenceTest independenceTest = new IndTestCramerT(dataSet1, .05);
        FciSearch fci = new FciSearch(independenceTest, new Knowledge());
        pags.add(fci.search());

        independenceTest = new IndTestCramerT(dataSet2, .05);
        fci = new FciSearch(independenceTest, new Knowledge());
        pags.add(fci.search());

        IonSearch search = new IonSearch(pags);
        search.search();
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestIonSearch.class);
    }
}
