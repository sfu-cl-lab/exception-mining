package edu.cmu.tetrad.search;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.PersistentRandomUtil;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.sem.SemIm;

import java.util.ArrayList;
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
public class FiddlingWithPatternResiduals {
    private double alpha = 0.05;
    private int numSamples = 15;


    public FiddlingWithPatternResiduals() {

    }

    public List<Graph> search(RectangularDataSet dataSet) {
        IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

        System.out.println("DataSet vars = " + dataSet.getVariables());

        PcPattern search = new PcPattern(test, new Knowledge());
        Graph pattern = search.search();

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);

        List<Dag> dags = new ArrayList<Dag>();

        while (iterator.hasNext()) {
            dags.add(new Dag(iterator.next()));
        }

        Dag dag = dags.get(0);

        System.out.println("DAG = " + dag);

        List<Node> nodes = dag.getNodes();
        DoubleMatrix2D data = dataSet.getDoubleData();
        DoubleMatrix2D residuals = getResiduals(dag, data, dataSet.getVariables());

        DoubleMatrix2D residualsCopy = residuals.copy();

        int m = nodes.size();
        int[][] dependencies = getDependencies(nodes, residuals);

        System.out.println("Initial dependencies:");
        printDependencies(dependencies);

        int[] tried = new int[m];
        boolean changed = true;

        while (changed) {
            changed = false;

            // Find row with maximal sum in dependencies.
            int maxSum = -1;
            int maxRow = -1;

            for (int i = 0; i < m; i++) {
                if (tried[i] == 1) {
                    continue;
                }

                int sum = sumRow(i, dependencies);

                if (sum > maxSum) {
                    maxSum = sum;
                    maxRow = i;
                }
            }

            if (maxRow == -1) break;
            if (maxSum == 0) break;

            Node target = nodes.get(maxRow);
            List<Node> adj = dag.getAdjacentNodes(target);
            if (adj.isEmpty()) continue;

            int[] _adj = new int[adj.size()];
            for (int d = 0; d < adj.size(); d++) {
                Node adjNode = adj.get(d);
                _adj[d] = dataSet.getVariables().indexOf(adjNode);
            }

            tried[maxRow] = 1;

            int min = m;
            int[] minAdjChoice = null;

            DepthChoiceGenerator generator = new DepthChoiceGenerator(_adj.length, 3);
            int[] choice;

            while ((choice = generator.next()) != null) {
                int[] adjchoice = new int[choice.length];
                for (int h = 0; h < choice.length; h++) adjchoice[h] = _adj[choice[h]];

                DoubleMatrix1D newResiduals = getResiduals(maxRow, adjchoice, data, dataSet.getVariables());
                residualsCopy.viewColumn(maxRow).assign(newResiduals);
                test = new IndTestFisherZ(residualsCopy, nodes, 0.05);

                int[] rowDependencies = new int[m];

                // Update dependencies.
                for (int j = 0; j < nodes.size(); j++) {
                    if (maxRow == j) continue;

                    int independent = test.isIndependent(nodes.get(maxRow), nodes.get(j),
                            new LinkedList<Node>()) ? 0 : 1;

                    rowDependencies[j] = independent;
                }


                int sum = sumRow(maxRow, rowDependencies);

                if (sum < min && sum < maxSum) {
                    min = sum;
                    changed = true;
                    minAdjChoice = adjchoice;
                }

                if (sum == 0) {
                    break;
                }
            }

            if (minAdjChoice == null) {
                continue;
            }

            if (changed) {
                System.out.println();

                System.out.print("Advice is ");
                printParents(nodes, maxRow, minAdjChoice);

                residuals.viewColumn(maxRow).assign(residualsCopy.viewColumn(maxRow));

                dependencies = calcDependencies(nodes, residuals);
                printDependencies(dependencies);

                dependencies = calcDependencies(nodes, residuals);
            }
        }


