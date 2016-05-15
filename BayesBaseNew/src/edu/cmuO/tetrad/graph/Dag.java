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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Represents a directed acyclic graph--that is, a graph containing only
 * directed edges, with no cycles. Variables are permitted to be either measured
 * or latent, with at most one edge per node pair, and no edges to self.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5331 $ $Date: 2006-01-11 13:36:55 -0500 (Wed, 11 Jan
 *          2006) $
 */
public final class Dag implements Graph {
    static final long serialVersionUID = 23L;

    /**
     * The constraints that the graph must satisfy.
     */
    private final static GraphConstraint[] constraints = {
            new MeasuredLatentOnly(), new AtMostOneEdgePerPair(),
            new NoEdgesToSelf(), new DirectedEdgesOnly(), new InArrowImpliesNonancestor()};

    /**
     * The wrapped graph.
     *
     * @serial
     */
    private final Graph graph;

    /**
     * A dpath matrix for the DAG. If used, it is updated (where necessary) each
     * time the getDirectedPath method is called with whatever edges are stored
     * in the dpathNewEdges list. New edges that are added are appended to the
     * dpathNewEdges list. When edges are removed and when nodes are added or
     * removed, dpath is set to null.
     */
    private transient int[][] dpath;

    /**
     * New edges that need to be added to the dpath matrix.
     */
    private transient LinkedList<Edge> dpathNewEdges = new LinkedList<Edge>();

    /**
     * The order of nodes used for dpath.
     */
    private transient List<Node> dpathNodes;

    //===============================CONSTRUCTORS=======================//

    /**
     * Constructs a new directed acyclic graph (DAG).
     */
    public Dag() {
        this.graph = new EdgeListGraph();
        setGraphConstraintsChecked(true);
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (GraphConstraint aConstraints1 : constraints1) {
            addGraphConstraint(aConstraints1);
        }
    }

    public Dag(List<Node> nodes) {
        this.graph = new EdgeListGraph(nodes);
        setGraphConstraintsChecked(true);
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (GraphConstraint aConstraints1 : constraints1) {
            addGraphConstraint(aConstraints1);
        }
    }

    /**
     * Constructs a new directed acyclic graph from the given graph object.
     *
     * @param graph the graph to base the new DAG on.
     * @throws IllegalArgumentException if the given graph cannot for some
     *                                  reason be converted into a DAG.
     */
    public Dag(Graph graph) throws IllegalArgumentException {
        this.graph = new EdgeListGraph();

        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (GraphConstraint aConstraints1 : constraints1) {
            addGraphConstraint(aConstraints1);
        }

        transferNodesAndEdges(graph);
        resetDPath();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Dag serializableInstance() {
        Dag dag = new Dag();
        GraphNode node1 = new GraphNode("X");
        dag.addNode(node1);
        return dag;
    }

    //===============================PUBLIC METHODS======================//

    @Override
	public boolean addBidirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean addEdge(Edge edge) {
        reconstituteDpath();
        Node _node1 = Edges.getDirectedEdgeTail(edge);
        Node _node2 = Edges.getDirectedEdgeHead(edge);

        int _index1 = dpathNodes.indexOf(_node1);
        int _index2 = dpathNodes.indexOf(_node2);

        if (dpath[_index2][_index1] == 1) {
            return false;
        }

        boolean added = graph.addEdge(edge);

        if (added) {
            dpathNewEdges().add(edge);
        }

        return added;
    }

    @Override
	public boolean addDirectedEdge(Node node1, Node node2) {
        return addEdge(Edges.directedEdge(node1, node2));
    }

    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        return graph.addGraphConstraint(gc);
    }

