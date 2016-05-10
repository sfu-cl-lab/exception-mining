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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesEstimator;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;

import java.text.NumberFormat;
import java.util.*;

/**
 * Contains methods to explore performance of conservative PC vs. related
 * algorithms. Might want to pull out some of these methods for wider use
 * or unit testing.
 *
 * @author Joseph Ramsey
 * @version $Revision$ $Date$
 */
public class ExploreCpcReport {
    public void countBidirectedEdges() {
        TetradLogger.getInstance().clear();

        double alpha = 0.01;

        for (int dim = 5; dim <= 100; dim += 5) {
            for (int iter = 0; iter < 5; iter++) {
                Dag trueGraph = GraphUtils.createRandomDag(dim, 0, dim, 10, 10,
                        10, false);

                SemPm semPm = new SemPm(trueGraph);
                SemIm semIm = new SemIm(semPm);
                RectangularDataSet dataSet = semIm.simulateData(1000, false);
                IndependenceTest test = new IndTestFisherZ(dataSet, alpha);
                PcSearch search = new PcSearch(test, new Knowledge());
                Graph resultGraph = search.search();
                int numEdges = resultGraph.getNumEdges();
                int numBidirected = 0;

                for (Edge edge : resultGraph.getEdges()) {
                    if (Edges.isBidirectedEdge(edge)) {
                        numBidirected++;
                    }
                }

                System.out.println(
                        dim + "\t" + numEdges + "\t" + numBidirected);
            }
        }
    }

//    public void analyzeFpColliders() {
//        LogUtils.removeStandardOutHandler("tetradlog");
//        Logger logger = LogUtils.getInstance().getLogger();
//        logger.setUseParentHandlers(false);
//        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
//
//        double alpha = 0.01;
//
//        System.out.println(
//                "DIM\tCEFP\tCEFN\tCINDFP\tCOLLFP\tCOLLFPMAX\tBIDE\tBIDEMAX");
//
//        for (int dim = 5; dim <= 100; dim += 5) {
//            int cefpSum = 0;
//            int cefnSum = 0;
//            int cintfpSum = 0;
//            int collfpSum = 0;
//            int collfpMax = 0;
//            int bideSum = 0;
//            int bideMax = 0;
//
//            int numIterations = 25;
//
//            for (int iter = 0; iter < numIterations; iter++) {
//                Dag trueGraph = GraphUtils.createRandomDag(dim, 0, dim, 10, 10,
//                        10, false);
//
//                SemPm semPm = new SemPm(trueGraph);
//                SemIm semIm = new SemIm(semPm);
//                RectangularDataSet dataSet = semIm.simulateData(1000);
//                IndependenceTest test = new IndTestFisherZ(dataSet, alpha);
//                Cpc search =
//                        new Cpc(test, new Knowledge());
//                search.setTrueGraph(trueGraph);
//                search.search();
//
//                cefpSum += search.getCefp();
//                cefnSum += search.getCefn();
//                cintfpSum += search.getCindfp();
//                collfpSum += search.getCollfp();
//
//                if (search.getCollfp() > collfpMax) {
//                    collfpMax = search.getCollfp();
//                }
//
//                bideSum += search.getBide();
//
//                if (search.getBide() > bideMax) {
//                    bideMax = search.getBide();
//                }
//            }
//
//            double cefpAvg = cefpSum / (double) numIterations;
//            double cefnAvg = cefnSum / (double) numIterations;
//            double cintffAvg = cintfpSum / (double) numIterations;
//            double collffAvg = collfpSum / (double) numIterations;
//            double bideAvg = bideSum / (double) numIterations;
//
//            System.out.println(dim + "\t" + nf.format(cefpAvg) + "\t" +
//                    nf.format(cefnAvg) + "\t" + nf.format(cintffAvg) + "\t" +
//                    nf.format(collffAvg) + "\t" + collfpMax + "\t" +
//                    nf.format(bideAvg) + "\t" + bideMax);
//        }
//    }

