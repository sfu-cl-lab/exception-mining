package edu.cmuO.tetrad.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Discretizes individual columns of discrete or continuous data. Continuous
 * data is discretized by specifying a list of n - 1 cutoffs for n values in the
 * discretized data, with optional string labels for these values. Discrete data
 * is discretized by specifying a mapping from old value names to new
 * value names, the idea being that old values may be merged.
 *
 * @author Joseph Ramsey
 * @author Tyler Gibson
 * @version $Revision: 5177 $ $Date: 2006-01-09 11:04:56 -0500 (Mon, 09 Jan
 *          2006) $
 */
public class Discretization {



    /**
     * The variable that was discretized.
     */
    private final DiscreteVariable variable;

    /**
     * The discretized data.
     */
    private final int[] data;


    /**
     * Constructs the discretization given the variable and data.
     *
     * @param variable
     * @param data
     */
    private Discretization(DiscreteVariable variable, int[] data) {
        this.variable = variable;
        this.data = data;
    }

    //============================ Public Methods =================================//

    /**
     * Returns the discretized varaible.
     *
     * @return - discretized variable.
     */
    public final DiscreteVariable getVariable() {
        return variable;
    }

    /**
     * Returns the discretized data.
     *
     * @return - discretized data.
     */
    public final int[] getData() {
        return data;
    }


    /**
     * Returns an array of breakpoints that divides the data into equal sized buckets.
     *
     * @param _data
     * @param numberOfCategories
     * @return
     */
    public static double[] getEqualFrequencyBreakPoints(double[] _data, int numberOfCategories) {
        double[] data = new double[_data.length];
        System.arraycopy(_data, 0, data, 0, data.length);

        // first sort the data.
        Arrays.sort(data);
        List<Chunk> chunks = new ArrayList<Chunk>(data.length);
        int startChunkCount = 0;
        double lastValue = data[0];
        for(int i = 0; i<data.length; i++){
            double value = data[i];
            if(value != lastValue){
                chunks.add(new Chunk(startChunkCount, i, value));
                startChunkCount = i;
            }
            lastValue = value;
        }
        chunks.add(new Chunk(startChunkCount, data.length, data[data.length -1]));

        // now find the breakpoints.
//        double interval = data.length / (double) numberOfCategories;
        double interval = data.length / numberOfCategories;
        double[] breakpoints = new double[numberOfCategories - 1];
        int current = 0;
        int freq = 0;
        for(Chunk chunk : chunks){
            int valuesInChunk = chunk.getNumberOfValuesInChunk();
            int halfChunk = (int) (valuesInChunk * .5);
            // if more than half the values in the chunk fit this bucket then put here,
            // otherwise the chunk should be added to the next bucket.
            if(freq + halfChunk <= interval){
                freq += valuesInChunk;
            } else {
                freq = valuesInChunk;
            }

            if(interval <= freq){
                freq = 0;
                if(current < breakpoints.length){
                    breakpoints[current++] = chunk.value;
                }
            }
        }

        for (int i = current; i < breakpoints.length; i++) {
            breakpoints[i] = Double.POSITIVE_INFINITY;
        }

        double[] _breakpoints = new double[current];
        System.arraycopy(breakpoints, 0, _breakpoints, 0, current);

        return breakpoints;
    }


