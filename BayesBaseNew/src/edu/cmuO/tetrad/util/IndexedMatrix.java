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
 * Returns values of the given square matrix, where the indices are remapped via
 * the given indices array. If the supplied matrix is 6 x 6, for example, and
 * the indices set are [5 4 2 1], then getValue(1, 2) will return element [4][2]
 * of the given matrix.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5177 $ $Date: 2006-01-05 17:09:26 -0500 (Thu, 05 Jan
 *          2006) $
 */
public final class IndexedMatrix {

    /**
     * A square matrix.
     */
    private final double[][] matrix;

    /**
     * Indices into this matrix, implicitly specifying a submatrix.
     */
    private int[] indices;

    /**
     * Constructs a new IndexedMatrix for the given matrix.
     */
    public IndexedMatrix(double[][] matrix) {
        assert MatrixUtils.isSquare(matrix);
        this.matrix = MatrixUtils.copy(matrix);
        setIndices(new int[0]);
    }

    /**
     * Returns the index array.
     */
    public final int[] getIndices() {
        return indices;
    }

    /**
     * Sets the index array. The index array must be of length <= the order of
     * the matrix, each element of which is distinct and < the order of the
     * matrix.
     */
    public final void setIndices(int[] indices) {
        if (indices == null) {
            throw new NullPointerException("Permutation must not be null.");
        }
        if (!isLegal(indices)) {
            throw new IllegalArgumentException(
                    "Illegal index array: " + ArrUtils.toString(indices));
        }
        this.indices = ArrUtils.copy(indices);
    }

    /**
     * Returns the value of the implied submatrix at the given indices-- that
     * is, matrix[getIndices()[i]][getIndices()[j]].
     */
    public final double getValue(int i, int j) {
        return matrix[indices[i]][indices[j]];
    }

    /**
     * Returns true iff the given array consists only of numbers in the range
     * 0..indices.length - 1, with no duplicates.
     */
    private boolean isLegal(int[] indices) {
        int[] check = new int[matrix.length];
        for (int indice : indices) {
            if (indice < 0 || indice >= matrix.length) {
                return false;
            }
            check[indice]++;
        }

        for (int i = 0; i < matrix.length; i++) {
            if (check[i] > 1) {
                return false;
            }
        }

        return true;
    }
}


