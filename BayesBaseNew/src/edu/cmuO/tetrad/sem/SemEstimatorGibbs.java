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

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.List;

/**
 * Implements the Gibbs sampler apporach to obtain samples of arbitrary size
 * from the posterior distribution over the parameters of a SEM given a
 * continuous dataset and a SemPm. Point estimates, standard deviations and
 * interval estimates for the parameters can be computed from these samples. See
 * "Bayesian Estimation and Testing of Structural Equation Models" by Scheines,
 * Hoijtink and Boomsma, Psychometrika, v. 64, no. 1.
 *
 * @author Frank Wimberly
 */
public final class SemEstimatorGibbs implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The SemPm containing the graph and the parameters to be estimated.
     *
     * @serial Cannot be null.
     */
    private SemPm semPm;

    /**
     * The parameters of the SEM (i.e. edge coeffs, error cov, etc.
     */
    private List parameters;

    private double[] parameterMeans;
    private double[][] parameterCovariances;
    private ParamConstraint[] paramConstraints;

    /**
     * Frank--I needed to put this back to avoid serialization problems...Joe
     *
     * @serial @deprecated
     */
    private RectangularDataSet dataSet;

    /**
     * An instance containing the parameters of this run.  These will eventually
     * be specified by the user via the GUI.
     */
    private SemEstimatorGibbsParams params;

    /**
     * The initial semIm, obtained via params.
     */
    private SemIm startIm;

    private DoubleMatrix2D priorCov;

    //private DoubleMatrix2D sampleCovar;

    /**
     * The most recently estimated model, or null if no model has been estimated
     * yet.
     *
     * @serial Can be null.
     */
    private SemIm estimatedSem;

    private boolean flatPrior;

    //=============================CONSTRUCTORS============================//

    /**
     * Returns a new SemEstimator for the given SemPm and continuous data set.
     * (Uses a default optimizer.)
     *
     * @param semPm a SemPm specifying the graph and parameterization for the
     *              model.
     * @param params an instance which contains values of the parameters of this
     *              run.
     */
    public SemEstimatorGibbs(SemPm semPm, SemEstimatorGibbsParams params) {
        //this.dataSet = dataSet;
        this.semPm = semPm;
        this.params = params;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemEstimatorGibbs serializableInstance() {
        return new SemEstimatorGibbs(null, null);
    }

    //==============================PUBLIC METHODS=========================//

    /**
     * Runs the estimator on the data and SemPm passed in through the
     * constructor.
     */
    public void estimate() {  //dogibbs in pascal

        //In the comments, getgibsprefs, PRIORINIT, GIBBSINIT, FORMAPPROXDIST,
        //DRAWFROMAPPROX refer to procedure in the Pascal version from which
        //this was adapted.  The same is true of the private methods such
        //as brent, neglogpost, etc.

        System.out.println("SemEstimatorGibbs.estimate() lives!!!");
        boolean lrtest = false;

        boolean wantbug = false;
        boolean wantstop = false;

        //Next 6 lines quivalent to getgibsprefs in pascal
        this.flatPrior = params.isFlatPrior();
        int numIterations = params.getNumIterations();
        double stretch1 = params.getStretch();
        double stretch2 = 1.0;
        double tol = params.getTolerance();
        RandomUtil ran = params.getRan();
        double priorVariance = 16.0;
        this.startIm = params.getStartIm();
        //this.sampleCovar = startIm.getSampleCovar();

        parameters = semPm.getParameters();
        int numParameters = parameters.size();
        parameterMeans = new double[numParameters];
        parameterCovariances = new double[numParameters][numParameters];

        paramConstraints = new ParamConstraint[numParameters];

        //PRIORINIT

        //Make sure we have the right parameters:
        System.out.println("Parameters of SEM in order:");
        List parametersList = semPm.getParameters();
        for (int i = 0; i < parametersList.size(); i++) {
            System.out.println(parametersList.get(i));
        }

        if (flatPrior) {
            Parameter param = null;
            for (int i = 0; i < numParameters; i++) {
                param = (Parameter) parametersList.get(i);
                parameterMeans[i] = 0.0; //Should come from params

                //Default parameter constraints.  The user should have the
                // option to change these via the GUI
                if (param.getType() == ParamType.VAR) {
                    paramConstraints[i] = new ParamConstraint(startIm, param,
                            ParamConstraintType.GT, 0.0);
                }
                else {
                    paramConstraints[i] = new ParamConstraint(startIm, param,
                            ParamConstraintType.NONE, 0.0);
                }

                for (int j = 0; j < numParameters; j++) {
                    if (i == j && !(param.isFixed())) {
                        parameterCovariances[i][j] = priorVariance;
                    }
                    else {
                        parameterCovariances[i][j] = 0.0;
                    }
                }
            }

            this.priorCov = new DenseDoubleMatrix2D(parameterCovariances);

        }
        else {   //Informative prior
            return;
        }
        //END PRIORINIT

        //GIBBSINIT
        DoubleMatrix2D impliedCovMatrix = startIm.getImplCovar();
        System.out.println("Imlplied covariance matrix of start IM");
        System.out.println(impliedCovMatrix);
        //GIBBSINIT DONE

        int nparams = parameters.size();
        double[] sums = new double[nparams];
        double[] means = new double[nparams];

        SemIm posteriorIm = new SemIm(startIm);      //Fix BUG100?

        for(int iter = 1; iter <= numIterations; iter++) {
            //if(iter > 1) break;  //DEBUG Only!!
            for(int param = 0; param < numParameters; param++) {

                Parameter p = (Parameter) parameters.get(param);
                ParamConstraint constraint = paramConstraints[param];

                if (!p.isFixed()) {
                    //FORMAPPROXDIST begin
                    double number;
                    if (constraint.getParam2() == null) {
                        number = constraint.getNumber();
                    }
                    else {
                        number = startIm.getParamValue(constraint.getParam2());
                    }
                    double ax, bx, cx;

                    if (constraint.getType() == ParamConstraintType.NONE)
                    {    // getbrentstarts
                        //getbrentstarts none2 case
                        ax = -500.0;
                        bx = 0.0;
                        cx = 500.0;
                    }
                    else if (constraint.getType() == ParamConstraintType.GT) {
                        //getbrentstarts gt case
                        ax = number;
                        cx = number + 500.0;
                        bx = (ax + cx) / 2.0;
                    }
                    else if (constraint.getType() == ParamConstraintType.LT) {
                        //gerbrentstarts lt case
                        cx = number;
                        ax = number - 500.0;
                        bx = (ax + cx) / 2.0;
                    }
                    else if (constraint.getType() == ParamConstraintType.EQ) {
                        bx = number;
                        ax = number - 500.0;
                        cx = number + 500.0;
                    }
                    else {     //same as constraint type NONE
                        ax = -500.0;
                        bx = 0.0;
                        cx = 500.0;
                    }

                    // end of getbrentstarts segment

                    //call to brent routine

                    double[] mean = new double[1];
                    double dmean = -brent(param, ax, bx, cx, tol, mean);

                    double gap = -0.005;
                    double denom = -1.0;

                    while (denom < 0.0) {
                        gap = -2.0 * gap;
                        double dmeanplus = neglogpost(param, mean[0] + gap);
                        denom = dmean + dmeanplus;
                    }

                    double vr = stretch1 * (0.5 * gap * gap) / denom;
                    //FORMAPPROXDIST end

                    //DRAWFROMAPPROX begin
                    //mean is mean[0]
                    boolean realdraw = false;
                    double rj = 0.0, accept = 0.0;
                    double cand = 0.0;
                    while (!realdraw || rj <= accept) {
                        cand = mean[0] + ran.nextGaussian() * Math.sqrt(vr);
                        realdraw = (constraint.wouldBeSatisfied(cand));
                        if (realdraw) {
                            double dcand = -1.0 * neglogpost(param, cand);
                            double numer = dcand - dmean;
                            double denom1 = (-1.0 * Math.sqrt(cand - mean[0]) /
                                    (2.0 * vr)) - Math.log(stretch2);
                            rj = numer - denom1;    //denom?
                            accept = Math.log(ran.nextDouble());
                        }
                    }

                    if (rj > 5.0) {
                        rj = 5.0;
                    }
                    //DRAWFROMAPPROX end

                    //startIm.setParamValue(p, cand);    BUG100?
                    List postFreeParams = posteriorIm.getFreeParameters();
                    Parameter ppost = (Parameter) postFreeParams.get(param);
                    posteriorIm.setParamValue(ppost, cand);
                }

            }
            /*if(iter%1000 == 1) {
                for(int i = 0; i < nparams; i++)
                    sum[i] = 0;
            }*/

            //System.out.println("Iteration number " + iter);
            //System.out.println("SEM = \n" + startIm);
            //DEBUG printing segment next 15 lines
            for (int i = 0; i < parameters.size(); i++) {
                Parameter ppost = (posteriorIm.getSemPm()).getParameters().get(i);
                //sums[i] += startIm.getParamValue(p);
                sums[i] += posteriorIm.getParamValue(ppost);
            }

            if (iter % 1000 == 0) {
                int iterm1000 = iter - 1000 + 1;
                System.out.println(
                        "Means over iters " + iterm1000 + " to " + iter);
                for (int i = 0; i < nparams; i++) {
                    means[i] = sums[i] / 1000.0;
                    System.out.print(means[i] + " ");
                    sums[i] = 0.0;
                }
                System.out.println();
            }
        }
    }

    private double sign(double a, double b) {
        double s;
        if (b >= 0.0) {
            s = Math.abs(a);
        }
        else {
            s = -Math.abs(a);
        }
        return s;
    }

    private double brent(int param, double ax, double bx, double cx, double tol,
            double[] xmin) {

        //const
        int itmax = 100;
        double cgold = 0.3819660;
        double zeps = 1.0e-10;

        //var
        double a, b, d, e, etemp;
        double fu, fv, fw, fx;
        int iter, numchars;
        double p, q, r, tol1, tol2;
        double u, v, w, x, xm;

        //begin
        if (ax < cx) {
            a = ax;
        }
        else {
            a = cx;
        }
        if (ax > cx) {
            b = ax;
        }
        else {
            b = cx;
        }
        v = bx;
        w = v;
        x = v;
        e = 0.0;

        // Not in Numerical Recipes but lack of initialization of causes error.
        d = 0.0;

        fx = neglogpost(param, x);
        fv = fx;
        fw = fx;

        for (iter = 1; iter <= itmax; iter++) {
            xm = 0.5 * (a + b);
            tol1 = tol * Math.abs(x) + zeps;
            tol2 = 2.0 * tol1;

            if (Math.abs(x - xm) <= tol2 - 0.5 * (b - a)) {
                //then goto 99;
                xmin[0] = x;
                return fx;
            }


            if (Math.abs(e) > tol1) {
                r = (x - w) * (fx - fv);
                q = (x - v) * (fx - fw);
                p = (x - v) * q - (x - w) * r;
                q = 2.0 * (q - r);

                if (q > 0.0) {
                    p = -p;
                }

                q = Math.abs(q);
                etemp = e;
                e = d;

                if ((Math.abs(p) >= Math.abs(0.5 * q * etemp)) ||
                        (p <= q * (a - x)) || (p >= q * (b - x))) {
                    e = ((x >= xm) ? a - x : b - x);
                    d = cgold * e;
                }
                else {
                    d = p / q;
                    u = x + d;
                    if ((u - a) < tol2 || (b - u) < tol2) {
                        //d = sign(tol1,xm-x);
                        d = ((xm - x) >= 0.0 ? Math.abs(tol1) : - Math.abs(
                                tol1));
                    }
                }
            }
            else {
                e = ((x >= xm) ? a - x : b - x);
                d = cgold * e;
            }

            double s = ((tol1 > - 0.0) ? Math.abs(d) : -Math.abs(d));

            u = ((Math.abs(d) >= tol1) ? x + d : x + s);
            fu = neglogpost(param, u);


            if (fu <= fx) {
                if (u >= x) {
                    a = x;
                }
                else {
                    b = x;
                }

                v = w;
                fv = fw;
                w = x;
                fw = fx;
                x = u;
                fx = fu;
            }
            else {
                if (u < x) {
                    a = u;
                }
                else {
                    b = u;
                }

                if ((fu <= fw) || (w == x)) {
                    v = w;
                    fv = fw;
                    w = u;
                    fw = fu;
                }
                else if ((fu <= fv) || (v == x) || (v == w)) {
                    v = u;
                    fv = fu;
                }
            }

            //twrite(rejfile,'BRENT - too many iters |');
            //writeparm(param, rejfile, numchars);
            //twriteeoln(rejfile);
            //99:
        }

        xmin[0] = x;
        return fx;

    }

    private double neglogpost(int param, double x) {

        //DEBUG print
        //Parameter p = (Parameter) parameters.get(param);
        //System.out.print("neglogpost INPUT:  parameter " + p + "value " + x);


        double a, b;
        boolean ok = false;

        a = negloglike(param, x, ok);
        b = 0.0;

        if (!flatPrior) {
            b = neglogprior(param, x);
        }

        double nlp;
        nlp = (flatPrior ? a : a + b);

        //DEBUG print
        //System.out.println("  OUTPUT:  " + nlp);
        return nlp;
    }

    private double negloglike(int param, double x, boolean ok) {
        Parameter p = semPm.getParameters().get(param);

        double tparm = startIm.getParamValue(p);

        if (paramConstraints[param].wouldBeSatisfied(x))  //DUBIOUS
        {
            startIm.setParamValue(p, x);
        }

        double nll = startIm.getNegTruncLL();

        //System.out.println("negloglike returning " + nll);

        startIm.setParamValue(p, tparm);

        return nll;

    }

    private double negchi2(int param, double x) {
        int numParameters = parameters.size();
        double[] xvec = new double[numParameters];
        double[] temp = new double[numParameters];
        double answer;

        answer = 0.0;
        int n = 0;
        for (int i = 0; i < numParameters; i++) {
            Parameter p = (Parameter) parameters.get(i);
            if (p.isFixed()) {
                continue;
            }
            n++;
            if (i == param) {
                xvec[n - 1] = x - parameterMeans[i];
            }
            else {
                xvec[n - 1] = startIm.getParamValue(p) - parameterMeans[i];
            }
        }

        DoubleMatrix2D invPrior = new Algebra().inverse(priorCov);

        for (int i = 0; i < n; i++) {
            temp[i] = 0.0;
            for (int col = 0; col < n; col++) {
                for (int k = 0; k < n; k++) {
                    temp[col] = temp[col] + (xvec[k] * invPrior.get(k, col));
                }
            }
        }

        for (int k = 0; k < n; k++) {
            answer += temp[k] * xvec[k];
        }

        return -answer;
    }

    private double neglogprior(int param, double x) {
        //double nlp = -negchi2(param, x)/2.0;
        return -negchi2(param, x) / 2.0;
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
        }
        else {
            SemIm sem = this.getEstimatedSem();
            buf.append("\n\n\tfml = ");

            buf.append("\n\n\tnegtruncll = ");
            buf.append(nf.format(sem.getNegTruncLL()));

            buf.append("\n\n\tmeasuredNodes:\n");
            buf.append("\t" + sem.getMeasuredNodes());

            buf.append("\n\n\tedgeCoef:\n");
            buf.append(MatrixUtils.toString(sem.getEdgeCoef().toArray()));

            buf.append("\n\n\terrCovar:\n");
            buf.append(MatrixUtils.toString(sem.getErrCovar().toArray()));
        }

        return buf.toString();
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


    /**
     * Sets the means of variables in the SEM IM based on the given data set.
     */
    private void setMeans(SemIm semIm, RectangularDataSet dataSet) {
        double[] means = new double[semIm.getSemPm().getVariableNodes().size()];
        int numMeans = means.length;

        if (dataSet == null) {
            for (int i = 0; i < numMeans; i++) {
                means[i] = 0.0;
            }
        }
        else {
            double[] sum = new double[numMeans];

            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                for (int i = 0; i < dataSet.getNumRows(); i++) {
                    sum[j] += dataSet.getDouble(i, j);
                }

                means[j] = sum[j] / dataSet.getNumRows();
            }
        }

        //Set the sample means to 0.0 or to the sample means of the columns in the dataset.
        for (int i = 0; i < semIm.getVariableNodes().size(); i++) {
            Node node = semIm.getVariableNodes().get(i);
            semIm.setMean(node, means[i]);
        }
    }

    /*
    private RectangularDataSet getDataSet() {
        return dataSet;
    }
    */

    public void setEstimatedSem(SemIm estimatedSem) {
        this.estimatedSem = estimatedSem;
    }

    private SemPm getSemPm() {
        return semPm;
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

//        if (semPm == null) {
//            throw new NullPointerException();
//        }

//        if (dataSet == null) {
//            throw new NullPointerException();
//        }
    }
}

