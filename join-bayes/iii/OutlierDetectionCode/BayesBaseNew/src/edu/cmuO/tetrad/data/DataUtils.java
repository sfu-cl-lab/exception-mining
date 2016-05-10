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
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.PersistentRandomUtil;

import java.rmi.MarshalledObject;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * Some static utility methods for dealing with data sets.
 *
 * @author Various folks.
 */
public final class DataUtils {


    public static void copyColumn(Node node, RectangularDataSet source, RectangularDataSet dest) {
        int sourceColumn = source.getColumn(node);
        int destColumn = dest.getColumn(node);
        if(sourceColumn < 0){
            throw new NullPointerException("The given node was not in the source dataset");
        }
        if(destColumn < 0){
            throw new NullPointerException("The given node was not in the destination dataset");
        }
        int sourceRows = source.getNumRows();
        int destRows = dest.getNumRows();
        if (node instanceof ContinuousVariable) {
            for (int i = 0; i < destRows && i < sourceRows; i++) {
               dest.setDouble(i, destColumn, source.getDouble(i, sourceColumn));
            }
        } else if (node instanceof DiscreteVariable) {
            for(int i = 0; i<destRows && i < sourceRows; i++){
                dest.setInt(i, destColumn, source.getInt(i, sourceColumn));
            }
        } else {
            throw new IllegalArgumentException("The given variable most be discrete or continuous");
        }
    }


    /**
     * States whether the given column of the given data set is binary.
     *
     * @param data
     * @param column
     * @return - true iff the column is binary.
     */
    public static boolean isBinary(RectangularDataSet data, int column) {
        Node node = data.getVariable(column);
        int size = data.getNumRows();
        if (node instanceof DiscreteVariable) {
            for (int i = 0; i < size; i++) {
                int value = data.getInt(i, column);
                if (value != 1 && value != 0) {
                    return false;
                }
            }
        } else if (node instanceof ContinuousVariable) {
            for (int i = 0; i < size; i++) {
                double value = data.getDouble(i, column);
                if (value != 1.0 && value != 0.0) {
                    return false;
                }
            }
        } else {
            throw new IllegalArgumentException("The given column is not discrete or continuous");
        }
        return true;
    }


