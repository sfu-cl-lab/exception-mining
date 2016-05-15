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

package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Parametric model for Structural Equation Models.
 * <p/>
 * Note: Could not
 * get a copy constructor to work properly, so to copy SemPm objects, use object
 * serialization--e.g. java.rmu.MarshalledObject.
 *
 * @author Donald Crimbchin
 * @author Joseph Ramsey
 * @version $Revision: 6551 $
 */
public final class SemPm implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The structural model graph that this sem parametric model is based on.
     *
     * @serial Cannot be null.
     */
    private SemGraph graph;

    /**
     * The list of all nodes (unmodifiable).
     *
     * @serial Cannot be null.
     */
    private List<Node> nodes;

    /**
     * The list of Parameters (unmodifiable).
     *
     * @serial Cannot be null.
     */
    private List<Parameter> parameters;

    /**
     * @serial
     * @deprecated
     */
    @Deprecated
	private List<Parameter> means;

    /**
     * The list of variable nodes (unmodifiable).
     *
     * @serial Cannot be null.
     */
    private List<Node> variableNodes;

    /**
     * @serial Cannot be null.
     * @deprecated
     */
    @Deprecated
	private List<Node> exogenousNodes;

    /**
     * The set of parameter comparisons.
     *
     * @serial Cannot be null.
     */
    private Map<ParameterPair, ParamComparison> paramComparisons =
            new HashMap<ParameterPair, ParamComparison>();

    /**
     * The index of the most recent "T" parameter. (These are variance and
     * covariance terms.)
     *
     * @serial Range >= 0.
     */
    private int tIndex = 0;

    /**
     * The index of the most recent "M" parameter. (These are means.)
     *
     * @serial Range >= 0.
     */
    private int mIndex = 0;

    /**
     * The index of the most recent "B" parameter. (These are edge
     * coefficients.)
     *
     * @serial Range >= 0.
     */
    private int bIndex = 0;

    /**
     * The distribution from which coefficients are drawn.
     */
    private Distribution coefDistribution = new SplitDistribution(0.5, 1.5);
