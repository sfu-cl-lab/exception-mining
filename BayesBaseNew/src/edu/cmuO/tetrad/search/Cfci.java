package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * Implements the FCI ("Fast Causal Inference") algorithm from Chapter 6 of
 * Spirtes, Glymour and Scheines, "Causation, Prediction, and Search," 2nd
 * edition, chapter 6, with a modified arrow-complete rule set, and using local
 * collider orientation instead of sepset collider orientation where possible.
 *
 * @author Erin Korber, June 2004
 * @author Joseph Ramsey, modifications 1/2005
 * @version $Revision: 4617 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class Cfci {

    /**
     * The PAG being constructed.
     */
    private Pag graph;

    /**
     * The SepsetMap being constructed.
     */
    private SepsetMap sepset = new SepsetMap();

    /**
     * The background knowledge.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * The independence test.
     */
    private IndependenceTest independenceTest;

    /**
     * change flag for repeat rules
     */
    private boolean changeFlag = true;

    /**
     * The depth for the fast adjacency search.
     */
    private int depth = -1;

    /**
     * Elapsed time of the last search.
     */
    private long elapsedTime;


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

    /**
     * Returns the maximum size of conditioning sets for independence tests.
     */
    private int getDepth() {
        return depth;
    }

    /**
     * Sets the maximum size of conditioning sets for independence tests.
     */
    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0: " + depth);
        }

        this.depth = depth;
    }

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a new FCI search for the given independence test and
     * background knowledge.
     */
    public Cfci(IndependenceTest independenceTest,
                Knowledge knowledge) {
        if (independenceTest == null || knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.knowledge = knowledge;
    }

    //========================PUBLIC METHODS==========================//

    /**
     * Runs the FCI algorithm and returns a PAG.
     */
    public Graph search() {
        long beginTime = System.currentTimeMillis();
        TetradLogger.getInstance().info("Starting CFCI algorithm.");
        TetradLogger.getInstance().info("Independence test = " + independenceTest + ".");

        List<Node> variables = independenceTest.getVariables();
        List<Node> nodes = new LinkedList<Node>();
        allTriples = new HashSet<Triple>();
        this.ambiguousTriples = new HashSet<Triple>();
        this.colliderTriples = new HashSet<Triple>();
        this.noncolliderTriples = new HashSet<Triple>();

        for (Node variable : variables) {
            nodes.add(variable);
        }

        this.graph = new Pag(nodes);

        // Step FCI A.
        getGraph().fullyConnect(Endpoint.CIRCLE);

        // Step FCI B.
//        FastAdjacencySearch fas =
//                new FastAdjacencySearch(graph, independenceTest);
//        fas.setKnowledge(getKnowledge());
//        fas.setDepth(getDepth());
//        this.sepset = fas.search();

        FastAdjacencySearchLo fas =
                new FastAdjacencySearchLo(getGraph(), independenceTest);
        fas.setKnowledge(knowledge);
        fas.setDepth(depth);
        fas.search();

        // Step FCI C
        long time1 = System.currentTimeMillis();
        orientUnshieldedTriples(independenceTest, depth);

        long time2 = System.currentTimeMillis();
        TetradLogger.getInstance().info("Step C: " + (time2 - time1) / 1000. + "s");

        // Step FCI D.
        long time3 = System.currentTimeMillis();

        PossibleDSepSearch3 possibleDSep =
                new PossibleDSepSearch3(getGraph(), sepset, independenceTest, getAmbiguousTriples());
        possibleDSep.setDepth(getDepth());
        possibleDSep.setKnowledge(getKnowledge());
        this.sepset = possibleDSep.search();

        System.out.println("###### Num dsep removals = " + sepset.size());

        // Reorient all edges as o-o.
        long time4 = System.currentTimeMillis();
        TetradLogger.getInstance().info("Step D: " + (time4 - time3) / 1000. + "s");

        getGraph().reorientAllWith(Endpoint.CIRCLE);

        // Step CI C
        long time5 = System.currentTimeMillis();
//        SearchGraphUtils.pcOrientbk(knowledge, graph, nodes);
        Cfci.fciOrientbk(getKnowledge(), getGraph(), independenceTest.getVariables());
        orientUnshieldedTriples(independenceTest, depth);

        long time6 = System.currentTimeMillis();
        TetradLogger.getInstance().info("Step CI C: " + (time6 - time5) / 1000. + "s");

        // Step CI D.
        doFinalOrientation();

        TetradLogger.getInstance().log("graph", "\nReturning this graph: " + getGraph());

        TetradLogger.getInstance().details("\nCollider triples judged from sepsets:");

        for (Triple triple : getColliderTriples()) {
            TetradLogger.getInstance().details("Collider: " + triple);
        }

        TetradLogger.getInstance().details("\nNoncollider triples judged from sepsets:");

        for (Triple triple : getNoncolliderTriples()) {
            TetradLogger.getInstance().details("Noncollider: " + triple);
        }

        TetradLogger.getInstance().details("\nAmbiguous triples judged from sepsets:");

        for (Triple triple : getAmbiguousTriples()) {
            TetradLogger.getInstance().details("Ambiguous: " + triple);
        }

//        graph.closeInducingPaths();   //to make sure it's a legal PAG

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - beginTime;

        TetradLogger.getInstance().info("\n\nElapsed time = " + (elapsedTime) / 1000. + " s");
        TetradLogger.getInstance().info("\nFinishing CFCI algorithm.");
        return getGraph();
    }

    //===========================PRIVATE METHODS=========================//

    private SepsetMap getSepset() {
        return sepset;
    }

    private void orientUnshieldedTriples(
            IndependenceTest test, int depth) {
        TetradLogger.getInstance().info("Starting Collider Orientation:");
        colliderTriples = new HashSet<Triple>();
        noncolliderTriples = new HashSet<Triple>();
        ambiguousTriples = new HashSet<Triple>();

        for (Node y : getGraph().getNodes()) {
            List<Node> adjacentNodes = getGraph().getAdjacentNodes(y);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node x = adjacentNodes.get(combination[0]);
                Node z = adjacentNodes.get(combination[1]);

                if (this.getGraph().isAdjacentTo(x, z)) {
                    continue;
                }

                allTriples.add(new Triple(x, y, z));
                TripleType type = getTripleType(x, y, z, test, depth);

                if (type == TripleType.COLLIDER) {
                    if (isArrowpointAllowed(x, y) &&
                            isArrowpointAllowed(z, y)) {
                        getGraph().setEndpoint(x, y, Endpoint.ARROW);
                        getGraph().setEndpoint(z, y, Endpoint.ARROW);
                        TetradLogger.getInstance().colliderOriented(SearchLogUtils.colliderOrientedMsg(x, y, z));
                    }

                    colliderTriples.add(new Triple(x, y, z));
                }
                else if (type == TripleType.NONCOLLIDER) {
                    noncolliderTriples.add(new Triple(x, y, z));
                }
                else {
                    Triple triple = new Triple(x, y, z);
                    ambiguousTriples.add(triple);
                    getGraph().setAmbiguous(triple);
                }
            }
        }

        TetradLogger.getInstance().info("Finishing Collider Orientation.");
    }

    private TripleType getTripleType(Node x, Node y, Node z,
                                     IndependenceTest test, int depth) {
        boolean existsSepsetContainingY = false;
        boolean existsSepsetNotContainingY = false;

        Set<Node> __nodes = new HashSet<Node>(this.getGraph().getAdjacentNodes(x));
        __nodes.remove(z);

        List<Node> _nodes = new LinkedList<Node>(__nodes);
        TetradLogger.getInstance().details("Adjacents for " + x + "--" + y + "--" + z + " = " + _nodes);

        int _depth = depth;
        if (_depth == -1) {
            _depth = Integer.MAX_VALUE;
        }
        _depth = Math.min(_depth, _nodes.size());

        for (int d = 0; d <= _depth; d++) {
            ChoiceGenerator cg = new ChoiceGenerator(_nodes.size(), d);
            int[] choice;

            while ((choice = cg.next()) != null) {
                List<Node> condSet = asList(choice, _nodes);

                if (test.isIndependent(x, z, condSet)) {
                    if (condSet.contains(y)) {
                        existsSepsetContainingY = true;
                    }
                    else {
                        existsSepsetNotContainingY = true;
                    }
                }
            }
        }

        __nodes = new HashSet<Node>(this.getGraph().getAdjacentNodes(z));
        __nodes.remove(x);

        _nodes = new LinkedList<Node>(__nodes);
        TetradLogger.getInstance().details("Adjacents for " + x + "--" + y + "--" + z + " = " + _nodes);

        _depth = depth;
        if (_depth == -1) {
            _depth = Integer.MAX_VALUE;
        }
        _depth = Math.min(_depth, _nodes.size());

        for (int d = 0; d <= _depth; d++) {
            ChoiceGenerator cg = new ChoiceGenerator(_nodes.size(), d);
            int[] choice;

            while ((choice = cg.next()) != null) {
                List<Node> condSet = asList(choice, _nodes);

                if (test.isIndependent(x, z, condSet)) {
                    if (condSet.contains(y)) {
                        existsSepsetContainingY = true;
                    }
                    else {
                        existsSepsetNotContainingY = true;
                    }
                }
            }
        }

        // Note: Unless sepsets are being collected during fas, most likely
        // this will be null. (Only sepsets found during possible dsep search
        // will be here.)
        List<Node> condSet = getSepset().get(x, z);

        if (condSet != null) {
            if (condSet.contains(y)) {
                existsSepsetContainingY = true;
            }
            else {
                existsSepsetNotContainingY = true;
            }
        }

        if (existsSepsetContainingY == existsSepsetNotContainingY) {
            return TripleType.AMBIGUOUS;
        }
        else if (!existsSepsetNotContainingY) {
            return TripleType.NONCOLLIDER;
        }
        else {
            return TripleType.COLLIDER;
        }
    }

    private static List<Node> asList(int[] indices, List<Node> nodes) {
        List<Node> list = new LinkedList<Node>();

        for (int i : indices) {
            list.add(nodes.get(i));
        }

        return list;
    }

    private void doFinalOrientation() {
        while (changeFlag) {
            changeFlag = false;
            doubleTriangle();
            awayFromColliderAncestorCycle();
            discrimPaths();   //slowest, so do last
        }
    }

    /**
     * Orients according to background knowledge
     */
    private static void fciOrientbk(Knowledge bk, Graph graph, List<Node> variables) {
        for (Iterator<KnowledgeEdge> it =
                bk.forbiddenEdgesIterator(); it.hasNext();) {
            KnowledgeEdge edge = it.next();

            //match strings to variables in the graph.
            Node from = SearchGraphUtils.translate(edge.getFrom(), variables);
            Node to = SearchGraphUtils.translate(edge.getTo(), variables);

            if (from == null || to == null) {
                continue;
            }

            if (graph.getEdge(from, to) == null) {
                continue;
            }

            // Orient to*->from
            graph.setEndpoint(to, from, Endpoint.ARROW);
            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Knowledge", graph.getEdge(from, to)));
        }

        for (Iterator<KnowledgeEdge> it =
                bk.requiredEdgesIterator(); it.hasNext();) {
            KnowledgeEdge edge = it.next();

            //match strings to variables in this graph
            Node from = SearchGraphUtils.translate(edge.getFrom(), variables);
            Node to = SearchGraphUtils.translate(edge.getTo(), variables);

            if (from == null || to == null) {
                continue;
            }

            if (graph.getEdge(from, to) == null) {
                continue;
            }

            // Orient from-->to
            graph.setEndpoint(from, to, Endpoint.ARROW);
            graph.setEndpoint(to, from, Endpoint.TAIL);
            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Knowledge", graph.getEdge(from, to)));
        }
    }

    /**
     * Implements the double-triangle orientation rule, which states that if
     * D*-oB, A*->B<-*C and A*-*D*-*C is a noncollider, then D*->B.
     */
    private void doubleTriangle() {
        List<Node> nodes = getGraph().getNodes();
                                                                            for (Node B : nodes) {
            List<Node> intoBArrows = getGraph().getNodesInTo(B, Endpoint.ARROW);
            List<Node> intoBCircles = getGraph().getNodesInTo(B, Endpoint.CIRCLE);

            //possible A's and C's are those with arrows into B
            List<Node> possA = new LinkedList<Node>(intoBArrows);
            List<Node> possC = new LinkedList<Node>(intoBArrows);

            //possible D's are those with circles into B
            for (Node d : intoBCircles) {
                for (Node a : possA) {
                    for (Node c : possC) {
                        if (c == a) {
                            continue;
                        }

                        //skip anything not a double triangle
                        if (!getGraph().isAdjacentTo(a, d) ||
                                !getGraph().isAdjacentTo(c, d)) {
                            continue;
                        }

                        //skip if a*-*d*-*c is not a noncollider.
                        if (!getNoncolliderTriples().contains(new Triple(a, d, c))) {
                            continue;
                        }

                        //if all of the previous tests pass, orient d*-oB as d*->B
                        if (!isArrowpointAllowed(d, B)) {
                            continue;
                        }

                        getGraph().setEndpoint(d, B, Endpoint.ARROW);
                        TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Double triangle", getGraph().getEdge(d, B)));
                        changeFlag = true;
                    }
                }
            }
        }
    }

    //Does all 3 of these rules at once instead of going through all
    // triples multiple times per iteration of doFinalOrientation.
    private void awayFromColliderAncestorCycle() {
        List<Node> nodes = getGraph().getNodes();

        for (Node b : nodes) {
            List<Node> adj = getGraph().getAdjacentNodes(b);

            if (adj.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adj.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node A = adj.get(combination[0]);
                Node C = adj.get(combination[1]);

                //choice gen doesnt do diff orders, so must switch A & C around.
                awayFromCollider(A, b, C);
                awayFromCollider(C, b, A);
                awayFromAncestor(A, b, C);
                awayFromAncestor(C, b, A);
                awayFromCycle(A, b, C);
                awayFromCycle(C, b, A);
            }
        }
    }

    // if a*->Bo-oC and not a*-*c, then a*->b-->c
    // (orient either circle if present, don't need both)
    private void awayFromCollider(Node a, Node b, Node c) {
        Endpoint bc = getGraph().getEndpoint(b, c);
        Endpoint cb = getGraph().getEndpoint(c, b);

        if (!(getGraph().isAdjacentTo(a, c)) &&
                (getGraph().getEndpoint(a, b) == Endpoint.ARROW)) {
            if (!(getGraph().isAdjacentTo(a, c)) &&
                    (getGraph().getEndpoint(a, b) == Endpoint.ARROW)) {
                if (cb == Endpoint.CIRCLE || cb == Endpoint.TAIL) {
                    if (bc == Endpoint.CIRCLE) {
                        if (!isArrowpointAllowed(b, c)) {
                            return;
                        }

                        if (getNoncolliderTriples().contains(new Triple(a, b, c))) {
                            getGraph().setEndpoint(b, c, Endpoint.ARROW);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from collider", getGraph().getEdge(b, c)));
                            changeFlag = true;
                        }
                    }
                }
                if (bc == Endpoint.CIRCLE || bc == Endpoint.ARROW) {
                    if (cb == Endpoint.CIRCLE) {
                        if (getNoncolliderTriples().contains(new Triple(a, b, c))) {
                            getGraph().setEndpoint(c, b, Endpoint.TAIL);
                            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from collider", getGraph().getEdge(b, c)));
                            changeFlag = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * The triangles that must be oriented this way (won't be done by another
     * rule) all look like the ones below, where the dots are a collider path
     * from L to A with each node on the path (except L) a parent of C.
     * <pre>
     *          B
     *         xo           x is either an arrowhead or a circle
     *        /  \
     *       v    v
     * L....A --> C
     * </pre>
     */
    private void discrimPaths() {
        List<Node> nodes = getGraph().getNodes();

        for (Node b : nodes) {

            //potential A and C candidate pairs are only those
            // that look like this:   A<-oBo->C  or  A<->Bo->C

            List<Node> possAandC = getGraph().getNodesOutTo(b, Endpoint.ARROW);
            List<Node> possA = new LinkedList<Node>(possAandC);

            //keep arrows and circles
            possA.removeAll(getGraph().getNodesInTo(b, Endpoint.TAIL));
            List<Node> possC = new LinkedList<Node>(possAandC);

            //keep only circles
            possC.retainAll(getGraph().getNodesInTo(b, Endpoint.CIRCLE));

            for (Node a : possA) {
                for (Node c : possC) {
                    if (!getGraph().isParentOf(a, c)) {
                        continue;
                    }

                    LinkedList<Node> reachable = new LinkedList<Node>();
                    reachable.add(a);
                    reachablePathFind(a, b, c, reachable);
                }
            }
        }
    }

    /**
     * a method to search "back from a" to find a DDP. It is called with a
     * reachability list (first consisting only of a). This is breadth-first,
     * utilizing "reachability" concept from Geiger, Verma, and Pearl 1990. </p>
     * The body of a DDP consists of colliders that are parents of c.
     */
    private void reachablePathFind(Node a, Node b, Node c,
                                   LinkedList<Node> reachable) {
        Set<Node> cParents = new HashSet<Node>(getGraph().getParents(c));

        //needed to avoid cycles in failure case
        Set<Node> visited = new HashSet<Node>();
        visited.add(b);
        visited.add(c);

        // We don't want to include a,b,or c on the path, so they are added to
        // the "visited"  set.  b and c are added explicitly here; a will be
        // added in the first while iteration.
        while (reachable.size() > 0) {
            Node x = reachable.removeFirst();
            visited.add(x);

            // Possible DDP path endpoints.
            List<Node> intoX = getGraph().getNodesInTo(x, Endpoint.ARROW);
            intoX.removeAll(visited);

            for (Node node : intoX) {

                // If its into a reachable node and not a parent of c, its a DDP
                // endpoint
                if (!getGraph().isAdjacentTo(node, c)) {
                    doDdpOrientation(node, a, b, c);

                    // Orientation done for this a,b,c set; stop search.
                    return;
                }
                else if (cParents.contains(node)) {

                    // If the node node is a parent of c and node<->x, it is reachable.
                    if (getGraph().getEndpoint(x, node) == Endpoint.ARROW) {
                        reachable.add(node);
                    }
                }
            }
        }
    }

    /**
     * Orients the edges inside the definite discriminating path triangle. Takes
     * the left endpoint, and a,b,c as arguments.
     */
    private void doDdpOrientation(Node l, Node a, Node b, Node c) {
        if (getColliderTriples().contains(new Triple(l, b, c))) {
            if (!isArrowpointAllowed(a, b)) {
                return;
            }

            if (!isArrowpointAllowed(c, b)) {
                return;
            }

            getGraph().setEndpoint(a, b, Endpoint.ARROW);
            getGraph().setEndpoint(c, b, Endpoint.ARROW);
            TetradLogger.getInstance().colliderOriented(SearchLogUtils.colliderOrientedMsg("DDP", a, b, c));
            changeFlag = true;
        }
        else if (getNoncolliderTriples().contains(new Triple(l, b, c))) {
            getGraph().setEndpoint(c, b, Endpoint.TAIL);
            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("DDP", getGraph().getEdge(c, b)));
            changeFlag = true;
        }
    }

    //if a*-oC and either a-->b*->c or a*->b-->c, then a*->c
    private void awayFromAncestor(Node a, Node b, Node c) {
        if ((getGraph().isAdjacentTo(a, c)) &&
                (getGraph().getEndpoint(a, c) == Endpoint.CIRCLE)) {

            if ((getGraph().getEndpoint(a, b) == Endpoint.ARROW) &&
                    (getGraph().getEndpoint(b, c) == Endpoint.ARROW) && (
                    (getGraph().getEndpoint(b, a) == Endpoint.TAIL) ||
                            (getGraph().getEndpoint(c, b) == Endpoint.TAIL))) {

                if (!isArrowpointAllowed(a, c)) {
                    return;
                }

                getGraph().setEndpoint(a, c, Endpoint.ARROW);
                TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from ancestor", getGraph().getEdge(a, c)));
                changeFlag = true;
            }
        }
    }

    //if Ao->c and a-->b-->c, then a-->c
    private void awayFromCycle(Node a, Node b, Node c) {
        if (getGraph().isAdjacentTo(a, c) &&
                getGraph().getEndpoint(a, c) == Endpoint.ARROW &&
                getGraph().getEndpoint(c, a) == Endpoint.CIRCLE &&
                getGraph().isDirectedFromTo(a, b) && getGraph().isDirectedFromTo(b, c)) {
            getGraph().setEndpoint(c, a, Endpoint.TAIL);
            TetradLogger.getInstance().edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from cycle", getGraph().getEdge(c, a)));
            changeFlag = true;
        }
    }

    private boolean isArrowpointAllowed(Node x, Node y) {
        if (getGraph().getEndpoint(x, y) == Endpoint.ARROW) {
            return true;
        }

        if (getGraph().getEndpoint(x, y) == Endpoint.TAIL) {
            return false;
        }

        if (getGraph().getEndpoint(y, x) == Endpoint.ARROW &&
                getGraph().getEndpoint(x, y) == Endpoint.CIRCLE) {
            return true;
        }

        return !knowledge.isForbiddenByTiers(x.getName(), y.getName());
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public Set<Triple> getColliderTriples() {
        return new HashSet<Triple>(colliderTriples);
    }

    public Set<Triple> getNoncolliderTriples() {
        return new HashSet<Triple>(noncolliderTriples);
    }

    public Set<Triple> getAmbiguousTriples() {
        return new HashSet<Triple>(ambiguousTriples);
    }

    private Knowledge getKnowledge() {
        return knowledge;
    }

    private Pag getGraph() {
        return graph;
    }

    //==============================CLASSES==============================//

    private enum TripleType {
        COLLIDER, NONCOLLIDER, AMBIGUOUS}
}
