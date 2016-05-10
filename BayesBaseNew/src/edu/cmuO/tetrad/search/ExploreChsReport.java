package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Oct 14, 2006 Time: 11:16:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExploreChsReport {

    //===========================PUBLIC METHODS==========================//

    public static void main(String[] args) {
        new ExploreChsReport().constructData11();
    }

    //===========================PRIVATE METHODS=========================//

    private void constructData1() {

        RectangularDataSet combined = makeCombinedContinuous();

        // Add the defined variables.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);
        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);
//        addG1(combined);
//        addG1a(combined);
        addG1b(combined);
        addG2(combined);
        addG3(combined);

        // Add the scores.
        addScores(combined);

//        addGalantamineIds(combined);

        writeData(combined, "test_data/combined.txt");

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("CBS");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");
        intermediateVars.add("RMBPC");
        intermediateVars.add("RMBPC-B");
        intermediateVars.add("RMBPC-M");
        intermediateVars.add("RMBPC-D");
        intermediateVars.add("IADL");
        intermediateVars.add("PSMS");
        intermediateVars.add("Memantine");
        intermediateVars.add("Antidepressants");
        intermediateVars.add("Anxiolytics");
        intermediateVars.add("Antipsychotics");
        intermediateVars.add("Sedatives");
//        intermediateVars.add("G1-0");
        intermediateVars.add("G1");
//        intermediateVars.add("RXRYTO");
//        intermediateVars.add("RXEXTO");
//        intermediateVars.add("RXARTO");
//        intermediateVars.add("DELIVE");
        intermediateVars.add("G2");
        intermediateVars.add("G3");

//        intermediateVars.add("ID");

        RectangularDataSet intermediate = subset(combined, intermediateVars);
        writeData(intermediate, "test_data/intermediate.txt");

        RectangularDataSet g1Set =
                rowSubset(intermediate, "G1", new double[]{0.0, 1.0});
        g1Set.removeColumn(g1Set.getVariable("G2"));
        g1Set.removeColumn(g1Set.getVariable("G3"));
//        g1Set = new MeanInterpolator().filter(g1Set);
        writeData(g1Set, "test_data/g1set.txt");

        RectangularDataSet g2Set =
                rowSubset(intermediate, "G2", new double[]{0.0, 1.0});
        g2Set.removeColumn(g2Set.getVariable("G1"));
        g2Set.removeColumn(g2Set.getVariable("G3"));
//        g2Set = new MeanInterpolator().filter(g2Set);
        writeData(g2Set, "test_data/g2set.txt");

        RectangularDataSet g3Set =
                rowSubset(intermediate, "G3", new double[]{0.0, 1.0});
        g3Set.removeColumn(g3Set.getVariable("G1"));
        g3Set.removeColumn(g3Set.getVariable("G2"));
//        g3Set = new MeanInterpolator().filter(g3Set);
        writeData(g3Set, "test_data/g3set.txt");
    }

    private void constructData1a() {
        RectangularDataSet combined = load2("test_data/combined.txt");

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("CBS");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");
        intermediateVars.add("RMBPC");
        intermediateVars.add("RMBPC-B");
        intermediateVars.add("RMBPC-M");
        intermediateVars.add("RMBPC-D");
        intermediateVars.add("IADL");
        intermediateVars.add("PSMS");
        intermediateVars.add("Memantine");
        intermediateVars.add("Antidepressants");
        intermediateVars.add("Anxiolytics");
        intermediateVars.add("Antipsychotics");
        intermediateVars.add("Sedatives");
        intermediateVars.add("G1-0");
        intermediateVars.add("G1");
        intermediateVars.add("RXRYTO");
        intermediateVars.add("RXEXTO");
        intermediateVars.add("RXARTO");
        intermediateVars.add("DELIVE");
//        intermediateVars.add("G2");
//        intermediateVars.add("G3");
//
//        intermediateVars.add("ID");

        RectangularDataSet intermediate = subset(combined, intermediateVars);
        writeData(intermediate, "test_data/intermediate.txt");

        RectangularDataSet g1Set =
                rowSubset(intermediate, "G1", new double[]{0.0, 1.0});
//        g1Set.removeColumn(g1Set.getVariable("G2"));
//        g1Set.removeColumn(g1Set.getVariable("G3"));
//        g1Set = new MeanInterpolator().filter(g1Set);
        writeData(g1Set, "test_data/g1set.txt");

//        RectangularDataSet g2Set =
//                rowSubset(intermediate, "G2", new double[]{0.0, 1.0});
//        g2Set.removeColumn(g2Set.getVariable("G1"));
//        g2Set.removeColumn(g2Set.getVariable("G3"));
////        g2Set = new MeanInterpolator().filter(g2Set);
//        writeData(g2Set, "test_data/g2set.txt");
//
//        RectangularDataSet g3Set =
//                rowSubset(intermediate, "G3", new double[]{0.0, 1.0});
//        g3Set.removeColumn(g3Set.getVariable("G1"));
//        g3Set.removeColumn(g3Set.getVariable("G2"));
////        g3Set = new MeanInterpolator().filter(g3Set);
//        writeData(g3Set, "test_data/g3set.txt");
    }

    private void constructData2() {

//        RectangularDataSet combined = makeCombinedContinuous();
        RectangularDataSet combined = readCombinedContinuous();

        // Add the defined variables.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC-0");
        copyAs(combined, "DSFMEM", "RMBPC-M-0");
        copyAs(combined, "DSFBEH", "RMBPC-B-0");
        copyAs(combined, "DSFDEP", "RMBPC-D-0");

        copyAs(combined, "QLIADL", "IADL-0");
        copyAs(combined, "QLPSMS", "PSMS-0");
        copyAs(combined, "BURDEN", "CBS-0");

        copyAs(combined, "RXADEP", "Antidepressants-0");
        copyAs(combined, "RXANX", "Anxiolytics-0");
        copyAs(combined, "RXAPSY", "Antipsychotics-0");
        copyAs(combined, "RXSED", "Sedatives-0");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);
        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);
//        addG1(combined);
//        addG1a(combined);
        addG1b(combined);
        addG2(combined);
        addG3(combined);
        addG4(combined);

        // Add the scores.
        addScores(combined);

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");

//        intermediateVars.add("RMBPC-0");
        intermediateVars.add("RMBPC");

//        intermediateVars.add("RMBPC-B-0");
        intermediateVars.add("RMBPC-B");

//        intermediateVars.add("RMBPC-M-0");
        intermediateVars.add("RMBPC-M");

//        intermediateVars.add("DSSAMF");
//        intermediateVars.add("DSRCTF");
//        intermediateVars.add("DSPSTF");
//        intermediateVars.add("DSFGTF");
//        intermediateVars.add("DSSTRF");
//        intermediateVars.add("DSCONF");

//        intermediateVars.add("RMBPC-D-0");
        intermediateVars.add("RMBPC-D");

//        intermediateVars.add("IADL-0");
        intermediateVars.add("IADL");

//        intermediateVars.add("PSMS-0");
        intermediateVars.add("PSMS");

//        intermediateVars.add("CBS-0");
        intermediateVars.add("CBS");

        intermediateVars.add("Memantine");

//        intermediateVars.add("Antidepressants-0");
        intermediateVars.add("Antidepressants");

//        intermediateVars.add("Anxiolytics-0");
        intermediateVars.add("Anxiolytics");

//        intermediateVars.add("Antipsychotics-0");
        intermediateVars.add("Antipsychotics");

//        intermediateVars.add("Sedatives-0");
        intermediateVars.add("Sedatives");

        intermediateVars.add("G1");
        intermediateVars.add("G2");
        intermediateVars.add("G3");
        intermediateVars.add("G4");

//        intermediateVars.add("RXRYTO");
//        intermediateVars.add("RXEXTO");
//        intermediateVars.add("RXARTO");
//        intermediateVars.add("RXNATO");
//        intermediateVars.add("DELIVE");

        writeData(combined, "test_data/combined.txt");

        RectangularDataSet intermediate = subset(combined, intermediateVars);
        writeData(intermediate, "test_data/intermediate.txt");

        RectangularDataSet g1Set =
                rowSubset(intermediate, "G1", new double[]{0.0, 1.0});
        g1Set.removeColumn(g1Set.getVariable("G2"));
        g1Set.removeColumn(g1Set.getVariable("G3"));
        g1Set.removeColumn(g1Set.getVariable("G4"));

//        doRegressionImputation(g1Set);

        writeData(g1Set, "test_data/g1set.txt");

        RectangularDataSet g2Set =
                rowSubset(intermediate, "G2", new double[]{0.0, 1.0});
        g2Set.removeColumn(g2Set.getVariable("G1"));
        g2Set.removeColumn(g2Set.getVariable("G3"));
        g2Set.removeColumn(g2Set.getVariable("G4"));
        writeData(g2Set, "test_data/g2set.txt");

        RectangularDataSet g3Set =
                rowSubset(intermediate, "G3", new double[]{0.0, 1.0});
        g3Set.removeColumn(g3Set.getVariable("G1"));
        g3Set.removeColumn(g3Set.getVariable("G2"));
        g3Set.removeColumn(g3Set.getVariable("G4"));
        writeData(g3Set, "test_data/g3set.txt");

        RectangularDataSet g4Set =
                rowSubset(intermediate, "G4", new double[]{0.0, 1.0});
        g4Set.removeColumn(g4Set.getVariable("G1"));
        g4Set.removeColumn(g4Set.getVariable("G2"));
        g4Set.removeColumn(g4Set.getVariable("G3"));
        writeData(g4Set, "test_data/g4set.txt");
    }

    private void constructData3() {

//        RectangularDataSet combined = makeCombinedContinuous();
        RectangularDataSet combined = readCombinedContinuous();

        // Add the defined variables.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC-0");
        copyAs(combined, "DSFMEM", "RMBPC-M-0");
        copyAs(combined, "DSFBEH", "RMBPC-B-0");
        copyAs(combined, "DSFDEP", "RMBPC-D-0");

        copyAs(combined, "QLIADL", "IADL-0");
        copyAs(combined, "QLPSMS", "PSMS-0");
        copyAs(combined, "BURDEN", "CBS-0");

        copyAs(combined, "RXADEP", "Antidepressants-0");
        copyAs(combined, "RXANX", "Anxiolytics-0");
        copyAs(combined, "RXAPSY", "Antipsychotics-0");
        copyAs(combined, "RXSED", "Sedatives-0");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);
        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);
