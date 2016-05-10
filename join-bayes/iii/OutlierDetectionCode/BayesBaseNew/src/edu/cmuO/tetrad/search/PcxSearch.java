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
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * @author Frank Wimberly
 */
public class PcxSearch {
    private IndependenceTest test;
    private String targetVariableName;
    private List<Node> listOfVariables;
    private int depth;
    //private Variable W;    //Target variable

    private Graph markovBlanket;

    public PcxSearch(IndependenceTest test, int depth) {
        if (test == null) {
            throw new NullPointerException();
        }

        this.test = test;
        this.depth = depth;
        this.listOfVariables = test.getVariables();
    }

    public Graph search(String targetVariableName) {
        this.targetVariableName = targetVariableName;

        if (targetVariableName == null) {
            throw new IllegalArgumentException(
                    "Null target name not permitted");
        }

        Node w = null;

        for (Node v : listOfVariables) {
            if (v.getName().equals(targetVariableName)) {
                w = v;
                break;
            }
        }

        if (w == null) {
            throw new IllegalArgumentException(
                    "Target variable not in dataset.");
        }

        //DEBUG Print:
        TetradLogger.getInstance().details("target = " + targetVariableName + " w = " + w);

        TetradLogger.getInstance().details("Will use Ramsey procedure.");

        List<Node> associated = findRamseyAssociatedVars(w);
        if (!associated.contains(w)) {
            associated.add(w);
        }

        //DEBUG Print stmts
        TetradLogger.getInstance().details("Variables ouput by findRamsey--used in PC Search");

        for (Node V : associated) {
            TetradLogger.getInstance().details("" + V);
        }


        TetradLogger.getInstance().details("Size of associated = " + associated.size());

        IndependenceTest indAssociated = test.indTestSubset(associated);

        //Step 4.
        //Run a PC search on this smaller dataset and store the resulting
        //EndpointMatrixGraph (a pattern) in associatedPattern.
        TetradLogger.getInstance().details("Entering step 4");
        Knowledge bk = new Knowledge();
        PcSearch pcAssociated = new PcSearch(indAssociated, bk);

        //        pcAssociated.addObserver(new BasicSearchObserver());
        //        pcAssociated.setDepth(4);  //Debug Xue's dd1_100.txt case
        if (depth > 0) {
            pcAssociated.setDepth(depth); //Debug Xue's dd1_100.txt case
        }

        Graph associatedPattern = pcAssociated.search();

        //Graph associatedPattern = new EndpointMatrixGraph(assocPattern);

        //Debug print:
        TetradLogger.getInstance().details("Pattern produced by PC algorithm");
        TetradLogger.getInstance().details("" + associatedPattern);

        //Step 5.
        //For each double-headed edge X<-->w and each undirected edge
        //X--w, replace the edge with w-->X.
        TetradLogger.getInstance().details("\nEntering step 5.");

        List<Node> adjW = associatedPattern.getAdjacentNodes(w);

        for (Node X : adjW) {

            //DEBUG Print
            //LogUtils.getInstance().fine("Considering the edge between " + w + " and " + X);

            if ((associatedPattern.getEndpoint(w, X) == Endpoint.ARROW &&
                    associatedPattern.getEndpoint(X, w) == Endpoint.ARROW) || (
                    associatedPattern.getEndpoint(w, X) == Endpoint.TAIL &&
                            associatedPattern.getEndpoint(X, w) == Endpoint
                                    .TAIL)) {
                associatedPattern.setEndpoint(w, X, Endpoint.ARROW);
                associatedPattern.setEndpoint(X, w, Endpoint.TAIL);
                TetradLogger.getInstance().details("Setting edge " + w + "-->" + X);  //DEBUG
            }
        }

        TetradLogger.getInstance().details("\nEntering Step 6."); //DEBUG

        //Step 6.
        //For each X which is a parent of w:
        List<Node> parentsOfW = associatedPattern.getParents(w);

        for (Node X : parentsOfW) {
            //X is a parent of w.
            List<Node> adjX = associatedPattern.getAdjacentNodes(X);

            for (Node Y : adjX) {
                if (Y != w) {
                    associatedPattern.removeEdge(Y, X);
                }
            }
        }

        //Step 7.
        //For all edges that are doubly directed or undirected either orient them or
        //remove them depending on whether they involve children of w.
        TetradLogger.getInstance().details("\nEntering Step 7.");

        List<Edge> allEdges = associatedPattern.getEdges();

        for (Edge edge : allEdges) {
            Endpoint fromEndpoint = edge.getEndpoint1();
            Endpoint toEndpoint = edge.getEndpoint2();

            Node fromNode = edge.getNode1();
            Node toNode = edge.getNode2();

            if ((fromEndpoint == Endpoint.ARROW &&
                    toEndpoint == Endpoint.ARROW) || (
                    fromEndpoint == Endpoint.TAIL &&
                            toEndpoint == Endpoint.TAIL)) {

                if (associatedPattern.isChildOf(fromNode, w) &&
                        !associatedPattern.isChildOf(toNode, w)) {
                    //if(childrenOfW.contains(fromNode) && !childrenOfW.contains(toNode)) {
                    associatedPattern.setEndpoint(toNode, fromNode,
                            Endpoint.ARROW);
                    associatedPattern.setEndpoint(fromNode, toNode,
                            Endpoint.TAIL);
                } else if (associatedPattern.isChildOf(toNode, w) &&
                        !associatedPattern.isChildOf(fromNode, w)) {
                    //if(childrenOfW.contains(toNode) && !childrenOfW.contains(fromNode)) {
                    associatedPattern.setEndpoint(fromNode, toNode,
                            Endpoint.ARROW);
                    associatedPattern.setEndpoint(toNode, fromNode,
                            Endpoint.TAIL);
                } else {
                    associatedPattern.removeEdge(fromNode, toNode);
                }
            }
        }

        //Recompute childrenOfW and parentsOfW here?
        List<Node> childrenOfW = associatedPattern.getChildren(w);
        parentsOfW = associatedPattern.getParents(w);

        //Step 8.
        TetradLogger.getInstance().details("\nEntering Step 8.");
        Set<Node> remaining = new HashSet<Node>();
        remaining.add(w);
        //List parentOfChildOfW = null;
        //List parentOfChildOfW = new LinkedList();

        //Include parents and children of w.
        for (Node parent : parentsOfW) {
            remaining.add(parent);
        }

        for (Node child : childrenOfW) {
            remaining.add(child);
        }

        for (Node child : childrenOfW) {
            List<Node> parChild = associatedPattern.getAdjacentNodes(child);

            for (Node parentOfChild : parChild) {

                //Include parents of children of w.
                remaining.add(parentOfChild);
            }
        }


        if (!remaining.contains(w)) {
            //remaining.add(w);
            throw new IllegalArgumentException("Target missing from MB");
        }

        TetradLogger.getInstance().details("After Step 8 there are " + remaining.size() +
                " variables remaining.");

        List<Node> remainingList = new LinkedList<Node>(remaining);

        //Create a subgraph containing the remaining variables.

        this.markovBlanket = associatedPattern.subgraph(remainingList);

        //Step 9 version suggested by Xue
        //Delete all edges into parents of w.
        TetradLogger.getInstance().details("\nEntering Step 9.");

        List<Node> parents = markovBlanket.getParents(w);

        for (Node parent : parents) {
            List<Node> parPar = markovBlanket.getParents(parent);

            for (Node parentPar : parPar) {
                markovBlanket.removeEdge(parentPar, parent);
            }

        }

        //Then delete all edges out of children of w.
        List<Node> children = markovBlanket.getChildren(w);

        for (Node child : children) {
            List<Node> childChild = markovBlanket.getChildren(child);

            for (Node childCh : childChild) {
                markovBlanket.removeEdge(childCh, child);
            }

        }

        //Step 10.  Delete edges between parents of children of the target.
        TetradLogger.getInstance().details("\nEntering Step 10.");

        childrenOfW = markovBlanket.getChildren(w);
        List<Node> parentsOfChildren = new LinkedList<Node>();

        for (Node child : childrenOfW) {
            List<Node> parChild = markovBlanket.getParents(child);

            for (Node parentOfChild : parChild) {
                if (parentOfChild == w) {
                    continue;  //Don't include w.
                }
                parentsOfChildren.add(parentOfChild);
            }
        }

        for (int i = 0; i < parentsOfChildren.size(); i++) {
            Node childParent1 = parentsOfChildren.get(i);
            for (int j = 0; j < parentsOfChildren.size(); j++) {
                if (i == j) {
                    continue;
                }
                Node childParent2 = parentsOfChildren.get(j);
                markovBlanket.removeEdge(childParent1, childParent2);
            }
        }

        //        //Step 11.  The Glymour designated "hack".
        //        parents = markovBlanket.getParents(w);   //Might have changed in Step 9.
        //        if(parents.size() >= 8) {
        //            VariableScorePair[] vsp = new VariableScorePair[parents.size()];
        //            int numberElements = 0;        //Index of the vsp array as it is filled.
        //            //LogUtils.getInstance().fine("Associated with " + w + " before sort.");  //Debug print
        //            for (Iterator it = parents.iterator(); it.hasNext();) {
        //                Variable par = (Variable) it.next();
        //                if (par == w) continue;
        //
        //                boolean ind = (!test.isIndependent(w, par, Collections.EMPTY_LIST));
        //                //double score = ((IndTestChiSquare) test).getXSquare();
        //                double score = ((IndTestChiSquare) test).getPValue();
        //                //double score = ((IndTestGSquare2) test).getPValue();
        //                int index = parents.indexOf(par);
        //                vsp[numberElements] = new VariableScorePair(par, score, index);
        //                //LogUtils.getInstance().fine(numberElements + " " + par + " " + score);     //Debug print
        //                numberElements++;
        //
        //            }
        //
        //            Arrays.sort(vsp, 0, numberElements);
        //
        //            //Debug print:
        //            LogUtils.getInstance().fine("Associated with " + w + " in order from strongest to weakes assoc.");
        //
        //            for (int i = 0; i < numberElements; i++) {
        //                LogUtils.getInstance().fine("H1" + vsp[i].getVariable());
        //                LogUtils.getInstance().fine(" " + vsp[i].getBic());
        //            }
        //
        //
        //            for(int i = 5; i < numberElements; i++) {
        //                markovBlanket.removeEdge((Variable) vsp[i], w);
        //            }
        //        }

        //Delete nodes which have no adjacent nodes.  Not necessary?
        for (Node R : remainingList) {
            if (markovBlanket.getEdges(R).isEmpty()) {
                markovBlanket.removeNode(R);
            }
        }

        TetradLogger.getInstance().details("DEBUG: Markov Blanket after Step 10");
        TetradLogger.getInstance().details("" + markovBlanket);

        return markovBlanket;
    }

