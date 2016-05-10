package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import cern.jet.random.engine.MersenneTwister;

/**
 * Tests the ability to get SEM models out of the Shimizu search.
 *
 * @author Joseph Ramsey
 */
public class ExploreLingam {
    private MersenneTwister mersenneTwister = new MersenneTwister(new Date());

    public ExploreLingam() {
    }

    public void scenario1() {
        try {
            DataParser parser = new DataParser();
            parser.setMaxIntegralDiscrete(0);

//            RectangularDataSet data = parser.parseTabular(new File("test_data/eigen4c.csv.dat"));
//            RectangularDataSet data = parser.parseTabular(new File("test_data/g1set.txt"));
//            data = new RegressionInterpolator().filter(data);
            RectangularDataSet data = parser.parseTabular(new File("test_data/SAHDMod.dat"));


            Lingam search = new Lingam(0.05);
            search.setPruningDone(false);
            GraphWithParameters result = search.lingam(data);
            System.out.println(result);

//            Graph graph = result.getGraph();
//            SemPm semPm = new SemPm(graph);
//            SemIm semIm = new SemIm(semPm);
//
//            for (Parameter parameter : semPm.getParameters()) {
//                if (parameter.getType() == ParamType.COEF) {
//                    Node node1 = parameter.getNodeA();
//                    Node node2 = parameter.getNodeB();
//                    Edge edge = graph.getEdge(node1, node2);
//                    double weight = result.getWeightHash().get(edge);
//                    semIm.setEdgeCoef(node1, node2, weight);
//                }
//            }
//
//            System.out.println(semIm);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Test that Clark described.
     * <p/>
     * Joe, I would like to add to your burdens by taking advantage of Patrik
     * Hoyer's visit, even if it means a brief interruption to the fmri work.
     * The chief question about their methods is how well they work, when. So I
     * would  like to start with a quick simulation study, The lognormal is a
     * commonly used asymmetric distribution, and it is easy to implement
     * simulated samples; generate a normal sample and take the log of each
     * positive value. (In order to make control of the sample sizes easier you
     * could generate the Normal values, square them, and then take logs).So, I
     * propose you undertake this:  Take a simple graph, X -> Y, with Y = a X +
     * e, generate 10 lognormal samples of X,e and Y, (or lognormals of
     * Xsquared, e squared and Y) for 5 random (but sufficiently large values of
     * a) at each of the sample sizes 50, 100, 1000, run Shimuzu over each
     * sample, estimate a by least squares, and give us the following:
     * <p/>
     * For each sample size and value of a:
     * <p/>
     * the % of cases in which the direction is correct
     * <p/>
     * the absolute value of the difference between the true value of a and the
     * estimated value of a, divided by the true value of a.
     * <p/>
     * What do you think?
     * <p/>
     * Clark
     */
    public void scenario2() {
        long time1 = System.currentTimeMillis();

        NumberFormat nf = new DecimalFormat("0.00");

        List<SamplingDistribution> distributions = new ArrayList<SamplingDistribution>();

        distributions.add(new LogNormal());
        distributions.add(new GaussianToPower(1.0, 2));
        distributions.add(new GaussianToPower(2.0, 3));
        distributions.add(new MixtureOfGaussians(.5, -1, 1, 1, 1));
        distributions.add(new MixtureOfGaussians(.1, -1, 1, 1, 1));
        distributions.add(new MixtureOfGaussians(.1, -2, 1, 2, 1));
        distributions.add(new MixtureOfGaussians(.5, -2, 1, 2, 1));
        distributions.add(new MixtureOfGaussians(.5, -1, 1, 1, 3));
        distributions.add(new Beta(.5, 1));
        distributions.add(new Beta(1, 3));
        distributions.add(new Beta(2, 2));
        distributions.add(new Beta(2, 5));
        distributions.add(new BreitWigner(0, .3, Double.NEGATIVE_INFINITY));
        distributions.add(new ChiSquare(1));
        distributions.add(new ChiSquare(5));
        distributions.add(new Exponential(1));
        distributions.add(new ExponentialPower(2));
        distributions.add(new Gamma(1, 2));
        distributions.add(new Gamma(2, 2));
        distributions.add(new Gamma(3, 2));
        distributions.add(new Gamma(5, 1));
        distributions.add(new Gamma(9, .5));
//        distributions.add(new Logarithmic(.5));
        distributions.add(new Poisson(1));
        distributions.add(new Poisson(4));
        distributions.add(new Poisson(10));
        distributions.add(new StudentT(1));
        distributions.add(new Uniform(-1, 1));
        distributions.add(new VonMises(.5));
        distributions.add(new VonMises(1));
        distributions.add(new VonMises(2));
        distributions.add(new VonMises(4));
        distributions.add(new VonMises(8));

        Dag graph = new Dag();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
//        Node z = new GraphNode("Z");
//        Node w = new GraphNode("W");
        graph.addNode(x);
        graph.addNode(y);
//        graph.addNode(z);
//        graph.addNode(w);
        graph.addDirectedEdge(x, y);
//        graph.addDirectedEdge(z, y);

//        System.out.println("Graph = " + graph);

        for (double coef : new double[]{.5, .8, 1.0, 1.2, 1.5}) {

            SemPm semPm = new SemPm(graph);
            SemIm semIm = new SemIm(semPm);

            semIm.setEdgeCoef(x, y, coef);

            double alpha = 0.05;

            for (int sampleSize : new int[]{50, 100, 500}) {
                System.out.println("\n\n******** Model X-(" + coef + ")-Y, Sample Size = " + sampleSize + "********");
                System.out.println("\n\nDistribution\t" +
                        "PC X---Y\t" +
                        "Avg aEstRegr\tAvg aEstRegr SE\tAvg ((a - aEstRegr) / a)\t" +
                        "Lingam X-->Y\tLingam X---Y\tAvg aEstLingam\tAvg ((a - aEstLingam) / a)");

                for (SamplingDistribution distribution : distributions) {
                    int numPcTotal = 0;
                    double sumAEstRegression = 0.0;
                    double sumAEstRegressionSe = 0.0;
                    double sumAEstRatioRegression = 0.0;

                    int numLingamTotal = 0;
                    int numLingamCorrect = 0;
                    double sumAEstLingam = 0.0;
                    double sumAEstRatioLingam = 0.0;

                    for (int i = 0; i < 100; i++) {
                        RectangularDataSet data = simulateDataNonNormal(semIm, sampleSize, distribution);

                        IndependenceTest test = new IndTestFisherZ(data, alpha);
                        PcSearch pcSearch = new PcSearch(test, new Knowledge());
                        Graph pcGraph = pcSearch.search();

                        Node _x = pcGraph.getNode("X");
                        Node _y = pcGraph.getNode("Y");

                        Edge edge = pcGraph.getEdge(_x, _y);

                        if (edge != null) {
                            numPcTotal++;
                        }

                        Lingam search = new Lingam(alpha);
//                        search.setPruningDone(false);
                        GraphWithParameters result = search.lingam(data);
                        Graph lingamGraph = result.getGraph();

                        _x = lingamGraph.getNode("X");
                        _y = lingamGraph.getNode("Y");

                        edge = lingamGraph.getEdge(_x, _y);

                        if (edge != null) {
                            numLingamTotal++;

                            if (edge.pointsTowards(_y)) {
                                numLingamCorrect++;
                                double aEstLingam = result.getWeightHash().get(edge);
                                sumAEstLingam += aEstLingam;
                                sumAEstRatioLingam += Math.abs((coef - aEstLingam) / coef);

                                Regression regression = new RegressionDataset(data);
                                Node dataX = data.getVariable("X");
                                Node dataY = data.getVariable("Y");
                                RegressionResult r = regression.regress(dataY, Collections.singletonList(dataX));
                                double aEstRegr = r.getCoef()[1];
                                sumAEstRegression += aEstRegr;
                                sumAEstRegressionSe += r.getSe()[1];
                                sumAEstRatioRegression += Math.abs((coef - aEstRegr) / aEstRegr);
                            }
                        }
                    }

                    String avgAEstRegr = nf.format(sumAEstRegression / numLingamTotal);
                    String avgAEstRegrSe = nf.format(sumAEstRegressionSe / numLingamTotal);
                    String avgLingamCoef = nf.format(sumAEstLingam / numLingamTotal);
                    String avgAMinusAEstOverA = nf.format(sumAEstRatioLingam / numLingamTotal);
                    String avgAMinusAEstRegrOverA = nf.format(sumAEstRatioRegression / numLingamTotal);

                    System.out.println(distribution + "\t" +
                            numPcTotal + "\t" +
                            avgAEstRegr + "\t" + avgAEstRegrSe + "\t" + avgAMinusAEstRegrOverA + "\t" +
                            numLingamCorrect + "\t" + numLingamTotal + "\t" + avgLingamCoef + "\t" + avgAMinusAEstOverA);


                }
            }
        }

        long time2 = System.currentTimeMillis();

        System.out.println("Elapsed time = " + (time2 - time1) / (double) 1000 + "s");
    }

    public void test3() {

        List<SamplingDistribution> distributions = new ArrayList<SamplingDistribution>();

        distributions.add(new LogNormal());
        distributions.add(new GaussianToPower(1.0, 2));
        distributions.add(new GaussianToPower(2.0, 3));
        distributions.add(new MixtureOfGaussians(.5, -1, 1, 1, 1));
        distributions.add(new MixtureOfGaussians(.1, -1, 1, 1, 1));
        distributions.add(new MixtureOfGaussians(.1, -2, 1, 2, 1));
        distributions.add(new MixtureOfGaussians(.5, -2, 1, 2, 1));
        distributions.add(new MixtureOfGaussians(.5, -1, 1, 1, 3));
        distributions.add(new Beta(.5, 1));  // 8
        distributions.add(new Beta(1, 3));
        distributions.add(new Beta(2, 2));
        distributions.add(new Beta(2, 5));
        distributions.add(new BreitWigner(0, .3, Double.NEGATIVE_INFINITY));
        distributions.add(new ChiSquare(1));
        distributions.add(new ChiSquare(5));
        distributions.add(new Exponential(1));
        distributions.add(new ExponentialPower(2));
        distributions.add(new Gamma(1, 2));  //17
        distributions.add(new Gamma(2, 2));
        distributions.add(new Gamma(3, 2));
        distributions.add(new Gamma(5, 1));
        distributions.add(new Gamma(9, .5));
//        distributions.add(new Hyperbolic(.1, .5));
        distributions.add(new Logarithmic(.5));
        distributions.add(new Poisson(1));
        distributions.add(new Poisson(4));
        distributions.add(new Poisson(10));
        distributions.add(new StudentT(1));
        distributions.add(new Uniform(-1, 1));
        distributions.add(new VonMises(.5));
        distributions.add(new VonMises(1));
        distributions.add(new VonMises(2));
        distributions.add(new VonMises(4));
        distributions.add(new VonMises(8));

        SamplingDistribution distribution = distributions.get(0);

        for (int i = 0; i < 20; i++) {

            System.out.println("\n*****************ROUND " + (i + 1) + "********************\n");

            System.out.println("Distribution: " + distribution);

            int numVars = 10;
            Dag graph = GraphUtils.createRandomDag(numVars, 0, numVars, 3, 3, 3, false);

            SemPm semPm = new SemPm(graph);
            SemIm semIm = new SemIm(semPm);
            int sampleSize = 500;
            RectangularDataSet data = simulateDataNonNormal(semIm, sampleSize, distribution);

            double alpha = 0.05;
            IndependenceTest test = new IndTestFisherZ(data, alpha);
            PcSearch pcSearch = new PcSearch(test, new Knowledge());
            Graph pcGraph = pcSearch.search();


            long time1 = System.currentTimeMillis();

//            Shimizu2006Search search2 = new Shimizu2006Search(alpha);
//            PatternWithParameters result2 = search2.lingamDiscovery_DAG(data);
//            Graph lingamGraph2 = result2.graph;

//        System.out.println("*************************************************");
//        System.out.println("*************************************************");
//        System.out.println("*************************************************");
//        System.out.println("*************************************************");

            long time2 = System.currentTimeMillis();
//            System.out.println("Shimizu2006Search time: " + (time2 - time1) / 1000d + "s");

            Lingam search = new Lingam(alpha);
//            search.setPruningDone(false);
//            search.setUpperTriangleKept(false);
            GraphWithParameters result = search.lingam(data);
            Graph lingamGraph = result.getGraph();

            long time3 = System.currentTimeMillis();
//            System.out.println("Lingam time: " + (time3 - time2) / 1000d + "s");

            System.out.println("Graph = " + graph);
            System.out.println("PC graph = " + pcGraph);
//            System.out.println("Shimizu2006 graph = " + lingamGraph2);
            System.out.println("Lingam graph = " + lingamGraph);

        }
    }

    public void rtest4() {
        try {
            SamplingDistribution distribution = new LogNormal();

            int numVars = 10;
            Dag graph = GraphUtils.createRandomDag(numVars, 0, numVars, 3, 3, 3, false);

            SemPm semPm = new SemPm(graph);
            SemIm semIm = new SemIm(semPm);
            int sampleSize = 500;
            RectangularDataSet data = simulateDataNonNormal(semIm, sampleSize, distribution);
            FileWriter writer = new FileWriter(new File("/home/jdramsey/nongaussiandata1.txt"));
            DataSavers.saveRectangularData(data, writer, '\t');
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RectangularDataSet simulateDataNonNormal(SemIm semIm, int sampleSize,
                                                     SamplingDistribution distribution) {
        List<SamplingDistribution> distributions = new ArrayList<SamplingDistribution>();

        for (int i = 0; i < semIm.getSemPm().getGraph().getNumNodes(); i++) {
            distributions.add(distribution);
        }

        return simulateDataNonNormal(semIm, sampleSize, distributions);
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Fast for large simulations but hangs for cyclic models.
     *
     * @param sampleSize   > 0.
     * @param distributions
     * @return the simulated data set.
     */
    private RectangularDataSet simulateDataNonNormal(SemIm semIm, int sampleSize,
                                                     List<SamplingDistribution> distributions) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = semIm.getSemPm().getVariableNodes();

        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            variables.add(var);
        }

        RectangularDataSet dataSet = new ColtDataSet(sampleSize, variables);

        // Create some index arrays to hopefully speed up the simulation.
        SemGraph graph = semIm.getSemPm().getGraph();
        List<Node> tierOrdering = graph.getTierOrdering();

        int[] tierIndices = new int[variableNodes.size()];

        for (int i = 0; i < tierIndices.length; i++) {
            tierIndices[i] = variableNodes.indexOf(tierOrdering.get(i));
        }

        int[][] _parents = new int[variables.size()][];

        for (int i = 0; i < variableNodes.size(); i++) {
            Node node = variableNodes.get(i);
            List<Node> parents = graph.getParents(node);

            for (Iterator<Node> j = parents.iterator(); j.hasNext();) {
                Node _node = j.next();

                if (_node.getNodeType() == NodeType.ERROR) {
                    j.remove();
                }
            }

            _parents[i] = new int[parents.size()];

            for (int j = 0; j < parents.size(); j++) {
                Node _parent = parents.get(j);
                _parents[i][j] = variableNodes.indexOf(_parent);
            }
        }

        // Do the simulation.
        for (int row = 0; row < sampleSize; row++) {
            for (int i = 0; i < tierOrdering.size(); i++) {
                int col = tierIndices[i];
                double value = distributions.get(col).drawSample();

                for (int j = 0; j < _parents[col].length; j++) {
                    int parent = _parents[col][j];
                    value += dataSet.getDouble(row, parent) *
                            semIm.getEdgeCoef().get(parent, col);
                }

                value += semIm.getMeans()[col];
                dataSet.setDouble(row, col, value);
            }
        }

        return dataSet;
    }

    public static void main() {
        new ExploreLingam().scenario1();
    }
}
