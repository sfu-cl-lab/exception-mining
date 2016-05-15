
package edu.cmu.tetrad.search;


import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.ChoiceGenerator;


import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements the ION algorithm by David Danks, for merging causal PAGS.
 *
 * @author Robert Tillman, Tyler Gibson, Trevor Burns
 */
public class IonSearch {

    /**
     * The input PAGs being intergrated, possibly FCI outputs.
     */
    private List<Pag> input = new ArrayList<Pag>();

    /**
     * Sets of nodes that are not adjacent in any of the input PAGs.
     */
    private Set<NodePair> notadjacent = new HashSet<NodePair>();


    /**
     * All the variables being integrated from the input PAGs
     */
    private List<Node> variables = new ArrayList<Node>();

    /**
     * locks for nonthreadsafe methods
     */
    private static ReentrantLock lock = new ReentrantLock(true);
    private static ReentrantLock fclock = new ReentrantLock(true);

    /**
     * tracks computational info for simulations
     */
    private List<Integer> recGraphs = new ArrayList<Integer>();
    private List<Float> recHitTimes = new ArrayList<Float>();
    private long runtime;

    /**
     * used for FCI orientation methods
     */
    private boolean changeFlag = true;

    /**
     * Constructs a new instance of the ION search from the input PAGs
     *
     * @param pags  The PAGs to be integrated
     */
    public IonSearch(List<Graph> pags) {
        for (Graph graph : pags) {
            if (graph instanceof Pag) {
                this.input.add((Pag) graph);
            } else {
                this.input.add(new Pag(graph));
            }
        }
        for (Pag pag : input) {
            for (Node node : pag.getNodes()) {
                if (!variables.contains(node)) {
                    this.variables.add(node);
                }
            }
        }
    }


    //============================= Public Methods ============================//