    public void generateUaiReport() {
        TetradLogger.getInstance().clear();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        NumberFormat nf2 = NumberFormatUtil.getInstance().getNumberFormat();

        int sampleSize = 20000;
//        int dim = 50;
        double alpha = 0.05;
        int depth = -1;
        int numIterations = 5;

        System.out.println(
                "DIM\tPCTIME\tPCAPTFP\tPCAPTFN\tNPATADJ\tPCNESTADJ\tPCADJFP\tPCADJFN\tPCBIDFP\tPCNCOL\tPCCOLFP\tPCCOLFN\tPCNNC\tPCNCFP\tPCNCFN" +
                        "\tCPCTIME\tCPCAPTFP\tCPCAPTFN\tCPCNESTADJ\tCPCADJFP\tCPCADJFN\tCPCBIDFP\tCPCNCOL\tCPCCOLFP\tCPCCOLFN\tCPCNNC\tCPCNCFP\tCPCNCFN" +
                        "\tCPCAPTFP2\tCPCAPTFN2\tCPCUFUT\tCPCALLUT\tCPC.UFUT");

//        for (int sampleSize = 100; sampleSize <= 5000; sampleSize += 100) {
        for (int dim = 5; dim <= 100; dim += 5) {
            long timePc = 0;
            int aptfpPc = 0;
            int aptfnPc = 0;
            int numAdjPattern = 0;
            int numAdjPc = 0;
            int adjfpPc = 0;
            int adjfnPc = 0;
            int bidfpPc = 0;
            int ncollPc = 0;
            int collFpPc = 0;
            int collFnPc = 0;
            int nncPc = 0;
            int ncFpPc = 0;
            int ncFnPc = 0;

            long timeCpc = 0;
            int aptfpCpc = 0;
            int aptfnCpc = 0;
            int numAdjCpc = 0;
            int adjfpCpc = 0;
            int adjfnCpc = 0;
            int bidfpCpc = 0;
            int ncollCpc = 0;
            int collFpCpc = 0;
            int collFnCpc = 0;
            int nncCpc = 0;
            int ncFpCpc = 0;
            int ncFnCpc = 0;

            int aptfp2Cpc = 0;
            int aptfn2Cpc = 0;

            int numUnfaithfulPairs = 0;
            int numPairs = 0;
            double percentUnfaithful = 0.0;

            for (int iter = 0; iter < numIterations; iter++) {
                Dag trueGraph =
                        GraphUtils.createRandomDag(dim, 0, dim, 10, 10,
                                10, false);

                SemPm semPm = new SemPm(trueGraph);
                SemIm semIm = new SemIm(semPm);
                RectangularDataSet dataSet = semIm.simulateData(sampleSize, false);
                IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

//                BayesPm bayesPm = new BayesPm(trueGraph, 2, 4);
//                BayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
//                RectangularDataSet dataSet = bayesIm.simulateData(sampleSize);
//                IndependenceTest test = new IndTestGSquare(dataSet, alpha);

                PcSearch pc = new PcSearch(test, new Knowledge());
                Graph pcGraph = pc.search();

//                GesSearch pc = new GesSearch(dataSet);
//                Graph pcGraph = pc.search();

//                IndependenceTest test2 = new IndTestGraph(trueGraph);
//                PcSearch patternSearch = new PcSearch(test2, new Knowledge());
//                patternSearch.setDepth(depth);
//                Graph pattern = patternSearch.search();

                EdgeListGraph pattern = new EdgeListGraph(trueGraph);
                rebuildPattern(pattern);

//                Graph pattern = trueGraph;
//
                timePc += pc.getElapsedTime();
                aptfpPc += countArrowptErrors(pcGraph, pattern);
                aptfnPc += countArrowptErrors(pattern, pcGraph);
                numAdjPattern += pattern.getNumEdges();
                numAdjPc += pcGraph.getNumEdges();
                adjfpPc += countAdjErrors(pcGraph, pattern);
                adjfnPc += countAdjErrors(pattern, pcGraph);
                bidfpPc += calcBidfp(pcGraph);
                ncollPc += countUnshieldedColliders(pcGraph);
//                cllFpPc += countColliderFpErrors(pattern, pcGraph, "PC");
                collFpPc += countUnshieldedColliderErrors(pcGraph, pattern);
                collFnPc += countUnshieldedColliderErrors(pattern, pcGraph);
                nncPc += countUnshieldedColliders(pcGraph);
                ncFpPc += countUnshieldedNoncolliderErrors(pcGraph, pattern);
//                ncFnPc += countNoncolliderFpErrors(pattern, pcGraph, "PC");
                ncFnPc += countUnshieldedNoncolliderErrors(pattern, pcGraph);

                Cpc cpc = new Cpc(test, new Knowledge());
                cpc.setDepth(depth);

                Graph cpcGraph = cpc.search();

//                Set<Triple> pcUnshieldedNoncolliders = pc.getUnshieldedNoncolliders();
//                Set<Triple> cpcUnshieldedNoncolliders = cpc.getNoncolliderTriples();
//
//                if (!pcUnshieldedNoncolliders.containsAll(cpcUnshieldedNoncolliders)) {
//                    System.out.println("PC: " + pcUnshieldedNoncolliders);
//                    System.out.println("CPC: " + cpcUnshieldedNoncolliders);
//                }

                timeCpc += cpc.getElapsedTime();
                aptfpCpc += countArrowptErrors(cpcGraph, pattern);
                aptfnCpc += countArrowptErrors(pattern, cpcGraph);
                numAdjCpc += cpcGraph.getNumEdges();
                adjfpCpc += countAdjErrors(cpcGraph, pattern);
                adjfnCpc += countAdjErrors(pattern, cpcGraph);
                bidfpCpc += calcBidfp(cpcGraph);
                ncollCpc += countUnshieldedColliders(cpcGraph);
//                collFpCpc += countColliderFpErrors(trueGraph, cpcGraph, "CPC");
                collFpCpc += countUnshieldedColliderErrors(cpcGraph, pattern);
                collFnCpc += countUnshieldedColliderErrors(pattern, cpcGraph);
                nncCpc += countUnshieldedNoncolliders(cpcGraph);
                ncFpCpc += countUnshieldedNoncolliderErrors(cpcGraph, pattern);
//                ncFnCpc += countNoncolliderFpErrors(pattern, cpcGraph, "CPC");
                ncFnCpc += countUnshieldedNoncolliderErrors(pattern, cpcGraph);

                Graph estGraph3 = cpc.orientationForGraph(trueGraph);

                aptfp2Cpc += countArrowptErrors(estGraph3, pattern);
                aptfn2Cpc += countArrowptErrors(pattern, estGraph3);

                numUnfaithfulPairs += cpc.getNumAmbiguousPairs();
                numPairs += cpc.getNumPairs();
                percentUnfaithful += (cpc.getNumAmbiguousPairs() /
                        (double) cpc.getNumPairs()) * 100.0;


                printNcfpInCPCButNotPC(pcGraph, cpcGraph, pattern, pc, cpc);
            }

            double timePcAvg = (timePc / 1000d) / (double) numIterations;
            double aptfpPcAvg = aptfpPc / (double) numIterations;
            double aptfnPcAvg = aptfnPc / (double) numIterations;
            double numAdjPatternAvg = numAdjPattern / (double) numIterations;
            double numAdjPcAvg = numAdjPc / (double) numIterations;
            double adjfpPcAvg = adjfpPc / (double) numIterations;
            double adjfnPcAvg = adjfnPc / (double) numIterations;
            double bidfpPcAvg = bidfpPc / (double) numIterations;
            double ncollPcAvg = ncollPc / (double) numIterations;
            double collFpPcAvg = collFpPc / (double) numIterations;
            double collFnPcAvg = collFnPc / (double) numIterations;
            double nncPcAvg = nncPc / (double) numIterations;
            double ncFpPcAvg = ncFpPc / (double) numIterations;
            double ncFnPcAvg = ncFnPc / (double) numIterations;

            double timeCpcAvg = (timeCpc / 1000d) / (double) numIterations;
            double aptfpCpcAvg = aptfpCpc / (double) numIterations;
            double aptfnCpcAvg = aptfnCpc / (double) numIterations;
            double numAdjCpcAvg = numAdjCpc / (double) numIterations;
            double adjfpCpcAvg = adjfpCpc / (double) numIterations;
            double adjfnCpcAvg = adjfnCpc / (double) numIterations;
            double bidfpCpcAvg = bidfpCpc / (double) numIterations;
            double ncollCpcAvg = ncollCpc / (double) numIterations;
            double collFpCpcAvg = collFpCpc / (double) numIterations;
            double collFnCpcAvg = collFnCpc / (double) numIterations;
            double nncCpcAvg = nncCpc / (double) numIterations;
            double ncFpCpcAvg = ncFpCpc / (double) numIterations;
            double ncFnCpcAvg = ncFnCpc / (double) numIterations;

            double aptfp2CpcAvg = aptfp2Cpc / (double) numIterations;
            double aptfn2CpcAvg = aptfn2Cpc / (double) numIterations;

            double numUnfaithfulPairsAvg =
                    numUnfaithfulPairs / (double) numIterations;
            double numPairsAvg = numPairs / (double) numIterations;
            double percentUnfaithfulAvg =
                    percentUnfaithful / (double) numIterations;

            System.out.println(dim /*sampleSize*/ + "\t" +
                    nf2.format(timePcAvg) + "\t" +
                    nf.format(aptfpPcAvg) + "\t" +
                    nf.format(aptfnPcAvg) + "\t" +
                    nf.format(numAdjPatternAvg) + "\t" +
                    nf.format(numAdjPcAvg) + "\t" +
                    nf.format(adjfpPcAvg) + "\t" +
                    nf.format(adjfnPcAvg) + "\t" +
                    nf.format(bidfpPcAvg) + "\t" +
                    nf.format(ncollPcAvg) + "\t" +
                    nf.format(collFpPcAvg) + "\t" +
                    nf.format(collFnPcAvg) + "\t" +
                    nf.format(nncPcAvg) + "\t" +
                    nf.format(ncFpPcAvg) + "\t" +
                    nf.format(ncFnPcAvg) + "\t" +

                    nf2.format(timeCpcAvg) + "\t" +
                    nf.format(aptfpCpcAvg) + "\t" +
                    nf.format(aptfnCpcAvg) + "\t" +
                    nf.format(numAdjCpcAvg) + "\t" +
                    nf.format(adjfpCpcAvg) + "\t" +
                    nf.format(adjfnCpcAvg) + "\t" +
                    nf.format(bidfpCpcAvg) + "\t" +
                    nf.format(ncollCpcAvg) + "\t" +
                    nf.format(collFpCpcAvg) + "\t" +
                    nf.format(collFnCpcAvg) + "\t" +
                    nf.format(nncCpcAvg) + "\t" +
                    nf.format(ncFpCpcAvg) + "\t" +
                    nf.format(ncFnCpcAvg) + "\t" +

                    nf.format(aptfp2CpcAvg) + "\t" +
                    nf.format(aptfn2CpcAvg) + "\t" +

                    nf.format(numUnfaithfulPairsAvg) + "\t" +
                    nf.format(numPairsAvg) + "\t" +
                    nf.format(percentUnfaithfulAvg));
        }
    }

    public void limitExperiment() {
        TetradLogger.getInstance().clear();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

//        int sampleSize = 20000;
        int dim = 50;
        double alpha = 0.001;
        int depth = -1;
        int numIterations = 1;

        System.out.println("N\tNUT\tNT");

        Dag trueGraph = GraphUtils.createRandomDag(dim, 0, dim, 10, 10,
                10, false);

        SemPm semPm = new SemPm(trueGraph);
        SemIm semIm = new SemIm(semPm);

        for (int sampleSize = 300000; sampleSize <= 300000; sampleSize += 5000) {
            double numUnfaithful = 0.0;
            double numTriples = 0.0;

            for (int iter = 0; iter < numIterations; iter++) {
                RectangularDataSet dataSet = semIm.simulateData(sampleSize, false);
                IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

                Cpc cpc = new Cpc(test, new Knowledge());
                cpc.setDepth(depth);

                Graph cpcPattern = cpc.search();

                numUnfaithful += cpc.getNumAmbiguousPairs();
                numTriples += cpc.getNumPairs();
            }

            double numUnfaithfulAvg =
                    numUnfaithful / (double) numIterations;
            double numTriplesAvg =
                    numTriples / (double) numIterations;

            System.out.println(sampleSize + "\t" + nf.format(numUnfaithfulAvg)
                    + "\t" + nf.format(numTriplesAvg));
        }
    }

