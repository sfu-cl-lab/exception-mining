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

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.PersistentRandomUtil;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Creates a table of stored cell probabilities for the given list of
 * variables. Since for a moderate number of variables and for a moderate number
 * of values per variables this could get to be a very large table, it might not
 * be a good idea to use this class except for unit testing.</p>
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class StoredCellProbs implements DiscreteProbs {
    private List<Node> variables;
    private int[] parentDims;
    private double[] probs;

    //============================CONSTRUCTORS============================//

    private StoredCellProbs(List<Node> variables) {
        if (variables == null) {
            throw new NullPointerException();
        }

        for (Object variable : variables) {
            if (variable == null) {
                throw new NullPointerException();
            }

            if (!(variable instanceof DiscreteVariable)) {
                throw new IllegalArgumentException(
                        "Not a discrete variable: " + variable.getClass());
            }
        }

        this.variables = Collections.unmodifiableList(variables);
        Set<Object> variableSet = new HashSet<Object>(this.variables);
        if (variableSet.size() < this.variables.size()) {
            throw new IllegalArgumentException("Duplicate variable.");
        }

        this.parentDims = new int[getVariables().size()];

        for (int i = 0; i < getVariables().size(); i++) {
            DiscreteVariable var = (DiscreteVariable) getVariables().get(i);
            parentDims[i] = var.getNumCategories();
        }

        int numCells = 1;

        for (int parentDim : this.parentDims) {
            numCells *= parentDim;
        }

        this.probs = new double[numCells];
    }

    public static StoredCellProbs createRandomCellTable(List<Node> variables) {
        StoredCellProbs cellProbs = new StoredCellProbs(variables);

        double sum = 0.0;

        for (int i = 0; i < cellProbs.probs.length; i++) {
            double value = PersistentRandomUtil.getInstance().nextDouble();
            cellProbs.probs[i] = value;
            sum += value;
        }

        for (int i = 0; i < cellProbs.probs.length; i++) {
            cellProbs.probs[i] /= sum;
        }

        return cellProbs;
    }

    public static StoredCellProbs createCellTable(BayesIm bayesIm) {
        if (bayesIm == null) {
            throw new NullPointerException();
        }

        BayesImProbs cellProbsOnTheFly = new BayesImProbs(bayesIm);
        StoredCellProbs cellProbs =
                new StoredCellProbs(cellProbsOnTheFly.getVariables());

        for (int i = 0; i < cellProbs.probs.length; i++) {
            int[] variableValues = cellProbs.getVariableValues(i);
            double p = cellProbsOnTheFly.getCellProb(variableValues);
            cellProbs.setCellProbability(variableValues, p);
        }

        return cellProbs;
    }

    //=============================PUBLIC METHODS=========================//

    /**
     * Returns the probability for the given cell, specified as a particular
     * combination of variable values, for the list of variables (in order)
     * returned by get
     */
    @Override
	public double getCellProb(int[] variableValues) {
        return probs[getOffset(variableValues)];
    }

    @Override
	public double getProb(Proposition assertion) {

        // Initialize to 0's.
        int[] variableValues = new int[assertion.getNumVariables()];

        for (int i = 0; i < assertion.getNumVariables(); i++) {
            variableValues[i] = nextValue(assertion, i, -1);
        }

        variableValues[variableValues.length - 1] = -1;
        double p = 0.0;

        loop:
        while (true) {
            for (int i = assertion.getNumVariables() - 1; i >= 0; i--) {
                if (hasNextValue(assertion, i, variableValues[i])) {
                    variableValues[i] =
                            nextValue(assertion, i, variableValues[i]);

                    for (int j = i + 1; j < assertion.getNumVariables(); j++) {
                        if (hasNextValue(assertion, j, -1)) {
                            variableValues[j] = nextValue(assertion, j, -1);
                        }
                        else {
                            break loop;
                        }
                    }

                    p += getCellProb(variableValues);
                    continue loop;
                }
            }

            break;
        }

        return p;
    }

    private static boolean hasNextValue(Proposition proposition, int variable,
            int curIndex) {
        return nextValue(proposition, variable, curIndex) != -1;
    }

    private static int nextValue(Proposition proposition, int variable,
            int curIndex) {
        for (int i = curIndex + 1;
                i < proposition.getNumCategories(variable); i++) {
            if (proposition.isAllowed(variable, i)) {
                return i;
            }
        }

        return -1;
    }

    @Override
	public double getConditionalProb(Proposition assertion,
            Proposition condition) {
        if (assertion.getVariableSource() != condition.getVariableSource()) {
            throw new IllegalArgumentException(
                    "Assertion and condition must be " +
                            "for the same Bayes IM.");
        }

        // Initialize to 0's.
        int[] variableValues = new int[condition.getNumVariables()];

        for (int i = 0; i < condition.getNumVariables(); i++) {
            variableValues[i] = nextValue(condition, i, -1);
        }

        variableValues[variableValues.length - 1] = -1;
        double conditionTrue = 0.0;
        double assertionTrue = 0.0;

        loop:
        while (true) {
            for (int i = condition.getNumVariables() - 1; i >= 0; i--) {
                if (hasNextValue(condition, i, variableValues[i])) {
                    variableValues[i] =
                            nextValue(condition, i, variableValues[i]);

                    for (int j = i + 1; j < condition.getNumVariables(); j++) {
                        if (hasNextValue(condition, j, -1)) {
                            variableValues[j] = nextValue(condition, j, -1);
                        }
                        else {
                            break loop;
                        }
                    }

                    double cellProb = getCellProb(variableValues);
                    boolean assertionHolds = true;

                    for (int j = 0; j < assertion.getNumVariables(); j++) {
                        if (!assertion.isAllowed(j, variableValues[j])) {
                            assertionHolds = false;
                            break;
                        }
                    }

                    if (assertionHolds) {
                        assertionTrue += cellProb;
                    }

                    conditionTrue += cellProb;
                    continue loop;
                }
            }

            break;
        }

        return assertionTrue / conditionTrue;
    }

    @Override
	public boolean isMissingValueCaseFound() {
        return false;
    }

    @Override
	public List<Node> getVariables() {
        return this.variables;
    }

    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        buf.append("\nCell Probabilities:");

        buf.append("\n");

        for (Node variable : variables) {
            buf.append(variable).append("\t");
        }

        double sum = 0.0;
        int maxLines = 500;

        for (int i = 0; i < probs.length; i++) {
            if (i >= maxLines) {
                buf.append("\nCowardly refusing to print more than ")
                        .append(maxLines).append(" lines.");
                break;
            }

            buf.append("\n");

            int[] variableValues = getVariableValues(i);

            for (int variableValue : variableValues) {
                buf.append(variableValue).append("\t");
            }

            buf.append(nf.format(probs[i]));
            sum += probs[i];
        }

        buf.append("\n\nSum = ").append(nf.format(sum));

        return buf.toString();
    }

    //============================PRIVATE METHODS==========================//

    /**
     * Returns the row in the table at which the given combination of parent
     * values is represented for the given node.  The row is calculated as a
     * variable-base place-value number.  For instance, if the array of
     * parent dimensions is [3, 5, 7] and the parent value combination is [2,
     * 4, 5], then the row number is (7 * (5 * (3 * 0 + 2) + 4)) + 5 = 103. This
     * is the inverse function to getVariableValues().  <p> Note: If the node
     * has n values, the length of 'values' must be >= the number of parents.
     * Only the first n values are used.
     *
     * @return the row in the table for the given node and combination of parent
     *         values.
     */
    private int getOffset(int[] values) {
        int[] dim = getParentDims();
        int offset = 0;

        for (int i = 0; i < dim.length; i++) {
            if (values[i] < 0 || values[i] >= dim[i]) {
                throw new IllegalArgumentException();
            }

            offset *= dim[i];
            offset += values[i];
        }

        return offset;
    }

    private int[] getVariableValues(int rowIndex) {
        int[] dims = getParentDims();
        int[] values = new int[dims.length];

        for (int i = dims.length - 1; i >= 0; i--) {
            values[i] = rowIndex % dims[i];
            rowIndex /= dims[i];
        }

        return values;
    }

    /**
     * Returns an array containing the number of values, in order, of each
     * variable.
     */
    private int[] getParentDims() {
        return this.parentDims;
    }

    /**
     * Sets the cell probability. Should not be made public for now, since
     * there's no way to guarantee the probabilities will add to 1.0 if they're
     * set one at a time.
     */
    private void setCellProbability(int[] variableValues, double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "Probability not in [0.0, 1.0]: " + probability);
        }

        probs[getOffset(variableValues)] = probability;
    }
}


