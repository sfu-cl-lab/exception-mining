package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 26, 2006 Time: 10:29:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class VanderbiltIamb implements MarkovBlanketSearch {

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
    public VanderbiltIamb(IndependenceTest test) {
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
        }

        // Backward phase.

        for (Node f : new LinkedList<Node>(cmb)) {
            cmb.remove(f);

            if (independenceTest.isIndependent(f, target, cmb)) {
                continue;
            }

            cmb.add(f);
        }

//        boolean changed = true;
//
//        while (changed) {
//            changed = false;
//
//            for (Node node : new LinkedList<Node>(cmb)) {
//                cmb.remove(node);
//
//                if (independenceTest.isIndependent(node, target, cmb)) {
//                    changed = true;
//                    continue;
//                }
//
//                cmb.add(node);
//            }
//        }

        return cmb;
    }

    private double associationStrength(Node v, Node target, List<Node> cmb) {
        independenceTest.isIndependent(v, target, cmb);
        return 1.0 - independenceTest.getPValue();
    }

    @Override
	public String getAlgorithmName() {
        return "IAMB";
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