//    private Distribution coefDistribution = new SplitDistribution(.3, 4);
//    private Distribution coefDistribution = new UniformDistribution(-1.5, 1.5);

    //===========================CONSTRUCTORS==========================//

    /**
     * Constructs a BayesPm from the given Graph, which must be convertible
     * first into a ProtoSemGraph and then into a SemGraph.
     */
    public SemPm(Graph graph) {
        this(new SemGraph(graph));
    }

    /**
     * Constructs a new SemPm from the given SemGraph.
     */
    public SemPm(SemGraph graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

        this.graph = graph;
        this.graph.setShowErrorTerms(false);

        initializeNodes(graph);
        initializeVariableNodes();
//        initializeExogenousNodes();
        initializeParams();
    }

    /**
     * Copy constructor.
     */
    public SemPm(SemPm semPm) {
        this.graph = semPm.graph;
        this.nodes = new LinkedList<Node>(semPm.nodes);
        this.parameters = new LinkedList<Parameter>(semPm.parameters);
//        this.means = new LinkedList<Parameter>(semPm.means);
        this.variableNodes = new LinkedList<Node>(semPm.variableNodes);
//        this.exogenousNodes = new LinkedList<Node>(semPm.exogenousNodes);
        this.paramComparisons = new HashMap<ParameterPair, ParamComparison>(
                semPm.paramComparisons);
        this.tIndex = semPm.tIndex;
        this.bIndex = semPm.bIndex;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemPm serializableInstance() {
        Dag dag = new Dag();
        GraphNode node1 = new GraphNode("X");
        dag.addNode(node1);
        return new SemPm(Dag.serializableInstance());
    }

    //============================PUBLIC METHODS========================//

    /**
     * Returns the structural model graph this SEM PM is using.
     */
    public SemGraph getGraph() {
        return this.graph;
    }

    /**
     * Returns a list of all the parameters, including variance, covariance,
     * coefficient, and mean parameters.
     */
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * Returns the list of variable nodes--that is, node that are not error
     * nodes.
     */
    public List<Node> getVariableNodes() {
        return this.variableNodes;
    }

//    /**
//     * Returns the list of exogenous nodes. These are in the same order as the
//     * variable nodes, in this sense: for each i, getExogenousNodes(i) is either
//     * the same as getVariableNodes(i) or else is the error node for
//     * getVariableNodes(i).
//     */
//    public List<Node> getExogenousNodes() {
//        return exogenousNodes;
//    }

    /**
     * Returns the list of exogenous variableNodes.
     */
    public List<Node> getErrorNodes() {
        List<Node> errorNodes = new ArrayList<Node>();

        for (Node node1 : this.nodes) {
            if (node1.getNodeType() == NodeType.ERROR) {
                errorNodes.add(node1);
            }
        }

        return errorNodes;
    }

    /**
     * Returns the list of measured variableNodes.
     */
    public List<Node> getMeasuredNodes() {
        List<Node> measuredNodes = new ArrayList<Node>();

        for (Node variable : getVariableNodes()) {
            if (variable.getNodeType() == NodeType.MEASURED) {
                measuredNodes.add(variable);
            }
        }

        return measuredNodes;
    }

    /**
     * Returns the list of latent variableNodes.
     */                                 
    public List<Node> getLatentNodes() {
        List<Node> latentNodes = new ArrayList<Node>();

        for (Node node1 : this.nodes) {
            if (node1.getNodeType() == NodeType.LATENT) {
                latentNodes.add(node1);
            }
        }

        return latentNodes;
    }

    /**
     * Returns the first parameter encountered with the given name, or null if
     * there is no such parameter.
     */
    public Parameter getParameter(String name) {
        for (Parameter parameter1 : getParameters()) {
            if (name.equals(parameter1.getName())) {
                return parameter1;
            }
        }

        return null;
    }

    /**
     * Return the parameter for the edge connecting the two nodes, or null if
     * there is no such parameter. Special note--when trying to get the
     * parameter for a directed edge, it's better to use getEdgeParameter, which
     * automatically adjusts if the user has changed the endpoints of an edge X1
     * --> X2 to X1 <-- X2.
     */
    public Parameter getVarianceParameter(Node node) {
        if (!getGraph().isExogenous(node)) {
            return null;
        }

        node = getGraph().getVarNode(node);

        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (node == _nodeA && node == _nodeB && parameter.getType() == ParamType.VAR) {
                return parameter;
            }
        }

        return null;
    }

    public Parameter getCovarianceParameter(Node nodeA, Node nodeB) {
        nodeA = getGraph().getVarNode(nodeA);
        nodeB = getGraph().getVarNode(nodeB);

        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (nodeA == _nodeA && nodeB == _nodeB && parameter.getType() == ParamType.COVAR) {
                return parameter;
            } else if (nodeB == _nodeA && nodeA == _nodeB && parameter.getType() == ParamType.COVAR) {
                return parameter;
            }
        }

        return null;
    }

    public Parameter getCoefficientParameter(Node nodeA, Node nodeB) {
        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (nodeA == _nodeA && nodeB == _nodeB && parameter.getType() == ParamType.COEF) {
                return parameter;
            }
        }

        return null;
    }

    public Parameter getMeanParameter(Node node) {
        for (Parameter parameter : this.parameters) {
            Node _nodeA = parameter.getNodeA();
            Node _nodeB = parameter.getNodeB();

            if (node == _nodeA && node == _nodeB && parameter.getType() == ParamType.MEAN) {
                return parameter;
            }
        }

        return null;
    }

    /**
     * Returns the list of measured variable names in the order in which they
     * appear in the list of nodes. (This order is fixed.)
     */
    public String[] getMeasuredVarNames() {
        List<Node> semPmVars = getVariableNodes();
        List<String> varNamesList = new ArrayList<String>();

        for (Node semPmVar : semPmVars) {
            if (semPmVar.getNodeType() == NodeType.MEASURED) {
                varNamesList.add(semPmVar.toString());
            }
        }

        return varNamesList.toArray(new String[0]);
    }

    /**
     * Returns the comparison of parmeter a to parameter b.
     */
    public ParamComparison getParamComparison(Parameter a, Parameter b) {
        if (a == null || b == null) {
            throw new NullPointerException();
        }

        ParameterPair pair1 = new ParameterPair(a, b);
        ParameterPair pair2 = new ParameterPair(b, a);

        if (paramComparisons.containsKey(pair1)) {
            return paramComparisons.get(pair1);
        } else if (paramComparisons.containsKey(pair2)) {
            return paramComparisons.get(pair2);
        } else {
            return ParamComparison.NC;
        }
    }

    /**
     * Returns the comparison of parmeter a to parameter b.
     */
    public void setParamComparison(Parameter a, Parameter b,
                                   ParamComparison comparison) {
        if (a == null || b == null || comparison == null) {
            throw new NullPointerException();
        }

        ParameterPair pair1 = new ParameterPair(a, b);
        ParameterPair pair2 = new ParameterPair(b, a);

        paramComparisons.remove(pair2);
        paramComparisons.remove(pair1);

        if (comparison != ParamComparison.NC) {
            paramComparisons.put(pair1, comparison);
        }
    }


    public List<Parameter> getFreeParameters() {
        List<Parameter> parameters = getParameters();
        List<Parameter> freeParameters = new ArrayList<Parameter>();

        for (Parameter _parameter : parameters) {
            ParamType type = _parameter.getType();

            if (type == ParamType.VAR || type == ParamType.COVAR || type == ParamType.COEF) {
                if (!_parameter.isFixed()) {
                    freeParameters.add(_parameter);
                }
            }
        }
        return freeParameters;
    }
    
    /**
     * Returns the degrees of freedom for the model.
     */
    public int getDof() {
        return (getMeasuredNodes().size() * (getMeasuredNodes().size() + 1)) /
                2 - getFreeParameters().size();
    }

    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\nSEM PM:");
        buf.append("\n\tParameters:");

        for (Parameter parameter : parameters) {
            buf.append("\n\t\t").append(parameter);
        }

        buf.append("\n\tNodes: ");
        buf.append(nodes);

        buf.append("\n\tVariable nodes: ");
        buf.append(variableNodes);

        return buf.toString();
    }

    //============================PRIVATE METHODS======================//

    private void initializeNodes(SemGraph graph) {
        this.nodes = Collections.unmodifiableList(graph.getNodes());
    }

    private void initializeVariableNodes() {
        List<Node> varNodes = new ArrayList<Node>();

        for (Node node1 : this.nodes) {
            Node node = (node1);

            if (node.getNodeType() == NodeType.MEASURED ||
                    node.getNodeType() == NodeType.LATENT) {
                varNodes.add(node);
            }
        }

        this.variableNodes = Collections.unmodifiableList(varNodes);
    }

