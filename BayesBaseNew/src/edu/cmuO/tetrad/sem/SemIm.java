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

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.PlusMult;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.graph.SemGraph;
import edu.cmu.tetrad.util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.MarshalledObject;
import java.util.*;

/**
 * Stores an instantiated structural equation model (SEM), with error covariance
 * terms, possibly cyclic, suitable for estimation and simulation. For
 * estimation, the maximum likelihood fitting function and the negative log
 * likelihood function (Bollen 1989, p. 109) are calculated; these can be
 * maximized by an estimator to estimate optimal parameter values. The values of
 * parameters are set as indicated in their corresponding Parameter objects as
 * initial values for estimation. Provides multiple ways to get and set the
 * values of free parameters. For simulation, cyclic and acyclic methods are
 * provided; the cyclic method is used by default, although the acyclic method
 * is considerably faster for large data sets.
 * <p/>
 * Let V be the set of variables in the model. The parameters of the model are
 * as follows: (a) the list of linear coefficients for all edges u-->v in the
 * model, where u, v are in V, (b) the list of variances for all variables in V,
 * (c) the list of all error covariances d<-->e, where d an e are exogenous
 * terms in the model (either exogenous variables or error terms for endogenous
 * variables), and (d) the list of means for all variables in V.
 * <p/>
 * It is important to note that the likelihood functions this class calculates
 * do not depend on variable means. They depend only on edge coefficients and
 * error covariances. Hence, variable means are treated differently from edge
 * coefficients and error covariances in the model.
 * <p/>
 * Reference: Bollen, K. A. (1989). Structural Equations with Latent Variables.
 * New York: John Wiley & Sons.
 *
 * @author Frank Wimberly
 * @author Ricardo Silva
 * @author Joseph Ramsey
 */
public final class SemIm implements TetradSerializable, OptimizationSemIm {
    static final long serialVersionUID = 23L;

    /**
     * The Sem PM containing the graph and the parameters to be estimated. For
     * now a defensive copy of this is not being constructed, since it is not
     * used anywhere in the code except in the the constructor and in its
     * accessor method. It somebody changes it, it's their own fault, but it
     * won't affect this class.
     *
     * @serial Cannot be null.
     */
    private final SemPm semPm;

    /**
     * The list of measured and latent variableNodes for the semPm.
     * (Unmodifiable.)
     *
     * @serial Cannot be null.
     */
    private final List<Node> variableNodes;

    /**
     * @serial Cannot be null.
     * @deprecated
     */
    @Deprecated
	private final List<Node> exogenousNodes = null;

    /**
     * The list of measured variableNodes from the semPm. (Unmodifiable.)
     *
     * @serial Cannot be null.
     */
    private final List<Node> measuredNodes;

    /**
     * The list of free parameters (Unmodifiable). This must be in the same
     * order as this.freeMappings.
     *
     * @serial Cannot be null.
     */
    private List<Parameter> freeParameters;

    /**
     * The list of fixed parameters (Unmodifiable). This must be in the same
     * order as this.fixedMappings.
     *
     * @serial Cannot be null.
     */
    private List<Parameter> fixedParameters;

    /**
     * The list of mean parameters (Unmodifiable). This must be in the same
     * order as variableMeans.
     */
    private List<Parameter> meanParameters;

    /**
     * Replaced by edgeCoefC. Please do not delete; required for serialization
     * backward compatibility.
     *
     * @serial
     */
    private double[][] edgeCoef;

    /**
     * Matrix of edge coefficients. edgeCoef[i][j] is the coefficient of the
     * edge from getVariableNodes().get(i) to getVariableNodes().get(j), or 0.0
     * if this edge is not in the graph. The values of these may be changed, but
     * the array itself may not.
     *
     * @serial Cannot be null.
     */
    private DoubleMatrix2D edgeCoefC;

    /**
     * Replaced by errCovarC. Please do not delete; required for serialization
     * backward compatibility.
     *
     * @serial
     */
    private double[][] errCovar;

    /**
     * Matrix of error covariances. errCovar[i][j] is the covariance of the
     * error term of getExoNodes().get(i) and getExoNodes().get(j), with the
     * special case (duh!) that errCovar[i][i] is the variance of
     * getExoNodes.get(i). The values of these may be changed, but the array
     * itself may not.
     *
     * @serial Cannot be null.
     */
    private DoubleMatrix2D errCovarC;

    /**
     * Means of variables. These will not be counted for purposes of calculating
     * degrees of freedom, since the increase in dof is exactly balanced by a
     * decrease in dof.
     *
     * @serial Cannot be null.
     */
    private double[] variableMeans;

    /**
     * Standard Deviations of means. Needed to calculate standard errors.
     *
     * @serial Cannot be null.
     */
    private double[] variableMeansStdDev;

    /**
     * Replaced by sampleCovarC. Please do not delete. Required for
     * serialization backward compatibility.
     *
     * @serial
     */
    private double[][] sampleCovar;

    /**
     * The covariance matrix used to estimate the SemIm. Note that the variables
     * names in the covariance matrix must be in the same order as the variable
     * names in the SemPm.
     *
     * @serial Can be null.
     */
    private DoubleMatrix2D sampleCovarC;

    /**
     * The sample size.
     *
     * @serial Range >= 0.
     */
    private int sampleSize;

    /**
     * Replaced by implCovarC. Please do not delete. Required for serialization
     * backward compatibility.
     *
     * @serial
     */
    private double[][] implCovar;

    /**
     * The covariance matrix of all the variables. May be null if it has not yet
     * been calculated. The implied covariance matrix is reset each time the
     * F_ML function is recalculated.
     *
     * @serial Can be null.
     */
    private DoubleMatrix2D implCovarC;

    /**
     * Replaced by implCovarMeasC. Please do not delete. Required for
     * serialization backward compatibility.
     *
     * @serial
     */
    private double[][] implCovarMeas;

    /**
     * The covariance matrix of the measured variables only. May be null if
     * implCovar has not been calculated yet. This is the submatrix of
     * implCovar, restricted to just the measured variables. It is recalculated
     * each time the F_ML function is recalculated.
     *
     * @serial Can be null.
     */
    private DoubleMatrix2D implCovarMeasC;

    /**
     * The list of freeMappings. This is an unmodifiable list. It is fixed (up
     * to order) by the SemPm. This must be in the same order as
     * this.freeParameters.
     *
     * @serial Cannot be null.
     */
    private List<Mapping> freeMappings;

    /**
     * The list of fixed parameters (Unmodifiable). This must be in the same
     * order as this.fixedParameters.
     *
     * @serial Cannot be null.
     */
    private List<Mapping> fixedMappings;

    /**
     * Stores the standard errors for the parameters.  May be null.
     *
     * @serial Can be null.
     */
    private double[] standardErrors;

