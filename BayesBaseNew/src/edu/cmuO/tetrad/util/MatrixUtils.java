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

import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.colt.matrix.linalg.Property;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import cern.jet.math.Functions;
import edu.cmu.tetrad.search.fastica.math.Matrix;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Class Matrix includes several public static functions performing matrix
 * operations. These function include: determinant, GJinverse, inverse,
 * multiple, difference, transpose, trace, duplicate, minor, identity, mprint,
 * impliedCovar, SEMimpliedCovar.
 *
 * @author Tianjiao Chu
 * @author Joseph Ramsey
 * @version $Revision: 6219 $
 */
public final class MatrixUtils {

    //=========================PUBLIC METHODS===========================//

    /**
     * Copies a 2D double m.
     */
    public static double[][] copy(double[][] m) {
        return new DenseDoubleMatrix2D(m).toArray();
    }

    /**
     * Copies a 2D int m.
     */
    public static int[][] copy(int[][] m) {
        if (m == null) {
            return null;
        }

        int[][] copy = new int[m.length][];
        for (int i = 0; i < m.length; i++) {
            int[] row = new int[m[i].length];
            System.arraycopy(m[i], 0, row, 0, m[i].length);
            copy[i] = row;
        }

        return copy;
    }

    /**
     * Copies a 2D boolean m.
     */
    public static boolean[][] copy(boolean[][] m) {
        boolean[][] copy = new boolean[m.length][];
        for (int i = 0; i < m.length; i++) {
            boolean[] row = new boolean[m[i].length];
            System.arraycopy(m[i], 0, row, 0, m[i].length);
            copy[i] = row;
        }
        return copy;
    }

    /**
     * Tests two matrices for equality.
     */
    public static boolean equals(double[][] ma, double[][] mb) {
        return new DenseDoubleMatrix2D(ma).equals(new DenseDoubleMatrix2D(mb));
    }

    /**
     * Tests two vectors for equality.
     */
    public static boolean equals(double[] va, double[] vb) {
        return new DenseDoubleMatrix1D(va).equals(new DenseDoubleMatrix1D(vb));
    }

    /**
     * Tests to see whether two matrices are equal within the given tolerance.
     * If any two corresponding elements differ by more than the given
     * tolerance, false is returned.
     */
    public static boolean equals(double[][] ma, double[][] mb,
                                 double tolerance) {
        return new Property(tolerance).equals(new DenseDoubleMatrix2D(ma),
                new DenseDoubleMatrix2D(mb));
    }

    /**
     * Tests to see whether two vectors are equal within the given tolerance. If
     * any two corresponding elements differ by more than the given tolerance,
     * false is returned.
     */
    public static boolean equals(double[] va, double[] vb, double tolerance) {
        return new Property(tolerance).equals(new DenseDoubleMatrix1D(va),
                new DenseDoubleMatrix1D(vb));
    }

    /**
     * Returns true iff <code>m</code> is square.
     */
    public static boolean isSquare(double[][] m) {
        return new Property(0.).isSquare(new DenseDoubleMatrix2D(m));
    }

    public static boolean isSquareC(DoubleMatrix2D m) {
        return new Property(0.).isSquare(m);
    }

    /**
     * Returns true iff <code>m</code> is symmetric to within the given
     * tolerance--that is, Math.abs(m[i][j] - m[j][i]) <= tolernance for each i,
     * j.
     */
    public static boolean isSymmetric(double[][] m, double tolerance) {
        return new Property(tolerance).isSymmetric(new DenseDoubleMatrix2D(m));
    }

    public static boolean isSymmetricC(DoubleMatrix2D m, double tolerance) {
        return new Property(tolerance).isSymmetric(m);
    }

    /**
     * Returns the determinant of a (square) m.
     */
    public static double determinant(double[][] m) {
        return new Algebra().det(new DenseDoubleMatrix2D(m));
    }


    public static double determinant(DoubleMatrix2D m) {
        return new Algebra(1e-5).det(m);
    }