    public void compareCpcToGes() {
        TetradLogger.getInstance().clear();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        NumberFormat nf2 = NumberFormatUtil.getInstance().getNumberFormat();

        int sampleSize = 1000;
        boolean latentDataSaved = false;
//        int dim = 50;
        double alpha = 0.05;
        int depth = -1;
        int numIterations = 5;

        System.out.println(
                "DIM\tCPCTIME\tCPCAPTFP\tCPCAPTFN\tNPATADJ\tCPCNESTADJ\tCPCADJFP\tCPCADJFN\tCPCBIDFP\tCPCNCOL\tCPCCOLFP\tCPCCOLFN\tCPCNNC\tCPCNCFP\tCPCNCFN" +
                        "\tGESTIME\tGESAPTFP\tGESAPTFN\tGESNESTADJ\tGESADJFP\tGESADJFN\tGESBIDFP\tGESNCOL\tGESCOLFP\tGESCOLFN\tGESNNC\tGESNCFP\tGESNCFN");
//        +
//                        "\tGESAPTFP2\tGESAPTFN2\tGESUFUT\tGESALLUT\tGES.UFUT");

//        for (int sampleSize = 100; sampleSize <= 5000; sampleSize += 100) {
        for (int dim = 5; dim <= 100; dim += 5) {
            long timeCpc = 0;
            int aptfpCpc = 0;
            int aptfnCpc = 0;
            int numAdjPattern = 0;
            int numAdjCpc = 0;
            int adjfpCpc = 0;
            int adjfnCpc = 0;
            int bidfpCpc = 0;
            int ncollCpc = 0;
            int collFpCpc = 0;
            int collFnCpc = 0;
            int nncCpc = 0;
            int ncFpCpc = 0;
            int ncFnCpc = 0;

            long timeGes = 0;
            int aptfpGes = 0;
            int aptfnGes = 0;
            int numAdjGes = 0;
            int adjfpGes = 0;
            int adjfnGes = 0;
            int bidfpGes = 0;
            int ncollGes = 0;
            int collFpGes = 0;
            int collFnGes = 0;
            int nncGes = 0;
            int ncFpGes = 0;
            int ncFnGes = 0;

            for (int iter = 0; iter < numIterations; iter++) {
                Dag trueGraph =
                        GraphUtils.createRandomDag(dim, 0, dim, 10, 10,
                                10, false);

                SemPm semPm = new SemPm(trueGraph);
                SemIm semIm = new SemIm(semPm);
                RectangularDataSet dataSet = semIm.simulateData(sampleSize, latentDataSaved);
                IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

//                BayesPm bayesPm = new BayesPm(trueGraph, 2, 4);
//                BayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
//                RectangularDataSet dataSet = bayesIm.simulateData(sampleSize);
//                IndependenceTest test = new IndTestGSquare(dataSet, alpha);

                Cpc cpc = new Cpc(test, new Knowledge());
                Graph cpcGraph = cpc.search();

//                GesSearch cpc = new GesSearch(dataSet);
//                Graph cpcGraph = cpc.search();

//                IndependenceTest test2 = new IndTestGraph(trueGraph);
//                PcSearch patternSearch = new PcSearch(test2, new Knowledge());
//                patternSearch.setDepth(depth);
//                Graph pattern = patternSearch.search();

                EdgeListGraph pattern = new EdgeListGraph(trueGraph);
                rebuildPattern(pattern);

//                Graph pattern = trueGraph;
//
                timeCpc += cpc.getElapsedTime();
                aptfpCpc += countArrowptErrors(cpcGraph, pattern);
                aptfnCpc += countArrowptErrors(pattern, cpcGraph);
                numAdjPattern += pattern.getNumEdges();
                numAdjCpc += cpcGraph.getNumEdges();
                adjfpCpc += countAdjErrors(cpcGraph, pattern);
                adjfnCpc += countAdjErrors(pattern, cpcGraph);
                bidfpCpc += calcBidfp(cpcGraph);
                ncollCpc += countUnshieldedColliders(cpcGraph);
//                cllFpPc += countColliderFpErrors(pattern, cpcGraph, "PC");
                collFpCpc += countUnshieldedColliderErrors(cpcGraph, pattern);
                collFnCpc += countUnshieldedColliderErrors(pattern, cpcGraph);
                nncCpc += countUnshieldedNoncolliders(cpcGraph);
                ncFpCpc += countUnshieldedNoncolliderErrors(cpcGraph, pattern);
//                ncFnCpc += countNoncolliderFpErrors(pattern, cpcGraph, "PC");
                ncFnCpc += countUnshieldedNoncolliderErrors(pattern, cpcGraph);

                GesSearch gesSearch = new GesSearch(dataSet);
//                ges.setDepth(depth);

                Graph gesGraph = gesSearch.search();

//                Set<Triple> pcUnshieldedNoncolliders = cpc.getUnshieldedNoncolliders();
//                Set<Triple> cpcUnshieldedNoncolliders = ges.getNoncolliderTriples();
//
//                if (!pcUnshieldedNoncolliders.containsAll(cpcUnshieldedNoncolliders)) {
//                    System.out.println("PC: " + pcUnshieldedNoncolliders);
//                    System.out.println("CPC: " + cpcUnshieldedNoncolliders);
//                }

                timeGes += gesSearch.getElapsedTime();
                aptfpGes += countArrowptErrors(gesGraph, pattern);
                aptfnGes += countArrowptErrors(pattern, gesGraph);
                numAdjGes += gesGraph.getNumEdges();
                adjfpGes += countAdjErrors(gesGraph, pattern);
                adjfnGes += countAdjErrors(pattern, gesGraph);
                bidfpGes += calcBidfp(gesGraph);
                ncollGes += countUnshieldedColliders(gesGraph);
//                collFpGes += countColliderFpErrors(trueGraph, gesGraph, "CPC");
                collFpGes += countUnshieldedColliderErrors(gesGraph, pattern);
                collFnGes += countUnshieldedColliderErrors(pattern, gesGraph);
                nncGes += countUnshieldedNoncolliders(gesGraph);
                ncFpGes += countUnshieldedNoncolliderErrors(gesGraph, pattern);
//                ncFnGes += countNoncolliderFpErrors(pattern, gesGraph, "CPC");
                ncFnGes += countUnshieldedNoncolliderErrors(pattern, gesGraph);

//                Graph estGraph3 = ges.orientationForGraph(trueGraph);

//                aptfp2Cpc += countArrowptErrors(estGraph3, pattern);
//                aptfn2Cpc += countArrowptErrors(pattern, estGraph3);
//
//                numUnfaithfulPairs += ges.getNumAmbiguousPairs();
//                numPairs += ges.getNumPairs();
//                percentUnfaithful += (ges.getNumAmbiguousPairs() /
//                        (double) ges.getNumPairs()) * 100.0;

//                printNcfpInCPCButNotPC(cpcGraph, gesGraph, pattern, cpc, ges);
            }

            double timeCpcAvg = (timeCpc / 1000d) / (double) numIterations;
            double aptfpCpcAvg = aptfpCpc / (double) numIterations;
            double aptfnCpcAvg = aptfnCpc / (double) numIterations;
            double numAdjPatternAvg = numAdjPattern / (double) numIterations;
            double numAdjCpcAvg = numAdjCpc / (double) numIterations;
            double adjfpCpcAvg = adjfpCpc / (double) numIterations;
            double adjfnCpcAvg = adjfnCpc / (double) numIterations;
            double bidfpCpcAvg = bidfpCpc / (double) numIterations;
            double ncollCpcAvg = ncollCpc / (double) numIterations;
            double collFpCpcAvg = collFpCpc / (double) numIterations;
            double collFnCpcAvg = collFnCpc / (double) numIterations;
            double nncCpcAvg = nncCpc / (double) numIterations;
            double ncFpCpcAvg = ncFpCpc / (double) numIterations;
            double ncFnCpcAvg = ncFnCpc / (double) numIterations;

            double timeGesAvg = (timeGes / 1000d) / (double) numIterations;
            double aptfpGesAvg = aptfpGes / (double) numIterations;
            double aptfnGesAvg = aptfnGes / (double) numIterations;
            double numAdjGesAvg = numAdjGes / (double) numIterations;
            double adjfpGesAvg = adjfpGes / (double) numIterations;
            double adjfnGesAvg = adjfnGes / (double) numIterations;
            double bidfpGesAvg = bidfpGes / (double) numIterations;
            double ncollGesAvg = ncollGes / (double) numIterations;
            double collFpGesAvg = collFpGes / (double) numIterations;
            double collFnGesAvg = collFnGes / (double) numIterations;
            double nncGesAvg = nncGes / (double) numIterations;
            double ncFpGesAvg = ncFpGes / (double) numIterations;
            double ncFnGesAvg = ncFnGes / (double) numIterations;

            System.out.println(dim /*sampleSize*/ + "\t" +
                    nf2.format(timeCpcAvg) + "\t" +
                    nf.format(aptfpCpcAvg) + "\t" +
                    nf.format(aptfnCpcAvg) + "\t" +
                    nf.format(numAdjPatternAvg) + "\t" +
                    nf.format(numAdjCpcAvg) + "\t" +
                    nf.format(adjfpCpcAvg) + "\t" +
                    nf.format(adjfnCpcAvg) + "\t" +
                    nf.format(bidfpCpcAvg) + "\t" +
                    nf.format(ncollCpcAvg) + "\t" +
                    nf.format(collFpCpcAvg) + "\t" +
                    nf.format(collFnCpcAvg) + "\t" +
                    nf.format(nncCpcAvg) + "\t" +
                    nf.format(ncFpCpcAvg) + "\t" +
                    nf.format(ncFnCpcAvg) + "\t" +

                    nf2.format(timeGesAvg) + "\t" +
                    nf.format(aptfpGesAvg) + "\t" +
                    nf.format(aptfnGesAvg) + "\t" +
                    nf.format(numAdjGesAvg) + "\t" +
                    nf.format(adjfpGesAvg) + "\t" +
                    nf.format(adjfnGesAvg) + "\t" +
                    nf.format(bidfpGesAvg) + "\t" +
                    nf.format(ncollGesAvg) + "\t" +
                    nf.format(collFpGesAvg) + "\t" +
                    nf.format(collFnGesAvg) + "\t" +
                    nf.format(nncGesAvg) + "\t" +
                    nf.format(ncFpGesAvg) + "\t" +
                    nf.format(ncFnGesAvg) + "\t"
            );
        }
    }