    /**
     * Begins the ION search procedure, described at each step
     */
    public List<Graph> search() {

        long start = System.currentTimeMillis();

        TetradLogger.getInstance().info("Starting ION Search.");
        logGraphs("\nInitial Pags: ", this.input);

        /**
         * Step 1
         *
         * Generate the complete graph over the variables being integrated
         * from the causal PAGs and queue to keep up with the PAGs being
         * searched over at each iteration
         */
        Pag graph = new Pag(this.variables);
        Queue<Pag> searchPags = new LinkedList<Pag>();
        graph.fullyConnect(Endpoint.CIRCLE);

        /**
         * Step 2
         *
         * Transfers local orientations and adjacencies from the input PAGs to
         * the complete graph
         */

        removeAdjacencies(graph);
        transferOrientations(graph);

        // if there is a path from a -> b and from b -> c, mark c as a nonancestor of a
        for (Node a : this.variables) {
            for (Node b : this.variables) {
                if (b.equals(a)) {
                    continue;
                }
                for (Node c : this.variables) {
                    if (c.equals(b) || c.equals(a)) {
                        continue;
                    }
                    if (graph.isAncestorOf(a, b) && graph.isAncestorOf(b, c)) {
                       graph.setEndpoint(a, c, Endpoint.ARROW);
                    }
                }
            }
        }

        // place graph construct in step 2 into the queue
        searchPags.offer(graph);

        /**
         * Step 3
         *
         * Branch and prune step that blocks problematic paths, possibly d-connecting paths
         * between nodes that are not adjacent in the input PAGs, while iterating over the
         * pairs of adjacencies
         */

        List<Set<IonIndependenceFacts>> sepAndAssoc = findSepAndAssoc();
        Set<IonIndependenceFacts> separations = sepAndAssoc.get(0);
        Set<IonIndependenceFacts> associations = sepAndAssoc.get(1);

        Map<Collection<Node>, List<PossibleDConnectingPath>> paths;
        Queue<Pag> step3Pags = new LinkedList<Pag>();
        Set<Pag> reject = new HashSet<Pag>();

        // if no d-separations, nothing left to orient
        if (separations.isEmpty()) {
            // orients edges that would otherwise produce a cycle
            awayFromColliderAncestorCycle(graph);
            List<Graph> output = new ArrayList<Graph>();
            output.add(graph);
            logGraphs("\nReturning output:", output);
            return output;
        }

        for (IonIndependenceFacts fact : separations) {

            // uses two queues to keep up with which PAGs are being iterated and which have been
            // accepted to be iterated over in the next iteration of the above for loop
            searchPags.addAll(step3Pags);
            recGraphs.add(searchPags.size());
            step3Pags.clear();

            while (!searchPags.isEmpty()) {

                // deques first PAG from searchPags
                Pag pag = searchPags.poll();

                // Part 3.a - finds possibly d-connecting paths between each pair of nodes
                // known to be d-separated

                List<PossibleDConnectingPath> dConnections = new ArrayList<PossibleDConnectingPath>();
                for (Collection<Node> conditions : fact.getZ())  {
                    lock.lock();
                    try {
                    dConnections.addAll(PossibleDConnectingPath.findDConnectingPaths
                            (pag, fact.getX(), fact.getY(), conditions));
                    }
                    finally {lock.unlock();}

                }

                // accept PAG go to next PAG if no possibly d-connecting paths
                if (dConnections.isEmpty()) {
                    step3Pags.add(pag);
                    continue;
                }

                // maps conditioning sets to list of possibly d-connecting paths
                paths = new HashMap<Collection<Node>, List<PossibleDConnectingPath>>();
                for (PossibleDConnectingPath path : dConnections) {
                    List<PossibleDConnectingPath> p = paths.get(path.getConditions());
                    if (p == null) {
                        p = new LinkedList<PossibleDConnectingPath>();
                    }
                        p.add(path);
                        paths.put(path.getConditions(), p);
                }

                // Part 3.b - finds minimal graphical changes to block possibly d-connecting paths
                fclock.lock();
                List<HashSet<GraphChange>> possibleChanges = new ArrayList<HashSet<GraphChange>>();
                try {
                    possibleChanges = findChanges(paths);
                }
                finally {
                    fclock.unlock();
                }


                float starthitset = System.currentTimeMillis();
                Collection<GraphChange> hittingSets = IonHittingSet.findHittingSet(possibleChanges);
                recHitTimes.add((System.currentTimeMillis() - starthitset)/100);

                // Part 3.c - checks the newly constructed graphs from 3.b and rejects those that
                // cycles or produce independencies known not to occur from the input PAGs or
                // include paths from definite nonancestors

                for (GraphChange gc : hittingSets) {
                    Pag changed = gc.applyTo(pag);

                    // if graph change has already been rejected move on to next graph
                    if (reject.contains(changed)) {
                        continue;
                    }

                    // if graph change has already been accepted move on to next graph
                    if (step3Pags.contains(changed)) {
                        continue;
                    }

                    // reject if null, predicts false independencies or has cycle
                    if (changed == null || predictsFalseIndependence(associations, changed)
                            || hasCircularPath(changed))   {
                       reject.add(changed);
                    }

                    // makes orientations preventing definite noncolliders from becoming colliders
                    // if a definite noncollider intersects with a collider
                    List<Triple> remTriples = new ArrayList<Triple>();
                    for (Triple triple : changed.getUnderLineTriples()) {
                        if (!changed.isAdjacentTo(triple.getX(), triple.getY()) || !changed.isAdjacentTo(triple.getZ(), triple.getY()) || changed.isDefiniteCollider(triple.getX(), triple.getY(), triple.getZ())) {
                            remTriples.add(triple);
                            continue;
                        }
                        if (changed.getEndpoint(triple.getX(), triple.getY()).equals(Endpoint.ARROW)) {
                            changed.setEndpoint(triple.getZ(), triple.getY(), Endpoint.TAIL);
                            remTriples.add(triple);
                            // now if non-tail end of edge is circle, orient to arrow
                            changed.setEndpoint(triple.getY(), triple.getZ(), Endpoint.ARROW);
                        }
                        else if (changed.getEndpoint(triple.getZ(), triple.getY()).equals(Endpoint.ARROW)) {
                            changed.setEndpoint(triple.getX(), triple.getY(), Endpoint.TAIL);
                            remTriples.add(triple);
                            // now if non-tail end of edge is circle, orient to arrow
                            changed.setEndpoint(triple.getX(), triple.getZ(), Endpoint.ARROW);
                        }
                    }
                    // now remove definite noncollider markings that are no longer relevant
                    for (Triple triple : remTriples) {
                        changed.removeUnderlineTriple(triple);
                    }

                    // orients edges that would otherwise produce a cycle
                    awayFromColliderAncestorCycle(changed);

                    // now add graph to queue
                    step3Pags.add(changed);
                }
            }
        }

        /**
         * Step 4
         *
         * Finds redundant paths and uses this information to expand the list
         * of possible graphs
         */

        Map<Edge, Boolean> necEdges;

        // uses hashmap to prevent duplicates
        Set<Pag> outputPags = new HashSet<Pag>();

        for (Pag pag : step3Pags) {
            necEdges = new HashMap<Edge, Boolean>();
            
            // Step 4.a - if x and y are known to be unconditionally associated and there is
            // exactly one trek between them, mark each edge on that trek as necessary and
            // make the tiples on the trek definite noncolliders

            // initially mark each edge as not necessary
            for (Edge edge : pag.getEdges()) {
                necEdges.put(edge, false);
            }

            // look for unconditional associations
            for (IonIndependenceFacts fact : associations) {
                for (List<Node> nodes : fact.getZ()) {
                    if (nodes.isEmpty()) {
                        List<List<Node>> treks = GraphUtils.treks(pag, fact.x, fact.y);
                        if (treks.size() == 1)
                        {
                            List<Node> trek = treks.get(0);
                            List<Triple> triples = new ArrayList<Triple>();
                            for (int i = 1; i < trek.size(); i++) {
                                // marks each edge in trek as necessary
                                necEdges.put(pag.getEdge(trek.get(i-1), trek.get(i)), true);
                                if (i == 1) {
                                    continue;
                                }
                                // makes each triple a noncollider
                                pag.addUnderlineTriple(new Triple(trek.get(i-2), trek.get(i-1), trek.get(i)));
                            }
                        }
                        // stop looping once the empty set is found
                        break;
                    }
                }
            }

            // Part 4.b - branches by generating graphs for every combination of removing
            // redundant paths

            boolean elimTreks;
            // checks to see if removing redundant paths eliminates every trek between
            // two variables known to be nconditionally assoicated

            for (Pag newPag : possRemove(pag, necEdges)) {
                elimTreks = false;
                // looks for unconditional associations
                for (IonIndependenceFacts fact : associations) {
                    for (List<Node> nodes : fact.getZ()) {
                        if (nodes.isEmpty()) {
                            // if all treks are eliminated

                         //  if (!newPag.existsTrek(fact.x, fact.y)) {
                            if (GraphUtils.treks(newPag, fact.x, fact.y).isEmpty()) {
                                elimTreks = true;
                            }
                            // stop looping once the empty set is found
                            break;
                        }
                    }
                }
                // add new PAG to output unless a necessary trek has been eliminated
                if (!elimTreks) {
                    // remove underlinings of nonadjacent triples remnant from edge removals
                    List<Triple> remTriple = new ArrayList<Triple>();
                    for (Triple triple : newPag.getUnderLineTriples()) {
                        if (!newPag.isAdjacentTo(triple.getX(), triple.getY())
                                || !newPag.isAdjacentTo(triple.getY(), triple.getZ())) {
                            remTriple.add(triple);
                        }
                        else if (newPag.getEndpoint(triple.getX(), triple.getY()).equals(Endpoint.TAIL)
                                || newPag.getEndpoint(triple.getZ(), triple.getY()).equals(Endpoint.TAIL)) {
                            remTriple.add(triple);    
                        }
                    }
                    for (Triple triple : remTriple) {
                        newPag.removeUnderlineTriple(triple);
                    }
                    // marks new definite noncolliders and adds pag to output
                    List<HashSet<GraphChange>> addTriples = new ArrayList<HashSet<GraphChange>>();
                    // looks for unconditional associations
                    Set<Triple> pagTriples = getPossibleTriples(newPag);
                    for (IonIndependenceFacts fact : associations) {
                        for (List<Node> nodes : fact.getZ()) {
                            if (nodes.isEmpty()) {
                                HashSet<GraphChange> tripleSet = new HashSet<GraphChange>();
                                for (List<Node> nodeList : GraphUtils.treks(newPag, fact.x, fact.y)) {
                                    boolean noTriples = true;
                                    for (int j = 0; j < nodeList.size() - 2; j++) {
                                        Triple checkTriple = new Triple(nodeList.get(j), nodeList.get(j+1), nodeList.get(j+2));
                                        if (pagTriples.contains(checkTriple)) {
                                            GraphChange noncollider = new GraphChange();
                                            noncollider.addNonCollider(checkTriple);
                                            tripleSet.add(noncollider);
                                            noTriples = false;
                                        }
                                    }
                                    // if there is a trek with no orientable triples then this association is fine
                                    if (noTriples) {
                                        tripleSet.clear();
                                        break;
                                    }

                                }
                                // adds tripleset to list
                                if (!tripleSet.isEmpty()) {
                                    addTriples.add(tripleSet);
                                }
                                // stop looping once the empty set is found
                                break;
                            }
                        }
                    }
                    // if nothing to change then add to output graphs
                    if (addTriples.isEmpty()) {
                        outputPags.add(newPag);
                    }
                    // otherwise get hitting set of noncolliders to add
                    else {
                        Collection<GraphChange> hittingSets = IonHittingSet.findHittingSet(addTriples);
                        for (GraphChange gc : hittingSets) {
                            Pag changed = gc.applyTo(newPag);
                            outputPags.add(changed);
                        }
                    }
                }
            }

        }

        /**
         * Step 5
         *
         * Ouputs the possible causal graphs
         */

        Set<Pag> moreSpecific = new HashSet<Pag>();

        // checks for and removes PAGs tht are more specific, same skeleton and orientations
        // except for one or more arrows or tails where another graph has circles, than other
        // pags in the output graphs that may be produced from the edge removes in step 4
        for (Pag pag : outputPags) {
            for (Pag pag2 : outputPags) {

                // if different number of edges then continue
                if (pag.getEdges().size() != pag2.getEdges().size()) {
                    continue;
                }

                // if same pag
                if (pag.equals(pag2)) {
                    continue;
                }

                boolean sameAdjacencies = true;

                for (Edge edge1 : pag.getEdges()) {
                    if (!pag2.isAdjacentTo(edge1.getNode1(), edge1.getNode2())) {
                        sameAdjacencies = false;
                    }
                }

                if (sameAdjacencies) {
                    // checks to see if pag2 has same arrows and tails
                    boolean arrowstails = true;
                    boolean circles = true;
                    for (Edge edge2 : pag2.getEdges()) {
                        
                        Edge edge1 = pag.getEdge(edge2.getNode1(), edge2.getNode2());
                        if (edge1.getNode1().equals(edge2.getNode1())) {
                            if (!edge2.getEndpoint1().equals(Endpoint.CIRCLE)) {
                                if (!edge1.getEndpoint1().equals(edge2.getEndpoint1())) {
                                    arrowstails = false;
                                }
                            }
                            else {
                                if (!edge1.getEndpoint1().equals(edge2.getEndpoint1())) {
                                    circles = false;
                                }
                            }
                            if (!edge2.getEndpoint2().equals(Endpoint.CIRCLE)) {
                                if (!edge1.getEndpoint2().equals(edge2.getEndpoint2())) {
                                   arrowstails = false;
                                }
                            }
                            else {
                                if (!edge1.getEndpoint2().equals(edge2.getEndpoint2())) {
                                    circles = false;
                                }
                            }
                        }

                        else  if (edge1.getNode1().equals(edge2.getNode2())) {
                            if (!edge2.getEndpoint1().equals(Endpoint.CIRCLE)) {
                                if (!edge1.getEndpoint2().equals(edge2.getEndpoint1())) {
                                    arrowstails = false;
                                }
                            }
                            else {
                                if (!edge1.getEndpoint2().equals(edge2.getEndpoint1())) {
                                    circles = false;
                                }
                            }
                            if (!edge2.getEndpoint2().equals(Endpoint.CIRCLE)) {
                                if (!edge1.getEndpoint1().equals(edge2.getEndpoint2())) {
                                   arrowstails = false;
                                }
                            }
                            else {
                                if (!edge1.getEndpoint1().equals(edge2.getEndpoint2())) {
                                    circles = false;
                                }                                
                            }
                        }
                    }
                    if (arrowstails && !circles) {
                        moreSpecific.add(pag);
                        break;
                    }
                }
            }
        }

        for (Pag pag : moreSpecific) {
            outputPags.remove(pag);
        }

        TetradLogger.getInstance().details("Returned " + outputPags.size() + " Graphs");
//        logGraphs("\nReturning output (" + outputPags.size() + " Graphs):", new ArrayList(outputPags));

        runtime = (System.currentTimeMillis() - start)/100;

        // returns graphs consistent with the input PAGs
        return new ArrayList<Graph>(outputPags);
    }

