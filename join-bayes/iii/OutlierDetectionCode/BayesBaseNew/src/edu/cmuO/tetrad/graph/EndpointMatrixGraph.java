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

package edu.cmu.tetrad.graph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * <p>Stores a graph (with at most one edge between any pair of nodes and no
 * connections back to self) as a matrix of paired endpoints e[y][x] and e[x][y]
 * for the edge x *-# y, where * = e[y][x] and # = e[x][y]. Permissible
 * endpoints are those in the class Endpoint. The endpoint matrix records the
 * value null where there is no endpoint.</p> <p>Pairing of endpoints is
 * invariant in the graph. That is, e[x][y] == null iff e[y][x] == null. If an
 * endpoint is set where no edge currently exists, the opposite endpoint will be
 * assumed to be Endpoint.TAIL.</p>
 *
 * @author Donald Crimbchin djc2@andrew.cmu.edu
 * @author Shane Harwood harwood@andrew.cmu.edu modifications summer 2000
 * @author Joseph Ramsey modifications 11/00
 * @author Frank Wimberly added existsCycle method 12/03.
 * @author Erin Korber additions summer 2004
 * @version $Revision: 6556 $ $Date: 2005-11-01 14:03:49 -0500 (Tue, 01 Nov
 *          2005) $
 * @see Endpoint
 */
public final class EndpointMatrixGraph implements Graph {
    static final long serialVersionUID = 23L;

    /**
     * The list of nodes in the graph, in the order in which they are
     * represented in <code>endpoints</code>.
     *
     * @serial
     */
    private List<Node> nodes;

    /**
     * Indicates the endpoint at node1 of the edge from 'node1' to 'node2' as
     * endpoints[node1][node2]; the endpoint of the same edge at node2 is
     * endpoints[node2][node1].
     *
     * @serial
     */
    private Endpoint[][] endpoints;

    /**
     * Fires property changes for nodes or edges added or removed.
     *
     * @serial
     */
    private transient PropertyChangeSupport pcs;

    /**
     * True iff graph constraints should be checked. May be set to false and
     * later to true to temporarily disable constraint checking.
     *
     * @serial
     */
    private boolean graphConstraintsChecked = false;

    /**
     * The list of graph constraints that are checked.
     *
     * @serial
     */
    private List<GraphConstraint> graphConstraints =
            new LinkedList<GraphConstraint>();

    /**
     * Set of unfaithful triples. Note the name can't be changed due to
     * serialization.
     */
    private Set<Triple> unfaithfulPairs = new HashSet<Triple>();

    //==============================CONSTRUCTORS==========================//

    /**
     * Constructs an empty graph.
     */
    public EndpointMatrixGraph() {
        this(new LinkedList<Node>());
    }

    /**
     * Constructs an empty graph with the given nodes.
     */
    public EndpointMatrixGraph(List<Node> nodes) {
        checkNodeList(nodes);
        this.nodes = new LinkedList<Node>(nodes);
        endpoints = new Endpoint[nodes.size()][nodes.size()];
    }


