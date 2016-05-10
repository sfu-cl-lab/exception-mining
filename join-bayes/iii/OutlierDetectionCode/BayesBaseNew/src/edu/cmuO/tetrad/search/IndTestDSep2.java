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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;


/**
 * Checks independence facts for variables associated with the nodes in a given
 * graph by checking d-separation facts on the underlying nodes.
 *
 * @author Joseph Ramsey
 */
public class IndTestDSep2 implements IndependenceTest {

    /**
     * The graph for which this is a variable map.
     *
     * @serial
     */
    private Graph graph;

    /**
     * The map from nodes to variables.
     *
     * @serial
     */
    private Map<Node, Node> nodesToVariables;

    /**
     * The map from variables to nodes.
     *
     * @serial
     */
    private Map<Node, Node> variablesToNodes;

    /**
     * The list of observed variables (i.e. variables for observed nodes).
     *
     * @serial
     */
    private List<Node> observedVars;

    /**
     * Constructs a new independence test that returns d-separation facts for
     * the given graph as independence results.
     */
    public IndTestDSep2(Graph graph) {
        if (graph == null) {
            throw new NullPointerException();
        }

        this.graph = graph;
        this.nodesToVariables = new HashMap<Node, Node>();
        this.variablesToNodes = new HashMap<Node, Node>();
        List<Node> nodes = graph.getNodes();

        for (Node node : nodes) {
            this.nodesToVariables.put(node, node);
            this.variablesToNodes.put(node, node);
        }

        this.observedVars = calcObservedVars(graph);
    }

    /**
     * Required by IndependenceTest.
     */
    @Override
	public IndependenceTest indTestSubset(List<Node> vars) {
        if (vars.isEmpty()) {
            throw new IllegalArgumentException("Subset may not be empty.");
        }

        for (Node var : vars) {
            if (!getVariables().contains(var)) {
                throw new IllegalArgumentException(
                        "All vars must be original vars");
            }
        }

        return this;
    }

    /**
     * Returns the list of observed nodes in the given graph.
     */
    private List<Node> calcObservedVars(Graph graph) {
        List<Node> observedVars = new ArrayList<Node>();

        for (Node node : graph.getNodes()) {
            if (node.getNodeType() == NodeType.MEASURED) {
                observedVars.add(getVariable(node));
            }
        }

        return observedVars;
    }

    /**
     * Checks the indicated d-separation fact.
     *
     * @param x one node.
     * @param y a second node.
     * @param z a List of nodes (conditioning variables)
     * @return true iff x _||_ y | z
     */
    @Override
	public boolean isIndependent(Node x, Node y, List<Node> z) {
        if (z == null) {
            throw new NullPointerException();
        }

        for (Node node : z) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        Node nodex = getNode(x);
        Node nodey = getNode(y);

        List<Node> nodesz = new ArrayList<Node>();
        for (Node aZ : z) {
            nodesz.add(getNode(aZ));
        }

        boolean dSeparated = getGraph().isDSeparatedFrom(nodex, nodey, nodesz);

        if (dSeparated) {
            double pValue = -1.0;
            TetradLogger.getInstance().independenceDetails(SearchLogUtils.independenceFactMsg(x, y, z, pValue));
        }

        return dSeparated;
    }

    @Override
	public boolean isDependent(Node x, Node y, List<Node> z) {
        return !isIndependent(x, y, z);
    }

    /**
     * Auxiliary method to calculate dseparation facts directly from nodes
     * instead of from variables.
     */
    public boolean isDSeparated(Node x, Node y, List<Node> z) {
        if (z == null) {
            throw new NullPointerException();
        }

        for (Node aZ : z) {
            if (aZ == null) {
                throw new NullPointerException();
            }
        }

        List<Node> nodesz = new ArrayList<Node>();

        for (Node node : z) {
            nodesz.add(node);
        }

        return getGraph().isDSeparatedFrom(x, y, nodesz);
    }

    /**
     * Needed for IndependenceTest interface. P value is not meaningful
     * here.
     */
    @Override
	public double getPValue() {
        return Double.NaN;
    }

    /**
     * Returns the list of TetradNodes over which this independence checker is
     * capable of determinine independence relations-- that is, all the
     * variables in the given graph or the given data set.
     */
    @Override
	public List<Node> getVariables() {
        return Collections.unmodifiableList(observedVars);
    }

    /**
     * Returns the list of variable varNames.
     */
    @Override
	public List<String> getVariableNames() {
        List<Node> nodes = getVariables();
        List<String> nodeNames = new ArrayList<String>();
        for (Node var : nodes) {
            nodeNames.add(var.getName());
        }
        return nodeNames;
    }

    @Override
	public boolean determines(List z, Node x1) {
        return z.contains(x1);
    }

    @Override
	public boolean splitDetermines(List<Node> z, Node x, Node y) {
        return z.contains(x) || z.contains(y);
    }

    @Override
	public double getAlpha() {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setAlpha(double alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Node getVariable(String name) {
        for (int i = 0; i < getVariables().size(); i++) {
            Node variable = getVariables().get(i);

            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    /**
     * Returns the underlying graph.
     */
    public Graph getGraph() {
        return this.graph;
    }

    /**
     * Returns the variable associated with the given node in the graph.
     */
    public Node getVariable(Node node) {
        return nodesToVariables.get(node);
    }

    /**
     * Returns the node associated with the given variable in the graph.
     */
    public Node getNode(Node variable) {
        return variablesToNodes.get(variable);
    }

    @Override
	public String toString() {
        return "D-separation";
    }

    @Override
	public RectangularDataSet getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}