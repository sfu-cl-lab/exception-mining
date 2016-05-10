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

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Some utilities for handling arrays.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-05 17:09:26 -0500 (Thu, 05 Jan
 *          2006) $
 */
public final class ArrUtils {

    //=========================PUBLIC METHODS===========================//

    /**
     * Copies a 2D double arr.
     */
    public static double[] copy(double[] arr) {
        if (arr == null) {
            return null;
        }

        double[] copy = new double[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);

        return copy;
    }

    /**
     * Copies a 2D double arr.
     */
    public static int[] copy(int[] arr) {
        if (arr == null) {
            return null;
        }

        int[] copy = new int[arr.length];
        System.arraycopy(arr, 0, copy, 0, arr.length);

        return copy;
    }

    /**
     * Tests two vectors for equality.
     */
    public static boolean equals(double[] va, double[] vb) {
        return withinTolerance(va, vb, 0.0);
    }

    /**
     * Tests to see whether two vectors are equal within the given tolerance. If
     * any two corresponding elements differ by more than the given tolerance,
     * false is returned.
     */
    public static boolean withinTolerance(double[] va, double[] vb,
            double tolerance) {
        if (va.length != vb.length) {
            throw new IllegalArgumentException(
                    "Incompatible matrix dimensions.");
        }

        if (tolerance < 0.0) {
            throw new IllegalArgumentException(
                    "Tolerance must be >= 0.0: " + tolerance);
        }

        for (int i = 0; i < va.length; i++) {
            if (Math.abs(va[i] - vb[i]) > tolerance) {
                return false;
            }
        }

        return true;
    }

    /**
     * Concatenates the vectors rows[i], i = 0...rows.length, into a single
     * vector.
     */
    public static double[] concatenate(double[] arr1, double[] arr2) {
        double[] concat = new double[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, concat, 0, arr1.length);
        System.arraycopy(arr2, 0, concat, arr1.length, arr2.length);
        return concat;
    }

    /**
     * Returns the vector as a 1 x n row matrix.
     */
    public static double[][] asRow(double[] v) {
        double[][] arr = new double[1][v.length];
        System.arraycopy(v, 0, arr[0], 0, v.length);
        return arr;
    }

    /**
     * Returns the vector as an n x 1 column matrix.
     */
    public static double[][] asCol(double[] v) {
        double[][] arr = new double[v.length][1];
        for (int i = 0; i < v.length; i++) {
            arr[i][0] = v[i];
        }
        return arr;
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with a tab character. The number format
     * is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(double[] arr) {
        NumberFormat nf = new DecimalFormat(" 0.0000;-0.0000");
        return toString(arr, nf);
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with a tab character. The number format
     * is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(int[] arr) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n");
        for (int anArr : arr) {
            buf.append(anArr).append("\t");
        }
        return buf.toString();
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with the given lineInit. The number
     * format is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(double[] arr, NumberFormat nf) {
        return toString(arr, nf, "\t");
    }

    /**
     * Copies the given array, using the given number formatter and starting
     * each line with the given liniInit. (For scientific notation, try
     * DecimalFormat(" 0.000E0;-0.000E0").)
     */
    public static String toString(double[] arr, NumberFormat nf,
            String lineInit) {
        if (nf == null) {
            throw new NullPointerException("NumberFormat must not be null.");
        }
        if (lineInit == null) {
            throw new NullPointerException("LineInit must not be null.");
        }

        if (arr == null) {
            return nullMessage(lineInit);
        }
        else {
            StringBuffer buf = new StringBuffer();
            buf.append("\n");
            buf.append(lineInit);

            for (double anArr : arr) {
                buf.append(nf.format(anArr)).append("\t");
            }
            return buf.toString();
        }
    }

    //=========================PRIVATE METHODS===========================//


    private static String nullMessage(String lineInit) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n");
        buf.append(lineInit);
        buf.append("<Matrix is null>");
        return buf.toString();
    }
}


