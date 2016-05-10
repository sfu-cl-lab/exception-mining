package edu.cmu.shuttle;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.ChoiceGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides tools for converting shuttle data from Choh Man's sparse (yet
 * readable to infinite Turing machines) format to a rectangular, tab-delimited
 * format that Tetrad can read. Choh Man's sparse format goes like this:
 * <p/>
 * <pre>
 * 98:20:30:32/48	v00002	-0:0:8:59
 * 98:20:30:32/48	v00003	15.759996
 * 98:20:30:32/48	v00004	15.279997
 * 98:20:30:32/48	v00331	199.19997
 * 98:20:30:32/48	v00332	201.59996
 * 98:20:30:32/48	v00333	-1.1999998
 * 98:20:30:32/48	v00339	0.0
 * 98:20:30:32/48	v00340	-1.1999998
 * 98:20:30:32/48	v00629	0.5999999
 * 98:20:30:32/48	v00765	0.081499994
 * 98:20:30:32/48	v00771	30.879993
 * ...
 * 98:20:30:32/97	v01768	OFF
 * 98:20:30:32/97	v01770	OFF
 * 98:20:30:32/97	v01772	OFF
 * ...
 * 98:20:30:32/319	v48978	null
 * 98:20:30:32/319	v48979	NaN
 * </pre>
 * There are no headers and no comment lines. The first column records the time
 * in day of year:hour:minute:second/millisecond format, at which a variable
 * datum is being recorded. The second column records the index of the variable
 * whose datum is being recorded. The third column records the datum of the
 * variable being recorded. The indices of the variables are linked to variable
 * descriptors in separate files. Variables are of three types: continuous,
 * discrete, and launch time. Continuous variables are 2-byte, with missing data
 * represented as NaN and Infinity permitted as a datum. Discrete variables are
 * all binary, with values ON/OFF, WET/DRY, or TRUE/FALSE, with null as a
 * missing datum indicator. Variable v00002 represents launch time, with values
 * of form (-?)0:0:minute:second, with negative values allowed. The delimiter is
 * whitespace.
 * <p/>
 * The target format is whitespace delimited, with an initial row of variable
 * names and unlabelled rows of data after that, one per case, with columns
 * corresponding to variable names in the first row. There are no comments.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5177 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
final class ShuttleDataConverter {

    /**
     * Returns a sorted list of all variable names in the given file.
     *
     * @throws FileNotFoundException if the given file cannot be found.
     * @throws IOException           if there is an error reading from the
     *                               file.
     */
    public static SortedSet<String> getVariables(File file)
            throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        SortedSet<String> variables = new TreeSet<String>();
        String line;

        while ((line = reader.readLine()) != null) {
            Record record = ConvertUtils.parseRecord(line);
            variables.add(record.getVariable());
        }