    /**
     * Uses the Ramsey procedure implemented in procedure 1 to select a set of
     * variables associated with the target variable.  That set is used in
     * calling the search method of PcSearch to produce a pattern which is used
     * in the PCX algorithm.
     *
     * @param target
     * @return a list of variables associated with the target variable.
     */
    public List<Node> findRamseyAssociatedVars(Node target) {
        List<Node> union = new ArrayList<Node>();

        //LogUtils.getInstance().fine("Calling procedure 1 with " + targetVariableName);
        List<Node> vOfT = procedure1(target, listOfVariables);

        for (Node v : vOfT) {
            if (!union.contains(v)) {
                union.add(v);
            }
        }

        for (Node x : vOfT) {
            if (x.equals(target)) {
                continue;
            }
            //LogUtils.getInstance().fine("Calling procedure 1 with " + x.getName());
            List<Node> vOfX = procedure1(x, listOfVariables);

            for (Node v : vOfX) {
                if (!union.contains(v)) {
                    union.add(v);
                }
            }

        }

        //if(!union.contains(W)) union.add(W);    //Probably not necessary.
        return union;
    }

    /**
     * Efficiently reduces the set of variables related to the target variable.
     * First it removes variables which are conditionally independent of the
     * target given one other variable.  Then it removes variables which are
     * independent of the target given a pair of other variables.
     *
     * @param target
     * @return the list of variables which are not removed.
     */
    public List<Node> procedure1(Node target, List<Node> variables) {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Null target name not permitted");
        }

