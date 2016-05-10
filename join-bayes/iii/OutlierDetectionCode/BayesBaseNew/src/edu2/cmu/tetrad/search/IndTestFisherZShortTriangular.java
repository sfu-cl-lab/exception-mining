///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.util.*;

import java.text.NumberFormat;
import java.util.*;

/**
 * Checks conditional independence of variable in a continuous data set using Fisher's Z test. See Spirtes, Glymour, and
 * Scheines, "Causation, Prediction and Search," 2nd edition, page 94.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted IndTestCramerT for Fisher's Z
 */
public final class IndTestFisherZShortTriangular implements IndependenceTest {

    /**
     * The covariance matrix.
     */
    private final ShortTriangularMatrix covMatrix;

    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private List<Node> variables;

    /**
     * The significance level of the independence tests.
     */
    private double alpha;

    /**
     * The value of the Fisher's Z statistic associated with the las calculated partial correlation.
     */
    private double fisherZ;

    /**
     * The FisherZD independence test, used when Fisher Z throws an exception (i.e., when there's a collinearity).
     */
    private IndTestFisherZGeneralizedInverse deterministicTest;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * Stores a reference to the dataset being analyzed.
     */
    private DataSet dataSet;

    /**
     * A stored p value, if the deterministic test was used.
     */
    private double pValue = Double.NaN;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based on the correlation matrix implied by the
     * given data set (must be continuous). The given significance level is used.
     *
     * @param dataSet A data set containing only continuous columns.
     * @param alpha   The alpha level of the test.
     */
    public IndTestFisherZShortTriangular(DataSet dataSet, double alpha) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.covMatrix = new ShortTriangularMatrix(dataSet.getNumColumns());
        this.covMatrix.becomeCorrelationMatrix(dataSet);
        this.variables = dataSet.getVariables();
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZGeneralizedInverse(dataSet, alpha);
        this.dataSet = dataSet;
    }

    /**
     * Constructs a new Fisher Z independence test with the listed arguments.
     *
     * @param data      A 2D continuous data set with no missing values.
     * @param variables A list of variables, a subset of the variables of <code>data</code>.
     * @param alpha     The significance cutoff level. p values less than alpha will be reported as dependent.
     */
    public IndTestFisherZShortTriangular(DoubleMatrix2D data, List<Node> variables, double alpha) {
        DataSet dataSet = ColtDataSet.makeContinuousData(variables, data);
        this.covMatrix = new ShortTriangularMatrix(dataSet.getNumColumns());
        this.covMatrix.becomeCorrelationMatrix(dataSet);
        this.variables = dataSet.getVariables();
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZGeneralizedInverse(dataSet, alpha);
    }

