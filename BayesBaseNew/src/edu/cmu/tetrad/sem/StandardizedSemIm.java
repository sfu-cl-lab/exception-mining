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

package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DataUtils;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;
import edu.cmu.tetradapp.model.PcSearchParams;

import java.util.*;
import java.text.NumberFormat;

/**
 * A special SEM model in which variances of variables are always 1 and means of variables
 * are always 0. In order to ensure that means of variables are always zero, means or error
 * terms are set to zero. (They are alway Gaussian for this model.) Connection functions are
 * always linear. In order to ensure that variances of variables are always 1, only coefficients
 * are allowed to change, and the error terms take up the slack. Becuase of this constraint,
 * given settings of other parameters, the range of a given parameter is always bounded
 * above and below. The user may query this range and set set the value of the coefficient
 * to anything within this range. The SEM is initialized from a linear, gaussian SEM by
 * calculating (or estimating) what the coefficients would be if a data set were simulated
 * from that SEM, standardized, and reestimated with the same SEM PM. The coefficients of
 * such an estimated SEM PM are used to initialize the standardized SEM, repeating if necessary
 * (due to possible noise issues) to get coefficients for which all errors variances can
 * be calculated. (Variances need to be >= 0 for Normal distributions.) This produces a
 * set of coefficients that are viable candidates for the standardized SEM. From there,
 * the user cannot make any change that does not also allow for a standardized SEM to be
 * defined, with error variances taking up the slack. Thus, the standardized SEM can
 * never go "out of bounds."
 * </p>
 * Currently we are not allowing bidirected edges in the SEM graph.
 */
public class StandardizedSemIm implements TetradSerializable {
    public enum Initialization {
        CALCULATE_FROM_SEM, INITIALIZE_FROM_DATA
    }

    static final long serialVersionUID = 23L;

    /**
     * The SEM model.
     */
    private SemPm semPm;

    /**
     * The graph of the model. Stored internally because it must be ensured that the error terms are showing.
     */
    private SemGraph semGraph;

    /**
     * A map from edges in the graph to their coefficients. These are the only parameters in the model. This
     * includes coefficients for directed as well as bidirected edges.
     */
    private Map<Edge, Double> edgeParameters;

    /**
     * A map from error nodes in the graph to their error variances. These are not parameters in the model; their
     * values are always calculated as residual variances, under the constraint that the variances of each
     * variable must be 1.
     */
//    private Map<Node, Double> errorVariances;

    private DoubleMatrix2D implCovar;
    private DenseDoubleMatrix2D implCovarMeas;
    private List<Node> measuredNodes;
    private Edge editingEdge;
    private ParameterRange range;


    //========================================CONSTRUCTORS========================================================//