        return new ArrayList<Graph>();
    }

    public void search2(RectangularDataSet dataSet) {
        List<Node> nodes = dataSet.getVariables();
        IndependenceTest test = new IndTestFisherZ(dataSet, 0.01);

        System.out.println("DataSet vars = " + dataSet.getVariables());

        PcPattern search = new PcPattern(test, new Knowledge());
        Graph pattern = search.search();

        DagInPatternIterator iterator = new DagInPatternIterator(pattern);

        List<Dag> dags = new ArrayList<Dag>();

        while (iterator.hasNext()) {
            dags.add(new Dag(iterator.next()));
        }

        Dag dag = dags.get(0);

        System.out.println("DAG = " + dag);

        DoubleMatrix2D data = dataSet.getDoubleData();
        int m = data.columns();
        DoubleMatrix2D residuals = getResiduals(dag, data, dataSet.getVariables());
        DoubleMatrix2D residualsCopy = residuals.copy();

        int[][] dependencies = getDependencies(dataSet.getVariables(), residuals);

        System.out.println("Initial dependencies:");
        printDependencies(dependencies);

        int index1 = -1;
        int index2 = -1;

        LOOP:
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                if (dependencies[i][j] == 1) {
                    index1 = i;
                    index2 = j;
                    break LOOP;
                }
            }
        }

        DepthChoiceGenerator g1 = new DepthChoiceGenerator(m, 2);
        int[] choice1;

        LOOP1:
        while ((choice1 = g1.next()) != null) {
            for (int h = 0; h < choice1.length; h++) {
                if (choice1[h] == index1) continue LOOP1;
            }

            DepthChoiceGenerator g2 = new DepthChoiceGenerator(m, 2);
            int[] choice2;

            LOOP2:
            while ((choice2 = g2.next()) != null) {
                for (int h = 0; h < choice2.length; h++) {
                    if (choice2[h] == index2) continue LOOP2;
                }

                if (index1 == -1 || index2 == -1) {
                    System.out.println("No dependency found.");
                    return;
                }

                RegressionResult result1 = getRegressionResult(index1, choice1, data, dataSet.getVariables());
                RegressionResult result2 = getRegressionResult(index2, choice2, data, dataSet.getVariables());

                double[] p1 = result1.getP();
                double[] p2 = result2.getP();

                double[] c1 = result1.getCoef();
                double[] c2 = result2.getCoef();

                for (int i = 0; i < p1.length; i++) {
//                    if (p1[i] > alpha) continue LOOP2;
//                    if (Math.abs(c1[i]) < 1e-5) continue LOOP2;
                }

                for (int i = 0; i < p2.length; i++) {
//                    if (p2[i] > alpha) continue LOOP2;
//                    if (Math.abs(c2[i]) < 1e-5) continue LOOP2;
                }


                DoubleMatrix1D res1 = result1.getResiduals();
                DoubleMatrix1D res2 = result2.getResiduals();

                residualsCopy.viewColumn(index1).assign(res1);
                residualsCopy.viewColumn(index2).assign(res2);
                test = new IndTestFisherZ(residualsCopy, nodes, alpha);

                boolean independent = test.isIndependent(nodes.get(index1), nodes.get(index2),
                        new LinkedList<Node>());

                if (!independent) {
                    System.out.println("Combination");
                    printParents(nodes, index1, choice1);
                    printParents(nodes, index2, choice2);
                    printCoefs(result1, result2);

                    System.out.println("R square 1 = " + result1.getRSquare());
                    System.out.println("R square 2 = " + result2.getRSquare());
                }
            }
        }
    }

    private double calcAverage1Up(double[] p1, double[] p2) {
        double avg = 0.0;
        for (int i = 1; i < p1.length; i++) avg += p1[i];
        for (int i = 1; i < p2.length; i++) avg += p2[i];
        avg /= p1.length + p2.length - 2;
        return avg;
    }

    private void printCoefs(RegressionResult result1, RegressionResult result2) {
        double[] c1 = result1.getCoef();
        double[] c2 = result2.getCoef();

        for (int i = 1; i < c1.length; i++) {
            System.out.print(c1[i] + "\t");
        }

        System.out.println();

        for (int i = 1; i < c2.length; i++) {
            System.out.print(c2[i] + "\t");
        }

        System.out.println();
    }

    public void search3(Dag dag, RectangularDataSet dataSet) {
        System.out.println("DataSet vars = " + dataSet.getVariables());
        System.out.println("DAG = " + dag);

        List<Node> nodes = dag.getNodes();
        DoubleMatrix2D data = dataSet.getDoubleData();
        DoubleMatrix2D residuals = getResiduals(dag, data, dataSet.getVariables());

        int m = nodes.size();
        int[][] dependencies = getDependencies(nodes, residuals);

        System.out.println("Initial dependencies:");
        printDependencies(dependencies);

        printRegressions(dag, dataSet.getVariables(), data);
    }

    public void search4() {
        Dag dag = new Dag();
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        dag.addNode(x);
        dag.addNode(y);

        dag.addDirectedEdge(x, y);

        System.out.println("DAG = " + dag);

        SemPm pm = new SemPm(dag);
        SemIm im = new SemIm(pm);

        RectangularDataSet dataSet = im.simulateData(500, false);

        dag.removeEdge(x, y);

        System.out.println("DAG = " + dag);

        List<Node> nodes = dag.getNodes();
        DoubleMatrix2D data = dataSet.getDoubleData();
        DoubleMatrix2D residuals = getResiduals(dag, data, dataSet.getVariables());

        int m = nodes.size();
        int[][] dependencies = getDependencies(nodes, residuals);

        System.out.println("Initial dependencies:");
        printDependencies(dependencies);


    }


    private int[][] getDependencies(List<Node> nodes, DoubleMatrix2D residuals) {
        System.out.println(nodes);

        IndependenceTest test = new IndTestFisherZ(residuals, nodes, alpha);
        int[][] dependencies = new int[nodes.size()][nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                dependencies[i][j] = test.isIndependent(nodes.get(i), nodes.get(j),
                        new LinkedList<Node>()) ? 0 : 1;
            }
        }

        return dependencies;
    }


    private void printRegressions(Dag dag, List<Node> dataNodes, DoubleMatrix2D data) {
        for (Node target : dag.getNodes()) {
            List<Node> parents = dag.getParents(target);

            System.out.println("\n\nRegression for " + target + " given " + parents + "...");

            int targetIndex = indexOf(dataNodes, target);
            int[] parentIndices = new int[parents.size()];

            for (int i = 0; i < parents.size(); i++) {
                parentIndices[i] = indexOf(dataNodes, parents.get(i));
            }

            RegressionResult result1 = getRegressionResult(targetIndex, parentIndices, data, dataNodes);
            System.out.println(result1);
        }
    }

    private int indexOf(List<Node> dataNodes, Node target) {
        String lookup = target.getName();

//        System.out.println(lookup);
        Node variable = getVariable(dataNodes, lookup);
//        System.out.println(variable);
        int index = dataNodes.indexOf(variable);

        System.out.println(index);
        return index;
    }

    private int[][] calcDependencies(List<Node> nodes, DoubleMatrix2D residuals) {
        int m = nodes.size();
        int[][] dependencies = new int[m][m];
        IndependenceTest test = new IndTestFisherZ(residuals, nodes, alpha);

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;

                dependencies[i][j] = test.isIndependent(nodes.get(i), nodes.get(j),
                        new LinkedList<Node>()) ? 0 : 1;
            }
        }
        return dependencies;
    }

    private void printParents(List<Node> nodes, int maxRow, int[] choice) {
        System.out.print("<");

        for (int r = 0; r < choice.length; r++) {
            System.out.print(nodes.get(choice[r]));

            if (r < choice.length - 1) {
                System.out.print(", ");
            }
        }

        System.out.print("> --> " + nodes.get(maxRow));
        System.out.println();
    }

    private void printParents(List<Node> nodes, int maxRow, List<Node> parents) {
        System.out.print("<");

        for (int r = 0; r < parents.size(); r++) {
            System.out.print(parents.get(r));

            if (r < parents.size() - 1) {
                System.out.print(", ");
            }
        }

        System.out.print("> --> " + nodes.get(maxRow));
        System.out.println();
    }

    private int sumRow(int i, int[][] dependencies) {
        int sum = 0;

        for (int j = 0; j < dependencies[0].length; j++) {
            sum += dependencies[i][j];
        }

        return sum;
    }

    private int sumRow(int i, int[] dependencies) {
        int sum = 0;

        for (int j = 0; j < dependencies.length; j++) {
            sum += dependencies[j];
        }

        return sum;
    }

    private void printDependencies(int[][] dependencies) {
        System.out.println("Dependencies");

        for (int i = 0; i < dependencies.length; i++) {
            for (int j = 0; j < dependencies[0].length; j++) {
                System.out.print(dependencies[i][j] + "\t");
            }

            System.out.println();
        }
    }

    private void printDependencies(int[] independencies) {
        System.out.println("Dependencies");

        for (int i = 0; i < independencies.length; i++) {
            System.out.print(independencies[i] + "\t");
        }

        System.out.println();
    }

    private DoubleMatrix2D getResiduals(Graph dag, DoubleMatrix2D data, List<Node> variables) {
        System.out.println("Scoring DAG: " + dag);

        Regression regression = new RegressionDataset(data, variables);

        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(data.rows(), data.columns());

        for (int i = 0; i < variables.size(); i++) {
            Node target = variables.get(i);
            Node dagTarget = dag.getNode(target.toString());
            List<Node> dagRegressors = dag.getParents(dagTarget);
            List<Node> regressors = new ArrayList<Node>();

            for (Node _regressor : dagRegressors) {
                Node variable = getVariable(variables, _regressor.getName());
                regressors.add(variable);
            }

            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        IndependenceTest test = new IndTestFisherZ(residuals, variables, alpha);

        for (int i = 0; i < variables.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (!test.isIndependent(variables.get(i), variables.get(j), new LinkedList<Node>())) {
                    System.out.println(variables.get(i) + " " + variables.get(j) + " residuals correlated.");
                }
            }
        }

        System.out.println();

        return residuals;
    }

    private RegressionResult getRegressionResult(int i, int[] choice, DoubleMatrix2D data, List<Node> nodes) {
        Regression regression = new RegressionDataset(data, nodes);

        Node target = nodes.get(i);
        List<Node> regressors = new LinkedList<Node>();

        for (int k = 0; k < choice.length; k++) {
            regressors.add(nodes.get(choice[k]));
        }

        return regression.regress(target, regressors);
    }

    private DoubleMatrix1D getResiduals(int i, int[] choice, DoubleMatrix2D data, List<Node> nodes) {
        Regression regression = new RegressionDataset(data, nodes);

        Node target = nodes.get(i);
        List<Node> regressors = new LinkedList<Node>();
        for (int k = 0; k < choice.length; k++) {
            regressors.add(nodes.get(k));
        }

        RegressionResult result = regression.regress(target, regressors);
        return result.getResiduals();
    }

    private Node getVariable(List<Node> variables, String name) {
        for (Node node : variables) {
            if (name.equals(node.getName())) {
                return node;
            }
        }

        return null;
    }

    private DoubleMatrix2D getBootstrapSample(DoubleMatrix2D dataSet, int sampleSize) {
        int actualSampleSize = dataSet.rows();

        int[] rows = new int[sampleSize];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = PersistentRandomUtil.getInstance().nextInt(actualSampleSize);
        }

        int[] cols = new int[dataSet.columns()];
        for (int i = 0; i < cols.length; i++) cols[i] = i;

        return dataSet.viewSelection(rows, cols).copy();
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