//    /**
//     * Constructs a new independence test that will determine conditional
//     * independence facts using the given correlation matrix and the given
//     * significance level.
//     */
//    public IndTestFisherZShortTriangular(CovarianceMatrix corrMatrix, double alpha) {
//        this.covMatrix = corrMatrix;
//        this.variables = Collections.unmodifiableList(corrMatrix.getVariables());
//        setAlpha(alpha);
//    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new IndTestCramerT instance for a subset of the variables.
     */
    public IndependenceTest indTestSubset(List<Node> vars) {
//        if (vars.isEmpty()) {
//            throw new IllegalArgumentException("Subset may not be empty.");
//        }
//
//        for (Node ar : vars) {
//            if (!variables.contains(ar)) {
//                throw new IllegalArgumentException(
//                        "All vars must be original vars");
//            }
//        }
//
//        int[] indices = new int[vars.size()];
//
//        for (int i = 0; i < indices.length; i++) {
//            indices[i] = variables.indexOf(vars.get(i));
//        }
//
//        CovarianceMatrix newCovMatrix = getSubmatrix(indices);
//
//        double alphaNew = getAlpha();
//        return new IndTestFisherZShortTriangular(newCovMatrix, alphaNew);

        throw new UnsupportedOperationException();
    }

    /**
     * Determines whether variable x is independent of variable y given a list of conditioning variables z.
     *
     * @param x the one variable being compared.
     * @param y the second variable being compared.
     * @param z the list of conditioning variables.
     * @return true iff x _||_ y | z.
     * @throws RuntimeException if a matrix singularity is encountered.
     */
    public boolean isIndependent(Node x, Node y, List<Node> z) {
        DoubleMatrix2D submatrix = subMatrix(x, y, z);
        double r = 0;

        try {
            r = StatUtils.partialCorrelation(submatrix);

            if (Double.isNaN((r)) || r < -1. || r > 1.) throw new RuntimeException();
        } catch (Exception e) {
            DepthChoiceGenerator gen = new DepthChoiceGenerator(z.size(), z.size());
            int[] choice;

            while ((choice = gen.next()) != null) {
                try {
                    List<Node> z2 = new ArrayList<Node>(z);
                    z2.removeAll(GraphUtils.asList(choice, z));
                    submatrix = subMatrix(x, y, z2);
                    r = StatUtils.partialCorrelation(submatrix);
                } catch (Exception e2) {
                    continue;
                }

//                if (Double.isNaN(r)) continue;
//
//                if (r > 1.) r = 1.;
//                 if (r < -1.) r = -1.;

                if (Double.isNaN(r) || r < -1. || r > 1.) continue;

                break;
            }
        }

        // Either dividing by a zero standard deviation (in which case it's dependent) or doing a regression
        // (effectively) with a multicolliarity
        if (Double.isNaN(r)) {
            int[] _z = new int[z.size()];
//            for (int i = 0; i < _z.length; i++) _z[i] = i + 2;
//
////            double varx = StatUtils.partialVariance(submatrix, 0, _z); // submatrix.get(0, 0);
////            double vary = StatUtils.partialVariance(submatrix, 1, _z); //submatrix.get(1, 1);
//
//            double varx = submatrix.get(0, 0);
//            double vary = submatrix.get(1, 1);
//
//            if (varx * vary == 0) {
            return true;
//            }
        }

        if (r > 1.) r = 1.;
        if (r < -1.) r = -1.;

        this.fisherZ = Math.sqrt(sampleSize() - z.size() - 3.0) *
                0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));

        if (Double.isNaN(this.fisherZ)) {
            throw new IllegalArgumentException("The Fisher's Z " +
                    "score for independence fact " + x + " _||_ " + y + " | " +
                    z + " is undefined. r = " + r);
        }

        boolean independent = getPValue() > alpha;

        if (independent) {
            TetradLogger.getInstance().log("independencies",
                    SearchLogUtils.independenceFactMsg(x, y, z, getPValue()));
        } else {
            TetradLogger.getInstance().log("dependencies",
                    SearchLogUtils.dependenceFactMsg(x, y, z, getPValue()));
        }

        return independent;
    }

    private DoubleMatrix2D subMatrix(Node x, Node y, List<Node> z) {
        int dim = z.size() + 2;
        int[] indices = new int[dim];
        indices[0] = variables.indexOf(x);
        indices[1] = variables.indexOf(y);
        for (int k = 0; k < z.size(); k++) {
            indices[k + 2] = variables.indexOf(z.get(k));
        }

        DoubleMatrix2D submatrix = new DenseDoubleMatrix2D(dim, dim);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                int i1 = indices[i];
                int i2 = indices[j];
                submatrix.set(i, j, covMatrix.getDouble(i1, i2));
            }
        }
        return submatrix;
    }

    public boolean isIndependent(Node x, Node y, Node... z) {
        return isIndependent(x, y, Arrays.asList(z));
    }

    public boolean isDependent(Node x, Node y, List<Node> z) {
        return !isIndependent(x, y, z);
    }

    public boolean isDependent(Node x, Node y, Node... z) {
        List<Node> zList = Arrays.asList(z);
        return isDependent(x, y, zList);
    }

    /**
     * Returns the probability associated with the most recently computed independence test.
     */
    public double getPValue() {
        if (!Double.isNaN(this.pValue)) {
            return Double.NaN;
        } else {
            return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));
        }
    }

    /**
     * Sets the significance level at which independence judgments should be made.  Affects the cutoff for partial
     * correlations to be considered statistically equal to zero.
     */
    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance out of range.");
        }

        this.alpha = alpha;
