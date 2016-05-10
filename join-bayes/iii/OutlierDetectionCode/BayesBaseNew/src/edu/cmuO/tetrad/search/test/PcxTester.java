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

package edu.cmu.tetrad.search.test;

import edu.cmu.tetrad.bayes.*;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.IndTestChiSquare;
import edu.cmu.tetrad.search.IndTestCramerT;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.PcxSearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated
 */
@Deprecated
public class PcxTester {
    public static void main(String[] args) {

        try {
            //Test new methods in SearchGraphMatrix:
            List V = new ArrayList();
            V.add(new ContinuousVariable("X1"));
            V.add(new ContinuousVariable("X2"));
            V.add(new ContinuousVariable("X3"));
            V.add(new ContinuousVariable("X4"));
            V.add(new ContinuousVariable("X5"));
            V.add(new ContinuousVariable("X6"));
            V.add(new ContinuousVariable("X7"));
            V.add(new ContinuousVariable("X8"));
            V.add(new ContinuousVariable("X9"));
            V.add(new ContinuousVariable("X10"));
            V.add(new ContinuousVariable("X11"));
            V.add(new ContinuousVariable("X12"));
            V.add(new ContinuousVariable("X13"));
            V.add(new ContinuousVariable("X14"));
            V.add(new ContinuousVariable("X15"));
            V.add(new ContinuousVariable("X16"));

            Graph testGraph = new EndpointMatrixGraph(V);

            testGraph.setEndpoint((Node) V.get(0), (Node) V.get(4), Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(4), (Node) V.get(0), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(1), (Node) V.get(4), Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(4), (Node) V.get(1), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(1), (Node) V.get(5), Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(5), (Node) V.get(1), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(2), (Node) V.get(5), Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(5), (Node) V.get(2), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(2), (Node) V.get(6), Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(6), (Node) V.get(2), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(3), (Node) V.get(6), Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(6), (Node) V.get(3), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(7), (Node) V.get(11),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(11), (Node) V.get(7), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(8), (Node) V.get(11),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(11), (Node) V.get(8), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(8), (Node) V.get(12),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(12), (Node) V.get(8), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(5), (Node) V.get(12),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(12), (Node) V.get(5), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(5), (Node) V.get(13),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(13), (Node) V.get(5), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(5), (Node) V.get(14),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(14), (Node) V.get(5), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(9), (Node) V.get(14),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(14), (Node) V.get(9), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(9), (Node) V.get(15),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(15), (Node) V.get(9), Endpoint.TAIL);

            testGraph.setEndpoint((Node) V.get(10), (Node) V.get(15),
                    Endpoint.ARROW);
            testGraph.setEndpoint((Node) V.get(15), (Node) V.get(10),
                    Endpoint.TAIL);

            System.out.println("Original Graph");
            System.out.println(testGraph);

            List subSet = new ArrayList();
            subSet.add(V.get(1));
            subSet.add(V.get(2));
            subSet.add(V.get(5));
            subSet.add(V.get(12));
            subSet.add(V.get(13));
            subSet.add(V.get(14));
            Graph testGraphSub;
            testGraphSub = testGraph.subgraph(subSet);

            System.out.println("Subgraph");
            System.out.println(testGraphSub);
            System.out.println("\n\n");

            //Test with continuous dataset
            RectangularDataSet dataSet = null;

            double alpha = 0.05;
            String file = "test_data/markovBlanketTest.dat";

            try {
                DataParser parser = new DataParser();
                dataSet = parser.parseTabular(new File(file));
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            IndependenceTest test;
            List listOfNodes;
            int numNodes;
            //Knowledge bk;
            //PcxSearch pcx;
            PcxSearch pcx;

            alpha = 0.05;
            System.out.println("For alpha = " + alpha);
            test = new IndTestCramerT(dataSet, alpha);

            listOfNodes = test.getVariables();
            numNodes = listOfNodes.size();

            System.out.println("List of nodes " + numNodes);
            for (Iterator it = listOfNodes.iterator(); it.hasNext();) {
                Node var = (Node) it.next();
                //String name = var.getName();
                System.out.println(var);
            }

            //bk = new Knowledge();

            int[][] ind = new int[numNodes][numNodes];
            //  Test indepencence facts statements:
            List em = new ArrayList();
            for (int i = 0; i < numNodes; i++) {
                for (int j = i + 1; j < numNodes; j++) {
                    boolean ixy = test.isIndependent((Node) listOfNodes.get(i),
                            (Node) listOfNodes.get(j), em);

                    if (ixy) {
                        ind[i][j] = 0;
                        ind[j][i] = 0;
                    }
                    else {
                        ind[i][j] = 1;
                        ind[j][i] = 1;
                    }
                }
                System.out.print(listOfNodes.get(i) + " ");
            }
            System.out.println();

            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    System.out.print(" " + ind[i][j] + " ");
                }
                System.out.println();
            }

            pcx = new PcxSearch(test, -1);
            //pcx = new PcxSearch(test);
            //pcx = new PcxSearch(cds, alpha);
            Graph mb = pcx.search("X6");  //TODO

            //mb.print();
            System.out.println(mb);
            System.out.println("Test search completed for continuous data.");

            //Test with discrete data.
            String filenameD = "test_data/markovBlanketTestDisc.dat";

            DataParser parser = new DataParser();
            RectangularDataSet dds = parser.parseTabular(new File(filenameD));

            IndependenceTest indTestD;
            PcxSearch pcxD;
            indTestD = new IndTestChiSquare(dds, alpha);

            pcxD = new PcxSearch(indTestD, -1);
            Graph mbD = pcxD.search("A6");

            System.out.println("Markov Blanket for A6 in discrete dataset");
            System.out.println(mbD);
            System.out.println("Test search completed for discrete data.");

            System.out.println("\n\nTesting of classification");
            Dag mbDAG = new Dag(mbD);
            BayesPm mbBayesPM = new BayesPm(mbDAG);

            List<Node> variables = new LinkedList<Node>();

            for (Node n : mbD.getNodes()) {
                variables.add(n);
            }

            RectangularDataSet ddsMB = dds.subsetColumns(variables);
            MlBayesEstimator estimator = new MlBayesEstimator();
            MlBayesIm mbBayesIM = (MlBayesIm) estimator.estimate(mbBayesPM, ddsMB);

            BayesUpdater bayesUpdaterMB = new RowSummingExactUpdater(mbBayesIM);

            int nVars = ddsMB.getNumColumns();
            int nCases = ddsMB.getNumRows();

            System.out.println("No cases = " + nCases + " No variables = " + nVars);

            List varsMB = ddsMB.getVariables();

            String target = "A6";
            DiscreteVariable targetVariable = null;
            for (int j = 0; j < varsMB.size(); j++) {
                DiscreteVariable dv = (DiscreteVariable) varsMB.get(j);
                if (dv.getName().equals(target)) {
                    targetVariable = dv;
                    break;
                }
            }

            //int numCorrect= 0;
            //int numIncorrect = 0;

            //Initialize crosstabs of counts of estimated versus observed values
            //of target variable.
            int nvalues = targetVariable.getNumCategories();
            int[][] crossTabs = new int[nvalues][nvalues];
            for (int i = 0; i < nvalues; i++) {
                for (int j = 0; j < nvalues; j++) {
                    crossTabs[i][j] = 0;
                }
            }

            for (int i = 0; i < nCases; i++) {
                /* DEBUG print
                for(int j = 0; j < varsMB.size(); j++) {
                  DiscreteVariable dv = (DiscreteVariable) varsMB.get(j);
                  System.out.print(dv.getCategory(rawData[j][i]) + " ");
                }
                System.out.println();
                */

                Evidence evidence = Evidence.tautology(mbBayesIM);

                //Let the target variable range over all its values.
                int itarget = evidence.getNodeIndex(target);
                evidence.getProposition().setVariable(itarget, true);

                //Restrict all other variables to their actual value in this case.
                for (int j = 0; j < varsMB.size(); j++) {
                    if (j == varsMB.indexOf(targetVariable)) {
                        continue;  //Skip target
                    }
                    String other = ((DiscreteVariable) varsMB.get(j)).getName();
                    int iother = evidence.getNodeIndex(other);
                    evidence.getProposition().setCategory(iother,
                            ddsMB.getInt(i, j));
                    //            evidence.getProposition().setComplementValues
                    //                                         (iother, rawData[j][i], false);
                }

                bayesUpdaterMB.setEvidence(evidence);
                BayesIm bayesIm = bayesUpdaterMB.getBayesIm();

                //for each possible value of target
                int indexTargetDDS = varsMB.indexOf(targetVariable);
                int indexTargetBN = bayesIm.getNodeIndex(targetVariable);

                double highestProb = 0.0;
                int probableValue = -1;
                for (int k = 0; k < targetVariable.getNumCategories(); k++) {
                    if (bayesUpdaterMB.getMarginal(indexTargetBN, k) > highestProb)
                    {
                        highestProb = bayesUpdaterMB.getMarginal(indexTargetBN, k);
                        probableValue = k;
                    }
                }

                //if(probableValue == rawData[indexTargetDDS][i]) numCorrect++;
                int observedValue = ddsMB.getInt(i, indexTargetDDS);
                crossTabs[observedValue][probableValue]++;
            }

            //System.out.println("Number correct = " + numCorrect);
            System.out.println("Target Variable " + target);
            System.out.println("\t\t\tEstimated\t");
            System.out.print("Observed\t");
            for (int i = 0; i < nvalues; i++) {
                System.out.print(targetVariable.getCategory(i) + "\t");
            }
            System.out.println();
            for (int i = 0; i < nvalues; i++) {
                System.out.print(targetVariable.getCategory(i) + "\t");
                for (int j = 0; j < nvalues; j++) {
                    System.out.print(crossTabs[i][j] + "\t\t");
                }
                System.out.println();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}


