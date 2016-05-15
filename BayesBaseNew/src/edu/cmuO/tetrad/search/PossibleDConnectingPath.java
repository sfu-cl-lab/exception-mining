package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;

import java.util.*;

/**
 * Finds possible d-connecting paths for the IonSearch.
 * <p/>
 * Not thread safe.
 *
 * @author Tyler Gibson
 */
public class PossibleDConnectingPath {


    /**
     * The pag we are searching in.
     */
    private Pag pag;


    /**
     * The conditions.
     */
    private Set<Node> conditions;


    /**
     * The path.
     */
    private List<Node> path;


    private PossibleDConnectingPath(Pag p, Set<Node> conditions, List<Node> path) {
        if (p == null || conditions == null || path == null) {
            throw new NullPointerException();
        }
        this.conditions = conditions;
        this.path = path;
        this.pag = p;
    }


    //========================= Public methods ======================//


    public Pag getPag() {
        return this.pag;
    }

    public Set<Node> getConditions() {
        return Collections.unmodifiableSet(conditions);
    }

    public List<Node> getPath() {
        return Collections.unmodifiableList(path);
    }


    /**
     * Finds all possible D-connection paths as sub-graphs of the pag given at
     * construction time from x to y given z.
     */
    public static List<PossibleDConnectingPath> findDConnectingPaths(Pag pag, Node x, Node y, Collection<Node> z) {
        if (!pag.containsNode(x) || !pag.containsNode(y) || x.equals(y)) {
            return Collections.emptyList();
        }
        for (Node node : z) {
            if (!pag.containsNode(node)) {
                return Collections.emptyList();
            }
        }
        if (pag.isAdjacentTo(x, y)) {
            return Collections.singletonList(new PossibleDConnectingPath(pag, new HashSet<Node>(z), Arrays.asList(x, y)));
        }
        List<PossibleDConnectingPath> connectingPaths = new LinkedList<PossibleDConnectingPath>();
        Set<Node> conditions = new HashSet<Node>(z);
        Set<Node> closure = getConditioningClosure(pag, z);
        Set<List<Node>> paths = new HashSet<List<Node>>();
        findPaths(pag, paths, null, x, y, closure, new LinkedList<Node>());
        for (List<Node> path : paths) {
            connectingPaths.add(new PossibleDConnectingPath(pag, conditions, path));
        }
        return connectingPaths;
    }

    /**
     * Finds all possible D-connection paths as sub-graphs of the pag given at
     * construction time from x to y given z for a particular path length.
     */
    public static List<PossibleDConnectingPath> findDConnectingPathsOfLength(Pag pag, Node x, Node y, Collection<Node> z, Integer length) {
        if (!pag.containsNode(x) || !pag.containsNode(y) || x.equals(y)) {
            return Collections.emptyList();
        }
        for (Node node : z) {
            if (!pag.containsNode(node)) {
                return Collections.emptyList();
            }
        }
        if (pag.isAdjacentTo(x, y)) {
            return Collections.singletonList(new PossibleDConnectingPath(pag, new HashSet<Node>(z), Arrays.asList(x, y)));
        }
        List<PossibleDConnectingPath> connectingPaths = new LinkedList<PossibleDConnectingPath>();
        Set<Node> conditions = new HashSet<Node>(z);
        Set<Node> closure = getConditioningClosure(pag, z);
        Set<List<Node>> paths = new HashSet<List<Node>>();
        findPathsOfLength(pag, paths, null, x, y, closure, new LinkedList<Node>(), length);
        for (List<Node> path : paths) {
            connectingPaths.add(new PossibleDConnectingPath(pag, conditions, path));
        }
        return connectingPaths;
    }



    @Override
	public boolean equals(Object o){
        if(!(o instanceof PossibleDConnectingPath)){
            return false;
        }
        PossibleDConnectingPath p = (PossibleDConnectingPath)o;
        return p.pag.equals(pag) && p.path.equals(path) && p.conditions.equals(conditions);
    }

 /*
    public int hashCode(){
        int result = 17;
        result += 19 * pag.hashCode();
        result += 23 * path.hashCode();
        result += 27 * conditions.hashCode();

        return result;
    }

   */

