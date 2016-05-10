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

package edu.cmu.tetrad.data;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores a correlation matrix together with variable names and sample size;
 * intended as a representation of a data set.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5919 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class CorrelationMatrix extends CovarianceMatrix
        implements TetradSerializable {
    static final long serialVersionUID = 23L;

    //=============================CONSTRUCTORS=========================//

    /**
     * Constructs a new correlation matrix using the covariances in the given
     * covariance matrix.
     */
    public CorrelationMatrix(CovarianceMatrix matrix) {
        this(matrix.getVariables(),
                matrix.getMatrix(),
                matrix.getSampleSize());
    }

    /**
     * Constructs a new correlation matrix from the the given DataSet.
     */
    public CorrelationMatrix(RectangularDataSet dataSet) {
        super(Collections.unmodifiableList(dataSet.getVariables()),
                dataSet.getCorrelationMatrix(), dataSet.getNumRows());
        if (!dataSet.isContinuous()) {
            throw new IllegalArgumentException("Data set not continuous.");
        }
    }

    /**
     * Constructs a correlation matrix data set using the given information. The
     * matrix matrix is internally converted to a correlation matrix.
     */
    private CorrelationMatrix(List<Node> variable, DoubleMatrix2D matrix,
            int sampleSize) {
        super(variable, MatrixUtils.convertCovToCorr(matrix), sampleSize);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CovarianceMatrix serializableInstance() {
        return new CorrelationMatrix(new LinkedList<Node>(),
                new DenseDoubleMatrix2D(0, 0), 0);
    }

    //=================================PUBLIC METHODS======================//

    @Override
	public final void setMatrix(DoubleMatrix2D matrix) {
        MatrixUtils.isSquareC(matrix);

        for (int i = 0; i < matrix.rows(); i++) {
            if (Math.abs(matrix.getQuick(i, i) - 1.0) > 1.e-5) {
                throw new IllegalArgumentException(
                        "For a correlation matrix, " +
                                "variances (diagonal elements) must be 1.0");
            }
        }

        super.setMatrix(matrix);
    }

    public CorrelationMatrix getSubCorrMatrix(String[] submatrixVarNames) {
        CovarianceMatrix covarianceMatrix = getSubmatrix(submatrixVarNames);
        return new CorrelationMatrix(covarianceMatrix);
    }

    public CorrelationMatrix getSubCorrMatrix(int[] indices) {
        CovarianceMatrix covarianceMatrix = getSubmatrix(indices);
        return new CorrelationMatrix(covarianceMatrix);
    }
}


