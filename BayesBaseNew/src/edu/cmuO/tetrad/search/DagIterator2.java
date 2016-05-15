package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.CombinationGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a graph, lists all DAGs that result from directing the undirected
 * edges in that graph every possible way. Does it the old fashioned way,
 * by actually producing all of the graphs you would get by orienting each
 * uoriented edge each way, in every combination, and the rembembering just
 * the ones that were acyclic.
 *
 * @author Joseph Ramsey
 */
public class DagIterator2 {
    private List<Dag> dags = new ArrayList<Dag>();
    private int index = -1;


    /**
     * The given graph must be a graph. If it does not consist entirely of
     * directed and undirected edges and if it is not acyclic, it is rejected.
     *
     * @throws IllegalArgumentException if the graph is not a graph.
     */
    public DagIterator2(Graph graph) {
        graph = new EdgeListGraph(graph);
        List<Edge> undirectedEdges = new ArrayList<Edge>();

        for (Edge edge : graph.getEdges()) {
            if (Edges.isUndirectedEdge(edge)) {
                undirectedEdges.add(edge);
            }
        }

        int[] dims = new int[undirectedEdges.size()];

        for (int i = 0; i < undirectedEdges.size(); i++) {
            dims[i] = 2;
        }

        CombinationGenerator generator = new CombinationGenerator(dims);
        int[] combination;

        while ((combination = generator.next()) != null) {
            for (int k = 0; k < combination.length; k++) {
                Edge edge = undirectedEdges.get(k);
                graph.removeEdge(edge.getNode1(), edge.getNode2());

                if (combination[k] == 0) {
                    graph.addDirectedEdge(edge.getNode1(), edge.getNode2());
                }
                else {
                    graph.addDirectedEdge(edge.getNode2(), edge.getNode1());
                }
            }

            if (!graph.existsDirectedCycle()) {
                dags.add(new Dag(graph));
            }
        }
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
        ++index;

        if (index < dags.size()) {
            return dags.get(index);
        }
        else {
            return null;
        }
    }

    /**
     * Returns true just in case there is still a DAG remaining in the
     * enumeration of DAGs for this pattern.
     */
    public boolean hasNext() {
        return index + 1 < dags.size();
    }
}