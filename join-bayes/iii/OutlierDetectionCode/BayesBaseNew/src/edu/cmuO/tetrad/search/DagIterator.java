package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;

import java.util.LinkedList;

/**
 * Given a graph, lists all DAGs that result from directing the undirected
 * edges in that graph every possible way. Uses a Meek-algorithm-type
 * method.
 *
 * @author Joseph Ramsey
 */
public class DagIterator {

    /**
     * The stack of graphs, with annotations as to the arbitrary undirected
     * edges chosen in them and whether or not these edges have already been
     * oriented left and/or right.
     */
    private LinkedList<DecoratedGraph> decoratedGraphs = new LinkedList<DecoratedGraph>();
    private Dag storedDag;

    /**
     * The given pattern must be a pattern. If it does not consist entirely of
     * directed and undirected edges and if it is not acyclic, it is rejected.
     *
     * @throws IllegalArgumentException if the pattern is not a pattern.
     */
    public DagIterator(Graph pattern) {

        for (Edge edge : pattern.getEdges()) {
            if (Edges.isDirectedEdge(edge) || Edges.isUndirectedEdge(edge)) {
                continue;
            }

            throw new IllegalArgumentException("The graph may consist only of " +
                    "directed and undirected edges: " + edge);
        }

        if (pattern.existsDirectedCycle()) {
            throw new IllegalArgumentException("The graph must be is acyclic: " +
                    GraphUtils.directedCycle(pattern));
        }

        decoratedGraphs.add(new DecoratedGraph(pattern));
    }

    /**
     * Successive calls to this method return successive DAGs in the pattern, in
     * a more or less natural enumeration of them in which an arbitrary
     * undirected edge is picked, oriented one way, Meek rules applied, then a
     * remaining unoriented edge is picked, oriented one way, and so on, until a
     * DAG is obtained, and then by backtracking the other orientation of each
     * chosen edge is tried. Nonrecursive, obviously.
     *
     * Returns a Graph instead of a DAG because sometimes, due to faulty patterns,
     * a cyclic graph is produced, and the end-user may need to decide what to
     * do with it. The simplest thing is to construct a DAG (Dag(graph)) and
     * catch an exception.
     */
    public Dag next() {
        if (storedDag != null) {
            Dag temp = storedDag;
            storedDag = null;
            return temp;
        }

        if (decoratedGraphs.isEmpty()) {
            return null;
        }

        // If it's a DAG
        if (!decoratedGraphs.getLast().hasUndirectedEdge()) {

            // Go back to the lastmost decorated graph whose successor is
            // oriented right, and add a graph with that oriented left.
            while (true) {
                if (decoratedGraphs.isEmpty()) {
                    return null;
                }

                DecoratedGraph graph = decoratedGraphs.removeLast();

                if (graph.hasUndirectedEdge() && !graph.wasDirectedRight()) {
                    throw new IllegalStateException();
                }

                if (decoratedGraphs.isEmpty() && !graph.hasUndirectedEdge()) {
                    return new Dag(graph.getGraph());
                }

                if (graph.wasDirectedRight() && !graph.wasDirectedLeft()) {
                    decoratedGraphs.add(graph);
                    DecoratedGraph graph1 = graph.directLeft();

                    if (graph1 == null) {
                        continue;
                    }

                    decoratedGraphs.add(graph1);
                    break;
                }
            }
        }

        // Apply right orientations and Meek orientations until there's a DAG.
        while (decoratedGraphs.getLast().hasUndirectedEdge()) {
            DecoratedGraph graph = decoratedGraphs.getLast().directRight();

            if (graph == null) {
                continue;
            }

            decoratedGraphs.add(graph);
        }

        // Return the DAG.
        return new Dag(decoratedGraphs.getLast().getGraph());
    }

    /**
     * Returns true just in case there is still a DAG remaining in the
     * enumeration of DAGs for this pattern.
     */
    public boolean hasNext() {
        if (storedDag == null) {
            storedDag = next();
        }

        return storedDag != null;
    }

    //==============================CLASSES==============================//

    private static class DecoratedGraph {
        private Graph graph;
        private Edge edge;
        private boolean wasDirectedRight = false;
        private boolean wasDirectedLeft = false;

        public DecoratedGraph(Graph graph) {
            this.setGraph(graph);
            this.edge = findUndirectedEdge(graph);
        }

        private Edge findUndirectedEdge(Graph graph) {
            for (Edge edge : graph.getEdges()) {
                if (Edges.isUndirectedEdge(edge)) {
                    return edge;
                }
            }

            return null;
        }

        private boolean hasUndirectedEdge() {
            return edge != null;
        }

        public Graph getGraph() {
            return graph;
        }

        public void setGraph(Graph graph) {
            this.graph = graph;
        }

        public Edge getEdge() {
            return edge;
        }

        public void setEdge(Edge edge) {
            this.edge = edge;
        }

        public boolean wasDirectedRight() {
            return wasDirectedRight;
        }

        public boolean wasDirectedLeft() {
            return wasDirectedLeft;
        }

        public DecoratedGraph directLeft() {
            if (edge == null) {
                throw new IllegalArgumentException();
            }

            if (graph.isAncestorOf(edge.getNode1(), edge.getNode2())) {
                wasDirectedLeft = true;
                return directRight();
            }

            if (!wasDirectedLeft) {
                Graph graph = new EdgeListGraph(this.graph);
                graph.removeEdge(edge.getNode1(), edge.getNode2());
                graph.addDirectedEdge(edge.getNode2(), edge.getNode1());
//                System.out.println("Orienting " + graph.getEdge(edge.getNode1(), edge.getNode2()));
                wasDirectedLeft = true;
                return new DecoratedGraph(graph);
            }

            return null;
        }

        public DecoratedGraph directRight() {
            if (edge == null) {
                throw new IllegalArgumentException();
            }

            if (graph.isAncestorOf(edge.getNode2(), edge.getNode1())) {
                wasDirectedRight = true;
                return directLeft();
            }

            if (!wasDirectedRight) {
                Graph graph = new EdgeListGraph(this.graph);
                graph.removeEdge(edge.getNode1(), edge.getNode2());
                graph.addDirectedEdge(edge.getNode1(), edge.getNode2());
//                System.out.println("Orienting " + graph.getEdge(edge.getNode1(), edge.getNode2()));
                wasDirectedRight = true;
                return new DecoratedGraph(graph);
            }

            return null;
        }

        @Override
		public String toString() {
             return graph.toString();
        }
    }
}