    // reports computational information
    public String getStats() {
        String stats = "Total running time:  " + runtime + "\\\\";
        int totalit = 0;
        for (Integer i : recGraphs) {
            totalit += i;
        }
        float totalhit = 0;
        float longesthit = 0;
        float averagehit = 0;
        for (Float i : recHitTimes) {
            totalhit += i;
            averagehit += i/recHitTimes.size();
            if (i > longesthit) {
                longesthit = i;
            }
        }
        stats += "Total iterations in step 3:  " + totalit + "\\\\";
        stats += "Total hitting sets calculation time:  " + totalhit + "\\\\";
        stats += "Average hitting set calculation time:  " + averagehit + "\\\\";
        stats += "Longest hitting set calculation time:  " + longesthit + "\\\\";
        return stats;
    }


    //============================= Private Methods =============================//

    /**
     * Returns all triples that could potentially be made into colliders
     */
    private static Set<Triple> getPossibleTriples(Pag pag) {
        Set<Triple> triples = new HashSet<Triple>();
        for (Node node : pag.getNodes()) {
            Set<Node> adjNodes = new HashSet<Node>();
            for (Node node2 : pag.getAdjacentNodes(node)) {
                if (!pag.getEndpoint(node2, node).equals(Endpoint.TAIL)) {
                    adjNodes.add(node2);
                }
            }
            for (Node node2 : adjNodes) {
                for (Node node3 : adjNodes) {
                    if (node2.equals(node3)) {
                        continue;
                    }
                    if (pag.getEndpoint(node2, node).equals(Endpoint.CIRCLE) ||
                            pag.getEndpoint(node3, node).equals(Endpoint.CIRCLE)) {
                        triples.add(new Triple(node2, node, node3));
                    }
                }
            }
        }
        return triples;
    }


