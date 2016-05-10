///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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
import edu.cmu.tetrad.util.TetradSerializable;
import edu.cmu.tetradapp.model.calculator.expression.Expression;
import edu.cmu.tetradapp.model.calculator.parser.ExpressionParser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.*;


/**
 * Parametric model for Generalized SEM PM.
 *
 * @author Joseph Ramsey
 */
public final class GeneralizedSemPm implements TetradSerializable {
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
     * The latent and measured nodes.
     */
    private List<Node> variableNodes;

    /**
     * The measured nodes.
     */
    private List<Node> measuredNodes;

    /**
     * The error nodes.
     */
    private List<Node> errorNodes;

    /**
     * The parameters in the model, mapped to the nodes that they are associated with. Each parameter may
     * be associated with more than one node. When parameters are removed from an equation or error distribution,
     * the associated nodes should be removed from the relevant set in this map, and if the set is empty,
     * the parameter should be removed from the map. Also, before adding a parameter to this map, it must be
     * checked whether a parameter by the same name already exists. If one such parameter by the same name
     * already exists, that one should be used instead of the new one. This is needed both to avoid confusion
     * and to allow parameters to be reused in different parts of the interface, creating equality constraints.
     *
     * @serial Cannot be null.
     */
    private Map<String, Set<Node>> referencedParameters;

    /**
     * The nodes of the model, variable nodes or error nodes, mapped to the other nodes that they are
     * associated with.
     */
    private Map<Node, Set<Node>> referencedNodes;

    /**
     * The map from variable nodes to equations.
     */
    private Map<Node, Expression> nodeExpressions;

    /**
     * The String representations of equations that were set.
     */
    private Map<Node, String> nodeExpressionStrings;

    /**
     * Distributions from which initial values for parameters are drawn.
     */
    private Map<String, Expression> parameterExpressions;

    /**
     * String representations of initial parameter distributions. A map from parameter names to expression strings.
     */
    private Map<String, String> parameterExpressionStrings;

    /**
     * The stored template for variables.
     */
    private String variablesTemplate = "TSUM(NEW(B)*$)";

    /**
     * The stored template for error terms.
     */
    private String errorsTemplate = "Normal(0, NEW(s))";

    /**
     * The stored template for parameters.
     */
    private String parametersTemplate = "Split(-1.5,-.5,.5,1.5)";

    /**
     * A map from initial name strings to parameter templates.
     */
    private Map<String, String> startsWithParametersTemplates = new HashMap<String, String>();

    //===========================CONSTRUCTORS==========================//

    /**
     * Constructs a BayesPm from the given Graph, which must be convertible
     * first into a ProtoSemGraph and then into a SemGraph.
     */
    public GeneralizedSemPm(Graph graph) {
        this(new SemGraph(graph));
    }

    /**
     * Constructs a new SemPm from the given SemGraph.
     */
    public GeneralizedSemPm(SemGraph graph) {
        if (graph == null) {
            throw new NullPointerException("Graph must not be null.");
        }

//        if (graph.existsDirectedCycle()) {
//            throw new IllegalArgumentExcneption("Cycles are not supported.");
//        }

        // Cannot afford to allow error terms on this graph to be shown or hidden from the outside; must make a
        // hidden copy of it and make sure error terms are shown.
        this.graph = new SemGraph(graph);
        this.graph.setShowErrorTerms(true);

        for (Edge edge : this.graph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                throw new IllegalArgumentException("The generalized SEM PM cannot currently deal with bidirected " +
                        "edges. Sorry.");
            }
        }

        this.nodes = Collections.unmodifiableList(this.graph.getNodes());

        this.variableNodes = new ArrayList<Node>();
        this.measuredNodes = new ArrayList<Node>();

        for (Node variable : this.nodes) {
//            if (!this.graph.isParameterizable(variable)) {
//                continue;
//            }

            if (variable.getNodeType() == NodeType.MEASURED ||
                    variable.getNodeType() == NodeType.LATENT) {
                variableNodes.add(variable);
            }

            if (variable.getNodeType() == NodeType.MEASURED) {
                measuredNodes.add(variable);
            }
        }

        this.errorNodes = new ArrayList<Node>();

//        for (Node variable : this.nodes) {
//            if (variable.getNodeType() == NodeType.ERROR) {
//                errorNodes.add(variable);
//            }
//        }

