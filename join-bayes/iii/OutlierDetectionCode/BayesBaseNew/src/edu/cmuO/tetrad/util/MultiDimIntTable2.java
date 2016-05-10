package edu.cmu.tetrad.util;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Stores a table of cells with int values of arbitrary dimension. The
 * dimensionality of the table is set in the reset() method; if a dimensionality
 * is set in the constructor, it will be passed to the reset() method. Every
 * time the dimensionality is changed, the table is reset to zero throughout. If
 * the dimensionality is set to null, the table cannot be used until a non-null
 * dimensionality is set.</p>
 *
 * @author Joseph Ramsey
 * @version $Revision: 5308 $ $Date: 2006-01-05 17:09:26 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class MultiDimIntTable2 implements IMultiDimIntTable {

    /**
     * A single-dimension array containing all of the cells of the table. Must
     * be at least long enough to contain data for each cell allowed for by the
     * given dimension array--in other words, the length must be greater than or
     * equal to dims[0] & dims[1] ... * dims[dims.length - 1].
     */
    private Map<Integer, Long> cells;

    /**
     * The number of cells in the table. (May be different from the length of
     * cells[].
     */
    private int numCells;

    /**
     * An array whose length is the number of dimensions of the cell and whose
     * contents, for each value dims[i], are the numbers of values for each
     * i'th dimension. Each of these dimensions must be an integer greater than
     * zero.
     */
    public int[] dims;

    /**
     * Indicates whether bounds on coordinate values are explicitly enforced.
     * This may slow down loops.
     */
    private boolean boundsEnforced = true;

    /**
     * Constructs a new multidimensional table of integer cells, with the given
     * (fixed) dimensions. Each dimension must be an integer greater than zero.
     *
     * @param dims An int[] array of length > 0, each element of which specifies
     *             the number of values of that dimension (> 0).
     */
    public MultiDimIntTable2(int[] dims) {
        reset(dims);
    }

    /**
     * Returns the index in the cells array for the cell at the given
     * coordinates.
     *
     * @param coords The coordinates of the cell. Each value must be less
     *               than the number of possible value for the corresponding
     *               dimension in the table. (Enforced.)
     * @return the row in the table for the given node and combination of parent
     *         values.
     */
    @Override
	public int getCellIndex(int[] coords) {
        int cellIndex = 0;

//        if (isBoundsEnforced()) {
//            if (coords.length != dims.length) {
//                throw new IllegalArgumentException(
//                        "Coordinate array must have the " +
//                                "proper number of dimensions.");
//            }
//
//            for (int i = 0; i < coords.length; i++) {
//                if ((coords[i] < 0) || (coords[i] >= dims[i])) {
//                    throw new IllegalArgumentException("Coordinate #" + i +
//                            " is out of bounds: " + coords[i]);
//                }
//            }
//        }

        for (int i = 0; i < dims.length; i++) {
            cellIndex *= dims[i];
            cellIndex += coords[i];
        }

        return cellIndex;
    }

    /**
     * Returns an array containing the coordinates of the cell at the given
     * index in the cells array.
     *
     * @param cellIndex an <code>int</code> value
     * @return the array representing the combination of parent values for this
     *         row.
     */
    @Override
	public int[] getCoordinates(int cellIndex) {
        int[] coords = new int[this.dims.length];

        for (int i = this.dims.length - 1; i >= 0; i--) {
            coords[i] = cellIndex % this.dims[i];
            cellIndex /= this.dims[i];
        }

        return coords;
    }

    /**
     * Increments the value at the given coordinates by the specified amount,
     * returning the new value.
     *
     * @param coords The coordinates of the table cell to update.
     * @param value  The amount by which the table cell at these coordinates
     *               should be incremented (an integer).
     * @return the new value at that table cell.
     */
    @Override
	public long increment(int[] coords, int value) {
        int cellIndex = getCellIndex(coords);

        if (!cells.containsKey(cellIndex)) {
            cells.put(cellIndex, 0L);
        }

        cells.put(cellIndex, cells.get(cellIndex) + 1);
        return cells.get(cellIndex);

//        this.cells[cellIndex] += value;
//        return this.cells[cellIndex];
    }

    /**
     * Sets the value at the given coordinates to the given value,
     * returning the new value.
     *
     * @param coords The coordinates of the table cell to update.
     * @param value  The amount by which the table cell at these coordinates
     *               should be incremented (an integer).
     * @return the new value at that table cell.
     */
    protected long setValue(int[] coords, int value) {
        int cellIndex = getCellIndex(coords);
        cells.put(cellIndex, (long) value);
        return cells.get(cellIndex);

//        this.cells[cellIndex] = value;
//        return this.cells[cellIndex];
    }

    /**
     * Returns the value at the given coordinates.
     *
     * @param coords The coordinates of the table cell to update.
     * @return the new value at that table cell.
     */
    @Override
	public long getValue(int[] coords) {
        int cellIndex = getCellIndex(coords);

        if (cells == null || !cells.containsKey(cellIndex)) {
            return 0L;
        }
        else {
            return cells.get(cellIndex);
        }

//        return this.cells[cellIndex];
    }

    /**
     * Returns true iff bounds are being strictly enforced on coordinate
     * values.
     *
     * @return this value (true by default).
     */
    private boolean isBoundsEnforced() {
        return this.boundsEnforced;
    }

    /**
     * Sets whether bounds should be enforced for coordinate values.
     *
     * @param boundsEnforced true if bounds should be enforced, false if not.
     *                       (True by default.)
     */
    @Override
	public void setBoundsEnforced(boolean boundsEnforced) {
        this.boundsEnforced = boundsEnforced;
    }

    /**
     * Returns the number of cells.
     *
     * @return this number.
     */
    @Override
	public int getNumCells() {
        return this.numCells;
    }

    /**
     * Returns the dimension array.
     *
     * @return this number.
     */
    @Override
	public int[] getDimensions() {
        return this.dims;
    }

    /**
     * Describe <code>getDimension</code> method here.
     *
     * @param var an <code>int</code> value
     * @return an <code>int</code> value
     */
    @Override
	public int getDimension(int var) {
        return this.dims[var];
    }

    /**
     * Returns the number of dimensions.
     *
     * @return an <code>int</code> value
     */
    @Override
	public int getNumDimensions() {
        return this.dims.length;
    }

    /**
     * Resets the table, allowing a different dimensionality. All cells are
     * reset to zero. The underlying data array is reused if possible.
     *
     * @param dims an <code>int[]</code> value
     */
    @Override
	public final void reset(int[] dims) {
        if (dims == null) {
            cells = null;
        }
        else {
            if (dims.length < 1) {
                throw new IllegalArgumentException(
                        "Table must have at " + "least one dimension.");
            }

            for (int i = 0; i < dims.length; i++) {
                if (dims[i] < 1) {
                    throw new IllegalArgumentException("Dimension " + i +
                            " has fewer than " + "one values: " + dims[i]);
                }
            }

            // Calculate length of cells[] array.
            this.numCells = 1;

            for (int dim : dims) {
                this.numCells *= dim;
            }

            // Construct (or reset) cells array.
            cells = new HashMap<Integer, Long>();

//            if ((cells == null) || (cells.length < this.numCells)) {
//                cells = new long[this.numCells];
//            }
//            else {
//                Arrays.fill(cells, 0);
//            }

            // Store the dimensions, making a copy for security.
            this.dims = new int[dims.length];

            System.arraycopy(dims, 0, this.dims, 0, dims.length);
        }
    }

    @Override
	public int getDims(int varIndex) {
        return dims[varIndex];
    }
}
