package edu.cmu.tetrad.search;

import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.data.RectangularDataSet;

public class PermutationMatrixPair {

	public DoubleMatrix2D matrixW;
	public RectangularDataSet matrixBhat;
	public DoubleMatrix2D matrixA;
	
	public List<Integer> permutation;
	
}
