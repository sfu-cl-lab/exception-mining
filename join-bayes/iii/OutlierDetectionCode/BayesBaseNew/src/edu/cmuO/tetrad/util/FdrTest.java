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

package edu.cmu.tetrad.util;

import java.util.Arrays;

/**
 * <p>Implements FDR ("False Discovery Rate") test. To calculate FDR for a set
 * of p-values, sort the p-values low to high and arrange them along the x-axis
 * at points 0.0, 1/n, 2/n, ..., 1.0 as vertical line segments for each p-value
 * p from y = 0 to y = p. Draw the line segment from (0, 0) to (1, alpha) for
 * uncorrelated p-values. (For correlated p-values substitute
 * alpha/(SUM_i=1^n(1/i) for alpha). Find the first p-value going right to left
 * (x = 1 to x = 0) which falls below this line. Return this p-value as a
 * cutoff. </p> <p>References: <ul> <li>Benjamini, Yaov and Daniel Yekutieli,
 * "The control of the false discovery rate in multiple testing under
 * dependency." Part of Ph.D. dissertation of D. Yekutieli at Tel Aviv
 * University. <li>Benjamini, Y. and Hochberg, Y. (1995). "Controlling the false
 * discovery rate: a practical and powerful approach to multiple testing.,"
 * Journal of the Royal Statistical Society B 57 289-300. </ul> </p> NOTE: This
 * class has not been rigorously tested yet. -JR 5/4/01
 *
 * @author Joseph Ramsey
 * @version $Revision: 5308 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class FdrTest {

    /**
     * Constructs a new FDR test for significance level 0.05.
     */
    public FdrTest() {
        this(0.05);
    }

    /**
     * Constructs a new FDR test for the given significance level.
     *
     * @param alpha the significance level, 0 <= alpha <= 1.
     */
    public FdrTest(double alpha) {
        setAlpha(alpha);
    }

    /**
     * Sets the significance level to the given value.
     */
    public void setAlpha(double alpha) {
        if ((alpha >= 0.0) && (alpha <= 1.0)) {
            this.alpha = alpha;
        }
    }

    /**
     * Returns the significance level.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Calculates the cutoff value for p-values using the FDR method.
     * Hypotheses with p-values less than or equal to this cutoff should be
     * rejected according to the test.
     *
     * @param pValues    An array containing p-values to be tested in positions
     *                   0, 1, ..., n. (The rest of the array is ignored.)
     *                   <i>Note:</i> This array will not be changed by this
     *                   class. Its values are copied into a separate array
     *                   before sorting.
     * @param n          The number of p-values in the array <code>pValues</code>.
     * @param correlated Whether the p-values in the array <code>pValues </code>
     *                   are correlated (true if yes, false if no). If they are
     *                   uncorrelated, a level of alpha is used; if they are not
     *                   correlated, a level of alpha / SUM_i=1_n(1 / i) is
     *                   used.
     * @return the cutoff value, which is the first p-value sorted high to
     *         low to fall below a line from (1.0, level) to (0.0, 0.0).
     *         Hypotheses less than or equal to this p-value should be
     *         rejected.
     */
    public double calcCutoff(double[] pValues, int n, boolean correlated) {
        if (n < pValues.length) {
            throw new IllegalArgumentException("Number of p-values " +
                    "stated is greater " + "than the size of the " +
                    "p-value array.");
        }

        // Copy the p-values to another array so the order is not
        // disturbed.
        if ((sorted == null) || (sorted.length < n)) {
            sorted = new double[n];
        }

        System.arraycopy(pValues, 0, sorted, 0, n);

        // Sort the p-values
        Arrays.sort(sorted, 0, n - 1);

        // Malculate where the discriminant line segment crosses x = 1.0.
        // (At x = 0.0, it crosses the origin.)
        // Note that this differs depending on whether the p-values
        // are correlated or not. If they're uncorrelated, the value
        // alpha is used. If they're correlated, the value
        // alpha / SUM_i=1^n(1/i) is used.
        double m = 0.0;

        if (correlated) {
            for (int i = 1; i <= n; i++) {
                m += 1.0 / i;
            }
        }
        else {
            m = alpha;
        }

        // Iterate through the p-values in high-to-low sorting to
        // determine which is the first to dip down below this line.
        // note that adjustments have to be made for zero-indexing.
        int i = n;

        while (--i > 0) {
            if (sorted[i] < m * ((double) (i + 1) / (double) n)) {
                break;
            }
        }

        // Return the p-value which is the first to dip down below
        // this line.
        return sorted[i];
    }

    /**
     * An array to contain the p-values tested, but in sorted order, low to
     * high.
     */
    private double[] sorted;

    /**
     * The significance level of the test, assuming uncorrelated p-values. (For
     * correlated p-values, a value of alpha / SUM_i=1^n(1/i) is used.
     */
    private double alpha;
}