    /**
     * Returns all the triples in the graph that can be either oriented as a collider or non-collider.
     */
    private static Set<Triple> getAllTriples(Pag pag){
        Set<Triple> triples = new HashSet<Triple>();
        for(Edge edge : pag.getEdges()){
            if(isUndirected(edge)){
                Node y = edge.getNode2();
                for(Edge adjEdge : pag.getEdges(y)){
                    if(isUndirected(adjEdge)){
                        if(!pag.isUnderlineTriple(edge.getNode1(), y, adjEdge.getNode2())){
                            triples.add(new Triple(edge.getNode1(), y, adjEdge.getNode2()));
                        }
                    }
                }
            }
        }
        return triples;
    }


    /**
     * Finds the association or seperation sets for every pair of nodes.
     */
    private List<Set<IonIndependenceFacts>> findSepAndAssoc() {
        Set<IonIndependenceFacts> separations = new HashSet<IonIndependenceFacts>();
        Set<IonIndependenceFacts> associations = new HashSet<IonIndependenceFacts>();
        Set<NodePair> allNodes = makeAllPairs(this.variables);

        for (NodePair pair : allNodes) {
            Node x = pair.getFirst();
            Node y = pair.getSecond();

            List<Node> variables = new ArrayList<Node>(this.variables);
            variables.remove(x);
            variables.remove(y);

            List<Set<Node>> subsets = SearchGraphUtils.powerSet(variables);

            IonIndependenceFacts indep = new IonIndependenceFacts(x, y, new HashSet<List<Node>>());
            IonIndependenceFacts assoc = new IonIndependenceFacts(x, y, new HashSet<List<Node>>());
            boolean addIndep = false;
            boolean addAssoc = false;

            for (Pag pag : input) {
                for (Set<Node> subset : subsets) {
                    if (containsAll(pag, subset, pair) ) {
                        if (pag.isDSeparatedFrom(x, y, new ArrayList<Node>(subset))) {
                            if (this.notadjacent.contains(pair)) {
                                addIndep = true;
                                indep.addMoreZ(new ArrayList<Node>(subset));
                            }
                        }
                        else {
                            addAssoc = true;
                            assoc.addMoreZ(new ArrayList<Node>(subset));
                        }
                    }
                }
            }
            if (addIndep) separations.add(indep);
            if (addAssoc) associations.add(assoc);
        }

        List<Set<IonIndependenceFacts>> ret = new ArrayList<Set<IonIndependenceFacts>> (2);
        ret.add(0,separations);
        ret.add(1,associations);
        return ret;
    }


