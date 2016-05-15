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

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.List;

import cern.jet.stat.Descriptive;
import cern.colt.list.DoubleArrayList;

/**
 * Estimates a SemIm given a CovarianceMatrix and a SemPm. (A DataSet may be
 * substituted for the CovarianceMatrix.)
 *
 * @author Frank Wimberly
 * @author Ricardo Silva
 * @author Don Crimbchin
 * @author Joseph Ramsey
 * @version $Revision: 6509 $ $Date: 2006-01-19 17:54:39 -0500 (Thu, 19 Jan
 *          2006) $
 */
public final class SemEstimator implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The SemPm containing the graph and the parameters to be estimated.
     *
     * @serial Cannot be null.
     */
    private SemPm semPm;

    /**
     * The covariance matrix used to estimate the SemIm. Note that the variables
     * names in the covariance matrix must be in the same order as the variable
     * names in the semPm.
     *
     * @serial Cannot be null.
     */
    private CovarianceMatrix covMatrix;

    /**
     * The algorithm that minimizes the fitting function for the SEM.
     *
     * @serial Cannot be null.
     */
    private SemOptimizer semOptimizer;

    /**
     * The most recently estimated model, or null if no model has been estimated
     * yet.
     *
     * @serial Can be null.
     */
    private SemIm estimatedSem;

    /**
     * The data set being estimated from (needed to calculate means of
     * variables).  May be null in which case means are set to zero.
     *
     * @serial Can be null.
     */
    private RectangularDataSet dataSet;

    /**
     * The true SEM IM. If this is included. then its score will be printed
     * out.
     */
    private SemIm trueSemIm;

    //=============================CONSTRUCTORS============================//

    /**
     * Constructs a Sem Estimator that does default estimation.
     *
     * @param semPm   a SemPm specifying the graph and parameterization for the
     *                model.
     * @param dataSet a DataSet, all of whose variables are contained in the
     *                given SemPm. (They are identified by name.)
     */
    public SemEstimator(RectangularDataSet dataSet, SemPm semPm) {
        this(dataSet, semPm, null);
    }

    /**
     * Constructs a SEM estimator that does default estimation.
     *
     * @param semPm     a SemPm specifying the graph and parameterization for
     *                  the model.
     * @param covMatrix a CovarianceMatrix, all of whose variables are contained
     *                  in the given SemPm. (They are identified by name.)
     */
    public SemEstimator(CovarianceMatrix covMatrix, SemPm semPm) {
        this(covMatrix, semPm, null);
    }

    /**
     * Constructs a new SemEstimator that uses the specified optimizer.
     *
     * @param semPm        a SemPm specifying the graph and parameterization for
     *                     the model.
     * @param dataSet      a DataSet, all of whose variables are contained in
     *                     the given SemPm. (They are identified by name.)
     * @param semOptimizer the optimizer that optimizes the Sem.
     */
    public SemEstimator(RectangularDataSet dataSet, SemPm semPm,
                        SemOptimizer semOptimizer) {
        this(new CovarianceMatrix(dataSet), semPm, semOptimizer);
        this.dataSet = subset(dataSet, semPm);
    }

    /**
     * Constructs a new SemEstimator that uses the specified optimizer.
     *
     * @param semPm        a SemPm specifying the graph and parameterization for
     *                     the model.
     * @param covMatrix    a covariance matrix, all of whose variables are
     *                     contained in the given SemPm. (They are identified by
     *                     name.)
     * @param semOptimizer the optimizer that optimizes the Sem.
     */
    public SemEstimator(CovarianceMatrix covMatrix, SemPm semPm,
                        SemOptimizer semOptimizer) {
        if (covMatrix == null) {
            throw new NullPointerException(
                    "CovarianceMatrix must not be null.");
        }

        if (semPm == null) {
            throw new NullPointerException("SemPm must not be null.");
        }

        this.covMatrix = submatrix(covMatrix, semPm);
        this.semPm = semPm;
        this.semOptimizer = semOptimizer;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemEstimator serializableInstance() {
        return new SemEstimator(CovarianceMatrix.serializableInstance(),
                SemPm.serializableInstance());
    }

    //==============================PUBLIC METHODS=========================//

    /**
     * Runs the estimator on the data and SemPm passed in through the
     * constructor.
     */
    public void estimate() {

        //long time = System.currentTimeMillis();
        //System.out.println("Start timer.");

        // Forget any previous estimation results. (If the estimation fails,
        // the estimatedSem should be null.)
        setEstimatedSem(null);

        // Create the Sem from the SemPm and CovarianceMatrix.
        SemIm semIm = new SemIm(getSemPm(), getCovMatrix());

        // Optimize the Sem.
        semIm.setParameterBoundsEnforced(false);

        //PATCH:
        // for some reason, semOptimizer gets stuck in a infinite loop when
        // there is only one variable in the model. Right now, I'm just
        // avoiding semOptimizer when that happens.
        if (semIm.getSemPm().getGraph().getNumNodes() == 1) {
            double params[] = new double[1];
//            System.out.println(params.length);
            params[0] = getCovMatrix().getValue(0, 0);
            semIm.setFreeParamValues(params);
        } else {
            if (semOptimizer == null) {
                doDefaultOptimization(semIm);
            } else {
                getSemOptimizer().optimize(semIm);
            }

            // hack to get the best of multiple iterations (Ricardo, November 2003)
            /*SemIm best = null;
            for (int i = 0; i < 50; i++) {
               this.semOptimizer.optimize(semIm);
               if (best == null || semIm.getChiSquare() < best.getChiSquare())
                  best = semIm;
                System.out.println(semIm.getChiSquare());
            }
            semIm = best;*/
            // hack to hook up mimbuild temporarily (Ricardo, November 2003)
            /*System.out.println("Using MimBuildEstimator");
            MimBuildEstimator myEst = new MimBuildEstimator(this.covMatrix, this.semPm, 5, 10);
            myEst.estimate();
            semIm = myEst.getEstimatedSem(); */
        }

        semIm.setParameterBoundsEnforced(true);
        setMeans(semIm, getDataSet());

        // Marks semIm as estimated
        semIm.setEstimated(true);

        // Set the estimated semIm to this.
        setEstimatedSem(semIm);

//        List<Node> variables = covMatrix.getVariables();
//        for (int i = 0; i < variables.size(); i++) {
//            System.out.println("Var(" + variables.get(i) + ") = " + covMatrix.getMatrix().get(i, i));
//        }

        //System.out.println("Stop timer: " + ((System.currentTimeMillis() - time) / 30)
        //        + " seconds");

//        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
//        TetradLogger.getInstance().log("info", "Final FML = " + nf.format(semIm.getFml()));
//        TetradLogger.getInstance().log("info", "Model Chi Square = " + nf.format(semIm.getChiSquare()));
//        TetradLogger.getInstance().log("info", "Model DOF = " + nf.format(semPm.getDof()));
//        TetradLogger.getInstance().log("info", "Model P Value = " + nf.format(semIm.getPValue()));
//        TetradLogger.getInstance().log("info", "Model BIC = " + nf.format(semIm.gelBicScore()));
    }

    public SemEstimator() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void doDefaultOptimization(SemIm semIm) {
        boolean containsLatent = false;

        for (Node node : semPm.getGraph().getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                containsLatent = true;
            }
        }

//        new SemOptimizerPalCds().optimize(semIm);
//        new SemOptimizerScattershot().optimize(semIm);
//        new SemOptimizerEm().optimize(semIm);
//        new SemOptimizerRegression().optimize(semIm);
//        new SemOptimizerUncmin().optimize(semIm);
//        new SemOptimizerNrPowell().optimize(semIm);

        if (containsFixedParam() || semPm.getGraph().existsDirectedCycle() ||
                containsCovarParam(semPm)) {
            new SemOptimizerScattershot().optimize(semIm);
//            new SemOptimizerPalCds().optimize(semIm);
        } else if (containsLatent) {
            new SemOptimizerEm().optimize(semIm);
        } else {
            new SemOptimizerRegression().optimize(semIm);
        }

    }

    public void setTrueSemIm(SemIm semIm) {
        trueSemIm = new SemIm(semIm);
        trueSemIm.setCovMatrix(this.covMatrix);
    }

    private boolean containsFixedParam() {
        return new SemIm(semPm).getNumFixedParams() > 0;
    }

    /**
     * Returns the estimated SemIm. If the <code>estimate</code> method has not
     * yet been called, <code>null</code> is returned.
     */
    public SemIm getEstimatedSem() {
        return this.estimatedSem;
    }

    /**
     * Returns a string representation of the Sem.
     */
    @Override
	public String toString() {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

        StringBuffer buf = new StringBuffer();
        buf.append("\nSemEstimator");

        if (this.getEstimatedSem() == null) {
            buf.append("\n\t...SemIm has not been estimated yet.");
        } else {
            SemIm sem = this.getEstimatedSem();
            buf.append("\n\n\tfml = ");

            buf.append("\n\n\tmeasuredNodes:\n");
            buf.append("\t").append(sem.getMeasuredNodes());

            buf.append("\n\n\tedgeCoef:\n");
            buf.append(MatrixUtils.toString(sem.getEdgeCoef().toArray()));

            buf.append("\n\n\terrCovar:\n");
            buf.append(MatrixUtils.toString(sem.getErrCovar().toArray()));
        }

        return buf.toString();
    }

    //==============================PRIVATE METHODS========================//

    /**
     * @return A submatrix of <code>covMatrix</code> with the order of its
     *         variables the same as in <code>semPm</code>.
     * @throws IllegalArgumentException if not all of the variables of
     *                                  <code>semPm</code> are in <code>covMatrix</code>.
     */
    private CovarianceMatrix submatrix(CovarianceMatrix covMatrix,
                                       SemPm semPm) {
        String[] measuredVarNames = semPm.getMeasuredVarNames();

        try {
            return covMatrix.getSubmatrix(measuredVarNames);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "All of the variables from the data set " +
                            "must be in the SEM parameterized model.", e);
        }
    }

    private RectangularDataSet subset(RectangularDataSet dataSet, SemPm semPm) {
        String[] measuredVarNames = semPm.getMeasuredVarNames();
        int[] varIndices = new int[measuredVarNames.length];

        for (int i = 0; i < measuredVarNames.length; i++) {
            Node variable = dataSet.getVariable(measuredVarNames[i]);
            varIndices[i] = dataSet.getVariables().indexOf(variable);
        }

        return dataSet.subsetColumns(varIndices);
    }

    private static boolean containsCovarParam(SemPm semPm) {
        boolean containsCovarParam = false;
        List<Parameter> params = semPm.getParameters();

        for (Parameter param : params) {
            if (param.getType() == ParamType.COVAR) {
                containsCovarParam = true;
                break;
            }
        }
        return containsCovarParam;
    }

    /**
     * Sets the means of variables in the SEM IM based on the given data set.
     */
    private void setMeans(SemIm semIm, RectangularDataSet dataSet) {
        if (dataSet != null) {
            int numColumns = dataSet.getNumColumns();

            for (int j = 0; j < numColumns; j++) {
                double[] column = dataSet.getDoubleData().viewColumn(j).toArray();
                DoubleArrayList list = new DoubleArrayList(column);
                double mean = Descriptive.mean(list);

                Node node = dataSet.getVariable(j);
                Node variableNode = semIm.getVariableNode(node.getName());
                semIm.setMean(variableNode, mean);

                double standardDeviation = Descriptive.standardDeviation(
                        Descriptive.variance(list.size(),
                                Descriptive.sum(list),
                                Descriptive.sumOfSquares(list)));

                semIm.setMeanStandardDeviation(variableNode, standardDeviation);
            }
        } else if (covMatrix != null) {
            List<Node> variables = covMatrix.getVariables();

            for (Node node : variables) {
                Node variableNode = semIm.getVariableNode(node.getName());
                semIm.setMean(variableNode, 0.0);
            }
        }
    }

    private RectangularDataSet getDataSet() {
        return dataSet;
    }

    public void setEstimatedSem  (SemIm  estimatedSem) {
        this.estimatedSem = estimatedSem;
    }

    private SemPm getSemPm () {
        return semPm;
    }

    private CovarianceMatrix getCovMatrix() {
        return covMatrix;
    }

    private SemOptimizer getSemOptimizer() {
        return semOptimizer;
    }

    /**
     * Adds semantic checks to the default deserialization method. This
     * method must have the standard signature for a readObject method, and
     * the body of the method must begin with "s.defaultReadObject();".
     * Other than that, any semantic checks can be specified and do not need
     * to stay the same from version to version. A readObject method of this
     * form may be added to any class, even if Tetrad sessions were
     * previously saved out using a version of the class that didn't include
     * it. (That's what the "s.defaultReadObject();" is for. See J. Bloch,
     * Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject
            (ObjectInputStream
                    s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (covMatrix == null) {
            throw new NullPointerException();
        }

        if (semPm == null) {
            throw new NullPointerException();
        }
    }
}

