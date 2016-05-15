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

import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.MimBuildEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * Implements different tests of tetrad constraints: using Wishart's test (CPS, Wishart 1928); Bollen's test (Bollen,
 * 1990) or a more computationally intensive test that fits one/two factor Gaussian models. These tests are the core
 * statistical procedure of search algorithms BuildPureClusters and Purify.
 * <p/>
 * References:
 * <p/>
 * Bollen, K. (1990). "Outlier screening and distribution-free test for vanishing tetrads." Sociological Methods and
 * Research 19, 80-92.
 * <p/>
 * Wishart, J. (1928). "Sampling errors in the theory of two factors". British Journal of Psychology 19, 180-187.
 *
 * @author Ricardo Silva
 */

public final class ContinuousTetradTest implements TetradTest {
    private double sig;
    private double sig1;
    private double sig2;
    private double sig3;
    private double prob[];
//    private double fourthMM[][][][];
    private boolean bvalues[], outputMessage;
    private ICovarianceMatrix covMatrix;
    private CorrelationMatrix corrMatrix;
    private double rho[][];
    private TestType sigTestType;
    private int sampleSize;
    private DataSet dataSet;
    private OneFactorEstimator oneFactorEst4, oneFactorEst5, oneFactorEst6;
    private TwoFactorsEstimator twoFactorsEst4, twoFactorsEst5, twoFactorsEst6;
    private boolean modeX = false;
    private double bufferMatrix[][];
//    private Map<Tetrad, Double> tetradDifference;
    private List<Node> variables;
    BollenTingTetradTest bollenTingTest;

    public ContinuousTetradTest(DataSet dataSet, TestType sigTestType,
                                double sig) {
        if (!(sigTestType == TestType.TETRAD_WISHART ||
                sigTestType == TestType.TETRAD_BOLLEN ||
                sigTestType == TestType.GAUSSIAN_FACTOR)) {
//            sigTestType = TestType.TETRAD_WISHART;
//
            throw new IllegalArgumentException("Unexpected type: " + sigTestType);
        }

        if (dataSet == null) {
            throw new NullPointerException("Data set must not be null.");
        }

//        bollenTingTest = new BollenTingTetradTest(dataSet);

        corrMatrix = new CorrelationMatrix(dataSet);
        this.dataSet = dataSet;
        this.sigTestType = sigTestType;
        setSignificance(sig);
        this.sampleSize = dataSet.getNumRows();
        this.variables = dataSet.getVariables();
//        if (sigTestType == TestType.TETRAD_BOLLEN) {
//            setCovMatrix(new CovarianceMatrix(dataSet));
//            fourthMM = getFourthMomentsMatrix(dataSet);
//        }


        initialization();
    }

    public ContinuousTetradTest(ICovarianceMatrix covMatrix,
                                TestType sigTestType, double sig) {
        if (!(sigTestType == TestType.TETRAD_WISHART ||
                sigTestType == TestType.TETRAD_BOLLEN ||
                sigTestType == TestType.GAUSSIAN_FACTOR)) {
            throw new IllegalArgumentException("Unexpected type: " + sigTestType);
        }
        this.dataSet = null;

        bollenTingTest = new BollenTingTetradTest(covMatrix);

        this.corrMatrix = new CorrelationMatrix(covMatrix);
        this.setCovMatrix(covMatrix);
        this.sigTestType = sigTestType;
        setSignificance(sig);
        this.sampleSize = covMatrix.getSize();
        initialization();

        this.variables = covMatrix.getVariables();
    }

    public ContinuousTetradTest(CorrelationMatrix correlationMatrix,
                                TestType sigTestType, double sig) {
        if (!(sigTestType == TestType.TETRAD_WISHART ||
                sigTestType == TestType.TETRAD_BOLLEN ||
                sigTestType == TestType.GAUSSIAN_FACTOR)) {
            throw new IllegalArgumentException("Unexpected type: " + sigTestType);
        }

        if (correlationMatrix == null) {
            throw new NullPointerException();
        }

        this.dataSet = null;
        this.corrMatrix = correlationMatrix;
        this.setCovMatrix(correlationMatrix);
        this.sigTestType = sigTestType;
        setSignificance(sig);
        this.sampleSize = correlationMatrix.getSize();
        initialization();

        this.variables = correlationMatrix.getVariables();
    }