//        this.thresh = Double.NaN;
    }

    /**
     * Gets the current significance level.
     */
    public double getAlpha() {
        return this.alpha;
    }

    /**
     * Returns the list of variables over which this independence checker is capable of determinine independence
     * relations-- that is, all the variables in the given graph or the given data set.
     */
    public List<Node> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable with the given name.
     */
    public Node getVariable(String name) {
        for (int i = 0; i < getVariables().size(); i++) {
            Node variable = getVariables().get(i);
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    /**
     * Returns the list of variable varNames.
     */
    public List<String> getVariableNames() {
        List<Node> variables = getVariables();
        List<String> variableNames = new ArrayList<String>();
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    /**
     * If <code>isDeterminismAllowed()</code>, deters to IndTestFisherZD; otherwise throws
     * UnsupportedOperationException.
     */
    public boolean determines(List<Node> z, Node x) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
//        int[] parents = new int[z.size()];
//
//        for (int j = 0; j < parents.length; j++) {
//            parents[j] = covMatrix.getVariables().indexOf(z.get(j));
//        }
//
//        int i = covMatrix.getVariables().indexOf(x);
//
//        DoubleMatrix2D matrix2D = covMatrix.getMatrix();
//        double variance = matrix2D.get(i, i);
//
//        if (parents.length > 0) {
//
//            // Regress z onto i, yielding regression coefficients b.
//            DoubleMatrix2D Czz =
//                   matrix2D.viewSelection(parents, parents);
//            DoubleMatrix2D inverse;
//            try {
//                inverse = new Algebra().inverse(Czz);
////                inverse = MatrixUtils.ginverse(Czz);
//            }
//            catch (Exception e) {
//                return true;
//            }
//
//            DoubleMatrix1D Cyz = matrix2D.viewColumn(i);
//            Cyz = Cyz.viewSelection(parents);
//            DoubleMatrix1D b = new Algebra().mult(inverse, Cyz);
//
//            variance -= new Algebra().mult(Cyz, b);
//        }
//
//        return variance < 0.01;
    }

    /**
     * Returns the data set being analyzed.
     */
    public DataSet getData() {
        return dataSet;
    }

    public void shuffleVariables() {
        List<Node> nodes = new ArrayList(this.variables);
        Collections.shuffle(nodes);
        this.variables = Collections.unmodifiableList(nodes);
    }

    /**
     * Returns a string representation of this test.
     */
    public String toString() {
        return "Fisher's Z, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

    /**
     * Computes that value x such that P(abs(N(0,1) > x) < alpha.  Note that this is a two sided test of the null
     * hypothesis that the Fisher's Z value, which is distributed as N(0,1) is not equal to 0.0.
     */
    private double cutoffGaussian(double alpha) {
        double upperTail = 1.0 - alpha / 2.0;
        double epsilon = 1e-14;

        // Find an upper bound.
        double lowerBound = -1.0;
        double upperBound = 0.0;

        while (ProbUtils.normalCdf(upperBound) < upperTail) {
            lowerBound += 1.0;
            upperBound += 1.0;
        }

        while (upperBound >= lowerBound + epsilon) {
            double midPoint = lowerBound + (upperBound - lowerBound) / 2.0;

            if (ProbUtils.normalCdf(midPoint) <= upperTail) {
                lowerBound = midPoint;
            } else {
                upperBound = midPoint;
            }
        }

        return lowerBound;
    }

    private int sampleSize() {
        return dataSet.getNumRows();
    }

//    private CovarianceMatrix covMatrix() {
//        return covMatrix;
//    }
}