        Node w = null;

        for (Node v : variables) {
            if (v.equals(target)) {
                w = v;
                break;
            }
        }

        //DEBUG Print:
        //LogUtils.getInstance().fine("target = " + targetVariable + " w = " + w);

        if (w == null) {
            throw new IllegalArgumentException(
                    "Target is not in list of variables");
        }

        //Find the variables associated with w and put them in the array
        //vsp which will be sorted by strength of association with w.

        VariableScorePair[] vsp = new VariableScorePair[variables.size()];

        int numberElements = 0;        //Index of the vsp array as it is filled.
        //LogUtils.getInstance().fine("Associated with " + w + " before sort.");  //Debug print
        for (Node isAssoc : variables) {
            if (isAssoc == w) {
                continue;
            }

            if (!test.isIndependent(w, isAssoc, new LinkedList<Node>())) {
                double score = test.getPValue();
                int index = variables.indexOf(isAssoc);
                vsp[numberElements] =
                        new VariableScorePair(isAssoc, score, index);
                numberElements++;
            }
        }

        Arrays.sort(vsp, 0, numberElements);

        List<Node> V = new ArrayList<Node>();
        for (int i = 0; i < numberElements; i++) {
            V.add(vsp[i].getVariable());
        }

        //Debug print:
        /*
        LogUtils.getInstance().fine("Associated with " + w + " in order.");

        for (int i = 0; i < numberElements; i++) {
            LogUtils.getInstance().fine("D1" + vsp[i].getVariable());
            LogUtils.getInstance().fine(" " + vsp[i].getBic());
        }
        */