    @Override
	public boolean addPartiallyOrientedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean addNode(Node node) {
        boolean added = graph.addNode(node);

        if (added) {
            resetDPath();
        }

        return added;
    }

    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        graph.addPropertyChangeListener(l);
    }

    @Override
	public boolean addUndirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean addNondirectedEdge(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    @Override
	public void clear() {
        graph.clear();
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
	public boolean defNonDescendent(Node node1, Node node2) {
        return graph.defNonDescendent(node1, node2);
    }

    @Override
	public boolean existsDirectedCycle() {
        return false;
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
	public boolean equals(Object o) {
        return graph.equals(o);
    }

    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        resetDPath();
        reconstituteDpath();

        //System.out.println(MatrixUtils.toString(dpath));

        int index1 = dpathNodes.indexOf(node1);
        int index2 = dpathNodes.indexOf(node2);

        return dpath[index1][index2] == 1;
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return false;
    }


    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes) {
        return graph.existsSemiDirectedPathFromTo(node1, nodes);
    }

    @Override
	public boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes) {
        return graph.existsInducingPath(node1, node2, observedNodes,
                conditioningNodes);
    }

    @Override
	public void fullyConnect(Endpoint endpoint) {
        throw new UnsupportedOperationException();
        //graph.fullyConnect(endpoint);
    }

    @Override
	public Endpoint getEndpoint(Node node1, Node node2) {
        return graph.getEndpoint(node1, node2);
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
        for (Node node1 : getNodes()) {
            notFound.add(node1);
        }

        // repeatedly run through the nodes left in 'notFound'.  If any node
        // has all of its parents already in 'found', then add it to the
        // current tier.
        while (!notFound.isEmpty()) {
            List<Node> thisTier = new LinkedList<Node>();

            for (Node node : notFound) {
                if (found.containsAll(getParents(node))) {
                    thisTier.add(node);
                }
            }

            // shift all the nodes in this tier from 'notFound' to 'found'.
            notFound.removeAll(thisTier);
            found.addAll(thisTier);

            // add the current tier to the list of tiers.
            tiers.add(thisTier);
        }

        return tiers;
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

    /**
     * This method returns the nodes of a digraph in such an order that as one
     * iterates through the list, the parents of each node have already been
     * encountered in the list.
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

        while (!notFound.isEmpty()) {
            for (Iterator<Node> it = notFound.iterator(); it.hasNext();) {
                Node node = it.next();

                if (found.containsAll(getParents(node))) {
                    found.add(node);
                    it.remove();
                }
            }
        }

        return found;
    }

    @Override
	public boolean isAdjacentTo(Node nodeX, Node nodeY) {
        return graph.isAdjacentTo(nodeX, nodeY);
    }

    @Override
	public boolean isAncestorOf(Node node1, Node node2) {
        return (node1 == node2) || isProperAncestorOf(node1, node2);
        //return graph.isAncestorOf(node1, node2);
    }

    @Override
	public boolean isDirectedFromTo(Node node1, Node node2) {
        return graph.isDirectedFromTo(node1, node2);
    }

    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2) {
        return false;
    }

    @Override
	public boolean isGraphConstraintsChecked() {
        return graph.isGraphConstraintsChecked();
    }

    @Override
	public boolean isParentOf(Node node1, Node node2) {
        return graph.isParentOf(node1, node2);
    }

    @Override
	public boolean isProperAncestorOf(Node node1, Node node2) {
        return existsDirectedPathFromTo(node1, node2);
        //return graph.isProperAncestorOf(node1, node2);
    }

    @Override
	public boolean isProperDescendentOf(Node node1, Node node2) {
        return existsDirectedPathFromTo(node2, node1);
    }

    @Override
	public boolean isExogenous(Node node) {
        return graph.isExogenous(node);
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
	public boolean isChildOf(Node node1, Node node2) {
        return graph.isChildOf(node1, node2);
    }

    @Override
	public boolean isDescendentOf(Node node1, Node node2) {
        return (node1 == node2) || isProperDescendentOf(node1, node2);
        //return graph.isDescendentOf(node1, node2);
    }

    /**
     * Prints the tiers found by method getTiers() to System.out.
     *
     * @param out the printstream to sent output to.
     * @see #getTiers
     */
    public void printTiers(PrintStream out) {

        List<List<Node>> tiers = getTiers();

        System.out.println();

        for (List<Node> thisTier : tiers) {
            for (Node thisNode : thisTier) {
                out.print(thisNode + "\t");
            }

            out.println();
        }

        out.println("done");
    }

    /**
     * Prints the tier ordering found by method getTierOrdering() to
     * System.out.
     *
     * @see #getTierOrdering
     */
    public void printTierOrdering() {
        List<Node> v = getTierOrdering();

        System.out.println();

        for (Node aV : v) {
            System.out.print(aV + "\t");
        }

        System.out.println();
    }

    @Override
	public boolean removeEdge(Node node1, Node node2) {
        boolean removed = graph.removeEdge(node1, node2);

        if (removed) {
            resetDPath();
        }

        return removed;
    }

    @Override
	public boolean removeEdges(Node node1, Node node2) {
        boolean removed = graph.removeEdges(node1, node2);

        if (removed) {
            resetDPath();
        }

        return removed;
    }

    @Override
	public boolean setEndpoint(Node node1, Node node2, Endpoint endpoint) {
        boolean ret = graph.setEndpoint(node1, node2, endpoint);

        resetDPath();

        return ret;
    }

    @Override
	public Graph subgraph(List<Node> nodes) {
        return graph.subgraph(nodes);
    }

    @Override
	public void setGraphConstraintsChecked(boolean checked) {
        graph.setGraphConstraintsChecked(checked);
    }

    @Override
	public boolean removeEdge(Edge edge) {
        boolean removed = graph.removeEdge(edge);
        resetDPath();
        return removed;
    }

    @Override
	public boolean removeEdges(List<Edge> edges) {
        boolean change = false;

        for (Edge edge : edges) {
            boolean _change = removeEdge(edge);
            change = change || _change;
        }

        return change;

        //return graph.removeEdges(edges);
    }

    @Override
	public boolean removeNode(Node node) {
        boolean removed = graph.removeNode(node);

        if (removed) {
            resetDPath();
        }

        return removed;
    }

    @Override
	public boolean removeNodes(List<Node> nodes) {
        return graph.removeNodes(nodes);
    }

    @Override
	public void reorientAllWith(Endpoint endpoint) {
        throw new UnsupportedOperationException();
        //graph.reorientAllWith(endpoint);
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
	public boolean possDConnectedTo(Node node1, Node node2, List<Node> z) {
        return graph.possDConnectedTo(node1, node2, z);
    }

    private void resetDPath() {
        dpath = null;
        dpathNewEdges().clear();
        dpathNewEdges().addAll(getEdges());
    }

    private void reconstituteDpath() {
        if (dpath == null) {
            dpathNodes = getNodes();
            int numNodes = dpathNodes.size();
            dpath = new int[numNodes][numNodes];
        }

        while (!dpathNewEdges().isEmpty()) {
            Edge edge = dpathNewEdges().removeFirst();
            Node _node1 = Edges.getDirectedEdgeTail(edge);
            Node _node2 = Edges.getDirectedEdgeHead(edge);
            int _index1 = dpathNodes.indexOf(_node1);
            int _index2 = dpathNodes.indexOf(_node2);
            dpath[_index1][_index2] = 1;

            for (int i = 0; i < dpathNodes.size(); i++) {
                if (dpath[i][_index1] == 1) {
                    dpath[i][_index2] = 1;
                }

                if (dpath[_index2][i] == 1) {
                    dpath[_index1][i] = 1;
                }
            }
        }
    }

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

    @Override
	public String toString() {
        return graph.toString();
    }

    private LinkedList<Edge> dpathNewEdges() {
        if (dpathNewEdges == null) {
            dpathNewEdges = new LinkedList<Edge>();
        }
        return dpathNewEdges;
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

    //Gustavo 5 May 2007
    //this returns the nodes that have zero parents
    //  
    //should we use getTiers() instead?
	public List<Node> getExogenousTerms() {
		List<Node> errorTerms = new Vector();

		List<Node> nodes = getNodes();
		for (int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			if (getParents(node).isEmpty())
				errorTerms.add(node);
		}
		
		return errorTerms;
	}
}