    public double getSignificance() {
        return this.sig;
    }

    public void setSignificance(double sig) {
        this.sig = sig;
        this.sig1 = sig / 3.;
        this.sig2 = 2. * sig / 3.;
        this.sig3 = sig;
    }

    public DataSet getDataSet() {
        return this.dataSet;
    }

    public CorrelationMatrix getCorrMatrix() {
        return this.corrMatrix;
    }

    @Override
    public ICovarianceMatrix getCovMatrix() {
        if (this.covMatrix != null) {
            return this.covMatrix;
        }
        if (this.dataSet != null) {
            this.covMatrix = new CovarianceMatrix(this.dataSet);
            return this.covMatrix;
        }
        return corrMatrix;
    }

    public String[] getVarNames() {
        return this.corrMatrix.getVariableNames().toArray(new String[0]);
    }

    public List<Node> getVariables() {
        if (this.variables == null) {
            if (dataSet != null) {
                this.variables = dataSet.getVariables();
            } else if (getCovMatrix() != null) {
                this.variables = getCovMatrix().getVariables();
            }
        }

        return this.variables;
    }

    public TestType getTestType() {
        return this.sigTestType;
    }

    public void setTestType(TestType sigTestType) {
        this.sigTestType = sigTestType;
    }

    private void initialization() {
        sampleSize = corrMatrix.getSampleSize();
        outputMessage = false;
        prob = new double[3];
        bvalues = new boolean[3];
        oneFactorEst4 = new OneFactorEstimator(corrMatrix, sig, 4);
        oneFactorEst5 = new OneFactorEstimator(corrMatrix, sig, 5);
        oneFactorEst6 = new OneFactorEstimator(corrMatrix, sig, 6);
        twoFactorsEst4 = new TwoFactorsEstimator(corrMatrix, sig, 4);
        twoFactorsEst5 = new TwoFactorsEstimator(corrMatrix, sig, 5);
        twoFactorsEst6 = new TwoFactorsEstimator(corrMatrix, sig, 6);
        bufferMatrix = new double[4][4];
        rho = corrMatrix.getMatrix().toArray();

        int m = corrMatrix.getVariables().size();
//        tetradDifference = new HashMap<Tetrad, Double>();

    }

    /**
     * Note: this implementation could be more optimized. This is the
     * simplest way of computing this corrMatrix, and will take exactly
     * sampleSize * (corrMatrix.getSize() ^ 4) steps.
     */

    /**
     * Sample scores: the real deal. The way by which significance is tested will vary from case to case. We are also
     * using false discovery rate to make a mild adjustment in the p-values.
     */

    public int tetradScore(int v1, int v2, int v3, int v4) {
        evalTetradDifferences(v1, v2, v3, v4);
        for (int i = 0; i < 3; i++) {
            bvalues[i] = (prob[i] >= sig);
        }
        //Order p-values for FDR (false discovery rate) decision
        Arrays.sort(prob);     // jdramsey 5/22/10

//        double tempProb;
//        if (prob[1] < prob[0] && prob[1] < prob[2]) {
//            tempProb = prob[0];
//            prob[0] = prob[1];
//            prob[1] = tempProb;
//        }
//        else if (prob[2] < prob[0] && prob[2] < prob[0]) {
//            tempProb = prob[0];
//            prob[0] = prob[2];
//            prob[2] = tempProb;
//        }
//        if (prob[2] < prob[1]) {
//            tempProb = prob[1];
//            prob[1] = prob[2];
//            prob[2] = tempProb;
//        }
        if (prob[2] <= sig3) {
            return 0;
        }
        if (prob[1] <= sig2) {
            //This is the case of 2 tetrad constraints holding, which is
            //a logical impossibility. On a future version we may come up with
            //better, more powerful ways of deciding what to do. Right now,
            //the default is to do just as follows:
//            return 1;
            return 1;
        }
        if (prob[0] <= sig1) {
//            throw new IllegalArgumentException();
            return 2;
        }
        return 3;
    }

