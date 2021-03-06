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

package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.List;

/**
 * Calculates updated structural equation models given evidence of the form
 * X1=x1',...,The main task of such and algorithm is to calculate P(X = x' |
 * evidence), where evidence takes the form of a Proposition over the variables
 * in the Bayes net, possibly with additional information about which variables
 * in the Bayes net have been manipulated.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6379 $ $Date: 2005-12-06 13:51:09 -0500 (Tue, 06 Dec
 *          2005) $
 * @see edu.cmu.tetrad.bayes.Evidence
 * @see edu.cmu.tetrad.bayes.Proposition
 * @see edu.cmu.tetrad.bayes.Manipulation
 */
public class SemUpdater implements TetradSerializable {
    static final long serialVersionUID = 23L;
    private SemEvidence evidence;
    private SemIm semIm;

    public SemUpdater(SemIm semIm) {
        if (semIm == null) {
            throw new NullPointerException();
        }

        setEvidence(new SemEvidence(semIm));
        this.semIm = semIm;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemUpdater serializableInstance() {
        return new SemUpdater(SemIm.serializableInstance());
    }

    public SemEvidence getEvidence() {
        return this.evidence;
    }

    /**
     * Sets new evidence for the updater. Once this is called, old updating
     * results should not longer be available.
     */
    public void setEvidence(SemEvidence evidence) {
        if (evidence == null) {
            throw new NullPointerException();
        }

        this.evidence = evidence;
    }

    /**
     * Returns the Bayes instantiated model that is being updated.
     */
    public SemIm getSemIm() {
        return this.semIm;
    }

    /**
     * See http://en.wikipedia.org/wiki/Multivariate_normal_distribution.
     */
    public SemIm getUpdatedSemIm() {
        Algebra algebra = new Algebra();

        // First manipulate the old semIm.
        SemIm manipulatedSemIm = getManipulatedSemIm();

        // Get out the means and implied covariances.
        double[] means = new double[manipulatedSemIm.getVariableNodes().size()];

        for (int i = 0; i < means.length; i++) {
            means[i] = manipulatedSemIm.getMean(manipulatedSemIm.getVariableNodes().get(i));
        }

        DoubleMatrix1D mu = new DenseDoubleMatrix1D(means);
        DoubleMatrix2D sigma = manipulatedSemIm.getImplCovar();

        // Updating on x2 = a.
        SemEvidence evidence = getEvidence();
        List nodesInEvidence = evidence.getNodesInEvidence();

        int[] x2 = new int[nodesInEvidence.size()];
        DoubleMatrix1D a = new DenseDoubleMatrix1D(nodesInEvidence.size());

        for (int i = 0; i < nodesInEvidence.size(); i++) {
            Node _node = (Node) nodesInEvidence.get(i);
            x2[i] = evidence.getNodeIndex(_node);
        }

        for (int i = 0; i < nodesInEvidence.size(); i++) {
            int j = evidence.getNodeIndex((Node) nodesInEvidence.get(i));
            a.set(i, evidence.getProposition().getValue(j));
        }

        // x1 is all the variables.
//        int[] x1 = new int[sigma.rows() - x2.length];
        int[] x1 = new int[sigma.rows()];

        for (int i = 0; i < sigma.rows(); i++) {
            x1[i] = i;
        }

//        int index = -1;
//        for (int i = 0; i < sigma.rows(); i++) {
//            if (Arrays.binarySearch(x2, i) == -1) {
//                x1[++index] = i;
//            }
//        }

        // Calculate sigmaBar. (Don't know how to use it yet.)
//        DoubleMatrix2D sigma11 = sigma.viewSelection(x1, x1);
        DoubleMatrix2D sigma12 = sigma.viewSelection(x1, x2);
        DoubleMatrix2D sigma22 = sigma.viewSelection(x2, x2);
//        DoubleMatrix2D sigma21 = sigma.viewSelection(x2, x1);
        DoubleMatrix2D inv_sigma22 = algebra.inverse(sigma22);
        DoubleMatrix2D temp1 = algebra.mult(sigma12, inv_sigma22);
//        DoubleMatrix2D temp2 = algebra.mult(temp1, sigma21.copy());
//        DoubleMatrix2D sigmaBar = sigma11.copy().assign(temp2, Functions.minus);

        // Calculate muBar.
        DoubleMatrix1D mu1 = mu.viewSelection(x1);
        DoubleMatrix1D mu2 = mu.viewSelection(x2);
        DoubleMatrix1D temp4 = a.copy().assign(mu2, Functions.minus);
        DoubleMatrix1D temp5 = algebra.mult(temp1, temp4);
        DoubleMatrix1D muBar = mu1.copy().assign(temp5, Functions.plus);

        // Estimate a SEM with this sigmaBar and muBar.
//        List variableNodes = manipulatedSemIm.getVariableNodes();
//        String[] varNames = new String[variableNodes.size()];
//
//        for (int i = 0; i < variableNodes.size(); i++) {
//            varNames[i] = ((Node) variableNodes.get(i)).getName();
//        }

//        System.out.println(sigmaBar);
//
//        CovarianceMatrix covMatrix = new CovarianceMatrix(varNames,
//                sigmaBar, 100);
//        SemPm semPm = manipulatedSemIm.getSemPm();
//        SemEstimator estimator = new SemEstimator(covMatrix, semPm);
//        estimator.estimate();
//        SemIm semIm = estimator.getEstimatedSem();
//        semIm.setMeanValues(muBar.toArray());
//        return semIm;

        DoubleMatrix2D sigma2 = manipulatedSemIm.getErrCovar();

//        for (int aX2 : x2) {
//            for (int j = 0; j < sigma2.columns(); j++) {
//                sigma2.set(aX2, j, 0.d);
//            }
//        }
//
//        for (int i = 0; i < sigma2.rows(); i++) {
//            for (int aX2 : x2) {
//                sigma2.set(i, aX2, 0.d);
//                sigma2.set(aX2, i, 0.d);
//            }
//        }

        System.out.println("Restricted sigma: " + sigma2);

        return manipulatedSemIm.updatedIm(sigma2, muBar);
    }

    public Graph getManipulatedGraph() {
        return createManipulatedGraph(getSemIm().getSemPm().getGraph());
    }

    public SemIm getManipulatedSemIm() {
        SemGraph graph = getSemIm().getSemPm().getGraph();
        SemGraph manipulatedGraph = createManipulatedGraph(graph);
        return SemIm.retainValues(getSemIm(), manipulatedGraph);
    }

    /**
     * Alters the graph by removing edges from parents to manipulated
     * variables.
     */
    private SemGraph createManipulatedGraph(Graph graph) {
        SemGraph updatedGraph = new SemGraph(graph);

        for (int i = 0; i < evidence.getNumNodes(); ++i) {
            if (evidence.isManipulated(i)) {
                Node node = evidence.getNode(i);
                List<Node> parents = updatedGraph.getParents(node);

                for (Node parent : parents) {
                    if (parent.getNodeType() == NodeType.ERROR) {
                        continue;
                    }

                    updatedGraph.removeEdge(node, parent);
                }
            }
        }

        return updatedGraph;
    }
}


