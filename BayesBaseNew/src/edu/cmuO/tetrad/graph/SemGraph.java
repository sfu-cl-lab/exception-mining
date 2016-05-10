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

import edu.cmu.tetrad.util.TetradSerializable;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * </p> Represents the graphical structure of a structural equation model. The
 * linear structure of the structural equation model is constructed by adding
 * non-error nodes to the graph and connecting them with directed edges. As this
 * is done, the graph automatically maintains the invariant that endogenous
 * non-error nodes are associated with explicit error nodes in the graph and
 * exogenous non-error nodes are not. An associated error node for a node N is
 * an error node that has N as its only child, E-->N. Error nodes for exogenous
 * nodes are always implicit in the graph. So as nodes become endogenous, error
 * nodes are added for them, and as they become exogenous, error nodes are
 * removed for them. Correlated errors are represented using bidirected edges
 * among exogenous nodes. Bidirected edges may therefore be added among any
 * exogenous nodes in the graph, though the easiest way to add (or remove)
 * exogenous nodes is determine which non-exogenous nodes N1, N2 they are
 * representing correlated errors for and then to use this formulation:</p>
 * <pre>
 *     addBidirectedEdge(getExogenous(node1), getExogenous(node2));
 *     removeEdge(getExogenous(node1), getExogenous(node2));
 * </pre>
 * </p> This avoids the problem of not knowing whether the exogenous node for a
 * node is itself or its associated error node.</p>
 *
 * @author Joseph Ramsey
 * @version $Revision: 6549 $ $Date: 2006-01-11 13:36:55 -0500 (Wed, 11 Jan
 *          2006) $
 */