    /**
     * Constructs a new standardized SEM IM, initializing from the parameters in the given SEM IM.
     *
     * @param im The SEM IM that the parameters will be initialized from.
     */
    public StandardizedSemIm(SemIm im) {
        this(im, Initialization.CALCULATE_FROM_SEM);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static StandardizedSemIm serializableInstance() {
        return new StandardizedSemIm(SemIm.serializableInstance());
    }

    //===========================================PUBLIC METHODS==================================================//

    /**
     * Constructs a new standardized SEM IM from the parameters in the given SEM IM.
     *
     * @param im             Stop asking me for these things! The given SEM IM!!!
     * @param initialization CALCULATE_FROM_SEM if the initial values will be calculated from the given SEM IM;
     *                       INITIALIZE_FROM_DATA if data will be simulated from the given SEM, standardized, and estimated.
     */
    public StandardizedSemIm(SemIm im, Initialization initialization) {
        this.semPm = new SemPm(im.getSemPm());
        this.semGraph = new SemGraph(semPm.getGraph());
        semGraph.setShowErrorTerms(true);

        if (semGraph.existsDirectedCycle()) {
            throw new IllegalArgumentException("The cyclic case is not handled.");
        }

        if (initialization == Initialization.CALCULATE_FROM_SEM) {
//         This code calculates the new coefficients directly from the old ones.
            edgeParameters = new HashMap<Edge, Double>();

            List<Node> nodes = im.getVariableNodes();
            DoubleMatrix2D impliedCovar = im.getImplCovar();

            for (Parameter parameter : im.getSemPm().getParameters()) {
                if (parameter.getType() == ParamType.COEF) {
                    Node a = parameter.getNodeA();
                    Node b = parameter.getNodeB();
                    int aindex = nodes.indexOf(a);
                    int bindex = nodes.indexOf(b);
                    double impliedStdA = Math.sqrt(impliedCovar.get(aindex, aindex));
                    double impliedStdB = Math.sqrt(impliedCovar.get(bindex, bindex));
                    double oldCoef = im.getEdgeCoef(a, b);
                    double newCoef = (impliedStdA * oldCoef) / impliedStdB;
                    edgeParameters.put(Edges.directedEdge(a, b), newCoef);
                } else if (parameter.getType() == ParamType.COVAR) {
                    Node a = parameter.getNodeA();
                    Node b = parameter.getNodeB();
                    Node exoa = semGraph.getExogenous(a);
                    Node exob = semGraph.getExogenous(b);
                    double covar = im.getErrCovar(a, b) / Math.sqrt(im.getErrVar(a) * im.getErrVar(b));
                    edgeParameters.put(Edges.bidirectedEdge(exoa, exob), covar);
                }
            }
        } else {

            // This code estimates the new coefficients from simulated data from the old model.
            DataSet dataSet = im.simulateData(1000, false);
            DoubleMatrix2D _dataSet = dataSet.getDoubleData();
            _dataSet = DataUtils.standardizeData(_dataSet);
            DataSet dataSetStandardized = ColtDataSet.makeData(dataSet.getVariables(), _dataSet);

            SemEstimator estimator = new SemEstimator(dataSetStandardized, im.getSemPm());
            SemIm imStandardized = estimator.estimate();

            edgeParameters = new HashMap<Edge, Double>();

            for (Parameter parameter : imStandardized.getSemPm().getParameters()) {
                if (parameter.getType() == ParamType.COEF) {
                    Node a = parameter.getNodeA();
                    Node b = parameter.getNodeB();
                    double coef = imStandardized.getEdgeCoef(a, b);
                    edgeParameters.put(Edges.directedEdge(a, b), coef);
                } else if (parameter.getType() == ParamType.COVAR) {
                    Node a = parameter.getNodeA();
                    Node b = parameter.getNodeB();
                    Node exoa = semGraph.getExogenous(a);
                    Node exob = semGraph.getExogenous(b);
                    double covar = -im.getErrCovar(a, b) / Math.sqrt(im.getErrVar(a) * im.getErrVar(b));
                    edgeParameters.put(Edges.bidirectedEdge(exoa, exob), covar);
                }
            }
        }

        this.measuredNodes = Collections.unmodifiableList(semPm.getMeasuredNodes());
    }

    public boolean containsParameter(Edge edge) {
        if (Edges.isBidirectedEdge(edge)) {
            edge = Edges.bidirectedEdge(semGraph.getExogenous(edge.getNode1()),
                    semGraph.getExogenous(edge.getNode2()));
        }

        return edgeParameters.keySet().contains(edge);
    }

    /**
     * Sets the coefficient for the a->b edge to the given coefficient, if within range. Otherwise
     * does nothing.
     *
     * @param a    a -> b
     * @param b    a -> b
     * @param coef The coefficient of a -> b.
     * @return true if the coefficent was set (i.e. was within range), false if not.
     */
    public boolean setEdgeCoefficient(Node a, Node b, final double coef) {
        Edge edge = Edges.directedEdge(a, b);

        if (edgeParameters.get(edge) == null) {
            throw new NullPointerException("Not a coefficient parameter in this model: " + edge);
        }

        if (editingEdge == null || !edge.equals(editingEdge)) {
            range = getParameterRange(edge);
            editingEdge = edge;
        }

        if (coef > range.getLow() && coef < range.getHigh()) {
            edgeParameters.put(edge, coef);
            return true;
        }

        return false;

//        if (!paramInBounds(edge, coef)) {
//            edgeParameters.put(edge, d);
//            return false;
//        }
//
//        edgeParameters.put(edge, coef);
//        return true;
    }

    /**
     * Sets the covariance for the a<->b edge to the given covariance, if within range. Otherwise
     * does nothing.
     *
     * @param a     a <-> b
     * @param b     a <-> b
     * @param covar The covariance of a <-> b.
     * @return true if the coefficent was set (i.e. was within range), false if not.
     */
    public boolean setErrorCovariance(Node a, Node b, final double covar) {
        Edge edge = Edges.bidirectedEdge(semGraph.getExogenous(a), semGraph.getExogenous(b));

        if (edgeParameters.get(edge) == null) {
            throw new IllegalArgumentException("Not a covariance parameter in this model: " + edge);
        }

        if (editingEdge == null || !edge.equals(editingEdge)) {
            range = getParameterRange(edge);
            editingEdge = edge;
        }

        if (covar > range.getLow() && covar < range.getHigh()) {
            edgeParameters.put(edge, covar);
            return true;
        }
        else {
            return false;
        }

//        if (!paramInBounds(edge, coef)) {
//            edgeParameters.put(edge, d);
//            return false;
//        }
//
//        edgeParameters.put(edge, coef);
//        return true;

//        if (!paramInBounds(edge, covar)) {
//            edgeParameters.put(edge, d);
//            return false;
//        }
//
//        edgeParameters.put(edge, covar);
//        return true;
    }

    /**
     * @param a a->b
     * @param b a->b
     * @return The coefficient for a->b.
     */
    public double getEdgeCoefficient(Node a, Node b) {
        Edge edge = Edges.directedEdge(a, b);
        Double d = edgeParameters.get(edge);

        if (d == null) {
            throw new IllegalArgumentException("Not a directed edge in this model: " + edge);
        }

        return d;
    }

    /**
     * @param a a->b
     * @param b a->b
     * @return The coefficient for a->b.
     */
    public double getErrorCovariance(Node a, Node b) {
        Edge edge = Edges.bidirectedEdge(semGraph.getExogenous(a), semGraph.getExogenous(b));
        Double d = edgeParameters.get(edge);

        if (d == null) {
            throw new IllegalArgumentException("Not a covariance parameter in this model: " + edge);
        }

        return d;
    }

    public double getParameterValue(Edge edge) {
        if (Edges.isDirectedEdge(edge)) {
            return getEdgeCoefficient(edge.getNode1(), edge.getNode2());
        } else if (Edges.isBidirectedEdge(edge)) {
            return getErrorCovariance(edge.getNode1(), edge.getNode2());
        } else {
            throw new IllegalArgumentException("Only directed and bidirected edges are supported: " + edge);
        }
    }

    public void setParameterValue(Edge edge, double value) {
        if (Edges.isDirectedEdge(edge)) {
            setEdgeCoefficient(edge.getNode1(), edge.getNode2(), value);
        } else if (Edges.isBidirectedEdge(edge)) {
            setErrorCovariance(edge.getNode1(), edge.getNode2(), value);
        } else {
            throw new IllegalArgumentException("Only directed and bidirected edges are supported: " + edge);
        }
    }

    public ParameterRange getCoefficientRange(Node a, Node b) {
        return getParameterRange(Edges.directedEdge(a, b));
    }

    public ParameterRange getCovarianceRange(Node a, Node b) {
        return getParameterRange(Edges.bidirectedEdge(semGraph.getExogenous(a), semGraph.getExogenous(b)));
    }

    /**
     * @param edge a->b or a<->b.
     * @return the range of the covariance parameter for a->b or a<->b.
     */
    public ParameterRange getParameterRange(Edge edge) {
        if (Edges.isBidirectedEdge(edge)) {
            edge = Edges.bidirectedEdge(semGraph.getExogenous(edge.getNode1()),
                    semGraph.getExogenous(edge.getNode2()));
        }


        if (!(edgeParameters.keySet().contains(edge))) {
            throw new IllegalArgumentException("Not an edge in this model: " + edge);
        }

        double initial = edgeParameters.get(edge);

        if (initial == Double.NEGATIVE_INFINITY) {
            initial = Double.MIN_VALUE;
        } else if (initial == Double.POSITIVE_INFINITY) {
            initial = Double.MAX_VALUE;
        }

        double value = initial;

        // look upward for a point that fails.
        double high = value + 1;

        while (paramInBounds(edge, high)) {
            high = value + 2 * (high - value);

            if (high == Double.POSITIVE_INFINITY) {
                break;
            }
        }

        // find the boundary using binary search.
        double rangeHigh;

        if (high == Double.POSITIVE_INFINITY) {
            rangeHigh = high;
        } else {
            double low = value;

            while (high - low > 1e-10) {
                double midpoint = (high + low) / 2.0;

                if (paramInBounds(edge, midpoint)) {
                    low = midpoint;
                } else {
                    high = midpoint;
                }
            }

            rangeHigh = (high + low) / 2.0;
        }

        // look downard for a point that fails.
        double low = value - 1;

        while (paramInBounds(edge, low)) {
            low = value - 2 * (value - low);

            if (low == Double.NEGATIVE_INFINITY) {
                break;
            }
        }

        double rangeLow;

        if (low == Double.NEGATIVE_INFINITY) {
            rangeLow = low;
        } else {

            // find the boundary using binary search.
            high = value;

            while (high - low > 1e-10) {
                double midpoint = (high + low) / 2.0;

                if (paramInBounds(edge, midpoint)) {
                    high = midpoint;
                } else {
                    low = midpoint;
                }
            }

            rangeLow = (high + low) / 2.0;
        }

        if (Edges.isDirectedEdge(edge)) {
            edgeParameters.put(edge, initial);
        } else if (Edges.isBidirectedEdge(edge)) {
            edgeParameters.put(edge, initial);
        }

        return new ParameterRange(edge, value, rangeLow, rangeHigh);
    }

    /**
     * @param error The error term. A node with NodeType.ERROR.
     * @return the error variance for the given error term. THIS IS NOT A PARAMETER OF THE
     *         MODEL! Its value is simply calculated from the given coefficients of the model.
     *         Returns Double.NaN if the error variance cannot be computed.
     */
    public double getErrorVariance(Node error) {
        return calculateErrorVarianceFromParams(error);
    }

    /**
     * @return a map from error to error variances, or to Double.NaN where these cannot be computed.
     */
    public Map<Node, Double> errorVariances() {
        Map<Node, Double> errorVarances = new HashMap<Node, Double>();

        for (Node error : getErrorNodes()) {
            errorVarances.put(error, getErrorVariance(error));
        }

        return errorVarances;
    }

    /**
     * @return a string representation of the coefficients and variances of the model.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        buf.append("\nStandardized SEM:");
        buf.append("\n\nEdge coefficients (parameters):\n");

        for (Edge edge : edgeParameters.keySet()) {
            if (!Edges.isDirectedEdge(edge)) {
                continue;
            }

            buf.append("\n" + edge + " " + nf.format(edgeParameters.get(edge)));
        }

        buf.append("\n\nError covariances (parameters):\n");

        for (Edge edge : edgeParameters.keySet()) {
            if (!Edges.isBidirectedEdge(edge)) {
                continue;
            }

            buf.append("\n" + edge + " " + nf.format(edgeParameters.get(edge)));
        }

        buf.append("\n\nError variances (calculated):\n");

        for (Node error : getErrorNodes()) {
            double variance = getErrorVariance(error);
            buf.append("\n" + error + " " + nf.format(Double.isNaN(variance) ? "Undefined" : variance));
        }

        buf.append("\n");

        return buf.toString();
    }

    /**
     * @return the list of variable nodes of the model, in order.
     */
    public List<Node> getVariableNodes() {
        return semPm.getVariableNodes();
    }

    /**
     * @return The edge coefficient matrix of the model, a la SemIm. Note that this will normally need to be
     *         transposed, since [a][b] is the edge coefficient for a-->b, not b-->a. Sorry. History. THESE ARE
     *         PARAMETERS OF THE MODEL--THE ONLY PARAMETERS.
     */
    public DoubleMatrix2D edgeCoef() {
        List<Node> variableNodes = getVariableNodes();

        DoubleMatrix2D edgeCoef = new DenseDoubleMatrix2D(variableNodes.size(), variableNodes.size());

        for (Edge edge : edgeParameters.keySet()) {
            if (Edges.isBidirectedEdge(edge)) {
                continue;
            }

            Node a = edge.getNode1();
            Node b = edge.getNode2();

            int aindex = variableNodes.indexOf(a);
            int bindex = variableNodes.indexOf(b);

            double coef = edgeParameters.get(edge);

            edgeCoef.set(aindex, bindex, coef);
        }

        return edgeCoef;
    }

    /**
     * @return Returns the error covariance matrix of the model. i.e. [a][b] is the covariance of E_a and E_b,
     *         with [a][a] of course being the variance of E_a. THESE ARE NOT PARAMETERS OF THE MODEL; THEY ARE
     *         CALCULATED. Note that elements of this matrix may be Double.NaN; this indicates that these
     *         elements cannot be calculated.
     */
    public DoubleMatrix2D errCovar() {
        return errCovar(errorVariances());
    }

    /**
     * @return For compatibility only. Returns the variable means of the model. These are always
     *         zero, since this is a standardized model. THESE ARE ALSO NOT PARAMETERS OF THE MODEL. ONLY THE
     *         COEFFICIENTS ARE PARAMETERS.
     */
    public double[] means() {
        return new double[semPm.getVariableNodes().size()];
    }

    /**
     * A convenience method, in case we want to change out mind about how to simulate. For instance,
     * it's unclear yet whether we can allow nongaussian errors, so we don't know yet whether the
     * reduced form method is needed.
     *
     * @param sampleSize      The sample size of the desired data set.
     * @param latentDataSaved True if latent variables should be included in the data set.
     * @return This returns a standardized data set simulated from the model, using the reduced form
     *         method.
     */
    public DataSet simulateData(int sampleSize, boolean latentDataSaved) {
        return simulateDataReducedForm(sampleSize, latentDataSaved);
    }

    /**
     * @param sampleSize      The sample size of the desired data set.
     * @param latentDataSaved True if latent variables should be included in the data set.
     * @return This returns a standardized data set simulated from the model, using the reduced form
     *         method.
     */
    public DataSet simulateDataReducedForm(int sampleSize, boolean latentDataSaved) {
        int numVars = getVariableNodes().size();

        // Calculate inv(I - edgeCoef)
        DoubleMatrix2D edgeCoef = edgeCoef().copy().viewDice();

        DoubleMatrix2D iMinusB = DoubleFactory2D.dense.identity(edgeCoef.rows());
        iMinusB.assign(edgeCoef, Functions.minus);

        DoubleMatrix2D inv = new Algebra().inverse(iMinusB);

        // Pick error values e, for each calculate inv * e.
        DoubleMatrix2D sim = new DenseDoubleMatrix2D(sampleSize, numVars);

        // Generate error data with the right variances and covariances, then override this
        // with error data for varaibles that have special distributions defined. Not ideal,
        // but not sure what else to do at the moment. It's better than not taking covariances
        // into account!
        DoubleMatrix2D cholesky = MatrixUtils.choleskyC(errCovar(errorVariances()));

        for (int i = 0; i < sampleSize; i++) {
            DoubleMatrix1D e = new DenseDoubleMatrix1D(exogenousData(cholesky, RandomUtil.getInstance()));
            DoubleMatrix1D ePrime = new Algebra().mult(inv, e);
            sim.viewRow(i).assign(ePrime);
        }

        DataSet fullDataSet = ColtDataSet.makeContinuousData(getVariableNodes(), sim);

        if (latentDataSaved) {
            return fullDataSet;
        } else {
            return DataUtils.restrictToMeasured(fullDataSet);
        }
    }

    /**
     * Returns a copy of the implied covariance matrix over all the variables.
     */
    public DoubleMatrix2D getImplCovar() {
        return implCovar().copy();
    }

    /**
     * Returns a copy of the implied covariance matrix over the measured
     * variables only.
     */
    public DoubleMatrix2D getImplCovarMeas() {
        return implCovarMeas().copy();
    }

    //========================================PRIVATE METHODS==========================================//

    /**
     * @return Returns the error covariance matrix of the model. i.e. [a][b] is the covariance of E_a and E_b,
     *         with [a][a] of course being the variance of E_a. THESE ARE NOT PARAMETERS OF THE MODEL; THEY ARE
     *         CALCULATED. Note that elements of this matrix may be Double.NaN; this indicates that these
     *         elements cannot be calculated.
     */
    private DoubleMatrix2D errCovar(Map<Node, Double> errorVariances) {
        List<Node> variableNodes = getVariableNodes();
        List<Node> errorNodes = new ArrayList<Node>();

        for (Node node : variableNodes) {
            errorNodes.add(semGraph.getExogenous(node));
        }

        DoubleMatrix2D errorCovar = new DenseDoubleMatrix2D(errorVariances.size(), errorVariances.size());

        for (int index = 0; index < errorNodes.size(); index++) {
            Node error = errorNodes.get(index);
            double variance = getErrorVariance(error);
            errorCovar.set(index, index, variance);
        }

        for (int index1 = 0; index1 < errorNodes.size(); index1++) {
            for (int index2 = 0; index2 < errorNodes.size(); index2++) {
                Node error1 = errorNodes.get(index1);
                Node error2 = errorNodes.get(index2);
                Edge edge = semGraph.getEdge(error1, error2);

                if (edge != null && Edges.isBidirectedEdge(edge)) {
                    double covariance = getErrorCovariance(error1, error2);
                    errorCovar.set(index1, index2, covariance);
                }
            }
        }

        return errorCovar;
    }

    private DoubleMatrix2D implCovar() {
        computeImpliedCovar();
        return this.implCovar;
    }

    private DoubleMatrix2D implCovarMeas() {
        computeImpliedCovar();
        return this.implCovarMeas;
    }

    /**
     * Computes the implied covariance matrices of the Sem. There are two:
     * <code>implCovar </code> contains the covariances of all the variables and
     * <code>implCovarMeas</code> contains covariance for the measured variables
     * only.
     */
    private void computeImpliedCovar() {
        DoubleMatrix2D edgeCoefT = new Algebra().transpose(edgeCoef());

        // Note. Since the sizes of the temp matrices in this calculation
        // never change, we ought to be able to reuse them.
        this.implCovar = MatrixUtils.impliedCovarC(edgeCoefT, errCovar(errorVariances()));

        // Submatrix of implied covar for measured vars only.
        int size = getMeasuredNodes().size();
        this.implCovarMeas = new DenseDoubleMatrix2D(size, size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Node iNode = getMeasuredNodes().get(i);
                Node jNode = getMeasuredNodes().get(j);

                int _i = getVariableNodes().indexOf(iNode);
                int _j = getVariableNodes().indexOf(jNode);

                this.implCovarMeas.set(i, j, this.implCovar.get(_i, _j));
            }
        }
    }

