package edu.cmu.tetrad.search;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.EvaluationResult.AdjacencyEvaluationResult;
import edu.cmu.tetrad.search.EvaluationResult.CoefficientEvaluationResult;
import edu.cmu.tetrad.search.EvaluationResult.OrientationEvaluationResult;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;


//Dag plus edge weights     //later include distributions for the error terms
public class GraphWithParameters {
	//a Dag has a list of edges
	//therefore, Hashmap from edges to weights

	private Graph graph;
	
	public String generatingMethodName = null;
	public String getGeneratingMethodName(){
		return generatingMethodName;
	}
	
	private HashMap<Edge,Double> weightHash;

//	public Dag patDag = null; //only non-null when graph is a pattern

	/*
	 * estimate the weights for the nodes that have all parents determined.
	 */
	//it would have been more efficient to only regression on the nodes that matter
	public GraphWithParameters(SemIm semIm, Graph truePattern) {
//		Graph g = (truePattern==null) ? semIm.getSemPm().getGraph() : truePattern;
//		this.graph = g;
//		weightHash = new HashMap<Edge,Double>();
		this(truePattern);

		//make the SemIm

		//estimate the weights for the nodes that have all parents determined.
		for (Node node : this.getGraph().getNodes()){
			if (GraphUtils.allAdjacenciesAreDirected(node, getGraph())){	//if we know the set of parents of 'node'

				//steal the coefficients from the SemIm
				for (Edge edge : this.getGraph().getEdges(node)){
					double semImWeight = semIm.getEdgeCoef(edge);
					this.getWeightHash().put(edge, semImWeight);
				}
			}
		}
		this.graph = getGraph();
	}

	public GraphWithParameters(Graph graph) {
		this.graph = graph;
		weightHash = new HashMap<Edge,Double>();
	}

//	public PatternWithParameters(ColtDataSet B) {
//		Shimizu2006Search.makeDagWithParms(B);
//	}

	public void addEdge(Node node1, Node node2, double weight){
		Edge edge = new Edge(node1,node2,Endpoint.TAIL,Endpoint.ARROW);
		getGraph().addEdge(edge);
		getWeightHash().put(edge, weight);
	}

