package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.MatrixUtils;

import java.text.NumberFormat;
import java.util.List;

/**
 * Implements a regression model from tabular continuous data.
 *
 * @author Joseph Ramsey
 */
public class RegressionDatasetGeneralized implements Regression {

    /**
     * The number formatter used for all numbers.
     */
    private NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * The data set.
     */
    private DoubleMatrix2D data;

    /**
     * The variables.
     */

    private List<Node> variables;

    /**
     * The significance level for determining which regressors are significant
     * based on their p values.
     */
    private double alpha = 0.05;

    /**
     * The graph of significant regressors into the target.
     */
    private Graph graph = null;

    //============================CONSTRUCTORS==========================//

    /**
     * Constructs a linear regression model for the given tabular data set.
     * @param data A rectangular data set, the relevant variables of which
     * are continuous.
     */
    public RegressionDatasetGeneralized(RectangularDataSet data) {
        this.data = data.getDoubleData();
        this.variables = data.getVariables();
    }

    public RegressionDatasetGeneralized(DoubleMatrix2D data, List<Node> variables) {
        this.data = data;
        this.variables = variables;
    }

    //===========================PUBLIC METHODS========================//

    /**
     * Sets the alpha level for deciding which regressors are significant
     * based on their p values.
     */
    @Override
	public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the graph of signifiocant regressors into the target.
     * @return This graph.
     */
    @Override
	public Graph getGraph() {
        return this.graph;
    }

    /**
     * Regresses the target on the given regressors.
     * @param target The target variable.
     * @param regressors The regressor variables.
     * @return The regression plane, specifying for each regressors its
     * coefficeint, se, t, and p values, and specifying the same for the
     * constant.
     */
    @Override
	public RegressionResult regress(Node target, List<Node> regressors) {
        int n = data.rows();
        int k = regressors.size() + 1;

        int _target = variables.indexOf(target);
        int[] _regressors = new int[regressors.size()];

        for (int i = 0; i < regressors.size(); i++) {
            _regressors[i] = variables.indexOf(regressors.get(i));
        }

        int rows[] = new int[data.rows()];
        for (int i = 0; i < rows.length; i++) rows[i] = i;

//        DoubleMatrix2D y = data.viewSelection(rows, new int[]{_target}).copy();
        DoubleMatrix2D xSub = data.viewSelection(rows, _regressors);


//        DoubleMatrix2D y = data.subsetColumns(Arrays.asList(target)).getDoubleData();
//        RectangularDataSet rectangularDataSet = data.subsetColumns(regressors);
//        DoubleMatrix2D xSub = rectangularDataSet.getDoubleData();
        DoubleMatrix2D X = new DenseDoubleMatrix2D(xSub.rows(),  xSub.columns() + 1);

        for (int i = 0; i < X.rows(); i++) {
            for (int j = 0; j < X.columns(); j++) {
                if (j == 0) {
                    X.set(i, j, 1);
                }
                else {
                    X.set(i, j, xSub.get(i, j - 1));
                }
            }
        }

//        for (int i = 0; i < zList.size(); i++) {
//            zCols[i] = getVariables().indexOf(zList.get(i));
//        }

//        int[] zRows = new int[data.rows()];
//        for (int i = 0; i < data.rows(); i++) {
//            zRows[i] = i;
//        }

        DoubleMatrix1D y = data.viewColumn(_target);
        DoubleMatrix2D Xt = new Algebra().transpose(X);
        DoubleMatrix2D XtX = new Algebra().mult(Xt, X);
        DoubleMatrix2D G = MatrixUtils.ginverse(XtX);

        DoubleMatrix2D Xt2 = Xt.like();
        Xt2.assign(Xt);
        DoubleMatrix2D GXt = new Algebra().mult(G, Xt2);

        DoubleMatrix1D b = new Algebra().mult(GXt, y);

        DoubleMatrix1D yPred = new Algebra().mult(X, b);

        DoubleMatrix1D xRes = yPred.copy().assign(y, Functions.minus);

        double rss = rss(X, y, b);
        double se = Math.sqrt(rss / (n - k));
        double tss = tss(y);
        double r2 = 1.0 - (rss / tss);

//        DoubleMatrix1D sqErr = new DenseDoubleMatrix1D(y.columns());
//        DoubleMatrix1D t = new DenseDoubleMatrix1D(y.columns());
//        DoubleMatrix1D p = new DenseDoubleMatrix1D(y.columns());
//
//        for (int i = 0; i < 1; i++) {
//            double _s = se * se * xTxInv.get(i, i);
//            double _se = Math.sqrt(_s);
//            double _t = b.get(i) / _se;
//            double _p = 2 * (1.0 - ProbUtils.tCdf(Math.abs(_t), n - k));
//
//            sqErr.set(i, _se);
//            t.set(i, _t);
//            p.set(i, _p);
//        }
//
//        this.graph = createOutputGraph(target.getName(), y, regressors, p);
//
        String[] vNames = new String[regressors.size()];

        for (int i = 0; i < regressors.size(); i++) {
            vNames[i] = regressors.get(i).getName();
        }
//
//        double[] bArray = b.toArray();
//        double[] tArray = t.toArray();
//        double[] pArray = p.toArray();
//        double[] seArray = sqErr.toArray();


        return new RegressionResult(false, vNames, n,
                b.toArray(), new double[0], new double[0], new double[0], r2, rss, alpha, yPred, xRes);
    }