    /**
     * A copy of the original (square) matrix with the stated index row/column
     * removed
     */
    public static double[][] submatrix(double[][] m, int rem) {
        int[] indices = new int[m.length];

        int j = -1;
        for (int i = 0; i < m.length; i++) {
            j++;
            if (j == rem) {
                j++;
            }
            indices[i] = j;
        }

        return new DenseDoubleMatrix2D(m).viewSelection(indices,
                indices).toArray();
    }

    /**
     * Returns the inverse of the given square matrix if it is nonsingular,
     * otherwise the pseudoinverse.
     */
    public static double[][] inverse(double[][] m) {
        Algebra algebra = new Algebra();
        DenseDoubleMatrix2D mm = new DenseDoubleMatrix2D(m);
        return algebra.inverse(mm).toArray();
    }

    public static DoubleMatrix2D inverseC(DoubleMatrix2D m) {
        return new Algebra().inverse(m);
    }

    public static DoubleMatrix2D ginverse(DoubleMatrix2D x) {
        if (x.columns() == 0) {
            return new DenseDoubleMatrix2D(0, 0);
        }

        Algebra algebra = new Algebra();

        SingularValueDecomposition svd = new SingularValueDecomposition(x);
        DoubleMatrix2D U = svd.getU();
        DoubleMatrix2D V = svd.getV();
        DoubleMatrix2D S = svd.getS();

        S.assign(new DoubleFunction() {
            @Override
			public double apply(double v) {
                return v == 0 ? 0 : 1.0 / v;
            }
        });

        DoubleMatrix2D G =
                new Algebra().mult(V, algebra.mult(S, algebra.transpose(U)));
        return G;
    }

    /**
     * Returns the outerProduct of ma and mb. The dimensions of ma and mb must
     * be compatible for multiplication.
     */
    public static double[][] product(double[][] ma, double[][] mb) {
        DenseDoubleMatrix2D d = new DenseDoubleMatrix2D(ma);
        DenseDoubleMatrix2D e = new DenseDoubleMatrix2D(mb);
        return new Algebra().mult(d, e).toArray();
    }

    public static DoubleMatrix2D productC(DoubleMatrix2D ma,
                                          DoubleMatrix2D mb) {
        return new Algebra().mult(ma, mb);
    }


    public static double[] product(double[] ma, double[][] mb) {
        return new Algebra().mult(new DenseDoubleMatrix2D(mb).viewDice(),
                new DenseDoubleMatrix1D(ma)).toArray();
    }

    public static DoubleMatrix1D product(DoubleMatrix1D ma, DoubleMatrix2D mb) {
        return new Algebra().mult(mb.viewDice(), ma);
    }

    public static double[] product(double[][] ma, double[] mb) {
        return new Algebra().mult(new DenseDoubleMatrix2D(ma),
                new DenseDoubleMatrix1D(mb)).toArray();
    }

    public static double[][] outerProduct(double[] ma, double[] mb) {
        return new Algebra().multOuter(new DenseDoubleMatrix1D(ma),
                new DenseDoubleMatrix1D(mb), null).toArray();
    }

    public static double innerProduct(double[] ma, double[] mb) {
        return new Algebra().mult(new DenseDoubleMatrix1D(ma),
                new DenseDoubleMatrix1D(mb));
    }

    public static double innerProduct(DoubleMatrix1D ma, DoubleMatrix1D mb) {
        return new Algebra().mult(ma, mb);
    }

    /**
     * Returns the transpose of the given matrix.
     */
    public static double[][] transpose(double[][] m) {
        return new DenseDoubleMatrix2D(m).viewDice().toArray();
    }

    public static DoubleMatrix2D transposeC(DoubleMatrix2D m) {
        return new Algebra().transpose(m);
    }

    /**
     * Returns the trace of the given (square) m.
     */
    public static double trace(double[][] m) {
        return new Algebra().trace(new DenseDoubleMatrix2D(m));
    }

    public static double trace(DoubleMatrix2D m) {
        return new Algebra().trace(m);
    }

    //Returns the sum of all values in a double matrix.
    public static double zSum(double[][] m) {
        return new DenseDoubleMatrix2D(m).zSum();
    }