    public void analyzeFci() {
        TetradLogger.getInstance().clear();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        NumberFormat nf2 = NumberFormatUtil.getInstance().getNumberFormat();

        int depth = 3;
        double alpha = 0.05;
        boolean compareToTrue = true;

        System.out.println("FCI, sepset on left, local on right.");
        System.out.println("alpha = " + alpha);
        System.out.println("depth = " + depth);
        System.out.println("Comparing to true depth " + compareToTrue);

        System.out.println(
                "DIM\tTIME\tAPTFP\tAPTFN\tNULLFP\tNULLFN\tADJFP\tADJFN\tBIDFP" +
                        "\tTIME\tAPTFP\tAPTFN\tNULLFP\tNULLFN\tADJFP\tADJFN\tBIDFP");

        for (int dim = 5; dim <= 100; dim += 5) {
            long timeSepset = 0;
            int aptfpSepset = 0;
            int aptfnSepset = 0;
            int nullfpSepset = 0;
            int nullfnSepset = 0;
            int adjfpSepset = 0;
            int adjfnSepset = 0;
            int bidfpSepset = 0;

            long timeLocal = 0;
            int aptfpLocal = 0;
            int aptfnLocal = 0;
            int nullfpLocal = 0;
            int nullfnLocal = 0;
            int adjfpLocal = 0;
            int adjfnLocal = 0;
            int bidfpLocal = 0;

            int numIterations = 1;

            for (int iter = 0; iter < numIterations; iter++) {
                Dag trueGraph = GraphUtils.createRandomDag(dim, 0, 2 * dim, 10, 10,
                        10, false);

                SemPm semPm = new SemPm(trueGraph);
                SemIm semIm = new SemIm(semPm);
                RectangularDataSet dataSet = semIm.simulateData(1000, false);
                IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

                IndependenceTest test2 = new IndTestDSep(trueGraph);
                FciSearch pagSearch = new FciSearch(test2, new Knowledge());

                if (!compareToTrue) {
                    pagSearch.setDepth(depth);
                }

                Graph pag = pagSearch.search();

                FciSearch sepsetSearch = new FciSearch(test, new Knowledge());
                sepsetSearch.setDepth(2);
                Graph estGraph = sepsetSearch.search();

                timeSepset += sepsetSearch.getElapsedTime();
                aptfpSepset += countArrowptErrors(estGraph, pag);
                aptfnSepset += countArrowptErrors(pag, estGraph);
                nullfpSepset += countNullptErrors(estGraph, pag);
                nullfnSepset += countNullptErrors(pag, estGraph);
                adjfpSepset += countAdjErrors(estGraph, pag);
                adjfnSepset += countAdjErrors(pag, estGraph);
                bidfpSepset += calcBidfp(estGraph);

                Cfci localSearch =
                        new Cfci(test, new Knowledge());
                localSearch.setDepth(5);
                Graph estGraph2 = localSearch.search();

                timeLocal += localSearch.getElapsedTime();
                aptfpLocal += countArrowptErrors(estGraph2, pag);
                aptfnLocal += countArrowptErrors(pag, estGraph2);
                nullfpLocal += countNullptErrors(estGraph2, pag);
                nullfnLocal += countNullptErrors(pag, estGraph2);
                adjfpLocal += countAdjErrors(estGraph2, pag);
                adjfnLocal += countAdjErrors(pag, estGraph2);
                bidfpLocal += calcBidfp(estGraph2);
            }

            double timeSepsetAvg =
                    (timeSepset / 1000d) / (double) numIterations;
            double aptfpSepsetAvg = aptfpSepset / (double) numIterations;
            double aptfnSepsetAvg = aptfnSepset / (double) numIterations;
            double nullfpSepsetAvg = nullfpSepset / (double) numIterations;
            double nullfnSepsestAvg = nullfnSepset / (double) numIterations;
            double adjfpSepsetAvg = adjfpSepset / (double) numIterations;
            double adjfnSepsetAvg = adjfnSepset / (double) numIterations;
            double bidfpSepsetAvg = bidfpSepset / (double) numIterations;

            double timeLocalAvg = (timeLocal / 1000d) / (double) numIterations;
            double aptfpLocalAvg = aptfpLocal / (double) numIterations;
            double aptfnLocalAvg = aptfnLocal / (double) numIterations;
            double nullfpLocalAvg = nullfpLocal / (double) numIterations;
            double nullfnLocalAvg = nullfnLocal / (double) numIterations;
            double adjfpLocalAvg = adjfpLocal / (double) numIterations;
            double adjfnLocalAvg = adjfnLocal / (double) numIterations;
            double bidfpLocalAvg = bidfpLocal / (double) numIterations;

            System.out.println(dim + "\t" + nf2.format(timeSepsetAvg) + "\t" +
                    nf.format(aptfpSepsetAvg) + "\t" +
                    nf.format(aptfnSepsetAvg) + "\t" +
                    nf.format(nullfpSepsetAvg) + "\t" +
                    nf.format(nullfnSepsestAvg) + "\t" +
                    nf.format(adjfpSepsetAvg) + "\t" +
                    nf.format(adjfnSepsetAvg) + "\t" +
                    nf.format(bidfpSepsetAvg) +

                    "\t" + nf2.format(timeLocalAvg) + "\t" +
                    nf.format(aptfpLocalAvg) + "\t" + nf.format(aptfnLocalAvg) +
                    "\t" + nf.format(nullfpLocalAvg) + "\t" +
                    nf.format(nullfnLocalAvg) + "\t" +
                    nf.format(adjfpLocalAvg) + "\t" + nf.format(adjfnLocalAvg) +
                    "\t" + nf.format(bidfpLocalAvg));
        }
    }

    public void compareCpcToGes3Nodes() {
        Dag dag = new Dag();

        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");

        dag.addNode(a);
        dag.addNode(b);
        dag.addNode(c);

        dag.addDirectedEdge(a, b);
        dag.addDirectedEdge(c, b);

        System.out.println("graph = " + dag);

        BayesPm bayesPm = new BayesPm(dag);
        bayesPm.setNumCategories(b, 4);

        for (int i = 0; i < 1000; i++) {
            BayesIm bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
            RectangularDataSet data = bayesIm.simulateData(20000, false);
            IndependenceTest test = new IndTestChiSquare(data, 0.05);

            Node a2 = test.getVariable(a.toString());
            Node b2 = test.getVariable(b.toString());
            Node c2 = test.getVariable(c.toString());

            List<Node> a2list = new LinkedList<Node>();
            a2list.add(a2);

            List<Node> b2list = new LinkedList<Node>();
            b2list.add(b2);

            List<Node> c2list = new LinkedList<Node>();
            c2list.add(c2);

            if (!test.isIndependent(a2, c2, new LinkedList<Node>())) {
                continue;
            }

            if (!test.isDependent(a2, b2, new LinkedList<Node>())) {
                continue;
            }

            if (!test.isDependent(a2, b2, c2list)) {
                continue;
            }

            if (!test.isDependent(b2, c2, new LinkedList<Node>())) {
                continue;
            }

            if (!test.isDependent(b2, c2, a2list)) {
                continue;
            }

            if (!test.isIndependent(a2, c2, b2list)) {
                continue;
            }

            System.out.println("**************");
            System.out.println(bayesIm);

            Cpc cpc = new Cpc(test, new Knowledge());
            Graph cpcPattern = cpc.search();
            System.out.println("CPC result");
            System.out.println(cpcPattern);
            System.out.println(
                    "Unfaithful triples: " + cpc.getAmbiguousTriples());

            System.out.println();
            GesSearch gesSearch = new GesSearch(data);
            Graph gesPattern = gesSearch.search();
            System.out.println("Ges result");
            System.out.println(gesPattern);
        }
    }

    public void compareAlphaVsDim() {
        TetradLogger.getInstance().clear();
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        NumberFormat nf2 = NumberFormatUtil.getInstance().getNumberFormat();

        int[] dims = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        double[] alphas = new double[]{0.001, 0.01, 0.05, 0.1, 0.15, 0.2};

        for (int dim : dims) {
            double[] rows = new double[alphas.length];

            for (int j = 0; j < alphas.length; j++) {
                double alpha = alphas[j];

                int aptfpLocal = 0;
                int numIterations = 25;

                for (int iter = 0; iter < numIterations; iter++) {
                    Dag trueGraph = GraphUtils.createRandomDag(dim, 0, dim, 10,
                            10, 10, false);

                    SemPm semPm = new SemPm(trueGraph);
                    SemIm semIm = new SemIm(semPm);
                    RectangularDataSet dataSet = semIm.simulateData(1000, false);
                    IndependenceTest test = new IndTestFisherZ(dataSet, alpha);

                    EdgeListGraph pattern = new EdgeListGraph(trueGraph);
                    rebuildPattern(pattern);

                    Cpc localSearch =
                            new Cpc(test, new Knowledge());
                    Graph estGraph = localSearch.search();

                    aptfpLocal += countArrowptErrors(estGraph, pattern);
                }

                rows[j] = aptfpLocal / (double) numIterations;
            }

            for (double row : rows) {
                System.out.print(row + "\t");
            }

            System.out.println();
        }
    }

    public void countIndependenceConditions() {
        int dimension = 10;
        int depth = 3;

        Graph graph = GraphUtils.createRandomDag(dimension, 0, dimension,
                10, 10, 10, false);

        List<IndependenceFact> allIndependenceFacts =
                getDsepFacts(graph, depth);

        List<IndependenceFact> markovIndependenceFacts =
                getDirectMarkovConstraints(graph, depth);

        List<IndependenceFact> ofDependenceFacts =
                getOfDependencies(graph, depth);

        List<IndependenceFact> afDependenceFacts =
                getAfDependencies(graph, depth);

        System.out.println("Dimension: " + dimension);
        System.out.println("Depth: " + depth);
        System.out.println("# independencies mentioned by the Markov condition: " +
                markovIndependenceFacts.size());
        System.out.println("# All independencies implied by the Markov condition: " +
                allIndependenceFacts.size());
        System.out.println("# dependencies mentioned by the Orientation Faithfulness condition: " +
                ofDependenceFacts.size());
        System.out.println("# dependencies mentioned by the Adjacency Faithfulness condition: " +
                afDependenceFacts.size());

        double percent = ofDependenceFacts.size() / (double) afDependenceFacts.size()
                * 100.0;

        System.out.println("Proportion of to af: " + percent);

        System.out.println(graph);

//        for (IndependenceFact fact : markovIndependenceFacts) {
//            System.out.println("Must be independent: " + fact);
//        }
//
//        for (IndependenceFact fact : ofDependenceFacts) {
//            System.out.println("Must be dependent: " + fact);
//        }
    }

