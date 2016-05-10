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

import cern.colt.matrix.DoubleMatrix2D;
import cern.jet.math.PlusMult;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemOptimizerPalCds;
import edu.cmu.tetrad.sem.SemPm;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestPValueImprover extends TestCase {
    private int randomCount = 100;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestPValueImprover(String name) {
        super(name);
    }

    public void test() {
    }

    public void rtest1() {
        Graph trueDag = GraphUtils.randomDag(15, 15, false);
        Graph truePattern = SearchGraphUtils.patternForDag(trueDag);

        SemPm pm = new SemPm(trueDag);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(1000, false);
        Jcpc search = new Jcpc(new IndTestFisherZ(data, .001));
//        Ges search = new Ges(data);
        Graph pattern = search.search();

//        pattern.removeEdges(pattern.getEdges());

        System.out.println("\nErrors for pattern:");
        printErrors(pattern, truePattern);

        truePattern = GraphUtils.replaceNodes(truePattern, pattern.getNodes());

        PValueImprover2 improver = new PValueImprover2(pattern, data);
//        PValueImproverGes improver = new PValueImproverGes(pattern, data);
        improver.setAlpha(0.05);
//        improver.setHighPValueAlpha(1e-18);
        improver.setTrueModel(truePattern);
        Graph pattern2 = improver.search();

        System.out.println("\nErrors for ''improved result'':");
        printErrors(pattern2, truePattern);

        System.out.println("pattern2 = " + pattern2);
        System.out.println("true pattern = " + truePattern);

        double originalScore = improver.scoreGraph(pattern).getPValue();
        double newScore = improver.scoreGraph(pattern2).getPValue();
        double trueScore = improver.scoreGraph(truePattern).getPValue();

        int originalDof = improver.getOriginalSemIm().getSemPm().getDof();
        int newDof = improver.getNewSemIm().getSemPm().getDof();
        int trueDof = im.getSemPm().getDof();

        System.out.println("\nOriginal p value = " + originalScore + " dof = " + originalDof);
        System.out.println("New p value = " + newScore + " dof = " + newDof);
        System.out.println("True p value = " + trueScore + " dof = " + trueDof);


        System.out.println(GraphUtils.graphComparisonString("pattern2", pattern2, "truePattern", truePattern, true));

    }

    public void test2() {
        NumberFormat nf = new DecimalFormat("0.00");

        int sumOrigAdjFp = 0;
        int sumOrigAdjFn = 0;
        int sumOrigDe = 0;
        int sumOrigBid = 0;
        int numOrigSignifacantP = 0;

        int sumNewAdjFp = 0;
        int sumNewAdjFn = 0;
        int sumNewDe = 0;
        int sumNewBid = 0;
        int numNewSignificantP = 0;

        double sumDistanceOrigFromTrueScore = 0.0;
        double sumDistanceNewFromTrueScore = 0.0;

        int numIterations = 30;
        int sampleSize = 1000;
        double alpha = .01;
        double highPValue = alpha;
        int numNodes = 25;

        for (int count = 0; count < numIterations; count++) {
            System.out.println("\n\n=======================COUNT = " + (count + 1));

            Graph trueDag = GraphUtils.randomDag(numNodes, numNodes, false);
            Graph truePattern = SearchGraphUtils.patternForDag(trueDag);

            System.out.println("True model: " + trueDag);

            SemPm pm = new SemPm(trueDag);
            SemIm im = new SemIm(pm);
            DataSet data = im.simulateData(sampleSize, false);

            trueDag = GraphUtils.replaceNodes(trueDag, data.getVariables());
            truePattern = GraphUtils.replaceNodes(truePattern, data.getVariables());
            Graph pattern;

//            Pc search = new Pc(new IndTestFisherZ(data, 5e-2));
            Cpc2 search = new Cpc2(new IndTestFisherZ(data, 5e-2));
//            Jcpc2 search = new Jcpc2(new IndTestFisherZ(data, 1e-3));
//            Jpc search = new Jpc(new IndTestFisherZ(data, 1e-3));
//            Ges search = new Ges(data);
//            Mmhc search = new Mmhc(new IndTestFisherZ(data, 1e-4));
//            search.setDiscreteScore(new BDeScore(data));
//            search.setAggressivelyPreventCycles(true);
            pattern = search.search();


//            Lingam search = new Lingam();
//            pattern = search.search(data);

            // Empty pattern.
//            pattern = new EdgeListGraph(data.getVariables());

            // Random pattern.
//            Graph graph = GraphUtils.randomDag(trueDag.getNodes(), numNodes, false);
//            pattern = SearchGraphUtils.patternForDag(graph);

//            pattern = new EdgeListGraph(truePattern);

            pattern = GraphUtils.removeBidirectedEdges(pattern);

            System.out.println("\nErrors for pattern:");
            printErrors(pattern, truePattern);

            PValueImprover4 improver = new PValueImprover4(pattern, data, new Knowledge());
//            PValueImproverGes improver = new PValueImproverGes(pattern, data);
            improver.setAlpha(alpha);
            improver.setHighPValueAlpha(highPValue);
            improver.setTrueModel(truePattern);
            improver.setBeamWidth(3);
//            improver.setCheckingCycles(false);
            Graph dag = improver.search();
            Graph newPattern = SearchGraphUtils.patternForDag(dag);

            System.out.println(newPattern);


            System.out.println("\nErrors for ''improved result'':");
            printErrors(newPattern, truePattern);

            Graph newDag = improver.getNewDag(); //dag(improver.getNewSemIm().getSemPm().getGraph());

            double originalPValue = improver.scoreGraph(improver.getOriginalSemIm().getSemPm().getGraph()).getPValue();
            double newPValue = improver.scoreGraph(newDag).getPValue();
            double truePValue = improver.scoreGraph(trueDag).getPValue();

            System.out.println("originalPValue = " + originalPValue);
            System.out.println("newPValue = " + newPValue);
            System.out.println("truePValue = " + truePValue);

            if (!newPattern.equals(truePattern)) {
                int adjFp = GraphUtils.adjacenciesComplement(newPattern, truePattern).size();
                int adjFn = GraphUtils.adjacenciesComplement(truePattern, newPattern).size();
                int adjError = adjFp + adjFn;
                System.out.println("False pattern error = " + adjError + " p = " + newPValue);
            }

            if (newPValue < alpha) {
                count--;
                continue;
            }

            sumOrigAdjFp += GraphUtils.adjacenciesComplement(pattern, truePattern).size();
            sumOrigAdjFn += GraphUtils.adjacenciesComplement(truePattern, pattern).size();
            sumOrigDe += GraphUtils.numDirectionalErrors(pattern, truePattern);
            sumOrigBid += GraphUtils.numBidirected(pattern);

            sumNewAdjFp += GraphUtils.adjacenciesComplement(newPattern, truePattern).size();
            sumNewAdjFn += GraphUtils.adjacenciesComplement(truePattern, newPattern).size();
            sumNewDe += GraphUtils.numDirectionalErrors(newPattern, truePattern);
            sumNewBid += GraphUtils.numBidirected(newPattern);

            DoubleMatrix2D dataCorr = new CovarianceMatrix(data).getMatrix();

            SemEstimator estimator = new SemEstimator(data, pm);
            SemIm im2 = estimator.estimate();

            DoubleMatrix2D trueCorr = im2.getImplCovar();

            DoubleMatrix2D diff = trueCorr.copy();
            diff.assign(dataCorr, PlusMult.plusMult(-1));
            System.out.println(diff);

            double originalScore = improver.scoreGraph(improver.getOriginalSemIm().getSemPm().getGraph()).getScore();
            double newScore = improver.scoreGraph(newDag).getScore();
            double trueScore = improver.scoreGraph(trueDag).getScore();

            double originalBic = improver.scoreGraph(improver.getOriginalSemIm().getSemPm().getGraph()).getBic();
            double newBic = improver.scoreGraph(newDag).getBic();
            double trueBic = improver.scoreGraph(trueDag).getBic();

            System.out.println("New model cyclic? " + improver.getNewSemIm().getSemPm().getGraph().existsDirectedCycle());

            sumDistanceOrigFromTrueScore += Math.abs(originalPValue - truePValue);

            System.out.println();
            System.out.println("originalScore = " + originalScore);
            System.out.println("newScore = " + newScore);
            System.out.println("trueScore = " + trueScore);

            System.out.println("originalBic = " + originalBic);
            System.out.println("newBic = " + newBic);
            System.out.println("trueBic = " + trueBic);

            System.out.println("(N - 1) * originalScore = " + (sampleSize - 1) * originalScore);
            System.out.println("(N - 1) * newScore = " + (sampleSize - 1) * newScore);
            System.out.println("(N - 1) * trueScore = " + (sampleSize - 1) * trueScore);


            System.out.println();
            System.out.println("originalDf = " + improver.getOriginalSemIm().getSemPm().getDof());
            System.out.println("newDf = " + improver.getNewSemIm().getSemPm().getDof());
            System.out.println("trueDf = " + new SemPm(trueDag).getDof());

            if (originalPValue >= alpha) numOrigSignifacantP++;
            sumDistanceNewFromTrueScore += Math.abs(newPValue - truePValue);
            if (newPValue >= alpha) numNewSignificantP++;

//            int originalDof = improver.getOriginalSemIm().getSemPm().getDof();
//            int newDof = improver.getNewSemIm().getSemPm().getDof();
//            int trueDof = im.getSemPm().getDof();
//
//            System.out.println("\nOriginal p value = " + originalPValue + " dof = " + originalDof);
//            System.out.println("New p value = " + newPValue + " dof = " + newDof);
//            System.out.println("True p value = " + truePValue + " dof = " + trueDof);
//

            System.out.println(GraphUtils.graphComparisonString("pattern", pattern, "truePattern", truePattern, true));
            System.out.println(GraphUtils.graphComparisonString("newPattern", newPattern, "truePattern", truePattern, true));

            double avgOrigAdjFp = sumOrigAdjFp / (double) (count + 1);
            double avgOrigAdjFn = sumOrigAdjFn / (double) (count + 1);
            double avgOrigDe = sumOrigDe / (double) (count + 1);
            double avgOrigBid = sumOrigBid / (double) (count + 1);
            double avgNumOrigZeroP = numOrigSignifacantP / (double) (count + 1);

            double avgNewAdjFp = sumNewAdjFp / (double) (count + 1);
            double avgNewAdjFn = sumNewAdjFn / (double) (count + 1);
            double avgNewDe = sumNewDe / (double) (count + 1);
            double avgNewBid = sumNewBid / (double) (count + 1);
            double avgNumNewZeroP = numNewSignificantP / (double) (count + 1);

            double avgDistanceOrigFromTrueScore = sumDistanceOrigFromTrueScore / (count + 1);
            double avgDistanceNewFromTrueScore = sumDistanceNewFromTrueScore / (count + 1);

            System.out.println("REPORT");
            System.out.println("Count = " + (count + 1));
            System.out.println("Sample size = " + sampleSize);

            System.out.println("\nORIG:\n");

            System.out.println("ADJ FP = " + nf.format(avgOrigAdjFp));
            System.out.println("ADJ FN = " + nf.format(avgOrigAdjFn));
            System.out.println("DE = " + nf.format(avgOrigDe));
            System.out.println("BID = " + nf.format(avgOrigBid));
            System.out.println("# Insignificant P Values = " + nf.format(avgNumOrigZeroP));
            System.out.println("Distance of p value from true p = " + nf.format(avgDistanceOrigFromTrueScore));

            System.out.println("\nNEW:\n");

            System.out.println("ADJ FP = " + nf.format(avgNewAdjFp));
            System.out.println("ADJ FN = " + nf.format(avgNewAdjFn));
            System.out.println("DE = " + nf.format(avgNewDe));
            System.out.println("BID = " + nf.format(avgNewBid));
            System.out.println("# Insignificant P Values = " + nf.format(avgNumNewZeroP));
            System.out.println("Distance of p value from true p = " + nf.format(avgDistanceNewFromTrueScore));


        }


    }

    public void rtest3() {
        NumberFormat nf = new DecimalFormat("0.00");

        int sumOrigAdjFp = 0;
        int sumOrigAdjFn = 0;
        int sumOrigDe = 0;
        int sumOrigBid = 0;
        int numOrigSignifacantP = 0;

        int sumNewAdjFp = 0;
        int sumNewAdjFn = 0;
        int sumNewDe = 0;
        int sumNewBid = 0;
        int numNewSignificantP = 0;

        double sumDistanceOrigFromTrueScore = 0.0;
        double sumDistanceNewFromTrueScore = 0.0;

        int numIterations = 30;
        int sampleSize = 100000;
        double alpha = 0.05;
        double highPValue = 0.001;


        int count = 1;
        System.out.println("\n\n=======================COUNT = " + (count + 1));

        Graph trueDag = GraphUtils.randomDag(15, 15, false);
        Graph truePattern = SearchGraphUtils.patternForDag(trueDag);

        SemPm pm = new SemPm(trueDag);
        SemIm im = new SemIm(pm);

        Graph pattern;

        for (int y = 0; y < 100; y++) {
//        for (sampleSize = 100; sampleSize < 100000; sampleSize *= 2) {
            DataSet data = im.simulateData(sampleSize, false);
            trueDag = GraphUtils.replaceNodes(trueDag, data.getVariables());
            truePattern = GraphUtils.replaceNodes(truePattern, data.getVariables());

            System.out.println("================ SAMPLE SIZE = " + sampleSize);

            Pc search = new Pc(new IndTestFisherZ(data, 5e-2));
//            Cpc2 search = new Cpc2(new IndTestFisherZ(data, 5e-2));
//            Jcpc2 search = new Jcpc2(new IndTestFisherZ(data, .01));
//            Jpc search = new Jpc(new IndTestFisherZ(data, 1e-3));
//            Ges search = new Ges(data);
//            search.setAggressivelyPredventCycles(true);
            pattern = search.search();


//            Lingam search = new Lingam();
//            pattern = search.search(data);

            // Empty pattern.
//            pattern = new EdgeListGraph(data.getVariables());

            // Random pattern.
//            Graph graph = GraphUtils.randomDag(trueDag.getNodes(), 15, false);
//            pattern = SearchGraphUtils.patternForDag(graph);

//            pattern = new EdgeListGraph(truePattern);

            pattern = GraphUtils.removeBidirectedEdges(pattern);

            System.out.println("\nErrors for pattern:");
            printErrors(pattern, truePattern);

            sumOrigAdjFp += GraphUtils.adjacenciesComplement(pattern, truePattern).size();
            sumOrigAdjFn += GraphUtils.adjacenciesComplement(truePattern, pattern).size();
            sumOrigDe += GraphUtils.numDirectionalErrors(pattern, truePattern);
            sumOrigBid += GraphUtils.numBidirected(pattern);

            PValueImprover4 improver = new PValueImprover4(pattern, data, new Knowledge());
//            PValueImproverGes improver = new PValueImproverGes(pattern, data);
            improver.setAlpha(alpha);
            improver.setHighPValueAlpha(highPValue);
            improver.setTrueModel(truePattern);
            improver.setCheckingCycles(false);
            Graph pattern2 = SearchGraphUtils.patternForDag(improver.search());

            System.out.println(pattern2);


            System.out.println("\nErrors for ''improved result'':");
            printErrors(pattern2, truePattern);

            sumNewAdjFp += GraphUtils.adjacenciesComplement(pattern2, truePattern).size();
            sumNewAdjFn += GraphUtils.adjacenciesComplement(truePattern, pattern2).size();
            sumNewDe += GraphUtils.numDirectionalErrors(pattern2, truePattern);
            sumNewBid += GraphUtils.numBidirected(pattern2);

            double originalPValue = improver.scoreGraph(improver.getOriginalSemIm().getSemPm().getGraph()).getPValue();
            SemEstimator estimator = new SemEstimator(data, pm, new SemOptimizerPalCds());
            SemIm im2 = estimator.estimate();

            double newPValue = improver.scoreGraph(dag(improver.getNewSemIm().getSemPm().getGraph())).getPValue();
            double truePValue = improver.scoreGraph(trueDag).getPValue();

//            DoubleMatrix2D newCorr = MatrixUtils.convertCovToCorr(improver.getNewSemIm().getImplCovar());
//            DoubleMatrix2D dataCorr = MatrixUtils.convertCovToCorr(new CovarianceMatrix(data).getMatrix());
//            DoubleMatrix2D trueCorr = MatrixUtils.convertCovToCorr(im.getImplCovar());

            DoubleMatrix2D newCorr = improver.getNewSemIm().getImplCovar();
            DoubleMatrix2D dataCorr = new CovarianceMatrix(data).getMatrix();


            DoubleMatrix2D trueCorr = im2.getImplCovar();

            DoubleMatrix2D diff = trueCorr.copy();
            diff.assign(dataCorr, PlusMult.plusMult(-1));
            System.out.println(diff);

            double originalScore = improver.scoreGraph(improver.getOriginalSemIm().getSemPm().getGraph()).getScore();
            double newScore = improver.scoreGraph(dag(improver.getNewSemIm().getSemPm().getGraph())).getScore();
//            double trueScore = im2.getPValue();
            double trueScore = improver.scoreGraph(trueDag).getScore();

            double originalChiSquare = improver.scoreGraph(improver.getOriginalSemIm().getSemPm().getGraph()).getChiSquare();
            double newChiSquare = improver.scoreGraph(dag(improver.getNewSemIm().getSemPm().getGraph())).getChiSquare();
//            double trueScore = im2.getPValue();
            double trueChiSquare = improver.scoreGraph(trueDag).getChiSquare();

            System.out.println("New model cyclic? " + improver.getNewSemIm().getSemPm().getGraph().existsDirectedCycle());

//            double originalPValue = improver.scoreGraph(dag(pattern)).getPValue();
//            double newPValue = improver.scoreGraph(dag(pattern2)).getPValue();
//            double truePValue = improver.scoreGraph(dag(truePattern)).getPValue();

            sumDistanceOrigFromTrueScore += Math.abs(originalPValue - truePValue);
//            System.out.println("AAA" + sumDistanceNewFromTrueScore);
            System.out.println("originalPValue = " + originalPValue);
            System.out.println("newPValue = " + newPValue);
            System.out.println("truePValue = " + truePValue);

            System.out.println();

            System.out.println("originalChiSquare = " + originalChiSquare);
            System.out.println("newPChiSquare = " + newChiSquare);
            System.out.println("trueChiSquare = " + trueChiSquare);

            System.out.println();
            System.out.println("originalScore = " + originalScore);
            System.out.println("newScore = " + newScore);
            System.out.println("trueScore = " + trueScore);

            System.out.println("(N - 1) * originalScore = " + (sampleSize - 1) * originalScore);
            System.out.println("(N - 1) * newScore = " + (sampleSize - 1) * newScore);
            System.out.println("(N - 1) * trueScore = " + (sampleSize - 1) * trueScore);


            System.out.println();
            System.out.println("originalDf = " + improver.getOriginalSemIm().getSemPm().getDof());
            System.out.println("newDf = " + improver.getNewSemIm().getSemPm().getDof());
            System.out.println("trueDf = " + new SemPm(trueDag).getDof());

            if (originalPValue >= alpha) numOrigSignifacantP++;
            sumDistanceNewFromTrueScore += Math.abs(newPValue - truePValue);
            if (newPValue >= alpha) numNewSignificantP++;

//            int originalDof = improver.getOriginalSemIm().getSemPm().getDof();
//            int newDof = improver.getNewSemIm().getSemPm().getDof();
//            int trueDof = im.getSemPm().getDof();
//
//            System.out.println("\nOriginal p value = " + originalPValue + " dof = " + originalDof);
//            System.out.println("New p value = " + newPValue + " dof = " + newDof);
//            System.out.println("True p value = " + truePValue + " dof = " + trueDof);
//

            System.out.println(GraphUtils.graphComparisonString("pattern", pattern, "truePattern", truePattern, true));
            System.out.println(GraphUtils.graphComparisonString("pattern2", pattern2, "truePattern", truePattern, true));

            double avgOrigAdjFp = sumOrigAdjFp / (double) (count + 1);
            double avgOrigAdjFn = sumOrigAdjFn / (double) (count + 1);
            double avgOrigDe = sumOrigDe / (double) (count + 1);
            double avgOrigBid = sumOrigBid / (double) (count + 1);
            double avgNumOrigZeroP = numOrigSignifacantP / (double) (count + 1);

            double avgNewAdjFp = sumNewAdjFp / (double) (count + 1);
            double avgNewAdjFn = sumNewAdjFn / (double) (count + 1);
            double avgNewDe = sumNewDe / (double) (count + 1);
            double avgNewBid = sumNewBid / (double) (count + 1);
            double avgNumNewZeroP = numNewSignificantP / (double) (count + 1);

            double avgDistanceOrigFromTrueScore = sumDistanceOrigFromTrueScore / (count + 1);
            double avgDistanceNewFromTrueScore = sumDistanceNewFromTrueScore / (count + 1);

            System.out.println("REPORT");
            System.out.println("Count = " + (count + 1));
            System.out.println("Sample size = " + sampleSize);

            System.out.println("\nORIG:\n");

            System.out.println("ADJ FP = " + nf.format(avgOrigAdjFp));
            System.out.println("ADJ FN = " + nf.format(avgOrigAdjFn));
            System.out.println("DE = " + nf.format(avgOrigDe));
            System.out.println("BID = " + nf.format(avgOrigBid));
            System.out.println("# Significant P Values = " + nf.format(avgNumOrigZeroP));
            System.out.println("Distance of p value from true p = " + nf.format(avgDistanceOrigFromTrueScore));

            System.out.println("\nNEW:\n");

            System.out.println("ADJ FP = " + nf.format(avgNewAdjFp));
            System.out.println("ADJ FN = " + nf.format(avgNewAdjFn));
            System.out.println("DE = " + nf.format(avgNewDe));
            System.out.println("BID = " + nf.format(avgNewBid));
            System.out.println("# Significant P Values = " + nf.format(avgNumNewZeroP));
            System.out.println("Distance of p value from true p = " + nf.format(avgDistanceNewFromTrueScore));


        }

    }

    public void rtest4() {
        NumberFormat nf = new DecimalFormat("0.00");

        int sumOrigAdjFp = 0;
        int sumOrigAdjFn = 0;
        int sumOrigDe = 0;
        int sumOrigBid = 0;
        int numOrigSignifacantP = 0;

        int sumNewAdjFp = 0;
        int sumNewAdjFn = 0;
        int sumNewDe = 0;
        int sumNewBid = 0;
        int numNewSignificantP = 0;

        double sumDistanceOrigFromTrueScore = 0.0;
        double sumDistanceNewFromTrueScore = 0.0;

        int numIterations = 30;
        int sampleSize = 100000;
        double alpha = 0.05;
        double highPValue = 0.001;


        int count = 1;
        System.out.println("\n\n=======================COUNT = " + (count + 1));

        Graph trueDag = GraphUtils.randomDag(15, 15, false);
        Graph truePattern = SearchGraphUtils.patternForDag(trueDag);

        SemPm pm = new SemPm(trueDag);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(sampleSize, false);

        trueDag = GraphUtils.replaceNodes(trueDag, data.getVariables());
        truePattern = GraphUtils.replaceNodes(truePattern, data.getVariables());
        Graph pattern;

//            Lingam search = new Lingam();
//            pattern = search.search(data);

        // Empty pattern.
//            pattern = new EdgeListGraph(data.getVariables());

        // Random pattern.
//            Graph graph = GraphUtils.randomDag(trueDag.getNodes(), 15, false);
//            pattern = SearchGraphUtils.patternForDag(graph);

        pattern = new EdgeListGraph(truePattern);

        System.out.println("\nErrors for pattern:");
        printErrors(pattern, truePattern);

        sumOrigAdjFp += GraphUtils.adjacenciesComplement(pattern, truePattern).size();
        sumOrigAdjFn += GraphUtils.adjacenciesComplement(truePattern, pattern).size();
        sumOrigDe += GraphUtils.numDirectionalErrors(pattern, truePattern);
        sumOrigBid += GraphUtils.numBidirected(pattern);

        PValueImprover6 improver = new PValueImprover6(pattern, data);
//            PValueImprover5 improver = new PValueImprover5(pattern, data);
//            PValueImproverGes improver = new PValueImproverGes(pattern, data);
        improver.setAlpha(alpha);
        improver.setHighPValueAlpha(highPValue);
        improver.setTrueModel(truePattern);
//            improver.setCheckingCycles(false);
        Graph pattern2 = SearchGraphUtils.patternForDag(improver.search());
    }

    private Graph dag(Graph pattern) {
//        System.out.println("Pattern = " + pattern);

//        Graph copy = new EdgeListGraph(pattern);

//        SearchGraphUtils.basicPattern(copy);
//
//        System.out.println("Basic pattern = " + copy);

//        new MeekRules().orientImplied(copy);

//        copy = SearchGraphUtils.dagFromPattern(pattern, new Knowledge());
//        System.out.println("After Meek = " + copy);

//        return copy;

        return SearchGraphUtils.dagFromPattern(pattern, new Knowledge());
//        return SearchGraphUtils.listPatternDags(pattern, true).iterator().next();
    }

    private void printErrors(Graph graph1, Graph graph2) {
        int adjFp = GraphUtils.adjacenciesComplement(graph1, graph2).size();
        int adjFn = GraphUtils.adjacenciesComplement(graph2, graph1).size();

        int de = GraphUtils.numDirectionalErrors(graph1, graph2);
        int bid = GraphUtils.numBidirected(graph1);

//        int arrowFp = GraphUtils.arrowEndpointComplement(graph1, graph2);
//        int arrowFn = GraphUtils.arrowEndpointComplement(graph2, graph1);

//        System.out.println("adj fp = " + adjFp + " adj fn = " + adjFn + " arrow fp = " + arrowFp + " + arrow fn = " + arrowFn);
        System.out.println("adj fp = " + adjFp + " adj fn = " + adjFn + " de = " + de + " + bid = " + bid);
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestPValueImprover.class);
    }

    public int getRandomCount() {
        return randomCount;
    }

    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
    }
}