    /**
     * Returns the identity matrix of the given order.
     */
    public static double[][] identity(int size) {
        return DoubleFactory2D.dense.identity(size).toArray();
    }

    public static DoubleMatrix2D identityC(int size) {
        return DoubleFactory2D.dense.identity(size);
    }

    /**
     * Returns the sum of ma and mb.
     */
    public static double[][] sum(double[][] ma, double[][] mb) {
        DoubleMatrix2D _ma = new DenseDoubleMatrix2D(ma);
        DoubleMatrix2D _mb = new DenseDoubleMatrix2D(mb);
        _ma.assign(_mb, Functions.plus);
        return _ma.toArray();
    }


    public static DoubleMatrix2D sumC(DoubleMatrix2D ma, DoubleMatrix2D mb) {
        DoubleMatrix2D mc = ma.copy();
        return mc.assign(mb, Functions.plus);
    }


    public static double[] sum(double[] ma, double[] mb) {
        DoubleMatrix1D _ma = new DenseDoubleMatrix1D(ma);
        DoubleMatrix1D _mb = new DenseDoubleMatrix1D(mb);
        _ma.assign(_mb, Functions.plus);
        return _ma.toArray();
    }

    public static double[][] subtract(double[][] ma, double[][] mb) {
        DoubleMatrix2D _ma = new DenseDoubleMatrix2D(ma);
        DoubleMatrix2D _mb = new DenseDoubleMatrix2D(mb);
        _ma.assign(_mb, Functions.minus);
        return _ma.toArray();
    }

    public static DoubleMatrix2D subtractC(DoubleMatrix2D ma,
                                           DoubleMatrix2D mb) {
        return ma.assign(mb, Functions.minus);
    }

    public static double[] subtract(double[] ma, double[] mb) {
        DoubleMatrix1D _ma = new DenseDoubleMatrix1D(ma);
        DoubleMatrix1D _mb = new DenseDoubleMatrix1D(mb);
        _ma.assign(_mb, Functions.minus);
        return _ma.toArray();
    }

    /**
     * Computes the direct (Kronecker) outerProduct.
     */
    public static double[][] directProduct(double[][] ma, double[][] mb) {
        int arow = ma.length;
        int brow = mb.length;
        int acol = ma[0].length;
        int bcol = mb[0].length;

        double[][] product = new double[arow * brow][acol * bcol];

        for (int i1 = 0; i1 < arow; i1++) {
            for (int j1 = 0; j1 < acol; j1++) {
                for (int i2 = 0; i2 < brow; i2++) {
                    for (int j2 = 0; j2 < bcol; j2++) {
                        int i = i1 * brow + i2;
                        int j = j1 * bcol + j2;
                        product[i][j] = ma[i1][j1] * mb[i2][j2];
                    }
                }
            }
        }

        return product;
    }

    /**
     * Multiplies the given matrix through by the given scalar.
     */
    public static double[][] scalarProduct(double scalar, double[][] m) {
        DoubleMatrix2D _m = new DenseDoubleMatrix2D(m);
        _m.assign(Functions.mult(scalar));
        return _m.toArray();
    }

    public static double[] scalarProduct(double scalar, double[] m) {
        DoubleMatrix1D _m = new DenseDoubleMatrix1D(m);
        _m.assign(Functions.mult(scalar));
        return _m.toArray();
    }

