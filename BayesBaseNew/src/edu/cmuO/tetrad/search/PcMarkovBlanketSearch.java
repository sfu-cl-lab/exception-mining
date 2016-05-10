package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Estimates the Markov blanket by first running PC on the data and then
 * graphically extracting the MB DAG.
 *
 * @author Joseph Ramsey
 */
public class PcMarkovBlanketSearch implements MarkovBlanketSearch {
    private IndependenceTest test;
    private int depth;

    public PcMarkovBlanketSearch(IndependenceTest test, int depth) {
        this.test = test;
        this.depth = depth;
    }

    @Override
	public List<Node> findMb(String targetName) {
        Node target = getVariableForName(targetName);

        PcSearch search = new PcSearch(test, new Knowledge());
        search.setDepth(depth);
        Graph graph = search.search();
        MbUtils.trimToMbNodes(graph, target, false);
        List<Node> mbVariables = graph.getNodes();
        mbVariables.remove(target);

        return mbVariables;
    }

    @Override
	public String getAlgorithmName() {
        return "PCMB";
    }

    @Override
	public int getNumIndependenceTests() {
        return 0;
    }

    private Node getVariableForName(String targetVariableName) {
        Node target = null;

        for (Node V : test.getVariables()) {
            if (V.getName().equals(targetVariableName)) {
                target = V;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset: " + targetVariableName);
        }

        return target;
    }

}
