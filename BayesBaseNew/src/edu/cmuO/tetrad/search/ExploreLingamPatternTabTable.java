package edu.cmu.tetrad.search;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 16, 2008 Time: 9:27:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExploreLingamPatternTabTable {

    private String header;
    private DenseDoubleMatrix2D data;
    private String[] columnNames;
    private double[] means;
    private double[] standardDeviations;
    private int[][] numDecimals;
    private int maxColumnNamesRows;

    public ExploreLingamPatternTabTable(int rows, String[] columnNames) {
        int columns = columnNames.length;
        this.header = "No Header";
        this.data = new DenseDoubleMatrix2D(rows, columns);
        this.numDecimals = new int[rows][columns];
        means = new double[columns];
        standardDeviations = new double[columns];
        setColumnNames(columnNames);
    }

    //=========================PUBLIC METHODS=============================//

    public void writeRecord(int line, int col, double value, int numDecimals) {
        this.numDecimals[line][col] = numDecimals;
        data.set(line, col, value);
    }

    public int getNumLines() {
        return getNumHeaderRows() + maxColumnNamesRows + data.rows() + 2;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getLine(int lineNumber) {

        // Headers.
        if (lineNumber < getNumHeaderRows()) {
            StringBuffer buf = new StringBuffer();
            buf.append(getHeader(lineNumber));
            buf.append("\t");
            
            for (int j = 0; j < data.columns(); j++) {
                buf.append("\t");
            }

            return buf.toString();
        }

        // Column names (multi-row)
        else if (lineNumber < getNumHeaderRows() + maxColumnNamesRows) {
            StringBuffer buf = new StringBuffer();
            buf.append("\t");

            for (int j = 0; j < data.columns(); j++) {
                String token = getColumnNameRow(lineNumber - getNumHeaderRows(), j);
                buf.append(token == null ? "" : token);
                buf.append("\t");
            }

            return buf.toString();
        }

        // Data
        else
        if (lineNumber < getNumHeaderRows() + maxColumnNamesRows + data.rows()) {
            StringBuffer buf = new StringBuffer();
            int rowNumber = lineNumber - getNumHeaderRows() - maxColumnNamesRows + 1;

            buf.append(rowNumber);
            buf.append("\t");

            for (int j = 0; j < data.columns(); j++) {
                int row = lineNumber - getNumHeaderRows() - maxColumnNamesRows;
                buf.append(format(data.get(row, j), numDecimals[row][j]));
                buf.append("\t");
            }

            return buf.toString();
        }

        // Means
        else
        if (lineNumber == getNumHeaderRows() + maxColumnNamesRows + data.rows()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Mean\t");
            calculateStatistics();

            for (int j = 0; j < data.columns(); j++) {
                buf.append(format(means[j], 1));
                buf.append("\t");
            }

            return buf.toString();
        }

        // Standard deviations
        else
        if (lineNumber < getNumHeaderRows() + maxColumnNamesRows + data.rows() + 2) {
            StringBuffer buf = new StringBuffer();
            buf.append("SD\t");
            calculateStatistics();

            for (int j = 0; j < data.columns(); j++) {
                buf.append(format(standardDeviations[j], 1));
                buf.append("\t");
            }

            return buf.toString();
        }

        throw new IllegalArgumentException("No such line: " + lineNumber);
    }

    public void calculateStatistics() {
        for (int j = 0; j < data.columns(); j++) {
            DoubleArrayList column = new DoubleArrayList();

            for (int i = 0; i < data.rows(); i++) {
                if (!Double.isNaN(data.get(i, j))) {
                    column.add(data.get(i, j));
                }
            }

            double mean = Descriptive.mean(column);
            means[j] = mean;

            double variance = Descriptive.variance(column.size(), Descriptive.sum(column),
                    Descriptive.sumOfSquares(column));
            double sd = Descriptive.standardDeviation(variance);
            standardDeviations[j] = sd;
        }
    }

    public double getMean(int column) {
        return means[column];
    }

    public double getStandardDeviation(int column) {
        return standardDeviations[column];
    }

    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();

        for (int line = 0; line < getNumLines(); line++) {
            buf.append(getLine(line));
            buf.append("\n");
        }

        return buf.toString();
    }

    public Object getHeader(int j) {
        String[] headerLines = header.split("/");
        return j < headerLines.length ? headerLines[j] : "";
    }

    public int getNumHeaderRows() {
        String[] headerLines = header.split("/");
        return headerLines.length;
    }

    //=========================PRIVATE METHODS=============================//

    private String format(double value, int numDecimals) {
        if (numDecimals < 0) throw new IllegalArgumentException();

        if (Double.isNaN(value)) return "-";

        StringBuffer format = new StringBuffer("0.");

        for (int i = 0; i < numDecimals; i++) {
            format.append("#");
        }

        NumberFormat nf = new DecimalFormat(format.toString());

        return nf.format(value);
    }

    private void setColumnNames(String[] columnNames) {
        if (columnNames.length != data.columns()) {
            throw new IllegalArgumentException("Number of columns names " +
                    "does not match number of columns.");
        }

        this.columnNames = columnNames;
        this.maxColumnNamesRows = 0;

        for (int j = 0; j < columnNames.length; j++) {
            int numRows = getColumnNameRows(j).length;
            if (numRows > maxColumnNamesRows) maxColumnNamesRows = numRows;
        }
    }

    private String getColumnNameRow(int i, int j) {
        final String[] rows = getColumnNameRows(j);
//        System.out.println("i = " + i + " j = " + j + " max = " + maxColumnNamesRows + " # rows = " + rows.length);

        if (i < maxColumnNamesRows - rows.length) {
            return "";
        }
        else if (i < maxColumnNamesRows) {
            return rows[(i -(maxColumnNamesRows - rows.length))];
        }
        else {
            return "";
        }
    }

    private String[] getColumnNameRows(int j) {
        return columnNames[j].split("/");
    }

}