    /**
     * Tests the tetrad (v1, v3) x (v2, v4) = (v1, v4) x (v2, v3)
     */

    public boolean tetradScore1(int v1, int v2, int v3, int v4) {
        /*if (tetradHolds(v1, v3, v4, v2) != tetradHolds(v4, v2, v1, v3)) {
            System.out.println("!");
            modeX = true;
            tetradHolds(v1, v3, v4, v2);
            System.out.println(prob[0]);
            tetradHolds(v4, v2, v1, v3);
            System.out.println(prob[0]);
            System.exit(0);
        }*/
        return tetradHolds(v1, v3, v4, v2) && !tetradHolds(v1, v3, v2, v4) &&
                !tetradHolds(v1, v4, v2, v3);
    }

    /**
     * Tests if all tetrad constraints hold
     */

    public boolean tetradScore3(int v1, int v2, int v3, int v4) {
        if (sigTestType != TestType.GAUSSIAN_FACTOR) {
            return tetradScore(v1, v2, v3, v4) == 3;
        } else {
            return oneFactorTest(v1, v2, v3, v4);
        }
    }

    public boolean tetradHolds(int v1, int v2, int v3, int v4) {
        evalTetradDifference(v1, v2, v3, v4);
        bvalues[0] = (prob[0] >= sig);
        return prob[0] >= sig;
    }

    public double tetradPValue(int v1, int v2, int v3, int v4) {
        evalTetradDifference(v1, v2, v3, v4);
        return prob[0];
    }

    public double tetradPValue(int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2) {
        evalTetradDifference(i1, j1, k1, l1, i2, j2, k2, l2);
        return prob[0];
    }


    /** --------------------------------------------------------------------------
     *  PRIVATE METHODS
     */

//    /**
//     * Note: this implementation could be more optimized. This is the simplest way of computing this matrix, and will
//     * take exactly sampleSize * (corrMatrix.getSize() ^ 4) steps.
//     */
//
//    private double[][][][] getFourthMomentsMatrix(DataSet dataSet) {
//        printlnMessage(
//                "Bollen's test preparation: starting computation of fourth moments");
//        int numVars = corrMatrix.getSize();
//        double fourthMM[][][][] = new double[numVars][numVars][numVars][numVars];
//
//        double data[][] = dataSet.getDoubleData().viewDice().toArray();
//        double means[] = new double[numVars];
//
//        for (int i = 0; i < numVars; i++) {
//            means[i] = 0.;
//        }
//
//        for (int d = 0; d < sampleSize; d++) {
//            for (int i = 0; i < numVars; i++) {
//                means[i] += data[i][d];
//            }
//        }
//
//        for (int i = 0; i < numVars; i++) {
//            means[i] /= sampleSize;
//        }
//
//        for (int i = 0; i < numVars; i++) {
//            for (int j = 0; j < numVars; j++) {
//                for (int k = 0; k < numVars; k++) {
//                    for (int t = 0; t < numVars; t++) {
//                        fourthMM[i][j][k][t] = 0.;
//                    }
//                }
//            }
//        }
//
//        for (int d = 0; d < sampleSize; d++) {
//            for (int i = 0; i < numVars; i++) {
//                for (int j = 0; j < numVars; j++) {
//                    for (int k = 0; k < numVars; k++) {
//                        for (int t = 0; t < numVars; t++) {
//                            fourthMM[i][j][k][t] += (data[i][d] - means[i]) *
//                                    (data[j][d] - means[j]) *
//                                    (data[k][d] - means[k]) *
//                                    (data[t][d] - means[t]);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < numVars; i++) {
//            for (int j = 0; j < numVars; j++) {
//                for (int k = 0; k < numVars; k++) {
//                    for (int t = 0; t < numVars; t++) {
//                        fourthMM[i][j][k][t] /= sampleSize;
//                    }
//                }
//            }
//        }
//
//        printlnMessage("Done with fourth moments");
//        return fourthMM;
//    }