//        addG1(combined);
//        addG1a(combined);
        addG1b(combined);
        addG2(combined);
        addG3(combined);
        addG4(combined);

        // Add the scores.
        addScores(combined);

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");

        intermediateVars.add("RMBPC-0");
//        intermediateVars.add("RMBPC");

        intermediateVars.add("RMBPC-B-0");
//        intermediateVars.add("RMBPC-B");

        intermediateVars.add("RMBPC-M-0");
//        intermediateVars.add("RMBPC-M");

//        intermediateVars.add("DSSAMF");
//        intermediateVars.add("DSRCTF");
//        intermediateVars.add("DSPSTF");
//        intermediateVars.add("DSFGTF");
//        intermediateVars.add("DSSTRF");
//        intermediateVars.add("DSCONF");

        intermediateVars.add("RMBPC-D-0");
//        intermediateVars.add("RMBPC-D");

//        intermediateVars.add("IADL-0");
        intermediateVars.add("IADL");

//        intermediateVars.add("PSMS-0");
        intermediateVars.add("PSMS");

//        intermediateVars.add("CBS-0");
        intermediateVars.add("CBS");

        intermediateVars.add("Memantine");

//        intermediateVars.add("Antidepressants-0");
        intermediateVars.add("Antidepressants");

//        intermediateVars.add("Anxiolytics-0");
        intermediateVars.add("Anxiolytics");

//        intermediateVars.add("Antipsychotics-0");
        intermediateVars.add("Antipsychotics");

//        intermediateVars.add("Sedatives-0");
        intermediateVars.add("Sedatives");

        intermediateVars.add("G1");
        intermediateVars.add("G2");
        intermediateVars.add("G3");
        intermediateVars.add("G4");

//        intermediateVars.add("RXRYTO");
//        intermediateVars.add("RXEXTO");
//        intermediateVars.add("RXARTO");
//        intermediateVars.add("RXNATO");
//        intermediateVars.add("DELIVE");

        writeData(combined, "test_data/combined.txt");

        RectangularDataSet intermediate = subset(combined, intermediateVars);
        writeData(intermediate, "test_data/intermediate.txt");

        RectangularDataSet g1Set =
                rowSubset(intermediate, "G1", new double[]{0.0, 1.0});
        g1Set.removeColumn(g1Set.getVariable("G2"));
        g1Set.removeColumn(g1Set.getVariable("G3"));
        g1Set.removeColumn(g1Set.getVariable("G4"));

//        doRegressionImputation(g1Set);

        writeData(g1Set, "test_data/g1set.txt");

        RectangularDataSet g2Set =
                rowSubset(intermediate, "G2", new double[]{0.0, 1.0});
        g2Set.removeColumn(g2Set.getVariable("G1"));
        g2Set.removeColumn(g2Set.getVariable("G3"));
        g2Set.removeColumn(g2Set.getVariable("G4"));
        writeData(g2Set, "test_data/g2set.txt");

        RectangularDataSet g3Set =
                rowSubset(intermediate, "G3", new double[]{0.0, 1.0});
        g3Set.removeColumn(g3Set.getVariable("G1"));
        g3Set.removeColumn(g3Set.getVariable("G2"));
        g3Set.removeColumn(g3Set.getVariable("G4"));
        writeData(g3Set, "test_data/g3set.txt");

        RectangularDataSet g4Set =
                rowSubset(intermediate, "G4", new double[]{0.0, 1.0});
        g4Set.removeColumn(g4Set.getVariable("G1"));
        g4Set.removeColumn(g4Set.getVariable("G2"));
        g4Set.removeColumn(g4Set.getVariable("G3"));
        writeData(g4Set, "test_data/g4set.txt");
    }


    public void constructData4() {

        // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
        w14Intersection = filterData(w14Intersection);
//        writeData(w14Intersection, "test_data/w14_filtered.txt");

        RectangularDataSet w15 = loadData("test_data/ad15.txt");
        RectangularDataSet w15_filtered = filterData(w15);
        writeData(w15_filtered, "test_data/w15_filtered.txt");

        RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);
        writeData(w15Intersection, "test_data/w15_sorted.txt");

        w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
        w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

        for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w14Intersection.getVariable(j);
            variable.setName(variable.getName() + "-14");
        }

        writeData(w14Intersection, "test_data/w14Intersection.txt");

        for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w15Intersection.getVariable(j);
            variable.setName(variable.getName() + "-15");
        }

        writeData(w15Intersection, "test_data/w15Intersection.txt");

        RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);

        writeData(combined, "test_data/combined.txt");

    }

    public void constructData5() {

        // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
        w14Intersection = filterData(w14Intersection);
//        writeData(w14Intersection, "test_data/w14_filtered.txt");

        RectangularDataSet w15 = loadData("test_data/ad15.txt");
        RectangularDataSet w15_filtered = filterData(w15);
        writeData(w15_filtered, "test_data/w15_filtered.txt");

        RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);
        writeData(w15Intersection, "test_data/w15_sorted.txt");

        w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
        w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

        for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w14Intersection.getVariable(j);
            variable.setName(variable.getName() + "-14");
        }

//        writeData(w14Intersection, "test_data/w14Intersection.txt");

        for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w15Intersection.getVariable(j);
            variable.setName(variable.getName() + "-15");
        }

//        writeData(w15Intersection, "test_data/w15Intersection.txt");

        RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);

        RectangularDataSet subset1 = rowSubset(combined, "G1-14", new double[]{1, 0});
        RectangularDataSet subset2 = rowSubset(subset1, "G1-15", new double[]{1, 0});

        writeData(subset2, "test_data/combined_subset.txt");

    }

    public RectangularDataSet filterData(RectangularDataSet combined) {
        // Add the defined variables.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC-0");
        copyAs(combined, "DSFMEM", "RMBPC-M-0");
        copyAs(combined, "DSFBEH", "RMBPC-B-0");
        copyAs(combined, "DSFDEP", "RMBPC-D-0");

        copyAs(combined, "QLIADL", "IADL-0");
        copyAs(combined, "QLPSMS", "PSMS-0");
        copyAs(combined, "BURDEN", "CBS-0");
        copyAs(combined, "PCOHWKQ", "OFCT");

//        copyAs(combined, "RXADEP", "Antidepressants-0");
//        copyAs(combined, "RXANX", "Anxiolytics-0");
//        copyAs(combined, "RXAPSY", "Antipsychotics-0");
//        copyAs(combined, "RXSED", "Sedatives-0");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);
        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);
        addXcareDays(combined);
//        addG1(combined);
        addG1a(combined);
//        addG1b(combined);

        // Add the scores.
        addScores(combined);

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");

        intermediateVars.add("RMBPC-0");
//        intermediateVars.add("RMBPC");

        intermediateVars.add("RMBPC-B-0");
//        intermediateVars.add("RMBPC-B");

        intermediateVars.add("RMBPC-M-0");
//        intermediateVars.add("RMBPC-M");

//        intermediateVars.add("DSSAMF");
//        intermediateVars.add("DSRCTF");
//        intermediateVars.add("DSPSTF");
//        intermediateVars.add("DSFGTF");
//        intermediateVars.add("DSSTRF");
//        intermediateVars.add("DSCONF");

        intermediateVars.add("RMBPC-D-0");
//        intermediateVars.add("RMBPC-D");

//        intermediateVars.add("IADL-0");
        intermediateVars.add("IADL");

//        intermediateVars.add("PSMS-0");
        intermediateVars.add("PSMS");

//        intermediateVars.add("CBS-0");
        intermediateVars.add("CBS");

        intermediateVars.add("Memantine");

//        intermediateVars.add("Antidepressants-0");
        intermediateVars.add("Antidepressants");

//        intermediateVars.add("Anxiolytics-0");
        intermediateVars.add("Anxiolytics");

//        intermediateVars.add("Antipsychotics-0");
        intermediateVars.add("Antipsychotics");

//        intermediateVars.add("Sedatives-0");
        intermediateVars.add("Sedatives");

        intermediateVars.add("G1");

        intermediateVars.add("ID");

//        intermediateVars.add("PCOHWKQ");
        intermediateVars.add("OFCT");
//        intermediateVars.add("SSDYCQ");
//        intermediateVars.add("SSRSCQ");
//        intermediateVars.add("SSHACQ");
//        intermediateVars.add("XCareDays");

//        intermediateVars.add("RXRYTO");
//        intermediateVars.add("RXEXTO");
//        intermediateVars.add("RXARTO");
//        intermediateVars.add("RXNATO");
//        intermediateVars.add("DELIVE");

        return subset(combined, intermediateVars);
    }

    public void constructData6() {

        // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
        w14Intersection = filterData2(w14Intersection);
//        writeData(w14Intersection, "test_data/w14_filtered.txt");

        RectangularDataSet w15 = loadData("test_data/ad15.txt");
        RectangularDataSet w15_filtered = filterData2(w15);
//        writeData(w15_filtered, "test_data/w15_filtered.txt");

        RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);