    /**
     * The list of measured nodes for the semPm. (Unmodifiable.)
     */
    public List<Node> getMeasuredNodes() {
        return getSemPm().getMeasuredNodes();
    }

    /**
     * Takes a Cholesky decomposition from the Cholesky.cholesky method and a
     * set of data simulated using the information in that matrix. Written by
     * Don Crimbchin. </p> Modified June 8, Matt Easterday: added a random #
     * seed so that data can be recalculated with the same result in Causality
     * lab
     *
     * @param cholesky   the result from cholesky above.
     * @param randomUtil a random number generator, if null the method will make
     *                   a new generator for each random number needed
     * @return an array the same length as the width or length (cholesky should
     *         have the same width and length) containing a randomly generate
     *         data set.
     */
    private double[] exogenousData(DoubleMatrix2D cholesky, RandomUtil
            randomUtil) {

        // Step 1. Generate normal samples.
        double exoData[] = new double[cholesky.rows()];

        for (int i = 0; i < exoData.length; i++) {
            exoData[i] = randomUtil.nextNormal(0, 1);
        }

        // Step 2. Multiply by cholesky to get correct covariance.
        double point[] = new double[exoData.length];

        for (int i = 0; i < exoData.length; i++) {
            double sum = 0.0;

            for (int j = 0; j <= i; j++) {
                sum += cholesky.get(i, j) * exoData[j];
            }

            point[i] = sum;
        }

        return point;
    }

