package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Implements the Koller-Sahami algorithm for estimating a Markov blanket
 * for a variable in a data set, as defined in section 3 of Koller and
 * Sahami, Toward Optimal Feature Selection.
 *
 * @author Joseph Ramsey
 */
public class KollerSahami implements MarkovBlanketSearch {

    /**
     * The list of variables being searched over. Must contain the target.
     */
    private List<Node> variables;

    /**
     * Constructs a new search.
     */
    public KollerSahami(RectangularDataSet dataSet) {
        this.variables = dataSet.getVariables();
    }

    @Override
	public List<Node> findMb(String targetName) {
        return null;
    }

    @Override
	public String getAlgorithmName() {
        return "Koller-Sahami";
    }

    @Override
	public int getNumIndependenceTests() {
        return 0;
    }

    private Node getVariableForName(String targetName) {
        Node target = null;

        for (Node V : variables) {
            if (V.getName().equals(targetName)) {
                target = V;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset: " + targetName);
        }

        return target;
    }
}
