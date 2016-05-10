package edu.cmu.tetrad.graph;

import edu.cmu.tetrad.util.TetradSerializable;

import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 25, 2007 Time: 12:52:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IDag extends Graph, TetradSerializable {
    @Override
	boolean addBidirectedEdge(Node node1, Node node2);

    @Override
	boolean addEdge(Edge edge);

    @Override
	boolean addDirectedEdge(Node node1, Node node2);

    @Override
	boolean addGraphConstraint(GraphConstraint gc);

    @Override
	boolean addPartiallyOrientedEdge(Node node1, Node node2);

    @Override
	boolean addNode(Node node);

    @Override
	void addPropertyChangeListener(PropertyChangeListener l);

    @Override
	boolean addUndirectedEdge(Node node1, Node node2);

    @Override
	boolean addNondirectedEdge(Node node1, Node node2);

    @Override
	void clear();

    @Override
	boolean containsEdge(Edge edge);

    @Override
	boolean containsNode(Node node);

    @Override
	boolean defNonDescendent(Node node1, Node node2);

    @Override
	boolean existsDirectedCycle();

    @Override
	boolean defVisible(Edge edge);

    @Override
	boolean isDefiniteNoncollider(Node node1, Node node2, Node node3);

    @Override
	boolean isDefiniteCollider(Node node1, Node node2, Node node3);

    @Override
	boolean existsTrek(Node node1, Node node2);

    @Override
	boolean equals(Object o);

    @Override
	boolean existsDirectedPathFromTo(Node node1, Node node2);

    @Override
	boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes);

    @Override
	boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes);

    @Override
	void fullyConnect(Endpoint endpoint);

    @Override
	Endpoint getEndpoint(Node node1, Node node2);

    @Override
	Endpoint[][] getEndpointMatrix();

    @Override
	List<Node> getAdjacentNodes(Node node);

    @Override
	List<Node> getNodesInTo(Node node, Endpoint endpoint);

    @Override
	List<Node> getNodesOutTo(Node node, Endpoint n);

    @Override
	List<Node> getNodes();

    @Override
	List<Edge> getEdges();

    @Override
	List<Edge> getEdges(Node node);

    @Override
	List<Edge> getEdges(Node node1, Node node2);

    @Override
	Node getNode(String name);

    @Override
	int getNumEdges();

    @Override
	int getNumNodes();

    @Override
	int getNumEdges(Node node);

    @Override
	List<GraphConstraint> getGraphConstraints();

    List<List<Node>> getTiers();

    @Override
	List<Node> getChildren(Node node);

    @Override
	int getConnectivity();

    @Override
	List<Node> getDescendants(List<Node> nodes);

    @Override
	Edge getEdge(Node node1, Node node2);

    @Override
	Edge getDirectedEdge(Node node1, Node node2);

    @Override
	List<Node> getParents(Node node);

    @Override
	int getIndegree(Node node);

    @Override
	int getOutdegree(Node node);

    List<Node> getTierOrdering();

    @Override
	boolean isAdjacentTo(Node nodeX, Node nodeY);

    @Override
	boolean isAncestorOf(Node node1, Node node2);

    @Override
	boolean isDirectedFromTo(Node node1, Node node2);

    @Override
	boolean isUndirectedFromTo(Node node1, Node node2);

    @Override
	boolean isGraphConstraintsChecked();

    @Override
	boolean isParentOf(Node node1, Node node2);

    @Override
	boolean isProperAncestorOf(Node node1, Node node2);

    @Override
	boolean isProperDescendentOf(Node node1, Node node2);

    @Override
	boolean isExogenous(Node node);

    @Override
	boolean isDConnectedTo(Node node1, Node node2,
            List<Node> conditioningNodes);

    @Override
	boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z);

    @Override
	boolean isChildOf(Node node1, Node node2);

    @Override
	boolean isDescendentOf(Node node1, Node node2);

    void printTiers(PrintStream out);

    void printTierOrdering();

    @Override
	boolean removeEdge(Node node1, Node node2);

    @Override
	boolean removeEdges(Node node1, Node node2);

    @Override
	boolean setEndpoint(Node node1, Node node2, Endpoint endpoint);

    @Override
	Graph subgraph(List<Node> nodes);

    @Override
	void setGraphConstraintsChecked(boolean checked);

    @Override
	boolean removeEdge(Edge edge);

    @Override
	boolean removeEdges(List<Edge> edges);

    @Override
	boolean removeNode(Node node);

    @Override
	boolean removeNodes(List<Node> nodes);

    @Override
	void reorientAllWith(Endpoint endpoint);

    @Override
	boolean possibleAncestor(Node node1, Node node2);

    @Override
	List<Node> getAncestors(List<Node> nodes);

    @Override
	boolean possDConnectedTo(Node node1, Node node2, List<Node> z);

    @Override
	void transferNodesAndEdges(Graph graph)
            throws IllegalArgumentException;

    @Override
	void setAmbiguous(Triple triple);

    @Override
	boolean isAmbiguous(Node x, Node y, Node z);

    @Override
	String toString();
}
