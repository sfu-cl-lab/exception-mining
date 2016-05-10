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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ProbUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates some scores for Bayes nets as a whole.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class BayesProperties {
    private RectangularDataSet dataSet;
    private BayesPm bayesPm;
    private Graph graph;
    private MlBayesIm blankBayesIm;
    private int pValueDf;
    private double chisq;

    //April 16th @zqian
    private double loglikelihood; 
    private double parameterPenalty_BIC;
    private int parameterPenalty_AIC;
    
    public BayesProperties(RectangularDataSet dataSet, Graph graph) {
        setDataSet(dataSet);
        setGraph(graph);
    }

    public final void setGraph(Graph graph) {
        if (graph == null) {
            throw new NullPointerException();
        }

        List<Node> vars = dataSet.getVariables();
        Map<String, DiscreteVariable> nodesToVars =
                new HashMap<String, DiscreteVariable>();
        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            DiscreteVariable var = (DiscreteVariable) vars.get(i);
            String name = var.getName();
            Node node = new GraphNode(name);
            nodesToVars.put(node.getName(), var);
        }

        Dag dag = new Dag(graph);
        BayesPm bayesPm = new BayesPm(dag);

        List<Node> nodes = bayesPm.getDag().getNodes();

        for (Node node1 : nodes) {
            Node var = nodesToVars.get(node1.getName());

            if (var instanceof DiscreteVariable) {
                DiscreteVariable var2 = (DiscreteVariable) var;
                List<String> categories = var2.getCategories();
                bayesPm.setCategories(node1, categories);
            }
        }

        this.graph = graph;
        this.bayesPm = bayesPm;
        this.blankBayesIm = new MlBayesIm(bayesPm);
    }

    /**
     * Calculates the BIC (Bayes Information Criterion) score for a BayesPM with
     * respect to a given discrete data set. Following formulas of Andrew Moore,
     * www.cs.cmu.edu/~awm.
     */
    public final double getBic() {//April 16th @zqian
    	loglikelihood= logProbDataGivenStructure2();
    	parameterPenalty_BIC = parameterPenalty();    	
        return logProbDataGivenStructure2() - parameterPenalty();
    }
    /**
     * Calculates the AIC score for a BayesPM with
     * respect to a given discrete data set. 
     * the parameter penalty should be the number of parameter.// April 16th @zqian
     */
    public final double getAic() { //April 16th @zqian
    	parameterPenalty_AIC= getnumParams();
    	return loglikelihood - parameterPenalty_AIC;
    }
    
    public final double getloglikelihood() {//April 16th @zqian
        return loglikelihood ;
    }
    
    public final double getparameterPenalty_BIC() {//April 16th @zqian
        return parameterPenalty_BIC ;
    }
    
    public final int getparameterPenalty_AIC() {//April 16th @zqian
        return parameterPenalty_AIC ;
    }
    /**
     * Calculates the p-value of the graph with respect to the given data.
     */
    public final double getPValue() {
        Graph graph1 = getGraph();
        List<Node> nodes = getGraph().getNodes();

        // Null hypothesis = no edges.
        Graph graph0 = new Dag();

        for (Node node : nodes) {
            graph0.addNode(node);
        }

        BayesProperties scorer1 = new BayesProperties(getDataSet(), graph1); // the learned model  @zqian
        BayesProperties scorer0 = new BayesProperties(getDataSet(), graph0); // model with no edge 

        double l1 = scorer1.logProbDataGivenStructure2();
        double l0 = scorer0.logProbDataGivenStructure2();

        double chisq = -2.0 * (l0 - l1);
        
        int n1 = scorer1.numNonredundantParams();
        int n0 = scorer0.numNonredundantParams();

        int df = n1 - n0;
        
        double pValue = (1.0 - ProbUtils.chisqCdf(chisq, df));

        //        System.out.println("\n*** P Value Calculation ***");
        //        System.out.println("l1 = " + l1 + " l0 = " + l0 + " l0 - l1 = " + (l0 - l1));
        //        System.out.println("n1 = " + n1 + " n0 = " + n0 + " n1 - n0 = " + (n1 - n0));
        //        System.out.println("chisq = " + chisq + " pvalue = " + pValue);

        this.pValueDf = df;
        this.chisq = chisq;
        return pValue;
    }

    private double logProbDataGivenStructure() {
        MlBayesEstimator estimator = new MlBayesEstimator();
        BayesIm bayesIm = estimator.estimate(bayesPm, dataSet);
        BayesImProbs probs = new BayesImProbs(bayesIm);
        RectangularDataSet reorderedDataSetDiscrete =
                estimator.getReorderedDataSet();

        int m = reorderedDataSetDiscrete.getNumRows();
        int n = reorderedDataSetDiscrete.getNumColumns();
        double score = 0.0;
        int[] _case = new int[n];

        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                _case[i] = reorderedDataSetDiscrete.getInt(j, i);
            }

            score += Math.log(probs.getCellProb(_case));
        }

        return score;
    }

    private int numNonredundantParams() {
        setGraph(getGraph());
        int numParams = 0;

        for (int j = 0; j < blankBayesIm.getNumNodes(); j++) {
            int numColumns = blankBayesIm.getNumColumns(j);
            int numRows = blankBayesIm.getNumRows(j);

            if (numColumns > 1) {
                numParams += (numColumns - 1) * numRows;
            }
        }

        return numParams;
    }

    /**
     * Calculates  log(P(Data | structure)) using Andrew Moore's formula.
     */
    public final double logProbDataGivenStructure2() {
        DataSetProbs probs = new DataSetProbs(getDataSet());

        double r = dataSet.getNumRows();
            //System.out.print("NUMBER ROWS of DATA "+r+"\n");//April 16th @zqian
        double score = 0.0;

        List<String> dataVarNames = dataSet.getVariableNames();

        for (int j = 0; j < blankBayesIm.getNumNodes(); j++) {

            rows:
            for (int k = 0; k < blankBayesIm.getNumRows(j); k++) {

                // Calculate probability of this combination of parent values.
                Proposition condition = Proposition.tautology(blankBayesIm);

                int[] parents = blankBayesIm.getParents(j);
                int[] parentValues = blankBayesIm.getParentValues(j, k);

                for (int v = 0; v < blankBayesIm.getNumParents(j); v++) {
                    int parent = parents[v];
                    int dataVar = translate(parent, dataVarNames);
                    condition.setCategory(dataVar, parentValues[v]);
                }

                double p1 = probs.getProb(condition);

                for (int v = 0; v < blankBayesIm.getNumColumns(j); v++) {
                    Proposition assertion = Proposition.tautology(blankBayesIm);

                    int _j = translate(j, dataVarNames);
                    assertion.setCategory(_j, v);
                    double p2 = probs.getConditionalProb(assertion, condition);

                    if (Double.isNaN(p2) || p2 == 0.) {
                        continue rows;
                    }

                    double numCases = r * p1 * p2;
                    score += numCases * Math.log(p2);
                }
            }
        }

        return score;
    }

    private int translate(int parent, List<String> dataVarNames) {
        String imName = blankBayesIm.getNode(parent).getName();
        return dataVarNames.indexOf(imName);
    }

    public final BayesPm getBayesPm() {
        return bayesPm;
    }

    private RectangularDataSet getDataSet() {
        return dataSet;
    }

    private void setDataSet(RectangularDataSet dataSet) {
        if (dataSet == null) {
            throw new NullPointerException();
        }

        this.bayesPm = null;
        this.blankBayesIm = null;
        this.graph = null;
        this.pValueDf = -1;
        this.chisq = Double.NaN;

        this.dataSet = dataSet;
    }
    

    private int numParams=0;//April 16th @zqian
    private double parameterPenalty() {
        numParams = numNonredundantParams();
        double r = dataSet.getNumRows();
        return numParams * Math.log(r) / 2.; //BIC April 16th
        //return numParams ; //April 16th, AIC

    }

    private int getnumParams() {
        return numParams ; //April 16th, AIC
    }


    public final int getPValueDf() {
        return pValueDf;
    }

    public final double getPValueChisq() {
        return chisq;
    }

    private Graph getGraph() {
        return graph;
    }
}


