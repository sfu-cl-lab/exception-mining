package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the PC ("Peter/Clark") algorithm, as specified in Chapter 6 of
 * Spirtes, Glymour, and Scheines, "Causation, Prediction, and Search," 2nd
 * edition, with a modified rule set in step D due to Chris Meek. For the
 * modified rule set, see Chris Meek (1995), "Causal inference and causal
 * explanation with background knowledge."
 *
 * @author Joseph Ramsey (this version).
 */
public class VanderbiltMmhc implements GraphSearch {

    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * The maximum number of nodes conditioned on in the search.
     */
    private int depth = Integer.MAX_VALUE;
    private RectangularDataSet data;
    private Knowledge knowledge = new Knowledge();

    //=============================CONSTRUCTORS==========================//

    public VanderbiltMmhc(IndependenceTest test, int depth) {
        this.depth = depth;
        this.independenceTest = test;
        this.data = test.getData();
    }

    //==============================PUBLIC METHODS========================//


    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public int getDepth() {
        return depth;
    }

    @Override
	public long getElapsedTime() {
        return 0;
    }

    /**
     * Runs PC starting with a fully connected graph over all of the variables
     * in the domain of the independence test.
     */
    @Override
	public Graph search() {
        List<Node> variables = independenceTest.getVariables();
        VanderbiltMmmb mmmb = new VanderbiltMmmb(independenceTest, depth, true);
        Map<Node, List<Node>> pc = new HashMap<Node, List<Node>>();

        for (Node x : variables) {
            pc.put(x, mmmb.getPc(x));
        }

        Graph graph = new EdgeListGraph();

        for (Node x : variables) {
            graph.addNode(x);
        }

        for (Node x : variables) {
            for (Node y : pc.get(x)) {
                if (!graph.isAdjacentTo(x, y)) {
//                    if (knowledge.edgeForbidden(x.getName(), y.getName()) ||
//                            knowledge.edgeForbidden(y.getName(), x.getName())) {
//                        continue;
//                    }

                    graph.addUndirectedEdge(x, y);
                }
            }
        }

        GesOrienter orienter = new GesOrienter(data, getKnowledge());

        orienter.orient(graph);

        return graph;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }
}