    //did not transfer over the constraints.  fixed by ekorber, 2004/06/09
    public EndpointMatrixGraph(Graph graph) {
        this();

        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        this.graphConstraints = graph.getGraphConstraints();

        transferNodesAndEdges(graph);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static EndpointMatrixGraph serializableInstance() {
        return new EndpointMatrixGraph();
    }

    //=============================PUBLIC METHODS=========================//

    /**
     * Transfers nodes and edges from one graph to another.  One way this is
     * used is to change graph types.  One constructs a new graph based on the
     * old graph, and this method is called to transfer the nodes and edges of
     * the old graph to the new graph.
     *
     * @param graph the graph from which nodes and edges are to be pilfered.
     * @throws IllegalArgumentException This exception is thrown if adding some
     *                                  node or edge violates one of the
     *                                  basicConstraints of this graph.
     */
    @Override
	public final void transferNodesAndEdges(Graph graph)
            throws IllegalArgumentException {
        for (Node node : graph.getNodes()) {
            if (!addNode(node)) {
                throw new IllegalArgumentException();
            }
        }

        for (Edge edge : graph.getEdges()) {
            if (!addEdge(edge)) {
                throw new IllegalArgumentException(edge + " not added.");
            }
        }
    }

    @Override
	public void setAmbiguous(Triple triple) {
        this.unfaithfulPairs.add(triple);
    }

    @Override
	public boolean isAmbiguous(Node x, Node y, Node z) {
        return unfaithfulPairs.contains(new Triple(x, y, z));
    }

    @Override
	public Set<Triple> getAmbiguousTriples() {
        return new HashSet<Triple>(unfaithfulPairs);
    }

    /**
     * Resets the graph so that it is fully connected it using #-# edges, where
     * # is the given endpoint.
     */
    @Override
	public void fullyConnect(Endpoint endpoint) {
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) {
                    endpoints[i][j] = null;
                }
                else {
                    endpoints[i][j] = endpoint;
                }
            }
        }
    }

    /**
     * Changes all endpoints in the graph to the given endpoint.
     */
    @Override
	public void reorientAllWith(Endpoint endpoint) {
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (!(endpoints[i][j] == null)) {
                    endpoints[i][j] = endpoint;
                }
            }
        }
    }

    /**
     * Returns a copy of the endpoint matrix.
     */
    @Override
	public Endpoint[][] getEndpointMatrix() {
        Endpoint[][] copy = new Endpoint[nodes.size()][nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            System.arraycopy(endpoints[i], 0, copy[i], 0, nodes.size());
        }

        return copy;
    }

    /**
     * Returns the list of nodes adjacent to the given node.
     */
    @Override
	public List<Node> getAdjacentNodes(Node node) {
        int index = this.nodes.indexOf(node);
        List<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (endpoints[i][index] != null && endpoints[index][i] != null) {
                nodes.add(this.nodes.get(i));
            }
        }

        return nodes;
    }

    /**
     * Returns the list of nodes x such that the (unique) edge from x to node
     * has the given proximal endpoint. For example, if the endpoint is
     * Endpoint.FISHER_Z, returns the list of nodes that are *-> the given node,
     * where * is any endpoint.
     */
    @Override
	public List<Node> getNodesInTo(Node node, Endpoint endpoint) {
        int index = this.nodes.indexOf(node);
        List<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (endpoints[index][i] != null && endpoints[i][index] == endpoint)
            {
                nodes.add(this.nodes.get(i));
            }
        }

        return nodes;
    }

    /**
     * Returns the list of nodes x such that the (unique) edge from x to node
     * has the given distal endpoint. For example, if the endpoint is
     * Endpoint.FISHER_Z, returns the list of nodes that are <-* the given node,
     * where * is any endpoint.
     */
    @Override
	public List<Node> getNodesOutTo(Node node, Endpoint n) {
        int index = this.nodes.indexOf(node);
        List<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (endpoints[i][index] != null && endpoints[index][i] == n) {
                nodes.add(this.nodes.get(i));
            }
        }

        return nodes;
    }

    /**
     * Returns the list of nodes.
     */
    @Override
	public List<Node> getNodes() {
        return new ArrayList<Node>(nodes);
    }

    /**
     * Removes the edge connecting the two given nodes.
     */
    @Override
	public boolean removeEdge(Node node1, Node node2) {
        int index1 = nodes.indexOf(node1);
        int index2 = nodes.indexOf(node2);

        if (index1 == index2) {
            return false;
        }

        Endpoint endpoint1 = endpoints[index2][index1];
        Endpoint endpoint2 = endpoints[index1][index2];

        if (endpoint1 != null && endpoint2 != null) {
            endpoints[index2][index1] = null;
            endpoints[index1][index2] = null;

            Edge edge = new Edge(node1, node2, endpoint1, endpoint2);
            getPcs().firePropertyChange("edgeRemoved", edge, null);

            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Removes the edge connecting node1 to node2, if one exists. (This graph
     * does not support multiple edges per node pair.)
     */
    @Override
	public boolean removeEdges(Node node1, Node node2) {
        return removeEdge(node1, node2);
    }

    /**
     * Returns true iff 'nodeX' and 'nodeY' are adjacent.
     */
    @Override
	public boolean isAdjacentTo(Node nodeX, Node nodeY) {
        return endpoints[nodes.indexOf(nodeX)][nodes.indexOf(nodeY)] != null &&
                endpoints[nodes.indexOf(nodeX)][nodes.indexOf(nodeY)] != null;
    }

    /**
     * If there is currently an edge from from to to, sets the endpoint at to to
     * the given endpoint; if there is no such edge, adds an edge from --# to
     * where # is the given endpoint. Setting an endpoint to null removes the
     * edge.
     */
    @Override
	public boolean setEndpoint(Node from, Node to, Endpoint endpoint) {

        // Replaced the low-level code because it didn't check graph
        // constraints, allowing inconsistent graphs to be constucted.
        // Maybe we just need a very low-level matrix graph implementation
        // that can be converted to a heavyweight graph at the end.
        Edge currentEdge = getEdge(from, to);

        if (currentEdge == null) {
            Edge newEdge = new Edge(from, to, Endpoint.TAIL, endpoint);
            addEdge(newEdge);
            return true;
        }
        else {
            Edge edge = getEdge(from, to);
            Edge newEdge = new Edge(from, to, edge.getProximalEndpoint(from),
                    endpoint);
            removeEdge(currentEdge);

            try {
                addEdge(newEdge);
                return true;
            }
            catch (IllegalArgumentException e) {
                addEdge(currentEdge);
                return false;
            }
        }

        //        int index1 = nodes.indexOf(from);
        //        int index2 = nodes.indexOf(to);
        //
        //        if (endpoints[index1][index2] != null &&
        //                endpoints[index2][index1] != null) {
        //            Edge edge = new Edge(from, to, endpoints[index2][index1],
        //                    endpoints[index1][index2]);
        //            getPcs().firePropertyChange("edgeRemoved", edge, null);
        //        }
        //
        //        endpoints[index1][index2] = endpoint;
        //
        //        if (endpoint == null) {
        //            endpoints[index1][index2] = null;
        //            endpoints[index2][index1] = null;
        //        }
        //        else {
        //            if (endpoints[index2][index1] == null) {
        //                endpoints[index2][index1] = Endpoint.TAIL;
        //            }
        //
        //            Edge edge = new Edge(from, to, endpoints[index2][index1],
        //                    endpoints[index1][index2]);
        //            getPcs().firePropertyChange("edgeAdded", null, edge);
        //        }
    }

    /**
     * Gets the endpoint type along the edge from 'node1' to 'node2', at the
     * 'node2' end (NONE, TAIL, ARROW, CIRCLE).
     */
    @Override
	public Endpoint getEndpoint(Node node1, Node node2) {
        return endpoints[nodes.indexOf(node1)][nodes.indexOf(node2)];
    }

    /**
     * Returns true iff the given object is a graph that is equal to this graph,
     * in the sense that it contains the same nodes and the edges are
     * isomorphic.
     */
    @Override
	public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        Graph graph = (Graph) o;

        // Make sure nodes are namewise isomorphic.
        List<Node> tNodes = getNodes();
        List<Node> oNodes = graph.getNodes();

        for (Iterator<Node> it = tNodes.iterator(); it.hasNext();) {
            Node thisNode = (it.next());

            for (Iterator<Node> it2 = oNodes.iterator(); it2.hasNext();) {
                Node otherNode = (it2.next());

                if (thisNode.getName().equals(otherNode.getName())) {
                    it.remove();
                    it2.remove();
                    break;
                }
            }
        }

        if (!(tNodes.isEmpty() && oNodes.isEmpty())) {
            return false;
        }

        // Make sure edges are isomorphic.
        List<Edge> tEdges = getEdges();
        List<Edge> oEdges = graph.getEdges();

        for (Iterator<Edge> it = tEdges.iterator(); it.hasNext();) {
            Edge thisEdge = (it.next());

            for (Iterator<Edge> it2 = oEdges.iterator(); it2.hasNext();) {
                Edge otherEdge = (it2.next());

                if (thisEdge.equals(otherEdge)) {
                    it.remove();
                    it2.remove();
                    break;
                }
            }
        }

        // If either resulting list is nonempty, the edges of the two graphs
        // are not isomorphic.
        if (!(tEdges.isEmpty() && oEdges.isEmpty())) {
            return false;
        }

        // If all tests pass, then return true.
        return true;
    }

    /**
     * Constructs and returns a subgraph consisting of a given subset of the
     * nodes of this graph together with the edges between them.
     */
    @Override
	public Graph subgraph(List<Node> nodes) {
        checkNodeList(nodes);

        for (Node node : nodes) {
            if (!this.nodes.contains(node)) {
                throw new IllegalArgumentException(
                        "Nodes of subgraph must be nodes" +
                                " of the orignal graph.");
            }
        }

        Graph newGraph = new EndpointMatrixGraph(nodes);

        for (Node node1 : nodes) {
            for (Node node2 : nodes) {
                if (node2 == node1) {
                    continue;
                }

                Endpoint endpoint1 = getEndpoint(node1, node2);
                Endpoint endpoint2 = getEndpoint(node2, node1);

                if (endpoint1 == null || endpoint2 == null) {
                    continue;
                }

                newGraph.setEndpoint(node1, node2, endpoint1);
                newGraph.setEndpoint(node2, node1, endpoint2);
            }
        }

        return newGraph;
    }

    /**
     * Returns true iff there is a directed path from node1 to node2.
     */
    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        LinkedList<Node> path = new LinkedList<Node>();
        return existsDirectedPathVisit(node1, node2, path);
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return existsUndirectedPathVisit(node1, node2, new LinkedList<Node>());
    }

    /**
     * Returns true iff there is a semidirected path from node1 to node2.
     */
    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes2) {
        return existsSemiDirectedPathVisit(node1, nodes2,
                new LinkedList<Node>());
    }

    /**
     * Adds a directed edge to the graph from node A to node B.
     */
    @Override
	public boolean addDirectedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.directedEdge(nodeA, nodeB));
    }

    /**
     * Adds an undirected edge to the graph from node A to node B.
     */
    @Override
	public boolean addUndirectedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.undirectedEdge(nodeA, nodeB));
    }

    /**
     * Adds an undirected edge to the graph from node A to node B.
     */
    @Override
	public boolean addNondirectedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.nondirectedEdge(nodeA, nodeB));
    }

    /**
     * Adds a hald-directed edge to the graph from node A to node B.
     */
    @Override
	public boolean addPartiallyOrientedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.partiallyOrientedEdge(nodeA, nodeB));
    }

    /**
     * Adds a bidirected edge to the graph from node A to node B.
     */
    @Override
	public boolean addBidirectedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.bidirectedEdge(nodeA, nodeB));
    }

    /**
     * Adds an edge to the graph in accordance with the graph constraints list.
     * Precondition: The nodes which the edge connects must already be in the
     * graph.
     *
     * @param edge the edge to be added.
     * @return true if the edge was added, false if not.
     */
    @Override
	public boolean addEdge(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();

        if (node1 == node2) {
            throw new IllegalArgumentException(
                    "This graph does not support " + "edges to self: " + edge);
        }

        if (getEndpoint(node2, node1) != null ||
                getEndpoint(node1, node2) != null) {
            throw new IllegalArgumentException("An edge already exists there.");
        }

        if (isGraphConstraintsChecked() && !checkAddEdge(edge)) {
            return false;
        }

        int index1 = nodes.indexOf(node1);
        int index2 = nodes.indexOf(node2);

        endpoints[index2][index1] = edge.getEndpoint1();
        endpoints[index1][index2] = edge.getEndpoint2();

        getPcs().firePropertyChange("edgeAdded", null, edge);

        return true;
    }

    /**
     * Adds a node to the graph. Precondition: The proposed name of the node
     * cannot already be used by any other node in the same graph.
     *
     * @param node the node to be added.
     * @return true if nodes were added, false if not.
     */
    @Override
	public boolean addNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }

        if (!(getNode(node.getName()) == null)) {
            throw new IllegalArgumentException("A node by name " +
                    node.getName() + " has already been added to the graph.");
        }

        if (isGraphConstraintsChecked() && !checkAddNode(node)) {
            return false;
        }

        this.nodes.add(node);

        Endpoint[][] endpointsCopy = new Endpoint[nodes.size()][nodes.size()];
        for (int i = 0; i < nodes.size() - 1; i++) {
            System.arraycopy(this.endpoints[i], 0, endpointsCopy[i], 0,
                    nodes.size() - 1);
        }

        this.endpoints = endpointsCopy;
        getPcs().firePropertyChange("nodeAdded", null, node);

        return true;
    }

    /**
     * Adds a PropertyChangeListener to the graph.
     */
    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        getPcs().addPropertyChangeListener(l);
    }

    /**
     * Determines whether this graph contains the given edge.
     *
     * @param edge the edge to check.
     * @return true iff the graph contain 'edge'.
     */
    @Override
	public boolean containsEdge(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        Endpoint endpoint1 = edge.getEndpoint1();
        Endpoint endpoint2 = edge.getEndpoint2();

        return getEndpoint(node1, node2) == endpoint2 &&
                getEndpoint(node2, node1) == endpoint1;
    }

    /**
     * Determines whether this graph contains the given node.
     *
     * @param node
     * @return true iff the graph contains 'node'.
     */
    @Override
	public boolean containsNode(Node node) {
        return this.nodes.contains(node);
    }

    /**
     * Returns the list of edges in the graph.  No particular ordering of the
     * edges in the list is guaranteed.
     */
    @Override
	public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<Edge>();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(j);
                Endpoint endpt1 = getEndpoint(node2, node1);
                Endpoint endpt2 = getEndpoint(node1, node2);

                if (endpt1 == null || endpt2 == null) {
                    continue;
                }

                edges.add(new Edge(node1, node2, endpt1, endpt2));
            }
        }

        return edges;
    }

    /**
     * Returns the list of edges connected to a particular node. No particular
     * ordering of the edges in the list is guaranteed.
     */
    @Override
	public List<Edge> getEdges(Node node) {
        List<Edge> edges = new ArrayList<Edge>();

        for (Node node1 : nodes) {
            if (node == node1) {
                continue;
            }

            Endpoint endpt1 = getEndpoint(node1, node);
            Endpoint endpt2 = getEndpoint(node, node1);

            if (endpt1 == null || endpt2 == null) {
                continue;
            }

            edges.add(new Edge(node, node1, endpt1, endpt2));
        }

        return edges;
    }

    /**
     * Returns a singleton list containing the edge between node1 and node2, if
     * one exists. (This graph does not support multiple edges between node
     * pairs.)
     */
    @Override
	public List<Edge> getEdges(Node node1, Node node2) {
        Edge edge = getEdge(node1, node2);

        if (edge == null) {
            return new LinkedList<Edge>();
        }
        else {
            return Collections.singletonList(edge);
        }
    }

    /**
     * Returns the node with the given string name.  In case of accidental
     * duplicates, the first node encountered with the given name is returned.
     * In case no node exists with the given name, null is returned.
     *
     * @param name the name of the node
     * @return the node
     */
    @Override
	public Node getNode(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        for (Node node1 : nodes) {
            if (name.equals(node1.getName())) {
                return node1;
            }
        }

        return null;
    }

    /**
     * Returns the number of edges in the (entire) graph.
     */
    @Override
	public int getNumEdges() {
        int numEdges = 0;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                if (endpoints[i][j] != null && endpoints[j][i] != null) {
                    numEdges++;
                }
            }
        }

        return numEdges;
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return this number.
     */
    @Override
	public int getNumNodes() {
        return nodes.size();
    }

    /**
     * Returns the number of edges in the graph which are connected to a
     * particular node.
     *
     * @param node the node in question
     * @return the number of edges connected to that node.
     */
    @Override
	public int getNumEdges(Node node) {
        int i = nodes.indexOf(node);
        int numEdges = 0;

        for (int j = 0; j < nodes.size(); j++) {
            if (i != j && endpoints[i][j] != null) {
                numEdges++;
            }
        }

        return numEdges;
    }

    /**
     * Returns the list of graph constraints for this graph.
     */
    @Override
	public List<GraphConstraint> getGraphConstraints() {
        return new LinkedList<GraphConstraint>(this.graphConstraints);
    }

    /**
     * Returns true iff graph constraints will be checked for future graph
     * modifications.
     */
    @Override
	public boolean isGraphConstraintsChecked() {
        return this.graphConstraintsChecked;
    }

    /**
     * True iff graph constraints will be checked for future graph
     * modifications.
     */
    @Override
	public void setGraphConstraintsChecked(boolean checked) {
        this.graphConstraintsChecked = checked;
    }

    /**
     * Removes the given edge from the graph.
     *
     * @param edge the edge to be removed.
     * @return true if the edge was removed, false if not.
     */
    @Override
	public boolean removeEdge(Edge edge) {
        Edge _edge = getEdge(edge.getNode1(), edge.getNode2());

        if (edge.equals(_edge) && checkRemoveEdge(edge)) {
            removeEdge(edge.getNode1(), edge.getNode2());
            getPcs().firePropertyChange("edgeRemoved", edge, null);
            return true;
        }

        return false;
    }

    /**
     * Iterates through the list and removes any permissible edges found.  The
     * order in which edges are added is the order in which they are presented
     * in the iterator.
     *
     * @param edges the list of edges.
     * @return true if edges were added, false if not.
     */
    @Override
	public boolean removeEdges(List<Edge> edges) {
        boolean removedSomething = false;

        for (Edge edge : edges) {
            removedSomething = removeEdge(edge) || removedSomething;
        }

        return removedSomething;
    }

    /**
     * Removes a node from the graph.
     *
     * @return true if the node was removed, false if not.
     */
    @Override
	public boolean removeNode(Node node) {
        if (nodes == null) {
            throw new NullPointerException();
        }

        if (!(nodes.contains(node))) {
            return false;
        }

        if (nodes.contains(node) && !checkRemoveNode(node)) {
            return false;
        }

        for (Node node1 : nodes) {
            removeEdge(node, node1);
        }

        List<Node> newNodes = new LinkedList<Node>(nodes);
        newNodes.remove(node);

        Endpoint[][] copy = new Endpoint[newNodes.size()][newNodes.size()];

        for (int i = 0; i < newNodes.size(); i++) {
            for (int j = 0; j < newNodes.size(); j++) {
                int oldi = nodes.indexOf(newNodes.get(i));
                int oldj = nodes.indexOf(newNodes.get(j));
                copy[i][j] = endpoints[oldi][oldj];
            }
        }

        this.nodes = newNodes;
        this.endpoints = copy;

        getPcs().firePropertyChange("nodeRemoved", node, null);

        return true;
    }

    /**
     * Removes all nodes (and therefore all edges) from the graph.
     */
    @Override
	public void clear() {
        this.nodes = new LinkedList<Node>();
        this.endpoints = new Endpoint[0][0];
    }

    /**
     * Iterates through the list and removes any permissible nodes found.  The
     * order in which nodes are removed is the order in which they are presented
     * in the iterator.
     *
     * @param nodes the list of nodes.
     * @return true if nodes were added, false if not.
     */
    @Override
	public boolean removeNodes(List<Node> nodes) {
        if (nodes == null) {
            throw new NullPointerException();
        }

        for (Node node : nodes) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        boolean removedSomething = false;

        for (Node node1 : nodes) {
            removedSomething = removedSomething || removeNode(node1);
        }

        return removedSomething;
    }

    @Override
	public boolean existsDirectedCycle() {
        for (Node node1 : getNodes()) {
            if (existsDirectedPathFromTo(node1, node1)) {
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean isDirectedFromTo(Node node1, Node node2) {
        return getEndpoint(node2, node1) == Endpoint.TAIL &&
                getEndpoint(node1, node2) == Endpoint.ARROW;
    }

    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2) {
        return getEndpoint(node2, node1) == Endpoint.TAIL &&
                getEndpoint(node1, node2) == Endpoint.TAIL;
    }

    /**
     * added by ekorber, 2004/06/11
     *
     * @return true if the given edge is definitely visible (Jiji, pg 25)
     * @throws IllegalArgumentException if the given edge is not a directed edge
     *                                  in the graph
     */
    @Override
	public boolean defVisible(Edge edge) {
        if (this.containsEdge(edge)) {

            Node A = Edges.getDirectedEdgeTail(edge);
            Node B = Edges.getDirectedEdgeHead(edge);
            List<Node> adjToA = getAdjacentNodes(A);

            while (!adjToA.isEmpty()) {
                Node Curr = adjToA.remove(0);
                if (!((getAdjacentNodes(Curr)).contains(B)) &&
                        ((getEdge(Curr, A)).getProximalEndpoint(A) == Endpoint
                                .ARROW)) {
                    return true;
                }
            }
            return false;
        }
        else {
            throw new IllegalArgumentException(
                    "Given edge is not in the graph.");
        }
    }


    /**
     * added by ekorber, 2004/6/9
     */
    @Override
	public boolean isDefiniteNoncollider(Node node1, Node node2, Node node3) {
        if (isDirectedFromTo(node2, node1) || isDirectedFromTo(node2, node3)) {
            return true;
        }
        else if (!isAdjacentTo(node1, node3)) {
            return !(isDirectedFromTo(node1, node2) &&
                    !(isDirectedFromTo(node3, node2)));
        }
        else {
            return false;
        }
    }

    /**
     * added by ekorber, 2004/6/9
     */
    @Override
	public boolean isDefiniteCollider(Node node1, Node node2, Node node3) {
        //return ((isDirectedFromTo(node1, node 2) && isDirectedFromTo(node3, node2)));
        return ((getEndpoint(node1, node2) == Endpoint.ARROW) &&
                (getEndpoint(node3, node2) == Endpoint.ARROW));
    }

    /**
     * Determines whether a trek exists between two nodes in the graph.  A trek
     * exists if there is a directed path between the two nodes or else, for
     * some third node in the graph, there is a path to each of the two nodes in
     * question.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return true if a trek exists between the two nodes, false if not.
     */
    @Override
	public boolean existsTrek(Node node1, Node node2) {
        for (Node node3 : getNodes()) {
            if (isAncestorOf(node3, node1) && isAncestorOf(node3, node2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the Collection of children for a node.
     *
     * @param node the parent node
     * @return the Collection of children for 'node'.
     */
    @Override
	public List<Node> getChildren(Node node) {
        List<Node> children = new LinkedList<Node>();

        for (Edge edge1 : getEdges(node)) {
            Node sub = Edges.traverseDirected(node, edge1);

            if (sub != null) {
                children.add(sub);
            }
        }

        return children;
    }

    @Override
	public int getConnectivity() {
        int connectivity = 0;

        List<Node> nodes = getNodes();

        for (Object node1 : nodes) {
            Node node = (Node) node1;
            int n = getNumEdges(node);
            if (n > connectivity) {
                connectivity = n;
            }
        }

        return connectivity;
    }

    @Override
	public List<Node> getDescendants(List<Node> nodes) {
        HashSet<Node> descendants = new HashSet<Node>();

        for (Node node : nodes) {
            collectDescendantsVisit(node, descendants);
        }

        return new LinkedList<Node>(descendants);

    }

    /**
     * Get the edge connecting node 1 to node 2.
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @return the edge connecting 1 to 2.
     */
    @Override
	public Edge getEdge(Node node1, Node node2) {
        Endpoint endpt1 = getEndpoint(node2, node1);
        Endpoint endpt2 = getEndpoint(node1, node2);

        if (endpt1 == null || endpt2 == null) {
            return null;
        }

        return new Edge(node1, node2, endpt1, endpt2);
    }

    @Override
	public Edge getDirectedEdge(Node node1, Node node2) {
        List<Edge> edges = getEdges(node1, node2);

        if (edges.size() == 0) {
            return null;
        }

        for (Edge edge : edges) {
            if (Edges.isDirectedEdge(edge) && edge.getProximalEndpoint(node2) == Endpoint.ARROW) {
                return edge;
            }
        }

        return null;
    }

    /**
     * Returns the list of parents for a node.
     *
     * @param node the node whose parents are requested.
     * @return the parents of that node as a collection.
     */
    @Override
	public List<Node> getParents(Node node) {
        List<Node> parents = new LinkedList<Node>();

        for (Edge edge1 : getEdges(node)) {
            Node sub = Edges.traverseReverseDirected(node, edge1);

            if (sub != null) {
                parents.add(sub);
            }
        }

        return parents;
    }

    /**
     * Returns the number of arrow endpoint adjacent to an edge.
     *
     * @param node the node in question
     * @return the in-degree of the node.
     * @see #getOutdegree
     */
    @Override
	public int getIndegree(Node node) {
        return getParents(node).size();
    }

    /**
     * Returns the number of null endpoints adjacent to an edge.
     *
     * @param node the node in question
     * @return the out-degree of the node.
     */
    @Override
	public int getOutdegree(Node node) {
        return getChildren(node).size();
    }

    /**
     * Determines whether one node is an ancestor of another.
     *
     * @param node1 the first node
     * @param node2 the second node.
     * @return true if node is an a (non-proper) ancestor of node2, false if
     *         not.
     */
    @Override
	public boolean isAncestorOf(Node node1, Node node2) {
        return (node1 == node2) || existsDirectedPathFromTo(node1, node2);
    }

    /**
     * added by ekorber, 2004/06/12
     *
     * @return true iff node1 is a possible ancestor of node2
     */
    @Override
	public boolean possibleAncestor(Node node1, Node node2) {
        return existsSemiDirectedPathFromTo(node1,
                Collections.singleton(node2));
    }

    /**
     * added by ekorber, 2004/06/15
     *
     * @return true iff node1 is a possible ancestor of at least one member of
     *         nodes2
     */
    public boolean possibleAncestorSet(Node node1, List<Node> nodes2) {
        for (Node aNodes2 : nodes2) {
            if (possibleAncestor(node1, aNodes2)) {
                return true;
            }
        }
        return false;
    }

    @Override
	public List<Node> getAncestors(List<Node> nodes) {
        HashSet<Node> ancestors = new HashSet<Node>();

        for (Node node : nodes) {
            collectAncestorsVisit(node, ancestors);
        }

        return new LinkedList<Node>(ancestors);
    }

    /**
     * Determines whether one node is a child of another.
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @return true if node1 is a child of node2, false if not.
     */
    @Override
	public boolean isChildOf(Node node1, Node node2) {
        Endpoint endpoint1 = getEndpoint(node2, node1);
        Endpoint endpoint2 = getEndpoint(node1, node2);
        return endpoint1 == Endpoint.ARROW && endpoint2 == Endpoint.TAIL;
    }

    /**
     * Determines whether one node is a descendent of another.
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @return true if node1 is a (non-proper) descendant of node2, false if
     *         not.
     */
    @Override
	public boolean isDescendentOf(Node node1, Node node2) {
        return (node1 == node2) || existsDirectedPathFromTo(node1, node2);
    }

    /**
     * added by ekorber, 2004/06/12
     *
     * @return true iff node2 is a definite nondecendent of node1
     */
    @Override
	public boolean defNonDescendent(Node node1, Node node2) {
        return !(possibleAncestor(node1, node2));
    }

    /**
     * Determines whether node1 is d-connected to node2, given a list of
     * conditioning nodes. According to Spirtes, Richardson & Meek, node1 is
     * d-connected to node2 given some conditioning set Z if there is an acyclic
     * undirected path U between node1 and node2, such that every collider on U
     * is an ancestor of some element in Z and every non-collider on U is not in
     * Z. Two elements are d-connected just in case they are not d-separated. A
     * collider is a node which two edges hold in common for which the endpoints
     * leading into the node are both arrow endpoints.
     *
     * @param node1             the first node.
     * @param node2             the second node.
     * @param conditioningNodes the set of conditioning nodes.
     * @return true if node1 is d-connected to node2 given set
     *         conditioningNodes, false if not.
     * @see #isDSeparatedFrom
     */
    @Override
	public boolean isDConnectedTo(Node node1, Node node2,
            List<Node> conditioningNodes) {

        // Set up a linked list to hold nodes along the current path (to check
        // for cycles).
        LinkedList<Node> path = new LinkedList<Node>();

        // Fine the closure of conditioningNodes under the parent relation.
        Set<Node> conditioningNodesClosure = new HashSet<Node>();

        for (Node conditioningNode : conditioningNodes) {
            doClosureVisit((conditioningNode), conditioningNodesClosure);
        }

        // Calls the recursive method to discover a d-connecting path from node1
        // to node2, if one exists.  If such a path is found, true is returned;
        // otherwise, false is returned.
        Endpoint incomingEndpoint = null;
        return isDConnectedToVisit(node1, incomingEndpoint, node2, path,
                conditioningNodes, conditioningNodesClosure);
    }

    /**
     * Determines whether one node is d-separated from another. According to
     * Spirtes, Richardson & Meek, two nodes are d- connected given some
     * conditioning set Z if there is an acyclic undirected path U between them,
     * such that every collider on U is an ancestor of some element in Z and
     * every non-collider on U is not in Z.  Two elements are d-separated just
     * in case they are not d-connected.  A collider is a node which two edges
     * hold in common for which the endpoints leading into the node are both
     * arrow endpoints.
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @param z     the conditioning set.
     * @return true if node1 is d-separated from node2 given set z, false if
     *         not.
     * @see #isDConnectedTo
     */
    @Override
	public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return !isDConnectedTo(node1, node2, z);
    }

    /**
     * Uses linear-time algorithm from Geiger/Verma/Pearl 1990. Added by
     * ekorber, 2004/06/15. Adapted from edu.cmu.tetrad.search.PossibleDSepSearch.
     */
    @Override
	public boolean possDConnectedTo(Node node1, Node node2,
            List<Node> condNodes) {
        // The way this is implemented would be good for getting all the nodes that
        // node1 is possibly d-connected to.  Kind of stupid for just a boolean method.
        // Is there a more optimal way? (some better ordering of nodes?)

        LinkedList<Node> allNodes = new LinkedList<Node>(this.getNodes());
        int sz = allNodes.size();
        int[][] edgeStage = new int[sz][sz];
        int stage = 1;

        int n1x = allNodes.indexOf(node1);
        int n2x = allNodes.indexOf(node2);

        edgeStage[n1x][n1x] = 1;
        edgeStage[n2x][n2x] = 1;

        List<int[]> currEdges;
        List<int[]> nextEdges = new LinkedList<int[]>();

        int[] temp1 = new int[2];
        temp1[0] = n1x;
        temp1[1] = n1x;
        nextEdges.add(temp1);

        int[] temp2 = new int[2];
        temp2[0] = n2x;
        temp2[1] = n2x;
        nextEdges.add(temp2);

        while (true) {
            currEdges = nextEdges;
            nextEdges = new LinkedList<int[]>();
            for (int[] edge : currEdges) {
                Node center = allNodes.get(edge[1]);
                LinkedList<Node> adj =
                        new LinkedList<Node>(getAdjacentNodes(center));

                for (Node anAdj : adj) {
                    // check if we've hit this edge before
                    int testIndex = allNodes.indexOf(anAdj);
                    if (edgeStage[edge[1]][testIndex] != 0) {
                        continue;
                    }

                    // if the edge pair violates possible d-connection,
                    // then go to the next adjacent node.

                    Node X = allNodes.get(edge[0]);
                    Node Y = allNodes.get(edge[1]);
                    Node Z = allNodes.get(testIndex);

                    if (!((isDefiniteNoncollider(X, Y, Z) &&
                            !(condNodes.contains(Y))) || (
                            isDefiniteCollider(X, Y, Z) &&
                                    possibleAncestorSet(Y, condNodes)))) {
                        continue;
                    }

                    // if it gets here, then it's legal, so:
                    // (i) if this is the one we want, we're done
                    if (anAdj.equals(node2)) {
                        return true;
                    }

                    // (ii) if we need to keep going,
                    // add the edge to the nextEdges list
                    int[] nextEdge = new int[2];
                    nextEdge[0] = edge[1];
                    nextEdge[1] = testIndex;
                    nextEdges.add(nextEdge);
                    // (iii) set the edgeStage array
                    edgeStage[edge[1]][testIndex] = stage;
                    edgeStage[testIndex][edge[1]] = stage;
                }
            }

            // find out if there's any reason to move to the next stage
            if (nextEdges.size() == 0) {
                break;
            }

            stage++;
        }

        return false;
    }

    /**
     * Determines whether an inducing path exists between node1 and node2, given
     * a set O of observed nodes and a set sem of conditioned nodes.
     *
     * @param node1             the first node.
     * @param node2             the second node.
     * @param observedNodes     the set of observed nodes.
     * @param conditioningNodes the set of nodes conditioned upon.
     * @return true if an inducing path exists, false if not.
     */
    @Override
	public boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes) {
        Set<Node> sPlus = new HashSet<Node>(conditioningNodes);
        Set<Node> pathNodes = new HashSet<Node>();

        sPlus.add(node1);
        sPlus.add(node2);

        Set<Node> sClosure = new HashSet<Node>();

        for (Node s : sPlus) {
            Node node = (s);
            doClosureVisit(node, sClosure);
        }

        return existsInducingPathVisit(node1, node2, null, pathNodes,
                observedNodes, conditioningNodes, sClosure);
    }

    /**
     * Determines whether one node is a parent of another.
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @return true if node1 is a parent of node2, false if not.
     * @see #isChildOf
     * @see #getParents
     * @see #getChildren
     */
    @Override
	public boolean isParentOf(Node node1, Node node2) {
        Endpoint endpoint1 = getEndpoint(node2, node1);
        Endpoint endpoint2 = getEndpoint(node1, node2);
        return endpoint1 == Endpoint.TAIL && endpoint2 == Endpoint.ARROW;
    }

    /**
     * Determines whether one node is a proper ancestor of another.
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @return return true if node1 is a proper ancestor of node2, false if
     *         not.
     * @see #isAncestorOf
     */
    @Override
	public boolean isProperAncestorOf(Node node1, Node node2) {
        return existsDirectedPathFromTo(node1, node2);
    }

    /**
     * Determines whether one node is a proper decendent of another
     *
     * @param node1 the first node.
     * @param node2 the second node.
     * @return true if node1 is a proper descendent of node2, false if not.
     * @see #isDescendentOf
     */
    @Override
	public boolean isProperDescendentOf(Node node1, Node node2) {
        return existsDirectedPathFromTo(node2, node1);
    }

    /**
     * Determines whether a node in a graph is exogenous.
     *
     * @param node the node in question.
     * @return true if the node is exogenous in the graph, false if not.
     */
    @Override
	public boolean isExogenous(Node node) {
        return getIndegree(node) == 0;
    }


    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();

//        buf.append("\nNodes: ");
//
//        for (int i = 0; i < nodes.size(); i++) {
//            buf.append("\n" + (i + 1) + ". " + nodes.get(i));
//        }

        buf.append("\nEdges: ");
        List<Edge> edges = getEdges();

        for (int i = 0; i < edges.size(); i++) {
            buf.append("\n").append(i + 1).append(". ").append(edges.get(i));
        }

        buf.append("\n");

        return buf.toString();

//        StringBuffer buf = new StringBuffer();
//
//        buf.append("\nEndpointMatrixGraph.");
//
//        if (nodes.size() == 0) {
//            buf.append("\n\t--No nodes--");
//        }
//        else {
//            buf.append("\n\n\tVariables:");
//
//            for (int i = 0; i < nodes.size(); i++) {
//                Node node = (Node) nodes.get(i);
//                buf.append("\n\t\t" + node);
//
//                if (node.getNodeType() == NodeType.LATENT) {
//                    buf.append("(Latent)");
//                }
//            }
//
//            buf.append("\n\n\tEndpoint matrix:");
//            buf.append("\n");
//
//            for (int i = 0; i < nodes.size(); i++) {
//                buf.append("\n\t\t");
//
//                for (int j = 0; j < nodes.size(); j++) {
//                    buf.append(endpoints[i][j] + "\t");
//                }
//            }
//
//            buf.append("\n\n\tEdges:");
//            List edges = getEdges();
//
//            for (int i = 0; i < edges.size(); i++) {
//                buf.append("\n\t\t" + edges.get(i));
//            }
//
//            buf.append("\n\n\tAdjacencies:");
//
//            for (int i = 0; i < nodes.size(); i++) {
//                Node node = (Node) getNodes().get(i);
//                List adjNodes = new ArrayList(getAdjacentNodes(node));
//                buf.append("\n\t\t" + node + ": " + adjNodes);
//            }
//
//            buf.append("\n");
//        }
//
//        return buf.toString();
    }

    /**
     * Adds a graph constraint.
     *
     * @param gc the graph constraint.
     * @return true if the constraint was added, false if not.
     */
    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        if (!graphConstraints.contains(gc)) {
            graphConstraints.add(gc);
            return true;
        }
        else {
            return false;
        }
    }

    //=============================PRIVATE METHODS========================//

    /**
     * Returns true iff there is a directed path from node1 to node2.
     */
    private boolean existsUndirectedPathVisit(Node node1, Node node2,
                                            LinkedList<Node> path) {
        path.addLast(node1);

        for (Edge edge : getEdges(node1)) {
            Node child = Edges.traverse(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (existsUndirectedPathVisit(child, node2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    /**
     * Returns true iff there is a directed path from node1 to node2. The
     * current path is maintained to avoid getting caught in cycles.
     */

    //Also depth first... breadth-first maybe better if memory is available.
    private boolean existsDirectedPathVisit(Node node1, Node node2,
            LinkedList<Node> path) {
        path.addLast(node1);

        for (Object o : getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, (Edge) (o));

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (existsDirectedPathVisit(child, node2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }


    private boolean existsSemiDirectedPathVisit(Node node1, Set<Node> nodes2,
            LinkedList<Node> path) {
        path.addLast(node1);

        for (Object o : getEdges(node1)) {
            Node child = Edges.traverseSemiDirected(node1, (Edge) (o));

            if (child == null) {
                continue;
            }

            if (nodes2.contains(child)) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (existsSemiDirectedPathVisit(child, nodes2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    private void checkNodeList(List<Node> nodes) {
        if (nodes == null) {
            throw new IllegalArgumentException();
        }

        for (Object node : nodes) {
            if (node == null) {
                throw new NullPointerException();
            }

            if (!(node instanceof Node)) {
                System.out.println("Node of type " + node.getClass() + "?");
            }

            if (!(node instanceof Node)) {
                throw new IllegalArgumentException(
                        "All nodes must implement Node.");
            }
        }
    }

    private void collectAncestorsVisit(Node node, Set<Node> ancestors) {
        ancestors.add(node);
        Collection<Node> parents = getParents(node);

        if (!parents.isEmpty()) {
            for (Node parent : parents) {
                doClosureVisit(parent, ancestors);
            }
        }
    }

    private void collectDescendantsVisit(Node node, Set<Node> descendants) {
        descendants.add(node);
        Collection<Node> children = getChildren(node);

        if (!children.isEmpty()) {
            for (Node child : children) {
                doChildClosureVisit(child, descendants);
            }
        }
    }

    /**
     * closure under the child relation
     */
    private void doChildClosureVisit(Node node, Set<Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Object o : getEdges(node)) {
                Edge edge = (Edge) (o);
                Node sub = Edges.traverseDirected(node, edge);

                if (sub == null) {
                    continue;
                }

                doChildClosureVisit(sub, closure);
            }
        }
    }


    /**
     * This is a simple auxiliary visit method for the isDConnectedTo() method
     * used to find the closure of a conditioning set of nodes under the parent
     * relation.
     *
     * @param node    the node in question
     * @param closure the closure of the conditioning set uner the parent
     *                relation (to be calculated recursively).
     */
    private void doClosureVisit(Node node, Set<Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Object o : getEdges(node)) {
                Edge edge = (Edge) (o);
                Node sub = Edges.traverseReverseDirected(node, edge);

                if (sub != null) {
                    doClosureVisit(sub, closure);
                }
            }
        }
    }


    /**
     * This is the main recursive visit method for the isDConnectedTo method.
     *
     * @param currentNode              the current node in the recursion
     * @param inEdgeEndpoint           the endpoint type of the incoming edge,
     *                                 needed to check for colliders.
     * @param targetNode               the node a d-connecting path is trying to
     *                                 reach.
     * @param path                     the list of nodes along the current path,
     *                                 to check for cycles.
     * @param conditioningNodes        a d-connecting path conditional on these
     *                                 nodes is being sought.
     * @param conditioningNodesClosure the closure of the conditioning nodes
     *                                 under the ancestor relation.
     * @return true if a d-connection is found along this path (here or down
     *         some sub-branch), false if not.
     * @see #isDConnectedTo
     * @see #isDSeparatedFrom
     */

    //This is doing depth-first search.  I think breadth-first might be better.
    //(also applies to other visit methods?)
    private boolean isDConnectedToVisit(Node currentNode,
            Endpoint inEdgeEndpoint, Node targetNode, LinkedList<Node> path,
            List<Node> conditioningNodes, Set<Node> conditioningNodesClosure) {

        if (currentNode == targetNode) {
            return true;
        }

        if (path.contains(currentNode)) {
            return false;
        }

        path.addLast(currentNode);

        for (Object o : getEdges(currentNode)) {
            Edge edge = (Edge) o;
            Endpoint outEdgeEndpoint = edge.getProximalEndpoint(currentNode);

            // Apply the definition of d-connection to determine whether
            // we can pass through on a path from this incoming edge to
            // this outgoing edge through this node.  it all depends
            // on whether this path through the node is a collider or
            // not--that is, whether the incoming endpoint and the outgoing
            // endpoint are both arrow endpoints.
            boolean isCollider = (inEdgeEndpoint == Endpoint.ARROW) &&
                    (outEdgeEndpoint == Endpoint.ARROW);
            boolean passAsCollider = isCollider &&
                    conditioningNodesClosure.contains(currentNode);
            boolean passAsNonCollider =
                    !isCollider && !conditioningNodes.contains(currentNode);

            //This could be optimized by putting tests in directly rather than
            // predefining the booleans, so that doing 'contains' on the (possibly
            // large, and def. larger than just the list of cond nodes)
            // closure would only be done if necessary.     --ekorber

            if (passAsCollider || passAsNonCollider) {
                Node nextNode = Edges.traverse(currentNode, edge);
                Endpoint previousEndpoint = edge.getProximalEndpoint(nextNode);

                if (isDConnectedToVisit(nextNode, previousEndpoint, targetNode,
                        path, conditioningNodes, conditioningNodesClosure)) {
                    return true;
                }
            }
        }

        path.removeLast();
        return false;
    }

    /**
     * This is the main visit method for the existsInducingPath() method.
     *
     * @param node1 the current node in the recursion.
     * @param node2 the target node.
     * @param inEnd the endpoint type of the incoming edge.
     * @return true if an inducing path is found along this path (here or down
     *         some sub-branch), false if not.
     * @see #existsInducingPath
     */
    private boolean existsInducingPathVisit(Node node1, Node node2,
            Endpoint inEnd, Set<Node> pathNodes, Set<Node> observedNodes,
            Set<Node> conditioningNodes, Set<Node> sClosure) {
        if (node1 == node2) {
            return true;
        }

        if (pathNodes.contains(node1)) {
            return false;
        }

        pathNodes.add(node1);

        for (Object o : getEdges(node1)) {
            Edge edge = (Edge) o;
            Endpoint outEnd = edge.getProximalEndpoint(node1);

            // apply the definition of inducing path to determine whether
            // we can pass through on a path from this incoming edge to
            // this outgoing edge through this node.  it all depends
            // on whether this path through the node is a collider or
            // not--that is, whether the incoming endpoint and the outgoing
            // endpoint are both arrows.
            boolean isCollider =
                    (inEnd == Endpoint.ARROW) && (outEnd == Endpoint.ARROW);
            boolean passAsCollider = isCollider && sClosure.contains(node1);
            boolean passAsNonCollider = !isCollider &&
                    !observedNodes.contains(node1) &&
                    !conditioningNodes.contains(node1);

            if (passAsCollider || passAsNonCollider) {
                Node sub = Edges.traverse(node1, edge);
                Endpoint newIn = edge.getProximalEndpoint(sub);

                if (existsInducingPathVisit(sub, node2, newIn, pathNodes,
                        observedNodes, conditioningNodes, sClosure)) {
                    return true;
                }
            }
        }

        pathNodes.remove(node1);

        return false;
    }

    private PropertyChangeSupport getPcs() {
        if (this.pcs == null) {
            this.pcs = new PropertyChangeSupport(this);
        }

        return this.pcs;
    }

    /**
     * Checks to see whether all of the graph basicConstraints will be satisfied
     * on adding a particular node.
     *
     * @param node the node to check.
     * @return true if adding the node is permitted by all of the graph
     *         constraints, false if not.
     */
    private boolean checkAddNode(Node node) {
        for (GraphConstraint graphConstraint : graphConstraints) {
            GraphConstraint gc = (graphConstraint);

            if (!gc.isNodeAddable(node, this)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks to see whether all of the graph constraints will be satisfied on
     * adding a particular edge.
     *
     * @param edge the edge to check.
     * @return true if the condition is met.
     */
    private boolean checkAddEdge(Edge edge) {
        for (GraphConstraint graphConstraint : graphConstraints) {
            GraphConstraint gc = (graphConstraint);

            if (!gc.isEdgeAddable(edge, this)) {
                System.out.println("Edge " + edge + " failed " + gc);
                return false;
            }
        }

        return true;
    }

    /**
     * Checks to see whether all of the graph constraints will be satisfied on
     * removing a particular node.
     *
     * @param node the node to check.
     * @return true if removing the node is permitted by all of the graph
     *         constraints, false if not.
     */
    private boolean checkRemoveNode(Node node) {
        for (GraphConstraint graphConstraint : graphConstraints) {
            GraphConstraint gc = (graphConstraint);

            if (!gc.isNodeRemovable(node, this)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks to see whether all of the graph constraints will be satisfied on
     * removing a particular edge.
     *
     * @param edge the edge to check.
     * @return true if removing the edge is permitted by all of the graph
     *         constraints, false if not.
     */
    private boolean checkRemoveEdge(Edge edge) {
        for (GraphConstraint graphConstraint : graphConstraints) {
            GraphConstraint gc = (graphConstraint);

            if (!gc.isEdgeRemovable(edge, this)) {
                return false;
            }
        }

        return true;
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

        if (nodes == null) {
            throw new NullPointerException();
        }

        if (endpoints == null) {
            throw new NullPointerException();
        }

        if (graphConstraints == null) {
            throw new NullPointerException();
        }
    }

}



