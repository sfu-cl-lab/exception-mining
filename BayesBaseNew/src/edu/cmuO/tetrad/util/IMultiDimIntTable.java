package edu.cmu.tetrad.util;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jan 2, 2007 Time: 10:01:36 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IMultiDimIntTable {
    int getCellIndex(int[] coords);

    int[] getCoordinates(int cellIndex);

    long increment(int[] coords, int value);

    long getValue(int[] coords);

    void setBoundsEnforced(boolean boundsEnforced);

    int getNumCells();

    int[] getDimensions();

    int getDimension(int var);

    int getNumDimensions();

    void reset(int[] dims);

    int getDims(int varIndex);
}
