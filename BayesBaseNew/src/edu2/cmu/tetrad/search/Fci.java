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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.KnowledgeEdge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;


/**
 * Extends Erin Korber's implementation of the Fast Causal Inference algorithm (found in Fci.java) with Jiji Zhang's
 * Augmented FCI rules (found in sec. 4.1 of Zhang's 2006 PhD dissertation, "Causal Inference and Reasoning in Causally
 * Insufficient Systems").
 * <p/>
 * This class is based off a copy of Fci.java taken from the repository on 2008/12/16, revision 7306. The extension is
 * done by extending doFinalOrientation() with methods for Zhang's rules R5-R10 which implements the augmented search.
 * (By a remark of Zhang's, the rule applications can be staged in this way.)
 *
 * @author Erin Korber, June 2004
 * @author Alex Smith, December 2008
 */
public final class Fci implements GraphSearch {

    /**
     * The PAG being constructed.
     */
    private Graph graph;

    /**
     * The SepsetMap being constructed.
     */
    private SepsetMap sepsets;

    /**
     * The background knowledge.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * The variables to search over (optional)
     */
    private List<Node> variables = new ArrayList<Node>();

    /**
     * The independence test.
     */
    private IndependenceTest independenceTest;

    /**
     * change flag for repeat rules
     */
    private boolean changeFlag = true;

    /**
     * flag for complete rule set, true if should use complete rule set, false otherwise.
     */
    private boolean completeRuleSetUsed = false;

    /**
     * True iff the possible dsep search is done.
     */
    private boolean possibleDsepSearchDone = true;

    /**
     * The maximum length for any discriminating path. -1 if unlimited; otherwise, a positive integer.
     */
    private int maxReachablePathLength = -1;

    /**
     * The depth for the fast adjacency search.
     */
    private int depth = -1;

    /**
     * Elapsed time of last search.
     */
    private long elapsedTime;

