///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.*;
import edu.cmu.tetradapp.model.GraphComparison;

import java.util.*;

/**
 * Implements the PC ("Peter/Clark") algorithm, as specified in Chapter 6 of
 * Spirtes, Glymour, and Scheines, "Causation, Prediction, and Search," 2nd
 * edition, with a modified rule set in step D due to Chris Meek. For the
 * modified rule set, see Chris Meek (1995), "Causal inference and causal
 * explanation with background knowledge."
 *
 * @author Joseph Ramsey (this version).
 */
public class Acpc implements GraphSearch {

    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * Forbidden and required edges for the search.
     */
    private Knowledge knowledge;
    /**
     * The maximum number of nodes conditioned on in the search.
     */
    private int depth = Integer.MAX_VALUE;

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;


    /**
     * True if cycles are to be aggressively prevented. May be expensive for
     * large graphs (but also useful for large graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * The logger to use.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    private double alpha;

    /**
     * The list of all unshielded triples.
     */
    private Set<Triple> allTriples;

    /**
     * Set of unshielded colliders from the triple orientation step.
     */
    private Set<Triple> colliderTriples;

    /**
     * Set of unshielded noncolliders from the triple orientation step.
     */
    private Set<Triple> noncolliderTriples;

    /**
     * Set of ambiguous unshielded triples.
     */
    private Set<Triple> ambiguousTriples;
    private Graph trueGraph;

    private SemIm semIm;
    private int maxAdjacencies = 6;

    //=============================CONSTRUCTORS==========================//

    public Acpc(IndependenceTest independenceTest, Knowledge knowledge) {
        if (independenceTest == null) {
            throw new NullPointerException();
        }

        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.knowledge = knowledge;
        this.allTriples = new HashSet<Triple>();
        this.ambiguousTriples = new HashSet<Triple>();
        this.colliderTriples = new HashSet<Triple>();
        this.noncolliderTriples = new HashSet<Triple>();
        TetradLoggerConfig config = logger.getTetradLoggerConfigForModel(this.getClass());

        if (config != null) {
            logger.setTetradLoggerConfig(config);
        }
    }