    private void evalTetradDifferences(int i, int j, int k, int l) {
        switch (sigTestType) {
            case TETRAD_BASED:
            case TETRAD_BASED2:
            case TETRAD_WISHART:
                wishartEvalTetradDifferences(i, j, k, l);
                break;
            case TETRAD_BOLLEN:
                bollenEvalTetradDifferences(i, j, k, l);
                break;
            default:
                /**
                 * The other tests are only for interface with Purify. The ContinuousTetradTest class is also
                 * used as a black box of arguments passed to Purify (e.g., see BuildPureClusters code), but it does
                 * not mean its internal tetrad tests are going to be used. See Purify.scoreBasedPurify(List) to
                 * see a situation when this happens.
                 */
                assert false;
        }
    }

    private void evalTetradDifference(int i, int j, int k, int l) {
        switch (sigTestType) {
            case TETRAD_BASED:
            case TETRAD_BASED2:
            case TETRAD_WISHART:
                wishartEvalTetradDifference(i, j, k, l);
                break;
            case TETRAD_BOLLEN:
                bollenEvalTetradDifference(i, j, k, l);
                break;
            default:
                assert false;
        }
    }

    private void evalTetradDifference(int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2) {
        wishartEvalTetradDifference(i1, j1, k1, l1, i2, j2, k2, l2);
    }


    /**
     * The asymptotic Wishart test for multivariate normal variables. See Wishart (1928).
     */

    private void wishartEvalTetradDifferences(int i, int j, int k, int l) {
        double TAUijkl, TAUijlk, TAUiklj;
        double ratio;

        TAUijkl = rho[i][j] * rho[k][l] -
                rho[i][k] * rho[j][l];

        double SD = wishartTestTetradDifference(i, j, k, l);

        ratio = TAUijkl / SD;

        if (ratio > 0.0) {
            ratio = -ratio;
        }

        prob[0] = 2.0 * ProbUtils.normalCdf(ratio);

        TAUijlk = rho[i][j] * rho[k][l] -
                rho[i][l] * rho[j][k];

        SD = wishartTestTetradDifference(i, j, l, k);

        ratio = TAUijlk / SD;

        if (ratio > 0.0) {
            ratio = -ratio;
        }

        prob[1] = 2.0 * ProbUtils.normalCdf(ratio);

        TAUiklj = rho[i][k] * rho[j][l] -
                rho[i][l] * rho[j][k];

        SD = wishartTestTetradDifference(i, k, l, j);   // A C D B

        ratio = TAUiklj / SD;

        if (ratio > 0.0) {
            ratio = -ratio;
        }

        prob[2] = 2.0 * ProbUtils.normalCdf(ratio);
    }

    private void wishartEvalTetradDifference(int i, int j, int k, int l) {
        double TAUijkl;
        double ratio;

        TAUijkl = rho[i][j] * rho[k][l] - rho[i][k] * rho[j][l];

        if (modeX) {
            System.out.println("T = " + TAUijkl);
        }

        double SD = wishartTestTetradDifference(i, j, k, l);

        ratio = TAUijkl / SD;

        if (ratio > 0.0) {
            ratio = -ratio;
        }

        double pValue = 2.0 * ProbUtils.normalCdf(ratio);

        if (modeX) {
            System.out.println();
            System.out.println("T = " + TAUijkl);
            System.out.println("SD = " + SD);
            System.out.println("ratio = " + ratio);
            System.out.println("P Value = " + pValue);
            System.out.println();
        }

        prob[0] = pValue;

        TetradLogger.getInstance().log("tetrads", new Tetrad(variables.get(i),
                variables.get(j), variables.get(k), variables.get(l)).toString()
                + " = 0, p = " + pValue);
    }

