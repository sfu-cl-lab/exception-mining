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
 * To see what Pattern is, look at the graph constraints it uses. </p> Important
 * to inform user that when dynamically adding edges, they may not have a valid
 * Pattern until they run closeInducingPaths.  This is done for them when they
 * construct a Pattern from another graph.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6549 $ $Date: 2006-01-09 13:18:07 -0500 (Mon, 09 Jan
 *          2006) $
 * @see MeasuredLatentOnly
 * @see DirectedUndirectedOnly
 * @see AtMostOneEdgePerPair
 */
public final class Pattern implements TetradSerializable, Graph {
    static final long serialVersionUID = 23L;

    private final static GraphConstraint[] constraints = {
            new MeasuredLatentOnly(), new DirectedUndirectedOnly(),
            new AtMostOneEdgePerPair(), new NoEdgesToSelf()};

    /**
     * @serial
     */
    private final Graph graph = new EdgeListGraph();

    //==========================CONSTRUCTORS==========================//

    /**
     * Constructs a new blank Pattern.
     */
    public Pattern() {
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (Object aConstraints1 : constraints1) {
            addGraphConstraint((GraphConstraint) aConstraints1);
        }
    }

    /**
     * Constructs a new Patterns based on the given graph.
     *
     * @param graph the graph to base the new Pattern on.
     * @throws IllegalArgumentException if the given graph cannot be converted
     *                                  to a Pattern for some reason.
     */
    public Pattern(Graph graph) throws IllegalArgumentException {
        if (graph == null) {
            throw new NullPointerException();
        }

        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (Object aConstraints1 : constraints1) {
            addGraphConstraint((GraphConstraint) aConstraints1);
        }

        transferNodesAndEdges(graph);
        closeInducingPaths();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Pattern serializableInstance() {
        return new Pattern();
    }

    //=========================PUBLIC METHODS===========================//

    @Override
	public final void transferNodesAndEdges(Graph graph)
            throws IllegalArgumentException {
        this.graph.transferNodesAndEdges(graph);
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

    public void closeInducingPaths() {
        List<Node> list = new LinkedList<Node>(getNodes());

        // look for inducing paths over all pairs of nodes.
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {

                //existsIndPath always returns true for a node to itself.  Do we actually
                // want to add self-edges all the time?  I don't think so.  Therefore not
                // even looking at node-to-self pairings

                Node node1 = list.get(i);
                Node node2 = list.get(j);

                Set<Node> allNodes = new HashSet<Node>(list);
                Set<Node> empty = new HashSet<Node>();

                if (existsInducingPath(node1, node2, allNodes, empty)) {
                    //is this the right check, or do I have to look at different cond sets?
                    if (getEdges(node1, node2).isEmpty()) {
                        addEdge(appropriateClosingEdge(node1, node2));
                    }
                }
            }
        }
    }

    //should check and make sure that existsIndPath looks in both dirs (99.9% sure it does),
    // not just from node1 to node2

    private Edge appropriateClosingEdge(Node node1, Node node2) {
        if (isAncestorOf(node1, node2)) {
            return Edges.directedEdge(node1, node2);
        }
        else if (isAncestorOf(node2, node1)) {
            return Edges.directedEdge(node2, node1);
        }
        else {
            return Edges.nondirectedEdge(node1, node2);
        }
    }


    @Override
	public void fullyConnect(Endpoint endpoint) {
        graph.fullyConnect(endpoint);
    }

    @Override
	public void reorientAllWith(Endpoint endpoint) {
        graph.reorientAllWith(endpoint);
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
        return graph.removeEdge(node1, node2);
    }

    @Override
	public boolean removeEdges(Node node1, Node node2) {
        return graph.removeEdges(node1, node2);
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
        return graph.addDirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addUndirectedEdge(Node nodeA, Node nodeB) {
        return graph.addUndirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addNondirectedEdge(Node nodeA, Node nodeB) {
        return graph.addNondirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addPartiallyOrientedEdge(Node nodeA, Node nodeB) {
        return graph.addPartiallyOrientedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addBidirectedEdge(Node nodeA, Node nodeB) {
        return graph.addBidirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addEdge(Edge edge) {
        return graph.addEdge(edge);
    }

    @Override
	public boolean addNode(Node node) {
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
        return graph.removeEdge(edge);
    }

    @Override
	public boolean removeEdges(List<Edge> edges) {
        return graph.removeEdges(edges);
    }

    @Override
	public boolean removeNode(Node node) {
        return graph.removeNode(node);
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
        return graph.isExogenous(node);
    }

    @Override
	public String toString() {
        return graph.toString();
    }

    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        return graph.addGraphConstraint(gc);
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



