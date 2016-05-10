package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetradapp.model.GraphComparison;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.List;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4609 $ $Date: 2005-12-21 12:23:40 -0500 (Wed, 21 Dec
 *          2005) $
 */
public class TestAcpc extends TestCase {
    private Object Acpcsearch;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestAcpc(String name) {
        super(name);
    }

//    public void setUp() throws Exception {
//        TetradLogger.getInstance().addOutputStream(System.out);
//        TetradLogger.getInstance().setForceLog(true);
//    }
//
//
//    public void tearDown() {
//        TetradLogger.getInstance().setForceLog(false);
//        TetradLogger.getInstance().removeOutputStream(System.out);
//    }

    /**
     * Runs the PC algorithm on the graph X1 --> X2, X1 --> X3, X2 --> X4, X3
     * --> X4. Should produce X1 -- X2, X1 -- X3, X2 --> X4, X3 --> X4.
     */
    public void testSearch1() {
        checkSearch("X1-->X2,X1-->X3,X2-->X4,X3-->X4",
                "X1---X2,X1---X3,X2-->X4,X3-->X4");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void testSearch2() {
        checkSearch("A-->D,A-->B,B-->D,C-->D,D-->E",
                "A-->D,A---B,B-->D,C-->D,D-->E");
    }

    /**
     * This will fail if the orientation loop doesn't continue after the first
     * orientation.
     */
    public void testSearch3() {
        Knowledge knowledge = new Knowledge();
        knowledge.setEdgeForbidden("B", "D", true);
        knowledge.setEdgeForbidden("D", "B", true);
        knowledge.setEdgeForbidden("C", "B", true);

        checkWithKnowledge("A-->B,C-->B,B-->D", "A-->B,C-->B,A-->D,C-->D",
                knowledge);
    }

    public void testSearch4() {
        try {
            File file = new File("test_data/cpcmbout.txt");
            PrintWriter out = new PrintWriter(file);

            System.out.println("acpc\tpc\tcpc\tges\tmmhc\tmbfs");
            out.println("cpcmb\tpc\tcpc\tges\tmmhc");
            out.flush();

            int numCounts = 4;
            int[] countsForAcpc = new int[numCounts + 1];

            for (int run = 0; run < 30; run++) {
                Graph graph = GraphUtils.createRandomDag(30, 0, 30, 4, 4, 4, false);

//                System.out.println(graph);

//                Graph pattern = SearchGraphUtils.patternFromDag(graph);
                Graph pattern = new PcSearch(new IndTestDSep(graph), new Knowledge()).search();

//                BayesPm pm = new BayesPm(new Dag(graph));
//                BayesIm im = new MlBayesIm(pm, MlBayesIm.RANDOM);
//                RectangularDataSet data = im.simulateData(1000, false);
//                IndependenceTest test = new IndTestChiSquare(data, 0.05);

                SemPm semPm = new SemPm(graph);
                SemIm semIm = new SemIm(semPm);
                RectangularDataSet data = semIm.simulateData(1000, false);

//                data = ColtDataSet.makeContinuousData(data.getVariables(),
//                        DataUtils.standardizeData(data.getDoubleData()));

//                CovarianceMatrix cov = new CovarianceMatrix(data);
//                DoubleMatrix2D m = cov.getMatrix();
//
//                for (int i = 0; i < m.rows(); i++) {
//                    System.out.println(m.get(i, i) + "\t");
//                }

                IndependenceTest test = new IndTestFisherZ(data, .00005);
                IndependenceTest test2 = new IndTestFisherZ(data, .05);
//                IndependenceTest test = new IndTestFisherZBootstrap(data, .005, 15, 1000);
                Knowledge knowledge = new Knowledge();

                GraphSearch search = new Acpc(test, knowledge);
                ((Acpc)search).setTrueGraph(graph);
                ((Acpc)search).setSemIm(semIm);
                ((Acpc)search).setMaxAdjacencies(10);
                Graph result = ((Acpc)search).search();

                int value = getValue(pattern, result);

                if (value < numCounts) countsForAcpc[value]++;
                else countsForAcpc[numCounts]++;

                printValue(pattern, result, out);

                search = new PcSearch(test2, knowledge);
                result = search.search();

                printValue(pattern, result, out);

//                search = new PcPattern(test, knowledge);
//                result = search.search();
//
//                printValue(pattern, result, out);
//
//                search = new PcdSearch(test, knowledge);
//                result = search.search();
//
//                printValue(pattern, result, out);

//                search = new PcStar(test, knowledge);
//                result = search.search();                      2
//
//                printValue(pattern, result, out);

                search = new Cpc(test2, knowledge);
                result = search.search();

                printValue(pattern, result, out);

//                search = new Npc(test, knowledge);
//                result = search.search();
//
//                printValue(pattern, result, out);

                search = new GesSearch(data);
                result = search.search();

                printValue(pattern, result, out);

                search = new VanderbiltMmhc(test2, -1);
                result = search.search();

                printValue(pattern, result, out);

                search = new MbFanSearch(test2, -1);
                result = search.search();

                printValue(pattern, result, out);

                out.println();
                System.out.println();
                out.flush();

            }

            System.out.println("\n\nCounts for ACPC:");

            for (int i = 0; i < numCounts; i++) {
                System.out.println("i = " + i + ": " + countsForAcpc[i]);
            }

            System.out.println("Remainder: " + countsForAcpc[numCounts]);

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void printValue(Graph pattern, Graph result, PrintWriter out) {
        printValue1(pattern, result, out);
//        printValue2(pattern, result, out);
    }

    private void printValue1(Graph pattern, Graph result, PrintWriter out) {
        int value = getValue(pattern, result);
        System.out.print(value + "\t");
        out.print(value + "\t");
        out.flush();
    }

    private void printValue2(Graph pattern, Graph result, PrintWriter out) {
        GraphComparison comparison = new GraphComparison(pattern, result);
        int fp = comparison.getEdgesAdded().size();
        int fn = comparison.getEdgesRemoved().size();
//        int value = comparison.getEdgesAdded().size() + comparison.getEdgesRemoved().size();

//        List<Edge> edges = comparison.getEdgesReorientedTo();
//
//        for (Edge edge : new LinkedList<Edge>(edges)) {
//            if (Edges.isUndirectedEdge(edge)) {
//                edges.remove(edge);
//            }
//        }
//
//        edges.addAll(comparison.getEdgesAdded());
//
//        int value = edges.size();

        String s = "[" + fp + "," + fn + "]\t";
        System.out.print(s);
        out.print(s + "\t");
        out.flush();
    }

    private int getValue(Graph pattern, Graph result) {
        GraphComparison comparison = new GraphComparison(pattern, result);
//        int value = comparison.getEdgesAdded().size();
//        int value = comparison.getEdgesRemoved().size();
        int value = comparison.getEdgesAdded().size() + comparison.getEdgesRemoved().size();

//        List<Edge> edges = comparison.getEdgesReorientedTo();
//
//        for (Edge edge : new LinkedList<Edge>(edges)) {
//            if (Edges.isUndirectedEdge(edge)) {
//                edges.remove(edge);
//            }
//        }
//
//        edges.addAll(comparison.getEdgesAdded());
//
//        int value = edges.size();

        return value;
    }

    public void test8() {
        DoubleMatrix2D m = new DenseDoubleMatrix2D(2, 2);
        
        m.set(0, 0, 2);
        m.set(0, 1, .2);
        m.set(1, 0, .2);
        m.set(1, 1, 106.0);

        DoubleMatrix2D m2 = new Algebra().inverse(m);

        System.out.println(m2);

    }


    public void test7() {
        int numVars = 6;
        int numEdges = 6;

        Dag trueGraph = GraphUtils.createRandomDag(numVars, 0, numEdges, 7, 5,
                5, false);

        System.out.println("\nInput graph:");
        System.out.println(trueGraph);

        SemPm semPm = new SemPm(trueGraph);
        SemIm semIm = new SemIm(semPm);
        RectangularDataSet _dataSet = semIm.simulateData(1000, false);

        IndependenceTest test = new IndTestFisherZ(_dataSet, 0.05);

        Cpc search = new Cpc(test, new Knowledge());
        Graph resultGraph = search.search();

    }

    public void tripleAccuracy() {
        int success = 0;
        int fail = 0;
        int totBidirected = 0;
        int numCyclic = 0;

        int numCorrectColliders = 0;
        int numCorrectNoncolliders = 0;
        int numEstColliders = 0;
        int numEstNoncolliders = 0;
        int numEstAmbiguous = 0;

        for (int i = 0; i < 100; i++) {
            TetradLogger.getInstance().log("info", "# " + (i + 1));

            Graph graph = GraphUtils.createRandomDag(20, 0, 20, 4, 4, 4, false);
            SemPm pm = new SemPm(graph);
            SemIm im = new SemIm(pm);
            RectangularDataSet dataSet = im.simulateData(1000, false);
            IndependenceTest test = new IndTestFisherZ(dataSet, 0.001);
            Cpc search = new Cpc(test, new Knowledge());
            Graph graph2 = search.search();

            ChoiceGenerator cg = new ChoiceGenerator(graph.getNumNodes(), 3);
            int[] choice;
            List<Node> nodes = graph.getNodes();

            while ((choice = cg.next()) != null) {
                Node node0 = nodes.get(choice[0]);
                Node node1 = nodes.get(choice[1]);
                Node node2 = nodes.get(choice[2]);


                Node node02 = graph2.getNode(node0.getName());
                Node node12 = graph2.getNode(node1.getName());
                Node node22 = graph2.getNode(node2.getName());

                if (graph2.isAdjacentTo(node02, node12) && graph2.isAdjacentTo(node12, node22)) {
                    if (graph2.isAmbiguous(node02, node12, node22)) {
                        numEstAmbiguous++;
                    } else

//                    if (!graph2.isAmbiguous(new Triple(node02, node12, node22))) {
//                        continue;
//                    }

                        if (graph2.isDefiniteCollider(node02, node12, node22)) {
                            numEstColliders++;

                            if (graph.isDefiniteCollider(node0, node1, node2)) {
                                numCorrectColliders++;
                            }
                        } else {
                            numEstNoncolliders++;

                            if (graph.isAdjacentTo(node0, node1) && graph.isAdjacentTo(node1, node2)) {
                                if (!graph.isDefiniteCollider(node0, node1, node2)) {
                                    numCorrectNoncolliders++;
                                }
                            }
                        }
                }
            }
        }

        double percentCorrectColliders = 100 * ((double) numCorrectColliders / numEstColliders);
        double percentCorrectNoncolliders = 100 * ((double) numCorrectNoncolliders / numEstNoncolliders);
        double percentAmbiguousTriples = 100 * ((double) numEstAmbiguous / (numEstColliders + numEstNoncolliders));
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        TetradLogger.getInstance().log("info", "# estimated colliders = " + numEstColliders);
        TetradLogger.getInstance().log("info", "# estimated noncolliders = " + numEstNoncolliders);
        TetradLogger.getInstance().log("info", "# estimated ambiguous = " + numEstAmbiguous);
        TetradLogger.getInstance().log("info", "# correct colliders = " + numCorrectColliders);
        TetradLogger.getInstance().log("info", "# correct noncolliders = " + numCorrectNoncolliders);
        TetradLogger.getInstance().log("info", "% correct colliders = " + nf.format(percentCorrectColliders));
        TetradLogger.getInstance().log("info", "% correct noncolliders = " + nf.format(percentCorrectNoncolliders));
        TetradLogger.getInstance().log("info", "% ambiguous triples = " + nf.format(percentAmbiguousTriples));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkSearch(String inputGraph, String outputGraph) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);

        // Set up search.
        IndependenceTest independence = new IndTestDSep(graph);
        Knowledge knowledge = new Knowledge();
        GraphSearch search = new Acpc(independence, knowledge);

        // Run search
        Graph resultGraph = search.search();

        // Build comparison graph.
        Graph trueGraph = GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
        System.out.println("\nTrue graph:");
        System.out.println(trueGraph);

        // Do test.
        assertTrue(resultGraph.equals(trueGraph));
    }

    /**
     * Presents the input graph to Fci and checks to make sure the output of Fci
     * is equivalent to the given output graph.
     */
    private void checkWithKnowledge(String inputGraph, String outputGraph,
                                    Knowledge knowledge) {

        // Set up graph and node objects.
        Graph graph = GraphConverter.convert(inputGraph);
        SemPm semPm = new SemPm(graph);
        SemIm semIM = new SemIm(semPm);
        RectangularDataSet dataSet = semIM.simulateData(1000, false);

        // Set up search.
//        IndependenceTest independence = new IndTestGraph(graph);
        IndependenceTest independence = new IndTestFisherZ(dataSet, 0.05);
        GraphSearch pcSearch = new Cpc(independence, knowledge);

        // Run search
        Graph resultGraph = pcSearch.search();

        // Build comparison graph.
//        Graph trueGraph = GraphConverter.convert(outputGraph);
        GraphConverter.convert(outputGraph);

        // PrintUtil out problem and graphs.
//        System.out.println("\nKnowledge:");
        System.out.println(knowledge);
        System.out.println("\nInput graph:");
        System.out.println(graph);
        System.out.println("\nResult graph:");
        System.out.println(resultGraph);
//        System.out.println("\nTrue graph:");
//        System.out.println(trueGraph);

        // Do test.
//        assertTrue(resultGraph.equals(trueGraph));
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