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

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.util.CombinationIterator;
import edu.cmu.tetrad.util.ProbUtils;

import java.util.Arrays;


/**
 * Performs conditional independence tests of discrete data using the Chi Square
 * method. Degrees of freedom are calculated as in Fienberg, The Analysis of
 * Cross-Classified Categorical Data, 2nd Edition, 142.
 *
 * @author Frank Wimberly original version
 * @author Joseph Ramsey revision 10/01
 * @version $Revision: 6523 $
 */
public final class ChiSquareTest2 {

    /**
     * The data set this test uses.
     */
    private RectangularDataSet dataSet;

    /**
     * The number of values for each variable in the data.
     */
    private int[] dims;

    /**
     * The significance level of the test.
     */
    private double alpha = 0.05;

    public ChiSquareTest2(RectangularDataSet dataSet, double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance level must be " +
                    "between 0.0 and 1.0: " + alpha);
        }

        this.dims = new int[dataSet.getNumColumns()];

        for (int i = 0; i < dims.length; i++) {
            DiscreteVariable variable =
                    (DiscreteVariable) dataSet.getVariable(i);
            this.dims[i] = variable.getNumCategories();
        }

        this.dataSet = dataSet;
        this.alpha = alpha;
    }

    /**
     * Calculates chi square for a conditional crosstabulation table for
     * independence question 0 _||_ 1 | 2, 3, ...max by summing up chi square
     * and degrees of freedom for each conditional table in turn, where rows or
     * columns that consist entirely of zeros have been removed.
     */
    public ChiSquareTest.Result calcChiSquare(int[] testIndices) {

        // Indicator arrays to tell the cell table which margins
        // to calculate. For x _||_ y | z1, z2, ..., we want to
        // calculate the margin for x, the margin for y, and the
        // margin for x and y. (These will be used later.)
        double xSquare = 0.0;
        int df = 0;

        int[] condDims = new int[testIndices.length - 2];
        System.arraycopy(selectFromArray(dims, testIndices), 2, condDims, 0,
                condDims.length);

        int[] coords = new int[testIndices.length];
        int numRows = totalCells(dataSet, testIndices[0]);
        int numCols = totalCells(dataSet, testIndices[1]);

        boolean[] attestedRows = new boolean[numRows];
        boolean[] attestedCols = new boolean[numCols];

        CombinationIterator combinationIterator =
                new CombinationIterator(condDims);

        while (combinationIterator.hasNext()) {
            int[] combination = (int[]) combinationIterator.next();

            System.arraycopy(combination, 0, coords, 2, combination.length);
            Arrays.fill(attestedRows, true);
            Arrays.fill(attestedCols, true);

            long total = calcMargin(dataSet, testIndices, coords, testIndices[0], testIndices[1]);

            if (total == 0) {
                continue;
            }

            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    coords[0] = i;
                    coords[1] = j;

                    long sumRow = calcMargin(dataSet, testIndices, coords, i);
                    long sumCol = calcMargin(dataSet, testIndices, coords, j);
                    long observed = calcCellValue(dataSet, testIndices, coords);

                    boolean skip = false;

                    if (sumRow == 0) {
                        attestedRows[i] = false;
                        skip = true;
                    }

                    if (sumCol == 0) {
                        attestedCols[j] = false;
                        skip = true;
                    }

                    if (skip) {
                        continue;
                    }

                    double expected =
                            (double) (sumCol * sumRow) / (double) total;
                    xSquare += Math.pow(observed - expected, 2.0) / expected;
                }
            }

            int numAttestedRows = 0;
            int numAttestedCols = 0;

            for (boolean attestedRow : attestedRows) {
                if (attestedRow) {
                    numAttestedRows++;
                }
            }

            for (boolean attestedCol : attestedCols) {
                if (attestedCol) {
                    numAttestedCols++;
                }
            }

            df += (numAttestedRows - 1) * (numAttestedCols - 1);
        }

        // If df == 0, return indep.
        if (df == 0) {
            df = 1;
        }

        double pValue = 1.0 - ProbUtils.chisqCdf(xSquare, df);
        boolean indep = (pValue > this.alpha);
        return new ChiSquareTest.Result(xSquare, pValue, df, indep);
    }

    private long calcMargin(RectangularDataSet dataSet, int[] testIndices, int[] coords, int...marginalVars) {
        int count = 0;

        ROW:
        for (int i = 0; i < dataSet.getNumRows(); i++) {

            COLUMNS:
            for (int j = 0; j < coords.length; j++) {
                for (int k = 0; i < marginalVars.length; k++) {
                    if (j == k) {
                        continue COLUMNS;
                    }
                }

                if (dataSet.getInt(i, testIndices[j]) != coords[j]) {
                    continue ROW;
                }
            }

            count++;
        }

        return count;
    }

    private long calcCellValue(RectangularDataSet dataSet, int[] testIndices, int[] coords) {
        int count = 0;

        ROW:
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            for (int j = 0; j < coords.length; j++) {
                if (dataSet.getInt(i, testIndices[j]) != coords[j]) {
                    continue ROW;
                }
            }

            count++;
        }

        return count;
    }

    private int totalCells(RectangularDataSet dataSet, int...testIndices) {
        int product = 1;

        for (int index : testIndices) {
            product *= dims[index];
        }

        return product;
    }

    public boolean isDetermined(int[] testIndices, double p) {

        // Indicator arrays to tell the cell table which margins
        // to calculate. For x _||_ y | z1, z2, ..., we want to
        // calculate the margin for x, the margin for y, and the
        // margin for x and y. (These will be used later.)
        int[] firstVar = new int[]{0};

        int[] condDims = new int[testIndices.length - 1];
        System.arraycopy(selectFromArray(dims, testIndices), 1, condDims, 0,
                condDims.length);

        int[] coords = new int[testIndices.length];
        int numValues = totalCells(dataSet, testIndices[0]);

        CombinationIterator combinationIterator =
                new CombinationIterator(condDims);

        while (combinationIterator.hasNext()) {
            int[] combination = (int[]) combinationIterator.next();
            System.arraycopy(combination, 0, coords, 1, combination.length);

            long total = calcMargin(dataSet, testIndices, coords, 0);

            if (total == 0) {
                continue;
            }

            boolean dominates = false;

            for (int i = 0; i < numValues; i++) {
                coords[0] = i;

                long numi = calcCellValue(dataSet, testIndices, coords);

                if ((double) numi / total >= p) {
                    dominates = true;
                }
            }

            if (!dominates) {
                return false;
            }
        }

        return true;
    }

    public boolean isSplitDetermined(int[] testIndices, double p) {

        // Indicator arrays to tell the cell table which margins
        // to calculate. For x _||_ y | z1, z2, ..., we want to
        // calculate the margin for x, the margin for y, and the
        // margin for x and y. (These will be used later.)
        int[] firstVar = new int[]{0};
        int[] secondVar = new int[]{1};
        int[] bothVars = new int[]{0, 1};

        int[] condDims = new int[testIndices.length - 2];
        System.arraycopy(selectFromArray(dims, testIndices), 2, condDims, 0,
                condDims.length);

        int[] coords = new int[testIndices.length];
        int numRows = totalCells(dataSet, testIndices[0]);
        int numCols = totalCells(dataSet, testIndices[1]);


        boolean[] attestedRows = new boolean[numRows];
        boolean[] attestedCols = new boolean[numCols];

        CombinationIterator combinationIterator =
                new CombinationIterator(condDims);

        while (combinationIterator.hasNext()) {
            int[] combination = (int[]) combinationIterator.next();

            System.arraycopy(combination, 0, coords, 2, combination.length);
            Arrays.fill(attestedRows, true);
            Arrays.fill(attestedCols, true);

            long total = calcMargin(dataSet, testIndices, coords, 0, 1);

            if (total == 0) {
                continue;
            }

            // For every table, some marginal has to dominate, either a row
            // marginal or a column marginal.
            boolean dominates = false;

            marginals:
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    coords[0] = i;
                    coords[1] = j;

                    long sumRow = calcMargin(dataSet, testIndices, coords, 1);
                    long sumCol = calcMargin(dataSet, testIndices, coords, 0);

                    if ((double) sumRow / total >= 0.99) {
                        dominates = true;
                        break marginals;
                    }

                    if ((double) sumCol / total >= 0.99) {
                        dominates = true;
                        break marginals;
                    }
                }
            }

            if (!dominates) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the current significance level being used for tests.
     */
    public double getAlpha() {
        return this.alpha;
    }

    /**
     * Sets the significance level to be used for tests.
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    private int[] selectFromArray(int[] arr, int[] indices) {
        int[] retArr = new int[indices.length];

        for (int i = 0; i < indices.length; i++) {
            retArr[i] = arr[indices[i]];
        }

        return retArr;
    }

    /**
     * Simple class to store the parameters of the result returned by the G
     * Square test.
     *
     * @author Frank Wimberly
     * @version $Revision: 6523 $ $Date: 2006-01-10 17:24:59 -0500 (Tue, 10 Jan
     *          2006) $
     */
    public class Result {

        /**
         * The g square value itself.
         */
        private double gSquare;

        /**
         * The pValue of the result.
         */
        private double pValue;

        /**
         * The adjusted degrees of freedom.
         */
        private int df;

        /**
         * Whether the conditional independence holds or not. (True if it does,
         * false if it doesn't.
         */
        private boolean isIndep;

        /**
         * Constructs a new g square result using the given parameters.
         */
        public Result(double gSquare, double pValue, int df, boolean isIndep) {
            this.gSquare = gSquare;
            this.pValue = pValue;
            this.df = df;
            this.isIndep = isIndep;
        }

        public double getXSquare() {
            return gSquare;
        }

        public double getPValue() {
            return pValue;
        }

        public int getDf() {
            return df;
        }

        public boolean isIndep() {
            return isIndep;
        }
    }
}