    /**
     * True iff setting parameters to out-of-bound values throws exceptions.
     *
     * @serial Any value.
     */
    private boolean parameterBoundsEnforced = true;

    /**
     * True iff this SemIm is estimated.
     *
     * @serial Any value.
     */
    private boolean estimated = false;

    /**
     * Used for some linear algebra calculations.
     */
    private transient Algebra algebra;

    /**
     * True just in case the graph for the SEM is cyclic.
     */
    private boolean cyclic;
    private transient double logDetSample;

    //=============================CONSTRUCTORS============================//

    public SemIm(SemPm semPm) {
        if (semPm == null) {
            throw new NullPointerException("Sem PM must not be null.");
        }

        this.semPm = new SemPm(semPm);
        this.variableNodes =
                Collections.unmodifiableList(semPm.getVariableNodes());
        this.measuredNodes =
                Collections.unmodifiableList(semPm.getMeasuredNodes());

        int numVars = this.variableNodes.size();
        this.edgeCoefC = new SparseDoubleMatrix2D(numVars, numVars);
        this.errCovarC = new SparseDoubleMatrix2D(numVars, numVars);
        this.variableMeans = new double[numVars];
        this.variableMeansStdDev = new double[numVars];

        this.freeParameters = initFreeParameters();
        this.freeMappings = createMappings(getFreeParameters());
        this.fixedParameters = initFixedParameters();
        this.fixedMappings = createMappings(getFixedParameters());
        this.meanParameters = initMeanParameters();

        // Set variable means to 0.0 pending the program creating the SemIm
        // setting them. I.e. by default they are set to 0.0.
        for (int i = 0; i < numVars; i++) {
            variableMeans[i] = 0;
            variableMeansStdDev[i] = Double.NaN;
        }

        this.cyclic = semPm.getGraph().existsDirectedCycle();
        initializeValues();
    }

    /**
     * Constructs a SEM model using the given SEM PM and sample covariance
     * matrix.
     */
    public SemIm(SemPm semPm, CovarianceMatrix covMatrix) {
        this(semPm);
        setCovMatrix(covMatrix);
    }

    /**
     * Special hidden constructor to generate updated models.
     */
    private SemIm(SemIm semIm, DoubleMatrix2D covariances,
                  DoubleMatrix1D means) {
        this(semIm);

        if (covariances.rows() != covariances.columns()) {
            throw new IllegalArgumentException(
                    "Expecting covariances to be square.");
        }

        if (!MatrixUtils.isSymmetricPositiveDefinite(covariances)) {
            throw new IllegalArgumentException("Covariances must be symmetric " +
                    "positive definite.");
        }

//        for (int i = 0; i < covariances.rows(); i++) {
//            if (covariances.get(i, i) < 0) {
//                throw new IllegalArgumentException(
//                        "Variances must be non-negative.");
//            }
//        }

        if (means.size() != semPm.getVariableNodes().size()) {
            throw new IllegalArgumentException(
                    "Number of means does not equal " + "number of variables.");
        }

        if (covariances.rows() != semPm.getVariableNodes().size()) {
            throw new IllegalArgumentException(
                    "Dimension of covariance matrix " +
                            "does not equal number of variables.");
        }

        this.errCovarC = covariances;
        this.variableMeans = means.toArray();

        this.freeParameters = initFreeParameters();
        this.freeMappings = createMappings(getFreeParameters());
        this.fixedParameters = initFixedParameters();
        this.fixedMappings = createMappings(getFixedParameters());
    }

    /**
     * Returns a variant of the current model with the given covariance matrix
     * and means. Used for updating.
     */
    public SemIm updatedIm(DoubleMatrix2D covariances, DoubleMatrix1D means) {
        return new SemIm(this, covariances, means);
    }

    /**
     * Copy constructor.
     *
     * @throws RuntimeException if the given SemIm cannot be serialized and
     *                          deserialized correctly.
     */
    public SemIm(SemIm semIm) {
        try {

            // We make a deep copy of semIm and then copy all of its fields
            // into this SEM IM. Otherwise, it's just too HARD to make a deep copy!
            // (Complain, complain.) jdramsey 4/20/2005
            SemIm _semIm = (SemIm) new MarshalledObject(semIm).get();

            semPm = _semIm.semPm;
            variableNodes = _semIm.variableNodes;
            measuredNodes = _semIm.measuredNodes;
            freeParameters = _semIm.freeParameters;
            fixedParameters = _semIm.fixedParameters;
            meanParameters = _semIm.meanParameters;
            edgeCoefC = _semIm.edgeCoefC;
            errCovarC = _semIm.errCovarC;
            variableMeans = _semIm.variableMeans;
            variableMeansStdDev = _semIm.variableMeansStdDev;
            sampleCovarC = _semIm.sampleCovarC;
            sampleSize = _semIm.sampleSize;
            implCovarC = _semIm.implCovarC;
            implCovarMeasC = _semIm.implCovarMeasC;
            freeMappings = _semIm.freeMappings;
            fixedMappings = _semIm.fixedMappings;
            standardErrors = _semIm.standardErrors;
            parameterBoundsEnforced = _semIm.parameterBoundsEnforced;
            estimated = _semIm.estimated;
            cyclic = _semIm.cyclic;
        }
        catch (IOException e) {
            throw new RuntimeException("SemIm could not be deep cloned.", e);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("SemIm could not be deep cloned.", e);
        }
    }