    public List<Node> getErrorNodes() {
        List<Node> errorNodes = new ArrayList<Node>();

        for (Node node : getVariableNodes()) {
            errorNodes.add(semGraph.getExogenous(node));
        }

        return errorNodes;
    }

    /**
     * @return a copy of the SEM PM.
     */
    public SemPm getSemPm() {
        return new SemPm(semPm);
    }

    //-------------------------------------------PUBLIC CLASSES--------------------------------------------//

    /**
     * Stores a coefficient range--i.e. the edge and coefficient value for which the range is needed,
     * plus the low and high ends of the range to which the coefficient value may be adjusted.
     *
     * @author Joseph Ramsey
     */
    public static final class ParameterRange implements TetradSerializable {
        static final long serialVersionUID = 23L;

        private Edge edge;
        private double coef;
        private double low;
        private double high;

        public ParameterRange(Edge edge, double coef, double low, double high) {
            this.edge = edge;
            this.coef = coef;
            this.low = low;
            this.high = high;
        }

        /**
         * Generates a simple exemplar of this class to test serialization.
         *
         * @see edu.cmu.TestSerialization
         * @see edu.cmu.tetradapp.util.TetradSerializableUtils
         */
        public static ParameterRange serializableInstance() {
            return new ParameterRange(Edge.serializableInstance(), 1.0, 1.0, 1.0);
        }