    /**
     * Concatenates the vectors rows[i], i = 0...rows.length, into a single
     * vector.
     */
    public static double[] concatenate(double[][] vectors) {
        int numVectors = vectors.length;
        int length = vectors[0].length;
        double[] concat = new double[numVectors * length];

        for (int i = 0; i < vectors.length; i++) {
            System.arraycopy(vectors[i], 0, concat, i * length, length);
        }

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
     * Returns the implied covariance matrix for a structural equation model
     * with measured variables only (no latents).
     *
     * @param coef  The matrix of edge coefficients for the variables.
     * @param covar The matrix of covariances among the error terms of the
     *              variables (including of course the variances of the error
     *              terms themselves along the diagonal). The order of variables
     *              must be the same as in coef.
     * @return the implied covariance matrix.
     */
    public static double[][] impliedCovar(double[][] coef, double[][] covar) {
        DenseDoubleMatrix2D _coef = new DenseDoubleMatrix2D(coef);
        DenseDoubleMatrix2D _covar = new DenseDoubleMatrix2D(covar);
        return impliedCovarC(_coef, _covar).toArray();
    }

    /**
     * Returns the implied covariance matrix for a structural equation model
     * with measured and possibly latent variables.
     * @param edgeCoef The edge covariance matrix. edgeCoef(i, j) is a parameter
     * in this matrix just in case i-->j is an edge in the model. All other
     * entries in the matrix are zero.
     * @param errCovar The error covariance matrix. errCovar(i, i) is the
     * variance of i; off-diagonal errCovar(i, j) are covariance parameters
     * that are specified in the model. All other matrix entries are zero.
     * @return The implied covariance matrix, which is the covariance matrix
     * over the measured variables that is implied by all the given information.
     * @throws IllegalArgumentException if edgeCoef or errCovar contains an
     * undefined value (Double.NaN), or errCovar is not positive definite.
     */
    public static DoubleMatrix2D impliedCovarC(DoubleMatrix2D edgeCoef,
                                               DoubleMatrix2D errCovar) {
        if (containsNaN(edgeCoef)) {
            throw new IllegalArgumentException("Edge coefficient matrix must not " +
                    "contain undefined values. Probably the search put them " +
                    "there");
        }

        if (containsNaN(errCovar)) {
            throw new IllegalArgumentException("Error covariance matrix must not " +
                    "contain undefined values. Probably the search put them " +
                    "there.");
        }

        if (!MatrixUtils.isSymmetricPositiveDefinite(errCovar)) {
            throw new IllegalArgumentException("Error covariance matrix must be " +
                    "positive definite.");
        }

        // I
        DoubleMatrix2D m1 = DoubleFactory2D.dense.identity(edgeCoef.rows());

        // I - B
        m1.assign(edgeCoef, Functions.minus);

        // (I - B) ^ -1
        DoubleMatrix2D m3 = new Algebra().inverse(m1);

        // ((I - B) ^ -1)'
        DoubleMatrix2D m4 = new Algebra().transpose(m3);

        // ((I - B) ^ -1) Cov(e)
        DoubleMatrix2D m5 = new Algebra().mult(m3, errCovar);

        // ((I - B) ^ -1) Cov(e) ((I - B) ^ -1)'
        DoubleMatrix2D m6 = new Algebra().mult(m5, m4);

        // This checks randomly to see if v' m6 v > 0 (criterion for positive
        // definiteness).
//        for (int k = 0; k < 1000; k++) {
//            DoubleMatrix1D x = new DenseDoubleMatrix1D(m6.rows());
//
//            for (int i = 0; i < x.size(); i++) {
//                x.set(i, 50 * PersistentRandomUtil.getInstance().nextDouble() - 25);
//            }
//
//            DoubleMatrix1D p1 = new Algebra().mult(new Algebra().transpose(m6), x);
//            double p2 = new Algebra().mult(p1, x);
//
//            System.out.println(x + " result = " + p2);
//        }

        // Ensure symmetry.
        for (int i = 0; i < m6.rows(); i++) {
            for (int j = i + 1; j < m6.columns(); j++) {
                m6.set(i, j, m6.get(j, i));
            }
        }


//        if (!MatrixUtils.isSymmetricPositiveDefinite(m6)) {
//            throw new IllegalArgumentException("Implied covariance matrix must be " +
//                    "positive definite.");
//        }

        return m6;
    }

    public static boolean containsNaN(DoubleMatrix2D m) {
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                if (Double.isNaN(m.get(i, j))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns vech of the given array. (This is what you get when you stack all
     * of the elements of m in the lower triangular of m to form a vector. The
     * elements are stacked in columns left to right, top to bottom.)
     */
    public static double[][] vech(double[][] m) {
        if (!isSymmetric(m, 1.e-5)) {
            throw new IllegalArgumentException("m must be a symmetric matrix.");
        }

        int order = m.length;
        int vechSize = sum0ToN(order);
        double[] vech = new double[vechSize];

        int index = -1;
        for (int i = 0; i < order; i++) {
            for (int j = i; j < order; j++) {
                vech[++index] = m[i][j];
            }
        }

        return asCol(vech);
    }

    /**
     * Returns the symmetric matrix for which the given array is the vech.
     */
    public static double[][] invVech(double[] vech) {

        int order = vechOrder(vech);

        // Recreate the symmetric matrix.
        double[][] m = new double[order][order];

        int index = -1;
        for (int i = 0; i < order; i++) {
            for (int j = i; j < order; j++) {
                ++index;
                m[i][j] = vech[index];
                m[j][i] = vech[index];
            }
        }

        return m;
    }

    /**
     * Returns vech of the given array. (This is what you get when you stack all
     * of the elements of m to form a vector. The elements are stacked in
     * columns left to right, top to bottom.)
     */
    public static double[][] vec(double[][] m) {
        assert isSquare(m);

        int order = m.length;
        int vecSize = order * order;
        double[] vec = new double[vecSize];

        int index = -1;
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                vec[++index] = m[i][j];
            }
        }

        return asCol(vec);
    }

    /**
     * Returns the sum of integers from 0 up to n.
     */
    public static int sum0ToN(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Argument must be >= 0: " + n);
        }

        return n * (n + 1) / 2;
    }