public final class SemGraph implements Graph, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The underlying graph that stores all the information. This needs to be an
     * EdgeListGraph or something at least that can allow nodes to quickly be
     * added or removed.
     *
     * @serial
     */
    private final Graph graph;

    /**
     * A map from nodes to their error nodes, if they exist. This includes
     * variables nodes to their error nodes and error nodes to themselves. Added
     * because looking them up in the graph was not scalable.
     *
     * @serial
     */
    private Map<Node, Node> errorNodes = new HashMap<Node, Node>();

    /**
     * True if error terms for exogenous terms should be shown.
     *
     * @serial
     */
    private boolean showErrorTerms = true;

    //=========================CONSTRUCTORS============================//

    /**
     * Constructs a new, empty SemGraph.
     */
    public SemGraph() {
        this.graph = new EdgeListGraph();
    }

    /**
     * Constructs a new SemGraph from the nodes and edges of the given graph.
     */
    public SemGraph(Graph graph) {
        if (graph instanceof SemGraph) {
            this.graph = new EdgeListGraph(graph);
            this.errorNodes =
                    new HashMap<Node, Node>(((SemGraph) graph).errorNodes);
            this.showErrorTerms = ((SemGraph) graph).showErrorTerms;
        }
        else {
            this.graph = new EdgeListGraph();

            for (Node node : graph.getNodes()) {
                addNode(node);
            }

            for (Edge edge : graph.getEdges()) {
                if (Edges.isDirectedEdge(edge)) {
                    addEdge(edge);
                }
                else if (Edges.isBidirectedEdge(edge)) {
                    Node node1 = edge.getNode1();
                    Node node2 = edge.getNode2();

                    addBidirectedEdge(getExogenous(node1), getExogenous(node2));
                }
                else {
                    throw new IllegalArgumentException("A SEM graph may contain " +
                            "only directed and bidirected edges: " + edge);
                }
            }
        }
    }

    /**
     * Copy constructor.
     */
    public SemGraph(SemGraph graph) {
        this.graph = new EdgeListGraph(graph.graph);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemGraph serializableInstance() {
        return new SemGraph();
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Returns the error node associated with the given node, or null if the
     * node has no associated error node.
     */
    public Node getErrorNode(Node node) {
        return errorNodes.get(node);
    }

    /**
     * Finds the set of nodes which have no children, followed by the set of
     * their parents, then the set of the parents' parents, and so on.  The
     * result is returned as a List of Lists.
     *
     * @return the tiers of this digraph.
     * @see #printTiers
     */
    public List<List<Node>> getTiers() {
        Set<Node> found = new HashSet<Node>();
        Set<Node> notFound = new HashSet<Node>();
        List<List<Node>> tiers = new LinkedList<List<Node>>();

        // first copy all the nodes into 'notFound'.
        for (Node node : getNodes()) {
            notFound.add(node);
        }

        // repeatedly run through the nodes left in 'notFound'.  If any node
        // has all of its parents already in 'found', then add it to the
        // current tier.
        while (!notFound.isEmpty()) {
            List<Node> thisTier = new LinkedList<Node>();

            for (Node aNotFound : notFound) {
                if (found.containsAll(getParents(aNotFound))) {
                    thisTier.add(aNotFound);
                }
            }

            // shift all the nodes in this tier from 'notFound' to 'found'.
            notFound.removeAll(thisTier);
            found.addAll(thisTier);

            // add the current tier to the lists of tiers.
            tiers.add(thisTier);
        }

        return tiers;
    }

    /**
     * This method returns the nodes of a digraph ordered in such a way that the
     * parents of any node are contained lower down in the list.
     *
     * @return a tier ordering for the nodes in this graph.
     * @see #printTierOrdering
     */
    public List<Node> getTierOrdering() {
        List<Node> found = new LinkedList<Node>();
        Set<Node> notFound = new HashSet<Node>();

        for (Node node1 : getNodes()) {
            notFound.add(node1);
        }

        List<Node> errorTerms = new LinkedList<Node>();

        for (Node node : notFound) {
            if (node.getNodeType() == NodeType.ERROR) {
                errorTerms.add(node);
            }
        }

        notFound.removeAll(errorTerms);

        while (!notFound.isEmpty()) {
            for (Iterator<Node> it = notFound.iterator(); it.hasNext();) {
                Node node = it.next();

                List<Node> parents = getParents(node);
                parents.removeAll(errorTerms);

                if (found.containsAll(parents)) {
                    found.add(node);
                    it.remove();
                }
            }
        }

        return found;
    }

    /**
     * This is a temporary method to help debug code that uses tiers.
     */
    public void printTiers() {
        List<List<Node>> tiers = getTiers();
        System.out.println();

        for (List<Node> thisTier : tiers) {
            for (Node thisNode : thisTier) {
                System.out.print(thisNode + "\t");
            }

            System.out.println();
        }

        System.out.println("done");
    }

    /**
     * Prints a tier ordering for this SMG.
     */
    public void printTierOrdering() {
        List<Node> v = getTierOrdering();

        System.out.println();

        for (Node aV : v) {
            System.out.print(aV + "\t");
        }

        System.out.println();
    }

    /**
     * Returns true iff either node associated with edge is an error term.
     */
    public static boolean isErrorEdge(Edge edge) {
        return (edge.getNode1().getNodeType() == NodeType.ERROR ||
                (edge.getNode2().getNodeType() == NodeType.ERROR));
    }

    /**
     * Returns the variable node for this node--that is, the associated node, if
     * this is an error node, or the node itself, if it is not.
     */
    public Node getVarNode(Node node) {
        boolean isError = node.getNodeType() == NodeType.ERROR;
     //   if (!containsNode(node) && (!isError || this.showErrorTerms)) {
        if(!containsNode(node)){
            throw new IllegalArgumentException("Node is not in graph: " + node);
        }

        if (isError) {
//            if(!this.showErrorTerms){
//                return null;
//            }
            return GraphUtils.getAssociatedNode(node, this);
        }
        else {
            return node;
        }
    }

    /**
     * Returns the exogenous node for the given node--either the error node for
     * the node, if there is one, or else the node itself, it it's exogenous.
     * @param node the node you want the exogenous node for.
     * @return the exogenous node for that node.
     */
    public Node getExogenous(Node node) {
        return isExogenous(node) ? node : errorNodes.get(node);
    }

    @Override
	public final void transferNodesAndEdges(Graph graph)
            throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setAmbiguous(Triple triple) {
        graph.setAmbiguous(triple);
    }

    @Override
	public boolean isAmbiguous(Node x, Node y, Node z) {
        return graph.isAmbiguous(x, y, z);
    }

    @Override
	public Set<Triple> getAmbiguousTriples() {
        return graph.getAmbiguousTriples();
    }

    @Override
	public void fullyConnect(Endpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    @Override
	public void reorientAllWith(Endpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Endpoint[][] getEndpointMatrix() {
        return graph.getEndpointMatrix();
    }

    @Override
	public List<Node> getAdjacentNodes(Node node) {
        return graph.getAdjacentNodes(node);
    }

    @Override
	public List<Node> getNodesInTo(Node node, Endpoint endpoint) {
        return graph.getNodesInTo(node, endpoint);
    }

    @Override
	public List<Node> getNodesOutTo(Node node, Endpoint n) {
        return graph.getNodesOutTo(node, n);
    }

    @Override
	public List<Node> getNodes() {
        return graph.getNodes();
    }

    @Override
	public boolean removeEdge(Node node1, Node node2) {
        List<Edge> edges = getEdges(node1, node2);

        if (edges.size() > 1) {
            throw new IllegalStateException(
                    "There is more than one edge between " + node1 + " and " +
                            node2);
        }

        return removeEdges(edges);
    }

    @Override
	public boolean removeEdges(Node node1, Node node2) {
        return removeEdges(getEdges(node1, node2));
    }

    @Override
	public boolean isAdjacentTo(Node nodeX, Node nodeY) {
        return graph.isAdjacentTo(nodeX, nodeY);
    }

    @Override
	public boolean setEndpoint(Node node1, Node node2, Endpoint endpoint) {
        return graph.setEndpoint(node1, node2, endpoint);
    }

    @Override
	public Endpoint getEndpoint(Node node1, Node node2) {
        return graph.getEndpoint(node1, node2);
    }

    @Override
	public boolean equals(Object o) {
        return graph.equals(o);
    }

    @Override
	public Graph subgraph(List<Node> nodes) {
        return graph.subgraph(nodes);
    }

    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        return graph.existsDirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return graph.existsUndirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes2) {
        return graph.existsSemiDirectedPathFromTo(node1, nodes2);
    }

    @Override
	public boolean addDirectedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.directedEdge(nodeA, nodeB));
    }

    @Override
	public boolean addUndirectedEdge(Node nodeA, Node nodeB) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean addNondirectedEdge(Node nodeA, Node nodeB) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean addPartiallyOrientedEdge(Node nodeA, Node nodeB) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean addBidirectedEdge(Node nodeA, Node nodeB) {
        return addEdge(Edges.bidirectedEdge(nodeA, nodeB));
    }

    @Override
	public boolean addEdge(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();

        if (!graph.containsNode(node1) || !graph.containsNode(node2)) {
            throw new IllegalArgumentException(
                    "Nodes for edge must be in graph already: " + edge);
        }

        if (Edges.isDirectedEdge(edge)) {
            if (graph.containsEdge(edge)) {
                return false;
            }

            Node head = Edges.getDirectedEdgeHead(edge);

            if (head.getNodeType() == NodeType.ERROR) {
                return false;
            }

            graph.addEdge(edge);
            adjustErrorForNode(head);
            return true;
        }
        else if (Edges.isBidirectedEdge(edge)) {
            if (graph.containsEdge(edge)) {
                return false;
            }

            if (!isExogenous(node1) || !isExogenous(node2)) {
                throw new IllegalArgumentException("Nodes for a bidirected " +
                        "edge must be exogenous: " + edge);
            }

            graph.addEdge(edge);
            return true;
        }
        else {
            throw new IllegalArgumentException(
                    "Only directed and bidirected edges allowed.");
        }
    }

    @Override
	public boolean addNode(Node node) {
        if (node.getNodeType() == NodeType.ERROR) {
            throw new IllegalArgumentException("Error nodes cannot be added " +
                    "directly to the graph: " + node);
        }

        return graph.addNode(node);
    }

    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        graph.addPropertyChangeListener(l);
    }

    @Override
	public boolean containsEdge(Edge edge) {
        return graph.containsEdge(edge);
    }

    @Override
	public boolean containsNode(Node node) {
        return graph.containsNode(node);
    }

    @Override
	public List<Edge> getEdges() {
        return graph.getEdges();
    }

    @Override
	public List<Edge> getEdges(Node node) {
        return graph.getEdges(node);
    }

    @Override
	public List<Edge> getEdges(Node node1, Node node2) {
        return graph.getEdges(node1, node2);
    }

    @Override
	public Node getNode(String name) {
        return graph.getNode(name);
    }

    @Override
	public int getNumEdges() {
        return graph.getNumEdges();
    }

    @Override
	public int getNumNodes() {
        return graph.getNumNodes();
    }

    @Override
	public int getNumEdges(Node node) {
        return graph.getNumEdges(node);
    }

    @Override
	public List<GraphConstraint> getGraphConstraints() {
        return graph.getGraphConstraints();
    }

    @Override
	public boolean isGraphConstraintsChecked() {
        return graph.isGraphConstraintsChecked();
    }

    @Override
	public void setGraphConstraintsChecked(boolean checked) {
        graph.setGraphConstraintsChecked(checked);
    }

    @Override
	public boolean removeEdge(Edge edge) {
        if (!graph.containsEdge(edge)) {
            throw new IllegalArgumentException(
                    "Can only remove edges that are " +
                            "already in the graph: " + edge);
        }

        if (Edges.isDirectedEdge(edge)) {
            Node head = Edges.getDirectedEdgeHead(edge);
            Node tail = Edges.getDirectedEdgeTail(edge);

            if (tail.getNodeType() != NodeType.ERROR) {
                graph.removeEdge(edge);
                adjustErrorForNode(head);
                return true;
            }
            else {
                return false;
            }
        }
        else if (Edges.isBidirectedEdge(edge)) {
            return graph.removeEdge(edge);
        }
        else {
            throw new IllegalArgumentException(
                    "Only directed and bidirected edges allowed.");
        }
    }

    @Override
	public boolean removeEdges(List<Edge> edges) {
        boolean change = false;

        for (Object edge : edges) {
            boolean _change = removeEdge((Edge) edge);
            change = change || _change;
        }

        return change;
    }

    @Override
	public boolean removeNode(Node node) {
        if (!graph.containsNode(node)) {
            throw new IllegalArgumentException(
                    "Graph must contain node: " + node);
        }

        if (node.getNodeType() == NodeType.ERROR) {
            throw new IllegalArgumentException(
                    "Error nodes cannot be removed " +
                            "directly from the graph: " + node);
        }

        Node errorNode = getErrorNode(node);

        if (errorNode != null) {
            graph.removeNode(errorNode);
            errorNodes.remove(errorNode);
            errorNodes.remove(node);
        }

        List<Node> children = graph.getChildren(node);
        graph.removeNode(node);

        for (int i = 0; i > children.size(); i++) {
            Node child = children.get(i);
            adjustErrorForNode(child);
        }

        return true;
    }

    @Override
	public void clear() {
        graph.clear();
    }

    @Override
	public boolean removeNodes(List<Node> nodes) {
        return graph.removeNodes(nodes);
    }

    @Override
	public boolean existsDirectedCycle() {
        return graph.existsDirectedCycle();
    }

    @Override
	public boolean isDirectedFromTo(Node node1, Node node2) {
        return graph.isDirectedFromTo(node1, node2);
    }

    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2) {
        return graph.isUndirectedFromTo(node1, node2);
    }

    @Override
	public boolean defVisible(Edge edge) {
        return graph.defVisible(edge);
    }

    @Override
	public boolean isDefiniteNoncollider(Node node1, Node node2, Node node3) {
        return graph.isDefiniteNoncollider(node1, node2, node3);
    }

    @Override
	public boolean isDefiniteCollider(Node node1, Node node2, Node node3) {
        return graph.isDefiniteCollider(node1, node2, node3);
    }

    @Override
	public boolean existsTrek(Node node1, Node node2) {
        return graph.existsTrek(node1, node2);
    }

    @Override
	public List<Node> getChildren(Node node) {
        return graph.getChildren(node);
    }

    @Override
	public int getConnectivity() {
        return graph.getConnectivity();
    }

    @Override
	public List<Node> getDescendants(List<Node> nodes) {
        return graph.getDescendants(nodes);
    }

    @Override
	public Edge getEdge(Node node1, Node node2) {
        return graph.getEdge(node1, node2);
    }

    @Override
	public Edge getDirectedEdge(Node node1, Node node2) {
        return graph.getDirectedEdge(node1, node2);
    }

    @Override
	public List<Node> getParents(Node node) {
        return graph.getParents(node);
    }

    @Override
	public int getIndegree(Node node) {
        return graph.getIndegree(node);
    }

    @Override
	public int getOutdegree(Node node) {
        return graph.getOutdegree(node);
    }

    @Override
	public boolean isAncestorOf(Node node1, Node node2) {
        return graph.isAncestorOf(node1, node2);
    }

    @Override
	public boolean possibleAncestor(Node node1, Node node2) {
        return graph.possibleAncestor(node1, node2);
    }

    @Override
	public List<Node> getAncestors(List<Node> nodes) {
        return graph.getAncestors(nodes);
    }

    @Override
	public boolean isChildOf(Node node1, Node node2) {
        return graph.isChildOf(node1, node2);
    }

    @Override
	public boolean isDescendentOf(Node node1, Node node2) {
        return graph.isDescendentOf(node1, node2);
    }

    @Override
	public boolean defNonDescendent(Node node1, Node node2) {
        return graph.defNonDescendent(node1, node2);
    }

    @Override
	public boolean isDConnectedTo(Node node1, Node node2,
            List<Node> conditioningNodes) {
        return graph.isDConnectedTo(node1, node2, conditioningNodes);
    }

    @Override
	public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return graph.isDSeparatedFrom(node1, node2, z);
    }

    @Override
	public boolean possDConnectedTo(Node node1, Node node2, List<Node> z) {
        return graph.possDConnectedTo(node1, node2, z);
    }

    @Override
	public boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes) {
        return graph.existsInducingPath(node1, node2, observedNodes,
                conditioningNodes);
    }

    @Override
	public boolean isParentOf(Node node1, Node node2) {
        return graph.isParentOf(node1, node2);
    }

    @Override
	public boolean isProperAncestorOf(Node node1, Node node2) {
        return graph.isProperAncestorOf(node1, node2);
    }

    @Override
	public boolean isProperDescendentOf(Node node1, Node node2) {
        return graph.isProperDescendentOf(node1, node2);
    }

    @Override
	public boolean isExogenous(Node node) {
        return graph.isExogenous(node) || getErrorNode(node) == null;
    }

    @Override
	public String toString() {
        return graph.toString();
    }

    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        throw new UnsupportedOperationException();
    }


    public boolean isShowErrorTerms() {
        return showErrorTerms;
    }

    public void setShowErrorTerms(boolean showErrorTerms) {
        this.showErrorTerms = showErrorTerms;

        List<Node> nodes = getNodes();

        for (Node node : nodes) {
            if (!(node.getNodeType() == NodeType.ERROR)) {
                adjustErrorForNode(node);
            }
        }
    }

    //========================PRIVATE METHODS===========================//

    /**
     * Creates a new error node, names it, and adds it to the graph.
     *
     * @param node the node which the new error node which be attached to.
     * @return the error node which was added.
     */
    private Node addErrorNode(Node node) {
        if (errorNodes.get(node) != null) {
            throw new IllegalArgumentException("Node already in map.");
        }

        List<Node> nodes = graph.getNodes();

        Node error = new GraphNode("E" + "_" + node.getName());

        for (Node possibleError : nodes) {
            if (error.getName().equals(possibleError.getName())) {
                moveAttachedBidirectedEdges(possibleError, node);
                graph.removeNode(possibleError);
                errorNodes.remove(node);
                errorNodes.remove(possibleError);
            }
        }

        error.setNodeType(NodeType.ERROR);

        graph.addNode(error);
        errorNodes.put(node, error);
        errorNodes.put(error, error);
        addEdge(Edges.directedEdge(error, node));
        return error;
    }

    private void moveAttachedBidirectedEdges(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            throw new IllegalArgumentException("Node must not be null.");
        }

        List<Edge> attachedEdges = new LinkedList<Edge>(graph.getEdges(node1));

        for (Edge edge : attachedEdges) {
            if (Edges.isBidirectedEdge(edge)) {
                graph.removeEdge(edge);
                Node distal = edge.getDistalNode(node1);
                graph.addBidirectedEdge(node2, distal);
            }
        }
    }

    /**
     * If the specified node is exogenous and has an error node, moves any edges
     * attached to its error node to the node itself and removes the error node.
     * If the specified node is endogenous and has no error node, adds an error
     * node and moves any bidirected edges attached to the node to its error
     * node.
     */
    private void adjustErrorForNode(Node node) {
        if (!showErrorTerms) {
//            if (!isShowErrorTerms() || shouldBeExogenous(node)) {
            Node errorNode = getErrorNode(node);

            if (errorNode != null) {
                moveAttachedBidirectedEdges(errorNode, node);
                graph.removeNode(errorNode);
                errorNodes.remove(node);
                errorNodes.remove(errorNode);
            }
        }
        else {
            Node errorNode = getErrorNode(node);

            if (errorNode == null) {
                addErrorNode(node);
            }

            errorNode = getErrorNode(node);
            moveAttachedBidirectedEdges(node, errorNode);
        }
    }

    /**
     * Returns true iff the given (non-error) term ought to be exogenous.
     */
    private boolean shouldBeExogenous(Node node) {
        List<Node> parents = graph.getParents(node);

        for (Node parent : parents) {
            if (parent.getNodeType() != NodeType.ERROR) {
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

        if (graph == null) {
            throw new NullPointerException();
        }
    }
}