    /**
     * Constructs a new SEM IM with the given graph, retaining parameter values
     * from <code>semIm</code> for nodes of the same name and edges connecting
     * nodes of the same names.
     *
     * @param semIm The old SEM IM.
     * @param graph The graph for the new SEM IM.
     * @return The new SEM IM, retaining values from <code>semIm</code>.
     */
    public static SemIm retainValues(SemIm semIm, SemGraph graph) {
        SemPm newSemPm = new SemPm(graph);
        SemIm newSemIm = new SemIm(newSemPm);

        for (Parameter p1 : newSemIm.getSemPm().getParameters()) {
            Node nodeA = semIm.getSemPm().getGraph().getNode(p1.getNodeA().getName());
            Node nodeB = semIm.getSemPm().getGraph().getNode(p1.getNodeB().getName());

            for (Parameter p2 : semIm.getSemPm().getParameters()) {
                if (p2.getNodeA() == nodeA && p2.getNodeB() == nodeB && p2.getType() == p1.getType()) {
                    newSemIm.setParamValue(p1, semIm.getParamValue(p2));
                }
            }
        }

        newSemIm.sampleSize = semIm.sampleSize;
        return newSemIm;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemIm serializableInstance() {
        return new SemIm(SemPm.serializableInstance());
    }

    //==============================PUBLIC METHODS=========================//

    /**
     * Sets the sample covariance matrix for this Sem as a submatrix of the
     * given matrix. The variable names used in the SemPm for this model must
     * all appear in this CovarianceMatrix.
     */
    public void setCovMatrix(CovarianceMatrix covMatrix) {
        if (covMatrix == null) {
            throw new NullPointerException(
                    "Covariance matrix must not be null.");
        }

        CovarianceMatrix covMatrix2 = fixVarOrder(covMatrix);
        this.sampleCovarC = covMatrix2.getMatrix().copy();
        this.sampleSize = covMatrix2.getSampleSize();

        if (this.sampleSize < 0) {
            throw new IllegalArgumentException(
                    "Sample size out of range: " + sampleSize);
        }
    }

    /**
     * Calculates the covariance matrix of the given DataSet and sets the sample
     * covariance matrix for this model to a subset of it. The measured variable
     * names used in the SemPm for this model must all appear in this data set.
     */
    public void setDataSet(RectangularDataSet dataSet) {
        setCovMatrix(new CovarianceMatrix(dataSet));
    }

    /**
     * Returns the Digraph which describes the causal structure of the Sem.
     */
    public SemPm getSemPm() {
        return this.semPm;
    }

    /**
     * Returns an array containing the current values for the free parameters,
     * in the order in which the parameters appear in getFreeParameters(). That
     * is, getFreeParamValues()[i] is the value for getFreeParameters()[i].
     */
    public double[] getFreeParamValues() {
        double[] paramValues = new double[freeMappings().size()];

        for (int i = 0; i < freeMappings().size(); i++) {
            Mapping mapping = freeMappings().get(i);
            paramValues[i] = mapping.getValue();
        }

        return paramValues;
    }

    /**
     * Sets the values of the free parameters (in the order in which they appear
     * in getFreeParameters()) to the values contained in the given array. That
     * is, params[i] is the value for getFreeParameters()[i].
     */
    public void setFreeParamValues(double[] params) {
        if (params.length != getNumFreeParams()) {
            throw new IllegalArgumentException("The array provided must be " +
                    "of the same length as the number of free parameters.");
        }

        for (int i = 0; i < freeMappings().size(); i++) {
            Mapping mapping = freeMappings().get(i);
            mapping.setValue(params[i]);
        }
    }

    /**
     * Gets the value of a single free parameter, or Double.NaN if the parameter
     * is not in this
     *
     * @throws IllegalArgumentException if the given parameter is not a free
     *                                  parameter in this model.
     */
    public double getParamValue(Parameter parameter) {
        if (parameter == null) {
            throw new NullPointerException();
        }

        if (getFreeParameters().contains(parameter)) {
            int index = getFreeParameters().indexOf(parameter);
            Mapping mapping = this.freeMappings.get(index);
            return mapping.getValue();
        } else if (getFixedParameters().contains(parameter)) {
            int index = getFixedParameters().indexOf(parameter);
            Mapping mapping = this.fixedMappings.get(index);
            return mapping.getValue();
        } else if (getMeanParameters().contains(parameter)) {
            int index = getMeanParameters().indexOf(parameter);
            return variableMeans[index];
        }

//        return Double.NaN;
        throw new IllegalArgumentException(
                "Not a parameter in this model: " + parameter);
    }

    /**
     * Sets the value of a single free parameter to the given value.
     *
     * @throws IllegalArgumentException if the given parameter is not a free
     *                                  parameter in this model.
     */
    public void setParamValue(Parameter parameter, double value) {
        if (getFreeParameters().contains(parameter)) {
            // Note this assumes the freeMappings are in the same order as the
            // free parameters.
            int index = getFreeParameters().indexOf(parameter);
            Mapping mapping = this.freeMappings.get(index);
            mapping.setValue(value);
        } else if (getMeanParameters().contains(parameter)) {
            int index = getMeanParameters().indexOf(parameter);
            variableMeans[index] = value;
        } else {
            throw new IllegalArgumentException("That parameter cannot be set in " +
                    "this model: " + parameter);
        }
    }

    /**
     * Sets the value of a single free parameter to the given value.
     *
     * @throws IllegalArgumentException if the given parameter is not a free
     *                                  parameter in this model.
     */
    public void setFixedParamValue(Parameter parameter, double value) {
        if (!getFixedParameters().contains(parameter)) {
            throw new IllegalArgumentException(
                    "Not a fixed parameter in " + "this model: " + parameter);
        }

        // Note this assumes the fixedMappings are in the same order as the
        // fixed parameters.
        int index = getFixedParameters().indexOf(parameter);
        Mapping mapping = this.fixedMappings.get(index);
        mapping.setValue(value);
    }

    public double getErrCovar(Node x) {
        SemGraph graph = getSemPm().getGraph();
        Node exogenousX = graph.getExogenous(x);
        return getParamValue(exogenousX, exogenousX);
    }

    public double getErrCovar(Node x, Node y) {
        SemGraph graph = getSemPm().getGraph();
        Node exogenousX = graph.getExogenous(x);
        Node exogenousY = graph.getExogenous(y);
        return getParamValue(exogenousX, exogenousY);
    }

    public double getEdgeCoef(Node x, Node y) {
        return getParamValue(x, y);
    }

    public double getEdgeCoef(Edge edge) {
        return getEdgeCoef(edge.getNode1(), edge.getNode2());
    }


    public void setErrCovar(Node x, double value) {
        SemGraph graph = getSemPm().getGraph();
        Node exogenousX = graph.getExogenous(x);
        setParamValue(exogenousX, exogenousX, value);
    }

    public void setEdgeCoef(Node x, Node y, double value) {
        setParamValue(x, y, value);
    }

    public boolean existsEdgeCoef(Node x, Node y) {
        if (x == y) {
            return false;
        }
        else {
            return semPm.getCoefficientParameter(x, y) != null;
        }
    }

    public void setErrCovar(Node x, Node y, double value) {
        SemGraph graph = getSemPm().getGraph();
        Node exogenousX = graph.getExogenous(x);
        Node exogenousY = graph.getExogenous(y);
        setParamValue(exogenousX, exogenousY, value);
    }

    /**
     * Sets the mean associated with the given node.
     */
    public void setMean(Node node, double mean) {
        int index = variableNodes.indexOf(node);
        variableMeans[index] = mean;
    }

    /**
     * Sets the mean associated with the given node.
     */
    public void setMeanStandardDeviation(Node node, double mean) {
        int index = variableNodes.indexOf(node);
        variableMeansStdDev[index] = mean;
    }

    /**
     * Sets the intercept. For acyclic SEMs only.
     *
     * @throws UnsupportedOperationException if called on a cyclic SEM.
     */
    public void setIntercept(Node node, double intercept) {
        if (isCyclic()) {
            throw new UnsupportedOperationException("Setting and getting of " +
                    "intercepts is supported for acyclic SEMs only. The internal " +
                    "parameterizations uses variable means; the relationship " +
                    "between variable means and intercepts has not been fully " +
                    "worked out for the cyclic case.");
        }


        SemGraph semGraph = getSemPm().getGraph();
        List<Node> tierOrdering = semGraph.getTierOrdering();

        double[] intercepts = new double[tierOrdering.size()];

        for (int i = 0; i < tierOrdering.size(); i++) {
            Node _node = tierOrdering.get(i);
            intercepts[i] = getIntercept(_node);
        }

        intercepts[tierOrdering.indexOf(node)] = intercept;

        for (int i = 0; i < tierOrdering.size(); i++) {
            Node _node = tierOrdering.get(i);

            List<Node> parents = semGraph.getParents(_node);

            double weightedSumOfParentMeans = 0.0;

            for (Node parent : parents) {
                if (parent.getNodeType() == NodeType.ERROR) {
                    continue;
                }

                double coef = getEdgeCoef(parent, _node);
                double mean = getMean(parent);
                weightedSumOfParentMeans += coef * mean;
            }

            double mean = weightedSumOfParentMeans + intercepts[i];
            setMean(_node, mean);
        }
    }

    /**
     * Returns the intercept. For acyclic SEMs only.
     *
     * @throws UnsupportedOperationException if called on a cyclic SEM.
     * @return the intercept, for acyclic models, or Double.NaN otherwise.
     */
    public double getIntercept(Node node) {
        if (isCyclic()) {
            return Double.NaN;
//            throw new UnsupportedOperationException("Setting and getting of " +
//                    "intercepts is supported for acyclic SEMs only. The internal " +
//                    "parameterizations uses variable means; the relationship " +
//                    "between variable means and intercepts has not been fully " +
//                    "worked out for the cyclic case.");
        }

        SemGraph semGrapb = getSemPm().getGraph();
        List<Node> parents = semGrapb.getParents(node);

        double weightedSumOfParentMeans = 0.0;

        for (Node parent : parents) {
            if (parent.getNodeType() == NodeType.ERROR) {
                continue;
            }

            double coef = getEdgeCoef(parent, node);
            double mean = getMean(parent);
            weightedSumOfParentMeans += coef * mean;
        }

        double mean = getMean(node);
        double intercept = mean - weightedSumOfParentMeans;
        return round(intercept, 10);
    }

    /**
     * Returns the value of the mean assoc iated with the given node.
     */
    public double getMean(Node node) {
        int index = variableNodes.indexOf(node);
        return variableMeans[index];
    }

    /**
     * Returns the means for variables in order.
     */
    public double[] getMeans() {
        double[] means = new double[variableMeans.length];
        System.arraycopy(variableMeans,  0, means,  0, variableMeans.length);
        return means;
    }

    /**
     * Returns the value of the mean associated with the given node.
     */
    public double getMeanStdDev(Node node) {
        int index = variableNodes.indexOf(node);
        return variableMeansStdDev[index];
    }

    /**
     * Returns the value of the variance associated with the given node.
     */
    public double getVariance(Node node) {
        if (getSemPm().getGraph().isExogenous(node)) {
//            if (node.getNodeType() == NodeType.ERROR) {
            Parameter parameter = getSemPm().getVarianceParameter(node);

            // This seems to be required to get the show/hide error terms
            // feature to work in the SemImEditor.
            if (parameter == null) {
                return Double.NaN;
            }

            return getParamValue(parameter);
        } else {
            int index = variableNodes.indexOf(node);
            DoubleMatrix2D impliedCovar = getImplCovar();
            return impliedCovar.get(index, index);
        }
    }

    /**
     * Returns the value of the standard deviation associated with the given
     * node.
     */
    public double getStdDev(Node node) {
        return Math.sqrt(getVariance(node));
    }

    /**
     * Gets the value of a single free parameter to the given value, where the
     * free parameter is specified by the endpoint nodes of its edge in the
     * graph. Note that coefficient parameters connect elements of
     * getVariableNodes(), whereas variance and covariance parameters connect
     * elements of getExogenousNodes(). (For variance parameters, nodeA and
     * nodeB are the same.)
     *
     * @throws IllegalArgumentException if the given parameter is not a free
     *                                  parameter in this model or if there is
     *                                  no parameter connecting nodeA with nodeB
     *                                  in this model.
     */
    public double getParamValue(Node nodeA, Node nodeB) {
        Parameter parameter = null;

        if (nodeA == nodeB) {
            parameter = getSemPm().getVarianceParameter(nodeA);
        }

        if (parameter == null) {
            parameter = getSemPm().getCovarianceParameter(nodeA, nodeB);
        }

        if (parameter == null) {
            parameter = getSemPm().getCoefficientParameter(nodeA, nodeB);
        }

        if (parameter == null) {
            return Double.NaN;
        }

        if (!getFreeParameters().contains(parameter)) {
            return Double.NaN;
        }

        return getParamValue(parameter);
    }

    /**
     * Sets the value of a single free parameter to the given value, where the
     * free parameter is specified by the endpoint nodes of its edge in the
     * graph. Note that coefficient parameters connect elements of
     * getVariableNodes(), whereas variance and covariance parameters connect
     * elements of getExogenousNodes(). (For variance parameters, nodeA and
     * nodeB are the same.)
     *
     * @throws IllegalArgumentException if the given parameter is not a free
     *                                  parameter in this model or if there is
     *                                  no parameter connecting nodeA with nodeB
     *                                  in this model, or if value is Double.NaN.
     */
    public void setParamValue(Node nodeA, Node nodeB, double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Parameters must be set to " +
                    "defined values (not NaN).");
        }