    //=======================PRIVATE METHODS================================//

    private Graph createOutputGraph(String target, DoubleMatrix2D x,
                                   List<Node> regressors, DoubleMatrix1D p) {
        // Create output graph.
        Node targetNode = new GraphNode(target);

        Graph graph = new EdgeListGraph();
        graph.addNode(targetNode);

        for (int i = 0; i < x.columns(); i++) {
            String variableName = (i > 0) ? regressors.get(i - 1).getName() : "const";

            //Add a node and edge to the output graph for significant predictors:
            if (p.get(i) < alpha) {
                Node predictorNode = new GraphNode(variableName);
                graph.addNode(predictorNode);
                Edge newEdge = new Edge(predictorNode, targetNode,
                        Endpoint.TAIL, Endpoint.ARROW);
                graph.addEdge(newEdge);
            }
        }

        return graph;
    }

    private String createResultString(int n, int k, double rss, double r2,
                                      DoubleMatrix2D x, List<Node> regressors,
                                      DoubleMatrix2D b, DoubleMatrix1D se,
                                      DoubleMatrix1D t, DoubleMatrix1D p) {
        // Create result string.
        String rssString = nf.format(rss);
        String r2String = nf.format(r2);
        String summary = "\n REGRESSION RESULT";
        summary += "\n n = " + n + ", k = " + k + ", alpha = " + alpha + "\n";
        summary += " SSE = " + rssString + "\n";
        summary += " R^2 = " + r2String + "\n\n";
        summary += " VAR\tCOEF\tSE\tT\tP\n";

        for (int i = 0; i < x.columns(); i++) {
            // Note: the first column contains the regression constants.
            String variableName = (i > 0) ? regressors.get(i - 1).getName() : "const";

            summary += " " + variableName + "\t" + nf.format(b.get(i, 0)) +
                    "\t" + nf.format(se.get(i)) + "\t" + nf.format(t.get(i)) +
                    "\t" + nf.format(p.get(i)) + "\t" +
                    ((p.get(i) < alpha) ? "significant " : "") + "\n";
        }
        return summary;
    }

    /**
     * Calculates the residual sum of squares for parameter data x, actual
     * values y, and regression coefficients b--i.e., for each point in the
     * data, the predicted value for that point is calculated, and then it is
     * subtracted from the actual value. The sum of the squares of these
     * difference values over all points in the data is calculated and
     * returned.
     *
     * @param x the array of data.
     * @param y the target vector.
     * @param b the regression coefficients.
     * @return the residual sum of squares.
     */
    private double rss(DoubleMatrix2D x, DoubleMatrix1D y, DoubleMatrix1D b) {
        double rss = 0.0;

        for (int i = 0; i < x.rows(); i++) {
            double yH = 0.0;

            for (int j = 0; j < b.size(); j++) {
                yH += b.get(j) * x.get(i, j);
            }

            double d = y.get(i) - yH;

            rss += d * d;
        }

        return rss;
    }

    private double tss(DoubleMatrix1D y) {
        // first calculate the mean
        double mean = 0.0;

        for (int i = 0; i < y.size(); i++) {
            mean += y.get(i);
        }

        mean /= (y.size());

        double ssm = 0.0;

        for (int i = 0; i < y.size(); i++) {
            double d = mean - y.get(i);
            ssm += d * d;
        }

        return ssm;
    }
}