        for (int i = 0; i < numberElements; i++) {

            boolean removed = false;
            for (int j = numberElements - 1; j >= 0; j--) {
                if (i == j) {
                    continue;
                }
                List<Node> condVar = new ArrayList<Node>();
                condVar.add(vsp[j].getVariable());
                if (!V.contains(vsp[j].getVariable())) {
                    continue;
                }
                if (test.isIndependent(vsp[i].getVariable(), w, condVar)) {
                    V.remove(vsp[i].getVariable());
                    removed = true;
                    break;
                }
            }
            if (removed) {
                break;
            }
        }

        /*Debug prints:
        LogUtils.getInstance().fine("V(T) After elimination of first order independencies");
        //int currentSizeOfV = V.size();
        for (int i = 0; i < V.size(); i++)
            LogUtils.getInstance().fine(((Variable) V.get(i)).getName());
        */

        if (V.size() > 2) {
            for (int i = 0; i < V.size(); i++) {
                Node isAssoc;
                boolean removed = false;
                isAssoc = V.get(i);

                //Now generate pairs of the variables remaining in V using those with
                //highest association with the target first.
                //Remove variables from V which are independent of target conditioned on
                //those pairs

                //The next method of ChoiceGenerator returns a sequence
                //of numberElements choose n instances (stored as arrays)
                Node var1, var2;

                ChoiceGenerator cg;
                cg = new ChoiceGenerator(V.size(), 2);
                //cg = new ChoiceGenerator(numberElements, 2);
                int[] indices;

                //The ArrayList instance s will contain, in turn, each subset
                //of size 2 of the list V.
                //LogUtils.getInstance().fine("Number of elements of V = " + V.size());
                while ((indices = cg.next()) != null) {
                    //LogUtils.getInstance().fine("indices of 0,1 = " + indices[0] + indices[1]);
                    //var1 = (Variable) V.get(numberElements - indices[0] - 1);
                    //var2 = (Variable) V.get(numberElements - indices[1] - 1);
                    var1 = V.get(V.size() - indices[0] - 1);
                    var2 = V.get(V.size() - indices[1] - 1);

                    //LogUtils.getInstance().fine("first = " + var1.getName() + " second = " + var2.getName());
                    if (var1 == w || var2 == w || var1 == isAssoc ||
                            var2 == isAssoc) {
                        continue;  //Probably not necessary.
                    }

                    List<Node> condVar = new LinkedList<Node>();
                    condVar.add(var1);
                    condVar.add(var2);

                    if (test.isIndependent(isAssoc, w, condVar)) {
                        V.remove(isAssoc);
                        removed = true;
                        break;
                    }
                }
                if (removed) {
                    break;
                }
            }
        }

        /* Debug prints
        LogUtils.getInstance().fine("V(T) After elimination of second order independencies (if any)");
        for (int i = 0; i < V.size(); i++)
            LogUtils.getInstance().fine(((Variable) V.get(i)).getName());
        */

        return V;

    }

    public Graph getMarkovBlanket() {
        return markovBlanket;
    }

    public String getTargetVariableName() {
        return targetVariableName;
    }

    /**
     * Inner class used by search11.  Members of this class can be stored in
     * arrays and be sorted by score, which is usually computed by the
     * GSquareTest class.
     */
    private static class VariableScorePair implements Comparable {
        private Node v;   //A variable or pair of variables.
        private double score;   //The score associated with this set.
        private int index;

        public VariableScorePair(Node v, double score, int index) {
            this.v = v;
            this.score = score;
            this.index = index;
        }

        public Node getVariable() {
            return v;
        }

        public double getScore() {
            return score;
        }

        public int getIndex() {
            return index;
        }

        @Override
		public int compareTo(Object other) {
            if (getScore() < ((VariableScorePair) other).getScore()) {
                return 1;
            }
            else if (getScore() == ((VariableScorePair) other).getScore()) {
                return 0;
            }
            else {
                return -1;
            }
        }
    }
}