//    private void initializeExogenousNodes() {
//        List<Node> varNodes = getVariableNodes();
//        List<Node> exogenousNodes = new ArrayList<Node>();
//
//        for (Node varNode : varNodes) {
//            Node exoNode = this.graph.getExogenous(varNode);
//            exogenousNodes.add(exoNode);
//        }
//
//        this.exogenousNodes = Collections.unmodifiableList(exogenousNodes);
//    }

    private void initializeParams() {
        List<Parameter> parameters = new ArrayList<Parameter>();
        List<Parameter> means = new ArrayList<Parameter>();
        List<Edge> edges = graph.getEdges();

        Collections.sort(edges, new Comparator<Edge>() {
            @Override
			public int compare(Edge o1, Edge o2) {
                int compareFirst = o1.getNode1().getName().compareTo(o2.getNode1().toString());
                int compareSecond = o1.getNode1().getName().compareTo(o2.getNode2().toString());

                if (compareFirst != 0) {
                    return compareFirst;
                }

                if (compareSecond != 0) {
                    return compareSecond;
                }

                return 0;
            }
        });

        // Add linear coefficient parameters for all directed edges that
        // aren't error edges.
        for (Edge edge : edges) {
            if (edge.getNode1() == edge.getNode2()) {
                throw new IllegalStateException("There should not be any" +
                        "edges from a node to itself in a SemGraph: " + edge);
            }

            if (!SemGraph.isErrorEdge(edge) &&
                    edge.getEndpoint1() == Endpoint.TAIL &&
                    edge.getEndpoint2() == Endpoint.ARROW) {
                Parameter param = new Parameter(newBName(), ParamType.COEF,
                        edge.getNode1(), edge.getNode2());

//                param.setDistribution(new SplitDistribution(0.5, 1.5));
//                param.setDistribution(new SplitDistributionSpecial(0.5, 1.5));
//                param.setDistribution(new UniformDistribution(-0.2, 0.2));
                param.setDistribution(coefDistribution);
                parameters.add(param);
            }
        }

        // Add error variance parameters for exogenous nodes of all nodes.
        for (Node node : getVariableNodes()) {
            Parameter param =
                    new Parameter(newTName(), ParamType.VAR, node, node);
            param.setDistribution(new UniformDistribution(1.0, 3.0));
            parameters.add(param);
        }

        // Add error covariance parameters for all bidirected edges. (These
        // connect exogenous nodes.)
        for (Edge edge : edges) {
            if (Edges.isBidirectedEdge(edge)) {
                Node node1 = edge.getNode1();
                Node node2 = edge.getNode2();

                node1 = getGraph().getVarNode(node1);
                node2 = getGraph().getVarNode(node2);

                Parameter param = new Parameter(newTName(), ParamType.COVAR,
                        node1, node2);
                param.setDistribution(new SingleValue(0.2));
                parameters.add(param);
            }
        }

        for (Node node : getVariableNodes()) {
            Parameter mean = new Parameter(newMName(), ParamType.MEAN, node,
                    node);
            mean.setDistribution(new NormalDistribution(0.0, 1.0));
            parameters.add(mean);
        }

        this.parameters = Collections.unmodifiableList(parameters);
        this.means = means;
    }

    /**
     * Returns a unique (for this PM) parameter name beginning with the Greek
     * letter theta.
     */
    private String newTName() {
        return "T" + (++this.tIndex);
    }

    /**
     * Returns a unique (for this PM) parameter name beginning with the Greek
     * letter mu.
     */
    private String newMName() {
        return "M" + (++this.mIndex);
    }

    /**
     * Returns a unique (for this PM) parameter name beginning with the letter
     * "B".
     */
    private String newBName() {
        return "B" + (++this.bIndex);
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

        if (nodes == null) {
            throw new NullPointerException();
        }

        if (parameters == null) {
            throw new NullPointerException();
        }

//        if (means == null) {
//            throw new NullPointerException();
//        }

        if (variableNodes == null) {
            throw new NullPointerException();
        }

//        if (exogenousNodes == null) {
//            throw new NullPointerException();
//        }

        if (paramComparisons == null) {
            throw new NullPointerException();
        }

        if (tIndex < 0) {
            throw new IllegalStateException("TIndex out of range: " + tIndex);
        }

        if (mIndex < 0) {
            throw new IllegalStateException("MIndex out of range: " + mIndex);
        }

        if (bIndex < 0) {
            throw new IllegalStateException("BIndex out of range: " + bIndex);
        }
    }

    public void setCoefDistribution(Distribution distribution) {
        this.coefDistribution = distribution;
    }
}


