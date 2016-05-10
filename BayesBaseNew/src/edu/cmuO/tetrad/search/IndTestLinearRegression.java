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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Checks independence of X _||_ Y | Z for variables X and Y and list Z of
 * variables. Partial correlations are calculated using generalized inverses, so
 * linearly dependent variables do not throw exceptions. Must supply a
 * continuous data set; don't know how to do this with covariance or correlation
 * matrices.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted IndTestCramerT for Fisher's Z
 * @version $Revision: 6413 $ $Date: 2006-01-20 13:45:01 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class IndTestLinearRegression implements IndependenceTest {

    /**
     * The correlation matrix.
     *
     * @serial
     */
    private final DoubleMatrix2D data;

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
     * The last calculated partial correlation, needed to calculate relative
     * strength.
     *
     * @serial
     */
    private double storedR = 0.;

    /**
     * The value of the Fisher's Z statistic associated with the las calculated
     * partial correlation.
     *
     * @serial
     */
    private double fishersZ;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based
     * on the correlation matrix implied by the given data set (must be
     * continuous). The given significance level is used.
     *
     * @param dataSet A data set containing only continuous columns.
     * @param alpha   The alpha level of the test.
     */
    public IndTestLinearRegression(RectangularDataSet dataSet, double alpha) {
        this.data = dataSet.getDoubleData();
        this.variables = Collections.unmodifiableList(dataSet.getVariables());
        setAlpha(alpha);
    }

//    /**
//     * Constructs a new independence test that will determine conditional
//     * independence facts using the given correlation matrix and the given
//     * significance level.
//     */
//    public IndTestFisherZD(CovarianceMatrix corrMatrix, double alpha) {
//        this.data = corrMatrix;
//        this.variables = Collections.unmodifiableList(corrMatrix.getVariables());
//        setAlpha(alpha);
//    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new IndTestCramerT instance for a subset of the variables.
     */
    @Override
	public IndependenceTest indTestSubset(List vars) {
//        if (vars.isEmpty()) {
//            throw new IllegalArgumentException("Subset may not be empty.");
//        }
//
//        for (int i = 0; i < vars.size(); i++) {
//            if (!variables.contains(vars.get(i))) {
//                throw new IllegalArgumentException(
//                        "All vars must be original vars");
//            }
//        }
//
//        double[][] m = new double[vars.size()][vars.size()];
//
//        for (int i = 0; i < vars.size(); i++) {
//            for (int j = 0; j < vars.size(); j++) {
//                double val = data.getValue(variables.indexOf(vars.get(i)),
//                        variables.indexOf(vars.get(j)));
//                m[i][j] = val;
//            }
//        }
//
//        int sampleSize = covMatrix().getN();
//        CorrelationMatrix newCorrMatrix = new CorrelationMatrix(vars, m,
//                sampleSize);
//
//        double alphaNew = getAlpha();
//        IndependenceTest newIndTest = new IndTestCramerT(newCorrMatrix,
//                alphaNew);
//        return newIndTest;
//
        return null;
    }

    /**
     * Determines whether variable x is independent of variable y given a list
     * of conditioning variables z.
     *
     * @param xVar  the one variable being compared.
     * @param yVar  the second variable being compared.
     * @param zList the list of conditioning variables.
     * @return true iff x _||_ y | z.
     * @throws RuntimeException if a matrix singularity is encountered.
     */
    @Override
	public boolean isIndependent(Node xVar, Node yVar, List<Node> zList) {
        if (zList == null) {
            throw new NullPointerException();
        }

        for (Node node : zList) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        int size = zList.size();
        int[] regressors = new int[size + 1];
        String[] regressorNames = new String[size + 1];
        regressors[0] = getVariables().indexOf(yVar);
        regressorNames[0] = yVar.getName();

        int xIndex = getVariables().indexOf(xVar);

        for (int i = 0; i < zList.size(); i++) {
            regressors[i + 1] = getVariables().indexOf(zList.get(i));
            regressorNames[i + 1] = zList.get(i).getName();
        }

        int[] zRows = new int[data.rows()];
        for (int i = 0; i < data.rows(); i++) {
            zRows[i] = i;
        }

        DoubleMatrix2D Z = data.viewSelection(zRows, regressors);
        DoubleMatrix1D x = data.viewColumn(xIndex);

        // Check for missing values.
        for (int i = 0; i < x.size(); i++) {
            if (Double.isNaN(x.getQuick(i))) {
                throw new IllegalArgumentException(
                        "Please impute or remove missing values first.");
            }
        }

        if (DataUtils.containsMissingValue(Z)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        RegressionOld regression = new RegressionOld();

        regression.setRegressors(Z.viewDice().toArray());
        regression.setRegressorNames(regressorNames);

        RegressionResult result = regression.regress(x.toArray(), xVar.getName());

        double p = result.getP()[1];
        boolean indep = p > alpha;

        if (indep) {
            TetradLogger.getInstance().independenceDetails(SearchLogUtils.independenceFactMsg(xVar, yVar, zList, p));
        }

        return indep;
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
        double q = 2.0 * Integrator.getArea(npdf, 0.0, Math.abs(fishersZ), 100);
        if (q > 1.0) {
            q = 1.0;
        }
        return 1.0 - q;
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

        for (Node variable : variables) {
            variableNames.add(variable.getName());
        }

        return variableNames;
    }

    @Override
	public String toString() {
        return "Linear Regression Test, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

//    /**
//     * Return the p-value of the last calculated independence fact.
//     *
//     * @return this p-value.  When accessed through the IndependenceChecker
//     *         interface, this p-value should only be considered to be a
//     *         relative strength.
//     */
//    private double getRelativeStrength() {
//
//        // precondition:  pdf is the most recently used partial
//        // correlation distribution function, and storedR is the most
//        // recently calculated partial correlation.
//        return 2.0 * Integrator.getArea(npdf, Math.abs(storedR), 9.0, 100);
//    }

//    /**
//     * Computes that value x such that P(abs(N(0,1) > x) < alpha.  Note that
//     * this is a two sided test of the null hypothesis that the Fisher's Z
//     * value, which is distributed as N(0,1) is not equal to 0.0.
//     */
//    private double cutoffGaussian() {
//        npdf = new NormalPdf();
//        final double upperBound = 9.0;
//        final double delta = 0.001;
//        //        double alpha = this.alpha/2.0;    //Two sided test
//        return CutoffFinder.getCutoff(npdf, upperBound, alpha, delta);
//    }

//    private int sampleSize() {
//        return data.rows();
//    }

    @Override
	public boolean determines(List<Node> zList, Node xVar) {
        if (zList == null) {
            throw new NullPointerException();
        }

        for (Node node : zList) {
            if (node == null) {
                throw new NullPointerException();
            }
        }

        int size = zList.size();
        int[] zCols = new int[size];

        int xIndex = getVariables().indexOf(xVar);

        for (int i = 0; i < zList.size(); i++) {
            zCols[i] = getVariables().indexOf(zList.get(i));
        }

        int[] zRows = new int[data.rows()];
        for (int i = 0; i < data.rows(); i++) {
            zRows[i] = i;
        }

        DoubleMatrix2D Z = data.viewSelection(zRows, zCols);
        DoubleMatrix1D x = data.viewColumn(xIndex);
        DoubleMatrix2D Zt = new Algebra().transpose(Z);
        DoubleMatrix2D ZtZ = new Algebra().mult(Zt, Z);
        DoubleMatrix2D G = MatrixUtils.ginverse(ZtZ);

        // Bug in Colt? Need to make a copy before multiplying to avoid
        // a ClassCastException.
        DoubleMatrix2D Zt2 = Zt.like();
        Zt2.assign(Zt);
        DoubleMatrix2D GZt = new Algebra().mult(G, Zt2);

        DoubleMatrix1D b_x = new Algebra().mult(GZt, x);

        DoubleMatrix1D xPred = new Algebra().mult(Z, b_x);

        DoubleMatrix1D xRes = xPred.copy().assign(x, Functions.minus);

        double SSE = xRes.aggregate(Functions.plus, Functions.square);
        boolean determined = SSE < 0.0001;

        if (determined) {
            StringBuffer sb = new StringBuffer();
            sb.append("Determination found: ").append(xVar).append(
                    " is determined by {");

            for (int i = 0; i < zList.size(); i++) {
                sb.append(zList.get(i));

                if (i < zList.size() - 1) {
                    sb.append(", ");
                }
            }

            sb.append("}");

            TetradLogger.getInstance().independenceDetails(sb.toString());
        }

        return determined;
    }

    @Override
	public boolean splitDetermines(List<Node> z, Node x, Node y) {
        return determines(z, x) || determines(z, y);
    }

    @Override
	public RectangularDataSet getData() {
        return null;
    }
}