    private List<IndependenceFact> getAfDependencies(Graph graph, int depth) {
        List<IndependenceFact> facts = new ArrayList<IndependenceFact>();

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();

            List<List<Node>> condLists =
                    getSubsets(x, x, y, graph, depth, false);

            for (List<Node> cond : condLists) {
                IndependenceFact fact = new IndependenceFact(x, y, cond);
                facts.add(fact);
            }
        }

        return facts;
    }

    private List<IndependenceFact> getOfDependencies(Graph graph, int depth) {
        List<IndependenceFact> facts = new ArrayList<IndependenceFact>();

        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        Collections.sort(nodes, new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (Node b : nodes) {
            List<Node> adjb = graph.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (isUnshieldedCollider(a, b, c, graph)) {
//                    System.out.println(
//                            "collider " + tripleString(graph, a, b, c));

                    List<List<Node>> condLists =
                            getSubsets(a, b, c, graph, depth, true);

                    for (List<Node> cond : condLists) {
                        IndependenceFact fact =
                                new IndependenceFact(a, c, cond);
                        facts.add(fact);
                    }
                } else if (isUnshieldedNoncollider(a, b, c, graph)) {
//                    System.out.println(
//                            "noncollider " + tripleString(graph, a, b, c));

                    List<List<Node>> condLists =
                            getSubsets(a, b, c, graph, depth, false);

                    for (List<Node> cond : condLists) {
                        IndependenceFact fact =
                                new IndependenceFact(a, c, cond);
                        facts.add(fact);
                    }
                }
            }
        }

        return facts;
    }

    private List<List<Node>> getSubsets(Node x, Node y, Node z, Graph graph,
                                        int depth, boolean with) {
        List<List<Node>> subsets = new ArrayList<List<Node>>();

        Set<Node> __nodes = new HashSet<Node>(graph.getAdjacentNodes(x));
        __nodes.addAll(graph.getAdjacentNodes(z));
        __nodes.remove(x);
        __nodes.remove(z);

        List<Node> _nodes = new LinkedList<Node>(__nodes);

        int _depth = depth;
        if (_depth == -1) {
            _depth = Integer.MAX_VALUE;
        }
        _depth = Math.min(_depth, _nodes.size());

        for (int d = 1; d <= _depth; d++) {
            ChoiceGenerator cg = new ChoiceGenerator(_nodes.size(), d);
            int[] choice;

            while ((choice = cg.next()) != null) {
                List<Node> condSet = asList(choice, _nodes);

                if (with && !condSet.contains(y)) {
                    continue;
                } else if (!with && condSet.contains(y)) {
                    continue;
                }

                subsets.add(condSet);
            }
        }

        return subsets;
    }

    private static List<Node> asList(int[] indices, List<Node> nodes) {
        List<Node> list = new LinkedList<Node>();

        for (int i : indices) {
            list.add(nodes.get(i));
        }

        return list;
    }


    private List<IndependenceFact> getDsepFacts(Graph graph, int depth) {
        List<IndependenceFact> facts = new ArrayList<IndependenceFact>();

        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        Collections.sort(nodes, new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (int d = 0; d <= depth; d++) {
            for (Node x : nodes) {
                for (Node y : nodes) {
                    if (x == y) {
                        continue;
                    }

                    // This is the standard algorithm, without the v1 bias.
                    if (nodes.size() >= d) {
                        ChoiceGenerator cg =
                                new ChoiceGenerator(nodes.size(), d);
                        int[] choice;

                        while ((choice = cg.next()) != null) {
                            List<Node> z =
                                    SearchGraphUtils.asList(choice, nodes);

                            if (z.contains(x) || z.contains(y)) {
                                continue;
                            }

                            if (graph.isDSeparatedFrom(x, y, z)) {
                                facts.add(new IndependenceFact(x, y, z));
                            }
                        }
                    }
                }
            }
        }

        return facts;
    }

    private List<IndependenceFact> getDirectMarkovConstraints(Graph graph,
                                                              int depth) {
        List<IndependenceFact> facts = new ArrayList<IndependenceFact>();

        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        Collections.sort(nodes, new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (int d = 0; d <= depth; d++) {
            for (Node x : nodes) {
                for (Node y : nodes) {
                    if (x == y) {
                        continue;
                    }

                    // This is the standard algorithm, without the v1 bias.
                    if (nodes.size() >= d) {
                        ChoiceGenerator cg =
                                new ChoiceGenerator(nodes.size(), d);
                        int[] choice;

                        while ((choice = cg.next()) != null) {
                            List<Node> z =
                                    SearchGraphUtils.asList(choice, nodes);

                            if (z.contains(x) || z.contains(y)) {
                                continue;
                            }

                            if (graph.isDSeparatedFrom(x, y, z)) {
                                IndependenceFact fact = new IndependenceFact(x, y, z);

                                if (mentionedByMarkovCondition(graph, x, y, z))
                                {
                                    facts.add(fact);
                                }
                            }
                        }
                    }
                }
            }
        }

        return facts;
    }

    private void printDsep(String label, Node x, Node y, List<Node> z) {
        System.out.println(
                label + ": I(" + x + ", " + y + ", " + z + ")");
    }

    private boolean mentionedByMarkovCondition(Graph graph, Node x, Node y, List<Node> z) {
        boolean b1 = !graph.isAncestorOf(x, y) &&
                graph.getParents(x).containsAll(z) &&
                z.containsAll(graph.getParents(x));
        boolean b2 = !graph.isAncestorOf(y, x) &&
                graph.getParents(y).containsAll(z) &&
                z.containsAll(graph.getParents(y));
        return b1 || b2;
    }

    private int calcBidfp(Graph estGraph) {
        int numBidirected = 0;

        for (Edge edge : estGraph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                numBidirected++;
            }
        }

        return numBidirected;
    }

    /**
     * Counts the adjacencies that are in graph1 but not in graph2.
     *
     * @throws IllegalArgumentException if graph1 and graph2 are not namewise
     *                                  isomorphic.
     */
    private int countAdjErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List<Node> graph1Nodes = graph1.getNodes();
        List<Node> graph2Nodes = graph2.getNodes();

        Comparator<Node> comparator = new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        if (graph1Nodes.size() != graph2Nodes.size()) {
            throw new IllegalArgumentException(
                    "The graph sizes are different.");
        }

        for (int i = 0; i < graph1Nodes.size(); i++) {
            String name1 = graph1Nodes.get(i).getName();
            String name2 = graph2Nodes.get(i).getName();

            if (!name1.equals(name2)) {
                throw new IllegalArgumentException(
                        "Graph names don't " + "correspond.");
            }
        }

        List<Edge> edges1 = graph1.getEdges();

        for (Edge edge : edges1) {
            Node node1 = graph2.getNode(edge.getNode1().getName());
            Node node2 = graph2.getNode(edge.getNode2().getName());

            if (!graph2.isAdjacentTo(node1, node2)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Counts the arrowpoints that are in graph1 but not in graph2.
     */
    private int countArrowptErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List<Node> graph1Nodes = graph1.getNodes();
        List<Node> graph2Nodes = graph2.getNodes();

        Comparator<Node> comparator = new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        if (graph1Nodes.size() != graph2Nodes.size()) {
            throw new IllegalArgumentException(
                    "The graph sizes are different.");
        }

        for (int i = 0; i < graph1Nodes.size(); i++) {
            String name1 = graph1Nodes.get(i).getName();
            String name2 = graph2Nodes.get(i).getName();

            if (!name1.equals(name2)) {
                throw new IllegalArgumentException(
                        "Graph names don't " + "correspond.");
            }
        }

        List<Edge> edges1 = graph1.getEdges();

        for (Edge edge1 : edges1) {
            Node node11 = edge1.getNode1();
            Node node12 = edge1.getNode2();

            Node node21 = graph2.getNode(node11.getName());
            Node node22 = graph2.getNode(node12.getName());

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null) {
                if (edge1.getEndpoint1() == Endpoint.ARROW) {
                    count++;
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW) {
                    count++;
                }
            } else {
                if (edge1.getEndpoint1() == Endpoint.ARROW) {
                    if (edge2.getProximalEndpoint(node21) != Endpoint.ARROW) {
                        count++;
                    }
                }

                if (edge1.getEndpoint2() == Endpoint.ARROW) {
                    if (edge2.getProximalEndpoint(node22) != Endpoint.ARROW) {
                        count++;
                    }
                }
            }
        }

//        System.out.println("Arrowpoint errors = " + count);

        return count;
    }

    private static int countNullptErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        // Construct parallel lists of nodes where nodes of the same
        // name in graph1 and workbench 2 occur in the same position in
        // the list.
        List<Node> graph1Nodes = graph1.getNodes();
        List<Node> graph2Nodes = graph2.getNodes();

        Comparator<Node> comparator = new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            }
        };

        Collections.sort(graph1Nodes, comparator);
        Collections.sort(graph2Nodes, comparator);

        if (graph1Nodes.size() != graph2Nodes.size()) {
            throw new IllegalArgumentException(
                    "The graph sizes are different.");
        }

        for (int i = 0; i < graph1Nodes.size(); i++) {
            String name1 = graph1Nodes.get(i).getName();
            String name2 = graph2Nodes.get(i).getName();

            if (!name1.equals(name2)) {
                throw new IllegalArgumentException(
                        "Graph names don't " + "correspond.");
            }
        }

        List<Edge> edges1 = graph1.getEdges();

        for (Edge edge1 : edges1) {
            Node node11 = edge1.getNode1();
            Node node12 = edge1.getNode2();

            Node node21 = graph2.getNode(node11.getName());
            Node node22 = graph2.getNode(node12.getName());

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null) {
                if (edge1.getEndpoint1() == Endpoint.TAIL) {
                    count++;
                }

                if (edge1.getEndpoint2() == Endpoint.TAIL) {
                    count++;
                }
            } else {
                if (edge1.getEndpoint1() == Endpoint.TAIL) {
                    if (edge2.getProximalEndpoint(node21) != Endpoint.TAIL) {
                        count++;
                    }
                }

                if (edge1.getEndpoint2() == Endpoint.TAIL) {
                    if (edge2.getProximalEndpoint(node22) != Endpoint.TAIL) {
                        count++;
                    }
                }
            }
        }