    /**
     * States whether the given graph contains the nodes in the given set and the
     * node pair.
     */
    private static boolean containsAll(Graph g, Set<Node> nodes, NodePair pair) {
        if (!g.containsNode(pair.getFirst()) || !g.containsNode(pair.getSecond())) {
            return false;
        }
        for (Node node : nodes) {
            if (!g.containsNode(node)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Remove all X,Y such that X,Y are not adjacent in some input PAG.  Assumes the given graph
     * is fully connected etc.
     */
    private void removeAdjacencies(Graph graph) {
        for (Edge edge : graph.getEdges()) {
            for (Graph input : this.input) {
                if (!input.containsNode(edge.getNode1()) || !input.containsNode(edge.getNode2())) {
                    continue;
                }
                if (!input.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                    graph.removeEdge(edge);
                    NodePair nodePair = new NodePair(edge.getNode1(), edge.getNode2());
                    this.notadjacent.add(nodePair);
                    TetradLogger.getInstance().details("Added to NotAdj : " + nodePair);
                }
            }
        }
    }


    /**
     * Transfers edge info from the input to the given graph.
     */
    private void transferOrientations(Pag graph) {
        Map<Edge, Edge> edgeMap = new HashMap<Edge, Edge>();
        Set<Edge> conflict = new HashSet<Edge>();
        // remove non-adjecent edges and transfer edge orientation.
        for (Edge sourceEdge : graph.getEdges()) {
            Node node1 = sourceEdge.getNode1();
            Node node2 = sourceEdge.getNode2();
            for (Graph pag : this.input) {
                if (!pag.containsNode(node1) || !pag.containsNode(node2)) {
                    continue;
                }
                Edge edge = pag.getEdge(node1, node2);
                if (edge == null) {
                    graph.removeEdge(sourceEdge);
                    NodePair nodePair = new NodePair(node1, node2);
                    this.notadjacent.add(nodePair);
                    TetradLogger.getInstance().details("Added to NotAdj: " + nodePair);
                } else if (isPartiallyDirected(edge) && !conflict.contains(edge)) {
                    Edge previous = edgeMap.get(sourceEdge);
                    if (previous != null && isConflict(previous, edge)) {
                        conflict.add(edge);
                        sourceEdge.setEndpoint1(Endpoint.CIRCLE);
                        sourceEdge.setEndpoint2(Endpoint.CIRCLE);
                    } else {
                        Edge clone = new Edge(sourceEdge);
                        if (sourceEdge.getNode1().equals(edge.getNode1())) {
                            sourceEdge.setEndpoint1(edge.getEndpoint1());
                            sourceEdge.setEndpoint2(edge.getEndpoint2());
                        } else {
                            sourceEdge.setEndpoint1(edge.getEndpoint2());
                            sourceEdge.setEndpoint2(edge.getEndpoint1());
                        }
                        TetradLogger.getInstance().details("Oriented edge " + clone + " to " + sourceEdge);
                        edgeMap.put(clone, new Edge(sourceEdge));
                    }
                }
            }
        }
        // Now deal with underline orientations.
        for (Graph g : this.input) {
            Pag pag = (Pag) g;
            for (Triple triple : pag.getUnderLineTriples()) {
                if (graph.isAdjacentTo(triple.getX(), triple.getY()) && graph.isAdjacentTo(triple.getZ(), triple.getY())) {
                    TetradLogger.getInstance().details("Marked definite noncollider:  " + triple);
                    graph.addUnderlineTriple(triple);
                }
            }
        }
    }


    /**
     * States whether there is a conflict between the previous edge and the current one.
     */
    private static boolean isConflict(Edge previous, Edge current) {
        Node node1 = previous.getNode1();
        if (previous.getEndpoint1() != Endpoint.CIRCLE && previous.getEndpoint1() != current.getProximalEndpoint(node1)) {
            return true;
        }
        Node node2 = previous.getNode2();
        return current.getEndpoint2() != Endpoint.CIRCLE && previous.getEndpoint2() != current.getProximalEndpoint(node2);
    }


    /**
     * True if the given graph contains nodes that match the triple (three adjacent nodes).
     */
    private static boolean matchesTriple(Graph graph, Triple triple) {
        if (!graph.containsNode(triple.getX()) || !graph.containsNode(triple.getY()) ||
                !graph.containsNode(triple.getZ())) {
            return false;
        }
        Edge edge1 = graph.getEdge(triple.getX(), triple.getY());
        Edge edge2 = graph.getEdge(triple.getY(), triple.getZ());

        if (!graph.containsEdge(edge1) || !graph.containsEdge(edge2)) {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if (edge1.getEndpoint1() != Endpoint.CIRCLE || edge1.getEndpoint2() != Endpoint.CIRCLE) {
            return false;
        }
        return !(edge2.getEndpoint1() != Endpoint.CIRCLE || edge2.getEndpoint2() != Endpoint.CIRCLE);
    }


    /**
     * States whether the edge is "partially directed", such as A 0-> B (this may have
     * a real name?)
     */
    private static boolean isPartiallyDirected(Edge edge) {
        Endpoint end1 = edge.getEndpoint1();
        Endpoint end2 = edge.getEndpoint2();
        return end1 == Endpoint.ARROW || end1 == Endpoint.TAIL ||
                end2 == Endpoint.ARROW || end2 == Endpoint.TAIL;
    }


    /**
     * Checks endpoints to determine whether edge is undirected
     */
    private static boolean isUndirected(Edge edge){
        return edge.getEndpoint1() == Endpoint.CIRCLE && edge.getEndpoint2() == Endpoint.CIRCLE;
    }


    private static void logGraphs(String message, List<? extends Graph> graphs) {
        if (message != null) {
            TetradLogger.getInstance().log("graph", message);
        }
        for (Graph graph : graphs) {
            TetradLogger.getInstance().log("graph", graph.toString());
        }
    }


    /**
     * Checks given pag against a set of necessary associations to determine if the pag
     * implies an indepedence where one is known to not exist.
     */
    private static boolean predictsFalseIndependence (Set<IonIndependenceFacts> associations, Pag pag) {
        for (IonIndependenceFacts assocFact : associations)
            for (List<Node> conditioningSet : assocFact.getZ())
                if ( pag.isDSeparatedFrom(
                        assocFact.getX(), assocFact.getY() , conditioningSet ) )
                    return true;
        return false;
    }


    /**
     * Returns whether the given Pag contains any circular triples
     */
    private static boolean hasCircularPath (Pag pag) {
        Set<Triple> triples = getAllTriples(pag);
        for (Triple trip : triples) {
            Edge one = pag.getEdge(trip.getX(), trip.getY());
            Edge two = pag.getEdge(trip.getY(), trip.getZ());
            Edge three = pag.getEdge(trip.getZ(), trip.getZ());
            if (one.getDistalEndpoint(trip.getX()).equals(Endpoint.ARROW)
                    && two.getDistalEndpoint(trip.getY()).equals(Endpoint.ARROW)
                    && three.getDistalEndpoint(trip.getZ()).equals(Endpoint.ARROW) )
                return true;
        }
        return false;
    }


    /**
     * Creates a set of NodePairs of all possible pairs of nodes from given
     * list of nodes.
     */
    private static Set<NodePair> makeAllPairs (List<Node> nodes) {
        Set<NodePair> allNodes = new HashSet<NodePair>();
        for (int i = 0 ; i<nodes.size() ; i++)
            for (int j = i+1 ; j < nodes.size() ; j++)
                allNodes.add(new NodePair(nodes.get(i), nodes.get(j)));

        return allNodes;
    }


    /**
     * Given a map between sets of conditioned on variables and lists of PossibleDConnectingPaths,
     * finds all the possible GraphChanges which could be used to block said paths
     */
    private static List findChanges (Map<Collection<Node>, List<PossibleDConnectingPath>> paths) {
        List<Set<GraphChange>> pagChanges = new ArrayList<Set<GraphChange>>();

        Set<Map.Entry<Collection<Node>, List<PossibleDConnectingPath>>> entries = paths.entrySet();

        /* Loop through each entry, ie each conditioned set of variables. */
        for (Map.Entry<Collection<Node>, List<PossibleDConnectingPath>> entry : entries) {
            Collection<Node> conditions = entry.getKey();
            List<PossibleDConnectingPath> dConnecting = entry.getValue();

            /* loop through each path */
            for (PossibleDConnectingPath possible : dConnecting ) {
                List<Node> possPath = possible.getPath();

                /* Created with 2*# of paths as appoximation. might have to increase size once */
                Set<GraphChange> pathChanges = new HashSet<GraphChange>(2 * possPath.size());

                /* find those conditions which are not along the path (used in colider) */
                List<Node> outsidePath = new ArrayList<Node>(conditions.size());
                for (Node condition : conditions) {
                    if (!possPath.contains(condition))
                        outsidePath.add(condition);
                }

                /* Walk through path, node by node */
                for (int i = 0; i < possPath.size() - 1; i++) {
                    Node current = possPath.get(i);
                    Node next = possPath.get(i + 1);
                    GraphChange gc;

                    /* for each pair of nodes, add the operation to remove their edge */
                    gc = new GraphChange();
                    gc.addRemove(possible.getPag().getEdge(current, next));
                    pathChanges.add(gc);

                    /* for each triple centered on a node which is an element of the conditioning
                     * set, add the operation to orient as a nonColider around that node */
                    if (conditions.contains(current) && i > 0) {
                        gc = new GraphChange();
                        Triple nonColider = new Triple( possPath.get(i - 1), current, next);
                        gc.addNonCollider(nonColider);
                        pathChanges.add(gc);
                    }

                    /* for each node on the path not in the conditioning set, make a colider. It
                     * is necessary though to ensure that there are no paths implying that a
                     * conditioned variable (even outside the path) is a decendant of a colider */
                    if ((!conditions.contains(current)) && i > 0) {
                        Triple colider = new Triple( possPath.get(i - 1), current, next);

                        if ( possible.getPag().isUnderlineTriple(possPath.get(i-1), current, next) )
                            continue;

                        Edge edge1 = possible.getPag().getEdge(colider.getX(), colider.getY());
                        Edge edge2 = possible.getPag().getEdge(colider.getZ(), colider.getY());

                        if (edge1.getNode1().equals(colider.getY())) {
                            if (edge1.getEndpoint1().equals(Endpoint.TAIL)) {
                                continue;
                            }
                        }
                        else if (edge1.getNode2().equals(colider.getY())) {
                            if (edge1.getEndpoint2().equals(Endpoint.TAIL)) {
                                continue;
                            }
                        }

                        if (edge2.getNode1().equals(colider.getY())) {
                            if (edge2.getEndpoint1().equals(Endpoint.TAIL)) {
                                continue;
                            }
                        }
                        else if (edge2.getNode2().equals(colider.getY())) {
                            if (edge2.getEndpoint2().equals(Endpoint.TAIL)) {
                                continue;
                            }
                        }

                        /* Simple case, no conditions outside the path, so just add colider */
                        if (outsidePath.size() == 0) {
                            gc = new GraphChange();
                            gc.addCollider(colider);
                            pathChanges.add(gc);
                            continue;
                        }

                        /* ensure nondecendency in possible path between current and each conditioned
                         * variable outside the path */
                        for (Node outside : outsidePath) {

                            /* list of possible decendant paths */

                            List<PossibleDConnectingPath> decendantPaths = new ArrayList<PossibleDConnectingPath>();
                            lock.lock();
                            try {
                                    decendantPaths
                                    = PossibleDConnectingPath.findDConnectingPaths
                                    (possible.getPag(), current, outside, new ArrayList<Node>());
                            }
                            finally {lock.unlock();}


                            /* loop over each possible path which might indicate decendency */
                            for (PossibleDConnectingPath decendantPDCPath : decendantPaths) {
                                List<Node> decendantPath = decendantPDCPath.getPath();

                                /* walk down path checking orientation (path may already
                                 * imply non-decendency) and creating changes if need be*/
                                boolean impliesDecendant = true;
                                Set<GraphChange> colideChanges = new HashSet<GraphChange>();
                                for (int j = 0; j < decendantPath.size() - 1; j++) {
                                    Node from = decendantPath.get(j);
                                    // chaneges from +1
                                    Node to = decendantPath.get(j+1);
                                    Edge currentEdge = possible.getPag().getEdge(from, to);

                                    if (currentEdge.getEndpoint1().equals(Endpoint.ARROW)) {
                                        impliesDecendant = false;
                                        break;
                                    }

                                    gc = new GraphChange();
                                    gc.addCollider(colider);
                                    gc.addRemove(currentEdge);
                                    colideChanges.add(gc);

                                    gc = new GraphChange();
                                    gc.addCollider(colider);
                                    gc.addOrient(to, from);
                                    colideChanges.add(gc);
                                }
                                if (impliesDecendant)
                                    pathChanges.addAll(colideChanges);
                            }
                        }
                    }
                }

                pagChanges.add(pathChanges);
            }
        }
        return pagChanges;
    }

    /**
     * Constructs PossRemove, every combination of removing of not removing redudant paths
     * @param pag The PAG to remove redundant edges from
     * @param necEdges A mapping of edges to booleans, indicating whether the edges are necessary
     * @return A list of possible PAGs
     */
    private List<Pag> possRemove(Pag pag, Map<Edge, Boolean> necEdges) {
        // list of edges that can be removed
        List<Edge> remEdges = new ArrayList<Edge>();
        for (Edge remEdge : necEdges.keySet()) {
            if (!necEdges.get(remEdge))
                remEdges.add(remEdge);
        }
        // powerset of edges that can be removed
        PowerSet<Edge> pset = new PowerSet<Edge>(remEdges);
        List<Pag> possRemove = new ArrayList<Pag>();
        // for each set of edges in the powerset remove edges from graph and add to PossRemove
        for (Set<Edge> set : pset) {
            Pag newPag = new Pag(pag);
            for (Edge edge : set) {
                newPag.removeEdge(edge);
            }
            possRemove.add(newPag);
         //   TetradLogger.getInstance().details("Added to PossRemove : " + pag);
        }
        return possRemove;
    }

    // FCI orientations for preventing cycles
    // Does all 3 of these rules at once instead of going through all
    // triples multiple times per iteration of doFinalOrientation.
    private void awayFromColliderAncestorCycle(Graph graph) {
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
                awayFromCollider(graph, A, B, C);
                awayFromCollider(graph, C, B, A);
                awayFromAncestor(graph, A, B, C);
                awayFromAncestor(graph, C, B, A);
                awayFromCycle(graph, A, B, C);
                awayFromCycle(graph, C, B, A);
            }
        }
    }


    private boolean isArrowpointAllowed(Graph graph, Node x, Node y) {
        if (graph.getEndpoint(x, y) == Endpoint.ARROW) {
            return true;
        }

        if (graph.getEndpoint(x, y) == Endpoint.TAIL) {
            return false;
        }

        if (graph.getEndpoint(y, x) == Endpoint.ARROW &&
                graph.getEndpoint(x, y) == Endpoint.CIRCLE) {
            return true;
        }

        return false;
    }


    // if a*->Bo-oC and not a*-*c, then a*->b-->c
    // (orient either circle if present, don't need both)
    private void awayFromCollider(Graph graph, Node a, Node b, Node c) {
        Endpoint BC = graph.getEndpoint(b, c);
        Endpoint CB = graph.getEndpoint(c, b);

        if (!(graph.isAdjacentTo(a, c)) &&
                (graph.getEndpoint(a, b) == Endpoint.ARROW)) {
            if (CB == Endpoint.CIRCLE || CB == Endpoint.TAIL) {
                if (BC == Endpoint.CIRCLE) {
                    if (!isArrowpointAllowed(graph, b, c)) {
                        return;
                    }

                    graph.setEndpoint(b, c, Endpoint.ARROW);
                    changeFlag = true;
                }
            }
            if (BC == Endpoint.CIRCLE || BC == Endpoint.ARROW) {
                if (CB == Endpoint.CIRCLE) {
                    graph.setEndpoint(c, b, Endpoint.TAIL);
                    changeFlag = true;
                }
            }
        }
    }

    //if a*-oC and either a-->b*->c or a*->b-->c, then a*->c
    private void awayFromAncestor(Graph graph, Node a, Node b, Node c) {
        if ((graph.isAdjacentTo(a, c)) &&
                (graph.getEndpoint(a, c) == Endpoint.CIRCLE)) {

            if ((graph.getEndpoint(a, b) == Endpoint.ARROW) &&
                    (graph.getEndpoint(b, c) == Endpoint.ARROW) && (
                    (graph.getEndpoint(b, a) == Endpoint.TAIL) ||
                            (graph.getEndpoint(c, b) == Endpoint.TAIL))) {

                if (!isArrowpointAllowed(graph, a, c)) {
                    return;
                }

                graph.setEndpoint(a, c, Endpoint.ARROW);
                changeFlag = true;
            }
        }
    }

    //if Ao->c and a-->b-->c, then a-->c
    private void awayFromCycle(Graph graph, Node a, Node b, Node c) {
        if ((graph.isAdjacentTo(a, c)) &&
                (graph.getEndpoint(a, c) == Endpoint.ARROW) &&
                (graph.getEndpoint(c, a) == Endpoint.CIRCLE)) {
            if (graph.isDirectedFromTo(a, b) && graph.isDirectedFromTo(b, c)) {
                graph.setEndpoint(c, a, Endpoint.TAIL);
                changeFlag = true;
            }
        }
    }


    /**
     * Exactly the same as edu.cmu.tetrad.graph.IndependenceFact excepting this class allows
     * for multiple conditioning sets to be associated with a single pair of nodes, which is
     * necessary for the proper ordering of iterations in the ION search.
     */
    private final class IonIndependenceFacts {
        private Node x;
        private Node y;
        private Collection<List<Node>> z;

        /**
         * Constructs a triple of nodes.
         */
        public IonIndependenceFacts(Node x, Node y, Collection<List<Node>> z) {
            if (x == null || y == null || z == null) {
                throw new NullPointerException();
            }

            this.x = x;
            this.y = y;
            this.z = z;
        }

        public final Node getX() {
            return x;
        }

        public final Node getY() {
            return y;
        }

        public final Collection<List<Node>> getZ() {
            return z;
        }

        public void addMoreZ(List<Node> moreZ) {
            z.add(moreZ);
        }

        @Override
		public final int hashCode() {
            int hash = 17;
            hash += 19 * x.hashCode() * y.hashCode();
            hash += 23 * z.hashCode();
            return hash;
        }

        @Override
		public final boolean equals(Object obj) {
            if (!(obj instanceof IonIndependenceFacts)) {
                return false;
            }

            IonIndependenceFacts fact = (IonIndependenceFacts) obj;
            return (x.equals(fact.x) && y.equals(fact.y) &&
                    z.equals( fact.z))
                    || (x.equals(fact.y) & y.equals(fact.x) &&
                    z.equals( fact.z));
        }

        @Override
		public String toString() {
            return "I(" + x + ", " + y + " | " + z + ")";
        }
    }

   /***
    * A PowerSet constructed with a collection with elements of type E can construct
    * an Iterator which enumerates all possible subsets (of type Collection<E>) of the
    * collection used to construct the PowerSet.
    *
    * @author pingel
    *
    * @param <E> The type of elements in the Collection passed to the constructor.
    */

    private class PowerSet<E> implements Iterable<Set<E>>
    {
        Collection<E> all;

        public PowerSet(Collection<E> all)
        {
            this.all = all;
        }

        /***
         * @return      an iterator over elements of type Collection<E> which enumerates
         *              the PowerSet of the collection used in the constructor
         */

        @Override
		public Iterator<Set<E>> iterator()
        {
            return new PowerSetIterator<E>(this);
        }

        class PowerSetIterator<InE> implements Iterator<Set<InE>>
        {
            PowerSet<InE> powerSet;
            List<InE> canonicalOrder = new ArrayList<InE>();
            List<InE> mask = new ArrayList<InE>();
            boolean hasNext = true;

            PowerSetIterator(PowerSet<InE> powerSet) {

                this.powerSet = powerSet;
                canonicalOrder.addAll(powerSet.all);
            }

            @Override
			public void remove()
            {
                throw new UnsupportedOperationException();
            }

            private boolean allOnes()
            {
                for( InE bit : mask) {
                    if( bit == null ) {
                        return false;
                    }
                }
                return true;
            }

            private void increment()
            {
                int i=0;
                while( true ) {
                    if( i < mask.size() ) {
                        InE bit = mask.get(i);
                        if( bit == null ) {
                            mask.set(i, canonicalOrder.get(i));
                            return;
                        }
                        else {
                            mask.set(i, null);
                            i++;
                        }
                    }
                    else {
                        mask.add(canonicalOrder.get(i));
                        return;
                   }
                }
            }

            @Override
			public boolean hasNext()
            {
                return hasNext;
            }

            @Override
			public Set<InE> next()
            {

                Set<InE> result = new HashSet<InE>();
                result.addAll(mask);
                result.remove(null);

                hasNext = mask.size() < powerSet.all.size() || ! allOnes();

                if( hasNext ) {
                    increment();
                }

                return result;

            }

        }
    }
}