    private void wishartEvalTetradDifference(int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2) {
        double TAUijkl;
        double ratio;

        TAUijkl = rho[i1][j1] * rho[k1][l1] -
                rho[i2][j2] * rho[k2][l2];

        if (modeX) {
            System.out.println("T = " + TAUijkl);
        }

        double SD = wishartTestTetradDifference(i1, j2, k2, l2);

        if (modeX) {
            System.out.println("T = " + TAUijkl);
            System.out.println("SD = " + SD);
            System.out.println();
        }
        ratio = TAUijkl / SD;

        if (ratio > 0.0) {
            ratio = -ratio;
        }

        prob[0] = 2.0 * ProbUtils.normalCdf(ratio);
    }

    private double wishartTestTetradDifference(int a, int b, int c, int d) {
//        if (tetradDifference.containsKey(new Tetrad(getVariables().get(a),
//                getVariables().get(b),
//                getVariables().get(c), getVariables().get(d)))) {
//            return tetradDifference.get(new Tetrad(getVariables().get(a),
//                getVariables().get(b),
//                getVariables().get(c), getVariables().get(d)));
//        }

        int indices[] = new int[4];
        indices[0] = a;
        indices[1] = b;
        indices[2] = c;
        indices[3] = d;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                bufferMatrix[i][j] = rho[indices[i]][indices[j]];
            }
        }
        double product1 = rho[a][a] * rho[d][d] - rho[a][d] * rho[a][d];
        double product2 = rho[b][b] * rho[c][c] - rho[b][c] * rho[b][c];
        double n = sampleSize;
        double product3 = (n + 1) / ((n - 1) * (n - 2))
                * product1 * product2;
        double determinant = MatrixUtils.determinant(bufferMatrix);
        double SD = (product3 - determinant / (n - 2));

        double diff = Math.sqrt(SD);

//        tetradDifference.put(new Tetrad(getVariables().get(a), getVariables().get(b),
//                getVariables().get(c), getVariables().get(d)), diff);

        return diff;
    }

    /**
     * The asymptotic distribution-free Bollen test. See Bollen (1990).
     */

    private void bollenEvalTetradDifferences(int i, int j, int k, int l) {
//        double TAUijkl, TAUijlk, TAUiklj;
//        double ratio;
//
//        TAUijkl = getCovMatrix().getValue(i, j) * getCovMatrix().getValue(k, l) -
//                getCovMatrix().getValue(i, k) * getCovMatrix().getValue(j, l);
//
//        double bt = bollenTetradStatistic(i, j, k, l);
//
//        ratio = TAUijkl / Math.sqrt(bt);
//
//        if (ratio > 0.0) {
//            ratio = -ratio;
//        }
//
//        prob[0] = 2.0 * ProbUtils.normalCdf(ratio);
//
//        TAUijlk = getCovMatrix().getValue(i, j) * getCovMatrix().getValue(k, l) -
//                getCovMatrix().getValue(i, l) * getCovMatrix().getValue(j, k);
//
//        bt = bollenTetradStatistic(i, j, l, k);
//
//        ratio = TAUijlk / Math.sqrt(bt);
//
//        if (ratio > 0.0) {
//            ratio = -ratio;
//        }
//
//        prob[1] = 2.0 * ProbUtils.normalCdf(ratio);
//
//        TAUiklj = getCovMatrix().getValue(i, k) * getCovMatrix().getValue(j, l) -
//                getCovMatrix().getValue(i, l) * getCovMatrix().getValue(j, k);
//
//        bt = bollenTetradStatistic(i, k, l, j);
//
//        ratio = TAUiklj / Math.sqrt(bt);
//
//        if (ratio > 0.0) {
//            ratio = -ratio;
//        }
//
//        prob[2] = 2.0 * ProbUtils.normalCdf(ratio);

        if (bollenTingTest == null) {
            if (dataSet != null) {
                bollenTingTest = new BollenTingTetradTest(dataSet);
            }
            else {
                bollenTingTest = new BollenTingTetradTest(covMatrix);
            }
        }

        Node ci = getVariables().get(i);
        Node cj = getVariables().get(j);
        Node ck = getVariables().get(k);
        Node cl = getVariables().get(l);

        bollenTingTest.calcChiSquare(new Tetrad(ci, cj, ck, cl));
        prob[0] = bollenTingTest.getPValue();

        bollenTingTest.calcChiSquare(new Tetrad(ci, cj, cl, ck));
        prob[1] = bollenTingTest.getPValue();

        bollenTingTest.calcChiSquare(new Tetrad(ci, ck, cl, cj));
        prob[2] = bollenTingTest.getPValue();
    }


    private void bollenEvalTetradDifference(int i, int j, int k, int l) {
//        double TAUijkl;
//        double ratio;
//
//        TAUijkl = getCovMatrix().getValue(i, j) * getCovMatrix().getValue(k, l) -
//                getCovMatrix().getValue(i, k) * getCovMatrix().getValue(j, l);
//
//        double bt = bollenTetradStatistic(i, j, k, l);
//
//        ratio = TAUijkl / Math.sqrt(bt);
//
//        if (ratio > 0.0) {
//            ratio = -ratio;
//        }
//
//        prob[0] = 2.0 * ProbUtils.normalCdf(ratio);

        Node ci = getVariables().get(i);
        Node cj = getVariables().get(j);
        Node ck = getVariables().get(k);
        Node cl = getVariables().get(l);

        if (bollenTingTest == null) {
            if (dataSet != null) {
                bollenTingTest = new BollenTingTetradTest(dataSet);
            }
            else {
                bollenTingTest = new BollenTingTetradTest(covMatrix);
            }
        }

        bollenTingTest.calcChiSquare(new Tetrad(ci, cj, ck, cl));
        prob[0] = bollenTingTest.getPValue();

        TetradLogger.getInstance().log("tetrads", new Tetrad(variables.get(i),
                variables.get(j), variables.get(k), variables.get(l)).toString()
                + " = 0, p = " + prob[0]);


    }

