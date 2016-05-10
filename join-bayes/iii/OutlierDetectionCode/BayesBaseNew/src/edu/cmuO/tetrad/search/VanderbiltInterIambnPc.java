package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 26, 2006 Time: 10:29:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class VanderbiltInterIambnPc implements MarkovBlanketSearch {

    /**
     * The independence test used to perform the search.
     */
    private IndependenceTest independenceTest;

    /**
     * The list of variables being searched over. Must contain the target.
     */
    private List<Node> variables;

    /**
     * Constructs a new search.
     *
     * @param test The source of conditional independence information for the
     *                search.
     */
    public VanderbiltInterIambnPc(IndependenceTest test) {
        if (test == null) {
            throw new NullPointerException();
        }

        this.independenceTest = test;
        this.variables = test.getVariables();
    }

    @Override
	public List<Node> findMb(String targetName) {
        Node target = getVariableForName(targetName);
        List<Node> cmb = new LinkedList<Node>();
        PcSearch pc = new PcSearch(independenceTest, new Knowledge());
        Set<Node> usedVars = new HashSet<Node>();
        boolean cont = true;

        // Forward phase.
        while (cont) {
            cont = false;

            List<Node> remaining = new LinkedList<Node>(variables);
            remaining.removeAll(cmb);
            remaining.remove(target);

            double strength = Double.NEGATIVE_INFINITY;
            Node f = null;

            for (Node v : remaining) {
                if (v == target) {
                    continue;
                }

                if (usedVars.contains(v)) {
                    continue;
                }

                double _strength = associationStrength(v, target, cmb);

                if (_strength > strength) {
                    strength = _strength;
                    f = v;
                }
            }

            if (f == null) {
                break;
            }

            if (!independenceTest.isIndependent(f, target, cmb)) {
                cmb.add(f);
                cont = true;
            }

            // Backward phase.
            cmb.add(target);
            Graph graph = pc.search(cmb);
            cmb.remove(target);

            MbUtils.trimToMbNodes(graph, target, false);

//            System.out.println(graph);

            List<Node> newCmb = graph.getNodes();
            newCmb.remove(target);

            List<Node> diff = new LinkedList<Node>(cmb);
            diff.removeAll(newCmb);
            usedVars.addAll(diff);

//            System.out.println("target = " + target + " cmb = " + cmb + " newCmb = " + newCmb
//                    + " diff = " + diff);
//            System.out.println("usedVars = " + usedVars);

            cmb = newCmb;
        }

        return cmb;
    }

    private double associationStrength(Node v, Node target, List<Node> cmb) {
        independenceTest.isIndependent(v, target, cmb);
        return 1.0 - independenceTest.getPValue();
    }

    @Override
	public String getAlgorithmName() {
        return "InterIAMBnPC";
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
