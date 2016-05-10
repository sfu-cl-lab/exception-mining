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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.PersistentRandomUtil;
import edu.cmu.tetrad.util.RandomUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.List;

/**
 * Calculates updated marginals for a Bayes net by simulating data and
 * calculating likelihood ratios. The method is as follows. For P(A | B), enough
 * sample points are simulated from the underlying BayesIm so that 1000 satisfy
 * the condition B. Then the maximum likelihood estimate of condition A is
 * calculated.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-09 09:38:16 -0500 (Mon, 09 Jan
 *          2006) $
 */
public final class ApproximateUpdater implements ManipulatingBayesUpdater {
    static final long serialVersionUID = 23L;

    /**
     * The IM which this updater modifies.
     *
     * @serial Cannot be null.
     */
    private BayesIm bayesIm;

    /**
     * Stores evidence for all variables.
     *
     * @serial Cannot be null.
     */
    private Evidence evidence;

    /**
     * Counts of data points for each variable value.
     *
     * @serial
     */
    private int[][] counts;

    /**
     * This is the source BayesIm after manipulation; all data simulations
     * should be taken from this.
     *
     * @serial
     */
    private BayesIm manipulatedBayesIm;

    //==============================CONSTRUCTORS===========================//

    public ApproximateUpdater(BayesIm bayesIm) {
        if (bayesIm == null) {
            throw new NullPointerException();
        }

        this.bayesIm = bayesIm;
        setEvidence(Evidence.tautology(bayesIm));
    }