//        System.out.println("Arrowpoint errors = " + count);

        return count;
    }

    /**
     * Counts the number of unshielded colliders in graph.
     */
    private int countUnshieldedColliders(Graph graph) {
        if (graph == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        for (Node b : nodes) {
            List<Node> adjb = graph.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (isUnshieldedCollider(a, b, c, graph)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Counts the colliders that are in graph1 but not in graph2.
     */
    private static int countUnshieldedColliderErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        List<Node> nodes = new LinkedList<Node>(graph1.getNodes());

        for (Node b : nodes) {
            List<Node> adjb = graph1.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (!isUnshieldedCollider(a, b, c, graph1)) {
                    continue;
                }

                Node a2 = graph2.getNode(a.getName());
                Node b2 = graph2.getNode(b.getName());
                Node c2 = graph2.getNode(c.getName());

                if (isUnshieldedCollider(a2, b2, c2, graph2)) {
                    continue;
                }

//                System.out.println(tripleString(graph1, a, b, c)
//                     + " Est: " + tripleString(graph2, a, b, c));

                count++;
            }
        }

        return count;
    }

    private int countColliderFpErrors(Graph trueGraph, Graph estGraph,
                                      String label) {
        System.out.println("\nCollider FPs for " + label);

        if (trueGraph == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (estGraph == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        int count = 0;

        for (Node b : estGraph.getNodes()) {
            List<Node> adjb = estGraph.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (estGraph.isAdjacentTo(a, c)) {
                    continue;
                }

                if (!isUnshieldedCollider(a, b, c, estGraph)) {
                    continue;
                }

                Node a2 = trueGraph.getNode(a.getName());
                Node b2 = trueGraph.getNode(b.getName());
                Node c2 = trueGraph.getNode(c.getName());

                if (isUnshieldedCollider(a2, b2, c2, trueGraph)) {
                    continue;
                }

//                if (isUnshieldedCollider(a, b, c, estGraph)) {
//                    continue;
//                }

                System.out.println(
                        label + "--True: " + tripleString(trueGraph, a, b, c)
                                + " Est: " + tripleString(estGraph, a, b, c));
//                System.out.println("FP NC: " + new Triple(a, b, c));
                count++;
            }
        }

        return count;
    }

    /**
     * Counts the number of unshielded colliders in graph.
     */
    private int countUnshieldedNoncolliders(Graph graph) {
        if (graph == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        // The number of omission errors.
        int count = 0;

        List<Node> nodes = new LinkedList<Node>(graph.getNodes());

        for (Node b : nodes) {
            List<Node> adjb = graph.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (isUnshieldedNoncollider(a, b, c, graph)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Counts the noncolliders that are in graph1 but not in graph2.
     */
    private int countUnshieldedNoncolliderErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        int count = 0;
        List<Node> nodes = new LinkedList<Node>(graph1.getNodes());

        for (Node b : nodes) {
            List<Node> adjb = graph1.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (!isUnshieldedNoncollider(a, b, c, graph1)) {
                    continue;
                }

                Node a2 = graph2.getNode(a.getName());
                Node b2 = graph2.getNode(b.getName());
                Node c2 = graph2.getNode(c.getName());

                if (isUnshieldedNoncollider(a2, b2, c2, graph2)) {
                    continue;
                }

//                System.out.println(tripleString(graph1, a, b, c)
//                     + " Est: " + tripleString(graph2, a, b, c));
                count++;
            }
        }

        return count;
    }


    private void printNcfpInCPCButNotPC(Graph pcGraph, Graph cpcGraph,
                                        Graph pattern, PcSearch pc, Cpc cpc) {
        if (pcGraph == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (pattern == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        List<Node> cpcNodes = new LinkedList<Node>(cpcGraph.getNodes());

        for (Node b_cpc : cpcNodes) {
            List<Node> adjb = pcGraph.getAdjacentNodes(b_cpc);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a_cpc = adjb.get(choice[0]);
                Node c_cpc = adjb.get(choice[1]);

                Node a_pc = pcGraph.getNode(a_cpc.getName());
                Node b_pc = pcGraph.getNode(b_cpc.getName());
                Node c_pc = pcGraph.getNode(c_cpc.getName());

                Node a_p = pattern.getNode(a_cpc.getName());
                Node b_p = pattern.getNode(b_cpc.getName());
                Node c_p = pattern.getNode(c_cpc.getName());

                if (isUnshieldedNoncollider(a_p, b_p, c_p, pattern)
                        && !isUnshieldedNoncollider(a_pc, b_pc, c_pc, pcGraph)
                        && !isUnshieldedNoncollider(a_cpc, b_cpc, c_cpc, cpcGraph))
                {

//                    String unfaithfulString =
//                            (cpcGraph.isUnfaithful(new Triple(a_cpc, b_cpc, c_cpc)) ? " Unfaithful " : " Faithful ");
//                    System.out.println("PC: " + tripleString(pcGraph, a_pc, b_pc, c_pc) +
//                            "; CPC: " + tripleString(cpcGraph, a_cpc, b_cpc, c_cpc) + unfaithfulString +
//                            "; Pattern =" + tripleString(pattern, a_p, b_p, c_p));

//                    Set<Triple> pcColliders = pc.getUnshieldedColliders();
//
//                    for (Triple collider : pcColliders) {
//                        if (collider.getY() == b_pc) {
//                            System.out.println("PC collider: " + tripleString(pcGraph, collider.getX(), collider.getY(), collider.getZ()));
//                        }
//                    }

//                    Set<Triple> cpcColliders = pc.getUnshieldedColliders();
//
//                    for (Triple collider : cpcColliders) {
//                        if (collider.getY() == b_cpc) {
//                            System.out.println("CPC collider: " + tripleString(pcGraph, collider.getX(), collider.getY(), collider.getZ()));
//                        }
//                    }
                }
            }
        }
    }

    /**
     * Counts the noncolliders that are in trueGraph but not in estGraph.
     */
    private int countNoncolliderFpErrors(Graph trueGraph, Graph estGraph,
                                         String label) {

        System.out.println("\nNoncollider FPs for " + label);

        if (trueGraph == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (estGraph == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        int count = 0;

        for (Node b : estGraph.getNodes()) {
            List<Node> adjb = estGraph.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (estGraph.isAdjacentTo(a, c)) {
                    continue;
                }

                if (!isNoncollider(a, b, c, estGraph)) {
                    continue;
                }

                Node a2 = trueGraph.getNode(a.getName());
                Node b2 = trueGraph.getNode(b.getName());
                Node c2 = trueGraph.getNode(c.getName());

                if (isNoncollider(a2, b2, c2, trueGraph)) {
                    continue;
                }

//                if (isUnshieldedCollider(a, b, c, estGraph)) {
//                    continue;
//                }

                System.out.println(
                        label + "--True: " + tripleString(trueGraph, a, b, c)
                                + " Est: " + tripleString(estGraph, a, b, c));
                count++;
            }
        }

        return count;
    }

    public void pcGesBiasTest1() {
        TetradLogger.getInstance().clear();

        // Make graph graph1 A-->B<--C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Graph graph1 = new Dag(nodes);

        graph1.addDirectedEdge(a, b);
        graph1.addDirectedEdge(c, b);

        System.out.println("graph1: " + graph1);

        // Build a SEM model for each.
        SemPm pm1 = new SemPm(graph1);

        SemIm im1 = new SemIm(pm1);

        // Set covar matrix for model 1 to
        // 1
        // .1 1
        // 0  .1 1
        im1.setErrCovar(a, 1.0);
        im1.setErrCovar(b, 0.98);
        im1.setErrCovar(c, 1.0);

        im1.setEdgeCoef(a, b, 0.1);
        im1.setEdgeCoef(c, b, 0.1);

        System.out.println("im1 = " + im1);

        int numTrials = 0;
        int numUnshieldedColliders = 0;
        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = im1.simulateData(700, false);
            IndependenceTest test = new IndTestFisherZ(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

//            if (result.isAdjacentTo(_a, _c)) {
//                System.out.println("SHIELDED");
//            }

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                    || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result);

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            } else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            } else {
                noncollider++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Num trials = " + numTrials);
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
    }

    public void pcGesBiasTest2() {
//        LogUtils.addStandardOutHandler("tetradlog");

//        logger.setLevel(Level.FINEST);

        // Make graph A-->B-->C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Graph graph2 = new Dag(nodes);

        graph2.addDirectedEdge(a, b);
        graph2.addDirectedEdge(b, c);

        System.out.println("graph2: " + graph2);


        // Build a SEM model for each.
        SemPm pm2 = new SemPm(graph2);
        SemIm im2 = new SemIm(pm2);

        // Set covar matrix for model 2 to
        // 1
        // .1 1
        // .01 .1 1
        im2.setErrCovar(a, 1.0);
        im2.setErrCovar(b, 0.99);
        im2.setErrCovar(c, 0.99);

        im2.setEdgeCoef(a, b, 0.1);
        im2.setEdgeCoef(b, c, 0.1);

        System.out.println("im2 = " + im2);

        int numTrials = 0;
        int n = 0;
        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        while (n < 100) {
            RectangularDataSet data = im2.simulateData(700, false);
            IndependenceTest test = new IndTestFisherZ(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
//            System.out.println(result);

            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                 || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            n++;

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            }
            else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            }
            else {
                noncollider++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Num trials = " + numTrials);
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
    }

    public void pcGesBiasTest3() {
        TetradLogger.getInstance().clear();

        // Make graph graph1 A-->B-->C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Dag graph1 = new Dag(nodes);

        graph1.addDirectedEdge(a, b);
        graph1.addDirectedEdge(c, b);

        System.out.println("graph1: " + graph1);

        // Build a SEM model for each.
        BayesPm pm = new BayesPm(graph1);
        MlBayesIm im = new MlBayesIm(pm);

        im.setProbability(0, 0, 0, .1);
        im.setProbability(0, 0, 1, .9);
        im.setProbability(1, 0, 0, .6);
        im.setProbability(1, 0, 1, .4);
        im.setProbability(1, 1, 0, .4);
        im.setProbability(1, 1, 1, .6);
        im.setProbability(1, 2, 0, .6);
        im.setProbability(1, 2, 1, .4);
        im.setProbability(1, 3, 0, .8);
        im.setProbability(1, 3, 1, .2);
        im.setProbability(2, 0, 0, .1);
        im.setProbability(2, 0, 1, .9);

        System.out.println("im = " + im);

        int numTrials = 0;
        int numUnshieldedColliders = 0;

        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = im.simulateData(700, false);
            IndependenceTest test = new IndTestChiSquare(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

//            if (result.isAdjacentTo(_a, _c)) {
//                System.out.println("SHIELDED");
//            }

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                    || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result);

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            } else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            } else {
                noncollider++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Num trials = " + numTrials);
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
    }


    public void pcGesBiasTest4() {
        TetradLogger.getInstance().clear();

        // Make graph graph1 A-->B-->C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Dag graph1 = new Dag(nodes);

        graph1.addDirectedEdge(a, b);
        graph1.addDirectedEdge(b, c);

        System.out.println("graph1: " + graph1);

        // Build a SEM model for each.
        BayesPm pm = new BayesPm(graph1);
        MlBayesIm im = new MlBayesIm(pm);

//	Node: A
//
//	:	0.7899	0.2101
//
//	Node: B
//
//	A
//	0	:	0.0402	0.9598
//	1	:	0.0657	0.9343
//
//	Node: C
//
//	B
//	0	:	0.2020	0.7980
//	1	:	0.3937	0.6063

        im.setProbability(0, 0, 0, .8);
        im.setProbability(0, 0, 1, .2);
        im.setProbability(1, 0, 0, .1);
        im.setProbability(1, 0, 1, .9);
        im.setProbability(1, 1, 0, .2);
        im.setProbability(1, 1, 1, .8);
        im.setProbability(2, 0, 0, .2);
        im.setProbability(2, 0, 1, .8);
        im.setProbability(2, 1, 0, .4);
        im.setProbability(2, 1, 1, .6);

        System.out.println("im = " + im);

        int numTrials = 0;
        int numUnshieldedColliders = 0;

        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = im.simulateData(700, false);
            IndependenceTest test = new IndTestChiSquare(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

//            if (result.isAdjacentTo(_a, _c)) {
//                System.out.println("SHIELDED");
//            }

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                    || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result);

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            } else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            } else {
                noncollider++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Num trials = " + numTrials);
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
    }

    public void pcGesBiasTest5() {
        TetradLogger.getInstance().clear();

        // Make graph colliderGraph A-->B-->C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Dag colliderGraph = new Dag(nodes);

        colliderGraph.addDirectedEdge(a, b);
        colliderGraph.addDirectedEdge(c, b);

        // Build a SEM model for each.
        BayesPm colliderPm = new BayesPm(colliderGraph);
        MlBayesIm colliderIm = new MlBayesIm(colliderPm);

        colliderIm.setProbability(0, 0, 0, .1);
        colliderIm.setProbability(0, 0, 1, .9);
        colliderIm.setProbability(1, 0, 0, .6);
        colliderIm.setProbability(1, 0, 1, .4);
        colliderIm.setProbability(1, 1, 0, .4);
        colliderIm.setProbability(1, 1, 1, .6);
        colliderIm.setProbability(1, 2, 0, .6);
        colliderIm.setProbability(1, 2, 1, .4);
        colliderIm.setProbability(1, 3, 0, .8);
        colliderIm.setProbability(1, 3, 1, .2);
        colliderIm.setProbability(2, 0, 0, .1);
        colliderIm.setProbability(2, 0, 1, .9);

        System.out.println("A-->B<--C model: " + colliderIm);

        Dag noncolliderGraph = new Dag(nodes);
        noncolliderGraph.addDirectedEdge(a, b);
        noncolliderGraph.addDirectedEdge(b, c);
        BayesPm noncolliderPm = new BayesPm(noncolliderGraph);

        RectangularDataSet _data = colliderIm.simulateData(10000, false);

        MlBayesEstimator estimator = new MlBayesEstimator();
        BayesIm noncolliderIm = estimator.estimate(noncolliderPm, _data);

        System.out.println("A-->B-->C model: " + noncolliderIm);


        int numUnshieldedColliders = 0;

        int numTrials = 0;
        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        int numTrials2 = 0;
        int collider2 = 0;
        int noncollider2 = 0;
        int unfaithful2 = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = colliderIm.simulateData(700, false);
            IndependenceTest test = new IndTestChiSquare(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

//            if (result.isAdjacentTo(_a, _c)) {
//                System.out.println("SHIELDED");
//            }

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                    || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result);

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            } else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            } else {
                noncollider++;
            }
        }

        numUnshieldedColliders = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = noncolliderIm.simulateData(700, false);
            IndependenceTest test = new IndTestChiSquare(data, 0.05);

//            PcSearch search2 = new PcSearch(test, new Knowledge());
//            Graph result2 = search2.search();

//            Cpc search2 = new Cpc(test, new Knowledge());
//            Graph result2 = search2.search();

            GesSearch search2 = new GesSearch(data, null);
            Graph result2 = search2.search();
            numTrials2++;

            Node _a = result2.getNode(a.getName());
            Node _b = result2.getNode(b.getName());
            Node _c = result2.getNode(c.getName());

            if (!result2.isAdjacentTo(_a, _b) || !result2.isAdjacentTo(_b, _c)
                    || result2.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result2);

            if (result2.isDirectedFromTo(_a, _b) && result2.isDirectedFromTo(_c, _b)) {
                collider2++;
            } else if (result2.isAmbiguous(_a, _b, _c)) {
                unfaithful2++;
            } else {
                noncollider2++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Using data simulated from A-->B<--C, <A, B, C> is judged as:");
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
        System.out.println("(Num trials = " + numTrials + ")");

        System.out.println();
        System.out.println("Using data simulated from A-->B-->C, <A, B, C> is judged as:");
        System.out.println("Collider = " + collider2);
        System.out.println("Noncollider = " + noncollider2);
        System.out.println("Unfaithful = " + unfaithful2);
        System.out.println("(Num trials = " + numTrials2 + ")");
    }

    public void pcGesBiasTest6() {
        TetradLogger.getInstance().clear();

        // Make graph graph1 A-->B<--C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Graph graph1 = new Dag(nodes);

        graph1.addDirectedEdge(a, b);
        graph1.addDirectedEdge(c, b);

        System.out.println("graph1: " + graph1);

        // Build a SEM model for each.
        SemPm pm1 = new SemPm(graph1);

        SemIm im1 = new SemIm(pm1);

        // Set covar matrix for model 1 to
        // 1
        // .1 1
        // 0  .1 1
        im1.setErrCovar(a, 1.0);
        im1.setErrCovar(b, 1.00);
        im1.setErrCovar(c, 1.0);

        im1.setEdgeCoef(a, b, 0.3);
        im1.setEdgeCoef(c, b, 0.3);

        System.out.println("im1 = " + im1);

        int numTrials = 0;
        int numUnshieldedColliders = 0;
        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = im1.simulateData(700, false);
            IndependenceTest test = new IndTestFisherZ(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

//            if (result.isAdjacentTo(_a, _c)) {
//                System.out.println("SHIELDED");
//            }

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                    || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result);

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            } else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            } else {
                noncollider++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Num trials = " + numTrials);
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
    }

    public void pcGesBiasTest7() {
        TetradLogger.getInstance().clear();

        // Make graph graph1 A-->B-->C.
        Node a = new GraphNode("A");
        Node b = new GraphNode("B");
        Node c = new GraphNode("C");
        List<Node> nodes = new LinkedList<Node>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        Dag graph1 = new Dag(nodes);

        graph1.addDirectedEdge(a, b);
        graph1.addDirectedEdge(c, b);

        System.out.println("graph1: " + graph1);

        // Build a SEM model for each.
        BayesPm pm = new BayesPm(graph1);
        MlBayesIm im = new MlBayesIm(pm);

        im.setProbability(0, 0, 0, .1);
        im.setProbability(0, 0, 1, .9);
        im.setProbability(1, 0, 0, .6);
        im.setProbability(1, 0, 1, .4);
        im.setProbability(1, 1, 0, .4);
        im.setProbability(1, 1, 1, .6);
        im.setProbability(1, 2, 0, .6);
        im.setProbability(1, 2, 1, .4);
        im.setProbability(1, 3, 0, .8);
        im.setProbability(1, 3, 1, .2);
        im.setProbability(2, 0, 0, .1);
        im.setProbability(2, 0, 1, .9);

        System.out.println("im = " + im);

        int numTrials = 0;
        int numUnshieldedColliders = 0;

        int collider = 0;
        int noncollider = 0;
        int unfaithful = 0;

        while (numUnshieldedColliders < 100) {
            RectangularDataSet data = im.simulateData(700, false);
            IndependenceTest test = new IndTestChiSquare(data, 0.05);

//            PcSearch search = new PcSearch(test, new Knowledge());
//            Graph result = search.search();

//            Cpc search = new Cpc(test, new Knowledge());
//            Graph result = search.search();

            GesSearch search = new GesSearch(data, null);
            Graph result = search.search();
            numTrials++;

            Node _a = result.getNode(a.getName());
            Node _b = result.getNode(b.getName());
            Node _c = result.getNode(c.getName());

//            if (result.isAdjacentTo(_a, _c)) {
//                System.out.println("SHIELDED");
//            }

            if (!result.isAdjacentTo(_a, _b) || !result.isAdjacentTo(_b, _c)
                    || result.isAdjacentTo(_a, _c)) {
                continue;
            }

            numUnshieldedColliders++;
//            System.out.println(result);

            if (result.isDirectedFromTo(_a, _b) && result.isDirectedFromTo(_c, _b)) {
                collider++;
            } else if (result.isAmbiguous(_a, _b, _c)) {
                unfaithful++;
            } else {
                noncollider++;
            }
        }

//        System.out.println("collider = " + collider + ", noncollider = " + noncollider);

        System.out.println("Num trials = " + numTrials);
        System.out.println("Collider = " + collider);
        System.out.println("Noncollider = " + noncollider);
        System.out.println("Unfaithful = " + unfaithful);
    }

    /**
     * Counts the noncolliders that are in graph1 but not in graph2.
     */
    private int countNoncolliderFnErrors(Graph graph1, Graph graph2) {
        if (graph1 == null) {
            throw new NullPointerException("The reference graph is missing.");
        }

        if (graph2 == null) {
            throw new NullPointerException("The target graph is missing.");
        }

        int count = 0;
        List<Node> nodes = new LinkedList<Node>(graph1.getNodes());

        for (Node b : nodes) {
            List<Node> adjb = graph1.getAdjacentNodes(b);

            if (adjb.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjb.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node a = adjb.get(choice[0]);
                Node c = adjb.get(choice[1]);

                if (!isUnshieldedNoncollider(a, b, c, graph1)) {
                    continue;
                }

                Node a2 = graph2.getNode(a.getName());
                Node b2 = graph2.getNode(b.getName());
                Node c2 = graph2.getNode(c.getName());

                if (isUnshieldedNoncollider(a2, b2, c2, graph2)) {
                    continue;
                }

                count++;
            }
        }

        return count;
    }

    private static boolean isUnshieldedCollider(Node a, Node b, Node c, Graph graph) {
        if (!graph.isAdjacentTo(a, b)) {
            return false;
        }

        if (!graph.isAdjacentTo(c, b)) {
            return false;
        }

        if (graph.isAdjacentTo(a, c)) {
            return false;
        }

//        if (graph.isUnfaithful(new Triple(a, b, c))) {
//            return false;
//        }

        return graph.getEndpoint(a, b) == Endpoint.ARROW &&
                graph.getEndpoint(c, b) == Endpoint.ARROW;
    }

    private boolean isUnshieldedNoncollider(Node a, Node b, Node c,
                                            Graph graph) {
        if (graph.isAdjacentTo(a, c)) {
            return false;
        }

        if (!graph.isAdjacentTo(a, b)) {
            return false;
        }

        if (!graph.isAdjacentTo(c, b)) {
            return false;
        }

        if (graph.isAmbiguous(a, b, c)) {
            return false;
        }

        if (graph.getEndpoint(a, b) == Endpoint.ARROW &&
                graph.getEndpoint(c, b) == Endpoint.ARROW) {
            return false;
        }

        return true;
    }

    private boolean isNoncollider(Node a, Node b, Node c, Graph graph) {
        if (graph.isAmbiguous(a, b, c)) {
            boolean directedIn =
                    graph.isDirectedFromTo(a, b) &&
                            graph.isDirectedFromTo(c, b);
            boolean directedOut =
                    graph.isDirectedFromTo(b, a) &&
                            graph.isDirectedFromTo(b, c);

            if (!(directedIn || directedOut)) {
                return false;
            }
        }

//        if (graph.isUnfaithful(new Triple(a, b, c))) {
//            return false;
//        }

        if (!graph.isAdjacentTo(a, b)) {
            return false;
        }

        if (!graph.isAdjacentTo(c, b)) {
            return false;
        }

//        if (graph.isAdjacentTo(a, c)) {
//            return false;
//        }

        if (graph.getEndpoint(a, b) == Endpoint.ARROW &&
                graph.getEndpoint(c, b) == Endpoint.ARROW) {
            return false;
        }

        return true;
    }


    /**
     * Completes a pattern that was modified by an insertion/deletion operator
     * Based on the algorithm described on Appendix C of (Chickering, 2002).
     */
    private void rebuildPattern(Graph graph) {
        basicPattern(graph);
//        addRequiredEdges(graph);
        MeekRules rules = new MeekRules();
        rules.orientImplied(graph);
    }

    /**
     * Get a graph and direct only the unshielded colliders.
     */
    public void basicPattern(Graph graph) {
        List<Edge> edges = graph.getEdges();
        Set<Edge> directedEdges = new HashSet<Edge>();

        EDGE:
        for (Edge edge : edges) {
            Node head = null, tail = null;

            if (edge.getEndpoint1() == Endpoint.ARROW &&
                    edge.getEndpoint2() == Endpoint.TAIL) {
                head = edge.getNode1();
                tail = edge.getNode2();
            } else if (edge.getEndpoint2() == Endpoint.ARROW &&
                    edge.getEndpoint1() == Endpoint.TAIL) {
                head = edge.getNode2();
                tail = edge.getNode1();
            }

            if (head != null) {
                for (Node node : graph.getParents(head)) {
                    if (node != tail && !graph.isAdjacentTo(tail, node)) {
                        directedEdges.add(edge);
                        continue EDGE;
                    }
                }
            }
        }

        for (Edge edge : edges) {
            if (!directedEdges.contains(edge)) {
                graph.removeEdge(edge);
                graph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }
    }

    private String tripleString(Graph graph,
                                Node a, Node b, Node c) {
        Node trueA = graph.getNode(a.getName());
        Node trueB = graph.getNode(b.getName());
        Node trueC = graph.getNode(c.getName());

//        List<Node> badj = new LinkedList<Node>();
//        for (Node node : graph.getAdjacentNodes(trueA)) {
//            badj.add(graph.getNode(node.toString()));
//        }

//        List<Node> cadj = new LinkedList<Node>();
//        for (Node node : graph.getAdjacentNodes(trueC)) {
//            cadj.add(graph.getNode(node.toString()));
//        }

//        List<Node> adj = new LinkedList<Node>();
//        adj.addAll(badj);
//        adj.addAll(cadj);

//        List<Node> badj = graph.getAdjacentNodes(a);
//        List<Node> cadj = graph.getAdjacentNodes(c);

        StringBuffer triple = new StringBuffer();

        triple.append(a);

//        if (!graph.isAdjacentTo(trueB, trueA) || !graph.isAdjacentTo(trueB, trueC)) {
//            return;
//        }

        if (graph.isAdjacentTo(trueA, trueB)) {
            Endpoint endpointA = graph.getEndpoint(trueB, trueA);
            Endpoint endpointB = graph.getEndpoint(trueA, trueB);

            if (endpointA.equals(Endpoint.ARROW)) {
                triple.append("<");
            } else if (endpointA.equals(Endpoint.TAIL)) {
                triple.append("-");
            }

            triple.append("-");

            if (endpointB.equals(Endpoint.ARROW)) {
                triple.append(">");
            } else if (endpointB.equals(Endpoint.TAIL)) {
                triple.append("-");
            }
        } else {
            triple.append("   ");
        }

        triple.append(b);

        if (graph.isAdjacentTo(trueB, trueC)) {
            Endpoint endpointB = graph.getEndpoint(trueC, trueB);
            Endpoint endpointC = graph.getEndpoint(trueB, trueC);

            if (endpointB.equals(Endpoint.ARROW)) {
                triple.append("<");
            } else if (endpointB.equals(Endpoint.TAIL)) {
                triple.append("-");
            }

            triple.append("-");

            if (endpointC.equals(Endpoint.ARROW)) {
                triple.append(">");
            } else if (endpointC.equals(Endpoint.TAIL)) {
                triple.append("-");
            }
        } else {
            triple.append("   ");
        }

        triple.append(c);

        if (graph.isAdjacentTo(trueA, trueC)) {
            triple.append(" (shielded) ");
        } else {
            triple.append(" (unshielded) ");
        }

        return triple.toString();
    }

    public static void main(String[] args) {
        new ExploreCpcReport().generateUaiReport();
    }
}