        Parameter parameter = null;

        if (nodeA == nodeB) {
            parameter = getSemPm().getVarianceParameter(nodeA);
        }

        if (parameter == null) {
            parameter = getSemPm().getCoefficientParameter(nodeA, nodeB);
        }

        if (parameter == null) {
            parameter = getSemPm().getCovarianceParameter(nodeA, nodeB);
        }

        if (parameter == null) {
            throw new IllegalArgumentException("There is no parameter in " +
                    "model for an edge from " + nodeA + " to " + nodeB + ".");
        }

        if (!this.getFreeParameters().contains(parameter)) {
            throw new IllegalArgumentException(
                    "Not a free parameter in " + "this model: " + parameter);
        }

        setParamValue(parameter, value);
    }

    /**
     * Returns the (unmodifiable) list of free parameters in the model.
     */

    public List<Parameter> getFreeParameters() {
        return freeParameters;
    }


    /**
     * Returns the number of free parameters.
     */
    public int getNumFreeParams() {
        return getFreeParameters().size();
    }

    /**
     * Returns the (unmodifiable) list of fixed parameters in the model.
     */
    public List<Parameter> getFixedParameters() {
        return this.fixedParameters;
    }

    public List<Parameter> getMeanParameters() {
        return this.meanParameters;
    }

    /**
     * Returns the number of free parameters.
     */
    public int getNumFixedParams() {
        return getFixedParameters().size();
    }

    /**
     * The list of measured and latent nodes for the semPm. (Unmodifiable.)
     */
    public List<Node> getVariableNodes() {
        return variableNodes;
    }

    /**
     * The list of measured nodes for the semPm. (Unmodifiable.)
     */
    public List<Node> getMeasuredNodes() {
        return measuredNodes;
    }

    /**
     * Returns the sample size (that is, the sample size of the CovarianceMatrix
     * provided at construction time).
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Returns a copy of the matrix of edge coefficients. Note that
     * edgeCoef[i][j] is the coefficient of the edge from
     * getVariableNodes().get(i) to getVariableNodes().get(j), or 0.0 if this
     * edge is not in the graph. The values of these may be changed, but the
     * array itself may not.
     */
    public DoubleMatrix2D getEdgeCoef() {
        return edgeCoef().copy();
    }

    /**
     * Returns a copy of the matrix of error covariances. Note that
     * errCovar[i][j] is the covariance of the error term of
     * getExoNodes().get(i) and getExoNodes().get(j), with the special case
     * (duh!) that errCovar[i][i] is the variance of getExoNodes.get(i). The
     * values of these may be changed, but the array itself may not.
     */
    public DoubleMatrix2D getErrCovar() {
        return errCovar().copy();
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

    /**
     * Returns a copy of the sample covariance matrix.
     */
    public DoubleMatrix2D getSampleCovar() {
        return sampleCovar() == null ? null : sampleCovar().copy();
    }

    /**
     * The value of the maximum likelihood function for the current the model
     * (Bollen 107). To optimize, this should be minimized.
     */
    public double getFml1() {
        DoubleMatrix2D implCovarMeas; // Do this once.

        try {
            implCovarMeas = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        DoubleMatrix2D sampleCovar = sampleCovar();

        double logDetSigma = logDet(implCovarMeas);
        double traceSSigmaInv = traceSSigmaInv(sampleCovar, implCovarMeas);
        double logDetSample = logDetSample();
        int pPlusQ = getMeasuredNodes().size();

//        System.out.println("Sigma = " + implCovarMeas);
//        System.out.println("Sample = " + sampleCovar);

//        System.out.println("log(det(sigma)) = " + logDetSigma + " trace = " + traceSSigmaInv
//         + " log(det(sample)) = " + logDetSample + " p plus q = " + pPlusQ);

        double fml = logDetSigma + traceSSigmaInv - logDetSample - pPlusQ;

//        System.out.println("FML = " + fml);

        if (Math.abs(fml) < 1e-14) {
            fml = 0.0;
        }

//        System.out.println(fml);

        return fml;
    }


    public double getFml() {
        DoubleMatrix2D sigma; // Do this once.

        try {
            sigma = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        DoubleMatrix2D s = sampleCovar();

        DoubleMatrix2D sInv = new Algebra().inverse(s);

        DoubleMatrix2D prod = new Algebra().mult(sigma, sInv);
        DoubleMatrix2D identity = DoubleFactory2D.dense.identity(s.rows());
        prod.assign(identity, PlusMult.plusMult(-1));
        double trace = MatrixUtils.trace(new Algebra().mult(prod, prod));
        double f = 0.5 * trace;

//        System.out.println(f);

        return f;
    }

    public double getFml3() {
        DoubleMatrix2D sigma; // Do this once.

        try {
            sigma = implCovarMeas();
        } catch (Exception e) {
//            e.printStackTrace();
            return Double.NaN;
        }

        DoubleMatrix2D s = sampleCovar();
        DoubleMatrix2D x = DoubleFactory2D.dense.identity(s.rows());
        DoubleMatrix2D sigmaInv = new Algebra().inverse(sigma);
        DoubleMatrix2D prod = new Algebra().mult(s, sigmaInv);
        x.assign(prod, PlusMult.plusMult(-1));
        double trace = MatrixUtils.trace(new Algebra().mult(x, x));
        final double f = 0.5 * trace;
//        System.out.println(f);
        return f;
    }


    /**
     * The negative  of the log likelihood function for the current model, with
     * the constant chopped off. (Bollen 134). This is an alternative, more
     * efficient, optimization function to Fml which produces the same result
     * when minimized.
     */
    public double getNegTruncLL() {
        DoubleMatrix2D impliedCovarMeas = implCovarMeas();

        // Using (n - 1) / n * s as in Bollen p. 134 causes sinkholes to open
        // up immediately. Not sure why.
        DoubleMatrix2D sampleCovar = sampleCovar();
        int n = getSampleSize();
        double logDetSigma = logDet(impliedCovarMeas);
        double traceSSigmaInv = traceSSigmaInv(sampleCovar, impliedCovarMeas);
        return (n / 2.0) * (logDetSigma + traceSSigmaInv);
    }

    private double logDetSample() {
        if (logDetSample == 0.0 && getSampleCovar() != null) {
            double det = MatrixUtils.determinant(getSampleCovar());
            logDetSample = Math.log(det);
        }

        return logDetSample;
    }

    /**
     * Returns the BIC score of the model.
     */
    public double gelBicScore() {
        double fml = getFml();
        int freeParams = getNumFreeParams();
        int sampleSize = getSampleSize();

        return -fml - (freeParams * Math.log(sampleSize));
    }

    /**
     * Returns the chi square value for the model.
     */
    public double getChiSquare() {
        return (getSampleSize() - 1) * getFml();
    }

//    /**
//     * Returns the degrees of freedom for the model.
//     */
//    public int getDof() {
//        return semPm.getDof();
////        return (getMeasuredNodes().size() * (getMeasuredNodes().size() + 1)) /
////                2 - getNumFreeParams();
//    }

    /**
     * Returns the p-value for the model.
     */
    public double getPValue() {
        double pValue = 1.0 - ProbUtils.chisqCdf(getChiSquare(), semPm.getDof());
//        System.out.println("P value = " + pValue);
        return pValue;
//        return (1.0 - Probability.chiSquare(getChiSquare(), semPm.getDof()));
    }

    /**
     * This simulate method uses the implied covariance metrix directly to
     * simulate data, instead of going tier by tier. It should work for cyclic
     * graphs as well as acyclic graphs.
     */
    public RectangularDataSet simulateData(int sampleSize, boolean latentDataSaved) {
        return simulateDataCyclic(sampleSize, latentDataSaved);
//        return simulateDataAcyclic(sampleSize);
    }

    /**
     * This simulate method uses the implied covariance metrix directly to
     * simulate data, instead of going tier by tier. It should work for cyclic
     * graphs as well as acyclic graphs.
     *
     * @param sampleSize      how many data points in sample
     * @param latentDataSaved
     * @param sampleSeed      a seed for random number generation
     */
    public RectangularDataSet simulateData(int sampleSize, boolean latentDataSaved,
                                           long sampleSeed) {
        return simulateDataCyclic(sampleSize, new SeededRandomUtil(sampleSeed),
                latentDataSaved);
    }

    /**
     * Iterates through all parameters, picking values for them from the
     * distributions that have been set for them.
     */
    public final void initializeValues() {
        do {
            for (Mapping fixedMapping : fixedMappings) {
                Parameter parameter = fixedMapping.getParameter();
                fixedMapping.setValue(initialValue(parameter));
            }

            for (Mapping freeMapping : freeMappings) {
                Parameter parameter = freeMapping.getParameter();
                freeMapping.setValue(initialValue(parameter));
            }
        } while (!MatrixUtils.isSymmetricPositiveDefinite(errCovarC));
    }

    /**
     * Returns the standard error for the given parameter
     * @param parameter
     * @param maxFreeParams
     * @return
     */
    public double getStandardError(Parameter parameter, int maxFreeParams) {
        if (getFreeParameters().contains(parameter)) {
            if (getNumFreeParams() <= maxFreeParams) {
                if (sampleCovar() == null) {
                    this.standardErrors = null;
                    return Double.NaN;
                }

                int index = getFreeParameters().indexOf(parameter);
                return standardErrors()[index];
            } else {
                return Double.NaN;
            }
        } else if (getFixedParameters().contains(parameter)) {
            return 0.0;
        }

        throw new IllegalArgumentException(
                "That is not a parameter of this model: " + parameter);
    }

    public List<Node> listUnmeasuredLatents() {
        return unmeasuredLatents(getSemPm());
    }

    private List<Node> unmeasuredLatents(SemPm semPm) {
        SemGraph graph = semPm.getGraph();

        List<Node> unmeasuredLatents = new LinkedList<Node>();

        NODES:
        for (Node node : graph.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                for (Node child : graph.getChildren(node)) {
                    if (child.getNodeType() == NodeType.MEASURED) {
                        continue NODES;
                    }
                }

                unmeasuredLatents.add(node);
            }
        }

        return unmeasuredLatents;
    }


    public double getTValue(Parameter parameter, int maxFreeParams) {
        return getParamValue(parameter) /
                getStandardError(parameter, maxFreeParams);
    }

    public double getPValue(Parameter parameter, int maxFreeParams) {
        double tValue = getTValue(parameter, maxFreeParams);
        int df = getSampleSize() - 1;
        return 2.0 * (1.0 - ProbUtils.tCdf(Math.abs(tValue), df));
    }

    public boolean isParameterBoundsEnforced() {
        return parameterBoundsEnforced;
    }

    public void setParameterBoundsEnforced(boolean parameterBoundsEnforced) {
        this.parameterBoundsEnforced = parameterBoundsEnforced;
    }

    public boolean isEstimated() {
        return estimated;
    }

    public void setEstimated(boolean estimated) {
        this.estimated = estimated;
    }

    public boolean isCyclic() {
        return cyclic;
    }


    /**
     * Returns the variable by the given name, or null if none exists.
     *
     * @throws NullPointerException if name is null.
     */
    public Node getVariableNode(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        List<Node> variables = getVariableNodes();

        for (Node variable : variables) {
            if (name.equals(variable.getName())) {
                return variable;
            }
        }

        return null;
    }

    /**
     * Returns a string representation of the Sem (pretty detailed).
     */
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\nSem");

        buf.append("\n\n\tVariable nodes:\n");
        buf.append("\t");
        buf.append(getVariableNodes());

        buf.append("\n\n\tmeasuredNodes:\n");
        buf.append("\t");
        buf.append(getMeasuredNodes());

        buf.append("\n\n\tedgeCoef:\n");
        buf.append(MatrixUtils.toString(edgeCoef().toArray()));

        buf.append("\n\n\terrCovar:\n");
        buf.append(MatrixUtils.toString(getErrCovar().toArray()));

        buf.append("\n\n\tVariable means:\n");

        for (int i = 0; i < getVariableNodes().size(); i++) {
            buf.append("\n\tmean(");
            buf.append(getVariableNodes().get(i));
            buf.append(") = ");
            buf.append(variableMeans[i]);
        }

        buf.append("\n\n\tsampleSize = ");
        buf.append("\t");
        buf.append(this.sampleSize);

        if (sampleCovarC == null) {
            buf.append("\n\n\tsample cov **not specified**");
        } else {
            buf.append("\n\n\tsample cov:\n");
            buf.append(MatrixUtils.toString(getSampleCovar().toArray()));
        }

        buf.append("\n\n\timplCovar:\n");
        buf.append(MatrixUtils.toString(implCovar().toArray()));

        buf.append("\n\n\timplCovarMeas:\n");
        buf.append(MatrixUtils.toString(implCovarMeas().toArray()));

        if (sampleCovarC != null) {
            buf.append("\n\n\tmodel chi square = ");
            buf.append(getChiSquare());

            buf.append("\n\tmodel dof = ");
            buf.append(semPm.getDof());

            buf.append("\n\tmodel p-value = ");
            buf.append(getPValue());
        }

        buf.append("\n\n\tfree mappings:\n");
        for (int i = 0; i < this.freeMappings.size(); i++) {
            Mapping iMapping = this.freeMappings.get(i);
            buf.append("\n\t");
            buf.append(i);
            buf.append(". ");
            buf.append(iMapping);
        }

        buf.append("\n\n\tfixed mappings:\n");
        for (int i = 0; i < this.fixedMappings.size(); i++) {
            Mapping iMapping = this.fixedMappings.get(i);
            buf.append("\n\t");
            buf.append(i);
            buf.append(". ");
            buf.append(iMapping);
        }

        return buf.toString();
    }

    //==============================PRIVATE METHODS========================//

    private DoubleMatrix2D errCovar() {
        return this.errCovarC;
    }

    private DoubleMatrix2D implCovar() {
        computeImpliedCovar();
        return this.implCovarC;
    }

    private DoubleMatrix2D implCovarMeas() {
        computeImpliedCovar();
        return this.implCovarMeasC;
    }

    private List<Parameter> initFreeParameters() {
        return Collections.unmodifiableList(semPm.getFreeParameters());
    }

    /**
     * Returns a random value from the appropriate distribution for the given
     * parameter.
     */
    private double initialValue(Parameter parameter) {
        if (parameter.isInitializedRandomly()) {
            return parameter.getDistribution().nextRandom();
        } else {
            return parameter.getStartingValue();
        }
    }

    /**
     * Returns the (unmodifiable) list of parameters (type Param).
     */
    private List<Mapping> freeMappings() {
        return this.freeMappings;
    }

    /**
     * @return A submatrix of <code>covMatrix</code> with the order of its
     *         variables the same as in <code>semPm</code>.
     * @throws IllegalArgumentException if not all of the variables of
     *                                  <code>semPm</code> are in <code>covMatrix</code>.
     */
    private CovarianceMatrix fixVarOrder(CovarianceMatrix covMatrix) {
        List<String> varNamesList = new ArrayList<String>();

        for (int i = 0; i < getMeasuredNodes().size(); i++) {
            Node node = getMeasuredNodes().get(i);
            varNamesList.add(node.getName());
        }

        //System.out.println("CovarianceMatrix var order: " + varNamesList);

        String[] measuredVarNames = varNamesList.toArray(new String[0]);
        return covMatrix.getSubmatrix(measuredVarNames);
    }

    /**
     * Creates an unmodifiable list of freeMappings in the same order as the
     * given list of parameters.
     */
    private List<Mapping> createMappings(List<Parameter> parameters) {
        List<Mapping> mappings = new ArrayList<Mapping>();
        SemGraph graph = getSemPm().getGraph();

        for (Parameter parameter : parameters) {
            Node nodeA = graph.getVarNode(parameter.getNodeA());
            Node nodeB = graph.getVarNode(parameter.getNodeB());

            int i = getVariableNodes().indexOf(nodeA);
            int j = getVariableNodes().indexOf(nodeB);

            if (parameter.getType() == ParamType.COEF) {
                Mapping mapping =
                        new Mapping(this, parameter, edgeCoef(), i, j);
                mappings.add(mapping);
            } else if (parameter.getType() == ParamType.VAR) {
                Mapping mapping =
                        new Mapping(this, parameter, errCovar(), i, i);
                mappings.add(mapping);
            } else if (parameter.getType() == ParamType.COVAR) {
                Mapping mapping =
                        new Mapping(this, parameter, errCovar(), i, j);
                mappings.add(mapping);
            }
        }

        return Collections.unmodifiableList(mappings);
    }

    private List<Parameter> initFixedParameters() {
        List<Parameter> fixedParameters = new ArrayList<Parameter>();

        for (Parameter _parameter : getSemPm().getParameters()) {
            ParamType type = _parameter.getType();

            if (type == ParamType.VAR || type == ParamType.COVAR || type == ParamType.COEF) {
                if (_parameter.isFixed()) {
                    fixedParameters.add(_parameter);
                }
            }
        }

        return Collections.unmodifiableList(fixedParameters);
    }

    private List<Parameter> initMeanParameters() {
        List<Parameter> meanParameters = new ArrayList<Parameter>();

        for (Parameter param : getSemPm().getParameters()) {
            if (param.getType() == ParamType.MEAN) {
                meanParameters.add(param);
            }
        }

        return Collections.unmodifiableList(meanParameters);
    }

    /**
     * Computes the implied covariance matrices of the Sem. There are two:
     * <code>implCovar </code> contains the covariances of all the variables and
     * <code>implCovarMeas</code> contains covariance for the measured variables
     * only.
     */
    private void computeImpliedCovar() {
        DoubleMatrix2D edgeCoefT = getAlgebra().transpose(edgeCoef());

        // Note. Since the sizes of the temp matrices in this calculation
        // never change, we ought to be able to reuse them.
        this.implCovarC = MatrixUtils.impliedCovarC(edgeCoefT, errCovar());

        // Submatrix of implied covar for measured vars only.
        int size = getMeasuredNodes().size();
        this.implCovarMeasC = new DenseDoubleMatrix2D(size, size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Node iNode = getMeasuredNodes().get(i);
                Node jNode = getMeasuredNodes().get(j);

                int _i = getVariableNodes().indexOf(iNode);
                int _j = getVariableNodes().indexOf(jNode);

                this.implCovarMeasC.set(i, j, this.implCovarC.get(_i, _j));
            }
        }
    }

    private double logDet(DoubleMatrix2D implCovarMeas) {
        return Math.log(MatrixUtils.determinant(implCovarMeas));
    }

    private double traceSSigmaInv(DoubleMatrix2D s,
                                  DoubleMatrix2D sigma) {

        // Note that at this point the sem and the sample covar MUST have the
        // same variables in the same order.
        DoubleMatrix2D inverse = new Algebra().inverse(sigma);
        DoubleMatrix2D product = new Algebra().mult(s, inverse);

        double v = MatrixUtils.trace(product);

        if (v < 0) {
            throw new IllegalArgumentException("Trace was negative.");
        }

        return v;
    }