    //==============================PUBLIC METHODS========================//

    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }


    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Runs PC starting with a fully connected graph over all of the variables
     * in the domain of the independence test.
     */
    @Override
	public Graph search() {
        IndependenceTest test = getIndependenceTest();

        Cpc search = new Cpc(test, getKnowledge());
        Graph graph = search.search();

//        Graph originalGraph = SearchGraphUtils.patternFromDag(trueGraph);
//        Graph pattern = GraphUtils.convertNodes(originalGraph, graph.getNodes());

//        Graph graph = GraphUtils.convertNodes(trueGraph, test.getVariables());

//        Graph randomGraph = GraphUtils.createRandomDag(test.getVariables().size(),
//                0, test.getVariables().size(), 4, 4, 4, false);
//        Graph graph = GraphUtils.convertNodes(randomGraph, test.getVariables());

//        Graph graph = new EdgeListGraph(test.getVariables());
//        graph.fullyConnect(Endpoint.TAIL);

        EdgeListGraph fullGraph = new EdgeListGraph(graph.getNodes());
        fullGraph.fullyConnect(Endpoint.TAIL);
        List<Edge> edges = fullGraph.getEdges();

        Set<IndependenceFact> removalFacts = new HashSet<IndependenceFact>();
        boolean changed = true;

        Graph savedGraph = null;

        int loop = 0;

//        for (int i = 0; i < 2; i++) {
        while (changed) {
            loop++;
//            System.out.println("loop " + loop + " #err " + totAdjErr(pattern, graph) + " #edges = " + graph.getNumEdges()
//                + " #FP = " + adjFp(pattern, graph) + " + #FN = " + adjFn(pattern, graph));

            changed = false;
            Collections.shuffle(edges);

            Set<Edge> removedEdges = new HashSet<Edge>();

            for (Edge edge : edges) {
                Edge _edge = graph.getEdge(edge.getNode1(), edge.getNode2());

                Node x = edge.getNode1();
                Node y = edge.getNode2();

                List<Node> sepsetX = pathBlockingSet(test, graph, x, y);
                List<Node> sepsetY = pathBlockingSet(test, graph, y, x);

                if (_edge == null) {
                    if (sepsetX == null && sepsetY == null) {
                        if (graph.getAdjacentNodes(x).size() >= getMaxAdjacencies()) {
                            continue;
                        }

                        if (graph.getAdjacentNodes(y).size() >= getMaxAdjacencies()) {
                            continue;
                        }

                        graph.addUndirectedEdge(x, y);
                        changed = true;
                        graph = orientCpc(graph, knowledge, depth);
                    }
                } else {
                    if ((sepsetX != null && removalFacts.contains(new IndependenceFact(x, y, sepsetX))
                            || (sepsetY != null && removalFacts.contains(new IndependenceFact(y, x, sepsetY))))) {
                        continue;
                    }

                    if (sepsetX != null || sepsetY != null) {
                        graph.removeEdge(_edge);

                        if (removedEdges.contains(edge)) {
                            System.out.println("Already removed " + edge);
                        }

                        removedEdges.add(edge);

                        changed = true;

                        if (sepsetX != null) {
                            removalFacts.add(new IndependenceFact(x, y, sepsetX));
                        }

                        if (sepsetY != null) {
                            removalFacts.add(new IndependenceFact(x, y, sepsetY));
                        }

                        graph = orientCpc(graph, knowledge, depth);
                    }
                }
//            }
            }

//            if (savedGraph != null) {
//                System.out.println(savedGraph.getNumNodes());
//            }

//            if (savedGraph != null && graph.getNumEdges() > savedGraph.getNumEdges()) {
//                return savedGraph;
//            }

            savedGraph = new EdgeListGraph(graph);
        }

//        printStuff(test, graph);
        return graph;
    }

    private void printStuff(IndependenceTest test, Graph graph) {
        //        System.out.println("Connectivity = " + graph.getConnectivity());
//        System.out.println("Num indep tests = " + numIndependenceTests);
//        System.out.println("Num nonadjacent adds = " + numNonadjacentAdds);

        for (Edge edge : trueGraph.getEdges()) {
            Node _x = edge.getNode1();
            Node _y = edge.getNode2();
            Node x = graph.getNode(_x.getName());
            Node y = graph.getNode(_y.getName());

            if (!graph.isAdjacentTo(x, y)) {
                if (graph.getEdge(y, x) != null) {
                    System.out.println("Bloody murder!");
                }

                System.out.println("False negative: " + edge);

                List<Node> commonAdj = trueGraph.getAdjacentNodes(_x);
                commonAdj.retainAll(trueGraph.getAdjacentNodes(_y));

                StringBuffer buf = new StringBuffer();

                for (Node adj : commonAdj) {
                    Node _adj = trueGraph.getNode(adj.getName());

                    if (graph.isDefiniteCollider(x, adj, y)) {
                        buf.append("collider " );
                    }
                    else {
                        buf.append("noncollider ");
                    }

                    if (trueGraph.isDefiniteCollider(_x, _adj, _y)) {
                        buf.append(" [collider] " );
                    }
                    else {
                        buf.append(" [noncollider] ");
                    }

                    if (graph.isAmbiguous(x, adj, y)) {
                        buf.append(" (ambiguous) ");

                    }
                }

                List<Node> sepsetX = pathBlockingSet(test, graph, x, y);
                List<Node> sepsetY = pathBlockingSet(test, graph, y, x);

//                List<Node> _sepsetX = pathBlockingSet2(trueGraph, _x, _y);
//                List<Node> _sepsetY = pathBlockingSet2(trueGraph, _y, _x);

                List<Node> _sepsetX = null;

                if (sepsetX != null) {
                    _sepsetX = new LinkedList<Node>();

                    for (Node node : sepsetX) {
                        _sepsetX.add(trueGraph.getNode(node.getName()));
                    }
                }

                List<Node> _sepsetY = null;

                if (sepsetY != null) {
                    _sepsetY = new LinkedList<Node>();

                    for (Node node : sepsetY) {
                        _sepsetY.add(trueGraph.getNode(node.getName()));
                    }
                }

//                if (sepsetX != null) {
//                    boolean indep1 = test.isIndependent(x, y, sepsetX);
//                    double p1 = test.getPValue();
//                    System.out.println("   x = " + x + " y = " + y);
//                    System.out.println("   Common adj = " + commonAdj + " " + buf.toString());
//                    System.out.println("   est B(x, y) = " + sepsetX + " indep1 = " + indep1 + " p1 = " + p1);
//                    System.out.println("   est adj(x) = " + graph.getAdjacentNodes(x));
//                    System.out.println("   est children(x) = " + graph.getChildren(x));
//                    System.out.println("   est parents(x) = " + graph.getParents(x));
//                    System.out.println("   true B(x, y) = " + _sepsetX);
//                    System.out.println("   true adj(x) = " + trueGraph.getAdjacentNodes(_x));
//                    System.out.println("   true children(x) = " + trueGraph.getChildren(_x));
//                    System.out.println("   true parents(x) = " + trueGraph.getParents(_x));
//                }
//
//                if (sepsetY != null) {
//                    boolean indep2 = test.isIndependent(x, y, sepsetY);
//                    double p2 = test.getPValue();
//
//                    System.out.println("   y = " + y + " x = " + x);
//                    System.out.println("   Common adj = " + commonAdj + " " + buf.toString());
//                    System.out.println("   est B(y, x) = " + sepsetY + " indep2 = " + indep2 + " p2 = " + p2);
//                    System.out.println("   est adj(y) = " + graph.getAdjacentNodes(y));
//                    System.out.println("   est children(y) = " + graph.getChildren(y));
//                    System.out.println("   est parents(y) = " + graph.getParents(y));
//                    System.out.println("   true B(y, x) = " + _sepsetY);
//                    System.out.println("   true adj(y) = " + trueGraph.getAdjacentNodes(_y));
//                    System.out.println("   true children(y) = " + trueGraph.getChildren(_y));
//                    System.out.println("   true parents(y) = " + trueGraph.getParents(_y));
//                }

                Set<Node> estNodes = new LinkedHashSet<Node>();
                Set<Node> trueNodes = new LinkedHashSet<Node>();

                if (sepsetX != null) {
                    estNodes.addAll(sepsetX);
                    boolean indep1 = test.isIndependent(x, y, sepsetX);
                    double p1 = test.getPValue();
                    System.out.println("   est B(x, y) = " + sepsetX + " indep1 = " + indep1 + " p1 = " + p1);

                    for (Node node : sepsetX) {
                        trueNodes.add(trueGraph.getNode(node.getName()));
                    }
                }

                if (sepsetY != null) {
                    estNodes.addAll(sepsetY);
                    boolean indep2 = test.isIndependent(x, y, sepsetY);
                    double p2 = test.getPValue();
                    System.out.println("   est B(y, x) = " + sepsetY + " indep1 = " + indep2 + " p1 = " + p2);

                    for (Node node : sepsetY) {
                        trueNodes.add(trueGraph.getNode(node.getName()));
                    }
                }


                estNodes.add(x);
                estNodes.add(y);
                List<Node> estNodesList = new LinkedList<Node>(estNodes);
                Graph subGraph = graph.subgraph(estNodesList);
                System.out.println("estimated nodes: " + estNodesList);
                System.out.println("Estimated subgraph: " + subGraph);

                trueNodes.add(_x);
                trueNodes.add(_y);
                List<Node> trueNodesList = new LinkedList<Node>(trueNodes);
                Graph _subGraph = trueGraph.subgraph(trueNodesList);

                System.out.println("true nodes: " + trueNodesList);
                System.out.println("True subgraph: " + _subGraph);

                SemPm semPm = new SemPm(trueGraph);
                SemEstimator estimator = new SemEstimator(test.getData(), semPm);
                estimator.estimate();
                SemIm estIm = estimator.getEstimatedSem();

                if (_sepsetX != null) {
                    List<List<Node>> dConnectingPathsX = GraphUtils.dConnectingPaths(
                            trueGraph, _x, _y, _sepsetX);
                    System.out.println("D connecting paths conditioning on _sepsetX:");

                    for (List<Node> path : dConnectingPathsX) {
                        System.out.println(GraphUtils.pathString(trueGraph, path, _sepsetX));
                    }

                    System.out.println("Sum of paths = " + sumOfPaths(dConnectingPathsX, estIm));
                }

                if (_sepsetY != null) {
                    List<List<Node>> dConnectingPathsY = GraphUtils.dConnectingPaths(
                            trueGraph, _x, _y, _sepsetY);
                    System.out.println("D connecting paths conditioning on _sepsetY:");

                    for (List<Node> path : dConnectingPathsY) {
                        System.out.println(GraphUtils.pathString(trueGraph, path, _sepsetY));
                    }

                    System.out.println("Sum of paths = " + sumOfPaths(dConnectingPathsY, estIm));
                }

//                List<List<Node>> dConnectingPathsY = GraphUtils.dConnectingPaths(
//                        trueGraph, _x, _y, _sepsetY);
//
//
//                System.out.println("D connecting paths conditioning on _sepsetY:");
//
//                for (List<Node> path : dConnectingPathsY) {
//                    System.out.println(GraphUtils.pathString(trueGraph, path, _sepsetY));
//                }
            }

        }
    }

    private double sumOfPaths(List<List<Node>> paths, SemIm semIm) {
        Graph semGraph = semIm.getSemPm().getGraph();
        double sum = 0.0;

        for (List<Node> path : paths) {
//            System.out.println("New path: " + path);

            double product = productOfCoefs(semIm, path);

//            System.out.println("Product = " + product);

            sum += product;
        }

        return sum;
    }

    private double productOfCoefs(SemIm semIm, List<Node> path) {
        Graph semGraph = semIm.getSemPm().getGraph();
        double product = 1.0;

        for (int i = 0; i < path.size() - 1; i++) {
            Node x = semGraph.getNode(path.get(i).getName());
            Node y = semGraph.getNode(path.get(i + 1).getName());

            double coef = semIm.getEdgeCoef(x, y);

            if (Double.isNaN(coef)) {
                coef = semIm.getEdgeCoef(y, x);
            }

            if (Double.isNaN(coef)) {
                coef = 0.0;
            }

//                System.out.println("Coef for " + x + "---" + y + " is " + coef);

            product *= coef;
        }
        return product;
    }

    private double[] coefsAlongPath(SemIm semIm, List<Node> path) {
        Graph semGraph = semIm.getSemPm().getGraph();
        double[] coefs = new double[path.size() - 1];

        for (int i = 0; i < path.size() - 1; i++) {
            Node x = semGraph.getNode(path.get(i).getName());
            Node y = semGraph.getNode(path.get(i + 1).getName());

            double coef = semIm.getEdgeCoef(x, y);

            if (Double.isNaN(coef)) {
                coef = semIm.getEdgeCoef(y, x);
            }

            if (Double.isNaN(coef)) {
                coef = 0.0;
            }

            coefs[i] = coef;
        }

        return coefs;
    }

    private List<Node> pathBlockingSet(IndependenceTest test, Graph graph, Node x, Node y) {
        List<Node> commonAdjacents = graph.getAdjacentNodes(x);
        commonAdjacents.retainAll(graph.getAdjacentNodes(y));

        DepthChoiceGenerator generator = new DepthChoiceGenerator(commonAdjacents.size(), commonAdjacents.size());
        int[] choice;

        while ((choice = generator.next()) != null) {
            List<Node> excludes = GraphUtils.asList(choice, commonAdjacents);
            List<Node> sepset = pathBlockingSetExcluding(graph, x, y, excludes);

            if (sepset == null) {
                continue;
            }

//            if (trueGraph != null) {
//                Node _x = trueGraph.getNode(x.getName());
//                Node _y = trueGraph.getNode(y.getName());
//                List<Node> _sepset = GraphUtils.convertNodes(sepset, trueGraph);
//
//                IndTestDSep dsep = new IndTestDSep(trueGraph);
//
//                if (!dsep.isDSeparated(_x, _y, _sepset) && test.isIndependent(x, y, sepset)) {
////                    List<Node> _trueB = pathBlockingSet2(trueGraph, _x, _y);
////
////                    if (!dsep.isIndependent(_x, _y, _trueB)) {
////                        System.out.print("false\t");
////
////                        if (!trueGraph.isAdjacentTo(_x, _y)) {
////                            System.out.print("Not adj\t");
////                        }
////                    }
//
////                    List<Node> trueB = GraphUtils.convertNodes(_trueB, graph);
//                    List<List<Node>> paths = GraphUtils.dConnectingPaths(trueGraph, _x, _y, _sepset);
////                    List<List<Node>> paths = GraphUtils.treks(trueGraph, _x, _y);
////                    List<List<Node>> paths = GraphUtils.dConnectingPaths(trueGraph, _x, _y, new LinkedList<Node>());
//
//                    boolean lengthOne = false;
//
//                    for (List<Node> path : paths) {
//                        if (path.size() == 2) {
//                            lengthOne = true;
//                            break;
//                        }
//                    }
//
//                    if (lengthOne) {
//                        System.out.println("\nFor " + x + " --- " + y + " p = " + test.getPValue());
//                        System.out.println("sepset = " + sepset);
//
//                        CovarianceMatrix m = new CovarianceMatrix(test.getData());
//                        int xIndex = m.getVariables().indexOf(x);
//                        int yIndex = m.getVariables().indexOf(y);
//
//                        System.out.println("Variance of " + x + " = " + m.getMatrix().get(xIndex, xIndex));
//                        System.out.println("Variance of " + y + " = " + m.getMatrix().get(yIndex, yIndex));
//
//                        List<Node> adjX = trueGraph.getAdjacentNodes(_x);
//
//                        for (Node _node : adjX) {
//                            Edge _edge = trueGraph.getEdge(_x, _node);
//                            Edge edge = graph.getEdge(x, graph.getNode(_node.getName()));
//                            System.out.println(_edge + ", in estimated graph = " + edge);
//                        }
//
//                        List<Node> adjY = trueGraph.getAdjacentNodes(_y);
//
//                        for (Node _node : adjY) {
//                            Edge _edge = trueGraph.getEdge(_y, _node);
//                            Edge edge = graph.getEdge(y, graph.getNode(_node.getName()));
//                            System.out.println(_edge + ", in estimated graph = " + edge);
//                        }
//
//                        System.out.println("adj(" + _y + ") = " + adjY);
//
//                        for (List<Node> path : paths) {
//                            System.out.println(" prod = " + productOfCoefs(semIm, path) + " " +
//                                    GraphUtils.pathString(trueGraph, path, _sepset));
//
////                            double[] coefs = coefsAlongPath(semIm, path);
////
////                            for (double coef : coefs) {
////                                System.out.print("\t" + coef);
////                            }
////
////                            System.out.println();
//                                                    }
//                    }
//                }
//            }


            if (test.isIndependent(x, y, sepset)) {
//                double alpha = test.getAlpha();
//
//                if (excludes.size() > 0) {
//                    test.setAlpha(0.0005);
//
//                    boolean independent = test.isIndependent(x, y, sepset);
//
//                    if (independent) {
//                        test.setAlpha(alpha);
//                        return sepset;
//                    }
//
//                    test.setAlpha(alpha);
//                }


//                RectangularDataSet data = test.getData();
//                int indexX = data.getColumn(x);
//                int indexY = data.getColumn(y);
//
//                double[][] data2 = new double[2][data.getNumRows()];
//
//                for (int i = 0; i < data.getNumRows(); i++) {
//                    data2[0][i] = data.getDouble(i, indexX);
//                    data2[1][i] = data.getDouble(i, indexY);
//                }
//
//                double varianceX = StatUtils.variance(data2[0]);
//                double varianceY = StatUtils.variance(data2[1]);
//
//                if (varianceX * 12 < varianceY) {
//                    double alpha = test.getAlpha();
//                    test.setAlpha(0.05);
//                    boolean indep = test.isIndependent(x, y, sepset);
//                    test.setAlpha(alpha);
//
//                    if (indep) {
//                        test.setAlpha(alpha);
//                        return sepset;
//                    }
//                    else {
//                        return null;
//                    }
//                }

                return sepset;
            }
        }

        return null;
    }

    private List<Node> pathBlockingSet2(Graph graph, Node x, Node y) {
        List<Node> commonAdjacents = graph.getAdjacentNodes(x);
        commonAdjacents.retainAll(graph.getAdjacentNodes(y));

        List<Node> excludes = new LinkedList<Node>();

        for (Node adj : commonAdjacents) {
            if (graph.isDefiniteCollider(x, adj, y)) {
                excludes.add(adj);
            }
            else if (!graph.isDefiniteNoncollider(x, adj, y)) {
                return null;
            }
        }

        return pathBlockingSetExcluding(graph, x, y, excludes);
    }

    private List<Node> pathBlockingSetExcluding(Graph graph, Node x, Node y, List<Node> excludes) {
        List<Node> condSet = new LinkedList<Node>();
        Set<Node> descendants = new HashSet<Node>();

        for (Node exclude : excludes) {
            descendants.addAll(graph.getDescendants(Collections.singletonList(exclude)));
        }

        descendants.remove(x);
        descendants.remove(y);

        for (Node parent : graph.getParents(x)) {
            if (descendants.contains(parent)) {
                return null;
            }
        }

        for (Node parent : graph.getParents(y)) {
            if (descendants.contains(parent)) {
                return null;
            }
        }

        for (Node adj : graph.getAdjacentNodes(x)) {
            if (adj == y) continue;

            if (descendants.contains(adj)) {
                continue;
            }

            condSet.add(adj);

            if (!graph.isParentOf(adj, x)) {
                for (Node parent : graph.getParents(adj)) {
                    if (parent != x && parent != y && !condSet.contains(parent)) {
                        condSet.add(parent);
                    }
                }
            }
        }

        for (Node parent : graph.getParents(y)) {
            if (parent != x && !condSet.contains(parent)) {
                condSet.add(parent);
            }
        }

        return condSet;
    }

    @Override
	public long getElapsedTime() {
        return elapsedTime;
    }

    private Graph orientCpc(Graph graph, Knowledge knowledge, int depth) {
        graph = GraphUtils.undirectedGraph(graph);
        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());
        graph = orientUnshieldedTriples(graph, knowledge, getIndependenceTest(), depth);
        MeekRules meekRules = new MeekRules();
        meekRules.setAggressivelyPreventCycles(true);
        meekRules.setKnowledge(knowledge);
        meekRules.orientImplied(graph);
        return graph;
    }

    @SuppressWarnings({"SameParameterValue"})
    private Graph orientUnshieldedTriples(Graph graph, Knowledge knowledge,
                                          IndependenceTest test, int depth) {
        TetradLogger.getInstance().info("Starting Collider Orientation:");

        colliderTriples = new HashSet<Triple>();
        noncolliderTriples = new HashSet<Triple>();
        ambiguousTriples = new HashSet<Triple>();

        for (Node y : graph.getNodes()) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(y);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node x = adjacentNodes.get(combination[0]);
                Node z = adjacentNodes.get(combination[1]);

                if (graph.isAdjacentTo(x, z)) {
                    continue;
                }

                allTriples.add(new Triple(x, y, z));

                SearchGraphUtils.CpcTripleType type = SearchGraphUtils.getCpcTripleType(x, y, z, test, depth, graph);

                if (type == SearchGraphUtils.CpcTripleType.COLLIDER) {
                    if (colliderAllowed(x, y, z, knowledge)) {
                        graph.setEndpoint(x, y, Endpoint.ARROW);
                        graph.setEndpoint(z, y, Endpoint.ARROW);

                        TetradLogger.getInstance().log("colliderOriented", SearchLogUtils.colliderOrientedMsg(x, y, z));
                    }

                    colliderTriples.add(new Triple(x, y, z));
                } else if (type == SearchGraphUtils.CpcTripleType.AMBIGUOUS) {
                    Triple triple = new Triple(x, y, z);
                    ambiguousTriples.add(triple);
                    graph.setAmbiguous(triple);
                } else {
                    noncolliderTriples.add(new Triple(x, y, z));
                }
            }
        }

        TetradLogger.getInstance().info("Finishing Collider Orientation.");

        return graph;
    }

    private boolean colliderAllowed(Node x, Node y, Node z, Knowledge knowledge) {
        return isArrowpointAllowed1(x, y, knowledge) &&
                isArrowpointAllowed1(z, y, knowledge);
    }

    private static boolean isArrowpointAllowed1(Node from, Node to,
                                                Knowledge knowledge) {
        if (knowledge == null) {
            return true;
        }

        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public void setSemIm(SemIm semIm) {
        this.semIm = semIm;

    }

    private int totAdjErr(Graph pattern, Graph result) {
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

    private int adjFp(Graph pattern, Graph result) {
        GraphComparison comparison = new GraphComparison(pattern, result);
        int value = comparison.getEdgesAdded().size();
//        int value = comparison.getEdgesRemoved().size();
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

        return value;
    }

    private int adjFn(Graph pattern, Graph result) {
        GraphComparison comparison = new GraphComparison(pattern, result);
//        int value = comparison.getEdgesAdded().size();
        int value = comparison.getEdgesRemoved().size();
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

        return value;
    }

    public int getMaxAdjacencies() {
        return maxAdjacencies;
    }

    /**
     * Sets the maximum number of adjacencies.
     * @param maxAdjacencies
     */
    public void setMaxAdjacencies(int maxAdjacencies) {
        if (maxAdjacencies < 1) {
            throw new IllegalArgumentException("Max adjacencies needs to be at " +
                    "least one, preferably at least 3");
        }

        this.maxAdjacencies = maxAdjacencies;
    }

    public List<Node> getSemidirectedDescendants(Graph graph, List<Node> nodes) {
        HashSet<Node> descendants = new HashSet<Node>();

        for (Object node1 : nodes) {
            Node node = (Node) node1;
            collectSemidirectedDescendantsVisit(graph, node, descendants);
        }

        return new LinkedList<Node>(descendants);
    }

    private void collectSemidirectedDescendantsVisit(Graph graph, Node node, Set<Node> descendants) {
        descendants.add(node);
        List<Node> children = graph.getChildren(node);

        if (!children.isEmpty()) {
            for (Object aChildren : children) {
                Node child = (Node) aChildren;
                doSemidirectedChildClosureVisit(graph, child, descendants);
            }
        }
    }

    /**
     * closure under the child relation
     */
    private void doSemidirectedChildClosureVisit(Graph graph, Node node, Set<Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Edge edge1 : graph.getEdges(node)) {
                Node sub = Edges.traverseUndirected(node, edge1);

                if (sub != null && (edge1.pointsTowards(sub) || Edges.isUndirectedEdge(edge1))) {
                    System.out.println(edge1);
                    doSemidirectedChildClosureVisit(graph, sub, closure);
                }
            }
        }
    }

}