	public void addEdge(String nodeName1, String nodeName2, double weight){
		Node node1 = getGraph().getNode(nodeName1);
		Node node2 = getGraph().getNode(nodeName2);
		addEdge(node1, node2, weight);
	}
	
	
	public GraphWithParameters(RectangularDataSet dataSet) {

		DoubleMatrix2D Bmatrix = dataSet.getDoubleData();
		
//    	List<Node> variables = Bmatrix.getVariables();
		
		this.graph = new EdgeListGraph();
		weightHash = new HashMap<Edge,Double>();

		int n = Bmatrix.rows();
//		System.out.println("n = " + n);
//		n = Bmatrix.columns();
//		System.out.println("n = " + n);
		
		//add nodes
		for (int i=0; i<n; i++){
			Node node = new GraphNode(dataSet.getVariableNames().get(i)); //new GraphNode("X"+(i+1));			
			this.getGraph().addNode(node);
		}
		
		//add edges with weights
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				double value = Bmatrix.get(i, j);
				if (value!=0){
					Node node1 = getGraph().getNode(dataSet.getVariableNames().get(j));//"X"+(j+1)); //read as the B matrix as: from column to row
					Node node2 = getGraph().getNode(dataSet.getVariableNames().get(i));
					Edge edge = new Edge(node1,node2,Endpoint.TAIL, Endpoint.ARROW);
					getGraph().addEdge(edge);
					getWeightHash().put(edge, value);
				}
			}
		}
	}

	@Override
	public String toString(){ //iterate through the edges and print their weight too
		String str="";
		for (Edge edge : getGraph().getEdges()){
			str+=edge.toString();
			str+="   " + getWeightHash().get(edge) + "\n";
		}
		return str;
	}


	int errorsOfOmission=0;
	int errorsOfCommission=0;

	public AdjacencyEvaluationResult evalAdjacency(Graph standardDag){
		//for each edge in this DAG, check whether it is in standardDag. If it isn't, that's an error of
		//commission.
		for (Edge thisEdge : this.getGraph().getEdges()){
			System.out.print("thisEdge = " + thisEdge);

			//is it in this DAG?
			Edge standardEdge = getCorrespondingEdge(standardDag, thisEdge);
			System.out.println(", standardEdge = " + standardEdge);

			boolean adjCorrect = (standardEdge!=null);
			if (!adjCorrect){
				errorsOfCommission++;
			}			
		}

		//for each edge in standardDag, check whether it is in this DAG. If it isn't, that's an error of
		//omission.
		for (Edge standardEdge : standardDag.getEdges()){
			System.out.print("standardEdge = " + standardEdge);

			//is it in this DAG?
			Edge thisEdge = getCorrespondingEdge(this.getGraph(), standardEdge);
			System.out.println(", thisEdge = " + thisEdge);

			boolean adjCorrect = (thisEdge!=null);
			if (!adjCorrect){
				errorsOfOmission++;
			}			
		}			
		return new AdjacencyEvaluationResult(errorsOfOmission,errorsOfCommission);
	}


	public void printAdjacencyEvaluation() {
		System.out.println("== Results of evaluating adjacency ==");
		System.out.println("errorsOfOmission = " + errorsOfOmission);
		System.out.println("errorsOfCommission = " + errorsOfCommission);
	}		


	int oriEvaluated=0;
	int oriCorrect=0;
	int directedWrongWay=0;
	int undirectedWhenShouldBeDirected=0;
	int directedWhenShouldBeUndirected=0;
	List<Edge> correctDirectedOrientationEdges;
	
	
	//evaluating orientations
	//should only evaluate on the adjacencies that are correct
	public OrientationEvaluationResult evalOrientations(Graph standardGraph){
		correctDirectedOrientationEdges = new Vector();

		for (Edge standardEdge : standardGraph.getEdges()){ //for each edge in the "correct" graph

			Edge thisEdge = getCorrespondingEdge(this.getGraph(), standardEdge);
			System.out.print("standardEdge = " + standardEdge +
					(standardEdge==null?"":" (directed = " + standardEdge.isDirected()));
			System.out.println("), thisEdge = " + thisEdge + 
					(thisEdge==null?"":" (directed = " + thisEdge.isDirected()) + ")");

			if (thisEdge==null) //skip the ones that are not adjacent
				continue;

			oriEvaluated++;

			if(!standardEdge.isDirected()){ 
				if(!thisEdge.isDirected()){ //both undirected
					oriCorrect++;
				}
				else{
					directedWhenShouldBeUndirected++;
				}
			}

			else { //standardEdge is directed
				if (thisEdge.isDirected()){ //estimate edge is directed: compare direction
					if (getCorrespondingDirectedEdge(this.getGraph(), standardEdge)!=null){ //there is a corresponding edge pointing "forward"
						oriCorrect++;
						correctDirectedOrientationEdges.add(thisEdge);
					}
					else { //standardEdge is undirected, is directed						
						directedWrongWay++;
					}
				}
				else{ //not directed when it should be
					undirectedWhenShouldBeDirected++;
				}
			}
			System.out.print("\n");

		} //end for
		return new OrientationEvaluationResult(oriCorrect,directedWrongWay,undirectedWhenShouldBeDirected,directedWhenShouldBeUndirected);
	}


	public void printOrientationEvaluation() {
		System.out.println("== Results of evaluating orientation ==");
		System.out.println("oriCorrect = " + oriCorrect + "  directedWrongWay = " + directedWrongWay +
				"  undirectedWhenShouldBeDirected = " + undirectedWhenShouldBeDirected + "  directedWhenShouldBeUndirected = " + directedWhenShouldBeUndirected);
		System.out.println("oriEvaluated = " + oriEvaluated);
	}



	//evaluating coefficients
	double totalCoeffErrorSq;

	//evaluate every node-pair
	public CoefficientEvaluationResult evalCoeffs(GraphWithParameters standardGraph) {
		totalCoeffErrorSq=0;

		List<Node> nodes = getGraph().getNodes();
		for (int i=0; i<nodes.size(); i++){ //iterating through each node pair
			Node node1 = nodes.get(i);
			Node realNode1 = getCorrespondingNode(standardGraph.getGraph(), node1);
			for (int j=0; j<i; j++){
				Node node2 = nodes.get(j);	
				Node realNode2 = getCorrespondingNode(standardGraph.getGraph(), node2);
				
				System.out.println("node1 = " + node1 + "  node2 = " + node2);				
				double coeff12 = getDirectedEdgeCoeff(node1,node2);
				double realCoeff12 = standardGraph.getDirectedEdgeCoeff(realNode1,realNode2);
				double err12 = java.lang.Math.pow(coeff12 - realCoeff12, 2);
				System.out.println("err12 = " + err12);

				double coeff21 = getDirectedEdgeCoeff(node2,node1);
				double realCoeff21 = standardGraph.getDirectedEdgeCoeff(realNode2,realNode1);
				double err21 = java.lang.Math.pow(coeff21 - realCoeff21, 2);
				System.out.println("err21 = " + err21);
				
				double error = err12 + err21;
				System.out.println("error = " + error);
				
				totalCoeffErrorSq+=error;
			}
		}
		
		return new CoefficientEvaluationResult(totalCoeffErrorSq, null);
	}
	
	
	//we call this, passing the edges that PC evaluates
	/**
	 * evalute coefficients for some node pairs
	 * @param standardGraph
	 * @param edges edges from the pattern returned by PC-search
	 */
	public CoefficientEvaluationResult evalCoeffsForNodePairs(GraphWithParameters standardGraph, List<Edge> edges) {

		totalCoeffErrorSq=0;

		//turn them into 'graph' edges	
		for (Edge edge : edges){
			Node node1Edges = edge.getNode1();
			Node node2Edges = edge.getNode2();

			System.out.println("node1Edges = " + node1Edges + "  node2Edges = " + node2Edges);				
			Node node1this = getCorrespondingNode(this.getGraph(), node1Edges);
			Node node2this = getCorrespondingNode(this.getGraph(), node2Edges);
			double coeff12 = getDirectedEdgeCoeff(node1this,node2this);
			Node node1sta = getCorrespondingNode(standardGraph.getGraph(), node1Edges);
			Node node2sta = getCorrespondingNode(standardGraph.getGraph(), node2Edges);
			double realCoeff12 = standardGraph.getDirectedEdgeCoeff(node1sta,node2sta);
			double err12 = java.lang.Math.pow(coeff12 - realCoeff12, 2);
			System.out.println("err12 = " + err12);

			double coeff21 = getDirectedEdgeCoeff(node2this,node1this);
			double realCoeff21 = standardGraph.getDirectedEdgeCoeff(node2sta,node1sta);
			double err21 = java.lang.Math.pow(coeff21 - realCoeff21, 2);
			System.out.println("err21 = " + err21);
			
			double error = err12 + err21;
			System.out.println("error = " + error);
			
			totalCoeffErrorSq+=error;
		}
		return new CoefficientEvaluationResult(totalCoeffErrorSq,edges.size());
	}

	
	
	private double getDirectedEdgeCoeff(Node node1, Node node2) {
		double result;
		Edge edge = getGraph().getDirectedEdge(node1, node2);
		if (edge==null)
			result = 0;
		else
			result = getWeightHash().get(edge);  //weightHash is null!
		return result;
	}

	//should only evaluate those that are oriented correctly
	public void evalCoeffsCorrectOrientation(GraphWithParameters standardGraph) {
		List<Edge> edgesToEvaluate;
//		if (patDag!=null) //we use it
//		{
//		edgesToEvaluate = new Vector();
//		//add only the patDag edges whose orientation is correct
//		for (Edge patDagEdge : patDag.getEdges()){
//		Edge standardEdge = getCorrespondingEdge(standardDag.graph,patDagEdge);

//		if (standardEdge!=null && oriAgrees(patDagEdge,standardEdge))
//		edgesToEvaluate.add(getCorrespondingEdge(this.graph,patDagEdge));
//		}
//		}
//		else 
		edgesToEvaluate = correctDirectedOrientationEdges;

		System.out.println("correctOrientationEdges = " + correctDirectedOrientationEdges);
		for (Edge edge : edgesToEvaluate){
			double thisCoeff = this.getWeightHash().get(edge);
			Edge standardEdge = getCorrespondingEdge(standardGraph.getGraph(), edge);
			double standardCoeff = standardGraph.getWeightHash().get(standardEdge);
			double diff = thisCoeff - standardCoeff;
			System.out.println("thisEdge " + edge + ": " + thisCoeff + "   err = " + diff);
			totalCoeffErrorSq+=java.lang.Math.pow(diff, 2);
		}

	}

	//either both point to the left or both point to the right
	private boolean oriAgrees(Edge edge1, Edge edge2) {
		int count=0;
		System.out.println();
        if (edge1.pointsTowards(edge1.getNode1()))
			count++;			
		if (edge2.pointsTowards(edge2.getNode1()))
			count++;
		return (count%2)==0;
	}

	public void printCoefficientEvaluation() {
		System.out.println("== Results of evaluating coefficients ==");
		System.out.println("totalCoeffErrorSq = " + totalCoeffErrorSq);
	}

	public static Node getCorrespondingNode(Graph graph, Node node){
		String nodeName = node.getName();
		Node node1 = graph.getNode(nodeName);
		return node1;
	}
	
	//returns the edge of graph corresponding to edge
	public static Edge getCorrespondingEdge(Graph graph, Edge edge){
//		System.out.println("entered getCorrespondingEdge: edge = " + edge);
		Node node1 = getCorrespondingNode(graph, edge.getNode1());
		Node node2 = getCorrespondingNode(graph, edge.getNode2());
		Edge result = graph.getEdge(node1, node2);
		return result;
	}

	//returns the directed edge of graph corresponding to edge
	public static Edge getCorrespondingDirectedEdge(Graph graph, Edge edge){
		if (edge==null)
			return null;
		else {
			String nodeName1 = edge.getNode1().getName();
			String nodeName2 = edge.getNode2().getName();
			Node node1 = graph.getNode(nodeName1);
			Node node2 = graph.getNode(nodeName2);		
			Edge result = graph.getDirectedEdge(node1, node2);
			return result;
		}
	}



	//does the graph have an edge similar to 'edge'?
	private static boolean hasCorrespondingAdjacency(Graph graph, Edge edge) {
		Edge corrEdge = getCorrespondingEdge(graph, edge);
		return corrEdge!=null;
	}

	private static boolean directionAgrees(Graph graph, Edge edge) {
		String edgeDirection = (edge.toString().indexOf(">")==-1) ? "left" : "right";

		String nodeName1 = edge.getNode1().getName();
		String nodeName2 = edge.getNode2().getName();
		Node node1 = graph.getNode(nodeName1);
		Node node2 = graph.getNode(nodeName2);		
		Edge graphEdge = graph.getEdge(node1, node2);

		String graphEdgeDirection = (graphEdge.toString().indexOf(">")==-1) ? "left" : "right";

		return edgeDirection.equals(graphEdgeDirection);
	}
	
	/**
	 * creates a PatternWithParameters by running a regression, given a graph and data
	 * @param dataSet
	 * @param graph
	 * @return
	 */
	public static GraphWithParameters regress(RectangularDataSet dataSet, Graph graph){
		SemPm semPmEstDag = new SemPm(graph);
		SemEstimator estimatorEstDag = new SemEstimator(dataSet,semPmEstDag);
		estimatorEstDag.estimate();
		SemIm semImEstDag = estimatorEstDag.getEstimatedSem();
		GraphWithParameters estimatedGraph = new GraphWithParameters(semImEstDag,graph);
		return estimatedGraph;		
	}


	/**
	 * 
	 * @return the B matrix corresponding to the graph
	 * we do the reverse of Shimizu2006Search.makeDagWithParms()
	 */
	//possible difference: makeDagWithParms() uses
	//    	List<Node> variables = ltDataSet.getVariables();
	public ColtDataSet getGraphMatrix() {
		int n = this.getGraph().getNumNodes();
		DoubleMatrix2D matrix = new DenseDoubleMatrix2D(n,n);
		for (Edge edge : this.getGraph().getEdges()){
			Node node1 = edge.getNode1();
			Node node2 = edge.getNode2();
			int node1Index = getGraph().getNodes().indexOf(node1);
			int node2Index = getGraph().getNodes().indexOf(node2);
			double value = getWeightHash().get(edge);
			matrix.set(node2Index, node1Index, value); //the B matrix is read: from column to row
		}		
		return Shimizu2006SearchOld.makeDataSet(matrix, getGraph().getNodes());
	}

	List<List<Integer>> cycles = null;
	
	public List<List<Integer>> getCycles(){
		if (cycles==null){
			//find cycles

		
		
		}
		return cycles;
	}


    public Graph getGraph() {
        return graph;
    }

    public HashMap<Edge, Double> getWeightHash() {
        return weightHash;
    }
}