//        writeData(w15Intersection, "test_data/w15_sorted.txt");

        w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
        w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

        for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w14Intersection.getVariable(j);
            variable.setName(variable.getName() + "-14");
        }

        writeData(w14Intersection, "test_data/w14Intersection.txt");

        for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w15Intersection.getVariable(j);
            variable.setName(variable.getName() + "-15");
        }

        writeData(w15Intersection, "test_data/w15Intersection.txt");

        RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);

        writeData(combined, "test_data/combinedG5.txt");
    }

    public RectangularDataSet filterData2(RectangularDataSet combined) {
        // Add the defined variables.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC-0");
        copyAs(combined, "DSFMEM", "RMBPC-M-0");
        copyAs(combined, "DSFBEH", "RMBPC-B-0");
        copyAs(combined, "DSFDEP", "RMBPC-D-0");

        copyAs(combined, "QLIADL", "IADL-0");
        copyAs(combined, "QLPSMS", "PSMS-0");
        copyAs(combined, "BURDEN", "CBS-0");
        copyAs(combined, "PCOHWKQ", "OFCT");

//        copyAs(combined, "RXADEP", "Antidepressants-0");
//        copyAs(combined, "RXANX", "Anxiolytics-0");
//        copyAs(combined, "RXAPSY", "Antipsychotics-0");
//        copyAs(combined, "RXSED", "Sedatives-0");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);
        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);
        addXcareDays(combined);
//        addG1(combined);
//        addG1a(combined);
//        addG1b(combined);
        addG5(combined);

        // Add the scores.
        addScores(combined);

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");

        intermediateVars.add("RMBPC-0");
//        intermediateVars.add("RMBPC");

        intermediateVars.add("RMBPC-B-0");
//        intermediateVars.add("RMBPC-B");

        intermediateVars.add("RMBPC-M-0");
//        intermediateVars.add("RMBPC-M");

//        intermediateVars.add("DSSAMF");
//        intermediateVars.add("DSRCTF");
//        intermediateVars.add("DSPSTF");
//        intermediateVars.add("DSFGTF");
//        intermediateVars.add("DSSTRF");
//        intermediateVars.add("DSCONF");

        intermediateVars.add("RMBPC-D-0");
//        intermediateVars.add("RMBPC-D");

//        intermediateVars.add("IADL-0");
        intermediateVars.add("IADL");

//        intermediateVars.add("PSMS-0");
        intermediateVars.add("PSMS");

//        intermediateVars.add("CBS-0");
        intermediateVars.add("CBS");

        intermediateVars.add("Memantine");

//        intermediateVars.add("Antidepressants-0");
        intermediateVars.add("Antidepressants");

//        intermediateVars.add("Anxiolytics-0");
        intermediateVars.add("Anxiolytics");

//        intermediateVars.add("Antipsychotics-0");
        intermediateVars.add("Antipsychotics");

//        intermediateVars.add("Sedatives-0");
        intermediateVars.add("Sedatives");

        intermediateVars.add("G5");

        intermediateVars.add("ID");

//        intermediateVars.add("PCOHWKQ");
        intermediateVars.add("OFCT");
//        intermediateVars.add("SSDYCQ");
//        intermediateVars.add("SSRSCQ");
//        intermediateVars.add("SSHACQ");
        intermediateVars.add("XCareDays");

//        intermediateVars.add("RXRYTO");
//        intermediateVars.add("RXEXTO");
//        intermediateVars.add("RXARTO");
//        intermediateVars.add("RXNATO");
//        intermediateVars.add("DELIVE");

        return subset(combined, intermediateVars);
    }

    public void constructData7() {

        // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
        w14Intersection = filterData3(w14Intersection);
//        writeData(w14Intersection, "test_data/w14_filtered.txt");

        RectangularDataSet w15 = loadData("test_data/ad15.txt");
        RectangularDataSet w15_filtered = filterData3(w15);
//        writeData(w15_filtered, "test_data/w15_filtered.txt");

        RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);
//        writeData(w15Intersection, "test_data/w15_sorted.txt");

        w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
        w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

        for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w14Intersection.getVariable(j);
            variable.setName(variable.getName() + "-14");
        }

        writeData(w14Intersection, "test_data/w14Intersection.txt");

        for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w15Intersection.getVariable(j);
            variable.setName(variable.getName() + "-15");
        }

        writeData(w15Intersection, "test_data/w15Intersection.txt");

        RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);

        RectangularDataSet subset1 = rowSubset(combined, "G6-14", new double[]{1, 0});
        RectangularDataSet subset2 = rowSubset(subset1, "G6-15", new double[]{1, 0});

        writeData(subset2, "test_data/combinedG6.txt");
    }

    public RectangularDataSet filterData3(RectangularDataSet combined) {
        // Add the defined variables.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC-0");
        copyAs(combined, "DSFMEM", "RMBPC-M-0");
        copyAs(combined, "DSFBEH", "RMBPC-B-0");
        copyAs(combined, "DSFDEP", "RMBPC-D-0");

        copyAs(combined, "QLIADL", "IADL-0");
        copyAs(combined, "QLPSMS", "PSMS-0");
        copyAs(combined, "BURDEN", "CBS-0");
        copyAs(combined, "PCOHWKQ", "OFCT");

//        copyAs(combined, "RXADEP", "Antidepressants-0");
//        copyAs(combined, "RXANX", "Anxiolytics-0");
//        copyAs(combined, "RXAPSY", "Antipsychotics-0");
//        copyAs(combined, "RXSED", "Sedatives-0");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);
        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);
        addXcareDays(combined);
        addG6(combined);

        // Add the scores.
        addScores(combined);

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("CT");
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_spouse");

        intermediateVars.add("RMBPC-0");
//        intermediateVars.add("RMBPC");

        intermediateVars.add("RMBPC-B-0");
//        intermediateVars.add("RMBPC-B");

        intermediateVars.add("RMBPC-M-0");
//        intermediateVars.add("RMBPC-M");

//        intermediateVars.add("DSSAMF");
//        intermediateVars.add("DSRCTF");
//        intermediateVars.add("DSPSTF");
//        intermediateVars.add("DSFGTF");
//        intermediateVars.add("DSSTRF");
//        intermediateVars.add("DSCONF");

        intermediateVars.add("RMBPC-D-0");
//        intermediateVars.add("RMBPC-D");

//        intermediateVars.add("IADL-0");
        intermediateVars.add("IADL");

//        intermediateVars.add("PSMS-0");
        intermediateVars.add("PSMS");

//        intermediateVars.add("CBS-0");
        intermediateVars.add("CBS");

        intermediateVars.add("Memantine");

//        intermediateVars.add("Antidepressants-0");
        intermediateVars.add("Antidepressants");

//        intermediateVars.add("Anxiolytics-0");
        intermediateVars.add("Anxiolytics");

//        intermediateVars.add("Antipsychotics-0");
        intermediateVars.add("Antipsychotics");

//        intermediateVars.add("Sedatives-0");
        intermediateVars.add("Sedatives");

        intermediateVars.add("G6");

        intermediateVars.add("ID");

//        intermediateVars.add("PCOHWKQ");
        intermediateVars.add("OFCT");
//        intermediateVars.add("SSDYCQ");
//        intermediateVars.add("SSRSCQ");
//        intermediateVars.add("SSHACQ");
        intermediateVars.add("XCareDays");

//        intermediateVars.add("RXRYTO");
//        intermediateVars.add("RXEXTO");
//        intermediateVars.add("RXARTO");
//        intermediateVars.add("RXNATO");
//        intermediateVars.add("DELIVE");

        return subset(combined, intermediateVars);
    }

    public void constructData8() {

        String varName = "G3";

        // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
        w14Intersection = filterData8(w14Intersection, varName);
//        writeData(w14Intersection, "test_data/w14_filtered.txt");

        RectangularDataSet w15 = loadData("test_data/ad15.txt");
        RectangularDataSet w15_filtered = filterData8(w15, varName);
//        writeData(w15_filtered, "test_data/w15_filtered.txt");

        RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);
//        writeData(w15Intersection, "test_data/w15_sorted.txt");

        w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
        w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

        for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w14Intersection.getVariable(j);
            variable.setName(variable.getName() + "-14");
        }

        writeData(w14Intersection, "test_data/w14Intersection.txt");

        for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w15Intersection.getVariable(j);
            variable.setName(variable.getName() + "-15");
        }

        writeData(w15Intersection, "test_data/w15Intersection.txt");

        RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);

        RectangularDataSet subset1 = rowSubset(combined, varName + "-14", new double[]{1, 0});
        RectangularDataSet subset2 = rowSubset(subset1, varName + "-15", new double[]{1, 0});

        writeData(subset2, "test_data/combined" + varName + ".txt");
    }

    public RectangularDataSet filterData8(RectangularDataSet combined, String varName) {

        // Rename the variable that are already in the dataset.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC");
        copyAs(combined, "DSFMEM", "RMBPC-M");
        copyAs(combined, "DSFBEH", "RMBPC-B");
        copyAs(combined, "DSFDEP", "RMBPC-D");

        copyAs(combined, "QLIADL", "IADL");
        copyAs(combined, "QLPSMS", "PSMS");
        copyAs(combined, "BURDEN", "CBS");
        copyAs(combined, "PCOHWKQ", "OFCT");

//        copyAs(combined, "RXNATO", "Memantine");
//        copyAs(combined, "RXADEP", "Antidepressants");
//        copyAs(combined, "RXAPSY", "Antipsychotics");
//        copyAs(combined, "RXANX", "Anxiolytics");
//        copyAs(combined, "RXSED", "Sedatives");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);

        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);

        addBehDrugs(combined);
        addXcareDays(combined);
        addRaceVars(combined);

        if ("G1".equals(varName)) {
            addG1(combined);
        } else if ("G1a".equals(varName)) {
            addG1a(combined);
        } else if ("G1b".equals(varName)) {
            addG1b(combined);
        } else if ("G2".equals(varName)) {
            addG2(combined);
        } else if ("G3".equals(varName)) {
            addG3(combined);
        } else if ("G4".equals(varName)) {
            addG4(combined);
        } else if ("G5".equals(varName)) {
            addG5(combined);
        } else if ("G6".equals(varName)) {
            addG6(combined);
        } else if ("G6b".equals(varName)) {
            addG6b(combined);
        } else if ("G7".equals(varName)) {
            addG7(combined);
        } else if ("G7b".equals(varName)) {
            addG7b(combined);
        }

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("ID");
        intermediateVars.add(varName);
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_spouse");
        intermediateVars.add("BehDrugs");
        intermediateVars.add("RMBPC");
        intermediateVars.add("IADL");
        intermediateVars.add("CBS");
        intermediateVars.add("PSMS");
        intermediateVars.add("XcareDays");
        intermediateVars.add("OFCT");
        intermediateVars.add("CT");
        intermediateVars.add("Cauc");
        intermediateVars.add("Black");
        intermediateVars.add("Hispanic");
        intermediateVars.add("Asian");

        return subset(combined, intermediateVars);
    }

    public void constructData9() {

        String varName = "G5";

       // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        System.out.println("Full wave 14 data " + w14.getNumRows());

        
        RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
        System.out.println("Subjects occurring in both waves = " + w14Intersection.getNumRows());

        w14Intersection = filterData9(w14Intersection, varName);
//        writeData(w14Intersection, "test_data/w14_filtered.txt");

        RectangularDataSet w15 = loadData("test_data/ad15.txt");
        System.out.println("Full wave 15 data " + w15.getNumRows());

        RectangularDataSet w15_filtered = filterData9(w15, varName);
//        writeData(w15_filtered, "test_data/w15_filtered.txt");

        RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);