        for (Node variable : this.variableNodes) {
            List<Node> parents = this.graph.getParents(variable);
            boolean added = false;

            for (Node _node : parents) {
                if (_node.getNodeType() == NodeType.ERROR) {
                    errorNodes.add(_node);
                    added = true;
                    break;
                }
            }

            if (!added) {
//                throw new NullPointerException("Missing error node for " + variable);
                if (!added) errorNodes.add(null);
            }
        }

        this.referencedParameters = new HashMap<String, Set<Node>>();
        this.referencedNodes = new HashMap<Node, Set<Node>>();
        this.nodeExpressions = new HashMap<Node, Expression>();
        this.nodeExpressionStrings = new HashMap<Node, String>();
        this.parameterExpressions = new HashMap<String, Expression>();
        this.parameterExpressionStrings = new HashMap<String, String>();

        try {
            List<Node> variableNodes = getVariableNodes();

            for (int i = 0; i < variableNodes.size(); i++) {
                Node node = variableNodes.get(i);

                if (!getGraph().isParameterizable(node)) continue;

                String variablestemplate = getVariablesTemplate();
                String formula = TemplateExpander.getInstance().expandTemplate(variablestemplate, this, node);
                setNodeExpression(node, formula);
                Set<String> parameters = getReferencedParameters(node);

                String parametersTemplate = getParametersTemplate();

                for (String parameter : parameters) {
                    if (parametersTemplate != null) {
                        setParameterExpression(parameter, parametersTemplate);
                    }
                    else if (getGraph().isTimeLagModel()) {
                        String expressionString = "Split(-0.9, -.1, .1, 0.9)";
                        setParameterExpression(parameter, expressionString);
                        setParametersTemplate(expressionString);
                    }
                    else {
                        String expressionString = "Split(-1.5, -.5, .5, 1.5)";
                        setParameterExpression(parameter, expressionString);
                        setParametersTemplate(expressionString);
                    }
                }
            }

            for (Node node : errorNodes) {
                if (node == null) continue;

                String template = getErrorsTemplate();
                String formula = TemplateExpander.getInstance().expandTemplate(template, this, node);
                setNodeExpression(node, formula);
                Set<String> parameters = getReferencedParameters(node);

                for (String parameter : parameters) {
                    setParameterExpression(parameter, "U(1, 3)");
                }
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Parse error in constructing initial model.", e);
        }
    }

    public GeneralizedSemPm(SemPm semPm) {
        this(semPm.getGraph());

        System.out.println(this);

        // Write down equations.
        try {
            List<Node> variableNodes = getVariableNodes();

            for (int i = 0; i < variableNodes.size(); i++) {
                Node node = variableNodes.get(i);
                List<Node> parents = getVariableParents(node);

                StringBuilder buf = new StringBuilder();

                for (int j = 0; j < parents.size(); j++) {
                    if (!(variableNodes.contains(parents.get(j)))) {
                        continue;
                    }

                    Node parent = parents.get(j);

                    Parameter _parameter = semPm.getParameter(parent, node);
                    String parameter = _parameter.getName();
                    Set<Node> nodes = new HashSet<Node>();
                    nodes.add(node);

                    referencedParameters.put(parameter, nodes);

                    buf.append(parameter);
                    buf.append("*");
                    buf.append(parents.get(j).getName());

                    if (j < parents.size() - 1) {
                        buf.append(" + ");
                    }
                }

                if (buf.toString().trim().length() != 0) {
                    buf.append(" + ");
                }

                buf.append(errorNodes.get(i));
                setNodeExpression(node, buf.toString());
            }

            for (Node node : variableNodes) {
                Parameter _parameter = semPm.getParameter(node, node);
                String parameter = _parameter.getName();

                Set<Node> nodes = new HashSet<Node>();
                nodes.add(node);

                String distributionFormula = "N(0, " + parameter + ")";
                setNodeExpression(getErrorNode(node), distributionFormula);
            }

            System.out.println(getParameters());
        } catch (ParseException e) {
            throw new IllegalStateException("Parse error in constructing initial model.", e);
        }
    }

