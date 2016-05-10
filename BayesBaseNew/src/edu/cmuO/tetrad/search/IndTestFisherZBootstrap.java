package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.NormalPdf;
import edu.cmu.tetrad.util.PersistentRandomUtil;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix2D;


public class IndTestFisherZBootstrap implements IndependenceTest {

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
    private double alpha = 0.05;


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
    private RectangularDataSet dataSet;
    private int numBootstrapSamples;
    private int bootstrapSampleSize;
    private DoubleMatrix2D[] bootstrapSamples;
    private IndependenceTest[] tests;

    public IndTestFisherZBootstrap(RectangularDataSet dataSet, double alpha, int numBootstrapSamples, int bootstrapSampleSize) {
        if (!(dataSet.isContinuous())) {
            throw new IllegalArgumentException("Data set must be continuous.");
        }

        this.covMatrix = new CovarianceMatrix(dataSet);
        this.dataSet = dataSet;

        this.variables = Collections.unmodifiableList(covMatrix.getVariables());
        setAlpha(alpha);

        this.deterministicTest = new IndTestFisherZD(dataSet, alpha);

        this.numBootstrapSamples = numBootstrapSamples;
        this.bootstrapSampleSize = bootstrapSampleSize;
        this.bootstrapSamples = new DoubleMatrix2D[numBootstrapSamples];
        this.tests = new IndependenceTest[numBootstrapSamples];

        for (int i = 0; i < numBootstrapSamples; i++) {
            DoubleMatrix2D fullData = dataSet.getDoubleData();
            bootstrapSamples[i] = getBootstrapSample(fullData, bootstrapSampleSize);
            tests[i] = new IndTestFisherZ(bootstrapSamples[i], dataSet.getVariables(), alpha);

        }

    }

    @Override
	public IndependenceTest indTestSubset(List<Node> vars) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public boolean isIndependent(Node x, Node y, List<Node> z) {
//        DoubleMatrix2D fullData = dataSet.getDoubleData();
        int[] independentGuys = new int[numBootstrapSamples];

        for (int i = 0; i < numBootstrapSamples; i++) {
//            DoubleMatrix2D data = getBootstrapSample(fullData, bootstrapSampleSize);
//            IndTestFisherZ test = new IndTestFisherZ(bootstrapSamples[i], dataSet.getVariables(), alpha);
            boolean independent = tests[i].isIndependent(x, y, z);
            independentGuys[i] = independent ? 1 : 0;
        }

        int sum = 0;
        for (int i = 0; i < numBootstrapSamples; i++) sum += independentGuys[i];
        return sum > numBootstrapSamples / 2;
    }

    @Override
	public boolean isDependent(Node x, Node y, List<Node> z) {
        return !isIndependent(x, y, z);
    }

    @Override
	public double getPValue() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
	public boolean determines(List<Node> z, Node x1) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public boolean splitDetermines(List<Node> z, Node x, Node y) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public double getAlpha() {
        return alpha;
    }

    @Override
	public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    @Override
	public String toString() {
        return "Fisher's Z Bootstrap";
    }

    /**
     * Returns a sample with replacement with the given sample size from the
     * given dataset.
     */
    private DoubleMatrix2D getBootstrapSample(DoubleMatrix2D data, int sampleSize) {
        int actualSampleSize = data.rows();

        int[] rows = new int[sampleSize];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = PersistentRandomUtil.getInstance().nextInt(actualSampleSize);
        }

        int[] cols = new int[data.columns()];
        for (int i = 0; i < cols.length; i++) cols[i] = i;

        return data.viewSelection(rows, cols);
    }


    @Override
	public RectangularDataSet getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}