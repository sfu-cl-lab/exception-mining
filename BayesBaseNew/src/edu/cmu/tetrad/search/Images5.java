///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.*;


/**
 * GesSearch is an implentation of the GES algorithm, as specified in Chickering (2002) "Optimal structure
 * identification with greedy search" Journal of Machine Learning Research. It works for both BayesNets and SEMs.
 * <p/>
 * Some code optimization could be done for the scoring part of the graph for discrete models (method scoreGraphChange).
 * Some of Andrew Moore's approaches for caching sufficient statistics, for instance.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public final class Images5 implements GraphSearch, IImages {

    /**
     * //     * The data set, various variable subsets of which are to be scored. //
     */
//    private List<DataSet> dataSets;

    private List<ICovarianceMatrix> covs;

    /**
     * The covariance matrices for the data set.
     */
    private List<DoubleMatrix2D> variances;

    /**
     * Sample size, either from the data set or from the variances.
     */
    private int sampleSize;

    /**
     * Specification of forbidden and required edges.
     */
    private Knowledge knowledge = new Knowledge();

//    /**
//     * For discrete data scoring, the structure prior.
//     */
//    private double structurePrior;
//
//    /**
//     * For discrete data scoring, the sample prior.
//     */
//    private double samplePrior;

    /**
     * Map from variables to their column indices in the data set.
     */
    private HashMap<Node, Integer> hashIndices;

    /**
     * Array of variable names from the data set, in order.
     */
    private String varNames[];

    /**
     * List of variables in the data set, in order.
     */
    private List<Node> variables;