    /**
     * The matrix which, when postmultiplied by vech, return vec.
     *
     * @param n the size of the square matrix that vec and vech come from.
     */
    public static double[][] vechToVecLeft(int n) {
        int row = n * n;
        int col = sum0ToN(n);
        double[][] m = new double[row][col];

        int index = -1;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int _row = i * n + j;
                int _col = ++index;
                m[_row][_col] = 1.0;
                _row = j * n + i;
                _col = index;
                m[_row][_col] = 1.0;
            }
        }

        return m;
    }

    /**
     * Returns true just in case the given matrix has the given dimensions
     * --that is, just in case m.length == i and m[0].length == j.
     */
    public static boolean hasDimensions(double[][] m, int i, int j) {
        DenseDoubleMatrix2D _m = new DenseDoubleMatrix2D(m);
        return _m.rows() == i && _m.columns() == j;
    }

    public static double[][] zeros(int rows, int cols) {
        DenseDoubleMatrix2D m = new DenseDoubleMatrix2D(rows, cols);
        m.assign(0.0);
        return m.toArray();
    }

    /**
     * Return true if the given matrix is symmetric positive definite--that is,
     * if it would make a valid covariance matrix.
     */
    public static boolean isSymmetricPositiveDefinite(DoubleMatrix2D matrix) {
        return new CholeskyDecomposition(matrix).isSymmetricPositiveDefinite();
    }

    public static double[][] cholesky(double[][] covar) {
        return new CholeskyDecomposition(
                new DenseDoubleMatrix2D(covar)).getL().toArray();
    }

    public static DoubleMatrix2D choleskyC(DoubleMatrix2D covar) {
        return new CholeskyDecomposition(covar).getL();
    }

    public static void pasteCol(double[][] m1, int m1Col, double[][] m2,
                                int m2Col) {
        if (m1.length != m2.length) {
            throw new IllegalArgumentException(
                    "Matrix must have the same number " + "of rows.");
        }

        for (int j = 0; j < m1.length; j++) {
            m2[j][m2Col] = m1[j][m1Col];
        }
    }

    /**
     * Converts a covariance matrix to a correlation matrix.
     */
    public static DoubleMatrix2D convertCovToCorr(DoubleMatrix2D m) {
        return Statistic.correlation(m.copy());
    }

    /**
     * Converts a matrix in lower triangular form to a symmetric matrix in
     * square form. The lower triangular matrix need not contain matrix elements
     * to represent elements in the upper triangle.
     */
    public static double[][] convertLowerTriangleToSymmetric(double[][] arr) {
        int size = arr.length;
        double[][] m = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= i; j++) {
                m[i][j] = arr[i][j];
                m[j][i] = arr[i][j];
            }
        }

        return m;
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with a tab character. The number format
     * is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(double[][] m) {
        NumberFormat nf = new DecimalFormat(" 0.0000;-0.0000");
        return toString(m, nf);
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with the given lineInit. The number
     * format is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(double[][] m, NumberFormat nf) {
        return toString(m, nf, "\t");
    }

    /**
     * Copies the given array, using the given number formatter and starting
     * each line with the given liniInit. (For scientific notation, try
     * DecimalFormat(" 0.000E0;-0.000E0").)
     */
    public static String toString(double[][] m, NumberFormat nf,
                                  String lineInit) {
        if (nf == null) {
            throw new NullPointerException("NumberFormat must not be null.");
        }
        if (lineInit == null) {
            throw new NullPointerException("LineInit must not be null.");
        }

        if (m == null) {
            return nullMessage(lineInit);
        } else {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < m.length; i++) {
                buf.append("\n");
                if (lineInit != null) {
                    buf.append(lineInit);
                }

                for (int j = 0; j < m[i].length; j++) {
                    buf.append(nf.format(m[i][j]) + "\t");
                }
            }
            return buf.toString();
        }
    }

    public static String toString(int[] m) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n");

        for (int i = 0; i < m.length; i++) {
            buf.append(m[i] + "\t");
        }

        return buf.toString();
    }


    public static String toString(int[][] m) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n");

        for (int i = 0; i < m.length; i++) {
            //buf.append("" + i + ".\t");
            for (int j = 0; j < m[0].length; j++) {
                buf.append("[" + i + "," + j + "]" + m[i][j] + "\t");
            }
            buf.append("\n");
        }

        return buf.toString();
    }

    /**
     * Copies the given array, starting each line with a tab character..
     */
    public static String toString(boolean[][] m) {
        return toString(m, "\t");
    }

    /**
     * Copies the given array, starting each line with the given liniInit.
     */
    public static String toString(boolean[][] m, String lineInit) {
        if (lineInit == null) {
            throw new NullPointerException("LineInit must not be null.");
        }

        if (m == null) {
            return nullMessage(lineInit);
        } else {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < m.length; i++) {
                buf.append("\n");
                if (lineInit != null) {
                    buf.append(lineInit);
                }
                for (int j = 0; j < m[i].length; j++) {
                    buf.append(m[i][j] + "\t");
                }
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

    /**
     * Returns the order of the matrix for which this is the vech.
     *
     * @throws IllegalArgumentException in case this matrix does not have
     *                                  dimension n x 1 for some n = 0 + 1 + 2 +
     *                                  ... + k for some k.
     */
    private static int vechOrder(double[] vech) {
        int difference = vech.length;
        int order = 0;
        while (difference > 0) {
            difference -= (++order);
            if (difference < 0) {
                throw new IllegalArgumentException(
                        "Illegal length for vech: " + vech.length);
            }
        }
        return order;
    }


    //Gustavo 7 May 2007
    //returns the linear combination of two vectors a, b (aw is the coefficient of a, bw is the coefficient of b)
    public static DoubleMatrix1D linearCombination(DoubleMatrix1D a, double aw, DoubleMatrix1D b, double bw) {
        DoubleMatrix1D resultMatrix = new DenseDoubleMatrix1D(a.size());
        for (int i = 0; i < a.size(); i++) {
            resultMatrix.set(i, aw * a.get(i) + bw * b.get(i));
        }
        return resultMatrix;
    }

    //the vectors are in vecs
    //the coefficients are in the vector 'weights'
    public static DoubleMatrix1D linearCombination(DoubleMatrix1D[] vecs, double[] weights) {
        //the elements of vecs must be vectors of the same size
        DoubleMatrix1D resultMatrix = new DenseDoubleMatrix1D(vecs[0].size());

        for (int i = 0; i < vecs[0].size(); i++) { //each entry
            double sum = 0;
            for (int j = 0; j < vecs.length; j++) { //for each vector
                sum += vecs[j].get(i) * weights[j];
            }
            resultMatrix.set(i, sum);
        }
        return resultMatrix;
    }

    //linear combination of matrices a,b
    public static DoubleMatrix2D linearCombination(DoubleMatrix2D a, double aw, DoubleMatrix2D b, double bw) {

        for (int i = 0; i < a.rows(); i++) {
            //System.out.println("b.get("+i+","+i+") = " + b.get(i,i));
        }


        DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(a.rows(), a.columns());
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.columns(); j++) {
                resultMatrix.set(i, j, aw * a.get(i, j) + bw * b.get(i, j));
                if (i == j) {
//					System.out.println("entry (" + i + ","+ i+ ")   is the sum of ");
//					System.out.println("aw*a.get("+i+","+j+") = " + aw*a.get(i,j) + " and");
//					System.out.println("bw*b.get("+i+","+j+") = " + bw*b.get(i,j));
//					System.out.println(", which is " + (aw*a.get(i,j) + bw*b.get(i,j)));
//					System.out.println("");
//					System.out.println("bw = " + bw);
//					System.out.println("b.get("+i+","+j+") = " + b.get(i,j));
//					System.out.println("");
//					System.out.println("");

                }

            }
        }
        return resultMatrix;
    }

    //Gustavo 7 May 2007
    //converts Colt vectors into double[]
    public static double[] convert(DoubleMatrix1D vector) {
        int n = vector.size();
        double[] v = new double[n];
        for (int i = 0; i < n; i++)
            v[i] = vector.get(i);
        return v;
    }

    //Gustavo 7 May 2007
    //converts Colt matrices into double[]
    public static double[][] convert(DoubleMatrix2D inVectors) {
        if (inVectors == null) return null;

        int m = inVectors.rows();
        int n = inVectors.columns();

        double[][] inV = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                inV[i][j] = inVectors.get(i, j);

        return inV;
    }

    //Gustavo 7 May 2007
    //converts double[] into Colt matrices
    public static DoubleMatrix2D convertToColt(double[][] vectors) {
        int m = Matrix.getNumOfRows(vectors);
        int n = Matrix.getNumOfColumns(vectors);

        DoubleMatrix2D mat = new DenseDoubleMatrix2D(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                mat.set(i, j, vectors[i][j]);

        return mat;
    }

    //Gustavo 7 May 2007
    //returns the identity matrix of dimension n
    public static DoubleMatrix2D identityMatrix(int n) {
        DoubleMatrix2D I = new DenseDoubleMatrix2D(n, n);
        I.assign(0);
        for (int i = 0; i < n; i++)
            I.set(i, i, 1);
        return I;
    }


    public static DoubleMatrix2D inverse(DoubleMatrix2D mat) {
        cern.colt.matrix.linalg.Algebra alg = new cern.colt.matrix.linalg.Algebra();
        return alg.inverse(mat);
    }


    //Gustavo 7 May 2007
    //
    //makes the diagonal 1, scaling the remainder of each row appropriately
    //pre: 'matrix' must be square
    public static DoubleMatrix2D normalizeDiagonal(DoubleMatrix2D matrix) {
        DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(MatrixUtils.convert(matrix));
        for (int i = 0; i < resultMatrix.rows(); i++) {
            double factor = 1 / resultMatrix.get(i, i);
            for (int j = 0; j < resultMatrix.columns(); j++)
                resultMatrix.set(i, j, factor * resultMatrix.get(i, j));
        }
        return resultMatrix;
    }


    public static boolean equals(DoubleMatrix2D m1, DoubleMatrix2D m2) {
        if ((m1 == null) || (m2 == null)) return false;

        if ((m1.rows() != m2.rows()) || (m1.columns() != m2.columns()))
            return false;

        for (int i = 0; i < m1.rows(); i++)
            for (int j = 0; j < m2.columns(); j++)
                if (m1.get(i, j) != m2.get(i, j))
                    return false;

        return true;
    }




	
}