//    private double bollenTetradStatistic(int t1, int t2, int t3, int t4) {
//        if (getCovMatrix() == null) {
//            throw new NullPointerException();
//        }
//
//        if (fourthMM == null) {
//            throw new NullPointerException();
//        }
//
//        double prod2323 = getCovMatrix().getValue(t2, t3) * getCovMatrix().getValue(t2, t3) *
//                fourthMM[t1][t1][t4][t4];
//        double prod1414 = getCovMatrix().getValue(t1, t4) * getCovMatrix().getValue(t1, t4) *
//                fourthMM[t2][t2][t3][t3];
//        double prod2424 = getCovMatrix().getValue(t2, t4) * getCovMatrix().getValue(t2, t4) *
//                fourthMM[t1][t1][t3][t3];
//        double prod1313 = getCovMatrix().getValue(t1, t3) * getCovMatrix().getValue(t1, t3) *
//                fourthMM[t2][t2][t4][t4];
//        double prod2314 = getCovMatrix().getValue(t2, t3) * getCovMatrix().getValue(t1, t4) *
//                fourthMM[t1][t2][t3][t4];
//        double prod2324 = getCovMatrix().getValue(t2, t3) * getCovMatrix().getValue(t2, t4) *
//                fourthMM[t1][t1][t3][t4];
//        double prod2313 = getCovMatrix().getValue(t2, t3) * getCovMatrix().getValue(t1, t3) *
//                fourthMM[t1][t2][t4][t4];
//        double prod1233 = getCovMatrix().getValue(t1, t4) * getCovMatrix().getValue(t2, t4) *
//                fourthMM[t1][t2][t3][t3];
//        double prod1413 = getCovMatrix().getValue(t1, t4) * getCovMatrix().getValue(t1, t3) *
//                fourthMM[t2][t2][t3][t4];
//        double prod2413 = getCovMatrix().getValue(t2, t4) * getCovMatrix().getValue(t1, t3) *
//                fourthMM[t1][t2][t3][t4];
//        double cov2314 = getCovMatrix().getValue(t2, t3) * getCovMatrix().getValue(t1, t4);
//        double cov2413 = getCovMatrix().getValue(t2, t4) * getCovMatrix().getValue(t1, t3);
//        double nStat = prod2323 + prod1414 + prod2424 + prod1313
//                + 2 * (prod2314 - prod2324 - prod2313 - prod1233 - prod1413 + prod2413)
//                - 4 * Math.pow(cov2314 - cov2413, 2.);
//        double stat = nStat / sampleSize;
//        if (stat < 0.) {
//            stat = 0.000001;
//        }
//        return stat;
//    }

    void printMessage(String message) {
        if (outputMessage) {
            System.out.print(message);
        }
    }

    void printlnMessage(String message) {
        if (outputMessage) {
            System.out.println(message);
        }
    }

    void printlnMessage() {
        if (outputMessage) {
            System.out.println();
        }
    }

    void printlnMessage(boolean flag) {
        if (outputMessage) {
            System.out.println(flag);
        }
    }

    public void setCovMatrix(ICovarianceMatrix covMatrix) {
        this.covMatrix = covMatrix;
    }

    public void setBollenTest(BollenTingTetradTest bollenTingTest) {
        this.bollenTingTest = bollenTingTest;
    }

    /*
     * This class is a easy, fast way of reusing one-factor models for
     * significance testing
     */

    abstract class SimpleFactorEstimator {
        ICovarianceMatrix sampleCov, subSampleCov;
        double sig;
        int indices[], nvar;
        SemPm semPm;
        String varNames[], submatrixNames[];

        /**
         * A maximum likelihood estimate of the parameters of a one factor model with four variables. Created to
         * simplify coding in BuildPureClusters.
         */
        public SimpleFactorEstimator(ICovarianceMatrix sampleCov, double sig,
                                     int nvar) {
            this.sampleCov = sampleCov;
            this.sig = sig;
            this.nvar = nvar;
            this.varNames = sampleCov.getVariableNames().toArray(new String[0]);
            this.submatrixNames = new String[nvar];
        }

        public void refreshDataMatrix(ICovarianceMatrix sampleCov) {
            this.sampleCov = sampleCov;
            this.varNames = sampleCov.getVariableNames().toArray(new String[0]);
        }

        public void init(int indices[]) {
            Arrays.sort(indices);

            for (int i = 0; i < indices.length; i++) {
                submatrixNames[i] = varNames[indices[i]];
            }
            semPm = buildSemPm(indices);

            //For some implementation reason, semPm changes the order of the nodes:
            //it doesn't match the order in subMatrixNames anymore.
            //The following procedure is similar to fixVarOrder found in
            //other classes:
//            List<Node> semPmVars = semPm.getVariableNodes();
//            int index = 0;
//            for (Node ar : semPmVars) {
//                if (ar.getNodeType() != NodeType.LATENT) {
//                    submatrixNames[index++] = ar.toString();
//                }
//            }

            //Finally, get the correct submatrix
            subSampleCov = sampleCov.getSubmatrix(submatrixNames);
        }

        public boolean isSignificant() {
            MimBuildEstimator estimator =
                    MimBuildEstimator.newInstance(subSampleCov, semPm, 3, 1);
            estimator.estimate();
            SemIm semIm = estimator.getEstimatedSem();
            //System.out.println("Model p-value: " + semIm.getLikelihoodRatioP());
            return semIm.getPValue() > sig;
        }

        protected abstract SemPm buildSemPm(int indices[]);
    }

    class OneFactorEstimator extends SimpleFactorEstimator {
        static final long serialVersionUID = 23L;

        public OneFactorEstimator(ICovarianceMatrix sampleCov, double sig,
                                  int nvar) {
            super(sampleCov, sig, nvar);
        }

        protected SemPm buildSemPm(int[] values) {
            Graph graph = new EdgeListGraph();
            Node latent = new GraphNode("__l");
            latent.setNodeType(NodeType.LATENT);
            graph.addNode(latent);
            for (int i = 0; i < nvar; i++) {
                Node node = new GraphNode(submatrixNames[i]);
                graph.addNode(node);
                graph.addDirectedEdge(latent, node);
            }
            semPm = new SemPm(graph);
            return semPm;
        }

    }

    class TwoFactorsEstimator extends SimpleFactorEstimator {
        static final long serialVersionUID = 23L;

        int nleft;

        public TwoFactorsEstimator(ICovarianceMatrix sampleCov, double sig,
                                   int nvar) {
            super(sampleCov, sig, nvar);
        }

        public void init(int indices[], int nleft) {
            this.nleft = nleft;
            super.init(indices);
        }

        protected SemPm buildSemPm(int[] values) {
            Graph graph = new EdgeListGraph();
            Node latent1 = new GraphNode("__l1");
            Node latent2 = new GraphNode("__l2");
            latent1.setNodeType(NodeType.LATENT);
            latent2.setNodeType(NodeType.LATENT);
            graph.addNode(latent1);
            graph.addNode(latent2);
            graph.addDirectedEdge(latent1, latent2);
            for (int i = 0; i < nvar; i++) {
                Node node = new GraphNode(submatrixNames[i]);
                graph.addNode(node);
                if (i < nleft) {
                    graph.addDirectedEdge(latent1, node);
                } else {
                    graph.addDirectedEdge(latent2, node);
                }
            }
            semPm = new SemPm(graph);
            return semPm;
        }
    }

    public boolean oneFactorTest(int v1, int v2, int v3, int v4) {
        int indices[] = {v1, v2, v3, v4};
        oneFactorEst4.init(indices);
        return oneFactorEst4.isSignificant();
    }

    public boolean oneFactorTest(int v1, int v2, int v3, int v4, int v5) {
        int indices[] = {v1, v2, v3, v4, v5};
        oneFactorEst5.init(indices);
        return oneFactorEst5.isSignificant();
    }

    public boolean oneFactorTest(int v1, int v2, int v3, int v4, int v5,
                                 int v6) {
        int indices[] = {v1, v2, v3, v4, v5, v6};
        oneFactorEst6.init(indices);
        return oneFactorEst6.isSignificant();
    }

    public boolean twoFactorTest(int v1, int v2, int v3, int v4) {
        int indices[] = {v1, v2, v3, v4};
        twoFactorsEst4.init(indices, 2);
        return twoFactorsEst4.isSignificant();
    }

    public boolean twoFactorTest(int v1, int v2, int v3, int v4, int v5) {
        int indices[] = {v1, v2, v3, v4, v5};
        twoFactorsEst5.init(indices, 3);
        return twoFactorsEst5.isSignificant();
    }

    public boolean twoFactorTest(int v1, int v2, int v3, int v4, int v5,
                                 int v6) {
        int indices[] = {v1, v2, v3, v4, v5, v6};
        twoFactorsEst6.init(indices, 3);
        return twoFactorsEst6.isSignificant();
    }

    public int tempTetradScore(int v1, int v2, int v3, int v4) {
        evalTetradDifferences(v1, v2, v3, v4);
        System.out.println(prob[0]);
        System.out.println(prob[1]);
        System.out.println(prob[2]);
        for (int i = 0; i < 3; i++) {
            bvalues[i] = (prob[i] >= sig);
        }
        //Order p-values for FDR (false discovery rate) decision
        double tempProb;
        if (prob[1] < prob[0] && prob[1] < prob[2]) {
            tempProb = prob[0];
            prob[0] = prob[1];
            prob[1] = tempProb;
        } else if (prob[2] < prob[0] && prob[2] < prob[0]) {
            tempProb = prob[0];
            prob[0] = prob[2];
            prob[2] = tempProb;
        }
        if (prob[2] < prob[1]) {
            tempProb = prob[1];
            prob[1] = prob[2];
            prob[2] = tempProb;
        }
        if (prob[2] <= sig3) {
            return 0;
        }
        if (prob[1] <= sig2) {
            return 1;
        }
        if (prob[0] <= sig1) {
            //This is the case of 2 tetrad constraints holding, which is
            //a logical impossibility. On a future version we may come up with
            //better, more powerful ways of deciding what to do. Right now,
            //the default is to do just as follows:
            return 3;
        }
        return 3;
    }

}



