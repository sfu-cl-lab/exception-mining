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


/**
 * Contains a number of basic statistical functions. Most methods are overloaded
 * for either long or double arrays. </p> </p> NOTE: </p> Some methods in this
 * class have been adapted from class DStat written by Michael Fanelli, and the
 * routines have been included here by permission. The methods which were
 * adapted are: <ul> <li>gamma <li>internalGamma <li>beta <li>igamma <li>erf
 * <li>poisson <li>chidist <li>contTable1 </ul> </p> These methods are protected
 * under copyright by the author. Here is the text of his copyright notice for
 * DSTAT.java: </p> "Copyright 1997 by Michael Fanelli. All Rights Reserved.
 * Unlimited use of this beta code granted for non-commercial use only subject
 * to the the expiration date. Commercial (for profit) use requires written
 * permission."
 *
 * @author Joseph Ramsey
 * @version $Revision: 5994 $ $Date: 2005-12-31 22:05:47 -0500 (Sat, 31 Dec
 *          2005) $
 */
public final class StatUtils extends Object {

    /**
     * @param array a long array.
     * @return the mean of the values in this array.
     */
    public static double mean(long[] array) {
        return mean(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the mean of the values in this array.
     */
    public static double mean(double[] array) {
        return mean(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the mean of the first N values in this array.
     */
    public static double mean(long array[], int N) {

        int i;
        long sum = 0;

        for (i = 0; i < N; i++) {
            sum += array[i];
        }

        return sum / N;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the mean of the first N values in this array.
     */
    public static double mean(double array[], int N) {
        double sum = 0.0;

        for (int i = 0; i < N; i++) {
            sum += array[i];
        }

        return sum / N;
    }

    /**
     * @param array a long array.
     * @return the median of the values in this array.
     */
    public static double median(long[] array) {
        return median(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the median of the values in this array.
     */
    public static double median(double[] array) {
        return median(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the median of the first N values in this array.
     */
    public static long median(long array[], int N) {

        long a[] = new long[N + 1];

        System.arraycopy(array, 0, a, 0, N);

        a[N] = Long.MAX_VALUE;

        long v, t;
        int i, j, l = 0;
        int r = N - 1, k1 = r / 2, k2 = r - k1;

        while (r > l) {
            v = a[l];
            i = l;
            j = r + 1;

            for (; ;) {
                while (a[++i] < v) {
                    ;
                }

                while (a[--j] > v) {
                    ;
                }

                if (i >= j) {
                    break;
                }

                t = a[i];
                a[i] = a[j];
                a[j] = t;
            }

            t = a[j];
            a[j] = a[l];
            a[l] = t;

            if (j <= k1) {
                l = j + 1;
            }

            if (j >= k2) {
                r = j - 1;
            }
        }

        return (a[k1] + a[k2]) / 2;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the median of the first N values in this array.
     */
    public static double median(double array[], int N) {

        double a[] = new double[N + 1];

        System.arraycopy(array, 0, a, 0, N);

        a[N] = Double.POSITIVE_INFINITY;

        double v, t;
        int i, j, l = 0;
        int r = N - 1, k1 = r / 2, k2 = r - k1;

        while (r > l) {
            v = a[l];
            i = l;
            j = r + 1;

            for (; ;) {
                while (a[++i] < v) {
                    ;
                }

                while (a[--j] > v) {
                    ;
                }

                if (i >= j) {
                    break;
                }

                t = a[i];
                a[i] = a[j];
                a[j] = t;
            }

            t = a[j];
            a[j] = a[l];
            a[l] = t;

            if (j <= k1) {
                l = j + 1;
            }

            if (j >= k2) {
                r = j - 1;
            }
        }

        return (a[k1] + a[k2]) / 2;
    }

    /**
     * @param array          a long array.
     * @param quartileNumber 1, 2, or 3.
     * @return the requested quartile of the values in this array.
     */
    public static double quartile(long array[], int quartileNumber) {
        return quartile(array, array.length, quartileNumber);
    }

    /**
     * @param array          a double array.
     * @param quartileNumber 1, 2, or 3.
     * @return the requested quartile of the values in this array.
     */
    public static double quartile(double array[], int quartileNumber) {
        return quartile(array, array.length, quartileNumber);
    }

    /**
     * @param array          a long array.
     * @param N              the number of values of array which should be
     *                       considered.
     * @param quartileNumber 1, 2, or 3.
     * @return the requested quartile of the first N values in this array.
     */
    public static double quartile(long array[], int N, int quartileNumber) {

        if ((quartileNumber < 1) || (quartileNumber > 3)) {
            throw new IllegalArgumentException("StatUtils.quartile:  " +
                    "Quartile number must be 1, 2, or 3.");
        }

        long a[] = new long[N + 1];

        System.arraycopy(array, 0, a, 0, N);

        a[N] = Long.MAX_VALUE;

        long v, t;
        int i, j, l = 0;
        int r = N - 1;

        // find the two indexes k1 and k2 (possibly equal) which need
        // to be interpolated to get the quartile, being careful to
        // zero-index.
        double doubleIndex = (quartileNumber / 4.0) * (N + 1.0) - 1;
        double ratio = doubleIndex - (int) (doubleIndex);
        int k1 = (int) Math.floor(doubleIndex);
        int k2 = (int) Math.ceil(doubleIndex);

        // partially sort array a[] to find k1 and k2
        while (r > l) {
            v = a[l];
            i = l;
            j = r + 1;

            for (; ;) {
                while (a[++i] < v) {
                    ;
                }

                while (a[--j] > v) {
                    ;
                }

                if (i >= j) {
                    break;
                }

                t = a[i];
                a[i] = a[j];
                a[j] = t;
            }

            t = a[j];
            a[j] = a[l];
            a[l] = t;

            if (j <= k1) {
                l = j + 1;
            }

            if (j >= k2) {
                r = j - 1;
            }
        }

        // return the interpolated value.
        return (a[k1] + ratio * (a[k2] - a[k1]));
    }

    /**
     * @param array          a double array.
     * @param N              the number of values of array which should be
     *                       considered.
     * @param quartileNumber 1, 2, or 3.
     * @return the requested quartile of the first N values in this array.
     */
    public static double quartile(double array[], int N, int quartileNumber) {

        if ((quartileNumber < 1) || (quartileNumber > 3)) {
            throw new IllegalArgumentException("StatUtils.quartile:  " +
                    "Quartile number must be 1, 2, or 3.");
        }

        double a[] = new double[N + 1];

        System.arraycopy(array, 0, a, 0, N);

        a[N] = Double.POSITIVE_INFINITY;

        double v, t;
        int i, j, l = 0;
        int r = N - 1;

        // find the two indexes k1 and k2 (possibly equal) which need
        // to be interpolated to get the quartile, being careful to
        // zero-index.  Also find interpolation ratio.
        double doubleIndex = (quartileNumber / 4.0) * (N + 1.0) - 1;
        double ratio = doubleIndex - (int) (doubleIndex);
        int k1 = (int) Math.floor(doubleIndex);
        int k2 = (int) Math.ceil(doubleIndex);

        // partially sort array a[] to find k1 and k2
        while (r > l) {
            v = a[l];
            i = l;
            j = r + 1;

            for (; ;) {
                while (a[++i] < v) {
                    ;
                }

                while (a[--j] > v) {
                    ;
                }

                if (i >= j) {
                    break;
                }

                t = a[i];
                a[i] = a[j];
                a[j] = t;
            }

            t = a[j];
            a[j] = a[l];
            a[l] = t;

            if (j <= k1) {
                l = j + 1;
            }

            if (j >= k2) {
                r = j - 1;
            }
        }

        // return the interpolated value.
        return (a[k1] + ratio * (a[k2] - a[k1]));
    }

    /**
     * @param array a long array.
     * @return the minimum of the values in this array.
     */
    public static double arrayMin(long[] array) {
        return arrayMin(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the minimum of the values in this array.
     */
    public static double arrayMin(double[] array) {
        return arrayMin(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the minimum of the first N values in this array.
     */
    public static double arrayMin(long array[], int N) {

        double min = array[0];

        for (int i = 1; i < N; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the minimum of the first N values in this array.
     */
    public static double arrayMin(double array[], int N) {

        double min = array[0];

        for (int i = 1; i < N; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * @param array a long array.
     * @return the maximum of the values in this array.
     */
    public static double arrayMax(long[] array) {
        return arrayMax(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the maximum of the values in this array.
     */
    public static double arrayMax(double[] array) {
        return arrayMax(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the maximum of the first N values in this array.
     */
    public static double arrayMax(long array[], int N) {

        double max = array[0];

        for (int i = 0; i < N; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the maximum of the first N values in this array.
     */
    public static double arrayMax(double array[], int N) {

        double max = array[0];

        for (int i = 0; i < N; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    /**
     * @param array a long array.
     * @return the range of the values in this array.
     */
    public static double range(long array[]) {
        return (arrayMax(array, array.length) - arrayMin(array, array.length));
    }

    /**
     * @param array a double array.
     * @return the range of the values in this array.
     */
    public static double range(double array[]) {
        return (arrayMax(array, array.length) - arrayMin(array, array.length));
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the range of the first N values in this array.
     */
    public static double range(long array[], int N) {
        return (arrayMax(array, N) - arrayMin(array, N));
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the range of the first N values in this array.
     */
    public static double range(double array[], int N) {
        return (arrayMax(array, N) - arrayMin(array, N));
    }

    /**
     * @param array a long array.
     * @return the length of this array.
     */
    public static int N(long[] array) {
        return array.length;
    }

    /**
     * @param array a double array.
     * @return the length of this array.
     */
    public static int N(double[] array) {
        return array.length;
    }

    /**
     * @param array a long array.
     * @return the sum of the squared differences from the mean in array.
     */
    public static double SSX(long[] array) {
        return SSX(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the sum of the squared differences from the mean in array.
     */
    public static double SSX(double[] array) {
        return SSX(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the sum of the squared differences from the mean of the first N
     *         values in array.
     */
    public static double SSX(long array[], int N) {

        int i;
        double difference;
        double meanValue = mean(array, N);
        double sum = 0.0;

        for (i = 0; i < N; i++) {
            difference = array[i] - meanValue;
            sum += difference * difference;
        }

        return sum;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the sum of the squared differences from the mean of the first N
     *         values in array.
     */
    public static double SSX(double array[], int N) {

        int i;
        double difference;
        double meanValue = mean(array, N);
        double sum = 0.0;

        for (i = 0; i < N; i++) {
            difference = array[i] - meanValue;
            sum += difference * difference;
        }

        return sum;
    }

    /**
     * @param array1 a long array.
     * @param array2 a long array, same length as array1.
     * @return the sum of the squared differences of the products from the
     *         products of the sample means for array1 and array2..
     */
    public static double SXY(long[] array1, long[] array2) {

        int N1 = array1.length;
        int N2 = array2.length;

        if (N1 != N2) {
            throw new IllegalArgumentException(
                    "StatUtils.SXY: Arrays passed (or lengths specified) of " +
                            "unequal lengths.");
        }

        return SXY(array1, array2, N1);
    }

    /**
     * @param array1 a double array.
     * @param array2 a double array, same length as array1.
     * @return the sum of the squared differences of the products from the
     *         products of the sample means for array1 and array2..
     */
    public static double SXY(double[] array1, double[] array2) {

        int N1 = array1.length;
        int N2 = array2.length;

        if (N1 != N2) {
            throw new IllegalArgumentException(
                    "StatUtils.SXY: Arrays passed (or lengths specified) of " +
                            "unequal lengths.");
        }

        return SXY(array1, array2, N1);
    }

    /**
     * @param array1 a long array.
     * @param array2 a long array.
     * @param N      the number of values of array which should be considered.
     * @return the sum of the squared differences of the products from the
     *         products of the sample means for the first N values in array1 and
     *         array2..
     */
    public static double SXY(long array1[], long array2[], int N) {

        int i;
        double sum = 0.0;
        double meanX = mean(array1, N);
        double meanY = mean(array2, N);

        for (i = 0; i < N; i++) {
            sum += (array1[i] - meanX) * (array2[i] - meanY);
        }
        ;

        return sum;
    }

    /**
     * @param array1 a double array.
     * @param array2 a double array.
     * @param N      the number of values of array which should be considered.
     * @return the sum of the squared differences of the products from the
     *         products of the sample means for the first N values in array1 and
     *         array2..
     */
    public static double SXY(double array1[], double array2[], int N) {
        double sum = 0.0;
        double meanX = mean(array1, N);
        double meanY = mean(array2, N);

        for (int i = 0; i < N; i++) {
            sum += (array1[i] - meanX) * (array2[i] - meanY);
        }

        return sum;
    }

    /**
     * @param array a long array.
     * @return the variance of the values in array.
     */
    public static double variance(long array[]) {
        return variance(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the variance of the values in array.
     */
    public static double variance(double array[]) {
        return variance(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the variance of the first N values in array.
     */
    public static double variance(long array[], int N) {
        return SSX(array, N) / N;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the variance of the first N values in array.
     */
    public static double variance(double array[], int N) {
        return SSX(array, N) / N;
    }

    /**
     * @param array a long array.
     * @return the standard deviation of the values in array.
     */
    public static double standardDeviation(long array[]) {
        return standardDeviation(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the standard deviation of the values in array.
     */
    public static double standardDeviation(double array[]) {
        return standardDeviation(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the standard deviation of the first N values in array.
     */
    public static double standardDeviation(long array[], int N) {
        return Math.pow(SSX(array, N) / N, .5);
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the standard deviation of the first N values in array.
     */
    public static double standardDeviation(double array[], int N) {
        return Math.pow(SSX(array, N) / N, .5);
    }

    /**
     * @param array1 a long array.
     * @param array2 a second long array (same length as array1).
     * @return the covariance of the values in array.
     */
    public static double covariance(long[] array1, long[] array2) {

        int N1 = array1.length;
        int N2 = array2.length;

        if (N1 != N2) {
            throw new IllegalArgumentException(
                    "Arrays passed (or lengths specified) of " +
                            "unequal lengths.");
        }

        return covariance(array1, array2, N1);
    }

    /**
     * @param array1 a double array.
     * @param array2 a second double array (same length as array1).
     * @return the covariance of the values in array.
     */
    public static double covariance(double[] array1, double[] array2) {
        int N1 = array1.length;
        int N2 = array2.length;

        if (N1 != N2) {
            throw new IllegalArgumentException(
                    "Arrays passed (or lengths specified) of " +
                            "unequal lengths.");
        }

        return covariance(array1, array2, N1);
    }

    /**
     * @param array1 a long array.
     * @param array2 a second long array.
     * @param N      the number of values to be considered in array1 and
     *               array2.
     * @return the covariance of the first N values in array1 and array2.
     */
    public static double covariance(long array1[], long array2[], int N) {
        return SXY(array1, array2, N) / N;
    }

    /**
     * @param array1 a double array.
     * @param array2 a second double array (same length as array1).
     * @param N      the number of values to be considered in array1 and
     *               array2.
     * @return the covariance of the first N values in array1 and array2.
     */
    public static double covariance(double array1[], double array2[], int N) {
        return SXY(array1, array2, N) / N;
    }

    /**
     * @param array1 a long array.
     * @param array2 a second long array (same length as array1).
     * @return the Pearson's correlation of the values in array1 and array2.
     */
    public static double rXY(long[] array1, long[] array2) {

        int N1 = array1.length;
        int N2 = array2.length;

        if (N1 != N2) {
            throw new IllegalArgumentException(
                    "Arrays passed (or lengths specified) of " +
                            "unequal lengths.");
        }

        return rXY(array1, array2, N1);
    }

    /**
     * @param array1 a double array.
     * @param array2 a second double array (same length as array1).
     * @return the Pearson's correlation of the values in array1 and array2.
     */
    public static double rXY(double[] array1, double[] array2) {

        int N1 = array1.length;
        int N2 = array2.length;

        if (N1 != N2) {
            throw new IllegalArgumentException(
                    "Arrays passed (or lengths specified) of " +
                            "unequal lengths.");
        }

        return rXY(array1, array2, N1);
    }

    /**
     * @param array1 a long array.
     * @param array2 a second long array.
     * @param N      the number of values to be considered in array1 and
     *               array2.
     * @return the Pearson's correlation of the first N values in array1 and
     *         array2.
     */
    public static double rXY(long array1[], long array2[], int N) {
        double covXY = SXY(array1, array2, N);
        double covXX = SXY(array1, array1, N);
        double covYY = SXY(array2, array2, N);
        return (covXY / (Math.pow(covXX, .5) * Math.pow(covYY, .5)));
    }

    /**
     * Returns the Pearson correlation of the first N values in array1 and
     * array2.
     *
     * @param array1 a double array.
     * @param array2 a second double array.
     * @param N      the number of values to be considered in array1 and
     *               array2.
     */
    public static double rXY(double array1[], double array2[], int N) {
        double covXY = SXY(array1, array2, N);
        double covXX = SXY(array1, array1, N);
        double covYY = SXY(array2, array2, N);
        return (covXY / (Math.sqrt(covXX) * Math.sqrt(covYY)));
    }

    /**
     * @param array1 a long array.
     * @param array2 a second long array (same length as array1).
     * @return the rank correlation of the values in array1 and array2.
     */
//    public static double rankCorrelation(long[] array1, long[] array2) {
//        int N1 = array1.length;
//        int N2 = array2.length;
//
//        if (N1 != N2) {
//            throw new IllegalArgumentException("Arrays passed (or lengths specified) of " +
//                    "unequal lengths.");
//        }
//
//        return rankCorrelation(array1, array2, N1);
//    }                                   

    /**
     * @param array1 a double array.
     * @param array2 a second double array (same length as array1).
     * @return the rank correlation of the values in array1 and array2.
     */
//    public static double rankCorrelation(double[] array1, double[] array2) {
//        int N1 = array1.length;
//        int N2 = array2.length;
//
//        if (N1 != N2) {
//            throw new IllegalArgumentException("Arrays passed (or lengths specified) of " +
//                    "unequal lengths.");
//        }
//
//        return rankCorrelation(array1, array2, N1);
//    }

    /**
     * @param array1 a long array.
     * @param array2 a second long array.
     * @param N      the number of values to be considered in array1 and
     *               array2.
     * @return the rank correlation of the first N values in array1 and array2.
     */
//    public static double rankCorrelation(long array1[], long array2[], int N) {
//
//        int i;
//        double r;
//        int rankX[] = SortUtils.rankTable(array1, N);
//        int rankY[] = SortUtils.rankTable(array2, N);
//        long rankXLong[] = new long[N];
//        long rankYLong[] = new long[N];
//
//        for (i = 0; i < N; i++) {
//            rankXLong[i] = (long) rankX[i];
//        }
//
//        for (i = 0; i < N; i++) {
//            rankYLong[i] = (long) rankY[i];
//        }
//
//        r = rXY(rankXLong, rankYLong, N);
//
//        return r;
//    }

    /**
     * @param array1 a double array.
     * @param array2 a second double array.
     * @param N      the number of values to be considered in array1 and
     *               array2.
     * @return the rank correlation of the first N values in array1 and array2.
     */
//    public static double rankCorrelation(double array1[], double array2[],
//            int N) {
//        int rankX[] = SortUtils.rankTable(array1, N);
//        int rankY[] = SortUtils.rankTable(array2, N);
//        long rankXLong[] = new long[N];
//        long rankYLong[] = new long[N];
//
//        for (int i = 0; i < N; i++) {
//            rankXLong[i] = (long) rankX[i];
//        }
//
//        for (int i = 0; i < N; i++) {
//            rankYLong[i] = (long) rankY[i];
//        }
//
//        return rXY(rankXLong, rankYLong, N);
//    }

    /**
     * @param array a long array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         values in array asuming the mean is unknown.
     */
    public static double sSquare(long[] array) {
        return sSquare(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         values in array asuming the mean is unknown.
     */
    public static double sSquare(double[] array) {
        return SSX(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values to be considered in array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         first N values in array asuming the mean is unknown.
     */
    public static double sSquare(long array[], int N) {
        return SSX(array, N) / (N - 1);
    }

    /**
     * @param array a double array.
     * @param N     the number of values to be considered in array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         first N values in array asuming the mean is unknown.
     */
    public static double sSquare(double array[], int N) {
        return SSX(array, N) / (N - 1);
    }

    /**
     * @param array a long array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         values in array asuming the mean is known.
     */
    public static double varHat(long array[]) {
        return varHat(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         values in array asuming the mean is known.
     */
    public static double varHat(double array[]) {
        return varHat(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values to be considered in array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         first N values in array asuming the mean is known.
     */
    public static double varHat(long array[], int N) {
        double sum = 0;
        double difference = 0;
        double meanX = mean(array, N);

        for (int i = 0; i < N; i++) {
            difference = array[i] - meanX;
            sum += difference * difference;
        }

        return sum / (N - 1);
    }

    /**
     * @param array a double array.
     * @param N     the number of values to be considered in array.
     * @return the unbaised estimate of the variance of the distribution of the
     *         first N values in array asuming the mean is known.
     */
    public static double varHat(double array[], int N) {
        double sum = 0.;
        double difference = 0.;
        double meanX = mean(array, N);

        for (int i = 0; i < N; i++) {
            difference = array[i] - meanX;
            sum += difference * difference;
        }

        return sum / (N - 1);
    }

    /**
     * @param array a long array.
     * @return the unbaised estimate of the mean of the distribution of the
     *         values in array.
     */
    public static double mu(long[] array) {
        return mean(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the unbaised estimate of the mean of the distribution of the
     *         values in array.
     */
    public static double mu(double[] array) {
        return mean(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values to be considered in array.
     * @return the unbaised estimate of the mean of the distribution of the
     *         first N values in array.
     */
    public static double mu(long array[], int N) {
        return mean(array, N);
    }

    /**
     * @param array a double array.
     * @param N     the number of values to be considered in array.
     * @return the unbaised estimate of the mean of the distribution of the
     *         first N values in array.
     */
    public static double mu(double array[], int N) {
        return mean(array, N);
    }

    /**
     * @param array a long array.
     * @return the maximum likelihood estimate of the mean of the distribution
     *         of the values in array.
     */
    public static double muHat(long[] array) {
        return muHat(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the maximum likelihood estimate of the mean of the distribution
     *         of the values in array.
     */
    public static double muHat(double[] array) {
        return muHat(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values to be considered in array.
     * @return the maximum likelihood estimate of the mean of the distribution
     *         of the first N values in array.
     */
    public static double muHat(long array[], int N) {
        return mean(array, N);
    }

    /**
     * @param array a long array.
     * @param N     the number of values to be considered in array.
     * @return the maximum likelihood estimate of the mean of the distribution
     *         of the first N values in array.
     */
    public static double muHat(double array[], int N) {
        return mean(array, N);
    }

    /**
     * @param array a long array.
     * @return the average deviation of the values in array.
     */
    public static double averageDeviation(long array[]) {
        return averageDeviation(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the average deviation of the values in array.
     */
    public static double averageDeviation(double array[]) {
        return averageDeviation(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values to be considered in array.
     * @return the average deviation of the first N values in array.
     */
    public static double averageDeviation(long array[], int N) {
        double mean = StatUtils.mean(array, N);
        double adev = 0.0;

        for (int j = 0; j < N; j++) {
            adev += (Math.abs(array[j] - mean));
        }

        adev /= N;

        return adev;
    }

    /**
     * @param array a double array.
     * @param N     the number of values to be considered in array.
     * @return the average deviation of the first N values in array.
     */
    public static double averageDeviation(double array[], int N) {
        double mean = StatUtils.mean(array, N);
        double adev = 0.0;

        for (int j = 0; j < N; j++) {
            adev += (Math.abs(array[j] - mean));
        }

        adev /= N;

        return adev;
    }

    /**
     * @param array a long array.
     * @return the skew of the values in array.
     */
    public static double skew(long[] array) {
        return skew(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the skew of the values in array.
     */
    public static double skew(double[] array) {
        return skew(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the skew of the first N values in array.
     */
    public static double skew(long array[], int N) {
        double mean = StatUtils.mean(array, N);
        double variance = StatUtils.variance(array, N);
        double skew = 0.0;

        for (int j = 0; j < N; j++) {
            double s = array[j] - mean;

            skew += s * s * s;
        }

        if (variance == 0) {
            throw new ArithmeticException("StatUtils.skew:  There is no skew " +
                    "when the variance is zero.");
        }

        skew /= (N * Math.pow(variance, 1.5));
        return skew;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the skew of the first N values in array.
     */
    public static double skew(double array[], int N) {
        double mean = StatUtils.mean(array, N);
        double variance = StatUtils.variance(array, N);
        double skew = 0.0;

        for (int j = 0; j < N; j++) {
            double s = array[j] - mean;

            skew += s * s * s;
        }

        if (variance == 0) {
            throw new ArithmeticException("StatUtils.skew:  There is no skew " +
                    "when the variance is zero.");
        }

        skew /= (N * Math.pow(variance, 1.5));
        return skew;
    }

    /**
     * @param array a long array.
     * @return the curtosis of the values in array.
     */
    public static double curtosis(long array[]) {
        return curtosis(array, array.length);
    }

    /**
     * @param array a double array.
     * @return the curtosis of the values in array.
     */
    public static double curtosis(double array[]) {
        return curtosis(array, array.length);
    }

    /**
     * @param array a long array.
     * @param N     the number of values of array which should be considered.
     * @return the curtosis of the first N values in array.
     */
    public static double curtosis(long array[], int N) {
        double mean = StatUtils.mean(array, N);
        double variance = StatUtils.variance(array, N);
        double curt = 0.0;

        for (int j = 0; j < N; j++) {
            double s = array[j] - mean;
            curt += s * s * s * s;
        }

        if (variance == 0) {
            throw new ArithmeticException(
                    "Curtosis is undefined when variance " + "is zero.");
        }

        curt = (curt / (N * variance * variance) - 3.0);

        return curt;
    }

    /**
     * @param array a double array.
     * @param N     the number of values of array which should be considered.
     * @return the curtosis of the first N values in array.
     */
    public static double curtosis(double array[], int N) {
        double mean = StatUtils.mean(array, N);
        double variance = StatUtils.variance(array, N);
        double curt = 0.0;

        for (int j = 0; j < N; j++) {
            double s = array[j] - mean;
            curt += s * s * s * s;
        }

        if (variance == 0) {
            throw new ArithmeticException(
                    "StatUtils.curtosis:  There is no curtosis " +
                            "when the variance is zero.");
        }

        curt = (curt / (N * variance * variance) - 3.0);

        return curt;
    }

    /**
     * GAMMA FUNCTION  (From DStat, used by permission).
     * <p/>
     * Calculates the value of gamma(double z) using Handbook of Mathematical
     * Functions AMS 55 by Abromowitz page 256.
     *
     * @param z nonnegative double value.
     * @return the gamma value of z.
     * @throws Exception
     */
    public static double gamma(double z) throws Exception {

        // if z is < 2 then do straight gamma
        try {
            if (z < 2.0) {
                return (Internalgamma(z));
            }
            else {

                // z >= 2.0, break up into N*1.5 and use Gauss
                // Multiplication formula.
                double multiplier = Math.floor(z / 1.2);
                double remainder = z / multiplier;
                double coef1 =
                        Math.pow(2.0 * Math.PI, (0.5 * (1.0 - multiplier)));
                double coef2 =
                        Math.pow(multiplier, ((multiplier * remainder) - 0.5));
                int N = (int) multiplier;
                double prod = 1.0;

                for (int k = 0; k < N; k++) {
                    prod *= Internalgamma(
                            remainder + (k / multiplier));
                }

                return coef1 * coef2 * prod;
            }
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * An internal method for finding gamma for a restricted range of reals.
     *
     * @param z argument
     * @return gamma of argument.
     * @throws Exception
     */
    private static double Internalgamma(double z) throws Exception {
        double sum = 0.0;
        double[] c = {1.0, 0.5772156649015329, -0.6558780715202538,
                -0.0420026350340952, 0.1665386113822915, -0.0421977345555443,
                -0.0096219715278770, 0.0072189432466630, -0.0011651675918591,
                -0.0002152416741149, 0.0001280502823882, -0.0000201348547807,
                -0.0000012504934821, 0.0000011330272320, -0.0000002056338417,
                0.0000000061160950, 0.0000000050020075, -0.0000000011812746,
                0.0000000001043427, 0.0000000000077823, -0.0000000000036968,
                0.0000000000005100, -0.0000000000000206, -0.0000000000000054,
                0.0000000000000014, 0.0000000000000001};

        for (int i = 0; i < c.length; i++) {
            sum += c[i] * Math.pow(z, (i + 1));
        }

        return (1.0 / sum);
    }

    /**
     * Calculates the value of beta for doubles
     *
     * @param x1 the first double
     * @param x2 the second double.
     * @return beta(x1, x2).
     * @throws Exception
     */
    public static double beta(double x1, double x2) throws Exception {
        return ((gamma(x1) * gamma(x2)) / gamma(x1 + x2));
    }

    /**
     * Calculates the incomplete gamma function for two doubles
     *
     * @param a first double.
     * @param x second double.
     * @return incomplete gamma of (a, x).
     * @throws Exception
     */
    public static double igamma(double a, double x) throws Exception {
        try {
            double coef = (Math.exp(-x) * Math.pow(x, a)) / gamma(a);
            double sum = 0.0;

            for (int i = 0; i < 100; i++) {
                sum += (gamma(a) / gamma(a + 1.0 + i)) *
                        Math.pow(x, i);
            }

            return (coef * sum);
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Calculates the error function for a double
     *
     * @param x argument.
     * @return error function of this argument.
     * @throws Exception
     */
    public static double erf(double x) throws Exception {
        try {
            return (igamma(0.5, Math.pow(x, 2.0)));
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Calculates the Poisson Distribution for mean x and k events for doubles.
     * If third parameter is boolean true, the cumulative Poisson function is
     * returned.
     *
     * @param k   # events
     * @param x   mean
     * @param cum true if the cumulative Poisson is desired.
     * @return the value of the Poisson (or cumPoisson) at x.
     * @throws Exception
     */
    public static double poisson(double k, double x, boolean cum)
            throws Exception {
        if ((x < 0) || (k < 1)) {
            throw new ArithmeticException(
                    "The Poisson Distribution Function requires x>=0 and k >= 1");
        }

        try {
            k = k + 1;    // algorithm uses k+1, not k

            if (cum) {
                return (1.0 - igamma(k, x));
            }
            else {
                return ((Math.exp(-x) * Math.pow(x, k)) / gamma(k));
            }
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Calculates the one-tail probability of the Chi-squared distribution for
     * doubles
     *
     * @param x
     * @param degreesOfFreedom
     * @return value of Chi at x with the stated degrees of freedom.
     * @throws Exception
     */
    public static double chidist(double x, int degreesOfFreedom)
            throws Exception {
        if ((x < 0.0) || (degreesOfFreedom < 0)) {
            throw new ArithmeticException(
                    "The Chi Distribution Function requires x > 0.0 and degrees of freedom > 0");
        }

        try {
            return (1.0 - igamma(degreesOfFreedom / 2.0, x / 2.0));
        }
        catch (Exception e) {
            throw e;
        }
    }
    

    /**
     * Calculates and returns a double array with elements Chi-squared,
     * Cramers's V, and Contigency Coeeficient Cross Tab for an Array of nominal
     * values.
     *
     * @param IArray
     * @return ???
     * @throws Exception
     */
    public static double[] ContTable1(int[][] IArray) throws Exception {
        int sumiDot;
        int sumjDot;
        int counti;
        int countj;
        int countTotal = 0;
        int minCount;
        double expectedN;
        int i;
        int j;
        int looper;
        double chisquare;
        double cramerV;
        double contCoef;
        double[] retValues = new double[3];

        // Count elements
        counti = IArray.length;
        countj = IArray[0].length;

        for (i = 0; i < counti; i++) {
            for (j = 0; j < countj; j++) {
                countTotal += IArray[i][j];
            }
        }

        // Calculate chi-squared
        try {
            chisquare = 0.0;

            for (i = 0; i < counti; i++) {
                for (j = 0; j < countj; j++) {
                    sumiDot = 0;
                    sumjDot = 0;

                    for (looper = 0; looper < countj; looper++) {
                        sumiDot += IArray[i][looper];
                    }

                    for (looper = 0; looper < counti; looper++) {
                        sumjDot += IArray[looper][j];
                    }

                    expectedN =
                            (double) (sumiDot * sumjDot) / (double) countTotal;
                    chisquare +=
                            Math.pow((IArray[i][j] - expectedN), 2) / expectedN;
                }
            }
        }
        catch (Exception e) {
            throw e;
        }

        // Calculate Cramer's V
        if (counti < countj) {
            minCount = counti - 1;
        }
        else {
            minCount = countj - 1;
        }

        try {
            cramerV =
                    Math.sqrt((chisquare / ((countTotal * minCount))));
        }
        catch (Exception e) {
            throw e;
        }

        // Calculate Contingency Coefficient
        try {
            contCoef =
                    Math.sqrt((chisquare / (chisquare + countTotal)));
        }
        catch (Exception e) {
            throw e;
        }

        retValues[0] = chisquare;
        retValues[1] = cramerV;
        retValues[2] = contCoef;

        return retValues;
    }
    
	//returns the value of a toss of an n-sided die
	public static int dieToss(int n) {
		return (int) java.lang.Math.floor(n * java.lang.Math.random());
	}

}