    /**
     * Throws an exception just in case not all of the variables from
     * <code>source1</code> are found in <code>source2</code>. A variable from
     * <code>source1</code> is found in <code>source2</code> if it is equal to a
     * variable in <code>source2</code>.
     */
    public static void ensureVariablesExist(VariableSource source1,
                                            VariableSource source2) {
        List<Node> variablesNotFound = source1.getVariables();
        variablesNotFound.removeAll(source2.getVariables());

        if (!variablesNotFound.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expected to find these variables from the given Bayes PM " +
                            "\nin the given discrete data set, but didn't (note: " +
                            "\ncategories might be different or in the wrong order): " +
                            "\n" + variablesNotFound);
        }
    }

    /**
     * Returns the default category for index i. (The default category should
     * ALWAYS be obtained by calling this method.)
     */
    public static String defaultCategory(int index) {
        return Integer.toString(index);
    }

    /**
     * Adds missing data values to cases in accordance with probabilities
     * specified in a double array which has as many elements as there are
     * columns in the input dataset.  Hence if the first element of the array of
     * probabilities is alpha, then the first column will contain a -99 (or
     * other missing value code) in a given case with probability alpha. </p>
     * This method will be useful in generating datasets which can be used to
     * test algorithms that handle missing data and/or latent variables. </p>
     * Author:  Frank Wimberly
     */
    public static RectangularDataSet addMissingData(
            RectangularDataSet inData, double[] probs) {
        RectangularDataSet outData;

        try {
            outData = (RectangularDataSet) new MarshalledObject(inData).get();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (probs.length != outData.getNumColumns()) {
            throw new IllegalArgumentException(
                    "Wrong number of elements in prob array");
        }

        for (double prob : probs) {
            if (prob < 0.0 || prob > 1.0) {
                throw new IllegalArgumentException("Probability out of range");
            }
        }

        for (int j = 0; j < outData.getNumColumns(); j++) {
            Node variable = outData.getVariable(j);

            for (int i = 0; i < outData.getNumRows(); i++) {
                double test = PersistentRandomUtil.getInstance().nextDouble();

                if (test < probs[j]) {
                    outData.setObject(i, j,
                            ((Variable) variable).getMissingValueMarker());
                }
            }
        }

        return outData;
    }

    /**
     * A continuous data set used to construct some other serializable
     * instances.
     */
    public static RectangularDataSet continuousSerializableInstance() {
        List<Node> variables = new LinkedList<Node>();
        variables.add(new ContinuousVariable("X"));
        return new ColtDataSet(10, variables);
    }

    /**
     * A discrete data set used to construct some other serializable instances.
     */
    public static RectangularDataSet discreteSerializableInstance() {
        List<Node> variables = new LinkedList<Node>();
        variables.add(new DiscreteVariable("X", 2));
        RectangularDataSet dataSet = new ColtDataSet(2, variables);
        dataSet.setInt(0, 0, 0);
        dataSet.setInt(1, 0, 1);
        return dataSet;
    }

    /**
     * Returns true iff the data sets contains a missing value.
     */
    public static boolean containsMissingValue(DoubleMatrix2D data) {
        for (int i = 0; i < data.rows(); i++) {
            for (int j = 0; j < data.columns(); j++) {
                if (Double.isNaN(data.getQuick(i, j))) {
                    return true;
                }
            }
        }

        return false;
    }


    public static boolean containsMissingValue(RectangularDataSet data) {
        for (int j = 0; j < data.getNumColumns(); j++) {
            Node node = data.getVariable(j);

            if (node instanceof ContinuousVariable) {
                for (int i = 0; i < data.getNumRows(); i++) {
                    if (Double.isNaN(data.getDouble(i, j))) {
                        return true;
                    }
                }
            }

            if (node instanceof DiscreteVariable) {
                for (int i = 0; i < data.getNumRows(); i++) {
                    if (data.getDouble(i, j) == -99) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Creates new time series dataset from the given one (fixed to deal with mixed datasets)
     */
    public static RectangularDataSet createTimeSeriesData(RectangularDataSet data, int numOfLags) {
        List<Node> variables = data.getVariables();
        int dataSize = variables.size();
        int laggedRows = data.getNumRows() - numOfLags + 1;
        Knowledge knowledge = new Knowledge();
        Node[][] laggedNodes = new Node[numOfLags][dataSize];
        List<Node> newVariables = new ArrayList<Node>(numOfLags * dataSize + 1);
        for (int lag = 0; lag < numOfLags; lag++) {
            for (int col = 0; col < dataSize; col++) {
                Node node = variables.get(col);
                String varName = node.getName();
                Node laggedNode;
                if (node instanceof ContinuousVariable) {
                    laggedNode = new ContinuousVariable(varName + "." + (lag + 1));
                } else if (node instanceof DiscreteVariable) {
                    DiscreteVariable var = (DiscreteVariable) node;
                    laggedNode = new DiscreteVariable(var);
                    var.setName(varName + "." + (lag + 1));
                } else {
                    throw new IllegalStateException("Node must be either continuous or discrete");
                }
                newVariables.add(laggedNode);
                laggedNode.setCenter(80 * col + 50, 80 * (numOfLags - lag) + 50);
                laggedNodes[lag][col] = laggedNode;
                knowledge.addToTier(lag, laggedNode.getName());
            }
        }
        RectangularDataSet laggedData = new ColtDataSet(laggedRows, newVariables);
        for (int lag = 0; lag < numOfLags; lag++) {
            for (int col = 0; col < dataSize; col++) {
                for (int row = 0; row < laggedRows; row++) {
                    Node laggedNode = laggedNodes[lag][col];
                    if (laggedNode instanceof ContinuousVariable) {
                        double value = data.getDouble(row + lag, col);
                        laggedData.setDouble(row, col + lag * dataSize, value);
                    } else {
                        int value = data.getInt(row + lag, col);
                        laggedData.setInt(row, col + lag * dataSize, value);
                    }
                }
            }
        }

        knowledge.setDefaultToKnowledgeLayout(true);
        laggedData.setKnowledge(knowledge);
        return laggedData;
    }

    public static DoubleMatrix2D standardizeData(DoubleMatrix2D data) {
        DoubleMatrix2D data2 = data.like();

        for (int j = 0; j < data.columns(); j++) {
            double sum = 0.0;

            for (int i = 0; i < data.rows(); i++) {
                sum += data.get(i, j);
            }

            double mean = sum / data.rows();

            double norm = 0.0;

            for (int i = 0; i < data.rows(); i++) {
                double v = data.get(i, j) - mean;
                norm += v * v;
            }

            norm = Math.sqrt(norm);

            for (int i = 0; i < data.rows(); i++) {
                data2.set(i, j, (data.get(i, j) - mean) / norm);
            }
        }

        return data2;
    }
}