        public Edge getEdge() {
            return edge;
        }

        public double getCoef() {
            return coef;
        }

        public double getLow() {
            return low;
        }

        public double getHigh() {
            return high;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();

            buf.append("\n\nRange for " + edge);
            buf.append("\nCurrent value = " + coef);
            buf.append("\nLow end of range = " + low);
            buf.append("\nHigh end of range = " + high);

            return buf.toString();
        }
    }

    //-------------------------------------------PRIVATE METHODS-------------------------------------------//

    private boolean paramInBounds(Edge edge, double newValue) {
        edgeParameters.put(edge, newValue);
        Map<Node, Double> errorVariances = new HashMap<Node, Double>();
        for (Node node : semPm.getVariableNodes()) {
            Node error = semGraph.getExogenous(node);
            double d2 = calculateErrorVarianceFromParams(error);
            if (Double.isNaN(d2)) {
                return false;
            }

            errorVariances.put(error, d2);
        }

        if (!MatrixUtils.isPositiveDefinite(errCovar(errorVariances))) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the error variance for the given error node, given all of the coefficient values in the model.
     *
     * @param error An error term in the model--i.e. a variable with NodeType.ERROR.
     * @return The value of the error variance, or Double.NaN is the value is undefined.
     */
    private double calculateErrorVarianceFromParams(Node error) {
        error = semGraph.getNode(error.getName());

        Node child = semGraph.getChildren(error).get(0);
        List<Node> parents = semGraph.getParents(child);

        double otherVariance = 0;

        for (Node parent : parents) {
            if (parent == error) continue;
            double coef = getEdgeCoefficient(parent, child);
            otherVariance += coef * coef;
        }

        if (parents.size() >= 2) {
            ChoiceGenerator gen = new ChoiceGenerator(parents.size(), 2);
            int[] indices;

            while ((indices = gen.next()) != null) {
                Node node1 = parents.get(indices[0]);
                Node node2 = parents.get(indices[1]);

                double coef1, coef2;

                if (node1.getNodeType() != NodeType.ERROR) {
                    coef1 = getEdgeCoefficient(node1, child);
                } else {
                    coef1 = 1;
                }

                if (node2.getNodeType() != NodeType.ERROR) {
                    coef2 = getEdgeCoefficient(node2, child);
                } else {
                    coef2 = 1;
                }

                List<List<Node>> treks = GraphUtils.treksIncludingBidirected(semGraph, node1, node2);

                double cov = 0.0;

                for (List<Node> trek : treks) {
                    double product = 1.0;

                    for (int i = 1; i < trek.size(); i++) {
                        Node _node1 = trek.get(i - 1);
                        Node _node2 = trek.get(i);

                        Edge edge = semGraph.getEdge(_node1, _node2);
                        double factor;

                        if (Edges.isBidirectedEdge(edge)) {
                            factor = edgeParameters.get(edge);
                        } else if (!edgeParameters.containsKey(edge)) {
                            factor = 1;
                        } else if (semGraph.isParentOf(_node1, _node2)) {
                            factor = getEdgeCoefficient(_node1, _node2);
                        } else {
                            factor = getEdgeCoefficient(_node2, _node1);
                        }

                        product *= factor;
                    }

                    cov += product;
                }

                otherVariance += 2 * coef1 * coef2 * cov;
            }
        }

        return 1.0 - otherVariance <= 0 ? Double.NaN : 1.0 - otherVariance;
    }
}





