//    /**
//     * True iff the data set is discrete.
//     */
//    private boolean discrete;

    /**
     * The true graph, if known. If this is provided, asterisks will be printed out next to false positive added edges
     * (that is, edges added that aren't adjacencies in the true graph).
     */
    private Graph trueGraph;

    /**
     * For formatting printed numbers.
     */
    private final NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * For linear algebra.
     */
    private final Algebra algebra = new Algebra();

    /**
     * Caches scores for discrete search.
     */
    private final LocalScoreCache localScoreCache = new LocalScoreCache();

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;


    /**
     * True if cycles are to be aggressively prevented. May be expensive for large graphs (but also useful for large
     * graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    private transient List<PropertyChangeListener> listeners;
    private double penaltyDiscount = 1.0;
    private int maxNumEdges = -1;
    private double bic;
//    private Map<DataSet, List<Node>> missingVariables;
//    private SortedSet<ScoredGraph> topGraphs = new TreeSet<ScoredGraph>();
    private int numPatternsToStore = 10;
    private Graph returnGraph;

    //===========================CONSTRUCTORS=============================//

    public Images5(List<ICovarianceMatrix> covs) {
        setDataSets(covs);
        initialize(10., 0.001);
    }

    //==========================PUBLIC METHODS==========================//


    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    /**
     * Greedy equivalence search: Start from the empty graph, add edges till model is significant. Then start deleting
     * edges till a minimum is achieved.
     *
     * @return the resulting Pattern.
     */
    public Graph search() {
        long startTime = System.currentTimeMillis();

        Graph graph = new EdgeListGraph(new LinkedList<Node>(getVariables()));
        fireGraphChange(graph);
        buildIndexing(graph);
        addRequiredEdges(graph);

        // Method 1-- original.
        double score = scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        bic = scoreGraph(graph);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

        TetradLogger.getInstance().log("graph", "\nReturning this graph: " + graph);

        TetradLogger.getInstance().log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");

        this.returnGraph = graph;

        return graph;

        // Method 2-- Ricardo's tweak.
//        double score = scoreGraph(graph), newScore;
//        int iter = 0;
//        do {
//            newScore = fes(graph, score);
//            if (newScore > score) {
//                score = newScore;
//                newScore = bes(graph, score);
//                if (score >= newScore) {
//                    break;
//                }
//                else {
//                    score = newScore;
//                }
//            }
//            else {
//                break;
//            }
//            //System.out.println("Current score = " + score);
//            iter++;
//        } while (iter < 100);
//        return graph;
    }

    public Graph search(List<Node> nodes) {
        long startTime = System.currentTimeMillis();
        localScoreCache.clear();

        if (!variables.containsAll(nodes)) {
            throw new IllegalArgumentException(
                    "All of the nodes must be in the supplied data set.");
        }

        Graph graph = new EdgeListGraph(nodes);
        buildIndexing(graph);
        addRequiredEdges(graph);
        double score = scoreGraph(graph);

        // Do forward search.
        score = fes(graph, score);

        // Do backward search.
        bes(graph, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;

        TetradLogger.getInstance().log("graph", "\nReturning this graph: " + graph);
        TetradLogger.getInstance().log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");

        this.returnGraph = graph;

        return graph;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.
     *
     * @param knowledge the knowledge object, specifying forbidden and required edges.
     */
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    //===========================PRIVATE METHODS========================//

    private void initialize(double samplePrior, double structurePrior) {
        setStructurePrior(structurePrior);
        setSamplePrior(samplePrior);
    }

    /**
     * Forward equivalence search.
     *
     * @param graph The graph in the state prior to the forward equivalence search.
     * @param score The score in the state prior to the forward equivalence search
     * @return the score in the state after the forward equivelance search. Note that the graph is changed as a
     *         side-effect to its state after the forward equivelance search.
     */
    private double fes(Graph graph, double score) {
        TetradLogger.getInstance().log("info", "** FORWARD EQUIVALENCE SEARCH");
        double bestScore = score;
        TetradLogger.getInstance().log("info", "Initial Score = " + nf.format(bestScore));

        Node x, y;
        Set<Node> t = new HashSet<Node>();

        do {
            x = y = null;
            List<Node> nodes = graph.getNodes();
            Collections.shuffle(nodes);

//            TEMP:
            for (int i = 0; i < nodes.size(); i++) {
                Node _x = nodes.get(i);

                for (Node _y : nodes) {
                    if (_x == _y) {
                        continue;
                    }

                    if (graph.isAdjacentTo(_x, _y)) {
                        continue;
                    }

                    if (getKnowledge().edgeForbidden(_x.getName(),
                            _y.getName())) {
                        continue;
                    }

                    List<Node> tNeighbors = getTNeighbors(_x, _y, graph);
                    List<Set<Node>> tSubsets = powerSet(tNeighbors);

                    for (Set<Node> tSubset : tSubsets) {

                        if (!validSetByKnowledge(_x, _y, tSubset, true)) {
                            continue;
                        }

                        double insertEval = insertEval(_x, _y, tSubset, graph);
                        double evalScore = score + insertEval;

                        TetradLogger.getInstance().log("edgeEvaluations", "Attempt adding " + _x + "-->" + _y +
                                " " + tSubset + " (" + evalScore + ")");

//                        System.out.println("Attempt adding " + _x + "-->" + _y +
//                                " " + tSubset + " (" + evalScore + ")");

                        if (!(evalScore > bestScore && evalScore > score)) {
                            continue;
                        }

                        if (!validInsert(_x, _y, tSubset, graph)) {
                            continue;
                        }

//                        System.out.println(insertEval);

                        bestScore = evalScore;
                        x = _x;
                        y = _y;
                        t = tSubset;

//                        break TEMP;
                    }
                }
            }

            if (x != null) {
                insert(x, y, t, graph, bestScore, true);
                rebuildPattern(graph);
                score = bestScore;

                if (getMaxNumEdges() != -1 && graph.getNumEdges() >= getMaxNumEdges()) {
                    break;
                }
            }
        } while (x != null);
        return score;
    }

    /**
     * Backward equivalence search.
     */
    private double bes(Graph graph, double initialScore) {
        TetradLogger.getInstance().log("info", "** BACKWARD ELIMINATION SEARCH");
        TetradLogger.getInstance().log("info", "Initial Score = " + nf.format(initialScore));
        double score = initialScore;
        double bestScore = score;
        Node x, y;
        Set<Node> t = new HashSet<Node>();
        do {
            x = y = null;
            List<Edge> edges1 = graph.getEdges();
            Collections.shuffle(edges1);
            List<Edge> edges = new ArrayList<Edge>();

            for (Edge edge : edges1) {
                Node _x = edge.getNode1();
                Node _y = edge.getNode2();

                if (Edges.isUndirectedEdge(edge)) {
                    edges.add(Edges.directedEdge(_x, _y));
                    edges.add(Edges.directedEdge(_y, _x));
                } else {
                    edges.add(edge);
                }
            }

            for (Edge edge : edges) {
                Node _x = Edges.getDirectedEdgeTail(edge);
                Node _y = Edges.getDirectedEdgeHead(edge);

                if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
                    continue;
                }

                List<Node> hNeighbors = getHNeighbors(_x, _y, graph);
                List<Set<Node>> hSubsets = powerSet(hNeighbors);

                for (Set<Node> hSubset : hSubsets) {
                    if (!validSetByKnowledge(_x, _y, hSubset, false)) {
                        continue;
                    }

                    double deleteEval = deleteEval(_x, _y, hSubset, graph);
                    double evalScore = score + deleteEval;

                    TetradLogger.getInstance().log("edgeEvaluations", "Attempt removing " + _x + "-->" + _y + "(" +
                            evalScore + ")");

                    if (!(evalScore > bestScore)) {
                        continue;
                    }

                    if (!validDelete(_x, _y, hSubset, graph)) {
                        continue;
                    }

                    bestScore = evalScore;
                    x = _x;
                    y = _y;
                    t = hSubset;
                }

                // Why is this here??? jdramsey
//                if (Edges.isUndirectedEdge(edge)) {
//                    _x = edge.getNode1();
//                    _y = edge.getNode2();
//                } else {
//                    _x = Edges.getDirectedEdgeTail(edge);
//                    _y = Edges.getDirectedEdgeHead(edge);
//                }
//
//                if (!getKnowledge().noEdgeRequired(_x.getName(), _y.getName())) {
//                    continue;
//                }
//
//                hNeighbors = getHNeighbors(_x, _y, graph);
//                hSubsets = powerSet(hNeighbors);
//
//                for (Set<Node> hSubset1 : hSubsets) {
//                    if (!validSetByKnowledge(_x, _y, hSubset1, false)) {
//                        continue;
//                    }
//
//                    double deleteEval = deleteEval(_x, _y, hSubset1, graph);
//                    double evalScore = score + deleteEval;
//
//                    TetradLogger.getInstance().details("Attempt removing " + _x + "-->" + _y + "(" +
//                            evalScore + ")");
//
//                    if (!(evalScore > bestScore)) {
//                        continue;
//                    }
//
//                    if (!validDelete(_x, _y, hSubset1, graph)) {
//                        continue;
//                    }
//
//                    storeGraphDelete(graph, _x, _y, hSubset1, evalScore);
//
//                    bestScore = evalScore;
//                    x = _x;
//                    y = _y;
//                    t = hSubset1;
//                }
            }
            if (x != null) {
                delete(x, y, t, graph, bestScore, true);
                rebuildPattern(graph);
                score = bestScore;
            }
        } while (x != null);

        return score;
    }

    /**
     * Get all nodes that are connected to Y by an undirected edge and not adjacent to X.
     */
    private static List<Node> getTNeighbors(Node x, Node y, Graph graph) {
        List<Node> tNeighbors = new LinkedList<Node>(graph.getAdjacentNodes(y));
        tNeighbors.removeAll(graph.getAdjacentNodes(x));

        for (int i = tNeighbors.size() - 1; i >= 0; i--) {
            Node z = tNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                tNeighbors.remove(z);
            }
        }

        return tNeighbors;
    }

    /**
     * Get all nodes that are connected to Y by an undirected edge and adjacent to X
     */
    private static List<Node> getHNeighbors(Node x, Node y, Graph graph) {
        List<Node> hNeighbors = new LinkedList<Node>(graph.getAdjacentNodes(y));
        hNeighbors.retainAll(graph.getAdjacentNodes(x));

        for (int i = hNeighbors.size() - 1; i >= 0; i--) {
            Node z = hNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);
            if (!Edges.isUndirectedEdge(edge)) {
                hNeighbors.remove(z);
            }
        }

        return hNeighbors;
    }

    /**
     * Evaluate the Insert(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double insertEval(Node x, Node y, Set<Node> t, Graph graph) {
        Set<Node> set1 = new HashSet<Node>(findNaYX(x, y, graph));
        set1.addAll(t);
        set1.addAll(graph.getParents(y));
        Set<Node> set2 = new HashSet<Node>(set1);
        set1.add(x);
        return scoreGraphChange(y, set1, set2);
    }

    /**
     * Evaluate the Delete(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double deleteEval(Node x, Node y, Set<Node> h, Graph graph) {
        Set<Node> set1 = new HashSet<Node>(findNaYX(x, y, graph));
        set1.removeAll(h);
        set1.addAll(graph.getParents(y));
        Set<Node> set2 = new HashSet<Node>(set1);
        set1.remove(x);
        set2.add(x);
        return scoreGraphChange(y, set1, set2);
    }

    /*
    * Do an actual insertion
    * (Definition 12 from Chickering, 2002).
    **/

    private void insert(Node x, Node y, Set<Node> subset, Graph graph, double score, boolean log) {
        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        graph.addDirectedEdge(x, y);

        if (log) {
            String label = trueGraph != null && trueEdge != null ? "*" : "";
            TetradLogger.getInstance().log("insertedEdges", graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) + " (" +
                    nf.format(score) + ") " + label);
        }

        for (Node node : subset) {
            Edge oldEdge = graph.getEdge(node, y);

            graph.removeEdge(node, y);
            graph.addDirectedEdge(node, y);

            if (log) {
                TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                        graph.getEdge(node, y));
            }
        }
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private void delete(Node x, Node y, Set<Node> subset, Graph graph, double score, boolean log) {

        if (log) {
            TetradLogger.getInstance().log("deletedEdges", graph.getNumEdges() + ". DELETE " + graph.getEdge(x, y) + subset.toString() + " (" +
                    nf.format(score) + ")");
        }

        graph.removeEdges(x, y);

        for (Node a : subset) {
            if (!graph.isParentOf(a, x) && !graph.isParentOf(x, a)) {
                graph.removeEdge(x, a);
                graph.addDirectedEdge(x, a);

                if (log) {
                    Edge oldEdge = graph.getEdge(x, a);
                    TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                            graph.getEdge(x, a));
                }
            }

            graph.removeEdge(y, a);
            graph.addDirectedEdge(y, a);

            if (log) {
                Edge oldEdge = graph.getEdge(y, a);
                TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                        graph.getEdge(y, a));
            }
        }
    }

    /*
     * Test if the candidate insertion is a valid operation
     * (Theorem 15 from Chickering, 2002).
     **/

    private boolean validInsert(Node x, Node y, Set<Node> subset, Graph graph) {
        List<Node> naYXT = new LinkedList<Node>(subset);
        naYXT.addAll(findNaYX(x, y, graph));

        if (!isClique(naYXT, graph)) {
            return false;
        }

        if (!isSemiDirectedBlocked(x, y, naYXT, graph, new HashSet<Node>())) {
            return false;
        }

        return true;
    }

    /**
     * Test if the candidate deletion is a valid operation (Theorem 17 from Chickering, 2002).
     */
    private static boolean validDelete(Node x, Node y, Set<Node> h,
                                       Graph graph) {
        List<Node> naYXH = findNaYX(x, y, graph);
        naYXH.removeAll(h);

        if (!isClique(naYXH, graph)) {
            return false;
        }

        return true;
    }

    //---Background knowledge methods.

    private void addRequiredEdges(Graph graph) {
        for (Iterator<KnowledgeEdge> it =
                this.getKnowledge().requiredEdgesIterator(); it.hasNext();) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (!graph.isAncestorOf(nodeB, nodeA)) {
                graph.removeEdges(nodeA, nodeB);
                graph.addDirectedEdge(nodeA, nodeB);
            }
        }
        for (Iterator<KnowledgeEdge> it =
                getKnowledge().forbiddenEdgesIterator(); it.hasNext();) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (nodeA != null && nodeB != null && graph.isAdjacentTo(nodeA, nodeB) &&
                    !graph.isChildOf(nodeA, nodeB)) {
//                System.out.println(graph);
                if (!graph.isAncestorOf(nodeA, nodeB)) {
                    graph.removeEdges(nodeA, nodeB);
                    graph.addDirectedEdge(nodeB, nodeA);
                }
            }
        }
    }

    /**
     * Use background knowledge to decide if an insert or delete operation does not orient edges in a forbidden
     * direction according to prior knowledge. If some orientation is forbidden in the subset, the whole subset is
     * forbidden.
     */
    private boolean validSetByKnowledge(Node x, Node y, Set<Node> subset,
                                        boolean insertMode) {
        if (insertMode) {
            for (Node aSubset : subset) {
                if (getKnowledge().edgeForbidden(aSubset.getName(),
                        y.getName())) {
                    return false;
                }
            }
        } else {
            for (Node nextElement : subset) {
                if (getKnowledge().edgeForbidden(x.getName(),
                        nextElement.getName())) {
                    return false;
                }
                if (getKnowledge().edgeForbidden(y.getName(),
                        nextElement.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    //--Auxiliary methods.

    /**
     * Find all nodes that are connected to Y by an undirected edge that are adjacent to X (that is, by undirected or
     * directed edge) NOTE: very inefficient implementation, since the current library does not allow access to the
     * adjacency list/matrix of the graph.
     */
    private static List<Node> findNaYX(Node x, Node y, Graph graph) {
        List<Node> naYX = new LinkedList<Node>(graph.getAdjacentNodes(y));
        naYX.retainAll(graph.getAdjacentNodes(x));

        for (int i = 0; i < naYX.size(); i++) {
            Node z = naYX.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                naYX.remove(z);
            }
        }

        return naYX;
    }

    /**
     * Returns true iif the given set forms a clique in the given graph.
     */
    private static boolean isClique(List<Node> set, Graph graph) {
        List<Node> setv = new LinkedList<Node>(set);
        for (int i = 0; i < setv.size() - 1; i++) {
            for (int j = i + 1; j < setv.size(); j++) {
                if (!graph.isAdjacentTo(setv.get(i), setv.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies if every semidirected path from y to x contains a node in naYXT.
     */
    private boolean isSemiDirectedBlocked(Node x, Node y, List<Node> naYXT,
                                          Graph graph, Set<Node> marked) {
        if (naYXT.contains(y)) {
            return true;
        }

        if (y == x) {
            return false;
        }

        for (Node node1 : graph.getNodes()) {
            if (node1 == y || marked.contains(node1)) {
                continue;
            }

            if (graph.isAdjacentTo(y, node1) && !graph.isParentOf(node1, y)) {
                marked.add(node1);

                if (!isSemiDirectedBlocked(x, node1, naYXT, graph, marked)) {
                    return false;
                }

                marked.remove(node1);
            }
        }

        return true;
    }

    private static List<Set<Node>> powerSet(List<Node> nodes) {
        List<Set<Node>> subsets = new ArrayList<Set<Node>>();
        int total = (int) Math.pow(2, nodes.size());
        for (int i = 0; i < total; i++) {
            Set<Node> newSet = new HashSet<Node>();
            String selection = Integer.toBinaryString(i);
            for (int j = selection.length() - 1; j >= 0; j--) {
                if (selection.charAt(j) == '1') {
                    newSet.add(nodes.get(selection.length() - j - 1));
                }
            }
            subsets.add(newSet);
        }
        return subsets;
    }


    /**
     * Completes a pattern that was modified by an insertion/deletion operator Based on the algorithm described on
     * Appendix C of (Chickering, 2002).
     */
    private void rebuildPattern(Graph graph) {
        SearchGraphUtils.basicPattern(graph);
        addRequiredEdges(graph);
        pdagWithBk(graph, getKnowledge());
        TetradLogger.getInstance().log("rebuiltPatterns", "Rebuilt graph = " + graph);
    }

    /**
     * Fully direct a graph with background knowledge. I am not sure how to adapt Chickering's suggested algorithm above
     * (dagToPdag) to incorporate background knowledge, so I am also implementing this algorithm based on Meek's 1995
     * UAI paper. Notice it is the same implemented in PcSearch. </p> *IMPORTANT!* *It assumes all colliders are
     * oriented, as well as arrows dictated by time order.*
     */
    private void pdagWithBk(Graph graph, Knowledge knowledge) {
        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);
    }

    private void setDataSets(List<ICovarianceMatrix> covs) {
        this.covs = covs;

        this.variables = covs.get(0).getVariables();
        List<String> varNames = covs.get(0).getVariableNames();
        this.varNames = varNames.toArray(new String[varNames.size()]);

        this.variances = new ArrayList<DoubleMatrix2D>();

        for (int i = 0; i < covs.size(); i++) {
            this.variances.add(covs.get(i).getMatrix());
        }
    }

    private List<String> toLowerCase(List<String> varNames) {
        List<String> _varNames = new ArrayList<String>();
        for (String name : varNames) {
            _varNames.add(name.toLowerCase());
        }
        return _varNames;
    }

    private void buildIndexing(Graph graph) {
        this.hashIndices = new HashMap<Node, Integer>();
        for (Node next : graph.getNodes()) {
            for (int i = 0; i < this.varNames.length; i++) {
                if (this.varNames[i].equals(next.getName())) {
                    this.hashIndices.put(next, i);
                    break;
                }
            }
        }
    }

    private static int getRowIndex(int dim[], int[] values) {
        int rowIndex = 0;
        for (int i = 0; i < dim.length; i++) {
            rowIndex *= dim[i];
            rowIndex += values[i];
        }
        return rowIndex;
    }

//    private static void print(String message) {
//        LogUtils.getInstance().fine(message);
//    }

    //===========================SCORING METHODS===========================//

    public double scoreGraph(Graph graph) {
        Graph dag = SearchGraphUtils.dagFromPattern(graph);
//        Graph dag = new EdgeListGraph(graph);
//        SearchGraphUtils.pdagToDag(dag);
        double score = 0.;

        for (Node next : dag.getNodes()) {
            Collection<Node> parents = dag.getParents(next);
            int nextIndex = -1;
            for (int i = 0; i < getVariables().size(); i++) {
                if (this.varNames[i].equals(next.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int parentIndices[] = new int[parents.size()];
            Iterator<Node> pi = parents.iterator();
            int count = 0;
            while (pi.hasNext()) {
                Node nextParent = pi.next();
                for (int i = 0; i < getVariables().size(); i++) {
                    if (this.varNames[i].equals(nextParent.getName())) {
                        parentIndices[count++] = i;
                        break;
                    }
                }
            }

            score += localSemScore(nextIndex, parentIndices);
        }

        return score;
    }

    private double scoreGraphChange(Node y, Set<Node> parents1,
                                    Set<Node> parents2) {
        int yIndex = hashIndices.get(y);
        int parentIndices1[] = new int[parents1.size()];

        int count = 0;
        for (Node aParents1 : parents1) {
            parentIndices1[count++] = (hashIndices.get(aParents1));
        }

        int parentIndices2[] = new int[parents2.size()];

        int count2 = 0;
        for (Node aParents2 : parents2) {
            parentIndices2[count2++] = (hashIndices.get(aParents2));
        }

        double score1 = localSemScore(yIndex, parentIndices1);
        double score2 = localSemScore(yIndex, parentIndices2);

        return score1 - score2;
    }

    /**
     * Calculates the sample likelihood and BIC score for i given its parents in a simple SEM model.
     */
    private double localSemScore(int i, int[] parents) {
        double sum = 0.0;
        int count = 0;

        for (int dataIndex = 0; dataIndex < covs.size(); dataIndex++) {
            double score = localSemScoreOneDataSet(dataIndex, i, parents);

//            logger.details("Score for data sets #" + (dataIndex + 1) + " = " + nf.format(score));

            if (Double.isNaN(score)) {
                continue;
            }

            sum += score;
            count++;
        }

//        logger.details("Overall score = " + nf.format(sum));

        return sum / count;
//        return sum;
    }

    private double localSemScoreOneDataSet(int dataIndex, int i, int[] parents) {
//        ICovarianceMatrix covarianceMatrix = covs.get(dataIndex);

        double c = getPenaltyDiscount();
        int n = covs.get(dataIndex).getSampleSize();
        double k = parents.length + 1;

        // Calculate the unexplained variance of i given z1,...,zn
        // considered as a naive Bayes model.
        double variance = getCovMatrices().get(dataIndex).get(i, i);
//        int n = dataSets().get(dataIndex).getNumRows();

        if (parents.length > 0) {

            // Regress z onto i, yielding regression coefficients b.
            DoubleMatrix2D Czz =
                    getCovMatrices().get(dataIndex).viewSelection(parents, parents);
            DoubleMatrix2D inverse;
            try {
                inverse = algebra().inverse(Czz);
//                inverse = MatrixUtils.ginverse(Czz);
            }
            catch (Exception e) {
                StringBuilder buf = new StringBuilder();
                buf.append("Could not invert matrix for variables: ");

                for (int j = 0; j < parents.length; j++) {
                    buf.append(variables.get(parents[j]));

                    if (j < parents.length - 1) {
                        buf.append(", ");
                    }
                }

//                throw new IllegalArgumentException(buf.toString());
                return Double.NaN;
            }

            DoubleMatrix1D Cyz = getCovMatrices().get(dataIndex).viewColumn(i);
            Cyz = Cyz.viewSelection(parents);
            DoubleMatrix1D b = algebra().mult(inverse, Cyz);

            variance -= algebra().mult(Cyz, b);
        }

//        int n2 = 0;
//
//        for (DataSet _dataSet : dataSets) {
//            n2 *= _dataSet.getNumRows();
//        }

        // Notice when it sums it up for all data sets it will be m * k parameters.
        return -n * Math.log(variance) - n * Math.log(2. * Math.PI) - n
                - c * k * Math.log(n);
//        return -n * Math.log(variance) - k * c * Math.log(n);
    }

    private int sampleSize() {
//        System.out.println("sample size = " + sampleSize);
        return this.sampleSize;

//        return dataSet().getNumRows();
    }

    private List<Node> getVariables() {
        return variables;
    }

    private List<DoubleMatrix2D> getCovMatrices() {
        return variances;
    }

    private Algebra algebra() {
        return algebra;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    private void fireGraphChange(Graph graph) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(new PropertyChangeEvent(this, "graph", null, graph));
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getListeners().add(l);
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
//        if (penaltyDiscount < 0) {
//            throw new IllegalArgumentException("Penalty discount must be >= 0: "
//                    + penaltyDiscount);
//        }

        this.penaltyDiscount = penaltyDiscount;
    }

    public int getMaxNumEdges() {
        return maxNumEdges;
    }

    public void setMaxNumEdges(int maxNumEdges) {
        if (maxNumEdges < -1) throw new IllegalArgumentException();

        this.maxNumEdges = maxNumEdges;
    }

    public double getModelScore() {
        return bic;
    }

    public double getScore(Graph dag) {
        return scoreGraph(dag);
    }

    public int getNumPatternsToStore() {
        return numPatternsToStore;
    }

    public void setNumPatternsToStore(int numPatternsToStore) {
        if (numPatternsToStore < 1) {
            throw new IllegalArgumentException("Must store at least one pattern: " + numPatternsToStore);
        }

        this.numPatternsToStore = numPatternsToStore;
    }

    public String bootstrapPercentagesString(int numBootstraps) {
        if (returnGraph == null) {
            returnGraph = search();
        }

        StringBuilder builder = new StringBuilder(
                "For " + numBootstraps + " repetitions, the percentage of repetitions in which each " +
                        "edge occurs in the IMaGES pattern for that repetition. In each repetition, for each " +
                        "input data set, a sample the size of that data set chosen randomly and with replacement. " +
                        "Images is run on the collection of these data sets. 100% for an edge means that that " +
                        "edge occurs in all such randomly chosen samples, over " + numBootstraps + " repetitions; " +
                        "0% means it never occurs. Edges not mentioned occur in 0% of the random samples.\n\n"
        );

        Map<Edge, Integer> counts = getBoostrapCounts(numBootstraps);
        builder.append(edgePercentagesString(counts, returnGraph.getEdges(), "The estimated pattern", null, numBootstraps));

        return builder.toString();
    }

    public String gesCountsString() {
        return "counts";
//        if (returnGraph == null) {
//            returnGraph = search();
//        }
//        Map<Edge, Integer> counts = getGesCounts(dataSets(), returnGraph.getNodes(), getKnowledge(), getPenaltyDiscount());
//        return gesEdgesString(counts, dataSets());
    }

    private Map<Edge, Integer> getGesCounts(List<DataSet> dataSets, List<Node> nodes, Knowledge knowledge, double penalty) {
        if (returnGraph == null) {
            returnGraph = search();
        }

        Map<Edge, Integer> counts = new HashMap<Edge, Integer>();

        for (DataSet dataSet : dataSets) {
            Ges ges = new Ges(dataSet);
            ges.setKnowledge(knowledge);
            ges.setPenaltyDiscount(penalty);
            Graph pattern = ges.search();

            incrementCounts(counts, pattern, nodes);
        }

        return counts;
    }

    public Map<Edge, Double> averageStandardizedCoefficients() {
        if (returnGraph == null) {
            returnGraph = search();
        }

        return averageStandardizedCoefficients(returnGraph);
    }

    public String averageStandardizedCoefficientsString() {
        if (returnGraph == null) {
            returnGraph = search();
        }

        Graph graph = GraphUtils.randomDag(returnGraph.getNodes(), 12, true);
        return averageStandardizedCoefficientsString(graph);
    }

    public String averageStandardizedCoefficientsString(Graph graph) {
        Map<Edge, Double> coefs = averageStandardizedCoefficients(graph);
        return edgeCoefsString(coefs, graph.getEdges(), "Estimated graph",
                "Average standardized coefficient");
    }

    public String logEdgeBayesFactorsString(Graph dag) {
        Map<Edge, Double> coefs = logEdgeBayesFactors(dag);
        return logBayesPosteriorFactorsString(coefs, scoreGraph(dag), dag.getEdges());
    }

    public Map<Edge, Double> logEdgeBayesFactors(Graph dag) {
        Map<Edge, Double> logBayesFactors = new HashMap<Edge, Double>();
        double withEdge = scoreGraph(dag);

        for (Edge edge : dag.getEdges()) {
            dag.removeEdge(edge);
            double withoutEdge = scoreGraph(dag);
            double difference = withoutEdge - withEdge;
            logBayesFactors.put(edge, difference);
            dag.addEdge(edge);
        }

        return logBayesFactors;
    }


    private Edge translateEdge(Edge edge, Graph graph) {
        Node node1 = graph.getNode(edge.getNode1().getName());
        Node node2 = graph.getNode(edge.getNode2().getName());
        return new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());
    }

    private String gesEdgesString(Map<Edge, Integer> counts, List<DataSet> dataSets) {
        if (returnGraph == null) {
            returnGraph = search();
        }

        return edgePercentagesString(counts, returnGraph.getEdges(), "Estimated graph",
                "Percentage of GES results each edge participates in", dataSets.size());
    }

    private void incrementCounts(Map<Edge, Integer> counts, Graph pattern, List<Node> nodes) {
        Graph _pattern = GraphUtils.replaceNodes(pattern, nodes);

        for (Edge e : _pattern.getEdges()) {
            if (counts.get(e) == null) {
                counts.put(e, 0);
            }

            counts.put(e, counts.get(e) + 1);
        }
    }

    /**
     * Prints edge counts, with edges in the order of the adjacencies in <code>edgeList</code>.
     *
     * @param counts           A map from edges to counts.
     * @param edgeList         A list of edges, the true edges or estimated edges.
     * @param edgeListLabel    A label for the edge list, e.g. "True edges" or "Estimated edges".
     * @param percentagesLabel
     * @param numBootstraps
     */
    private String edgePercentagesString(Map<Edge, Integer> counts, List<Edge> edgeList, String edgeListLabel,
                                         String percentagesLabel, int numBootstraps) {
        NumberFormat nf = new DecimalFormat("0");
        StringBuilder builder = new StringBuilder();

        if (percentagesLabel != null) {
            builder.append("\n" + percentagesLabel + ":\n\n");
        }

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            int total = 0;

            for (Edge _edge : new HashMap<Edge, Integer>(counts).keySet()) {
                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
                    total += counts.get(_edge);
                    double percentage = counts.get(_edge) / (double) numBootstraps * 100.;
                    builder.append((i + 1) + ". " + _edge + " " + nf.format(percentage) + "%\n");
                    counts.remove(_edge);
                }
            }

            double percentage = total / (double) numBootstraps * 100.;
            builder.append("   (Sum = " + nf.format(percentage) + "%)\n\n");
        }

        // The left over edges.
        builder.append("Edges not adjacent in the estimated pattern:\n\n");

//        for (Edge edge : counts.keySet()) {
//            double percentage = counts.get(edge) / (double) numBootstraps * 100.;
//            builder.append(edge + " " + nf.format(percentage) + "%\n");
//        }

        for (Edge edge : new ArrayList<Edge>(counts.keySet())) {
            if (!counts.keySet().contains(edge)) continue;

            int total = 0;

            for (Edge _edge : new HashMap<Edge, Integer>(counts).keySet()) {
                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
                    total += counts.get(_edge);
                    double percentage = counts.get(_edge) / (double) numBootstraps * 100.;
                    builder.append(_edge + " " + nf.format(percentage) + "%\n");
                    counts.remove(_edge);
                }
            }

            double percentage = total / (double) numBootstraps * 100.;
            builder.append("   (Sum = " + nf.format(percentage) + "%)\n\n");
        }

        builder.append("\nThe estimated pattern, for reference:\n\n");

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            builder.append(((i + 1) + ". " + edge + "\n"));
        }

        return builder.toString();
    }

    private String edgeCoefsString(Map<Edge, Double> coefs, List<Edge> edgeList, String edgeListLabel,
                                   String percentagesLabel) {
        NumberFormat nf = new DecimalFormat("0.00");
        StringBuilder builder = new StringBuilder();

        builder.append("\n" + edgeListLabel + ":\n\n");

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);
            builder.append(((i + 1) + ". " + edge + "\n"));
        }

        builder.append("\n" + percentagesLabel + ":\n\n");

        for (int i = 0; i < edgeList.size(); i++) {
            Edge edge = edgeList.get(i);

            for (Edge _edge : new HashMap<Edge, Double>(coefs).keySet()) {
                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
                    double coef = coefs.get(_edge);
                    builder.append((i + 1) + ". " + _edge + " " + nf.format(coef) + "\n");
                    coefs.remove(_edge);
                }
            }
        }


        return builder.toString();
    }

    private String logBayesPosteriorFactorsString(final Map<Edge, Double> coefs, double modelScore, List<Edge> edgeList) {
        NumberFormat nf = new DecimalFormat("0.00");
        StringBuilder builder = new StringBuilder();

        SortedMap<Edge, Double> sortedCoefs = new TreeMap<Edge, Double>(new Comparator<Edge>() {
            public int compare(Edge edge1, Edge edge2) {
                return new Double(coefs.get(edge1)).compareTo(new Double(coefs.get(edge2)));
            }
        });

        sortedCoefs.putAll(coefs);

        builder.append("Model score: " + nf.format(modelScore) + "\n\n");

        builder.append("Edge Posterior Log Bayes Factors:\n\n");

        builder.append("For a DAG in the IMaGES pattern with model score m, for each edge e in the " +
                "DAG, the model score that would result from removing each edge, calculating " +
                "the resulting model score m(e), and then reporting m(e) - m. The score used is " +
                "the IMScore, L - SUM_i{kc ln n(i)}, L is the maximum likelihood of the model, " +
                "k isthe number of parameters of the model, n(i) is the sample size of the ith " +
                "data set, and c is the penalty discount. Note that the more negative the score, " +
                "the more important the edge is to the posterior probability of the IMaGES model. " +
                "Edges are given in order of their importance so measured.\n\n");

        int i = 0;

        for (Edge edge : sortedCoefs.keySet()) {
            builder.append((++i) + ". " + edge + " " + nf.format(sortedCoefs.get(edge)) + "\n");
        }


//        for (int i = 0; i < edgeList.size(); i++) {
//            Edge edge = edgeList.get(i);
//
//            for (Edge _edge : new HashMap<Edge, Double>(sortedCoefs).keySet()) {
//                if (_edge.getNode1() == edge.getNode1() && _edge.getNode2() == edge.getNode2()
//                        || _edge.getNode1() == edge.getNode2() && _edge.getNode2() == edge.getNode1()) {
//                    double coef = sortedCoefs.get(_edge);
//                    builder.append((i + 1) + ". " + _edge + " " + nf.format(coef) + "\n");
//                    sortedCoefs.remove(_edge);
//                }
//            }
//        }


        return builder.toString();
    }

    public void setStructurePrior(double structurePrior) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSamplePrior(double samplePrior) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public SortedSet<ScoredGraph> getTopGraphs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<Edge, Integer> getBoostrapCounts(int numBootstraps) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<Edge, Double> averageStandardizedCoefficients(Graph graph) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

//    public void setfCutoffP(double v) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void setUseFCutoff(boolean useFCutoff) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }


    public void setMinJump(double minJump) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}