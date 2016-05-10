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
import cern.jet.random.Normal;
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
 * @version $Revision: 6574 $ $Date: 2006-01-20 13:45:01 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class IndTestFisherZD implements IndependenceTest {

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
     * The value of the Fisher's Z statistic associated with the las
     * calculated partial correlation.
     *
     * @serial
     */
    private double fishersZ;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
    private RectangularDataSet dataSet;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new Independence test which checks independence facts based
     * on the correlation matrix implied by the given data set (must be
     * continuous). The given significance level is used.
     *
     * @param dataSet A data set containing only continuous columns.
     * @param alpha   The alpha level of the test.
     */
    public IndTestFisherZD(RectangularDataSet dataSet, double alpha) {
        this.dataSet = dataSet;
        this.data = dataSet.getDoubleData();
        this.variables = Collections.unmodifiableList(dataSet.getVariables());
        setAlpha(alpha);
    }

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
        int[] zCols = new int[size];

        int xIndex = getVariables().indexOf(xVar);
        int yIndex = getVariables().indexOf(yVar);

        for (int i = 0; i < zList.size(); i++) {
            zCols[i] = getVariables().indexOf(zList.get(i));
        }

        int[] zRows = new int[data.rows()];
        for (int i = 0; i < data.rows(); i++) {
            zRows[i] = i;
        }

        DoubleMatrix2D Z = data.viewSelection(zRows, zCols);
        DoubleMatrix1D x = data.viewColumn(xIndex);
        DoubleMatrix1D y = data.viewColumn(yIndex);
        DoubleMatrix2D Zt = new Algebra().transpose(Z);
        DoubleMatrix2D ZtZ = new Algebra().mult(Zt, Z);
        DoubleMatrix2D G = MatrixUtils.ginverse(ZtZ);

        DoubleMatrix2D Zt2 = Zt.like();
        Zt2.assign(Zt);
        DoubleMatrix2D GZt = new Algebra().mult(G, Zt2);

        DoubleMatrix1D b_x = new Algebra().mult(GZt, x);
        DoubleMatrix1D b_y = new Algebra().mult(GZt, y);

        DoubleMatrix1D xPred = new Algebra().mult(Z, b_x);
        DoubleMatrix1D yPred = new Algebra().mult(Z, b_y);

        DoubleMatrix1D xRes = xPred.copy().assign(x, Functions.minus);
        DoubleMatrix1D yRes = yPred.copy().assign(y, Functions.minus);

        // Note that r will be NaN if either xRes or yRes is constant.
        double r = StatUtils.rXY(xRes.toArray(), yRes.toArray());

        if (Double.isNaN(thresh)) {
            this.thresh = cutoffGaussian();
        }

        if (Double.isNaN(r)) {
            TetradLogger.getInstance().independenceDetails(SearchLogUtils.independenceFactMsg(xVar, yVar, zList, getPValue()));
            return true;
        }

        this.fishersZ = 0.5 * Math.sqrt(sampleSize() - zList.size() - 3.0) *
                Math.log(Math.abs(1.0 + r) / Math.abs(1.0 - r));

        if (Double.isNaN(this.fishersZ)) {
            throw new IllegalArgumentException("The Fisher's Z " +
                    "score for independence fact " + xVar + " _||_ " + yVar +
                    " | " + zList + " is undefined.");
        }

        boolean indFisher = true;

        //System.out.println("thresh = " + thresh);
        //if(Math.abs(fishersZ) > 1.96) indFisher = false; //Two sided with alpha = 0.05
        if (Math.abs(fishersZ) > thresh) {
            indFisher = false;  //Two sided
        }

        if (indFisher) {
            TetradLogger.getInstance().independenceDetails(SearchLogUtils.independenceFactMsg(xVar, yVar, zList, getPValue()));
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
        Normal normal = new Normal(0, 1, PersistentRandomUtil.getInstance().getRandomEngine());
        return 2.0 * (1.0 - normal.cdf(Math.abs(fishersZ)));

//        double q = 2.0 * Integrator.getArea(npdf, 0.0, Math.abs(fishersZ), 100);
//        if (q > 1.0) {
//            q = 1.0;
//        }
//        return 1.0 - q;
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
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    @Override
	public String toString() {
        return "Fisher's Z Deterministic, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

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

//        npdf = new NormalPdf();
//        final double upperBound = 9.0;
//        final double delta = 0.001;
//                double alpha = this.alpha/2.0;    //Two sided test
//        return CutoffFinder.getCutoff(npdf, upperBound, alpha, delta);
    }

    private int sampleSize() {
        return data.rows();
    }

    @Override
	public boolean determines(List<Node> zList, Node xVar) {
        if (zList == null) {
            throw new NullPointerException();
        }

        if (zList.isEmpty()) {
            return false;
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
        DoubleMatrix2D Zt2 = Zt.copy();
        DoubleMatrix2D GZt = new Algebra().mult(G, Zt2);
        DoubleMatrix1D b_x = new Algebra().mult(GZt, x);
        DoubleMatrix1D xPred = new Algebra().mult(Z, b_x);
        DoubleMatrix1D xRes = xPred.copy().assign(x, Functions.minus);
        double SSE = xRes.aggregate(Functions.plus, Functions.square);

//        System.out.println(SSE);
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

            sb.append(" SSE = ").append(nf.format(SSE));

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
        return dataSet;
    }
}


