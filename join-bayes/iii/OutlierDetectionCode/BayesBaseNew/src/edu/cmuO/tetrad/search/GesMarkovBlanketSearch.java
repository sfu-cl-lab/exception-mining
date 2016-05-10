package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Estimates the Markov blanket by first running GES on the data and then
 * graphically extracting the MB DAG.
 *
 * @author Joseph Ramsey
 */
public class GesMarkovBlanketSearch implements MarkovBlanketSearch {
    private RectangularDataSet dataSet;

    public GesMarkovBlanketSearch(RectangularDataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
	public List<Node> findMb(String targetName) {
        Node target = getVariableForName(targetName);

        GesSearch search = new GesSearch(dataSet);
        Graph graph = search.search();
        MbUtils.trimToMbNodes(graph, target, false);
        List<Node> mbVariables = graph.getNodes();
        mbVariables.remove(target);

        return mbVariables;
    }

    @Override
	public String getAlgorithmName() {
        return "GESMB";
    }

    @Override
	public int getNumIndependenceTests() {
        return 0;
    }

    private Node getVariableForName(String targetVariableName) {
        Node target = null;

        for (Node V : dataSet.getVariables()) {
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
