package edu.cmu.shuttle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.util.*;

/**
 * Some preliminary tests.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4923 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class TestFoolingAround extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestFoolingAround(String name) {
        super(name);
    }

    /**
     * Blank test to keep the automatic JUnit runner happy.
     */
    public static void test() {
        // Blank test.
    }

    /**
     * Tries stupidly to convert the entire data set to rectangular format,
     * marking unspecified data as missing. Not  a good thing to do. It's like 5
     * GB and almost all missing values. Variables of values should persist
     * until they change.
     */
    public static void rtest1() {

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));

            String outFilename =
                    inFilename.substring(0, inFilename.lastIndexOf('.'));
            outFilename = outFilename + ".out.txt";

            Set<String> variableSet = new HashSet<String>();
            String time = "";
            String line;

            while ((line = reader.readLine()) != null) {
                Record record = ConvertUtils.parseRecord(line);

                // Prints out when the time changes.
                if (!time.equals(record.getTime())) {
                    System.out.println(record.getTime());
                    time = record.getTime();
                }

                // Collects up variables.
                if (!variableSet.contains(record.getVariable())) {
                    variableSet.add(record.getVariable());
                }
            }

            // Sorts variables and prints them.
            List<String> variables = new ArrayList<String>(variableSet);
            Collections.sort(variables);

            for (int i = 0; i < variables.size(); i++) {
                System.out.println(i + ". " + variables.get(i));
            }

            // Write out the first line of variable names.
            reader = new BufferedReader(new FileReader(new File(inFilename)));
            PrintWriter out = new PrintWriter(new FileWriter(outFilename));
            out.print("Time\t");

            for (String variable1 : variables) {
                out.print(variable1 + "\t");
            }

            // Now reparse the file and write out each line of data, without
            // storing much.
            Map<String, Integer> indices = new HashMap<String, Integer>();

            for (int i = 0; i < variables.size(); i++) {
                indices.put(variables.get(i), i);
            }

            String[] row = new String[variables.size()];
            time = "";

            while ((line = reader.readLine()) != null) {
                Record record = ConvertUtils.parseRecord(line);

                String newTime = record.getTime();

                if (!time.equals(newTime)) {
//                    if (!"".equals(time)) {
//                        out.print("\n" + time + "\t");
//
//                        for (int j = 0; j < row.length; j++) {
//                            out.print(row[j] + "\t");
//                        }

//                        Arrays.fill(row, null);
//                    }

                    time = newTime;
//                    System.out.println(time);
                }

                String variable = record.getVariable();
                String datum = record.getDatum();

                int index = indices.get(variable);
                row[index] = datum;
            }

            out.print("\n" + time + "\t");

            for (String aRow : row) {
                out.print(aRow + "\t");
            }

            out.println();
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtest2() {

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));

            String line;

            while ((line = reader.readLine()) != null) {
                Record record = ConvertUtils.parseRecord(line);

                if (!"v00011".equals(record.getVariable())) {
                    continue;
                }

                System.out.println(line);

//                try {
//                    System.out.println(Double.parseDouble(record.getDatum()));
//                }
//                catch (NumberFormatException e) {
//                    System.out.println(record.getValue() + " " + record.getTime());
//                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtest3() {

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
//            String outFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.test_subset.txt";
            String outFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.test_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));
            PrintWriter writer =
                    new PrintWriter(new FileWriter(new File(outFilename)));

            String line;
            int n = -1;

            while ((line = reader.readLine()) != null) {
                ++n;

                if (n > 20000) {
                    break;
                }
//                Record record = ConvertUtils.parseRecord(line);

//                System.out.println(line);
                writer.println(line);

//                if (!"v38058".equals(record.getVariable())) {
//                    continue;
//                }

//                try {
//                    Double.parseDouble(datum);
//                }
//                catch (NumberFormatException e) {
//                    System.out.println(record.getValue() + " " + record.getTime());
//                }
            }

            reader.close();
            writer.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtest4() {
        Map<String, String> firstValue = new HashMap<String, String>();
        Set<String> nonConstantVariables = new HashSet<String>();

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));

            String line;
            int n = 0;

            while ((line = reader.readLine()) != null) {
                n++;
                if (n % 10000 == 0) {
                    System.out.println(n);
                }

                Record record = ConvertUtils.parseRecord(line);

                if (!"v38015".equals(record.getVariable())) {
                    continue;
                }

                try {
                    Double.parseDouble(record.getDatum());
                }
                catch (NumberFormatException e) {

                    // It's discrete?
                    if (!firstValue.containsKey(record.getVariable())) {
                        firstValue.put(record.getVariable(), record.getDatum());
                    }
                    else {
                        String _value = firstValue.get(record.getVariable());
                        if (!record.getDatum().equals(_value)) {
                            nonConstantVariables.add(record.getVariable());
                        }
                    }

                    System.out.println(record.getDatum());
                }
            }

            List<String> _nonConstantVariables =
                    new ArrayList<String>(nonConstantVariables);
            Collections.sort(_nonConstantVariables);

            for (int i = 0; i < _nonConstantVariables.size(); i++) {
                System.out.println(
                        (i + 1) + ". " + _nonConstantVariables.get(i));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtest5() {
        Map<String, Double> oldValue = new HashMap<String, Double>();
        Set<String> nonConstantVariables = new HashSet<String>();
        String firstTime = null;
        String lastTime = null;
        int numPts = 0;

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));
            String line;
            int n = 0;

            while ((line = reader.readLine()) != null) {
                n++;
                if (n % 10000 == 0) {
                    System.out.println(n);
                }

                Record record = ConvertUtils.parseRecord(line);

                if (firstTime == null) {
                    firstTime = record.getTime();
                }

                if (!record.getTime().equals(lastTime)) {
                    numPts++;
                }

                lastTime = record.getTime();

                try {
                    double dvalue = Double.parseDouble(record.getDatum());

                    System.out.println(record.getDatum());

                    if (Double.isNaN(dvalue)) {
                        continue;
                    }

                    if (!oldValue.containsKey(record.getVariable())) {
                        oldValue.put(record.getVariable(), dvalue);
                    }
                    else {
                        double d = oldValue.get(record.getVariable());

                        if (Math.abs(dvalue - d) > 1e-40) {
                            nonConstantVariables.add(record.getVariable());
                        }
                    }
                }
                catch (NumberFormatException e) {
                    // Skip--discrete or time.
                }
            }

            System.out.println("First time = " + firstTime);
            System.out.println("Last time = " + lastTime);
            System.out.println("Numpts = " + numPts);

            List<String> _nonConstantVariables =
                    new ArrayList<String>(nonConstantVariables);
            Collections.sort(_nonConstantVariables);

            for (int i = 0; i < _nonConstantVariables.size(); i++) {
                System.out.println(
                        (i + 1) + ". " + _nonConstantVariables.get(i));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtest6() {
        Map<String, Double> oldValue = new HashMap<String, Double>();
        Set<String> nonConstantVariables = new HashSet<String>();
        String firstTime = null;
        String lastTime = null;
        int numPts = 0;

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));

            String line;
            int n = 0;

            while ((line = reader.readLine()) != null) {
                n++;
                if (n % 10000 == 0) {
                    System.out.println(n);
                }

                Record record = ConvertUtils.parseRecord(line);

                if (firstTime == null) {
                    firstTime = record.getTime();
                }

                if (!record.getTime().equals(lastTime)) {
                    numPts++;
                }

                lastTime = record.getTime();

                try {
                    double dvalue = Double.parseDouble(record.getDatum());

                    System.out.println(record.getDatum());

                    if (Double.isNaN(dvalue)) {
                        continue;
                    }

                    if (!oldValue.containsKey(record.getVariable())) {
                        oldValue.put(record.getVariable(), dvalue);
                    }
                    else {
                        double d = oldValue.get(record.getVariable());

                        if (Math.abs(dvalue - d) > 1e-40) {
                            nonConstantVariables.add(record.getVariable());
                        }
                    }
                }
                catch (NumberFormatException e) {
                    // Skip--discrete or time.
                }
            }

            System.out.println("First time = " + firstTime);
            System.out.println("Last time = " + lastTime);
            System.out.println("Numpts = " + numPts);

            List<String> _nonConstantVariables =
                    new ArrayList<String>(nonConstantVariables);
            Collections.sort(_nonConstantVariables);

            for (int i = 0; i < _nonConstantVariables.size(); i++) {
                System.out.println(
                        (i + 1) + ". " + _nonConstantVariables.get(i));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rtest7() {
        Map<String, Object> oldValues = new HashMap<String, Object>();

        int[] numRecords = new int[60000];
        String[] lastValue = new String[60000];

        DataType[] dataTypes = new DataType[60000];

        boolean[] nonconstant = new boolean[60000];

        try {
//            String inFilename = "C:\\\\work\\proj\\shuttle\\shuttle1\\110_subset.txt";
            String inFilename =
                    "/home/jdramsey/proj/shuttle/shuttle1/110_subset.txt";
            BufferedReader reader =
                    new BufferedReader(new FileReader(new File(inFilename)));
            String line;
            int n = 0;

            while ((line = reader.readLine()) != null) {
                n++;

                if (n % 10000 == 0) {
                    System.out.println(n);
                }

                Record record = ConvertUtils.parseRecord(line);

                try {
                    int _var =
                            ConvertUtils.parseVarNumber(record.getVariable());
                    DataType dataType =
                            ConvertUtils.getDataType(record.getDatum());

                    numRecords[_var]++;
                    lastValue[_var] = record.getDatum();

                    if (dataTypes[_var] != null && dataTypes[_var] != dataType)
                    {
                        throw new IllegalArgumentException(
                                "Data type changed: " + _var);
                    }
                    else {
                        dataTypes[_var] = dataType;
                    }

                    if (dataTypes[_var] == DataType.CONTINUOUS) {
                        double dvalue = Double.parseDouble(record.getDatum());

                        if (Double.isNaN(dvalue)) {
                            continue;
                        }

                        if (!oldValues.containsKey(record.getVariable())) {
                            oldValues.put(record.getVariable(), dvalue);
                        }
                        else {
                            double d = (Double) oldValues.get(
                                    record.getVariable());

                            if (Math.abs(dvalue - d) > 0) {
                                nonconstant[_var] = true;
                            }
                        }
                    }
                    else if (dataTypes[_var] == DataType.DISCRETE ||
                            dataTypes[_var] == DataType.LAUNCH_TIME) {
                        if (!oldValues.containsKey(record.getVariable())) {
                            oldValues.put(record.getVariable(),
                                    record.getDatum());
                        }
                        else {
                            String _oldValue = (String) oldValues.get(
                                    record.getVariable());

                            if (!record.getDatum().equals(_oldValue)) {
                                nonconstant[_var] = true;
                            }
                        }
                    }
                }
                catch (NumberFormatException e) {
                    // Skip--discrete or time.
                }
            }

//            System.out.println("Numpts = " + numPts);

            System.out.println("Variable\tType\t# Records\tChanges?");

            for (int i = 0; i < numRecords.length; i++) {
                if (numRecords[i] == 0) {
                    continue;
                }

                if (lastValue[i] == null) {
                    continue;
                }

                if ("null".equals(lastValue[i])) {
                    continue;
                }

                DataType dataType = dataTypes[i];
                System.out.println(i + "\t" + dataType + "\t" + numRecords[i] +
                        "\t" + (nonconstant[i] ? "CHANGES" : "constant"));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestFoolingAround.class);
    }
}