//        writeData(w15Intersection, "test_data/w15_sorted.txt");

        w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
        w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

        for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w14Intersection.getVariable(j);
            variable.setName(variable.getName() + "-14");
        }

        writeData(w14Intersection, "test_data/w14Intersection.txt");

        for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
            Variable variable = (Variable) w15Intersection.getVariable(j);
            variable.setName(variable.getName() + "-15");
        }

        writeData(w15Intersection, "test_data/w15Intersection.txt");

        RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);

        RectangularDataSet subset1 = rowSubset(combined, varName + "-14", new double[]{1, 0});
        RectangularDataSet subset2 = rowSubset(subset1, varName + "-15", new double[]{1, 0});

        writeData(subset2, "test_data/combined" + varName + ".txt");
    }


    public RectangularDataSet filterData9(RectangularDataSet combined, String varName) {

        // Rename the variable that are already in the dataset.
        copyAs(combined, "PCHWKQ", "CT");
        copyAs(combined, "DEXAGE", "P_age");
        copyAs(combined, "DECAGE", "C_age");
        copyAs(combined, "DECINC", "C_inc");

        copyAs(combined, "SEVERE", "RMBPC");
        copyAs(combined, "DSFMEM", "RMBPC-M");
        copyAs(combined, "DSFBEH", "RMBPC-B");
        copyAs(combined, "DSFDEP", "RMBPC-D");

        copyAs(combined, "QLIADL", "IADL");
        addLogIadl(combined);
        copyAs(combined, "QLPSMS", "PSMS");

        copyAs(combined, "BURDEN", "CBS");
        copyAs(combined, "PCOHWKQ", "OFCT");

//        copyAs(combined, "RXNATO", "Memantine");
//        copyAs(combined, "RXADEP", "Antidepressants");
//        copyAs(combined, "RXAPSY", "Antipsychotics");
//        copyAs(combined, "RXANX", "Anxiolytics");
//        copyAs(combined, "RXSED", "Sedatives");

        addPSex(combined);
        addCSex(combined);
        addCSpouse(combined);
        addCFulltime(combined);

        addMemantine(combined);
        addAntidepressants(combined);
        addAntipsychotics(combined);
        addAnxiolytics(combined);
        addSedatives(combined);

        addBehDrugs(combined);
        addXcareDays(combined);
        addLogXCareDays(combined);

        addRaceVars(combined);

        if ("G1".equals(varName)) {
            addG1(combined);
        } else if ("G1a".equals(varName)) {
            addG1a(combined);
        } else if ("G1b".equals(varName)) {
            addG1b(combined);
        } else if ("G2".equals(varName)) {
            addG2(combined);
        } else if ("G3".equals(varName)) {
            addG3(combined);
        } else if ("G4".equals(varName)) {
            addG4(combined);
        } else if ("G5".equals(varName)) {
            addG5(combined);
        } else if ("G6".equals(varName)) {
            addG6(combined);
        } else if ("G6b".equals(varName)) {
            addG6b(combined);
        } else if ("G7".equals(varName)) {
            addG7(combined);
        } else if ("G7b".equals(varName)) {
            addG7b(combined);
        }

        // Create final subset.
        List<String> intermediateVars = new ArrayList<String>();
        intermediateVars.add("ID");
        intermediateVars.add(varName);
        intermediateVars.add("P_age");
        intermediateVars.add("P_sex");
        intermediateVars.add("C_age");
        intermediateVars.add("C_sex");
        intermediateVars.add("C_inc");
        intermediateVars.add("C_fulltime");
        intermediateVars.add("C_spouse");
        intermediateVars.add("BehDrugs");
        intermediateVars.add("RMBPC");
//        intermediateVars.add("IADL");
        intermediateVars.add("LOG_IADL");
        intermediateVars.add("CBS");
//        intermediateVars.add("PSMS");
//        intermediateVars.add("XcareDays");
        intermediateVars.add("LOG_XcareDays");
//        intermediateVars.add("OFCT");
//        intermediateVars.add("CT");
        intermediateVars.add("Cauc");
//        intermediateVars.add("Black");
        intermediateVars.add("Hispanic");
//        intermediateVars.add("Asian");
//        intermediateVars.add("DELIVE");
//        intermediateVars.add("RXNATO");

        return subset(combined, intermediateVars);
    }

    private void constructData10() {
        RectangularDataSet combined = makeCombinedContinuous();
        RectangularDataSet intermediate = filter10(combined);
        intermediate.removeColumn(intermediate.getVariable("ID"));
        intermediate = new RegressionInterpolator().filter(intermediate);
        writeData(intermediate, "test_data/set10.txt");
    }

    private void constructData11() {
        // Load W14 and W15 data.
         RectangularDataSet w14 = loadData("test_data/ad14.txt");
         System.out.println("Full wave 14 data " + w14.getNumRows());

         RectangularDataSet w14Intersection = rowSubset(w14, "WAVE15", new double[]{1.0});
         System.out.println("Subjects occurring in both waves = " + w14Intersection.getNumRows());

         w14Intersection = filter10(w14Intersection);

         RectangularDataSet w15 = loadData("test_data/ad15.txt");
         System.out.println("Full wave 15 data " + w15.getNumRows());

         RectangularDataSet w15_filtered = filter10(w15);

         RectangularDataSet w15Intersection = extractSortedByID(w14Intersection, w15_filtered);

         w14Intersection.removeColumn(w14Intersection.getVariable("ID"));
         w15Intersection.removeColumn(w15Intersection.getVariable("ID"));

         for (int j = 0; j < w14Intersection.getNumColumns(); j++) {
             Variable variable = (Variable) w14Intersection.getVariable(j);
             variable.setName(variable.getName() + "-14");
         }

         writeData(w14Intersection, "test_data/w14Intersection.txt");

         for (int j = 0; j < w15Intersection.getNumColumns(); j++) {
             Variable variable = (Variable) w15Intersection.getVariable(j);
             variable.setName(variable.getName() + "-15");
         }

         writeData(w15Intersection, "test_data/w15Intersection.txt");

         RectangularDataSet combined = appendColumns(w14Intersection, w15Intersection);
        combined = new RegressionInterpolator().filter(combined);

         writeData(combined, "test_data/set11.txt");

    }

    private RectangularDataSet filter10(RectangularDataSet combined) {
        List<String> intermediateVars = new ArrayList<String>();

        String[] rmbpcMem = new String[]{
                "DSSAMF", "DSRCTF", "DSPSTF", "DSLOSF", "DSFGTF", "DSSTRF",
                "DSCONF"
        };

        String[] rmbpcBeh = new String[]{
                "DSDTYF", "DSEMBF", "DSWAKF", "DSLDYF", "DSDNGF", "DSHURF",
                "DSVBAF", "DSIRRF"
        };

        String[] rmbpcDep = new String[]{
                "DSANXF", "DSTHRF", "DSSADF", "DSHLPF", "DSCRYF", "DSDTHF",
                "DSLNYF", "DSBRDF", "DSFALF"
        };

        String[] iadl = new String[]{
                "QLPHON", "QLCANW", "QLSHOP", "QLMEAL", "QLHSWK", "QLHAND",
                "QLLAUN", "QLMMON", "QLDRUG"
        };

        String[] psms = new String[]{
                "QLPEAT", "QLDRES", "QLCRE", "QLARON", "QLPBED", "QLBATH",
                "QLTRBL", "QLSOIL"
        };

        String[] cbsDemand = new String[]{
                "CBTRTO", "CBCARO", "CBWLKO",
                "CBTRNO", "CBINFO", "CBHHTO", "CBOUTO", "CBACTO", "CBCCO",
                "CBSERO", "CBEMOO", "CBSYMO", "CBBEHO", "CBCOMO"
        };

        String[] cbsDifficulty = new String[]{
                "CBTRTD", "CBCARD", "CBWLKD",
                "CBTRND", "CBINFD", "CBHHTD", "CBOUTD", "CBACTD", "CBCCD",
                "CBSERD", "CBEMOD", "CBSYMD", "CBBEHD", "CBCOMD"

        };

        addToIntermediate(intermediateVars, rmbpcMem);
        addToIntermediate(intermediateVars, rmbpcBeh);
        addToIntermediate(intermediateVars, rmbpcDep);
        addToIntermediate(intermediateVars, iadl);
        addToIntermediate(intermediateVars, psms);
        addToIntermediate(intermediateVars, cbsDemand);
        addToIntermediate(intermediateVars, cbsDifficulty);

        intermediateVars.add("ID");

        return subset(combined, intermediateVars);
    }

    private void addToIntermediate(List<String> intermediateVars, String[] vars) {
        for (String var : vars) {
            intermediateVars.add(var);
        }
    }


    private RectangularDataSet extractSortedByID(RectangularDataSet w14_filtered, RectangularDataSet w15_filtered) {
        RectangularDataSet w15_sorted = new ColtDataSet(w14_filtered.getNumRows(),
                w15_filtered.getVariables());

        int id14col = w14_filtered.getColumn(w14_filtered.getVariable("ID"));
        int id15col = w15_filtered.getColumn(w15_filtered.getVariable("ID"));

        DiscreteVariable id14var = (DiscreteVariable) w14_filtered.getVariable(id14col);
        DiscreteVariable id15var = (DiscreteVariable) w15_filtered.getVariable(id15col);

        for (int i = 0; i < w14_filtered.getNumRows(); i++) {
            String id14 = id14var.getCategory(w14_filtered.getInt(i, id14col));

            int correspondingRow = -1;

            for (int k = 0; k < w15_filtered.getNumRows(); k++) {
                String id15 = id15var.getCategory(w15_filtered.getInt(k, id15col));

                if (id14.equals(id15)) {
                    correspondingRow = k;
                    break;
                }
            }

            if (correspondingRow == -1) {
                throw new IllegalArgumentException();
            }

            System.out.println("Row in 14 " + i + " --> " + correspondingRow);


            for (int j = 0; j < w15_filtered.getNumColumns(); j++) {
                if (w15_filtered.getVariable(j) instanceof ContinuousVariable) {
                    w15_sorted.setDouble(i, j, w15_filtered.getDouble(correspondingRow, j));
                } else
                if (w15_filtered.getVariable(j) instanceof DiscreteVariable) {
                    w15_sorted.setInt(i, j, w15_filtered.getInt(correspondingRow, j));
                }
            }
        }
        return w15_sorted;
    }

    private RectangularDataSet makeCombinedContinuous() {
        // Load W14 and W15 data.
        RectangularDataSet w14 = loadData("test_data/ad14.txt");
        RectangularDataSet w15 = loadData("test_data/ad15.txt");

//        rename(w15, "RSPNDTID", "ID");

        w15 = rowSubset(w15, "WV15REP", new double[]{1.0});

        // Calculate names that are in both 14 and 15.
        List<String> w14Names = w14.getVariableNames();
        List<String> w15Names = w15.getVariableNames();

        List<String> intersection = new ArrayList<String>(w14Names);
        intersection.retainAll(w15Names);

        System.out.println(intersection);

        // Calculate subsets of 14 and 15 using intersection of names.
        RectangularDataSet w14Subset = subset(w14, intersection);
        RectangularDataSet w15Subset = subset(w15, intersection);

        // Combine the two data sets.
//        return appendCases(w14Subset, w15Subset);
        return appendCases(w15Subset, w14Subset);
    }

    private RectangularDataSet readCombinedContinuous() {
        String path = "test_data/ad14-ad15r.dat";

        try {
            TetradLogger.getInstance().addOutputStream(System.out);
            DataParser parser = new DataParser();
            parser.setMissingValueMarker(".");
            parser.setDelimiter(DelimiterType.COMMA);
            parser.setQuoteChar('"');
            parser.setMaxIntegralDiscrete(0);
            RectangularDataSet dataSet = parser.parseTabular(new File(path));
            dataSet.setNumberFormat(NumberFormatUtil.getInstance().getNumberFormat());

            TetradLogger.getInstance().removeOutputStream(System.out);
            return dataSet;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RectangularDataSet rowSubset(RectangularDataSet data, String var,
                                         double[] values) {

        int col = data.getColumn(data.getVariable(var));

        if (col == -1) {
            throw new IllegalArgumentException("Variable not in data: " + var);
        }

        int count = 0;

        for (int i = 0; i < data.getNumRows(); i++) {
            for (double value : values) {
                if (data.getDouble(i, col) == value) {
                    count++;
                    break;
                }
            }
        }

        RectangularDataSet data2 = new ColtDataSet(count, data.getVariables());
        int row2 = -1;

        for (int i = 0; i < data.getNumRows(); i++) {
            for (double value : values) {
                if (data.getDouble(i, col) == value) {
                    row2++;

                    for (int j = 0; j < data.getNumColumns(); j++) {
                        if (data.getVariable(j) instanceof ContinuousVariable) {
                            data2.setDouble(row2, j, data.getDouble(i, j));
                        } else {
                            data2.setInt(row2, j, data.getInt(i, j));
                        }
                    }

                    break;
                }
            }
        }

        return data2;
    }

    /**
     * Combines the two data sets, assuming the variables are the same for
     * both.
     */
    private RectangularDataSet appendCases(RectangularDataSet data1,
                                           RectangularDataSet data2) {
        if (!data1.getVariableNames().equals(data2.getVariableNames())) {
            throw new IllegalArgumentException("Variable names not equal for " +
                    "data sets.");
        }

        int data1Rows = data1.getNumRows();
        int data2Rows = data2.getNumRows();
        List<Node> variables = data1.getVariables();

        List<Node> newVariables = new ArrayList<Node>();

        for (Node variable : variables) {
            if (variable instanceof ContinuousVariable) {
                newVariables.add(variable);
            } else {
                DiscreteVariable v1 = (DiscreteVariable) variable;
                DiscreteVariable v2 = (DiscreteVariable) data2.getVariable(v1.getName());

                List<String> categories = v1.getCategories();

                for (String category : v2.getCategories()) {
                    if (!categories.contains(category)) {
                        categories.add(category);
                    }
                }

                Collections.sort(categories);

                DiscreteVariable v3 = new DiscreteVariable(v1.getName(), categories);
                newVariables.add(v3);
            }
        }

        RectangularDataSet combined = new ColtDataSet(data1Rows + data2Rows,
                newVariables);

        for (int i = 0; i < data1Rows; i++) {
            for (int j = 0; j < variables.size(); j++) {
                if (data1.getVariable(j) instanceof ContinuousVariable) {
                    combined.setDouble(i, j, data1.getDouble(i, j));
                } else {
                    combined.setInt(i, j, data1.getInt(i, j));
                }
            }
        }

        for (int i = 0; i < data2Rows; i++) {
            for (int j = 0; j < variables.size(); j++) {
                if (data1.getVariable(j) instanceof ContinuousVariable) {
                    combined.setDouble(i + data1Rows, j, data2.getDouble(i, j));
                } else {
                    combined.setInt(i + data1Rows, j, data2.getInt(i, j));
                }
            }
        }

        return combined;
    }

    /**
     * Combines the two data sets, assuming the variables are the same for
     * both.
     */
    private RectangularDataSet appendColumns(RectangularDataSet data1,
                                             RectangularDataSet data2) {
        if (data1.getNumRows() != data2.getNumRows()) {
            throw new IllegalArgumentException("Number of rows not equal.");
        }

        List<Node> variables1 = data1.getVariables();
        List<Node> variables2 = data2.getVariables();
        List<Node> variables = new ArrayList<Node>(variables1);
        variables.addAll(variables2);

        RectangularDataSet combined = new ColtDataSet(data1.getNumRows(), variables);

        for (int i = 0; i < data1.getNumRows(); i++) {
            for (int j = 0; j < variables1.size(); j++) {
                Variable variable = (Variable) variables1.get(j);

                if (variable instanceof DiscreteVariable) {
                    combined.setInt(i, j, data1.getInt(i, j));
                } else if (variable instanceof ContinuousVariable) {
                    combined.setDouble(i, j, data1.getDouble(i, j));
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }


        for (int i = 0; i < data2.getNumRows(); i++) {
            for (int j = 0; j < variables2.size(); j++) {
                Variable variable = (Variable) variables2.get(j);

                if (variable instanceof DiscreteVariable) {
                    combined.setInt(i, j + data1.getNumColumns(), data2.getInt(i, j));
                } else if (variable instanceof ContinuousVariable) {
                    combined.setDouble(i, j + data1.getNumColumns(), data2.getDouble(i, j));
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        return combined;
    }

    private void copyAs(RectangularDataSet data, String s1, String s2) {
        int s1Col = data.getColumn(data.getVariable(s1));
        Node node = data.getVariable(s1Col);

        if (s1Col == -1) {
            throw new IllegalArgumentException(
                    "Expecting to find a variable named " + s1);
        }

        if (node instanceof ContinuousVariable) {
            data.addVariable(new ContinuousVariable(s2));
            int s2Col = data.getColumn(data.getVariable(s2));

            for (int i = 0; i < data.getNumRows(); i++) {
                data.setDouble(i, s2Col, data.getDouble(i, s1Col));
            }
        } else {
            DiscreteVariable v1 = (DiscreteVariable) data.getVariable(s1);
            DiscreteVariable v2 = new DiscreteVariable(s2, v1.getCategories());
            data.addVariable(v2);

            int s2Col = data.getColumn(data.getVariable(s2));

            for (int i = 0; i < data.getNumRows(); i++) {
                data.setInt(i, s2Col, data.getInt(i, s1Col));
            }
        }
    }

    private void rename(RectangularDataSet data, String s1, String s2) {
        int s1Col = data.getColumn(data.getVariable(s1));
        Node node = data.getVariable(s1Col);

        if (s1Col == -1) {
            throw new IllegalArgumentException(
                    "Expecting to find a variable named " + s1);
        }

        if (node instanceof ContinuousVariable) {
            ContinuousVariable v1 = (ContinuousVariable) data.getVariable(s1);
            ContinuousVariable v2 = new ContinuousVariable(s2);
            data.changeVariable(v1, v2);
        } else {
            DiscreteVariable v1 = (DiscreteVariable) data.getVariable(s1);
            DiscreteVariable v2 = new DiscreteVariable(s2, v1.getCategories());
            data.changeVariable(v1, v2);
        }
    }


    private void addCSex(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("C_sex"));
        int s2Col = combined.getColumn(combined.getVariable("C_sex"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double value = getDoubleValue(combined, "DEGNDR", i);

            // 1 if female.
            if (value == 2.0) {
                combined.setDouble(i, s2Col, 1.0);
            } else {
                combined.setDouble(i, s2Col, 0.0);
            }
        }
    }

    private void addLogXCareDays(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("LOG_XcareDays"));
        int log = combined.getColumn(combined.getVariable("LOG_XcareDays"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double value = getDoubleValue(combined, "XcareDays", i);
            combined.setDouble(i, log, Math.log(value + 0.5));
        }
    }

    private void addLogIadl(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("LOG_IADL"));
        int log = combined.getColumn(combined.getVariable("LOG_IADL"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double value = getDoubleValue(combined, "IADL", i);
            combined.setDouble(i, log, Math.log(value));
        }
    }

    private void addPSex(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("P_sex"));
        int s2Col = combined.getColumn(combined.getVariable("P_sex"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double value = getDoubleValue(combined, "DEXSEX", i);

            // 1 if female.
            if (value == 2.0) {
                combined.setDouble(i, s2Col, 1.0);
            } else {
                combined.setDouble(i, s2Col, 0.0);
            }
        }
    }

    private void addCSpouse(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("C_spouse"));
        int s2Col = combined.getColumn(combined.getVariable("C_spouse"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double value = getDoubleValue(combined, "DERELA", i);

            // 1 if "My spouse/significant other."u
            if (value == 1.0) {
                combined.setDouble(i, s2Col, 1.0);
            } else {
                combined.setDouble(i, s2Col, 0.0);
            }
        }
    }

    private void addCFulltime(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("C_fulltime"));
        int s2Col = combined.getColumn(combined.getVariable("C_fulltime"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double value = getDoubleValue(combined, "DECPEMP", i);

            // 1 if "Full-time"
            if (value == 1.0) {
                combined.setDouble(i, s2Col, 1.0);
            } else {
                combined.setDouble(i, s2Col, 0.0);
            }
        }
    }

    private void addMemantine(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("Memantine"));
        int col = combined.getColumn(combined.getVariable("Memantine"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXNATO", i) == 1.0) {
                combined.setDouble(i, col, 1.0);
            } else {
                combined.setDouble(i, col, 0.0);
            }
        }
    }

    private void addAntidepressants(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("Antidepressants"));
        int col = combined.getColumn(combined.getVariable("Antidepressants"));

        // CYMBALTA (RXCYTO), CELEXA (RXCXTO*), LEXAPRO (RXLXTO*),
        // PAXIL (RXPLTO*), PROZAC (RXPZTO*), ZOLOFT (RXZLTO)

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXCXTO", i) == 1.0
                    || getDoubleValue(combined, "RXLXTO", i) == 1.0
                    || getDoubleValue(combined, "RXPLTO", i) == 1.0
                    || getDoubleValue(combined, "RXPZTO", i) == 1.0
//                    || getValue(combined, "RXCYTO", i) == 1.0
                    || getDoubleValue(combined, "RXZLTO", i) == 1.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else {
                combined.setDouble(i, col, 0.0);
            }
        }
    }

    private void addAnxiolytics(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("Anxiolytics"));
        int col = combined.getColumn(combined.getVariable("Anxiolytics"));

        // ADAVAN (RXAVTO), XANAX (RXXXTO)

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXAVTO", i) == 1.0
                    || getDoubleValue(combined, "RXXXTO", i) == 1.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else {
                combined.setDouble(i, col, 0.0);
            }
        }
    }

    private void addSedatives(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("Sedatives"));
        int col = combined.getColumn(combined.getVariable("Sedatives"));

        // AMBIEN (RXAMTO)

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXAMTO", i) == 1.0) {
                combined.setDouble(i, col, 1.0);
            } else {
                combined.setDouble(i, col, 0.0);
            }
        }
    }

    private void addBehDrugs(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("BehDrugs"));
        int col = combined.getColumn(combined.getVariable("BehDrugs"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            boolean b1 = getDoubleValue(combined, "Antidepressants", i) == 1;
            boolean b2 = getDoubleValue(combined, "Anxiolytics", i) == 1;
            boolean b3 = getDoubleValue(combined, "Antipsychotics", i) == 1;
            boolean b4 = getDoubleValue(combined, "Sedatives", i) == 1;
            boolean b5 = b1 || b2 || b3 || b4;

            combined.setDouble(i, col, b5 ? 1.0 : 0.0);
        }
    }

    private void addXcareDays(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("XcareDays"));
        int col = combined.getColumn(combined.getVariable("XcareDays"));

        int col1 = combined.getColumn(combined.getVariable("SSDYCQ"));
        int col2 = combined.getColumn(combined.getVariable("SSRSCQ"));
        int col3 = combined.getColumn(combined.getVariable("SSHACQ"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double ssdycq = combined.getDouble(i, col1);
            double ssrscq = combined.getDouble(i, col2);
            double sshacq = combined.getDouble(i, col3);

            double sum = ssdycq + ssrscq + sshacq;

            combined.setDouble(i, col, sum);
        }
    }

    private void addRaceVars(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("Cauc"));
        int cauc = combined.getColumn(combined.getVariable("Cauc"));

        combined.addVariable(new ContinuousVariable("Black"));
        int black = combined.getColumn(combined.getVariable("Black"));

        combined.addVariable(new ContinuousVariable("Hispanic"));
        int hispanic = combined.getColumn(combined.getVariable("Hispanic"));

        combined.addVariable(new ContinuousVariable("Asian"));
        int asian = combined.getColumn(combined.getVariable("Asian"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            double race = getDoubleValue(combined, "DEXRACE", i);

            combined.setDouble(i, cauc, Double.isNaN(race) ? Double.NaN : 0.0);
            combined.setDouble(i, black, Double.isNaN(race) ? Double.NaN : 0.0);
            combined.setDouble(i, hispanic, Double.isNaN(race) ? Double.NaN : 0.0);
            combined.setDouble(i, asian, Double.isNaN(race) ? Double.NaN : 0.0);

            if (race == 1.0) {
                combined.setDouble(i, black, 1.0);
            } else if (race == 3.0) {
                combined.setDouble(i, asian, 1.0);
            } else if (race == 4.0) {
                combined.setDouble(i, hispanic, 1.0);
            } else if (race == 5.0) {
                combined.setDouble(i, cauc, 1.0);
            }
        }
    }

    private void addG1(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G1"));
        int col = combined.getColumn(combined.getVariable("G1"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
//                            && getValue(combined, "RXEXTO", i) != 1.0
//                            && getValue(combined, "RXARTO", i) != 1.0
//                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) != 1.0
                            && getDoubleValue(combined, "RXEXTO", i) != 1.0
                            && getDoubleValue(combined, "RXARTO", i) != 1.0
//                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG1a(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G1a"));
        int col = combined.getColumn(combined.getVariable("G1a"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
//                            && getValue(combined, "RXEXTO", i) != 1.0
//                            && getValue(combined, "RXARTO", i) != 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) != 1.0
                            && getDoubleValue(combined, "RXEXTO", i) != 1.0
                            && getDoubleValue(combined, "RXARTO", i) != 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG1b(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G1b"));
        int col = combined.getColumn(combined.getVariable("G1b"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
                            && (getDoubleValue(combined, "RXNATO", i) == 1.0 ||
                            getDoubleValue(combined, "RXNATO", i) == 2.0)
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) != 1.0
                            && getDoubleValue(combined, "RXEXTO", i) != 1.0
                            && getDoubleValue(combined, "RXARTO", i) != 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG2(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G2"));
        int col = combined.getColumn(combined.getVariable("G2"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXRYTO", i) == 1.0
                    && getDoubleValue(combined, "DELIVE", i) == 2.0) {
                combined.setDouble(i, col, 1.0);
            } else if (getDoubleValue(combined, "DELIVE", i) == 2.0) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG3(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G3"));
        int col = combined.getColumn(combined.getVariable("G3"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXRYTO", i) == 1.0
                    && getDoubleValue(combined, "DELIVE", i) == 2.0) {
                combined.setDouble(i, col, 1.0);
            } else if ((getDoubleValue(combined, "RXARTO", i) == 1.0
                    || getDoubleValue(combined, "RXEXTO", i) == 1.0)
                    && getDoubleValue(combined, "DELIVE", i) == 2.0) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG4(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G4"));
        int col = combined.getColumn(combined.getVariable("G4"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXRYTO", i) == 1.0
                    && (getDoubleValue(combined, "RXNATO", i) == 1.0 ||
                              getDoubleValue(combined, "RXNATO", i) == 2.0)
                    && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            }
            else if (getDoubleValue(combined, "DELIVE", i) == 2.0) {
                combined.setDouble(i, col, 0.0);
            }
            else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG5(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G5"));
        int col = combined.getColumn(combined.getVariable("G5"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXRYTO", i) == 1.0) {
                combined.setDouble(i, col, 1.0);
            } else {
                combined.setDouble(i, col, 0.0);
            }
        }
    }

    private void addG6(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G6"));
        int col = combined.getColumn(combined.getVariable("G6"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) == 2.0
                            && getDoubleValue(combined, "RXEXTO", i) == 2.0
                            && getDoubleValue(combined, "RXARTO", i) == 2.0
                            && getDoubleValue(combined, "RXNATO", i) == 2.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG6b(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G6b"));
        int col = combined.getColumn(combined.getVariable("G6b"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) == 2.0
                            && getDoubleValue(combined, "RXEXTO", i) != 1.0
                            && getDoubleValue(combined, "RXARTO", i) != 1.0
                            && getDoubleValue(combined, "RXNATO", i) != 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG7(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G7"));
        int col = combined.getColumn(combined.getVariable("G7"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
                            && getDoubleValue(combined, "RXEXTO", i) == 2.0
                            && getDoubleValue(combined, "RXARTO", i) == 2.0
                            && getDoubleValue(combined, "RXNATO", i) == 2.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) == 2.0
                            && getDoubleValue(combined, "RXEXTO", i) == 2.0
                            && getDoubleValue(combined, "RXARTO", i) == 2.0
                            && getDoubleValue(combined, "RXNATO", i) == 2.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addG7b(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G7b"));
        int col = combined.getColumn(combined.getVariable("G7b"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (
                    getDoubleValue(combined, "RXRYTO", i) == 1.0
                            && getDoubleValue(combined, "RXEXTO", i) != 1.0
                            && getDoubleValue(combined, "RXARTO", i) != 1.0
                            && getDoubleValue(combined, "RXNATO", i) != 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 1.0);
            } else if (
                    getDoubleValue(combined, "RXRYTO", i) == 2.0
                            && getDoubleValue(combined, "RXEXTO", i) != 1.0
                            && getDoubleValue(combined, "RXARTO", i) != 1.0
                            && getDoubleValue(combined, "RXNATO", i) != 1.0
                            && getDoubleValue(combined, "DELIVE", i) == 2.0
                    ) {
                combined.setDouble(i, col, 0.0);
            } else {
                combined.setDouble(i, col, Double.NaN);
            }
        }
    }

    private void addAntipsychotics(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("Antipsychotics"));
        int col = combined.getColumn(combined.getVariable("Antipsychotics"));

        // ABILIFY (RXABTO) GEODON (RXGDTO), HALDOL (RXHATO), RISPERDAL (RXRPTO)
        // SERIQUIL (RXSQTO), ZYPREXA (RXZPTO)

        for (int i = 0; i < combined.getNumRows(); i++) {
            if (getDoubleValue(combined, "RXABTO", i) == 1.0
                    || getDoubleValue(combined, "RXRPTO", i) == 1.0
                    || getDoubleValue(combined, "RXGDTO", i) == 1.0
                    || getDoubleValue(combined, "RXHATO", i) == 1.0
                    || getDoubleValue(combined, "RXSQTO", i) == 1.0
                    || getDoubleValue(combined, "RXZPTO", i) == 1.0) {
                combined.setDouble(i, col, 1.0);
            } else {
                combined.setDouble(i, col, 0.0);
            }
        }
    }

    private double getDoubleValue(RectangularDataSet dataSet, String var, int row) {
        Node variable = dataSet.getVariable(var);

        if (variable == null) {
            throw new IllegalArgumentException(
                    "Expected to find variable named " +
                            var);
        }

        int col = dataSet.getColumn(variable);
        return dataSet.getDouble(row, col);
    }

    private String getStringValue(RectangularDataSet dataSet, DiscreteVariable variable,
                                  int row) {

        if (variable == null) {
            throw new IllegalArgumentException(
                    "Expected to find variable named " +
                            variable);
        }

        int col = dataSet.getColumn(variable);
        int value = dataSet.getInt(row, col);
        return ((DiscreteVariable) dataSet.getVariable(col)).getCategory(value);
    }

    private RectangularDataSet subset(RectangularDataSet data,
                                      List<String> varNames) {
        List<Node> nodes = new ArrayList<Node>();

        for (String varName : varNames) {
            Node variable = data.getVariable(varName);

            if (variable == null) {
                throw new IllegalArgumentException(
                        "Variables missing from source: "
                                + varName);
            }

            nodes.add(variable);
        }

        return data.subsetColumns(nodes);
    }

    private void addScores(RectangularDataSet dataSet) {
        addRmbpcScores(dataSet);
        addIadlScores(dataSet);
        addPsmsScores(dataSet);
        addCbsScores(dataSet);
    }

    private void addRmbpcScores(RectangularDataSet dataSet) {
        dataSet.addVariable(new ContinuousVariable("RMBPC-M"));
        dataSet.addVariable(new ContinuousVariable("RMBPC-B"));
        dataSet.addVariable(new ContinuousVariable("RMBPC-D"));
        dataSet.addVariable(new ContinuousVariable("RMBPC"));

        String[] rmbpcMem = new String[]{
                "DSSAMF", "DSRCTF", "DSPSTF", "DSLOSF", "DSFGTF", "DSSTRF",
                "DSCONF"
        };

        String[] rmbpcBeh = new String[]{
                "DSDTYF", "DSEMBF", "DSWAKF", "DSLDYF", "DSDNGF", "DSHURF",
                "DSVBAF", "DSIRRF"
        };

        String[] rmbpcDep = new String[]{
                "DSANXF", "DSTHRF", "DSSADF", "DSHLPF", "DSCRYF", "DSDTHF",
                "DSLNYF", "DSBRDF", "DSFALF"
        };

        String[] rmbpcAll = new String[]{
                "DSSAMF", "DSRCTF", "DSPSTF", "DSLOSF", "DSFGTF", "DSSTRF",
                "DSCONF",
                "DSDTYF", "DSEMBF", "DSWAKF", "DSLDYF", "DSDNGF", "DSHURF",
                "DSVBAF", "DSIRRF",
                "DSANXF", "DSTHRF", "DSSADF", "DSHLPF", "DSCRYF", "DSDTHF",
                "DSLNYF", "DSBRDF", "DSFALF"
        };

        Map<String, Double> means = precalculateMeansOfItems(rmbpcAll, dataSet);

        for (String varName : rmbpcMem) {
            shiftAndFixMissing(dataSet, varName);
        }

        for (String varName : rmbpcBeh) {
            shiftAndFixMissing(dataSet, varName);
        }

        for (String varName : rmbpcDep) {
            shiftAndFixMissing(dataSet, varName);
        }

        // For each row.
        for (int i = 0; i < dataSet.getNumRows(); i++) {

            // Count # missing values for each set of score vars.
            int numMemMissing = nmiss(dataSet, rmbpcMem, i);
            int numBehMissing = nmiss(dataSet, rmbpcBeh, i);
            int numDepMissing = nmiss(dataSet, rmbpcDep, i);

            // Determine whether # missing values is less than a third for that score.
            boolean countMem = numMemMissing <= 2;
            boolean countBeh = numBehMissing <= 3;
            boolean countDep = numDepMissing <= 3;

            // Otherwise, set any missing value to mean within score.
            if (countMem) {
                setMissingToItemMeans(dataSet, means, rmbpcMem, i);
                double sumMem = addUp(dataSet, rmbpcMem, i);
                int col = dataSet.getColumn(dataSet.getVariable("RMBPC-M"));
                dataSet.setDouble(i, col, sumMem / rmbpcMem.length);
            }

            if (countBeh) {
                setMissingToItemMeans(dataSet, means, rmbpcBeh, i);
                double sumBeh = addUp(dataSet, rmbpcBeh, i);
                int col = dataSet.getColumn(dataSet.getVariable("RMBPC-B"));
                dataSet.setDouble(i, col, sumBeh / rmbpcBeh.length);
            }

            if (countDep) {
                setMissingToItemMeans(dataSet, means, rmbpcDep, i);
                double sumDep = addUp(dataSet, rmbpcDep, i);
                int col = dataSet.getColumn(dataSet.getVariable("RMBPC-D"));
                dataSet.setDouble(i, col, sumDep / rmbpcDep.length);
            }


            if (countMem && countBeh && countDep) {
                double sumAll = addUp(dataSet, rmbpcAll, i);
                int col = dataSet.getColumn(dataSet.getVariable("RMBPC"));
                dataSet.setDouble(i, col, sumAll / rmbpcAll.length);
            } else {
                int j = dataSet.getColumn(dataSet.getVariable("RMBPC"));
                dataSet.setDouble(i, j, Double.NaN);
            }
        }
    }

    private Map<String, Double> precalculateMeansOfItems(String[] vars,
                                                         RectangularDataSet dataSet) {
        Map<String, Double> means = new HashMap<String, Double>();

        for (String var : vars) {
            int count = 0;
            double sum = 0.0;
            int col = dataSet.getColumn(dataSet.getVariable(var));

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                double value = dataSet.getDouble(i, col);

                if (!Double.isNaN(value)) {
                    sum += dataSet.getDouble(i, col);
                    count++;
                }
            }

            means.put(var, sum / count);
        }

        return means;
    }

    private void addIadlScores(RectangularDataSet dataSet) {
        dataSet.addVariable(new ContinuousVariable("IADL"));

        String[] iadl = new String[]{
                "QLPHON", "QLCANW", "QLSHOP", "QLMEAL", "QLHSWK", "QLHAND",
                "QLLAUN", "QLMMON", "QLDRUG"
        };

        for (String varName : iadl) {
            int j = dataSet.getColumn(dataSet.getVariable(varName));

            if (j == -1) {
                throw new IllegalArgumentException(
                        "Variable " + varName + " not in data.");
            }

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                if (!Double.isNaN(dataSet.getDouble(i, j))) {
                    dataSet.setDouble(i, j, 4.0 - dataSet.getDouble(i, j));
                }
            }
        }

        Map<String, Double> means = precalculateMeansOfItems(iadl, dataSet);

        // For each row.
        for (int i = 0; i < dataSet.getNumRows(); i++) {

            // Count # missing values for each set of score iadl.
            int numMemMissing = nmiss(dataSet, iadl, i);

            // Determine whether # missing values is less than a third for that score.
            boolean countMem = numMemMissing <= 3;

            // Otherwise, set any missing value to mean within score.
            if (countMem) {
                setMissingToItemMeans(dataSet, means, iadl, i);
                double sumMem = addUp(dataSet, iadl, i);
                int col = dataSet.getColumn(dataSet.getVariable("IADL"));
                dataSet.setDouble(i, col, sumMem);
            }
        }
    }

    private void addPsmsScores(RectangularDataSet dataSet) {
        dataSet.addVariable(new ContinuousVariable("PSMS"));

        String[] psms = new String[]{
                "QLPEAT", "QLDRES", "QLCRE", "QLARON", "QLPBED", "QLBATH",
                "QLTRBL", "QLSOIL"
        };

        String[] reverse4vars = new String[]{
                "QLPEAT", "QLDRES", "QLCRE", "QLARON", "QLPBED", "QLBATH"
        };

        for (String varName : reverse4vars) {
            int j = dataSet.getColumn(dataSet.getVariable(varName));

            if (j == -1) {
                throw new IllegalArgumentException(
                        "Variable " + varName + " not in data.");
            }

            for (int i = 0; i < dataSet.getNumRows(); i++) {
                if (!Double.isNaN(dataSet.getDouble(i, j))) {
                    dataSet.setDouble(i, j, 4.0 - dataSet.getDouble(i, j));
                }
            }
        }

//        qltrblr=&qltrblr;
//qlsoilr=5-&qlsoil;
//if qlsoilr<4 then qltrblr=1;

        int qltrbl = dataSet.getColumn(dataSet.getVariable("QLTRBL"));
        int qlsoil = dataSet.getColumn(dataSet.getVariable("QLSOIL"));

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            if (!Double.isNaN(dataSet.getDouble(i, qlsoil))) {
                dataSet.setDouble(i, qlsoil, 5.0 - dataSet.getDouble(i, qlsoil));
            }
        }

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            if (!Double.isNaN(dataSet.getDouble(i, qlsoil))) {
                double val = dataSet.getDouble(i, qlsoil);
                if (val < 4.0) {
                    dataSet.setDouble(i, qltrbl, 1.0);
                }
            }
        }

        Map<String, Double> means = precalculateMeansOfItems(psms, dataSet);

        // For each row.
        for (int i = 0; i < dataSet.getNumRows(); i++) {

            // Count # missing values for each set of score psms.
            int numMemMissing = nmiss(dataSet, psms, i);

            // Determine whether # missing values is less than a third for that score.
            boolean countMem = numMemMissing <= 3;

            // Otherwise, set any missing value to mean within score.
            if (countMem) {
                setMissingToItemMeans(dataSet, means, psms, i);
                double sumMem = addUp(dataSet, psms, i);
                int col = dataSet.getColumn(dataSet.getVariable("PSMS"));
                dataSet.setDouble(i, col, sumMem);
            }
        }
    }

    private void addCbsScores(RectangularDataSet dataSet) {
        dataSet.addVariable(new ContinuousVariable("CBS"));
        int cbsColumn = dataSet.getColumn(dataSet.getVariable("CBS"));

        String[] cbsDemand = new String[]{
                "CBTRTO", "CBCARO", "CBWLKO",
                "CBTRNO", "CBINFO", "CBHHTO", "CBOUTO", "CBACTO", "CBCCO",
                "CBSERO", "CBEMOO", "CBSYMO", "CBBEHO", "CBCOMO", "CBINFO"
        };

        String[] cbsDifficulty = new String[]{
                "CBTRTD", "CBCARD", "CBWLKD",
                "CBTRND", "CBINFD", "CBHHTD", "CBOUTD", "CBACTD", "CBCCD",
                "CBSERD", "CBEMOD", "CBSYMD", "CBBEHD", "CBCOMD", "CBINFD"

        };

        Map<String, Double> meansDemand = precalculateMeansOfItems(cbsDemand, dataSet);
        Map<String, Double> meansDifficulty = precalculateMeansOfItems(cbsDifficulty, dataSet);

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            boolean countDemand = nmiss(dataSet, cbsDemand, i) <= 5;
            boolean countDifficulty = nmiss(dataSet, cbsDifficulty, i) <= 5;

            if (countDemand && countDifficulty) {
                setMissingToItemMeans(dataSet, meansDemand, cbsDemand, i);
                setMissingToItemMeans(dataSet, meansDifficulty, cbsDifficulty, i);

                double sum = 0.0;

                for (int k = 0; k < 15; k++) {
                    int demandColumn = dataSet.getColumn(
                            dataSet.getVariable(cbsDemand[k]));
                    int difficultyColumn = dataSet.getColumn(
                            dataSet.getVariable(cbsDifficulty[k]));
                    double demand = dataSet.getDouble(i, demandColumn);
                    double difficulty = dataSet.getDouble(i, difficultyColumn);
                    sum += Math.sqrt(demand * difficulty);
                }

                double burden = sum / 15;
                dataSet.setDouble(i, cbsColumn, burden);
            } else {
                dataSet.setDouble(i, cbsColumn, Double.NaN);
            }
        }
    }

    private void addGalantamineIds(RectangularDataSet combined) {
        combined.addVariable(new ContinuousVariable("G1-0"));

        RectangularDataSet g1ids = load2("test_data/g1ids.txt");

        Set<String> galIds = new HashSet<String>();
        Set<String> noaIds = new HashSet<String>();
        DiscreteVariable v1 = (DiscreteVariable) g1ids.getVariable("Medication-0");
        DiscreteVariable v2 = (DiscreteVariable) g1ids.getVariable("ID-0");

        for (int i = 0; i < g1ids.getNumRows(); i++) {
            String stringValue = getStringValue(g1ids, v1, i);

            if ("Galantamine".equals(stringValue)) {
                galIds.add(getStringValue(g1ids, v2, i));
                System.out.println(i + ". Galantamtamine");
            } else if ("No_AChEI".equals(stringValue)) {
                noaIds.add(getStringValue(g1ids, v2, i));
                System.out.println(i + ". No_AChEI");
            } else {
                throw new IllegalArgumentException();
            }
        }

        System.out.println("Galantamine: " + galIds);
        System.out.println("No AChEi: " + noaIds);

        DiscreteVariable v3 = (DiscreteVariable) combined.getVariable("ID");
        int column = combined.getColumn(combined.getVariable("G1-0"));

        for (int i = 0; i < combined.getNumRows(); i++) {
            String id = getStringValue(combined, v3, i);
            System.out.println(i + ". id = " + id);

            double value;

            if (galIds.contains(id)) {
                value = 1.0;
            } else if (noaIds.contains(id)) {
                value = 0.0;
            } else {
                value = -1.0;
            }

            combined.setDouble(i, column, value);
        }
    }

    private void setMissingToItemMeans(RectangularDataSet data,
                                       Map<String, Double> means, String[] vars, int row) {
        for (String var : vars) {
            int col = data.getColumn(data.getVariable(var));

            if (Double.isNaN(data.getDouble(row, col))) {
                data.setDouble(row, col, means.get(var));
            }
        }
    }

    private double addUp(RectangularDataSet dataSet, String[] vars, int row) {
        double sum = 0;

        for (String var : vars) {
            int col = dataSet.getColumn(dataSet.getVariable(var));
            sum += dataSet.getDouble(row, col);
        }

        return sum;
    }

    private int nmiss(RectangularDataSet dataSet, String[] vars,
                      int row) {
        int numMissing = 0;

        for (String var : vars) {
            int col = dataSet.getColumn(dataSet.getVariable(var));

            if (Double.isNaN(dataSet.getDouble(row, col))) {
                numMissing++;
            }
        }

        return numMissing;
    }

    private void shiftAndFixMissing(RectangularDataSet dataSet, String var) {
        int j = dataSet.getColumn(dataSet.getVariable(var));

        if (j == -1) {
            throw new IllegalArgumentException(
                    "Variable " + var + " not in data.");
        }

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            if (!Double.isNaN(dataSet.getDouble(i, j))) {
                dataSet.setDouble(i, j, dataSet.getDouble(i, j) - 1.0);
            }
        }

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            if (dataSet.getDouble(i, j) == 5.0) {
                dataSet.setDouble(i, j, Double.NaN);
//                System.out.println(i + " " + j);
            }
        }
    }

    private RectangularDataSet loadData(String pathname) {
        try {

            TetradLogger.getInstance().addOutputStream(System.out);
            DataParser parser = new DataParser();
            parser.setDelimiter(DelimiterType.COMMA);
            parser.setQuoteChar('"');
            parser.setMaxIntegralDiscrete(0);
            RectangularDataSet dataSet = parser.parseTabular(new File(pathname));
            dataSet.setNumberFormat(NumberFormatUtil.getInstance().getNumberFormat());

            TetradLogger.getInstance().removeOutputStream(System.out);
            return dataSet;

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RectangularDataSet load2(String pathname) {
        try {

            TetradLogger.getInstance().addOutputStream(System.out);
            DataParser parser = new DataParser();
            parser.setDelimiter(DelimiterType.TAB);
//            parser.setQuoteChar('"');
//            parser.setMaxIntegralDiscrete(0);
            RectangularDataSet dataSet = parser.parseTabular(new File(pathname));
            dataSet.setNumberFormat(NumberFormatUtil.getInstance().getNumberFormat());

            TetradLogger.getInstance().removeOutputStream(System.out);
            return dataSet;

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeData(RectangularDataSet dataSet, String pathname) {
        try {
            File file = new File(pathname);
            PrintWriter writer = new PrintWriter(file);
            dataSet.setNumberFormat(NumberFormatUtil.getInstance().getNumberFormat());
            dataSet.setOutputDelimiter(',');
            writer.println(dataSet);
            writer.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