    /**
     * Constructs a new updater for the given Bayes net.
     */
    public ApproximateUpdater(BayesIm bayesIm, Evidence evidence) {
        if (bayesIm == null) {
            throw new NullPointerException();
        }

        this.bayesIm = bayesIm;
        setEvidence(evidence);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ApproximateUpdater serializableInstance() {
        return new ApproximateUpdater(MlBayesIm.serializableInstance());
    }

    //============================PUBLIC METHODS==========================//

    /**
     * Returns the Bayes instantiated model that is being updated.
     */
    @Override
	public BayesIm getBayesIm() {
        return bayesIm;
    }

    /**
     * Returns the Bayes instantiated model after manipulations have been
     * applied.
     */
    @Override
	public BayesIm getManipulatedBayesIm() {
        return this.manipulatedBayesIm;
    }

    /**
     * Returns the graph for getManipulatedBayesIm().
     */
    @Override
	public Graph getManipulatedGraph() {
        return this.manipulatedBayesIm.getDag();
    }

    /**
     * Returns the updated Bayes IM, or null if there is no updated Bayes IM.
     */
    @Override
	public BayesIm getUpdatedBayesIm() {
        return null;
    }

    /**
     * Returns a copy of the current evidence.
     */
    @Override
	public Evidence getEvidence() {
        return new Evidence(this.evidence);
    }

    /**
     * Sets new evidence for the next update operation.
     */
    @Override
	public final void setEvidence(Evidence evidence) {
        if (evidence == null) {
            throw new NullPointerException();
        }

        if (!evidence.isCompatibleWith(bayesIm)) {
            throw new IllegalArgumentException("The variables for the given " +
                    "evidence must be compatible with the Bayes IM being updated.");
        }

        this.evidence = new Evidence(evidence);

        Dag graph = bayesIm.getBayesPm().getDag();
        Dag manipulatedGraph = createManipulatedGraph(graph);
        BayesPm manipulatedBayesPm = createUpdatedBayesPm(manipulatedGraph);
        this.manipulatedBayesIm = createdUpdatedBayesIm(manipulatedBayesPm);

        this.counts = null;
    }

    @Override
	public double getMarginal(int variable, int value) {
        doUpdate();
        int sum = 0;

        for (int i = 0; i < manipulatedBayesIm.getNumColumns(variable); i++) {
            sum += counts[variable][i];
        }

        return counts[variable][value] / (double) sum;
    }

    @Override
	public boolean isJointMarginalSupported() {
        return false;
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
	public double getJointMarginal(int[] variables, int[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
	public double[] calculatePriorMarginals(int nodeIndex) {
        Evidence evidence = getEvidence();
        setEvidence(Evidence.tautology(evidence.getVariableSource()));

        double[] marginals = new double[evidence.getNumCategories(nodeIndex)];

        for (int i = 0;
                i < getBayesIm().getNumColumns(nodeIndex); i++) {
            marginals[i] = getMarginal(nodeIndex, i);
        }

        setEvidence(evidence);
        return marginals;
    }

    @Override
	public double[] calculateUpdatedMarginals(int nodeIndex) {
        double[] marginals = new double[evidence.getNumCategories(nodeIndex)];

        for (int i = 0;
                i < getBayesIm().getNumColumns(nodeIndex); i++) {
            marginals[i] = getMarginal(nodeIndex, i);
        }

        return marginals;
    }

    /**
     * Prints out the most recent marginal.
     */
    @Override
	public String toString() {
        return "Approximate updater, evidence = " + evidence;
    }

    //==============================PRIVATE METHODS=======================//

    private void doUpdate() {
        if (counts != null) {
            return;
        }

        this.counts = new int[manipulatedBayesIm.getNumNodes()][];

        for (int i = 0; i < manipulatedBayesIm.getNumNodes(); i++) {
            this.counts[i] = new int[manipulatedBayesIm.getNumColumns(i)];
        }

        // Get a tier ordering and convert it to an int array.
        Graph graph = getManipulatedGraph();
        Dag dag = (Dag) graph;
        List<Node> tierOrdering = dag.getTierOrdering();
        int[] tiers = new int[tierOrdering.size()];

        for (int i = 0; i < tierOrdering.size(); i++) {
            tiers[i] =
                    getManipulatedBayesIm().getNodeIndex(tierOrdering.get(i));
        }

        int numCounted = 0;
        
        //Elwin added the following to avoid the infinite loop while evidence prob = 0
        
        int hit = 0;

        // Construct the sample.
        while (numCounted < 1000) {
        	hit++;
        	
        	if (hit>1000&&numCounted==0) break;
        	
            int[] point = getSinglePoint(getManipulatedBayesIm(), tiers);

            if (evidence.getProposition().isPermissibleCombination(point)) {
                numCounted++;

                for (int j = 0; j < getManipulatedBayesIm().getNumNodes(); j++)
                {
                    counts[j][point[j]]++;
                }
            }
        }
    }

    private BayesIm createdUpdatedBayesIm(BayesPm updatedBayesPm) {
        return new MlBayesIm(updatedBayesPm, bayesIm, MlBayesIm.RANDOM);
    }

    private BayesPm createUpdatedBayesPm(Dag updatedGraph) {
        return new BayesPm(updatedGraph, bayesIm.getBayesPm());
    }

    private Dag createManipulatedGraph(Graph graph) {
        Dag updatedGraph = new Dag(graph);

        // alters graph for manipulated evidenceItems
        for (int i = 0; i < evidence.getNumNodes(); ++i) {
            if (evidence.isManipulated(i)) {
                Node node = evidence.getNode(i);
                Collection<Node> parents = updatedGraph.getParents(node);

                for (Node parent1 : parents) {
                    updatedGraph.removeEdge(node, parent1);
                }
            }
        }

        return updatedGraph;
    }

    private static int[] getSinglePoint(BayesIm bayesIm, int[] tiers) {
        int[] point = new int[bayesIm.getNumNodes()];
        int[] combination = new int[bayesIm.getNumNodes()];
        RandomUtil randomUtil = PersistentRandomUtil.getInstance();

        for (int nodeIndex : tiers) {
            double cutoff = randomUtil.nextDouble();

            for (int k = 0; k < bayesIm.getNumParents(nodeIndex); k++) {
                combination[k] = point[bayesIm.getParent(nodeIndex, k)];
            }

            int rowIndex = bayesIm.getRowIndex(nodeIndex, combination);
            double sum = 0.0;

            for (int k = 0; k < bayesIm.getNumColumns(nodeIndex); k++) {
                double probability =
                        bayesIm.getProbability(nodeIndex, rowIndex, k);

                if (Double.isNaN(probability)) {
                    throw new IllegalStateException("Some probability " +
                            "values in the BayesIm are not filled in; " +
                            "cannot simulate data to do approximate updating.");
                }

                sum += probability;

                if (sum >= cutoff) {
                    point[nodeIndex] = k;
                    break;
                }
            }
        }

        return point;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (bayesIm == null) {
            throw new NullPointerException();
        }

        if (evidence == null) {
            throw new NullPointerException();
        }
    }
}