    /**
     * The logger to use.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    //============================CONSTRUCTORS============================//

    /**
     * Constructs a new FCI search for the given independence test and background knowledge.
     */
    public Fci(IndependenceTest independenceTest) {
        if (independenceTest == null || knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.variables.addAll(independenceTest.getVariables());
    }

    /**
     * Constructs a new FCI search for the given independence test and background knowledge and a list of variables to
     * search over.
     */
    public Fci(IndependenceTest independenceTest, List<Node> searchVars) {
        if (independenceTest == null || knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.variables.addAll(independenceTest.getVariables());

        Set<Node> remVars = new HashSet<Node>();
        for (Node node1 : this.variables) {
            boolean search = false;
            for (Node node2 : searchVars) {
                if (node1.getName().equals(node2.getName())) {
                    search = true;
                }
            }
            if (!search) {
                remVars.add(node1);
            }
        }
        this.variables.removeAll(remVars);
    }

    //========================PUBLIC METHODS==========================//

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0: " + depth);
        }

        this.depth = depth;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public Graph search() {
        long beginTime = System.currentTimeMillis();
        logger.log("info", "Starting FCI algorithm.");
        logger.log("info", "Independence test = " + independenceTest + ".");

        setMaxReachablePathLength(maxReachablePathLength);

        //List<Node> variables = independenceTest.getVariables();       - Robert Tillman 2008
        List<Node> nodes = new LinkedList<Node>();

        for (Node variable : variables) {
            nodes.add(variable);
        }

        this.graph = new EdgeListGraph(nodes);

//        // Step FCI A. (Zhang's step F1.)
//        graph.fullyConnect(Endpoint.CIRCLE);
//
////        // Step FCI B.  (Zhang's step F2.)
//        Fas adj = new Fas(graph, independenceTest);
//        adj.setKnowledge(getKnowledge());
//        adj.setDepth(depth);
//        adj.setFci(true);
//        graph = adj.search();
//        this.sepsets = adj.getSepsets();

        // Switching to the faster FAS, that creates its own graph with tail endpoints.
        // The old code is commented out, above.

        // Step FCI A. (Zhang's step F1.)
//        graph.fullyConnect(Endpoint.CIRCLE);

//        // Step FCI B.  (Zhang's step F2.)
        Fas5 adj = new Fas5(graph, independenceTest);
        adj.setKnowledge(getKnowledge());
        adj.setDepth(depth);
//        adj.setFci(true);
        graph = adj.search();
        graph.reorientAllWith(Endpoint.CIRCLE);
        this.sepsets = adj.getSepsets();

//        // Optional step: Possible Dsep. (Needed for correctness but very time consuming.)
        if (isPossibleDsepSearchDone()) {
            long time1 = System.currentTimeMillis();
            ruleR0();

            long time2 = System.currentTimeMillis();
            logger.log("info", "Step C: " + (time2 - time1) / 1000. + "s");

            // Step FCI D.
            long time3 = System.currentTimeMillis();

            PossibleDsepFci possibleDSep =
                    new PossibleDsepFci(graph, independenceTest, getSepsets());
            possibleDSep.setDepth(getDepth());
            possibleDSep.setKnowledge(getKnowledge());
            possibleDSep.setMaxReachablePathLength(getMaxReachablePathLength());
            this.sepsets = possibleDSep.search();
            long time4 = System.currentTimeMillis();
            logger.log("info", "Step D: " + (time4 - time3) / 1000. + "s");

            // Reorient all edges as o-o.
            graph.reorientAllWith(Endpoint.CIRCLE);
        }

        // Step CI C (Zhang's step F3.)
        long time5 = System.currentTimeMillis();
        //fciOrientbk(getKnowledge(), graph, independenceTest.getVariables());    - Robert Tillman 2008
        fciOrientbk(getKnowledge(), graph, variables);
        ruleR0();

        long time6 = System.currentTimeMillis();
        logger.log("info", "Step CI C: " + (time6 - time5) / 1000. + "s");

        // Step CI D. (Zhang's step F4.)
        doFinalOrientation();

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - beginTime;

//        graph.closeInducingPaths();   //to make sure it's a legal PAG
        logger.log("graph", "Returning graph: " + graph);
        return graph;
    }

    public SepsetMap getSepsets() {
        return this.sepsets;
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

    /**
     * @return true if Zhang's complete rule set should be used, false if only R1-R4 (the rule set of the original FCI)
     *         should be used. False by default.
     */
    public boolean isCompleteRuleSetUsed() {
        return completeRuleSetUsed;
    }

    /**
     * @param completeRuleSetUsed set to true if Zhang's complete rule set should be used, false if only R1-R4 (the rule
     *                            set of the original FCI) should be used. False by default.
     */
    public void setCompleteRuleSetUsed(boolean completeRuleSetUsed) {
        this.completeRuleSetUsed = completeRuleSetUsed;
    }

    //===========================PRIVATE METHODS=========================//

    /**
     * Orients colliders in the graph.  (FCI Step C)
     * <p/>
     * Zhang's step F3, rule R0.
     */
    private void ruleR0() {
        List<Node> nodes = graph.getNodes();

        for (Node b : nodes) {
            List<Node> adjacentNodes = graph.getAdjacentNodes(b);

            if (adjacentNodes.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node a = adjacentNodes.get(combination[0]);
                Node c = adjacentNodes.get(combination[1]);

                // Skip triples that are shielded.
                if (graph.isAdjacentTo(a, c)) {
                    continue;
                }

                if (!this.sepsets.get(a, c).contains(b)) {
                    if (!isArrowpointAllowed(a, b)) {
                        continue;
                    }

                    if (!isArrowpointAllowed(c, b)) {
                        continue;
                    }

                    graph.setEndpoint(a, b, Endpoint.ARROW);
                    graph.setEndpoint(c, b, Endpoint.ARROW);
                    logger.log("colliderOrientations", SearchLogUtils.colliderOrientedMsg(a, b, c));
                }
            }
        }
    }

    /**
     * Orients the graph according to rules in the graph (FCI step D).
     * <p/>
     * Zhang's step F4, rules R1-R10.
     */
    private void doFinalOrientation() {

        // This loop handles Zhang's rules R1-R4, and is "normal FCI."
        changeFlag = true;

        while (changeFlag) {
            changeFlag = false;
            rulesR1R2cycle();
            ruleR3();
            ruleR4();   //slowest, so do last
        }

        if (isCompleteRuleSetUsed()) {
            // Now, by a remark on page 100 of Zhang's dissertation, we apply rule
            // R5 once.
            ruleR5();

            // Now, by a further remark on page 102, we apply R6,R7 as many times
            // as possible.
            changeFlag = true;

            while (changeFlag) {
                changeFlag = false;
                ruleR6R7();
            };

            // Finally, we apply R8-R10 as many times as possible.
            changeFlag = true;

            while (changeFlag) {
                changeFlag = false;
                rulesR8R9R10();
            };
        }
    }

    //Does all 3 of these rules at once instead of going through all
    // triples multiple times per iteration of doFinalOrientation.

    private void rulesR1R2cycle() {
        List<Node> nodes = graph.getNodes();

        for (Node B : nodes) {
            List<Node> adj = graph.getAdjacentNodes(B);

            if (adj.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adj.size(), 2);
            int[] combination;

            while ((combination = cg.next()) != null) {
                Node A = adj.get(combination[0]);
                Node C = adj.get(combination[1]);

                //choice gen doesnt do diff orders, so must switch A & C around.
                ruleR1(A, B, C);
                ruleR1(C, B, A);
                ruleR2(A, B, C);
                ruleR2(C, B, A);
//                awayFromCycle(A, B, C);
//                awayFromCycle(C, B, A);
            }
        }
    }

    // if a*->Bo-oC and not a*-*c, then a*->b-->c
    // (orient either circle if present, don't need both)
    // This is special case of Zhang's rule R1.
//    private void awayFromCollider(Node a, Node b, Node c) {
//        Endpoint BC = graph.getEndpoint(b, c);
//        Endpoint CB = graph.getEndpoint(c, b);
//
//        if (!(graph.isAdjacentTo(a, c)) &&
//                (graph.getEndpoint(a, b) == Endpoint.ARROW)) {
//            if (CB == Endpoint.CIRCLE || CB == Endpoint.TAIL) {
//                if (BC == Endpoint.CIRCLE) {
//                    if (!isArrowpointAllowed(b, c)) {
//                        return;
//                    }
//
//                    graph.setEndpoint(b, c, Endpoint.ARROW);
//                    logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from collider", graph.getEdge(b, c)));
//                    changeFlag = true;
//                }
//            }
//            if (BC == Endpoint.CIRCLE || BC == Endpoint.ARROW) {
//                if (CB == Endpoint.CIRCLE) {
//                    graph.setEndpoint(c, b, Endpoint.TAIL);
//                    logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from collider", graph.getEdge(c, b)));
//                    changeFlag = true;
//                }
//            }
//        }
//    }

    /// R1, away from collider

    private void ruleR1(Node a, Node b, Node c) {
        if (graph.isAdjacentTo(a, c)) {
            return;
        }

        if (graph.getEndpoint(a, b) == Endpoint.ARROW && graph.getEndpoint(c, b) == Endpoint.CIRCLE) {
            if (!isArrowpointAllowed(b, c)) {
                return;
            }

            graph.setEndpoint(c, b, Endpoint.TAIL);
            graph.setEndpoint(b, c, Endpoint.ARROW);
            changeFlag = true;
            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Away from collider", graph.getEdge(b, c)));
        }
    }

//    //if a*-oC and either a-->b*->c or a*->b-->c, then a*->c
//    // This is Zhang's rule R2.
//    private void awayFromAncestor(Node a, Node b, Node c) {
//        if ((graph.isAdjacentTo(a, c)) &&
//                (graph.getEndpoint(a, c) == Endpoint.CIRCLE)) {
//
//            if ((graph.getEndpoint(a, b) == Endpoint.ARROW) &&
//                    (graph.getEndpoint(b, c) == Endpoint.ARROW) && (
//                    (graph.getEndpoint(b, a) == Endpoint.TAIL) ||
//                            (graph.getEndpoint(c, b) == Endpoint.TAIL))) {
//
//                if (!isArrowpointAllowed(a, c)) {
//                    return;
//                }
//
//                graph.setEndpoint(a, c, Endpoint.ARROW);
//                logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from ancestor", graph.getEdge(a, c)));
//                changeFlag = true;
//            }
//        }
//    }

//    //if Ao->c and a-->b-->c, then a-->c
//    // This may be handled also by Zhang's R1?
//    private void awayFromCycle(Node a, Node b, Node c) {
//        if ((graph.isAdjacentTo(a, c)) &&
//                (graph.getEndpoint(a, c) == Endpoint.ARROW) &&
//                (graph.getEndpoint(c, a) == Endpoint.CIRCLE)) {
//            if (graph.isDirectedFromTo(a, b) && graph.isDirectedFromTo(b, c)) {
//                System.out.println("Orienting away from cycle: " + c + " --* " + a);
//
//                graph.setEndpoint(c, a, Endpoint.TAIL);
//                logger.edgeOriented(SearchLogUtils.edgeOrientedMsg("Away from cycle", graph.getEdge(c, a)));
//                changeFlag = true;
//            }
//        }
//    }

    //if Ao->c and a-->b-->c, then a-->c
    // Zhang's rule R2, awy from ancestor.

    private void ruleR2(Node a, Node b, Node c) {
        if (!graph.isAdjacentTo(a, c)) {
            return;
        }

        if (graph.getEndpoint(b, a) == Endpoint.TAIL && graph.getEndpoint(a, b) == Endpoint.ARROW
                && graph.getEndpoint(b, c) == Endpoint.ARROW && graph.getEndpoint(a, c) == Endpoint.CIRCLE) {
            if (!isArrowpointAllowed(a, c)) {
                return;
            }

            graph.setEndpoint(a, c, Endpoint.ARROW);
            changeFlag = true;
            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Away from ancestor (a)", graph.getEdge(a, c)));
        } else if (graph.getEndpoint(a, b) == Endpoint.ARROW && graph.getEndpoint(c, b) == Endpoint.TAIL
                && graph.getEndpoint(b, c) == Endpoint.ARROW && graph.getEndpoint(a, c) == Endpoint.CIRCLE
                ) {
            if (!isArrowpointAllowed(a, c)) {
                return;
            }

            graph.setEndpoint(a, c, Endpoint.ARROW);
            changeFlag = true;
            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Away from ancestor (b)", graph.getEdge(a, c)));
        }
    }

    /**
     * Implements the double-triangle orientation rule, which states that if D*-oB, A*->B<-*C and A*-oDo-*C, then
     * D*->B.
     * <p/>
     * This is Zhang's rule R3.
     */
    private void ruleR3() {
        List<Node> nodes = graph.getNodes();

        for (Node B : nodes) {

            List<Node> intoBArrows = graph.getNodesInTo(B, Endpoint.ARROW);
            List<Node> intoBCircles = graph.getNodesInTo(B, Endpoint.CIRCLE);

            for (Node D : intoBCircles) {
                if (intoBArrows.size() < 2) {
                    continue;
                }

                ChoiceGenerator gen = new ChoiceGenerator(intoBArrows.size(), 2);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    Node A = intoBArrows.get(choice[0]);
                    Node C = intoBArrows.get(choice[1]);

                    if (graph.isAdjacentTo(A, C)) {
                        continue;
                    }

                    if (!graph.isAdjacentTo(A, D) ||
                            !graph.isAdjacentTo(C, D)) {
                        continue;
                    }

                    if (graph.getEndpoint(A, D) != Endpoint.CIRCLE) {
                        continue;
                    }

                    if (graph.getEndpoint(C, D) != Endpoint.CIRCLE) {
                        continue;
                    }

                    if (!isArrowpointAllowed(D, B)) {
                        continue;
                    }

                    graph.setEndpoint(D, B, Endpoint.ARROW);
                    logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Double triangle", graph.getEdge(D, B)));
                    changeFlag = true;
                }
            }
        }
    }

    /**
     * The triangles that must be oriented this way (won't be done by another rule) all look like the ones below, where
     * the dots are a collider path from L to A with each node on the path (except L) a parent of C.
     * <pre>
     *          B
     *         xo           x is either an arrowhead or a circle
     *        /  \
     *       v    v
     * L....A --> C
     * </pre>
     * <p/>
     * This is Zhang's rule R4, discriminating paths.
     */
    private void ruleR4() {
        List<Node> nodes = graph.getNodes();

        for (Node b : nodes) {

            //potential A and C candidate pairs are only those
            // that look like this:   A<-*Bo-*C
            List<Node> possA = graph.getNodesOutTo(b, Endpoint.ARROW);
            List<Node> possC = graph.getNodesInTo(b, Endpoint.CIRCLE);

//            //keep arrows and circles
//            List<Node> possA = new LinkedList<Node>(possAandC);
//            possA.removeAll(graph.getNodesInTo(b, Endpoint.TAIL));
//
//            //keep only circles
//            List<Node> possC = new LinkedList<Node>(possAandC);
//            possC.retainAll(graph.getNodesInTo(b, Endpoint.CIRCLE));

            for (Node a : possA) {
                for (Node c : possC) {
                    if (!graph.isParentOf(a, c)) {
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
     * a method to search "back from a" to find a DDP. It is called with a reachability list (first consisting only of
     * a). This is breadth-first, utilizing "reachability" concept from Geiger, Verma, and Pearl 1990. </p> The body of
     * a DDP consists of colliders that are parents of c.
     */
    private void reachablePathFind(Node a, Node b, Node c,
                                   LinkedList<Node> reachable) {
        Set<Node> cParents = new HashSet<Node>(graph.getParents(c));

        // Needed to avoid cycles in failure case.
        Set<Node> visited = new HashSet<Node>();
        visited.add(b);
        visited.add(c);

        // We don't want to include a,b,or c on the path, so they are added to
        // the "visited" set.  b and c are added explicitly here; a will be
        // added in the first while iteration.
        while (reachable.size() > 0) {
            Node x = reachable.removeFirst();
            visited.add(x);

            // Possible DDP path endpoints.
            List<Node> pathExtensions = graph.getNodesInTo(x, Endpoint.ARROW);
            pathExtensions.removeAll(visited);

            for (Node d : pathExtensions) {

                // If d is reachable and not adjacent to c, its a DDP
                // endpoint, so do DDP orientation. Otherwise, if d <-> c,
                // add d to the list of reachable nodes.
                if (!graph.isAdjacentTo(d, c)) {

                    // Check whether <a, b, c> should be reoriented given
                    // that d is not adjacent to c; if so, orient and stop.
                    doDdpOrientation(d, a, b, c);
                    return;
                } else if (cParents.contains(d)) {
                    if (graph.getEndpoint(x, d) == Endpoint.ARROW) {
                        reachable.add(d);
                    }
                }
            }
        }
    }

    /**
     * Orients the edges inside the definte discriminating path triangle. Takes the left endpoint, and a,b,c as
     * arguments.
     */
    private void doDdpOrientation(Node d, Node a, Node b, Node c) {
        List<Node> sepset = this.sepsets.get(d, c);

        if (sepset == null) {
            throw new IllegalArgumentException("The edge from d to c must have " +
                    "been removed at this point.");
        }

        if (sepset.contains(b)) {
//            System.out.println("DDP orientation: " + c + " *-- " + b);

            graph.setEndpoint(c, b, Endpoint.TAIL);
            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Definite discriminating path d = " + d, graph.getEdge(b, c)));
            changeFlag = true;
        } else {
            if (!isArrowpointAllowed(a, b)) {
                return;
            }

            if (!isArrowpointAllowed(c, b)) {
                return;
            }

            graph.setEndpoint(a, b, Endpoint.ARROW);
            graph.setEndpoint(c, b, Endpoint.ARROW);
            logger.log("colliderOrientations", SearchLogUtils.colliderOrientedMsg("Definite discriminating path.. d = " + d, a, b, c));
            changeFlag = true;
        }
    }

    /**
     * Implements Zhang's rule R5, orient circle paths: for any Ao-oB, if there is an uncovered circle path u =
     * <A,C,...,D,B> such that A,D nonadjacent and B,C nonadjacent, then A---B and orient every edge on u undirected.
     */
    private void ruleR5() {
        List<Node> nodes = graph.getNodes();

        for (Node a : nodes) {
            List<Node> adjacents = graph.getNodesInTo(a, Endpoint.CIRCLE);

            for (Node b : adjacents) {
                if (!(graph.getEndpoint(a, b) == Endpoint.CIRCLE)) continue;
                // We know Ao-oB.

                List<List<Node>> ucCirclePaths = getUcCirclePaths(a, b);

//                System.out.println("Circle paths:");

//                for (List<Node> path : ucCirclePaths) {
//                    System.out.println(GraphUtils.pathString(graph, path));
//                }

                for (List<Node> u : ucCirclePaths) {
                    if (u.size() < 3) continue;

                    Node c = u.get(1);
                    Node d = u.get(u.size() - 2);

//                    System.out.println("a = " + a + " c = " + c + " d = " + d + " b = " + b);

                    if (graph.isAdjacentTo(a, d)) continue;
                    if (graph.isAdjacentTo(b, c)) continue;
                    // We know u is as required: R5 applies!

                    logger.log("colliderOrientations", SearchLogUtils.edgeOrientedMsg("Orient circle path", graph.getEdge(a, b)));

                    graph.setEndpoint(a, b, Endpoint.TAIL);
                    graph.setEndpoint(b, a, Endpoint.TAIL);
                    orientTailPath(u);
                    changeFlag = true;

                    // TODO we should break here if one doesn't need to undirect
                    // TODO every such uncovered circle path, ask Jiji?
                }
            }
        }
    }

    /**
     * Implements Zhang's rules R6 and R7, applies them over the graph once. Orient single tails. R6: If A---Bo-*C then
     * A---B--*C. R7: If A--oBo-*C and A,C nonadjacent, then A--oB--*C
     */
    private void ruleR6R7() {
        List<Node> nodes = graph.getNodes();

        for (Node b : nodes) {
            List<Node> adjacents = graph.getAdjacentNodes(b);

            if (adjacents.size() < 2) continue;

            ChoiceGenerator cg = new ChoiceGenerator(adjacents.size(), 2);

            for (int[] choice = cg.next(); choice != null; choice = cg.next()) {
                Node a = adjacents.get(choice[0]);
                Node c = adjacents.get(choice[1]);

                if (graph.isAdjacentTo(a, c)) continue;

                if (!(graph.getEndpoint(b, a) == Endpoint.TAIL)) continue;
                if (!(graph.getEndpoint(c, b) == Endpoint.CIRCLE)) continue;
                // We know A--*Bo-*C.

                if (graph.getEndpoint(a, b) == Endpoint.TAIL) {
//                    System.out.println("Single tails (tail) " + c + " *-> " + b);

                    // We know A---Bo-*C: R6 applies!
                    graph.setEndpoint(c, b, Endpoint.TAIL);

                    logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Single tails (tail)", graph.getEdge(c, b)));

                    changeFlag = true;
                }

                if (graph.getEndpoint(a, b) == Endpoint.CIRCLE) {
//                    if (graph.isAdjacentTo(a, c)) continue;

                    logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Single tails (tail)", graph.getEdge(c, b)));

                    // We know A--oBo-*C and A,C nonadjacent: R7 applies!
                    graph.setEndpoint(c, b, Endpoint.TAIL);
                    changeFlag = true;
                }

            }
        }
    }

    /**
     * Implements Zhang's rules R8, R9, R10, applies them over the graph once. Orient arrow tails. I.e., tries R8, R9,
     * and R10 in that sequence on each Ao->C in the graph.
     */
    private void rulesR8R9R10() {
        List<Node> nodes = graph.getNodes();

        for (Node c : nodes) {
            List<Node> intoCArrows = graph.getNodesInTo(c, Endpoint.ARROW);

            for (Node a : intoCArrows) {
                if (!(graph.getEndpoint(c, a) == Endpoint.CIRCLE)) continue;
                // We know Ao->C.

                // Try each of R8, R9, R10 in that order, stopping ASAP.
                if (!ruleR8(a, c)) {
                    if (!ruleR9(a, c)) {
                        ruleR10(a, c);
                    }
                }
            }
        }

    }

    /**
     * Orients every edge on a path as undirected (i.e. A---B).
     * <p/>
     * DOES NOT CHECK IF SUCH EDGES ACTUALLY EXIST: MAY DO WEIRD THINGS IF PASSED AN ARBITRARY LIST OF NODES THAT IS NOT
     * A PATH.
     *
     * @param path The path to orient as all tails.
     */
    private void orientTailPath(List<Node> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            Node n1 = path.get(i);
            Node n2 = path.get(i + 1);

//            System.out.println("Tail path " + n1 + "---" + n2);

            graph.setEndpoint(n1, n2, Endpoint.TAIL);
            graph.setEndpoint(n2, n1, Endpoint.TAIL);
            changeFlag = true;

            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("Orient circle paths", graph.getEdge(n1, n2)));
        }
    }

    /**
     * Gets a list of every uncovered partially directed path between two nodes in the graph.
     * <p/>
     * Probably extremely slow.
     *
     * @param n1 The beginning node of the paths.
     * @param n2 The ending node of the paths.
     * @return A list of uncovered partially directed paths from n1 to n2.
     */
    private List<List<Node>> getUcPdPaths(Node n1, Node n2) {
        List<List<Node>> ucPdPaths = new LinkedList();

        List<Node> soFar = new LinkedList();
        soFar.add(n1);

        List<Node> adjacencies = graph.getAdjacentNodes(n1);
        for (Node curr : adjacencies) {
            getUcPdPsHelper(curr, soFar, n2, ucPdPaths);
        }

        return ucPdPaths;
    }

    /**
     * Used in getUcPdPaths(n1,n2) to perform a breadth-first search on the graph.
     * <p/>
     * ASSUMES soFar CONTAINS AT LEAST ONE NODE!
     * <p/>
     * Probably extremely slow.
     *
     * @param curr      The current node to test for addition.
     * @param soFar     The current partially built-up path.
     * @param end       The node to finish the paths at.
     * @param ucPdPaths The current list of uncovered p.d. paths.
     */
    private void getUcPdPsHelper(Node curr, List<Node> soFar, Node end,
                                 List<List<Node>> ucPdPaths) {

        if (soFar.contains(curr)) return;

        Node prev = soFar.get(soFar.size() - 1);
        if (graph.getEndpoint(prev, curr) == Endpoint.TAIL ||
                graph.getEndpoint(curr, prev) == Endpoint.ARROW) {
            return; // Adding curr would make soFar not p.d.
        } else if (soFar.size() >= 2) {
            Node prev2 = soFar.get(soFar.size() - 2);
            if (graph.isAdjacentTo(prev2, curr)) {
                return; // Adding curr would make soFar not uncovered.
            }
        }

        soFar.add(curr); // Adding curr is OK, so let's do it.

        if (curr.equals(end)) {
            // We've reached the goal! Save soFar as a path.
            ucPdPaths.add(new LinkedList<Node>(soFar));
        } else {
            // Otherwise, try each node adjacent to the current one.
            List<Node> adjacents = graph.getAdjacentNodes(curr);
            for (Node next : adjacents) {
                getUcPdPsHelper(next, soFar, end, ucPdPaths);
            }
        }

        soFar.remove(soFar.get(soFar.size() - 1)); // For other recursive calls.
    }

    /**
     * Gets a list of every uncovered circle path between two nodes in the graph by iterating through the uncovered
     * partially directed paths and only keeping the circle paths.
     * <p/>
     * Probably extremely slow.
     *
     * @param n1 The beginning node of the paths.
     * @param n2 The ending node of the paths.
     * @return A list of uncovered circle paths between n1 and n2.
     */
    private List<List<Node>> getUcCirclePaths(Node n1, Node n2) {
        List<List<Node>> ucCirclePaths = new LinkedList<List<Node>>();
        List<List<Node>> ucPdPaths = getUcPdPaths(n1, n2);

        for (List<Node> path : ucPdPaths) {
            for (int i = 0; i < path.size() - 1; i++) {
                Node j = path.get(i);
                Node sj = path.get(i + 1);

                if (!(graph.getEndpoint(j, sj) == Endpoint.CIRCLE)) break;
                if (!(graph.getEndpoint(sj, j) == Endpoint.CIRCLE)) break;
                // This edge is OK, it's all circles.

                if (i == path.size() - 2) {
                    // We're at the last edge, so this is a circle path.
                    ucCirclePaths.add(path);
                }
            }
        }

        return ucCirclePaths;
    }

    /**
     * Tries to apply Zhang's rule R8 to a pair of nodes A and C which are assumed to be such that Ao->C.
     * <p/>
     * MAY HAVE WEIRD EFFECTS ON ARBITRARY NODE PAIRS.
     * <p/>
     * R8: If Ao->C and A-->B-->C or A--oB-->C, then A-->C.
     *
     * @param a The node A.
     * @param c The node C.
     * @return Whether or not R8 was successfully applied.
     */
    private boolean ruleR8(Node a, Node c) {
        List<Node> intoCArrows = graph.getNodesInTo(c, Endpoint.ARROW);

        for (Node b : intoCArrows) {
            // We have B*->C.
            if (!graph.isAdjacentTo(a, b)) continue;
            if (!graph.isAdjacentTo(b, c)) continue;

            // We have A*-*B*->C.
            if (!(graph.getEndpoint(b, a) == Endpoint.TAIL)) continue;
            if (!(graph.getEndpoint(c, b) == Endpoint.TAIL)) continue;
            // We have A--*B-->C.

            if (graph.getEndpoint(a, b) == Endpoint.TAIL) continue;
            // We have A-->B-->C or A--oB-->C: R8 applies!

            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("R8", graph.getEdge(c, a)));

            graph.setEndpoint(c, a, Endpoint.TAIL);
            changeFlag = true;
            return true;
        }

        return false;
    }

    /**
     * Tries to apply Zhang's rule R9 to a pair of nodes A and C which are assumed to be such that Ao->C.
     * <p/>
     * MAY HAVE WEIRD EFFECTS ON ARBITRARY NODE PAIRS.
     * <p/>
     * R9: If Ao->C and there is an uncovered p.d. path u=<A,B,..,C> such that C,B nonadjacent, then A-->C.
     *
     * @param a The node A.
     * @param c The node C.
     * @return Whether or not R9 was succesfully applied.
     */
    private boolean ruleR9(Node a, Node c) {
        List<List<Node>> ucPdPsToC = getUcPdPaths(a, c);

        for (List<Node> u : ucPdPsToC) {
            Node b = u.get(1); // TODO do we need to check if b is c?
            if (graph.isAdjacentTo(b, c)) continue;
            if (b == c) continue;
            // We know u is as required: R9 applies!

            logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("R9", graph.getEdge(c, a)));

            graph.setEndpoint(c, a, Endpoint.TAIL);
            changeFlag = true;
            return true;
        }

        return false;
    }

    /**
     * Tries to apply Zhang's rule R10 to a pair of nodes A and C which are assumed to be such that Ao->C.
     * <p/>
     * MAY HAVE WEIRD EFFECTS ON ARBITRARY NODE PAIRS.
     * <p/>
     * R10: If Ao->C, B-->C<--D, there is an uncovered p.d. path u1=<A,M,...,B> and an uncovered p.d. path
     * u2=<A,N,...,D> with M != N and M,N nonadjacent then A-->C.
     *
     * @param a The node A.
     * @param c The node C.
     * @return Whether or not R10 was successfully applied.
     */
    private boolean ruleR10(Node a, Node c) {
        List<Node> intoCArrows = graph.getNodesInTo(c, Endpoint.ARROW);

        for (Node b : intoCArrows) {
            if (b == a) continue;

            if (!(graph.getEndpoint(c, b) == Endpoint.TAIL)) continue;
            // We know Ao->C and B-->C.

            for (Node d : intoCArrows) {
                if (d == a || d == b) continue;

                if (!(graph.getEndpoint(d, c) == Endpoint.TAIL)) continue;
                // We know Ao->C and B-->C<--D.
                // TODO do we need to check if d is b?  I think so--jdramsey--added code.

                List<List<Node>> ucPdPsToB = getUcPdPaths(a, b);
                List<List<Node>> ucPdPsToD = getUcPdPaths(a, d);
                for (List<Node> u1 : ucPdPsToB) {
                    Node m = u1.get(1);
                    for (List<Node> u2 : ucPdPsToD) {
                        Node n = u2.get(1);

                        if (m.equals(n)) continue; // TODO use ==?
                        if (graph.isAdjacentTo(m, n)) continue;
                        // We know B,D,u1,u2 as required: R10 applies!

                        logger.log("impliedOrientations", SearchLogUtils.edgeOrientedMsg("R10", graph.getEdge(c, a)));

                        graph.setEndpoint(c, a, Endpoint.TAIL);
                        changeFlag = true;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Orients according to background knowledge
     */
    private void fciOrientbk(Knowledge bk, Graph graph, List<Node> variables) {
        logger.log("info", "Starting BK Orientation.");

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
            changeFlag = true;
            logger.log("knowledgeOrientation", SearchLogUtils.edgeOrientedMsg("Knowledge", graph.getEdge(from, to)));
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

            // Orient from*->to (?)
            // Orient from-->to

//            System.out.println("Rule R8: Orienting " + from + "-->" + to);

            graph.setEndpoint(to, from, Endpoint.TAIL);
            graph.setEndpoint(from, to, Endpoint.ARROW);
            changeFlag = true;
            logger.log("knowledgeOrientation", SearchLogUtils.edgeOrientedMsg("Knowledge", graph.getEdge(from, to)));
        }

        logger.log("info", "Finishing BK Orientation.");
    }


    /**
     * Helper method. Appears to check if an arrowpoint is permitted by background knowledge.
     *
     * @param x The possible other node.
     * @param y The possible point node.
     * @return Whether the arrowpoint is allowed.
     */
    private boolean isArrowpointAllowed(Node x, Node y) {
        if (graph.getEndpoint(x, y) == Endpoint.ARROW) {
            return true;
        }

        if (graph.getEndpoint(x, y) == Endpoint.TAIL) {
            return false;
        }

        if (graph.getEndpoint(y, x) == Endpoint.ARROW) {
//            return true;
            if (!knowledge.edgeForbidden(x.getName(), y.getName())) return true;
        }

        if (graph.getEndpoint(y, x) == Endpoint.TAIL) {
            if (!knowledge.edgeForbidden(x.getName(), y.getName())) return true;
        }

        if (graph.getEndpoint(y, x) == Endpoint.CIRCLE) {
            return true;
        }

        return false;

//        return graph.getEndpoint(y, x) == Endpoint.ARROW;


//        return !knowledge.isForbiddenByTiers(x.getName(), y.getName());
    }


//    /**
//     * Helper Method: Checks if a directed edge is allowed by background
//     * knowledge.
//     */
//    private boolean isDirEdgeAllowed(Node from, Node to) {
//        return !getKnowledge().edgeRequired(to.getName(), from.getName()) &&
//                !getKnowledge().edgeForbidden(from.getName(), to.getName());
//    }

    public boolean isPossibleDsepSearchDone() {
        return possibleDsepSearchDone;
    }

    public void setPossibleDsepSearchDone(boolean possibleDsepSearchDone) {
        this.possibleDsepSearchDone = possibleDsepSearchDone;
    }

    /**
     * @return the maximum length of any discriminating path, or -1 of unlimited.
     */
    public int getMaxReachablePathLength() {
        return maxReachablePathLength == Integer.MAX_VALUE ? -1 : maxReachablePathLength;
    }

    /**
     * @param maxReachablePathLength the maximum length of any discriminating path, or -1 if unlimited.
     */
    public void setMaxReachablePathLength(int maxReachablePathLength) {
        if (maxReachablePathLength < -1) {
            throw new IllegalArgumentException("Max path length must be -1 (unlimited) or >= 0: " + maxReachablePathLength);
        }

        this.maxReachablePathLength = maxReachablePathLength == -1
                ? Integer.MAX_VALUE : maxReachablePathLength;
    }
}