    //================================== Private methods =======================//


    private static Set<Node> getConditioningClosure(Pag pag, Collection<Node> z) {
        Set<Node> closure = new HashSet<Node>();
        for (Node node : z) {
            doParentClosureVisit(pag, node, closure);
        }
        return closure;
    }


    /**
     * Find the closure of a conditioning set of nodes under the parent
     * relation.
     *
     * @param pag
     * @param node    the node in question
     * @param closure the closure of the conditioning set uner the parent
     *                relation (to be calculated recursively).
     */
    private static void doParentClosureVisit(Pag pag, Node node, Set<Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Edge edge1 : pag.getEdges(node)) {
                Node sub = Edges.traverseReverseDirected(node, edge1);

                if (sub == null) {
                    continue;
                }

                doParentClosureVisit(pag, sub, closure);
            }
        }
    }


    /**
     * Recursive methods that finds all the paths.
     */
    private static void findPaths(Pag pag, Set<List<Node>> paths, Node previous, Node current,
                                  Node target, Set<Node> conditionClosure, List<Node> history) {
        // check for cycles.
        if (history.contains(current)) {
            return;
        }
        // add path if we've reached the target.
        if (current.equals(target)) {
            history.add(current);
            paths.add(history);
            return;
        }
        // recurse
        List<Node> adjacencies = pag.getAdjacentNodes(current);
        for (Node adj : adjacencies) {
            if (previous == null) {
                List<Node> h = new ArrayList<Node>(history);
                h.add(current);
                findPaths(pag, paths, current, adj, target, conditionClosure, h);
                continue;
            }
            boolean pass;
            boolean isCondition = conditionClosure.contains(current);
            if (pag.isDefiniteCollider(previous, current, adj)) {
                pass = isCondition;
            } else {
                pass = !isCondition || !pag.isUnderlineTriple(previous, current, adj) && isOpen(pag, previous, current, adj);
            }

            if (pass) {
                List<Node> h = new ArrayList<Node>(history);
                h.add(current);
                findPaths(pag, paths, current, adj, target, conditionClosure, h);
            }
        }

    }

    /**
     * Recursive methods that finds all the paths of a specified length.
     */
    private static void findPathsOfLength(Pag pag, Set<List<Node>> paths, Node previous, Node current,
                                  Node target, Set<Node> conditionClosure, List<Node> history, Integer length) {
        // checks if size greater than length
        if (history.size() > length) {
            return;
        }

        // check for cycles.
        if (history.contains(current)) {
            return;
        }
        // add path if we've reached the target and is of correct length.
        if (current.equals(target)) {
            history.add(current);
            if (history.size() - 1 == length) {
                paths.add(history);
            }
            return;
        }
        // recurse
        List<Node> adjacencies = pag.getAdjacentNodes(current);
        for (Node adj : adjacencies) {
            if (previous == null) {
                List<Node> h = new ArrayList<Node>(history);
                h.add(current);
                findPaths(pag, paths, current, adj, target, conditionClosure, h);
                continue;
            }
            boolean pass;
            boolean isCondition = conditionClosure.contains(current);
            if (pag.isDefiniteCollider(previous, current, adj)) {
                pass = isCondition;
            } else {
                pass = !isCondition || !pag.isUnderlineTriple(previous, current, adj) && isOpen(pag, previous, current, adj);
            }

            if (pass) {
                List<Node> h = new ArrayList<Node>(history);
                h.add(current);
                findPaths(pag, paths, current, adj, target, conditionClosure, h);
            }
        }

    }


    private static boolean isOpen(Pag pag, Node x, Node y, Node z) {
        Edge edge = pag.getEdge(x, y);
        if(edge.getEndpoint1() != Endpoint.CIRCLE || edge.getEndpoint2() != Endpoint.CIRCLE){
            return false;
        }
        edge = pag.getEdge(y, z);
        return edge.getEndpoint1() == Endpoint.CIRCLE && edge.getEndpoint2() == Endpoint.CIRCLE;
    }

}