    /**
     * Copy constructor.
     */
    public GeneralizedSemPm(GeneralizedSemPm semPm) {
        this.graph = new SemGraph(semPm.graph);
        this.nodes = new ArrayList<Node>(semPm.nodes);
        this.variableNodes = new ArrayList<Node>(semPm.variableNodes);
        this.measuredNodes = new ArrayList<Node>(semPm.measuredNodes);
        this.errorNodes = new ArrayList<Node>(semPm.errorNodes);
        this.referencedParameters = new HashMap<String, Set<Node>>();
        this.referencedNodes = new HashMap<Node, Set<Node>>();

        for (String parameter : semPm.referencedParameters.keySet()) {
            this.referencedParameters.put(parameter, new HashSet<Node>(semPm.referencedParameters.get(parameter)));
        }

        for (Node node : semPm.referencedNodes.keySet()) {
            this.referencedNodes.put(node, new HashSet<Node>(semPm.referencedNodes.get(node)));
        }

        this.nodeExpressions = new HashMap<Node, Expression>(semPm.nodeExpressions);
        this.nodeExpressionStrings = new HashMap<Node, String>(semPm.nodeExpressionStrings);
        this.parameterExpressions = new HashMap<String, Expression>(semPm.parameterExpressions);
        this.parameterExpressionStrings = new HashMap<String, String>(semPm.parameterExpressionStrings);
        this.variablesTemplate = semPm.variablesTemplate;
        this.errorsTemplate = semPm.errorsTemplate;
        this.parametersTemplate = semPm.parametersTemplate;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static GeneralizedSemPm serializableInstance() {
        Dag dag = new Dag();
        GraphNode node1 = new GraphNode("X");
        dag.addNode(node1);
        return new GeneralizedSemPm(Dag.serializableInstance());
    }

    //============================PUBLIC METHODS========================//

    public Expression getNodeExpression(Node node) {
        return this.nodeExpressions.get(node);
    }

    public String getNodeExpressionString(Node node) {
        return nodeExpressionStrings.get(node);
    }

    public void setNodeExpression(Node node, String expressionString) throws ParseException {
        if (node == null) {
            throw new NullPointerException("Node was null.");
        }

        if (expressionString == null) {
//            return;
            throw new NullPointerException("Expression string was null.");
        }

        // Parse the expression. This could throw an ParseException, but that exception needs to handed up the
        // chain, because the interface will need it.
        ExpressionParser parser = new ExpressionParser();
        Expression expression = parser.parseExpression(expressionString);
        List<String> parameterNames = parser.getParameters();
        List<String> variableNames = new ArrayList<String>();

        // Make a list of parent names.
        List<Node> parents = this.graph.getParents(node);
        List<String> parentNames = new LinkedList<String>();

        for (Node parent : parents) {
            parentNames.add(parent.getName());
        }

        // Make a list of parameter names, by removing from the parser's list of parameters any that correspond
        // to parent variables. If there are any variable names (including error terms) that are not among the list of
        // parents, that's a time to throw an exception. We must respect the graph! (We will not complain if any parents
        // are missing.)

        for (Node variable : nodes) {
            if (parentNames.contains(variable.getName())) {
                parameterNames.remove(variable.getName());
                variableNames.add(variable.getName());
            }
//            else {
//                throw new IllegalArgumentException("An equation for node " + node + " may only use as " +
//                        "variables " + getVariableString(parents) + ". (See graph.)");
//            }
        }

//        for (Node variable : nodes) {
//            if (parameterNames.contains(variable.getName())) {
//                throw new IllegalArgumentException("The list of parameter names may not include variables: " + variable.getName());
//            }
//        }

        // Remove old parameter references.
        List<String> parametersToRemove = new LinkedList<String>();

        for (String parameter : this.referencedParameters.keySet()) {
            Set<Node> nodes = this.referencedParameters.get(parameter);

            if (nodes.contains(node)) {
                nodes.remove(node);
            }

            if (nodes.isEmpty()) {
                parametersToRemove.add(parameter);
            }
        }

        for (String parameter : parametersToRemove) {
            this.referencedParameters.remove(parameter);
            this.parameterExpressions.remove(parameter);
            this.parameterExpressionStrings.remove(parameter);
        }

        // Add new parameter references.
        for (String parameterString : parameterNames) {
            if (this.referencedParameters.get(parameterString) == null) {
                this.referencedParameters.put(parameterString, new HashSet<Node>());
            }

            Set<Node> nodes = this.referencedParameters.get(parameterString);
            nodes.add(node);

            setSuitableParameterDistribution(parameterString);
        }

        // Remove old node references.
        List<Node> nodesToRemove = new LinkedList<Node>();

        for (Node _node : this.referencedNodes.keySet()) {
            Set<Node> nodes = this.referencedNodes.get(_node);

            if (nodes.contains(node)) {
                nodes.remove(node);
            }

            if (nodes.isEmpty()) {
                nodesToRemove.add(_node);
            }
        }

        for (Node _node : nodesToRemove) {
            this.referencedNodes.remove(_node);
        }

        // Add new parameters.
        for (String variableString : variableNames) {
            Node _node = getNode(variableString);

            if (this.referencedNodes.get(_node) == null) {
                this.referencedNodes.put(_node, new HashSet<Node>());
            }

            Set<Node> nodes = this.referencedNodes.get(_node);
            nodes.add(node);
        }

        // Finally, save the parsed expression and the original string that the user entered. No need to annoy
        // the user by changing spacing.
        nodeExpressions.put(node, expression);
        nodeExpressionStrings.put(node, expressionString);
    }

    private void setSuitableParameterDistribution(String parameterString) throws ParseException {
        boolean found = false;

        for (String prefix : startsWithParametersTemplates.keySet()) {
            if (parameterString.startsWith(prefix)) {
                setParameterExpression(parameterString, startsWithParametersTemplates.get(prefix));
                found = true;
            }
        }

        if (!found) {
            setParameterExpression(parameterString, getParametersTemplate());
        }
    }

    /**
     * Sets the expression which should be evaluated when calculating new values for the given
     * parameter. These values are used to initialize the parameters.
     * @param parameter The parameter whose initial value needs to be computed.
     * @param expressionString The formula for picking initial values.
     * @throws ParseException If the formula cannot be parsed or contains variable names.
     */
    public void setParameterExpression(String parameter, String expressionString)
            throws ParseException {
        if (parameter == null) {
            throw new NullPointerException("Parameter was null.");
        }

        if (expressionString == null) {
            throw new NullPointerException("Expression string was null.");
        }

        // Parse the expression. This could throw an ParseException, but that exception needs to handed up the
        // chain, because the interface will need it.
        ExpressionParser parser = new ExpressionParser();
        Expression expression = parser.parseExpression(expressionString);
        List<String> parameterNames = parser.getParameters();

        if (parameterNames.size() > 0) {
            throw new IllegalArgumentException("Initial distribution may not " +
                    "contain parameters: " + expressionString);
        }

        parameterExpressions.put(parameter, expression);
        parameterExpressionStrings.put(parameter,  expressionString);
    }

    /**
     * Sets the expression which should be evaluated when calculating new values for the given
     * parameter. These values are used to initialize the parameters.
     * @param parameter The parameter whose initial value needs to be computed.
     * @param expressionString The formula for picking initial values.
     * @throws ParseException If the formula cannot be parsed or contains variable names.
     */
    public void setParameterExpression(String startsWith, String parameter, String expressionString)
            throws ParseException {
        if (parameter == null) {
            throw new NullPointerException("Parameter was null.");
        }

        if (startsWith == null) {
            throw new NullPointerException("StartsWith expression was null.");
        }

        if (startsWith.contains(" ")) {
            throw new IllegalArgumentException("StartsWith expression contains spaces.");
        }

        if (expressionString == null) {
            throw new NullPointerException("Expression string was null.");
        }

        // Parse the expression. This could throw an ParseException, but that exception needs to handed up the
        // chain, because the interface will need it.
        ExpressionParser parser = new ExpressionParser();
        Expression expression = parser.parseExpression(expressionString);
        List<String> parameterNames = parser.getParameters();

        if (parameterNames.size() > 0) {
            throw new IllegalArgumentException("Initial distribution may not " +
                    "contain parameters: " + expressionString);
        }

        parameterExpressions.put(parameter, expression);
        parameterExpressionStrings.put(parameter, expressionString);
        startsWithParametersTemplates.put(startsWith, expressionString);
    }

    /**
     * @return the set of parameters for the model.
     */
    public Set<String> getParameters() {
        return new HashSet<String>(parameterExpressions.keySet());
    }

    /**
     * @param parameter The parameter whose initial value needs to be evaluated.
     * @return an expression that can be used to calculate the initial value.
     */
    public Expression setParameterExpression(String parameter) {
        return parameterExpressions.get(parameter);
    }

    /**
     * @param parameter The parameter wheo initial value needs to be computeed.
     * @return The formula string that was set using <code>setParameterExpression</code>, with spacing intact.
     */
    public String getParameterExpressionString(String parameter) {
        return parameterExpressionStrings.get(parameter);
    }

    /**
     * Returns the structural model graph this SEM PM is using.
     */
    public SemGraph getGraph() {
        return new SemGraph(this.graph);
    }


    /**
     * @return all of the nodes in the sem, including error nodes.
     */
    public List<Node> getNodes() {
        return new ArrayList<Node>(nodes);
    }

    /**
     * Returns the list of variable nodes--that is, node that are not error
     * nodes.
     */
    public List<Node> getVariableNodes() {
        return new ArrayList<Node>(this.variableNodes);
    }

    /**
     * @return the lsit of measured nodes.
     */
    public List<Node> getMeasuredNodes() {
        return new ArrayList<Node>(this.measuredNodes);
    }

    /**
     * Returns the list of exogenous variableNodes.
     */
    public List<Node> getErrorNodes() {
        return new ArrayList<Node>(this.errorNodes);
    }

    /**
     * Returns the variable node for the given error node.
     * @param errorNode the error node.
     */
    public Node getVariableNode(Node errorNode) {
        int index = errorNodes.indexOf(errorNode);

        if (index == -1) {
            throw new NullPointerException(errorNode + " is not an error node in this model.");
        }

        return variableNodes.get(index);
    }

    /**
     * @return the error node for the given node.
     * @param node The variable node in question.
     */
    public Node getErrorNode(Node node) {
        if (errorNodes.contains(node)) {
            return node;
        }

        int index = variableNodes.indexOf(node);

        if (index == -1) {
            return null;
//            throw new NullPointerException(node + " is not a node in this model.");
        }

        return errorNodes.get(index);
    }

    /**
     * @return the variable with the given name, if there is one. Otherwise, null.
     * @param name the name of the parameter.
     */
    public Node getNode(String name) {
        for (Node node : nodes) {
            if (name.equals(node.getName())) {
                return node;
            }
        }

        return null;
    }

    /**
     * @return the set of nodes that reference a given parameter.
     * @param parameter The parameter in question.
     */
    public Set<Node> getReferencingNodes(String parameter) {
        Set<Node> set = this.referencedParameters.get(parameter);
        return set == null ? new HashSet<Node>() : new HashSet<Node>(set);
    }

    /**
     * @return the parameters referenced by the given variable (variable node or error node).
     * @param node the node doing the referencing.
     */
    public Set<String> getReferencedParameters(Node node) {
        Set<String> parameters = new HashSet<String>();

        for (String parameter : this.referencedParameters.keySet()) {
            if (this.referencedParameters.get(parameter).contains(node)) {
                parameters.add(parameter);
            }
        }

        return parameters;
    }

    /**
     * @return the set of nodes (variable or error) referenced by the expression for the given
     * node.
     * @param node the node doing the referencing.
     */
    public Set<Node> getReferencingNodes(Node node) {
        Set<Node> set = referencedNodes.get(node);
        return set == null ? new HashSet<Node>() : new HashSet<Node>(set);
    }

    /**
     * @return the variables referenced by the expression for the given node (variable node or
     * error node.
     * @param node the node doing the referencing.
     */
    public Set<Node> getReferencedNodes(Node node) {
        Set<Node> nodes = new HashSet<Node>();

        for (Node _node : this.referencedNodes.keySet()) {
            if (this.referencedNodes.get(_node).contains(node)) {
                nodes.add(_node);
            }
        }

        return nodes;
    }

    /**
     * Given base <b> (a String), returns the first name in the sequence "<b>1",
     * "<b>2", "<b>3", etc., which is not already the name of a node in the
     * workbench.
     *
     * @param base the base string.
     * @param usedNames A further list of parameter names to avoid.
     * @return the first string in the sequence not already being used.
     */
    public String nextParameterName(String base, List<String> usedNames) {
        if (getGraph().getNode(base) != null) {
            throw new IllegalArgumentException(base + " is a variable name.");
        }

        // Names should start with "1."
        int i = 0;

        loop:
        while (true) {
            String name = base + (++i);

            for (String parameter : referencedParameters.keySet()) {
                if (parameter.equals(name)) {
                    continue loop;
                }
            }

            for (String parameter : usedNames) {
                if (parameter.equals(name)) {
                    continue loop;
                }
            }

            break;
        }

        return base + i;
    }

    /**
     * @return all parents of the given node, with error node(s?) last.
     * @param node the given node, variable or error.
     */
    public List<Node> getParents(Node node) {
        List<Node> parents = this.graph.getParents(node);
        parents = putErrorNodesLast(parents);
        return new ArrayList<Node>(parents);
    }

    /**
     * Returns a relatively brief String representation of this SEM PM--the equations and distributions
     * of the model. Initial value distributions for parameters are not printed.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("\nEquations:\n");

        for (Node node : variableNodes) {
            buf.append("\n").append(node).append(" = ").append(nodeExpressionStrings.get(node));
        }

        buf.append("\n\nDistributions:\n");

        for (Node node : errorNodes) {
            buf.append("\n").append(node).append(" ~ ").append(nodeExpressionStrings.get(node));
        }

        return buf.toString();
    }

    //============================PRIVATE METHODS======================//

    /**
     * @param node A node in the graph.
     * @return The non-error parents of <code>node</code>.
     */
    private List<Node> getVariableParents(Node node) {
        List<Node> allParents = this.graph.getParents(node);
        List<Node> parents = new LinkedList<Node>();

        for (Node _parent : allParents) {
            if (_parent.getNodeType() != NodeType.ERROR) {
                parents.add(_parent);
            }
        }
        return parents;
    }

    private String getVariableString(List<Node> parents) {
        StringBuilder buf = new StringBuilder();

        // Putting error nodes last. (Allowing multiple error nodes for debugging purposes; doesn't hurt.)
        List<Node> sortedNodes = putErrorNodesLast(parents);

        for (int i = 0; i < sortedNodes.size(); i++) {
            Node node = sortedNodes.get(i);
            buf.append(node.getName());

            if (i < sortedNodes.size() - 1) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

    private List<Node> putErrorNodesLast(List<Node> parents) {
        List<Node> sortedNodes = new LinkedList<Node>();

        for (Node node : parents) {
            if (node.getNodeType() != NodeType.ERROR) {
                sortedNodes.add(node);
            }
        }

        for (Node node : parents) {
            if (node.getNodeType() == NodeType.ERROR) {
                sortedNodes.add(node);
            }
        }

        return sortedNodes;
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

    }

    public String getVariablesTemplate() {
        return variablesTemplate;
    }

    public void setVariablesTemplate(String variablesTemplate) throws ParseException {
        if (variablesTemplate == null) {
            throw new NullPointerException();
        }

        // Test to make sure it's parsable.
        ExpressionParser parser = new ExpressionParser();
        parser.parseExpression(variablesTemplate);

        this.variablesTemplate = variablesTemplate;
    }

    public String getErrorsTemplate() {
        return errorsTemplate;
    }

    public void setErrorsTemplate(String errorsTemplate) throws ParseException {
        if (errorsTemplate == null) {
            throw new NullPointerException();
        }

        // Test to make sure it's parsable.
        ExpressionParser parser = new ExpressionParser();
        parser.parseExpression(errorsTemplate);

        this.errorsTemplate = errorsTemplate;
    }

    public String getParametersTemplate() {
        return this.parametersTemplate;
    }

    public void setParametersTemplate(String parametersTemplate) throws ParseException {
        if (parametersTemplate == null) {
            throw new NullPointerException();
        }

        // Test to make sure it's parsable.
        ExpressionParser parser = new ExpressionParser();
        parser.parseExpression(parametersTemplate);

        this.parametersTemplate = parametersTemplate;
    }

    public void setStartsWithParametersTemplate(String startsWith, String parametersTemplate) throws ParseException {
        if (startsWith == null || startsWith.isEmpty()) {
            return;
        }

        if (parametersTemplate == null) {
            throw new NullPointerException();
        }

        // Test to make sure it's parsable.
        ExpressionParser parser = new ExpressionParser();
        parser.parseExpression(parametersTemplate);

        if (startsWith.contains(" ")) {
            throw new IllegalArgumentException("Starts with string contains spaces.");
        }

//        this.parametersTemplate = parametersTemplate;

        this.startsWithParametersTemplates.put(startsWith, parametersTemplate);
    }

    public String getStartsWithParameterTemplate(String startsWith) {
        return startsWithParametersTemplates.get(startsWith);
    }

    public Set<String> startsWithPrefixes() {
        return startsWithParametersTemplates.keySet();
    }
}
