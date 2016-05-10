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
 * To see what PAG is, look at the graph constraints it uses. </p> Important to
 * inform user that when dynamically adding edges, they may not have a valid PAG
 * until they run closeInducingPaths.  This is done for them when they construct
 * a PAG from another graph.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6556 $ $Date: 2006-01-09 13:18:07 -0500 (Mon, 09 Jan
 *          2006) $
 * @see MeasuredLatentOnly
 * @see AtMostOneEdgePerPair
 * @see InArrowImpliesNonancestor
 */
public final class Pag implements TetradSerializable, Graph {
    static final long serialVersionUID = 23L;

    private final static GraphConstraint[] constraints = {
            new MeasuredLatentOnly(), new AtMostOneEdgePerPair(),
            new NoEdgesToSelf() /*, new InArrowImpliesNonancestor() */};

    /**
     * @serial
     */
    private final Graph graph = new EdgeListGraph();

    /**
     * @serial
     */
    private List<Triple> underLineTriples = new ArrayList<Triple>();

    /**
     * @serial
     */
    private List<Triple> dottedUnderLineTriples = new ArrayList<Triple>();

    //============================CONSTRUCTORS===========================//

    /**
     * Constructs a new blank PAG.
     */
    public Pag() {
        List<GraphConstraint> constraints1 = Arrays.asList(constraints);

        for (Object aConstraints1 : constraints1) {
            addGraphConstraint((GraphConstraint) aConstraints1);
        }
    }


    /**
     * Copy constructor.
     */
    public Pag(Pag p){
        this((Graph)p);
        this.underLineTriples.addAll(p.underLineTriples);
        this.dottedUnderLineTriples.addAll(p.dottedUnderLineTriples);
    }



