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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Stores a covariance matrix together with variable names and sample size,
 * intended as a representation of a data set. When constructed from a
 * continuous data set, the matrix is not checked for positive definiteness;
 * however, when a covariance matrix is supplied, its positive definiteness is
 * always checked. If the sample size is less than the number of variables, the
 * positive definiteness is "spot-checked"--that is, checked for various
 * submatrices.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5935 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 * @see CorrelationMatrix
 */
public class CovarianceMatrix implements DataModel, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The name of the covariance matrix.
     *
     * @serial May be null.
     */
    private String name;

    /**
     * The variables (in order) for this covariance matrix.
     *
     * @serial Cannot be null.
     */
    private List<Node> variables;

    /**
     * The size of the sample from which this covariance matrix was calculated.
     *
     * @serial Range > 0.
     */
    private int sampleSize;

    /**
     * @serial Do not remove this field; it is needed for serialization.
     * @deprecated
     */
    @Deprecated
	private double[][] matrix;

    /**
     * Stored matrix data. Should be square. This may be set by derived classes,
     * but it must always be set to a legitimate covariance matrix.
     *
     * @serial Cannot be null. Must be symmetric and positive definite.
     */
    private DoubleMatrix2D matrixC;

    /**
     * The list of selected variables.
     *
     * @serial Cannot be null.
     */
    private Set<Node> selectedVariables = new HashSet<Node>();

    /**
     * The knowledge for this data.
     *
     * @serial Cannot be null.
     */
    private Knowledge knowledge = new Knowledge();

    //=============================CONSTRUCTORS=========================//

    /**
     * Constructs a new covariance matrix from the given DataSet.
     *
     * @throws IllegalArgumentException if this is not a continuous data set.
     */
    public CovarianceMatrix(RectangularDataSet dataSet) {
        if (!dataSet.isContinuous()) {
            throw new IllegalArgumentException("Not a continuous data set.");
        }

        this.variables = Collections.unmodifiableList(dataSet.getVariables());
        this.sampleSize = dataSet.getNumRows();
        this.matrixC = dataSet.getCovarianceMatrix();
    }                                         

    /**
     * Constructs a new correlation matrix using the covariances in the given
     * covariance matrix.
     */
    public CovarianceMatrix(CovarianceMatrix covMatrix) {
        this(covMatrix.variables, covMatrix.matrixC.copy(),
                covMatrix.sampleSize);
    }

    /**
     * Constructs a new covariance matrix by making new variables from all of
     * the supplied variable names and using the given symmetric, positive
     * definite matrix and sample size. The number of variables must equal the
     * dimension of the array.
     *
     * @param varNames   the list of variables (in order) for the covariance
     *                   matrix.
     * @param matrix     an square array of containing covariances.
     * @param sampleSize the sample size of the data for these covariances.
     * @throws IllegalArgumentException if the given matrix is not symmetric
     *                                  positive definite, if the number of
     *                                  variables does not equal the dimension
     *                                  of m, or if the sample size is not
     *                                  positive.
     */
    public CovarianceMatrix(String[] varNames, DoubleMatrix2D matrix,
            int sampleSize) {
        this(createVariables(varNames), matrix, sampleSize);
//        checkMatrix();
    }

    /**
     * Constructs a new covariance matrix by making new variables from all of
     * the supplied variable names and using the given symmetric, positive
     * definite matrix and sample size. The number of variables must equal the
     * dimension of the array.
     *
     * @param varNames   the list of variables (in order) for the covariance
     *                   matrix.
     * @param matrix     an square array of containing covariances.
     * @param sampleSize the sample size of the data for these covariances.
     * @throws IllegalArgumentException if the given matrix is not symmetric
     *                                  positive definite, if the number of
     *                                  variables does not equal the dimension
     *                                  of m, or if the sample size is not
     *                                  positive.
     * @deprecated Use a COLT DoubleMatrix2D matrix instead.
     */
    @Deprecated
	public CovarianceMatrix(String[] varNames, double[][] matrix,
            int sampleSize) {
        this(createVariables(varNames), new DenseDoubleMatrix2D(matrix),
                sampleSize);
        checkMatrix();
    }

    /**
     * Protected constructor to construct a new covariance matrix using the
     * supplied continuous variables and the the given symmetric, positive
     * definite matrix and sample size. The number of variables must equal the
     * dimension of the array.
     *
     * @param variables  the list of variables (in order) for the covariance
     *                   matrix These must be ContinuousVariable's. //     *
     * @param matrix     an square array of containing covariances.
     * @param sampleSize the sample size of the data for these covariances.
     * @throws IllegalArgumentException if the given matrix is not symmetric (to
     *                                  a tolerance of 1.e-5) and positive
     *                                  definite, if the number of variables
     *                                  does not equal the dimension of m, or if
     *                                  the sample size is not positive.
     */
    protected CovarianceMatrix(List<Node> variables, DoubleMatrix2D matrix,
            int sampleSize) {
        this.variables = Collections.unmodifiableList(variables);
        this.sampleSize = sampleSize;
        this.matrixC = matrix.copy();
    }


    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CovarianceMatrix serializableInstance() {
        List<Node> variables = new ArrayList<Node>();
        ContinuousVariable x = new ContinuousVariable("X");
        variables.add(x);
        DoubleMatrix2D matrix = MatrixUtils.identityC(1);
        return new CovarianceMatrix(variables, matrix, 100);
    }

    //============================PUBLIC METHODS=========================//

    /**
     * Returns the list of variables (unmodifiable).
     */
    @Override
	public final List<Node> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable names, in order.
     */
    @Override
	public final List<String> getVariableNames() {
        List<String> names = new ArrayList<String>();

        for (int i = 0; i < getVariables().size(); i++) {
            Node variable = getVariables().get(i);
            names.add(variable.getName());
        }

        return names;
    }

    /**
     * Returns the variable name at the given index.
     */
    public final String getVariableName(int index) {
        if (index >= getVariables().size()) {
            throw new IllegalArgumentException("Index out of range: " + index);
        }

        Node variable = getVariables().get(index);
        return variable.getName();
    }

    /**
     * Returns the dimension of the covariance matrix.
     */
    public final int getDimension() {
        return variables.size();
    }

    /**
     * The size of the sample used to calculated this covariance matrix.
     *
     * @return The sample size (> 0).
     */
    public final int getSampleSize() {
        return this.sampleSize;
    }

    /**
     * Gets the name of the covariance matrix.
     */
    @Override
	public final String getName() {
        return this.name;
    }

    /**
     * Sets the name of the covariance matrix.
     */
    @Override
	public final void setName(String name) {
        this.name = name;
    }

    @Override
	public final Knowledge getKnowledge() {
        return new Knowledge(this.knowledge);
    }

    @Override
	public final void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = new Knowledge(knowledge);
    }

    public final CovarianceMatrix getSubmatrix(int[] indices) {
        List<Node> submatrixVars = new LinkedList<Node>();

        for (int indice : indices) {
            submatrixVars.add(variables.get(indice));
        }

        DoubleMatrix2D cov = matrixC.viewSelection(indices, indices);
        return new CovarianceMatrix(submatrixVars, cov, getSampleSize());
    }

    /**
     * Returns a submatrix of this matrix
     */
    public final CovarianceMatrix getSubmatrix(String[] submatrixVarNames) {
        List<Node> submatrixVars = new LinkedList<Node>();

        for (String submatrixVarName : submatrixVarNames) {
            submatrixVars.add(getVariable(submatrixVarName));
        }

        // This breaks MIMBUILD.
//        Collections.sort(submatrixVars, new Comparator<Node>() {
//            public int compare(Node o1, Node o2) {
//                return getVariables().indexOf(o1) - getVariables().indexOf(o2);
//            }
//        });

        if (!getVariables().containsAll(submatrixVars)) {
            throw new IllegalArgumentException(
                    "The variables in the submatrix " +
                            "must be in the original matrix: original==" +
                            getVariables() + ", sub==" + submatrixVars);
        }

        for (int i = 0; i < submatrixVars.size(); i++) {
            if (submatrixVars.get(i) == null) {
                throw new NullPointerException(
                        "The variable name at index " + i + " is null.");
            }
        }

        int[] indices = new int[submatrixVars.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = getVariables().indexOf(submatrixVars.get(i));
        }

        DoubleMatrix2D cov = matrixC.viewSelection(indices, indices);
        return new CovarianceMatrix(submatrixVars, cov, getSampleSize());
    }

    /**
     * Returns the value of element (i,j) in the matrix
     */
    public final double getValue(int i, int j) {
        return matrixC.getQuick(i, j);
    }

    public void setMatrix(DoubleMatrix2D matrix) {
        this.matrixC = matrix.copy();
        checkMatrix();
    }

    public final void setSampleSize(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be > 0.");
        }

        this.sampleSize = sampleSize;
    }

    /**
     * Returns the size of the square matrix.
     */
    public final int getSize() {
        return matrixC.rows();
    }

    /**
     * Returns a copy of the covariance matrix as a double[][] matrix.
     */
    public final DoubleMatrix2D getMatrix() {
        return matrixC;
    }

    public final void select(Node variable) {
        if (variables.contains(variable)) {
            getSelectedVariables().add(variable);
        }
    }

    public final void clearSelection() {
        getSelectedVariables().clear();
    }

    public final boolean isSelected(Node variable) {
        if (variable == null) {
            throw new NullPointerException("Null variable. Try again.");
        }

        return getSelectedVariables().contains(variable);
    }

    public final List<String> getSelectedVariableNames() {
        List<String> selectedVariableNames = new LinkedList<String>();

        for (Node variable : selectedVariables) {
            selectedVariableNames.add(variable.getName());
        }

        return selectedVariableNames;
    }

    /**
     * Prints out the matrix
     */
    @Override
	public final String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("\nCovariance matrix:");
        buf.append("\n\tVariables = ").append(getVariables());
        buf.append("\n\tSample size = ").append(getSampleSize());
        buf.append("\n");
        buf.append(MatrixUtils.toString(getMatrix().toArray()));

        if (getKnowledge() != null && !getKnowledge().isEmpty()) {
            buf.append(getKnowledge());
        }

        return buf.toString();
    }

    //========================PRIVATE METHODS============================//

    public ContinuousVariable getVariable(String name) {
        for (int i = 0; i < getVariables().size(); i++) {
            ContinuousVariable variable = (ContinuousVariable) getVariables()
                    .get(i);
            if (name.equals(variable.getName())) {
                return variable;
            }
        }

        return null;
    }

    private static List<Node> createVariables(String[] varNames) {
        List<Node> variables = new LinkedList<Node>();

        for (String varName : varNames) {
            variables.add(new ContinuousVariable(varName));
        }

        return variables;
    }

    private Set<Node> getSelectedVariables() {
        return selectedVariables;
    }

    /**
     * Checks the sample size, variable, and matrix information.
     */
    private void checkMatrix() {
        int numVars = variables.size();

        for (int i = 0; i < numVars; i++) {
            if (variables.get(i) == null) {
                throw new NullPointerException();
            }

            if (!(variables.get(i) instanceof ContinuousVariable)) {
                throw new IllegalArgumentException();
            }
        }

        if (sampleSize < 1) {
            throw new IllegalArgumentException(
                    "Sample size must be at least 1.");
        }

        if (numVars != matrixC.rows() || numVars != matrixC.columns()) {
            throw new IllegalArgumentException("Number of variables does not " +
                    "equal the dimension of the matrix.");
        }


        if (sampleSize > matrixC.rows()) {
            if (!MatrixUtils.isSymmetricPositiveDefinite(matrixC)) {
                throw new IllegalArgumentException(
                        "Matrix is not positive definite.");
            }
        }
        else {
            System.out.println(
                    "Covariance matrix cannot be positive definite since " +
                            "\nthere are more variables than sample points. Spot-checking " +
                            "\nsome submatrices.");

            for (int from = 0; from < numVars; from += sampleSize / 2) {
                int to = from + sampleSize / 2;

                if (to > numVars) {
                    to = numVars;
                }

                int[] indices = new int[to - from];

                for (int i = 0; i < indices.length; i++) {
                    indices[i] = from + i;
                }

                DoubleMatrix2D m2 = matrixC.viewSelection(indices, indices);

                if (!MatrixUtils.isSymmetricPositiveDefinite(m2)) {
                    throw new IllegalArgumentException(
                            "Positive definite spot-check " + "failed.");
                }
            }
        }
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (getVariables() == null) {
            throw new NullPointerException();
        }

        if (matrix != null) {
            matrixC = new DenseDoubleMatrix2D(matrix);
            matrix = null;
        }

        if (knowledge == null) {
            throw new NullPointerException();
        }

        if (sampleSize < -1) {
            throw new IllegalStateException();
        }

        if (selectedVariables == null) {
            selectedVariables = new HashSet<Node>();
        }
    }
}


