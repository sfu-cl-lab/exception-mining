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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.random.Normal;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Checks independence of variable in a continuous data set using Fisher's Z
 * test. See Spirtes, Glymour, and Scheines, "Causation, Prediction and Search,"
 * 2nd edition, page 94.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted IndTestCramerT for Fisher's Z
 * @version $Revision: 6576 $ $Date: 2006-01-20 13:45:01 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class IndTestFisherZ implements IndependenceTest {

    /**
     * The correlation matrix.
     *
     * @serial
     */
    private final CovarianceMatrix covMatrix;

    /**
     * The variables of the correlation matrix, in order. (Unmodifiable list.)
     *
     * @serial
     */
    private final List<Node> variables;

    /**
     * The significance level of the independence tests.
     *
     * @serial
     */
    private double alpha;


    /**
     * The density function of a N(0,1) random variable (A Gaussian with mean 0,
     * sd 1.0)
     *
     * @serial
     */
    private NormalPdf npdf;

    /**
     * The cutoff value for 'alpha' area in the two tails of the partial
     * correlation distribution function.
     *
     * @serial
     */
    private double thresh = Double.NaN;

    /**
     * The value of the Fisher's Z statistic associated with the las
     * calculated partial correlation.
     *
     * @serial
     */
    private double fishersZ;

    /**
     * The FisherZD independence test, used when Fisher Z throws an exception
     * (i.e., when there's a collinearity).
     */
    private IndTestFisherZD deterministicTest;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
    private RectangularDataSet dataSet;

    /**
     * A stored p value, if the deterministic test was used.
     */
    private double pValue = Double.NaN;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based
     * on the correlation matrix implied by the given data set (must be
     * continuous). The given significance level is used.
     *
     * @param dataSet A data set containing only continuous columns.
     * @param alpha   The alpha level of the test.
     */
    public IndTestFisherZ(RectangularDataSet dataSet, double alpha) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.covMatrix = new CovarianceMatrix(dataSet);

        // This check fails for large data sets with few cases sometimes, even
        // though it's mostly OK to use it anyway. TODO.
//        if (!(MatrixUtils.isSymmetricPositiveDefinite(this.covMatrix.getMatrix()))) {
//            throw new IllegalArgumentException("Covariance matrix is not " +
//                    "symmetric positive definite.");
//        }

        this.variables = Collections.unmodifiableList(covMatrix.getVariables());
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZD(dataSet, alpha);
        this.dataSet = dataSet;
    }

    public IndTestFisherZ(DoubleMatrix2D data, List<Node> variables, double alpha) {
        RectangularDataSet dataSet = ColtDataSet.makeContinuousData(variables, data);
        this.covMatrix = new CovarianceMatrix(dataSet);
        this.variables = Collections.unmodifiableList(variables);
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZD(dataSet, alpha);
    }

    /**
     * Constructs a new independence test that will determine conditional
     * independence facts using the given correlation matrix and the given
     * significance level.
     */
    public IndTestFisherZ(CovarianceMatrix corrMatrix, double alpha) {
        this.covMatrix = corrMatrix;
        this.variables =
                Collections.unmodifiableList(corrMatrix.getVariables());
        setAlpha(alpha);
    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new IndTestCramerT instance for a subset of the variables.
     */
    @Override
	public IndependenceTest indTestSubset(List<Node> vars) {
        if (vars.isEmpty()) {
            throw new IllegalArgumentException("Subset may not be empty.");
        }

        for (Node var : vars) {
            if (!variables.contains(var)) {
                throw new IllegalArgumentException(
                        "All vars must be original vars");
            }
        }

        int[] indices = new int[vars.size()];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = variables.indexOf(vars.get(i));
        }

        CovarianceMatrix newCovMatrix = covMatrix.getSubmatrix(indices);

        double alphaNew = getAlpha();
        return new IndTestFisherZ(newCovMatrix, alphaNew);
    }

    /**
     * Determines whether variable x is independent of variable y given a list
     * of conditioning variables z.
     *
     * @param x the one variable being compared.
     * @param y the second variable being compared.
     * @param z the list of conditioning variables.
     * @return true iff x _||_ y | z.
     * @throws RuntimeException if a matrix singularity is encountered.
     */
    @Override
	public boolean isIndependent(Node x, Node y, List<Node> z) {
        if (z == null) {
            throw new NullPointerException();
        }

        for (Node nodes : z) {
            if (nodes == null) {
                throw new NullPointerException();
            }
        }

        this.pValue = Double.NaN;

//        System.out.println(z);

        // Precondition: this.variables, this.corrMatrix properly set up.
        //
        // Postcondition: this.r and this.func should be the
        // most recently calculated partial correlation and
        // partial correlation distribution function, respectively.
        //
        // PROCEDURE:
        // calculate the partial correlation of x and y given z
        // by finding the submatrix of variables x, y, z1...zn,
        // inverting it, and examining the value at position (0, 1)
        // in the inverted matrix.  the partial correlation is
        // -1 * this value / the square root of the outerProduct of the
        // diagonal elements on the same row and column as this
        // value.
        //
        // Design consideration:
        // Minimize object creation by reusing submatrix and condition
        // arrays and inverting submatrix in place.

        // Create index array for the given variables.
        int size = z.size() + 2;
        int[] indices = new int[size];

        indices[0] = getVariables().indexOf(x);
        indices[1] = getVariables().indexOf(y);

        for (int i = 0; i < z.size(); i++) {
            indices[i + 2] = getVariables().indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        DoubleMatrix2D submatrix =
                covMatrix().getMatrix().viewSelection(indices, indices);

        // Check for missing values.
        if (DataUtils.containsMissingValue(submatrix)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        // Invert submatrix.
        if (new Algebra().rank(submatrix) != submatrix.rows()) {
            boolean independent = deterministicTest.isIndependent(x, y, z);
            this.pValue = deterministicTest.getPValue();
            return independent;
        }

        submatrix = MatrixUtils.inverseC(submatrix);

        double a = -1.0 * submatrix.get(0, 1);
        double v0 = submatrix.get(0, 0);
        double v1 = submatrix.get(1, 1);
        double b = Math.sqrt(v0 * v1);
        double r = a / b;

        this.fishersZ = 0.5 * Math.sqrt(sampleSize() - z.size() - 3.0) *
                Math.log(Math.abs(1.0 + r) / Math.abs(1.0 - r));

        if (Double.isNaN(this.fishersZ)) {
            throw new IllegalArgumentException("The Fisher's Z " +
                    "score for independence fact " + x + " _||_ " + y + " | " +
                    z + " is undefined.");
        }

        boolean indFisher = true;

        if (Double.isNaN(thresh)) {
            this.thresh = cutoffGaussian();
        }

        //System.out.println("thresh = " + thresh);
        if (Math.abs(fishersZ) > thresh) {
            indFisher = false;  //Two sided
        }

        if (indFisher) {
            TetradLogger.getInstance().independenceDetails(SearchLogUtils.independenceFactMsg(x, y, z, getPValue()));
        }

        return indFisher;
    }

    @Override
	public boolean isDependent(Node x, Node y, List<Node> z) {
        return !isIndependent(x, y, z);
    }

    /**
     * Returns the probability associated with the most recently computed
     * independence test.
     */
    @Override
	public double getPValue() {
        if (!Double.isNaN(this.pValue)) {
            return this.pValue;
        }
        else {
            Normal normal = new Normal(0, 1, PersistentRandomUtil.getInstance().getRandomEngine());
            return 2.0 * (1.0 - normal.cdf(Math.abs(fishersZ)));
        }

//        double q = 2.0 * Integrator.getArea(npdf, 0.0, Math.abs(fishersZ), 100);
//
//        if (q > 1.0) {
//            q = 1.0;
//        }
//
//        return 1.0 - q;
        //        return 2.0 * Integrator.getArea(npdf, Math.abs(fishersZ), 9.0, 100);
    }

    /**
     * Sets the significance level at which independence judgments should be
     * made.  Affects the cutoff for partial correlations to be considered
     * statistically equal to zero.
     */
    @Override
	public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance out of range.");
        }

        this.alpha = alpha;
        this.thresh = Double.NaN;
    }

    /**
     * Gets the current significance level.
     */
    @Override
	public double getAlpha() {
        return this.alpha;
    }

    /**
     * Returns the list of variables over which this independence checker is
     * capable of determinine independence relations-- that is, all the
     * variables in the given graph or the given data set.
     */
    @Override
	public List<Node> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable with the given name.
     */
    @Override
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
    @Override
	public List<String> getVariableNames() {
        List<Node> variables = getVariables();
        List<String> variableNames = new ArrayList<String>();
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    @Override
	public String toString() {
        return "Fisher's Z, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

//    private double cutoffGaussian() {
//        npdf = new NormalPdf();
//        final double upperBound = 50.0;
//        final double delta = 0.000001;
//        //        double alpha = this.alpha/2.0;    //Two sided test
//        return CutoffFinder.getCutoff(npdf, upperBound, alpha, delta);
//    }

    /**
     * Computes that value x such that P(abs(N(0,1) > x) < alpha.  Note that
     * this is a two sided test of the null hypothesis that the Fisher's Z
     * value, which is distributed as N(0,1) is not equal to 0.0.
     */
    private double cutoffGaussian() {
        Normal normal = new Normal(0, 1, PersistentRandomUtil.getInstance().getRandomEngine());
        double upperTail = 1.0 - getAlpha() / 2.0;
        double epsilon = 1e-14;

        // Find an upper bound.
        double lowerBound = -1.0;
        double upperBound = 0.0;

        while (normal.cdf(upperBound) < upperTail) {
            lowerBound += 1.0;
            upperBound += 1.0;
        }

        while (upperBound >= lowerBound + epsilon) {
            double midPoint = lowerBound + (upperBound - lowerBound) / 2.0;

            if (normal.cdf(midPoint) <= upperTail) {
                lowerBound = midPoint;
            }
            else {
                upperBound = midPoint;
            }
        }

        return lowerBound;
    }

    private int sampleSize() {
        return covMatrix().getSampleSize();
    }

    private CovarianceMatrix covMatrix() {
        return covMatrix;
    }

    @Override
	public boolean determines(List z, Node x1) {
        throw new UnsupportedOperationException(
                "This independence test does not " +
                        "test whether Z determines X for list Z of variable and variable X.");
    }

    @Override
	public boolean splitDetermines(List z, Node x, Node y) {
        throw new UnsupportedOperationException(
                "This independence test does not " +
                        "test whether Z determines X for list Z of variable and variable X.");
    }

    @Override
	public RectangularDataSet getData() {
        return dataSet;
    }
}