//    private double traceSSigmaInv2(DoubleMatrix2D s,
//                                  DoubleMatrix2D sigma) {
//
//        // Note that at this point the sem and the sample covar MUST have the
//        // same variables in the same order.
//        DoubleMatrix2D inverse = new Algebra().inverse(sigma);
//
//        for (int i = 0; i < sigma.rows(); i++) {
//            for (int j = 0; j < sigma.columns(); j++) {
//                if (sigma.get(i, j) < 1e-10) {
//                    sigma.set(i, j, 0);
//                }
//            }
//        }
//
//        System.out.println("Sigma = " + sigma);
//
//        for (int i = 0; i < inverse.rows(); i++) {
//            for (int j = 0; j < inverse.columns(); j++) {
//                if (inverse.get(i, j) < 1e-10) {
//                    inverse.set(i, j, 0);
//                }
//            }
//        }
//
//        System.out.println("Inverse of signa = " + inverse);
//
//        for (int i = 0; i < getFreeParameters().size(); i++) {
//            System.out.println(i + ". " + getFreeParameters().get(i));
//        }
//
//        DoubleMatrix2D product = new Algebra().mult(s, inverse);
//
//        double v = MatrixUtils.trace(product);
//
//        if (v < 0) {
//            throw new IllegalArgumentException("Trace was negative.");
//        }
//
//        return v;
//    }

    private DoubleMatrix2D sampleCovar() {
        return this.sampleCovarC;
    }

    private DoubleMatrix2D edgeCoef() {
        return this.edgeCoefC;
    }

    private double[] standardErrors() {
        if (this.standardErrors == null) {
            SemStdErrorEstimator estimator = new SemStdErrorEstimator();
            estimator.computeStdErrors(this);
            this.standardErrors = estimator.getStdErrors();
        }
        return this.standardErrors;
    }

    private RectangularDataSet simulateDataCyclic(int sampleSize, boolean latentDataSaved) {
        return simulateDataCyclic(sampleSize,
                PersistentRandomUtil.getInstance(), latentDataSaved);
    }

    /**
     * Simulates data from this Sem using a Cholesky decomposition of the
     * implied covariance matrix. This method works even when the underlying
     * graph is cyclic.
     *
     * @param sampleSize      the number of rows of data to simulate.
     * @param latentDataSaved True iff data for latents should be saved.
     */
    private RectangularDataSet simulateDataCyclic(int sampleSize,
                                                  RandomUtil randomUtil,
                                                  boolean latentDataSaved) {
//        List<Node> measuredNodes = getMeasuredNodes();
        List<Node> variables = new LinkedList<Node>();

        if (latentDataSaved) {
            for (Node node : getVariableNodes()) {
                variables.add(node);
            }
        } else {
            for (Node node : getMeasuredNodes()) {
                variables.add(node);
            }
        }

        List<Node> newVariables = new ArrayList<Node>();

        for (Node node : variables) {
            newVariables.add(new ContinuousVariable(node.getName()));
        }

        RectangularDataSet dataSet = new ColtDataSet(sampleSize, newVariables);
        DoubleMatrix2D cholesky = MatrixUtils.choleskyC(implCovar());

        // Simulate the data by repeatedly calling the Cholesky.exogenousData
        // method. Store only the data for the measured variables.
        for (int row = 0; row < sampleSize; row++) {
            double rowData[] = exogenousData(cholesky, randomUtil);

            for (int col = 0; col < variables.size(); col++) {
                int index = getVariableNodes().indexOf(variables.get(col));
                dataSet.setDouble(row, col,
                        rowData[index] + variableMeans[col]);
            }
        }

        return dataSet;
    }

    /**
     * This simulates data by picking random values for the exogenous terms and
     * percolating this information down through the SEM, assuming it is
     * acyclic. Fast for large simulations but hangs for cyclic models.
     *
     * @param sampleSize > 0.
     * @return the simulated data set.
     */
    private RectangularDataSet simulateDataAcyclic(int sampleSize) {
        List<Node> variables = new LinkedList<Node>();
        List<Node> variableNodes = getVariableNodes();

        for (Node node : variableNodes) {
            ContinuousVariable var = new ContinuousVariable(node.getName());
            variables.add(var);
        }

        RectangularDataSet dataSet = new ColtDataSet(sampleSize, variables);

        // Create some index arrays to hopefully speed up the simulation.
        SemGraph graph = getSemPm().getGraph();
        List<Node> tierOrdering = graph.getTierOrdering();

        int[] tierIndices = new int[variableNodes.size()];

        for (int i = 0; i < tierIndices.length; i++) {
            tierIndices[i] = variableNodes.indexOf(tierOrdering.get(i));
        }

        int[][] _parents = new int[variables.size()][];

        for (int i = 0; i < variableNodes.size(); i++) {
            Node node = variableNodes.get(i);
            List<Node> parents = graph.getParents(node);

            for (Iterator<Node> j = parents.iterator(); j.hasNext();) {
                Node _node = j.next();

                if (_node.getNodeType() == NodeType.ERROR) {
                    j.remove();
                }
            }

            _parents[i] = new int[parents.size()];

            for (int j = 0; j < parents.size(); j++) {
                Node _parent = parents.get(j);
                _parents[i][j] = variableNodes.indexOf(_parent);
            }
        }

        // Do the simulation.
        for (int row = 0; row < sampleSize; row++) {
            for (int i = 0; i < tierOrdering.size(); i++) {
                int col = tierIndices[i];
                double value =
                        PersistentRandomUtil.getInstance().nextGaussian() *
                                errCovarC.get(col, col);

                for (int j = 0; j < _parents[col].length; j++) {
                    int parent = _parents[col][j];
                    value += dataSet.getDouble(row, parent) *
                            edgeCoefC.get(parent, col);
                }

                value += variableMeans[col];
                dataSet.setDouble(row, col, value);
            }
        }

        return dataSet;
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
    private double[] exogenousData(DoubleMatrix2D cholesky,
                                   RandomUtil randomUtil) {

        // Step 1. Generate breitWigner samples.
        double exoData[] = new double[cholesky.rows()];

        for (int i = 0; i < exoData.length; i++) {
            exoData[i] = randomUtil.nextGaussian();
        }

        // Step 2. Multiply by cholesky to get correct covariance.
        double point[] = new double[exoData.length];

        if (exoData.length > 0) {
            point[0] = cholesky.getQuick(0, 0) * exoData[0];
        }

        for (int i = 1; i < exoData.length; i++) {
            double sum = 0.0;

            for (int j = 0; j <= i; j++) {
                sum += cholesky.getQuick(i, j) * exoData[j];
            }

            point[i] = sum;
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

        if (semPm == null) {
            throw new NullPointerException();
        }

        if (variableNodes == null) {
            throw new NullPointerException();
        }

        if (measuredNodes == null) {
            throw new NullPointerException();
        }

        // Translate old data formats into new.
        if (edgeCoef != null) {
            edgeCoefC = new DenseDoubleMatrix2D(edgeCoef);
            edgeCoef = null;
        }

        if (errCovar != null) {
            errCovarC = new DenseDoubleMatrix2D(errCovar);
            errCovar = null;
        }

        if (sampleCovar != null) {
            sampleCovarC = new DenseDoubleMatrix2D(sampleCovar);
            sampleCovar = null;
        }

        if (implCovar != null) {
            implCovarC = new DenseDoubleMatrix2D(implCovar);
            implCovar = null;
        }

        if (implCovarMeas != null) {
            implCovarMeasC = new DenseDoubleMatrix2D(implCovarMeas);
            implCovarMeas = null;
        }

        if (variableMeans == null) {
            throw new NullPointerException();
        }

        if (freeParameters == null) {
            throw new NullPointerException();
        }

        if (freeMappings == null) {
            throw new NullPointerException();
        }

        if (fixedParameters == null) {
            throw new NullPointerException();
        }

        if (fixedMappings == null) {
            throw new NullPointerException();
        }

        if (meanParameters == null) {
            meanParameters = initMeanParameters();
        }

        if (sampleSize < 0) {
            throw new IllegalArgumentException(
                    "Sample size out of range: " + sampleSize);
        }
    }

    private Algebra getAlgebra() {
        if (algebra == null) {
            algebra = new Algebra();
        }

        return algebra;
    }

    private double round(double value, int decimalPlace) {
        double power_of_ten = 1;
        while (decimalPlace-- > 0) {
            power_of_ten *= 10.0;
        }
        return Math.round(value * power_of_ten)
                / power_of_ten;
    }

}

