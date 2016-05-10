package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Given a pattern, chooses the best DAG(s) in the pattern by calculating an
 * objective function for each DAG in the pattern and reporting the best of
 * those. Algorithm by Patrik Hoyer.
 * <p/>
 * It is assumed that the pattern is the result of a pattern search such as PC
 * or GES. In any case, it is important that the residuals be independent for
 * ICA to work.
 *
 * @author Joseph Ramsey
 */
public class AlessiosMethodSimple {
    private double alpha = 0.05;
    private int numSamples = 15;

    public AlessiosMethodSimple() {

    }

    public Graph search(RectangularDataSet dataSet, int lags) throws IllegalArgumentException {
        RectangularDataSet timeLags = DataUtils.createTimeSeriesData(dataSet, lags);

        List<Node> regressors = new ArrayList<Node>();

        for (int i = dataSet.getNumColumns(); i < timeLags.getNumColumns(); i++) {
            regressors.add(timeLags.getVariable(i));
        }

        Regression regression = new RegressionDatasetGeneralized(timeLags);
        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(dataSet.getNumRows() - lags + 1, dataSet.getNumColumns());

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node target = timeLags.getVariable(i);
            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        RectangularDataSet data = ColtDataSet.makeContinuousData(dataSet.getVariables(), residuals);

        try {
            Writer writer = new PrintWriter(new File("test_data/residualdata1.dat"));
            DataSavers.saveRectangularData(data, writer, ',');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        IndependenceTest test = new IndTestFisherZ(data, 0.03);

        Graph graph = new Cpc(test, new Knowledge()).search();
        LingamPattern pattern = new LingamPattern();
        pattern.setNumSamples(200);
        LingamPattern.Result result = pattern.search(graph, dataSet);


        System.out.println(result);

        return result.getDags().get(0);
    }

    public Graph search2(RectangularDataSet dataSet, int lags) throws IllegalArgumentException {
        double alpha = 0.03;

        RectangularDataSet timeLags = DataUtils.createTimeSeriesData(dataSet, lags + 1);
        IndependenceTest test = new IndTestFisherZ(timeLags, 0.03);

        Cpc search = new Cpc(test, timeLags.getKnowledge());
        Graph estPattern = search.search();

        LingamPattern lingamPattern = new LingamPattern();
        lingamPattern.setAlpha(alpha);
        lingamPattern.setNumSamples(200);

        List<Dag> dags = SearchGraphUtils.getDagsInPatternMeek(estPattern, timeLags.getKnowledge());
        LingamPattern.Result result1 = lingamPattern.search(dags, timeLags);
        Graph graph = result1.getDags().get(0);

        List<String> dataVarNames = dataSet.getVariableNames();
        List<String> dataVarNamesToConditionOn = new LinkedList<String>();

        for (String name : dataVarNames) {
            for (int lag = 1; lag <= lags + 1; lag++) {
                Node node = nodeAtLag(name, timeLags, lag);
                List<Node> parents = graph.getParents(node);

                for (Node parent : parents) {
//                    if (!dataVarNamesToConditionOn.contains(baseName(parent.getName()))) {
//                        dataVarNamesToConditionOn.add(baseName(parent.getName()));
//                    }

                    if (!dataVarNamesToConditionOn.contains(parent.getName())) {
                        dataVarNamesToConditionOn.add(parent.getName());
                    }
                }
            }
        }

        for (String name : new LinkedList<String>(dataVarNamesToConditionOn)) {
            String predecessor = baseName(name) + "." + lags;

            if (!dataVarNamesToConditionOn.contains(predecessor)) {
                dataVarNamesToConditionOn.add(predecessor);
            }
        }

        Collections.sort(dataVarNamesToConditionOn);
        System.out.println(dataVarNamesToConditionOn);

        List<Node> regressors = new ArrayList<Node>();

        for (int i = dataSet.getNumColumns(); i < timeLags.getNumColumns(); i++) {
            Node variable = timeLags.getVariable(i);

//            if (!dataVarNamesToConditionOn.contains(baseName(variable.getName()))) {
//                continue;
//            }

            if (!dataVarNamesToConditionOn.contains(variable.getName())) {
                continue;
            }

            regressors.add(variable);
        }

        System.out.println("Regressors = " + regressors);

        Regression regression = new RegressionDatasetGeneralized(timeLags);
        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(dataSet.getNumRows() - lags, dataSet.getNumColumns());

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node target = timeLags.getVariable(i);
            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        RectangularDataSet residualData = ColtDataSet.makeContinuousData(dataSet.getVariables(), residuals);

        try {
            Writer writer = new PrintWriter(new File("test_data/residualdata2.dat"));
            DataSavers.saveRectangularData(residualData, writer, ',');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IndependenceTest test2 = new IndTestFisherZ(residualData, 0.03);

        Graph graph2 = new Cpc(test2, new Knowledge()).search();
        LingamPattern pattern = new LingamPattern();
        pattern.setNumSamples(200);
        LingamPattern.Result result = pattern.search(graph2, residualData);

        System.out.println(result);

        return result.getDags().get(0);
    }

    private String baseName(String name) {
        return name.split("\\.")[0];
    }

    private Node nodeAtLag(String name, RectangularDataSet timeLags, int i) {
        String suffixedName = name + "." + i;
        Node variable = timeLags.getVariable(suffixedName);

        if (variable == null) {
            throw new IllegalArgumentException("There was no variable by the " +
                    "name " + variable + " in that dataset.");
        }

        return variable;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha is in range [0, 1]" + alpha);
        }

        this.alpha = alpha;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public void setNumSamples(int numSamples) {
        if (numSamples < 1) {
            throw new IllegalArgumentException("Must use at least one sample: " + numSamples);
        }

        this.numSamples = numSamples;
    }
}