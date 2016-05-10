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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.session.SessionModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Compares a target workbench with a reference workbench by counting errors of
 * omission and commission.  (for edge presence only, not orientation).
 *
 * @author Joseph Ramsey
 * @author Erin Korber (added remove latents functionality July 2004)
 * @version $Revision: 6535 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class GraphComparison implements SessionModel {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private GraphComparisonParams params;

    /**
     * The target workbench.
     *
     * @serial Cannot be null.
     */
    private final Graph targetGraph;

    /**
     * The workbench to which the target workbench is being compared.
     *
     * @serial Cannot be null.
     */
    private final Graph referenceGraph;

    /**
     * The true DAG, if available. (May be null.)
     */
    private Graph trueGraph;

    /**
     * @serial
     * @deprecated
     */
    @Deprecated
	private int numMissingEdges;

    /**
     * @serial
     * @deprecated
     */
    @Deprecated
	private int numCorrectEdges;

    /**
     * @serial
     * @deprecated
     */
    @Deprecated
	private int commissionErrors;

    /**
     * The number of correct edges the last time they were counted.
     *
     * @serial Range >= 0.
     */
    private int adjCorrect;

    /**
     * The number of errors of commission that last time they were counted.
     *
     * @serial Range >= 0.
     */
    private int adjFp;

    /**
     * The number of errors of omission the last time they were counted.
     *
     * @serial Range >= 0.
     */
    private int adjFn;

    /**
     * The number of correct edges the last time they were counted.
     *
     * @serial Range >= 0.
     */
    private int arrowptCorrect;

    /**
     * The number of errors of commission that last time they were counted.
     *
     * @serial Range >= 0.
     */
    private int arrowptFp;

    /**
     * The number of errors of omission the last time they were counted.
     *
     * @serial Range >= 0.
     */
    private int arrowptFn;

    /**
     * Arrowpoint false positives for edges that exist in the true graph.
     *
     * @serial Range >= 0.
     */
    private  int arrowptAfp;

    /**
     * Arrowpoint false negatives for edges that exist in the true graph.
     *
     * @serial Range >= 0.
     */
    private int arrowptAfn;

    /**
     * The list of edges that were added to the target graph. These are
     * new adjacencies.
     */
    private ArrayList<Edge> edgesAdded;

    /**
     * The list of edges that were removed from the reference graphs. These
     * are missing adjacencies.
     */
    private ArrayList<Edge> edgesRemoved;

    /**
     * The list of edges that were reoriented from the reference to the
     * target graph, as they were in the reference graph. This list
     * coordinates with <code>edgesReorientedTo</code>, in that
     * the i'th element of <code>edgesReorientedFrom</code> and the ith
     * element of <code>edgesReorientedTo</code> represent the same
     * adjacency.
     */
    private ArrayList<Edge> edgesReorientedFrom;

    /**
     * The list of edges that were reoriented from the reference to the
     * target graph, as they are in the target graph. This list
     * coordinates with <code>edgesReorientedFrom</code>, in that
     * the i'th element of <code>edgesReorientedFrom</code> and the ith
     * element of <code>edgesReorientedTo</code> represent the same
     * adjacency.
     */
    private ArrayList<Edge> edgesReorientedTo;

    //=============================CONSTRUCTORS==========================//

    /**
     * Compares the results of a Pc to a reference workbench by counting errors
     * of omission and commission. The counts can be retrieved using the methods
     * <code>countOmissionErrors</code> and <code>countCommissionErrors</code>.
     */
    public GraphComparison(SessionModel model1, SessionModel model2,
            GraphComparisonParams params) {
        if (params == null) {
            throw new NullPointerException("Params must not be null");
        }

        // Need to be able to construct this object even if the models are
        // null. Otherwise the interface is annoying.
        if (model2 == null) {
            model2 = new DagWrapper(new Dag());
        }

        if (model1 == null) {
            model1 = new DagWrapper(new Dag());
        }

        if (!(model1 instanceof GraphSource) ||
                !(model2 instanceof GraphSource)) {
            throw new IllegalArgumentException("Must be graph sources.");
        }

        this.params = params;

        String referenceName = this.params.getReferenceGraphName();

        if (referenceName == null) {
            this.referenceGraph = ((GraphSource) model1).getGraph();
            this.targetGraph = ((GraphSource) model2).getGraph();
            this.params.setReferenceGraphName(model1.getName());
        }
        else if (referenceName.equals(model1.getName())) {
            this.referenceGraph = ((GraphSource) model1).getGraph();
            this.targetGraph = ((GraphSource) model2).getGraph();
        }
        else if (referenceName.equals(model2.getName())) {
            this.referenceGraph = ((GraphSource) model2).getGraph();
            this.targetGraph = ((GraphSource) model1).getGraph();
        }
        else {
            throw new IllegalArgumentException(
                    "Neither of the supplied session " + "models is named '" +
                            referenceName + "'.");
        }

        doCounts();
    }

    public GraphComparison(GraphWrapper referenceGraph,
            AbstractAlgorithmRunner algorithmRunner,
            GraphComparisonParams params) {
        this(referenceGraph, (SessionModel) algorithmRunner,
                params);
    }

    public GraphComparison(GraphWrapper referenceWrapper,
            GraphWrapper targetWrapper, GraphComparisonParams params) {
        this(referenceWrapper, (SessionModel) targetWrapper,
                params);
    }

    public GraphComparison(DagWrapper referenceGraph,
            AbstractAlgorithmRunner algorithmRunner,
            GraphComparisonParams params) {
        this(referenceGraph, (SessionModel) algorithmRunner,
                params);
    }

    public GraphComparison(DagWrapper referenceWrapper,
            GraphWrapper targetWrapper, GraphComparisonParams params) {
        this(referenceWrapper, (SessionModel) targetWrapper,
                params);
    }

    public GraphComparison(Graph referenceGraph, Graph targetGraph) {
        this.referenceGraph = referenceGraph;
        this.targetGraph = targetGraph;
        doCounts();
    }

    public GraphComparison(Graph referenceGraph, Graph targetGraph,
                           Graph trueGraph) {
        this.referenceGraph = referenceGraph;
        this.targetGraph = targetGraph;
        this.trueGraph = trueGraph;
        doCounts();
    }

    private void doCounts() {
        Graph alteredRefGraph;

        //Normally, one's target graph won't have latents, so we'll want to
        // remove them from the ref graph to compare, but algorithms like
        // MimBuild might not want to do this.
        if (params != null && params.isKeepLatents()) {
            alteredRefGraph = this.referenceGraph;
        }
        else {
            alteredRefGraph = removeLatent(this.targetGraph);
        }
        this.adjFn = countAdjErrors(alteredRefGraph, targetGraph);
        this.adjFp = countAdjErrors(targetGraph, alteredRefGraph);
        this.adjCorrect = targetGraph.getNumEdges() - adjFp;

        this.arrowptFn = countArrowptErrors(alteredRefGraph, targetGraph);
        this.arrowptFp = countArrowptErrors(targetGraph, alteredRefGraph);

        this.arrowptAfn =
                countArrowptErrors(alteredRefGraph, targetGraph);
        this.arrowptAfp =
                countArrowptErrors(targetGraph, alteredRefGraph);
        this.arrowptCorrect = getNumArrowpts(targetGraph) - getArrowptFp();

        this.edgesAdded = new ArrayList<Edge>();
        this.edgesRemoved = new ArrayList<Edge>();
        this.edgesReorientedFrom = new ArrayList<Edge>();
        this.edgesReorientedTo = new ArrayList<Edge>();

        for (int i = 0; i < targetGraph.getNodes().size(); i++) {
            for (int j = i + 1; j < targetGraph.getNodes().size(); j++) {
                Node node1t = targetGraph.getNodes().get(i);
                Node node2t = targetGraph.getNodes().get(j);

                Node node1r = referenceGraph.getNode(node1t.getName());
                Node node2r = referenceGraph.getNode(node2t.getName());

                Edge edget = targetGraph.getEdge(node1t, node2t);
                Edge edger = referenceGraph.getEdge(node1r, node2r);

                if (edger == null && edget == null) {
                    continue;
                }

                if (edger == null) {
                    this.getEdgesAdded().add(edget);
                }
                else if (edget == null) {
                    this.getEdgesRemoved().add(edger);
                }
                else if (!(edger.equals(edget))) {
                    this.getEdgesReorientedFrom().add(edger);
                    this.getEdgesReorientedTo().add(edget);
                }
            }
        }

        if (params != null) {
            params.addRecord(getAdjCorrect(), getAdjFn(), getAdjFp(),
                    getArrowptCorrect(), getArrowptFn(), getArrowptFp(),
                    getArrowptAfn(), getArrowptAfp());
        }               
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GraphComparison serializableInstance() {
        return new GraphComparison(DagWrapper.serializableInstance(),
                DagWrapper.serializableInstance(),
                GraphComparisonParams.serializableInstance());
    }

    //==============================PUBLIC METHODS========================//

    public RectangularDataSet getDataSet() {
        return params.getDataSet();
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    public int getArrowptAfp() {
        return arrowptAfp;
    }

    public int getArrowptAfn() {
        return arrowptAfn;
    }

    public ArrayList<Edge> getEdgesAdded() {
        return edgesAdded;
    }

    public ArrayList<Edge> getEdgesRemoved() {
        return edgesRemoved;
    }

    public ArrayList<Edge> getEdgesReorientedFrom() {
        return edgesReorientedFrom;
    }

    public ArrayList<Edge> getEdgesReorientedTo() {
        return edgesReorientedTo;
    }

    @Override
	public String toString() {
        return "Errors of omission = " + getAdjFn() +
                ", Errors of commission = " + getAdjFp();
    }

    //============================PRIVATE METHODS=========================//

    /**
     * Counts the adjacencies that are in graph1 but not in graph2.
     *
     * @throws IllegalArgumentException if graph1 and graph2 are not namewise
     *                                  isomorphic.
     */
    private int countAdjErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List<Node> graph1Nodes = graph1.getNodes();
        List<Node> graph2Nodes = graph2.getNodes();

        Comparator<Node> comparator = new Comparator<Node>() {
            @Override
			public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        if (graph1Nodes.size() != graph2Nodes.size()) {
            throw new IllegalArgumentException(
                    "The graph sizes are different.");
        }

        for (int i = 0; i < graph1Nodes.size(); i++) {
            String name1 = graph1Nodes.get(i).getName();
            String name2 = graph2Nodes.get(i).getName();

            if (!name1.equals(name2)) {
                throw new IllegalArgumentException(
                        "Graph names don't " + "correspond.");
            }
        }

        List<Edge> edges1 = graph1.getEdges();

        for (Edge edge : edges1) {
            Node node1 = graph2.getNode(edge.getNode1().getName());
            Node node2 = graph2.getNode(edge.getNode2().getName());

            if (!graph2.isAdjacentTo(node1, node2)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Counts the arrowpoints that are in graph1 but not in graph2.
     */
    private static int countArrowptErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List<Node> graph1Nodes = graph1.getNodes();
        List<Node> graph2Nodes = graph2.getNodes();

        Comparator<Node> comparator = new Comparator<Node>() {
            @Override
			public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        if (graph1Nodes.size() != graph2Nodes.size()) {
            throw new IllegalArgumentException(
                    "The graph sizes are different.");
        }

        for (int i = 0; i < graph1Nodes.size(); i++) {
            String name1 = graph1Nodes.get(i).getName();
            String name2 = graph2Nodes.get(i).getName();

            if (!name1.equals(name2)) {
                throw new IllegalArgumentException(
                        "Graph names don't " + "correspond.");
            }
        }

        List<Edge> edges1 = graph1.getEdges();

        for (Edge edge1 : edges1) {
            Node node11 = edge1.getNode1();
            Node node12 = edge1.getNode2();

            Node node21 = graph2.getNode(node11.getName());
            Node node22 = graph2.getNode(node12.getName());

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null) {
                if (edge1.getEndpoint1() == Endpoint.ARROW) {
                    count++;
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW) {
                    count++;
                }
            }
            else {
                if (edge1.getEndpoint1() == Endpoint.ARROW) {
                    if (edge2.getProximalEndpoint(node21) != Endpoint.ARROW) {
                        count++;
                    }
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW) {
                    if (edge2.getProximalEndpoint(node22) != Endpoint.ARROW) {
                        count++;
                    }
                }
            }
        }

//        System.out.println("Arrowpoint errors = " + count);

        return count;
    }

    private static int getNumArrowpts(Graph graph) {
        List<Edge> edges = graph.getEdges();
        int numArrowpts = 0;

        for (Edge edge : edges) {
            if (edge.getEndpoint1() == Endpoint.ARROW) {
                numArrowpts++;
            }
            if (edge.getEndpoint2() == Endpoint.ARROW) {
                numArrowpts++;
            }
        }

//        System.out.println("Num arrowpoints = " + numArrowpts);

        return numArrowpts;
    }

    private Graph getTargetGraph() {
        return new EdgeListGraph(targetGraph);
    }


    public Graph getReferenceGraph() {
        return new EdgeListGraph(referenceGraph);
    }

    //This removes the latent nodes in G and connects nodes that were formerly
    //adjacent to the latent node with an undirected edge (edge type doesnt matter).
    private static Graph removeLatent(Graph g) {
        Graph result = new EdgeListGraph(g);
        result.setGraphConstraintsChecked(false);

        List<Node> allNodes = g.getNodes();
        LinkedList<Node> toBeRemoved = new LinkedList<Node>();

        for (Node curr : allNodes) {
            if (curr.getNodeType() == NodeType.LATENT) {
                List<Node> adj = result.getAdjacentNodes(curr);

                for (int i = 0; i < adj.size(); i++) {
                    Node a = adj.get(i);
                    for (int j = i + 1; j < adj.size(); j++) {
                        Node b = adj.get(j);

                        if (!result.isAdjacentTo(a, b)) {
                            result.addEdge(Edges.undirectedEdge(a, b));
                        }
                    }
                }

                toBeRemoved.add(curr);
            }
        }

        result.removeNodes(toBeRemoved);
        return result;
    }

    /**
     * Returns the number of correct edges last time they were counted.
     */
    private int getAdjCorrect() {
        return adjCorrect;
    }

    /**
     * Returns the number of errors of omission (in the reference workbench but
     * not in the target workbench) the last time they were counted.
     */
    private int getAdjFn() {
        return adjFn;
    }

    private int getAdjFp() {
        return adjFp;
    }

    private int getArrowptCorrect() {
        return arrowptCorrect;
    }

    private int getArrowptFn() {
        return arrowptFn;
    }

    private int getArrowptFp() {
        return arrowptFp;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (params == null) {
            throw new NullPointerException();
        }

        if (targetGraph == null) {
            throw new NullPointerException();
        }

        if (referenceGraph == null) {
            throw new NullPointerException();
        }

        if (getAdjCorrect() < 0) {
            throw new IllegalArgumentException();
        }

        if (getAdjFn() < 0) {
            throw new IllegalArgumentException();
        }

        if (getAdjFp() < 0) {
            throw new IllegalArgumentException();
        }
    }

    public Graph getTrueGraph() {
        return trueGraph;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }
}


