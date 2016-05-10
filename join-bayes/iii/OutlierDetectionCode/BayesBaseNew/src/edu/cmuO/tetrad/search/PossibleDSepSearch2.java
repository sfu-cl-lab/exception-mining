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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class implements the Possible-D-Sep search step of Spirtes, et al's
 * (1993) FCI algorithm (pp 144-145). Specifically, the methods in this class
 * perform step D. of the algorithm. </p> The algorithm implemented by this
 * class is a bit broader, however, because it allows for the possibility that
 * some pairs of variables have already been compared by a different algorithm.
 * Specifically, if the <code>prevCheck</code> variable is provided in the
 * constructor, then the algorithm pairwise checks every variable in the graph
 * with every variable in V \ <code>prevCheck</code> (that is, the unchecked
 * variables). This feature is used by the CIVI algorithm of Danks's "Efficient
 * Inclusion of Novel Variables."
 *
 * @author David Danks
 * @version $Revision: 5757 $; Mar 20, 2002
 */
final class PossibleDSepSearch2 {
    private Graph graph;
    private IndependenceTest test;
    private List<Node> nodes;

    /**
     * This sepset collects up only the sepsets discovered by this search.
     */
    private SepsetMap sepset;
    private int depth = -1;
    private LegalPairs legalPairs;

    /**
     * The background knowledge.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * @param graph   The GaSearchGraph on which to work
     * @param test The IndependenceChecker to use as an oracle
     */
    public PossibleDSepSearch2(Graph graph, IndependenceTest test) {
        if (graph == null) {
            throw new NullPointerException("null GaSearchGraph passed in " +
                    "PossibleDSepSearch constructor!");
        }
        if (test == null) {
            throw new NullPointerException("null IndependenceChecker passed " +
                    "in PossibleDSepSearch " + "constructor!");
        }

        this.graph = graph;
        this.test = test;
        this.nodes = new LinkedList<Node>(this.graph.getNodes());
        this.sepset = new SepsetMap();
        this.legalPairs = new FciDsepLegalPairs(this.graph);
    }

    /**
     * Performs pairwise comparisons of each variable in the graph with the
     * variables that have not already been checked. We get the Possible-D-Sep
     * sets for the pair of variables, and we check to see if they are
     * independent conditional on some subset of the union of Possible-D-Sep
     * sets. This method returns the SepSet passed in the constructor (if any),
     * possibly augmented by some edge removals in this step. The GaSearchGraph
     * passed in the constructor is directly changed.
     */
    public final SepsetMap search() {
        for (int i = 0; i < nodes.size(); i++) {
            Node node1 = nodes.get(i);

            List<Node> adj = graph.getAdjacentNodes(node1);

            // Remove the variables that we've already looked at.
            for (int j = 0; j < i; j++) {
                adj.remove(nodes.get(j));
            }

            // now we need to iterate through adj
            for (Node node2 : adj) {

                // now get the two Possible-D-Sep sets
                boolean removed = tryRemovingUsingDsep(node1, node2);

                if (!removed) {
                    tryRemovingUsingDsep(node2, node1);
                }
            }
        }

        return sepset;
    }

    private boolean tryRemovingUsingDsep(Node node1, Node node2) {
        List<Node> possDsep = new LinkedList<Node>(getPossibleDsep(node1, node2));

        boolean noEdgeRequired =
                getKnowledge().noEdgeRequired(node1.getName(), node2.getName());

        // Added this in accordance with the algorithm spec.
        // jdramsey 1/8/04
        possDsep.remove(node1);
        possDsep.remove(node2);

        List<Node> possibleParents = possibleParents(node1, possDsep, getKnowledge());
        int _depth = possibleParents.size();

        if (getDepth() != -1 && _depth > getDepth()) {
            _depth = getDepth();
        }

        for (int num = 1; num <= _depth; num++) {
            ChoiceGenerator cg =
                    new ChoiceGenerator(possibleParents.size(), num);
            int[] indSet;

            while ((indSet = cg.next()) != null) {
                List<Node> condSet = SearchGraphUtils.asList(indSet, possibleParents);

                boolean independent =
                        test.isIndependent(node1, node2, condSet);

                if (independent && noEdgeRequired) {
                    graph.removeEdge(node1, node2);
                    List<Node> z = new LinkedList<Node>(condSet);
                    sepset.set(node1, node2, z);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Removes from the list of nodes any that cannot be parents of x given the
     * background knowledge.
     */
    private List<Node> possibleParents(Node x, List<Node> nodes, Knowledge knowledge) {
        List<Node> possibleParents = new LinkedList<Node>();
        String _x = x.getName();

        for (Node z : nodes) {
            String _z = z.getName();

            if (possibleParentOf(_z, _x, knowledge)) {
                possibleParents.add(z);
            }
        }

        return possibleParents;
    }

    private static boolean possibleParentOf(String _z, String _x, Knowledge bk) {
        return !(bk.edgeForbidden(_z, _x) || bk.edgeRequired(_x, _z));
    }

    /**
     * A variable V is in Possible-D-Sep(A,B) iff
     * <pre>
     * 	(i) V != A & V != B
     * 	(ii) there is an undirected path U between A and V such that for every
     * 		 subpath <X,Y,Z> of U either:
     * 		(a) Y is a collider on the subpath, or
     * 		(b) X is adjacent to Z.
     * </pre>
     */
    private Set<Node> getPossibleDsep(Node node1, Node node2) {
        List<Node> initialNodes = Collections.singletonList(node1);
        List<Node> c = null;
        List<Node> d = null;

        Set<Node> reachable = SearchGraphUtils.getReachableNodes(initialNodes,
                legalPairs, c, d, graph);

        reachable.remove(node1);
        reachable.remove(node2);

        TetradLogger.getInstance().details(
                "Possible-D-Sep(" + node1 + ", " + node2 + ") = " + reachable);

        return reachable;
    }

    private int getDepth() {
        return depth;
    }

    public final void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0: " + depth);
        }

        this.depth = depth;
    }

    private Knowledge getKnowledge() {
        return knowledge;
    }

    public final void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }
}