    /**
     * Discretizes the continuous data in the given column using the specified
     * cutoffs and category names. The following scheme is used. If cutoffs[i -
     * 1] < v <= cutoffs[i] (where cutoffs[-1] = negative infinity), then v is
     * mapped to category i. If category names are supplied, the discrete column
     * returned will use these category names.
     *
     * @param cutoffs      The cutoffs used to discretize the data. Should have
     *                     length c - 1, where c is the number of categories in
     *                     the discretized data.
     * @param variableName the name of the returned variable.
     * @param categories   An optional list of category names; may be null. If
     *                     this is supplied, the discrete column returned will
     *                     use these category names. If this is non-null, it
     *                     must have length c, where c is the number of
     *                     categories for the discretized data. If any category
     *                     names are null, default category names will be used
     *                     for those.
     * @return The discretized column.
     */
    public static Discretization discretize(double[] _data, double[] cutoffs,
                                            String variableName, List<String> categories) {

        if (cutoffs == null) {
            throw new NullPointerException();
        }

        for (int i = 0; i < cutoffs.length - 1; i++) {
            if (!(cutoffs[i] <= cutoffs[i + 1])) {
                throw new NullPointerException(
                        "Cutoffs must be in nondecreasing order.");
            }
        }

        if (variableName == null) {
            throw new NullPointerException();
        }

        int numCategories = cutoffs.length + 1;

        if (categories != null && categories.size() != numCategories) {
            throw new IllegalArgumentException("If specified, the list of " +
                    "categories names must be one longer than the length of " +
                    "the cutoffs array.");
        }

        DiscreteVariable variable;

        if (categories == null) {
            variable = new DiscreteVariable(variableName, numCategories);
        } else {
            variable = new DiscreteVariable(variableName, categories);
        }

        int[] discreteData = new int[_data.length];

        loop:
        for (int i = 0; i < _data.length; i++) {
            for (int j = 0; j < cutoffs.length; j++) {
                if (_data[i] < cutoffs[j]) {
                    discreteData[i] = j;
                    continue loop;
                }
            }

            discreteData[i] = cutoffs.length;
        }

        return new Discretization(variable, discreteData);
    }

//    /**
//     * Discretizes the continuous data in the given column using the specified
//     * cutoffs and category names. The following scheme is used. If cutoffs[i -
//     * 1] < v <= cutoffs[i] (where cutoffs[-1] = negative infinity), then v is
//     * mapped to category i. If category names are supplied, the discrete column
//     * returned will use these category names.
//     *
//     * @param remap        An index array with length equal to the number of
//     *                     categories of the given column, such that remap[c] is
//     *                     the index of the category that values of c in the old
//     *                     column should have in the new column. OK, it just
//     *                     maps old values to new values. The value remap[c]
//     *                     must be nonnegative for all c. The returned column
//     *                     will have categories 0, 1,..., max{remap[c]}.
//     * @param variableName the name of the returned variable.
//     * @param categories   An optional list of category names; may be null. If
//     *                     this is supplied, the discrete column returned will
//     *                     use these category names. If this is non-null, it
//     *                     must have length c, where c is the number of
//     *                     categories for the discretized data. If any category
//     *                     names are null, default category names will be used
//     *                     for those.
//     * @return The discretized column.
//     */
//    public static Discretization discretize(DiscreteVariable oldVariable,
//                                            int[] oldData, int[] remap, String variableName,
//                                            List<String> categories) {
//        if (remap == null) {
//            throw new NullPointerException();
//        }
//
//        int oldNumCategories = oldVariable.getNumCategories();
//
//        if (remap.length != oldNumCategories) {
//            throw new IllegalArgumentException("Remap array must have length " +
//                    "equal to the number of categories for the given column.");
//        }
//
//        if (variableName == null) {
//            throw new NullPointerException();
//        }
//
//        DiscreteVariable newVariable =
//                new DiscreteVariable(variableName, categories);
//
//        int[] newData = new int[oldData.length];
//
//        for (int i = 0; i < oldData.length; i++) {
//            if (oldData[i] == -99) {
//                newData[i] = -99;
//            } else {
//                newData[i] = remap[oldData[i]];
//            }
//        }
//
//        return new Discretization(newVariable, newData);
//    }


    @Override
	public final String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\nDiscretization:");

        for (int aData : data) {
            buf.append("\n").append(variable.getCategory(aData));
        }

        buf.append("\n");
        return buf.toString();
    }

    //========================= Private methods ======================//





    //======================== Inner class ================================//

    /**
     * Represents a chunk of data in a sorted array of data.  If low == high then
     * then the chunk only contains one member.
     */
    private static class Chunk {

        private int valuesInChunk;
        private double value;

        public Chunk(int low, int high, double value){
            this.valuesInChunk = (high - low);
            this.value = value;
        }

        public int getNumberOfValuesInChunk(){
            return this.valuesInChunk;
        }

    }

}