    /**
     * Contstucts a new PAG with given nodes and no edges.
     */
    public Pag(List<Node> nodes) {
        this();

        if (nodes == null) {
            throw new NullPointerException();
        }

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) == null) {
                throw new NullPointerException();
            }

            for (int j = 0; j < i; j++) {
                if (nodes.get(i).equals(nodes.get(j))) {
                    throw new IllegalArgumentException();
                }
            }
        }

        for (Object variable : nodes) {
            if (!addNode((Node) variable)) {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Constructs a new PAG based on the given graph.
     *
     * @param graph the graph to base the new PAG on.
     * @throws IllegalArgumentException if the given graph cannot be converted
     *                                  to a PAG for some reason.
     */
    public Pag(Graph graph) throws IllegalArgumentException {
        if (graph == null) {
            throw new NullPointerException("Please specify a PAG.");
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
    public static Pag serializableInstance() {
        return new Pag(Dag.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

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
            for (int j = 0; j < list.size(); j++) {

                // ExistsIndPath always returns true for a node to itself.
                // Do we actually want to add self-edges all the time?
                // I don't think so.  Therefore not even looking at node-to-self
                // pairings
                if (i == j) {
                    continue;
                }

                Node node1 = list.get(i);
                Node node2 = list.get(j);

                Set<Node> allNodes = new HashSet<Node>(list);
                Set<Node> empty = new HashSet<Node>();

                if (existsInducingPath(node1, node2, allNodes, empty)) {

                    // Is this the right check, or do I have to look at different
                    // cond sets?
                    if (getEdges(node1, node2).isEmpty()) {
                        addEdge(appropriateClosingEdge(node1, node2));
                    }
                }
            }
        }
    }

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
        return this.graph.getEndpointMatrix();
    }

    @Override
	public List<Node> getAdjacentNodes(Node node) {
        return this.graph.getAdjacentNodes(node);
    }

    @Override
	public List<Node> getNodesInTo(Node node, Endpoint endpoint) {
        return this.graph.getNodesInTo(node, endpoint);
    }

    @Override
	public List<Node> getNodesOutTo(Node node, Endpoint n) {
        return this.graph.getNodesOutTo(node, n);
    }

    @Override
	public List<Node> getNodes() {
        return this.graph.getNodes();
    }

    @Override
	public boolean removeEdge(Node node1, Node node2) {
        return this.graph.removeEdge(node1, node2);
    }

    @Override
	public boolean removeEdges(Node node1, Node node2) {
        return this.graph.removeEdges(node1, node2);
    }

    @Override
	public boolean isAdjacentTo(Node nodeX, Node nodeY) {
        return this.graph.isAdjacentTo(nodeX, nodeY);
    }

    @Override
	public boolean setEndpoint(Node node1, Node node2, Endpoint endpoint) {
        return this.graph.setEndpoint(node1, node2, endpoint);
    }

    @Override
	public Endpoint getEndpoint(Node node1, Node node2) {
        return this.graph.getEndpoint(node1, node2);
    }

    @Override
	public Graph subgraph(List<Node> nodes) {
        return this.graph.subgraph(nodes);
    }

    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        return this.graph.existsDirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        return graph.existsUndirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes2) {
        return this.graph.existsSemiDirectedPathFromTo(node1, nodes2);
    }

    @Override
	public boolean addDirectedEdge(Node nodeA, Node nodeB) {
        return this.graph.addDirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addUndirectedEdge(Node nodeA, Node nodeB) {
        return this.graph.addUndirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addNondirectedEdge(Node nodeA, Node nodeB) {
        return this.graph.addNondirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addPartiallyOrientedEdge(Node nodeA, Node nodeB) {
        return this.graph.addPartiallyOrientedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addBidirectedEdge(Node nodeA, Node nodeB) {
        return this.graph.addBidirectedEdge(nodeA, nodeB);
    }

    @Override
	public boolean addEdge(Edge edge) {
        return this.graph.addEdge(edge);
    }

    @Override
	public boolean addNode(Node node) {
        return this.graph.addNode(node);
    }

    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        this.graph.addPropertyChangeListener(l);
    }

    @Override
	public boolean containsEdge(Edge edge) {
        return this.graph.containsEdge(edge);
    }

    @Override
	public boolean containsNode(Node node) {
        return this.graph.containsNode(node);
    }

    @Override
	public List<Edge> getEdges() {
        return this.graph.getEdges();
    }

    @Override
	public List<Edge> getEdges(Node node) {
        return this.graph.getEdges(node);
    }

    @Override
	public List<Edge> getEdges(Node node1, Node node2) {
        return this.graph.getEdges(node1, node2);
    }

    @Override
	public Node getNode(String name) {
        return this.graph.getNode(name);
    }

    @Override
	public int getNumEdges() {
        return this.graph.getNumEdges();
    }

    @Override
	public int getNumNodes() {
        return this.graph.getNumNodes();
    }

    @Override
	public int getNumEdges(Node node) {
        return this.graph.getNumEdges(node);
    }

    @Override
	public List<GraphConstraint> getGraphConstraints() {
        return this.graph.getGraphConstraints();
    }

    @Override
	public boolean isGraphConstraintsChecked() {
        return this.graph.isGraphConstraintsChecked();
    }

    @Override
	public void setGraphConstraintsChecked(boolean checked) {
        this.graph.setGraphConstraintsChecked(checked);
    }

    @Override
	public boolean removeEdge(Edge edge) {
        return this.graph.removeEdge(edge);
    }

    @Override
	public boolean removeEdges(List<Edge> edges) {
        return this.graph.removeEdges(edges);
    }

    @Override
	public boolean removeNode(Node node) {
        return this.graph.removeNode(node);
    }

    @Override
	public void clear() {
        this.graph.clear();
    }

    @Override
	public boolean removeNodes(List<Node> nodes) {
        return this.graph.removeNodes(nodes);
    }

    @Override
	public boolean existsDirectedCycle() {
        return this.graph.existsDirectedCycle();
    }

    @Override
	public boolean isDirectedFromTo(Node node1, Node node2) {
        return this.graph.isDirectedFromTo(node1, node2);
    }

    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2) {
        return this.graph.isUndirectedFromTo(node1, node2);
    }

    @Override
	public boolean defVisible(Edge edge) {
        return this.graph.defVisible(edge);
    }

    @Override
	public boolean existsTrek(Node node1, Node node2) {
        return this.graph.existsTrek(node1, node2);
    }

    @Override
	public List<Node> getChildren(Node node) {
        return this.graph.getChildren(node);
    }

    @Override
	public int getConnectivity() {
        return this.graph.getConnectivity();
    }

    @Override
	public List<Node> getDescendants(List<Node> nodes) {
        return this.graph.getDescendants(nodes);
    }

    @Override
	public Edge getEdge(Node node1, Node node2) {
        return this.graph.getEdge(node1, node2);
    }

    @Override
	public Edge getDirectedEdge(Node node1, Node node2) {
        return graph.getDirectedEdge(node1, node2);                
    }

    @Override
	public List<Node> getParents(Node node) {
        return this.graph.getParents(node);
    }

    @Override
	public int getIndegree(Node node) {
        return this.graph.getIndegree(node);
    }

    @Override
	public int getOutdegree(Node node) {
        return this.graph.getOutdegree(node);
    }

    @Override
	public boolean isAncestorOf(Node node1, Node node2) {
        return this.graph.isAncestorOf(node1, node2);
    }

    @Override
	public boolean possibleAncestor(Node node1, Node node2) {
        return this.graph.possibleAncestor(node1, node2);
    }

    @Override
	public List<Node> getAncestors(List<Node> nodes) {
        return this.graph.getAncestors(nodes);
    }

    @Override
	public boolean isChildOf(Node node1, Node node2) {
        return this.graph.isChildOf(node1, node2);
    }

    @Override
	public boolean isDescendentOf(Node node1, Node node2) {
        return this.graph.isDescendentOf(node1, node2);
    }

    @Override
	public boolean defNonDescendent(Node node1, Node node2) {
        return this.graph.defNonDescendent(node1, node2);
    }

    @Override
	public boolean isDefiniteNoncollider(Node node1, Node node2, Node node3) {
        return this.graph.isDefiniteNoncollider(node1, node2, node3);
    }

    @Override
	public boolean isDefiniteCollider(Node node1, Node node2, Node node3) {
        return this.graph.isDefiniteCollider(node1, node2, node3);
    }

    @Override
	public boolean isDConnectedTo(Node node1, Node node2,
            List<Node> conditioningNodes) {
        return this.graph.isDConnectedTo(node1, node2, conditioningNodes);
    }

    @Override
	public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return this.graph.isDSeparatedFrom(node1, node2, z);
    }

    @Override
	public boolean possDConnectedTo(Node node1, Node node2, List<Node> z) {
        return this.graph.possDConnectedTo(node1, node2, z);
    }

    @Override
	public boolean existsInducingPath(Node node1, Node node2,
            Set<Node> observedNodes, Set<Node> conditioningNodes) {
        return this.graph.existsInducingPath(node1, node2, observedNodes,
                conditioningNodes);
    }

    @Override
	public boolean isParentOf(Node node1, Node node2) {
        return this.graph.isParentOf(node1, node2);
    }

    @Override
	public boolean isProperAncestorOf(Node node1, Node node2) {
        return this.graph.isProperAncestorOf(node1, node2);
    }

    @Override
	public boolean isProperDescendentOf(Node node1, Node node2) {
        return this.graph.isProperDescendentOf(node1, node2);
    }

    @Override
	public boolean isExogenous(Node node) {
        return this.graph.isExogenous(node);
    }

    @Override
	public String toString() {
        String graphString = this.graph.toString();
        String ulTrips = "Underline triples:   \n";

        for (Object underLineTriple : underLineTriples) {
            ulTrips += underLineTriple.toString() + "\n";
        }

        String dotUlTrips = "Dotted underline triples:  \n";

        for (Object dottedUnderLineTriple : dottedUnderLineTriples) {
            dotUlTrips += dottedUnderLineTriple.toString() + "\n";
        }

        return graphString + ulTrips + dotUlTrips;
    }

    @Override
	public boolean addGraphConstraint(GraphConstraint gc) {
        return this.graph.addGraphConstraint(gc);
    }

    /**
     * Returns the list of underline triples for the PAG computed by the search
     * method.
     */
    public List<Triple> getUnderLineTriples() {
        return underLineTriples;
    }

    /**
     * Returns the list of dotted underline triples for the PAG computed by the
     * search method.
     */
    public List<Triple> getDottedUnderLineTriples() {
        return dottedUnderLineTriples;
    }


    /**
     * States whether x-y-x is an underline triple or not.
     */
    public boolean isUnderlineTriple(Node x, Node y, Node z){
        for(Triple triple : this.underLineTriples){
            if(triple.getX().equals(x) && triple.getY().equals(y) && triple.getZ().equals(z)){
                return true;
            }
        }
        return false;
    }



    /**
     * Adds an underline triple.
     *
     * @param triple
     */
    public void addUnderlineTriple(Triple triple){
        if(this.isAdjacentTo(triple.getX(), triple.getY())
                && this.isAdjacentTo(triple.getY(), triple.getZ())
                && !this.underLineTriples.contains(new Triple(triple.getZ(), triple.getY(), triple.getX())))
        this.underLineTriples.add(triple);
    }

    /**
     * Removes an underline triple.
     *
     * @param triple
     */
    public void removeUnderlineTriple(Triple triple) {
        if (this.underLineTriples.contains(triple)) {
            underLineTriples.remove(triple);
        }
        else {
            Triple reverseTriple = new Triple(triple.getZ(), triple.getY(), triple.getX());
            if (this.underLineTriples.contains(reverseTriple)) {
                underLineTriples.remove(reverseTriple);
            }
        }
    }


    public void setUnderLineTriples(List<Triple> ulTriples) {
        for (Triple triple : ulTriples) {
            if (triple == null) {
                throw new IllegalArgumentException(
                        "Expecting a Triple: " + triple);
            }
        }

        this.underLineTriples = new ArrayList<Triple>(ulTriples);
    }


    public void setDottedUnderLineTriples(List<Triple> dotUlTriples) {
        for (Triple dotUlTriple : dotUlTriples) {
            if (dotUlTriple == null) {
                throw new IllegalArgumentException(
                        "Expecting a Triple: " + dotUlTriple);
            }
        }

        this.dottedUnderLineTriples = new ArrayList<Triple>(dotUlTriples);
    }


    public Graph getGraph() {
        return this.graph;
    }

    @Override
	public int hashCode() {
        int hashCode = 17;

        for (Node node : getNodes()) {
            hashCode += 23 * node.hashCode();
        }

        for (Edge edge : getEdges()) {
            hashCode += 29 * edge.hashCode();
        }

        return hashCode;
    }

    /**
     * Returns true iff the given object is a graph that is equal to this graph,
     * in the sense that it contains the same nodes and the edges are
     * isomorphic.
     */
    @Override
	public boolean equals(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        Pag pag = (Pag) o;

        // Make sure nodes are namewise isomorphic.
        List<Node> tNodes = pag.getNodes();
        List<Node> oNodes = graph.getNodes();

        loop1:
        for (Iterator<Node> it = tNodes.iterator(); it.hasNext();) {
            Node thisNode = (it.next());

            for (Iterator<Node> it2 = oNodes.iterator(); it2.hasNext();) {
                Node otherNode = it2.next();

                if (thisNode.getName().equals(otherNode.getName())) {
                    it.remove();
                    it2.remove();
                    continue loop1;
                }
            }
        }

        if (!(tNodes.isEmpty() && oNodes.isEmpty())) {
            return false;
        }

        // Make sure edges are isomorphic.
        List<Edge> tEdges = pag.getEdges();
        List<Edge> oEdges = graph.getEdges();

        loop2:
        for (Iterator<Edge> it = tEdges.iterator(); it.hasNext();) {
            Edge thisEdge = it.next();

            for (Iterator<Edge> it2 = oEdges.iterator(); it2.hasNext();) {
                Edge otherEdge = it2.next();

                if (thisEdge.equals(otherEdge)) {
                    it.remove();
                    it2.remove();
                    continue loop2;
                }
            }
        }

        // If either resulting list is nonempty, the edges of the two graphs
        // are not isomorphic.
        if (!(tEdges.isEmpty() && oEdges.isEmpty())) {
            return false;
        }

        if (!(new HashSet<Triple>(underLineTriples).equals(
                new HashSet<Triple>(pag.getUnderLineTriples())))) {
            return false;
        }

        if (!(new HashSet<Triple>(dottedUnderLineTriples).equals(
                new HashSet<Triple>(pag.getDottedUnderLineTriples())))) {
            return false;
        }

        // If all tests pass, then return true.
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

        if (underLineTriples == null) {
            throw new NullPointerException();
        }

        if (dottedUnderLineTriples == null) {
            throw new NullPointerException();
        }
    }

}