        reader.close();
        return variables;
    }

    /**
     * Returns the variables among those in the given list that are discrete in
     * the given file.
     *
     * @param file      A file in Choh Man's sparse format.
     * @param variables The initial list of variables of which the discrete
     *                  variables are needed.
     * @return The discrete variables.
     */
    public static SortedSet<String> getDiscreteVariables(File file,
            SortedSet<String> variables)
            throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SortedSet<String> _variables = new TreeSet<String>(variables);

        String line;

        while ((line = reader.readLine()) != null) {
            Record record = ConvertUtils.parseRecord(line);
            String variable = record.getVariable();

            if (_variables.contains(variable)) {
                String datum = record.getDatum();

                if (!ConvertUtils.isDiscreteDatum(datum)) {
                    _variables.remove(variable);
                }
            }
        }

        reader.close();
        return _variables;
    }

    /**
     * Returns the variables among those in the given list that are continuous
     * in the given file.
     *
     * @param file      A file in Choh Man's sparse format.
     * @param variables The initial list of variables of which the continuous
     *                  variables are needed.
     * @return The discrete variables.
     */
    public static SortedSet<String> getContinuousVariables(File file,
            SortedSet<String> variables)
            throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SortedSet<String> _variables = new TreeSet<String>(variables);

        String line;

        while ((line = reader.readLine()) != null) {
            Record record = ConvertUtils.parseRecord(line);
            String variable = record.getVariable();

            if (_variables.contains(variable)) {
                String datum = record.getDatum();

                if (!ConvertUtils.isContinuousDatum(datum)) {
                    _variables.remove(variable);
                }
            }
        }

        reader.close();
        return _variables;
    }

    /**
     * Writes names of continuous variables to the given file, one per line.
     */
    public static void writeContinuousVariableNames(File inFile, File outFile)
            throws IOException {
        SortedSet<String> variables = getVariables(inFile);
        SortedSet<String> continuousVariables =
                getContinuousVariables(inFile, variables);
        PrintWriter writer = new PrintWriter(new FileWriter(outFile));

        for (String continuousVariable : continuousVariables) {
            writer.println(continuousVariable);
        }

        writer.close();

    }

    /**
     * Writes names of discrete variables to the given file, one per line.
     */
    public static void writeDiscreteVariableNames(File inFile, File outFile)
            throws IOException {
        SortedSet<String> variables = getVariables(inFile);
        SortedSet<String> discreteVariable =
                getDiscreteVariables(inFile, variables);
        PrintWriter writer = new PrintWriter(new FileWriter(outFile));

        for (String aDiscreteVariable : discreteVariable) {
            writer.println(aDiscreteVariable);
        }

        writer.close();
    }

    /**
     * Reads variables names from the given file, one per line. Assumes the
     * variable name is at the beginning of the line and there is nothing else
     * on each line.
     */
    public static SortedSet<String> readVariableNames(File file)
            throws FileNotFoundException, IOException {
        SortedSet<String> variableNames = new TreeSet<String>();
        BufferedReader in = new BufferedReader(new FileReader(file));

        String name;

        while ((name = in.readLine()) != null) {
            if ("".equals(name)) {
                continue;
            }

            variableNames.add(name);
        }

        return variableNames;
    }

    /**
     * Reads in data from the given file in Choh Man's format. The form of the
     * data set is fixed in advance. The variables in the data set are read in
     * from <code>file</code>. The variable must all be continuous. Cases in the
     * returned data set begin with <code>beginTime</code>, increment in time by
     * <code>interval</code>, and do not exceed in time <code>endTime</code>.
     * All of these times must be values returned by ConvertUtils.parseYearTime().
     * In each time interval, the last value recorded for a variable before the
     * end of the time interval will be entered into the data set for that time
     * interval, for that variable.
     *
     * @throws IllegalArgumentException if a variable does not record data of
     *                                  the given type.
     */
    public static RectangularDataSet readContinuousData(File file,
            SortedSet<String> variables, long beginTime, long endTime,
            long interval) throws FileNotFoundException, IOException {
        int rows = (int) ((endTime - beginTime) / interval);
        int cols = variables.size();

        List<String> variableList = new ArrayList<String>(variables);
        List<Node> _variables = new ArrayList<Node>();

        for (String variable : variableList) {
            _variables.add(new ContinuousVariable(variable));
        }

        // The data set to be returned.
        RectangularDataSet data = new ColtDataSet(rows, _variables);

        // Holds one row. Updated for a variable whenever a new datum for it
        // is read.
        double[] _case = new double[cols];

        // Go through the data set, and for each record:
        // 1. If it's past the end of the interval, write out the case and
        // go to the next row.
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        long intervalBegin = beginTime;
        int row = 0;

        while ((line = reader.readLine()) != null) {
            Record record = ConvertUtils.parseRecord(line);
            String variable = record.getVariable();

            if (!variables.contains(variable)) {
                continue;
            }

            long time = ConvertUtils.parseYearTime(record.getTime());

            while (time > intervalBegin + interval) {
                writeCase(data, row, _case);
                intervalBegin += interval;
                row++;
            }

            if (time >= endTime) {
                break;
            }

            double datum = Double.parseDouble(record.getDatum());
            int col = variableList.indexOf(variable);

            _case[col] = datum;
        }

        reader.close();

        return data;
    }

    public static String removeCollinearities(RectangularDataSet data,
            int depth) {
        System.out.println("num cols = " + data.getNumColumns());

        for (int d = 1; d <= depth; d++) {

            VARIABLE:
            for (int i = d - 1; i < data.getNumColumns(); i++) {
                ChoiceGenerator choiceGenerator = new ChoiceGenerator(i, d - 1);
                int[] indices = new int[d];
                int[] choice;

                while ((choice = choiceGenerator.next()) != null) {
                    System.arraycopy(choice, 0, indices, 0, choice.length);
                    indices[indices.length - 1] = i;

                    CovarianceMatrix cov =
                            new CovarianceMatrix(data.subsetColumns(indices));
                    DoubleMatrix2D matrix = cov.getMatrix();

                    if (new Algebra().rank(matrix) < d) {
                        StringBuffer buf = new StringBuffer();
                        buf.append(data.getVariable(i));
                        buf.append(" removed because rank(cov(<");

                        for (int m = 0; m < indices.length; m++) {
                            buf.append(data.getVariable(indices[m]));

                            if (m < indices.length - 1) {
                                buf.append(", ");
                            }
                        }

                        buf.append(">) < ").append(d);

                        System.out.println(buf);

                        data.removeColumn(i);
                        i--;
                        continue VARIABLE;
                    }
                }
            }
        }

        System.out.println("num cols = " + data.getNumColumns());

        return "";
    }

    private static void writeCase(RectangularDataSet data, int row,
            double[] _case) {
        for (int col = 0; col < _case.length; col++) {
            data.setDouble(row, col, _case[col]);
        }
    }
}
