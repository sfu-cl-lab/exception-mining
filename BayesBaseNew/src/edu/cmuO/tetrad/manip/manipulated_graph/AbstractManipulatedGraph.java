package edu.cmu.tetrad.manip.manipulated_graph;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.manip.experimental_setup.VariableManipulation;

import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: Mar 16, 2004
 * Time: 1:29:22 AM
 * To change this template use Options | File Templates.
 *
 * This is the superclass of all Manipulated Graph classes.
 */
public class AbstractManipulatedGraph implements Graph {
    static final long serialVersionUID = 23L;

    private DirectedGraph DG;
    protected Map <String,VariableManipulation> variableManipulations;
    protected List <ManipulatedEdge>brokenEdges;
    protected List <ManipulatedEdge>frozenEdges;

    /**
     * Constructor.
     * @throws IllegalArgumentException
     */
    public AbstractManipulatedGraph(Graph graph) throws IllegalArgumentException {
        DG = new DirectedGraph(graph);
        brokenEdges = new ArrayList<ManipulatedEdge>();
        variableManipulations = new HashMap<String,VariableManipulation>();
        frozenEdges = new ArrayList<ManipulatedEdge>();
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static AbstractManipulatedGraph serializableInstance() {
        return new AbstractManipulatedGraph(new EdgeListGraph());
    }

    /**
     * @return a list of broken edges EdgeInfos.
     */
    public ManipulatedEdge [] getBrokenEdges(){
        ManipulatedEdge [] edgeInfos = new ManipulatedEdge[brokenEdges.size()];
        for(int i = 0; i < brokenEdges.size(); i++){
            edgeInfos[i] = brokenEdges.get(i);
        }
        return edgeInfos;
    }

    /**
     * @return a list of frozen edges EdgeInfos.
     */
    public ManipulatedEdge [] getFrozenEdges(){
        ManipulatedEdge [] edgeInfos = new ManipulatedEdge[frozenEdges.size()];
        for(int i = 0; i < frozenEdges.size(); i++){
            edgeInfos[i] = frozenEdges.get(i);
        }
        return edgeInfos;
    }

    /**
     * Returns the manipulation on a given variable.  If the variable is "DISABLED"
     * the method will return "DISABLED" even though the variable is not in the graph
     * @param variableName the name of the variable to check
     * @return ManipulationType on the variable.
     */
    public VariableManipulation getManipulationFor(String variableName){
        return variableManipulations.get(variableName);
    }

    /**
     * @return a list of EdgeInfo of non-latent edges.
     */
    public ManipulatedEdge [] getAllNonLatentEdges(){
        int i;
        List <Edge>nonLatentEdges = new ArrayList<Edge>();
        Edge edge;

        for(Iterator normalEdges = DG.getEdges().iterator(); normalEdges.hasNext();){
            edge = (Edge) normalEdges.next();
            if((edge.getNode1().getNodeType() == NodeType.MEASURED) && (edge.getNode2().getNodeType() == NodeType.MEASURED)){
                nonLatentEdges.add(edge);
            }
        }

        ManipulatedEdge [] edges = new ManipulatedEdge[brokenEdges.size() + frozenEdges.size() + nonLatentEdges.size()];

        for(i=0; i < nonLatentEdges.size(); i++){
            edges[i] = new ManipulatedEdge(nonLatentEdges.get(i));
        }
        for(Iterator brokens = brokenEdges.iterator(); brokens.hasNext(); i++){
            edges[i] = (ManipulatedEdge) brokens.next();
        }
        for(Iterator frozens = frozenEdges.iterator(); frozens.hasNext(); i++){
            edges[i] = (ManipulatedEdge) frozens.next();
        }
        return edges;
    }

    /**
     * @param fromNode starting node.
     * @param toNode ending node.
     * @return the EdgeInfo of the directed edge from one node to another.
     */
    public ManipulatedEdge getAnyEdge(String fromNode, String toNode){
        //return a normal edge if there is one
        Edge edge = DG.getEdge(DG.getNode(fromNode), DG.getNode(toNode));
        if(edge != null){
            return new ManipulatedEdge(edge);
        }

        //return a broken edge if there is one
        ManipulatedEdge [] edges;
        ManipulatedEdge edgeInfo;
        edges = getBrokenEdges();
        for(int i = 0; i < edges.length; i++){
            edgeInfo = edges[i];
            if(edgeInfo.getFromNode().equals(fromNode) && edgeInfo.getToNode().equals(toNode)){
                return edgeInfo;
            }
        }

        //return a frozen edge if there is one
        edges = getFrozenEdges();
        for(int i = 0; i < edges.length; i++){
            edgeInfo = edges[i];
            if(edgeInfo.getFromNode().equals(fromNode) && edgeInfo.getToNode().equals(toNode)){
                return edgeInfo;
            }
        }

        return null;
    }

    /**
     * @param fromNode starting node.
     * @param toNode ending node.
     * @return the manipulation type for the edge from one node to another.
     */
    public EdgeManipulation getManipulationForEdge(String fromNode, String toNode){
        Edge edge = DG.getEdge(DG.getNode(fromNode), DG.getNode(toNode));
        if(edge != null){
            return EdgeManipulation.NORMAL;
        }

        ManipulatedEdge [] edges = getFrozenEdges();
        ManipulatedEdge edgeInfo;
        int i;
        for(i = 0; i < edges.length; i++){
            edgeInfo = edges[i];
            if(edgeInfo.getFromNode().equals(fromNode) && edgeInfo.getToNode().equals(toNode)){
                return EdgeManipulation.FROZEN;
            }
        }

        edges = getBrokenEdges();
        for(i = 0; i < edges.length; i++){
            edgeInfo = edges[i];
            if(edgeInfo.getFromNode().equals(fromNode) && edgeInfo.getToNode().equals(toNode)){
                return EdgeManipulation.BROKEN;
            }
        }

        return null;
    }

    /**
     * Breaks the list of edges.
     * @param edges list of edges to break.
     */
    protected void breakEdges(List edges){
        Edge edge;
        for(Iterator e = edges.listIterator(); e.hasNext(); ){
            edge = (Edge) e.next();
            brokenEdges.add(new ManipulatedEdge(edge, EdgeManipulation.BROKEN));
            DG.removeEdge(edge);
        }
    }

    /**
     * Freezes the list of edges.
     * @param edges list of edges to freeze.
     */
    protected void freezeEdges(List edges){
        Edge edge;
        for(Iterator e = edges.listIterator(); e.hasNext(); ){
            edge = (Edge) e.next();
            frozenEdges.add(new ManipulatedEdge(edge, EdgeManipulation.FROZEN));
            //you don't remove the frozen edge because this would mean you have to redefine
            //the probabilities for the toNode,  easier to just leave the probabilities
            //and change the values of the from node
        }
    }






    //===========================================================
    //
    //                      GRAPH METHODS
    //
    //===========================================================

    /**
     * Adds a bidirected edges <-> to the graph.
     */
    @Override
	public boolean addBidirectedEdge(Node node1, Node node2){
        return DG.addBidirectedEdge(node1,node2);
    }

    /**
     * Adds a directed edge --> to the graph.
     */
    @Override
	public boolean addDirectedEdge(Node node1, Node node2){
        return DG.addDirectedEdge(node1, node2);
    }

    /**
     * Adds an undirected edge --- to the graph.
     */
    @Override
	public boolean addUndirectedEdge(Node node1, Node node2){
        return DG.addUndirectedEdge(node1, node2);
    }

    /**
     * Adds the specified edge to the graph, provided it is not already in the
     * graph.
     *
     * @return true if the edge was added, false if not.
     */
    @Override
	public boolean addEdge(Edge edge){
        return DG.addEdge(edge);
    }

    /**
     * Adds a graph constraint.
     *
     * @return true if the constraint was added, false if not.
     */
    @Override
	public boolean addGraphConstraint(GraphConstraint gc){
        return DG.addGraphConstraint(gc);
    }

    /**
     * Adds a node to the graph. Precondition: The proposed name of the node
     * cannot already be used by any other node in the same graph.
     *
     * @return true if nodes were added, false if not.
     */
    @Override
	public boolean addNode(Node node){
        return DG.addNode(node);
    }

    /**
     * Adds a PropertyChangeListener to the graph.
     */
    @Override
	public void addPropertyChangeListener(PropertyChangeListener e){
        DG.addPropertyChangeListener(e);
    }

    /**
     * Removes all nodes (and therefore all edges) from the graph.
     */
    @Override
	public void clear(){
        DG.clear();
    }

    /**
     * Determines whether this graph contains the given edge.
     *
     * @return true iff the graph contain 'edge'.
     */
    @Override
	public boolean containsEdge(Edge edge){
        return DG.containsEdge(edge);
    }

    /**
     * Determines whether this graph contains the given node.
     *
     * @return true iff the graph contains 'node'.
     */
    @Override
	public boolean containsNode(Node node){
        return DG.containsNode(node);
    }

    /**
     * Returns true iff there is a directed cycle in the graph.
     */
    @Override
	public boolean existsDirectedCycle(){
        return DG.existsDirectedCycle();
    }

    /**
     * Returns true iff there is a directed path from node1 to node2 in the
     * graph.
     */
    @Override
	public boolean existsDirectedPathFromTo(Node node1, Node node2){
        return DG.existsDirectedPathFromTo(node1, node2);
    }

    @Override
	public boolean existsUndirectedPathFromTo(Node node1, Node node2) {
        throw new UnsupportedOperationException();
    }

    /**
     * </p> A semi-directed path from A to B is an undirected path in which no
     * edge has an arrowhead pointing "back" towards A.
     *
     * @return true iff there is a semi-directed path from node1 to something in
     *         nodes2 in the graph
     */
    @Override
	public boolean existsSemiDirectedPathFromTo(Node node1, Set <Node>nodes){
        return DG.existsSemiDirectedPathFromTo(node1, nodes);
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
                                      Set <Node>observedNodes,
                                      Set <Node>conditioningNodes){
        return DG.existsInducingPath(node1, node2, observedNodes, conditioningNodes);
    }

    /**
     * Returns true iff a trek exists between two nodes in the graph.  A trek
     * exists if there is a directed path between the two nodes or else, for
     * some third node in the graph, there is a path to each of the two nodes in
     * question.
     */
    @Override
	public boolean existsTrek(Node node1, Node node2){
        return DG.existsTrek(node1, node2);
    }

    /**
     * Determines whether this graph is equal to some other graph, in the sense
     * that they contain the same nodes and the sets of edges defined over these
     * nodes in the two graphs are isomorphic typewise. That is, if node A and B
     * exist in both graphs, and if there are, e.g., three edges between A and B
     * in the first graph, two of which are directed edges and one of which is
     * an undirected edge, then in the second graph there must also be two
     * directed edges and one undirected edge between nodes A and B.
     */
    @Override
	public boolean equals(Object o){
        return DG.equals(o);
    }

    /**
     * Removes all edges from the graph and fully connects it using #-# edges,
     * where # is the given endpoint.
     */
    @Override
	public void fullyConnect(Endpoint endpoint){
        DG.fullyConnect(endpoint);
    }

    /**
     * Reorients all edges in the graph with the given endpoint.
     */
    @Override
	public void reorientAllWith(Endpoint endpoint){
        DG.reorientAllWith(endpoint);
    }

    /**
     * Returns the list of nodes adjacent to the given node.
     */
    @Override
	public List<Node> getAdjacentNodes(Node node){
        return DG.getAdjacentNodes(node);
    }

    /**
     * Returns the list of ancestors for the given nodes.
     */
    @Override
	public List<Node> getAncestors(List <Node> nodes){
        return DG.getAncestors(nodes);
    }

    /**
     * Returns the list of children for a node.
     */
    @Override
	public List<Node> getChildren(Node node){
        return DG.getChildren(node);
    }

    /**
     * Returns the connectivity of the graph.
     */
    @Override
	public int getConnectivity(){
        return DG.getConnectivity();
    }

    /**
     * Returns the list of descendants for the given nodes.
     */
    @Override
	public List <Node> getDescendants(List <Node> nodes){
        return DG.getDescendants(nodes);
    }

    /**
     * Returns the edge connecting node1 and node2, provided a unique such edge
     * exists.
     *
     * @throws UnsupportedOperationException if the graph allows multiple edges
     *                                       between node pairs.
     */
    @Override
	public Edge getEdge(Node node1, Node node2){
        return DG.getEdge(node1, node2);
    }

    @Override
	public Edge getDirectedEdge(Node node1, Node node2) {
        return DG.getDirectedEdge(node1, node2);
    }

    /**
     * Returns the list of edges connected to a particular node. No particular
     * ordering of the edges in the list is guaranteed.
     */
    @Override
	public List <Edge> getEdges(Node node){
        return DG.getEdges(node);
    }

    /**
     * Returns the edges connecting node1 and node2.
     */
    @Override
	public List <Edge> getEdges(Node node1, Node node2){
        return DG.getEdges(node1, node2);
    }

    /**
     * Returns the list of edges in the graph.  No particular ordering of the
     * edges in the list is guaranteed.
     */
    @Override
	public List <Edge> getEdges(){
        return DG.getEdges();
    }

    /**
     * Returns the endpoint along the edge from node to node2 at the node2 end.
     */
    @Override
	public Endpoint getEndpoint(Node node1, Node node2){
        return DG.getEndpoint(node1, node2);
    }

    /**
     * Returns a matrix of endpoints for the nodes in this graph, with nodes in
     * the same order as getNodes().
     */
    @Override
	public Endpoint[][] getEndpointMatrix(){
        return DG.getEndpointMatrix();
    }

    /**
     * Returns the list of graph constraints for this graph.
     */
    @Override
	public List <GraphConstraint> getGraphConstraints(){
        return DG.getGraphConstraints();
    }

    /**
     * Returns the number of arrow endpoints adjacent to a node.
     */
    @Override
	public int getIndegree(Node node){
        return DG.getIndegree(node);
    }

    /**
     * Returns the node with the given string name.  In case of accidental
     * duplicates, the first node encountered with the given name is returned.
     * In case no node exists with the given name, null is returned.
     */
    @Override
	public Node getNode(String name){
        return DG.getNode(name);
    }

    /**
     * Returns the list of nodes for the graph.
     */
    @Override
	public List <Node> getNodes(){
        return DG.getNodes();
    }

    /**
     * Returns the number of edges in the (entire) graph.
     */
    @Override
	public int getNumEdges(){
        return DG.getNumEdges();
    }

    /**
     * Returns the number of edges in the graph which are connected to a
     * particular node.
     */
    @Override
	public int getNumEdges(Node node){
        return DG.getNumEdges(node);
    }

    /**
     * Returns the number of nodes in the graph.
     */
    @Override
	public int getNumNodes(){ return DG.getNumNodes(); }

    /**
     * Returns the number of null endpoints adjacent to an edge.
     */
    @Override
	public int getOutdegree(Node node){ return DG.getOutdegree(node);}

    /**
     * Returns the list of parents for a node.
     */
    @Override
	public List <Node> getParents(Node node){ return DG.getParents(node);}

    /**
     * Returns true iff node1 is adjacent to node2 in the graph.
     */
    @Override
	public boolean isAdjacentTo(Node node1, Node node2){ return DG.isAdjacentTo(node1,node2);}

    /**
     * Determines whether one node is an ancestor of another.
     */
    @Override
	public boolean isAncestorOf(Node node1, Node node2){ return DG.isAncestorOf(node1, node2);}

    /**
     * added by ekorber, 2004/06/12
     *
     * @return true if node1 is a possible ancestor of node2.
     */
    @Override
	public boolean possibleAncestor(Node node1, Node node2){ return DG.possibleAncestor(node1, node2);}

    /**
     * Returns true iff node1 is a child of node2 in the graph.
     */
    @Override
	public boolean isChildOf(Node node1, Node node2){ return DG.isChildOf(node1, node2);}

    /**
     * Determines whether node1 is a parent of node2.
     */
    @Override
	public boolean isParentOf(Node node1, Node node2){ return DG.isParentOf(node1, node2);}

    /**
     * Determines whether one node is a proper ancestor of another.
     */
    @Override
	public boolean isProperAncestorOf(Node node1, Node node2){ return DG.isProperAncestorOf(node1, node2);}

    /**
     * Determines whether one node is a proper decendent of another.
     */
    @Override
	public boolean isProperDescendentOf(Node node1, Node node2){ return DG.isProperDescendentOf(node1, node2);}

    /**
     * Returns true iff node1 is a (non-proper) descendant of node2.
     */
    @Override
	public boolean isDescendentOf(Node node1, Node node2){ return DG.isDescendentOf(node1, node2);}

    /**
     * A node Y is a definite nondescendent of a node X just in case there is no
     * semi-directed path from X to Y.
     * <p/>
     * added by ekorber, 2004/06/12.
     *
     * @return true if node 2 is a definite nondecendent of node 1
     */
    @Override
	public boolean defNonDescendent(Node node1, Node node2){
        return DG.defNonDescendent(node1, node2);
    }

    /**
     * Added by ekorber, 2004/6/9.
     *
     * @return true if node 2 is a definite noncollider between 1 and 3
     */
    @Override
	public boolean isDefiniteNoncollider(Node node1, Node node2, Node node3){
        return DG.isDefiniteNoncollider(node1, node2, node3);
    }

    /**
     * Added by ekorber, 2004/6/9.
     *
     * @return true if node 2 is a definite collider between 1 and 3
     */
    @Override
	public boolean isDefiniteCollider(Node node1, Node node2, Node node3){
        return DG.isDefiniteCollider(node1, node2, node3);
    }

    /**
     * Determines whether one node is d-connected to another. According to
     * Spirtes, Richardson & Meek, two nodes are d- connected given some
     * conditioning set Z if there is an acyclic undirected path U between them,
     * such that every collider on U is an ancestor of some element in Z and
     * every non-collider on U is not in Z.  Two elements are d-separated just
     * in case they are not d-connected.  A collider is a node which two edges
     * hold in common for which the endpoints leading into the node are both
     * arrow endpoints.
     */
    @Override
	public boolean isDConnectedTo(Node node1, Node node2, List<Node> z){
        return DG.isDConnectedTo(node1, node2, z);
    }

    /**
     * Determines whether one node is d-separated from another. Two elements are
     * d-separated just in case they are not d-connected.
     */
    @Override
	public boolean isDSeparatedFrom(Node node1, Node node2, List <Node> z){
        return DG.isDSeparatedFrom(node1, node2, z);
    }

    /**
     * Determines if nodes 1 and 2 are possibly d-connected given conditioning
     * set z.  A path U is possibly-d-connecting if every definite collider on U
     * is a possible ancestor of a node in z and every definite non-collider is
     * not in z.
     * <p/>
     * added by ekorber, 2004/06/15.
     *
     * @return true iff nodes 1 and 2 are possibly d-connected given z
     */
    @Override
	public boolean possDConnectedTo(Node node1, Node node2, List<Node> z){
        return DG.possDConnectedTo(node1, node2, z);
    }

    /**
     * Returns true iff there is a single directed edge from node1 to node2 in
     * the graph.
     */
    @Override
	public boolean isDirectedFromTo(Node node1, Node node2){
        return DG.isDirectedFromTo(node1, node2);
    }

    /**
     * Returns true iff there is a single undirected edge from node1 to node2 in
     * the graph.
     */
    @Override
	public boolean isUndirectedFromTo(Node node1, Node node2){
        return DG.isUndirectedFromTo(node1, node2);
    }

    /**
     * A directed edge A->B is definitely visible if there is a node C not
     * adjacent to B such that C*->A is in the PAG. Added by ekorber,
     * 2004/06/11.
     *
     * @return true if the given edge is definitely visible (Jiji, pg 25)
     * @throws IllegalArgumentException if the given edge is not a directed edge
     *                                  in the graph
     */
    @Override
	public boolean defVisible(Edge edge){
        return DG.defVisible(edge);
    }

    /**
     * Returns true iff the given node is exogenous in the graph.
     */
    @Override
	public boolean isExogenous(Node node){
        return DG.isExogenous(node);
    }

    /**
     * Nodes adjacent to the given node with the given proximal endpoint.
     */
    @Override
	public List<Node> getNodesInTo(Node node, Endpoint n){
        return DG.getNodesInTo(node, n);
    }

    /**
     * Nodes adjacent to the given node with the given distal endpoint.
     */
    @Override
	public List<Node> getNodesOutTo(Node node, Endpoint n){
        return DG.getNodesOutTo(node, n);
    }

    /**
     * Removes the given edge from the graph.
     *
     * @return true if the edge was removed, false if not.
     */
    @Override
	public boolean removeEdge(Edge edge){
        return DG.removeEdge(edge);
    }

    /**
     * Removes the edge connecting the two given nodes, provided there is
     * exactly one such edge.
     *
     * @throws UnsupportedOperationException if multiple edges between node
     *                                       pairs are not supported.
     */
    @Override
	public boolean removeEdge(Node node1, Node node2){
        return DG.removeEdge(node1, node2);
    }

    /**
     * Removes all edges connecting node A to node B.  In most cases, this will
     * remove at most one edge, but since multiple edges are permitted in some
     * graph implementations, the number will in some cases be greater than
     * one.
     *
     * @return true if edges were removed, false if not.
     */
    @Override
	public boolean removeEdges(Node node1, Node node2){
        return DG.removeEdges(node1, node2);
    }

    /**
     * Iterates through the list and removes any permissible edges found.  The
     * order in which edges are added is the order in which they are presented
     * in the iterator.
     *
     * @return true if edges were added, false if not.
     */
    @Override
	public boolean removeEdges(List <Edge> edges){
        //return DG.removeNodes(edges);
        return DG.removeEdges(edges);
    }

    /**
     * Removes a node from the graph.
     *
     * @return true if the node was removed, false if not.
     */
    @Override
	public boolean removeNode(Node node){
        return DG.removeNode(node);
    }

    /**
     * Iterates through the list and removes any permissible nodes found.  The
     * order in which nodes are removed is the order in which they are presented
     * in the iterator.
     *
     * @return true if nodes were added, false if not.
     */
    @Override
	public boolean removeNodes(List <Node> nodes){
        return DG.removeNodes(nodes);
    }

    /**
     * Sets the endpoint type at the 'to' end of the edge from 'from' to 'to' to
     * the given endpoint.  Note: NOT CONSTRAINT SAFE
     */
    @Override
	public boolean setEndpoint(Node from, Node to, Endpoint endPoint){
        return DG.setEndpoint(from, to, endPoint);
    }

    /**
     * Returns true iff graph constraints will be checked for future graph
     * modifications.
     */
    @Override
	public boolean isGraphConstraintsChecked(){
        return DG.isGraphConstraintsChecked();
    }

    /**
     * Set whether graph constraints will be checked for future graph
     * modifications.
     */
    @Override
	public void setGraphConstraintsChecked(boolean checked){
        DG.setGraphConstraintsChecked(checked);
    }

    /**
     * Constructs and returns a subgraph consisting of a given subset of the
     * nodes of this graph together with the edges between them.
     */
    @Override
	public Graph subgraph(List <Node> nodes){
        return DG.subgraph(nodes);
    }

    /**
     * Returns a string representation of the graph.
     */
    @Override
	public String toString(){
        return DG.toString();
    }

    /**
     * Transfers nodes and edges from one graph to another.  One way this is
     * used is to change graph types.  One constructs a new graph based on the
     * old graph, and this method is called to transfer the nodes and edges of
     * the old graph to the new graph.
     *
     * @param graph the graph from which nodes and edges are to be pilfered.
     * @throws java.lang.IllegalArgumentException
     *          This exception is thrown if adding some node or edge violates
     *          one of the basicConstraints of this graph.
     */
    @Override
	public void transferNodesAndEdges(Graph graph) throws IllegalArgumentException{
        DG.transferNodesAndEdges(graph);
    }

    /**
     * NOT USED.
     * Adds an nondirected edges o-o to the graph.
     */
    @Override
	public boolean addNondirectedEdge(Node node1, Node node2) {
        return DG.addNondirectedEdge(node1, node2);
    }

    /**
     * NOT USED.
     * Adds a partially oriented edge o-> to the graph.
     */
    @Override
	public boolean addPartiallyOrientedEdge(Node node1, Node node2) {
        return DG.addPartiallyOrientedEdge(node1, node2);
    }

    /**
     * NOT USED.
     * Marks the given triple with the given label.
     */
    @Override
	public void setAmbiguous(Triple triple) {
        DG.setAmbiguous(triple);
    }

    /**
     * NOT USED.
     * Returns true if the given triple was marked unfaithful.
     * @param x
     * @param y
     * @param z
     */
    @Override
	public boolean isAmbiguous(Node x, Node y, Node z) {
        return DG.isAmbiguous(x, y, z);
    }

    @Override
	public Set<Triple> getAmbiguousTriples() {
        return DG.getAmbiguousTriples();
    }
}
