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

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * <p>Stores a column of count data--that is, data that ranges over positive
 * integers and represents counts of things.  The implementation uses an int[]
 * array to store the data.  Methods implementing the Column interface are
 * provided to allow the column to be edited using objects (Strings and
 * Numbers). It is not intended for these methods to be used directly by
 * algorithms operating on the underlying data.  Rather, for algorithms, the
 * underlying array should be obtained and used directly.  To obtain the
 * underlying array, use the getRawData() method and cast to int[] --e.g.,
 * <pre>    int[] data = (int[])getRawData(); </pre>
 * This array may be longer than the actual data represented.  The actual data
 * will range from index 0 to index (size() - 1).</p> </p> <p>This type of
 * column may be used to store a column of counts in a DataSet; however, if
 * these counts need to be used in a statistical analysis, the data should be
 * converted to discrete or continuous data first.</p>
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5308 $ $Date: 2006-01-20 13:45:01 -0500 (Fri, 20 Jan
 *          2006) $
 * @deprecated Was never used.
 */
@Deprecated
public final class CountColumn extends AbstractList
        implements Column, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The variable for this column.
     *
     * @serial
     */
    private CountVariable variable;

    /**
     * The data for this column.
     *
     * @serial
     */
    private int[] data;

    /**
     * The number of data points stored in this column.  Data points range from
     * index 0 toi index (size() - 1); The rest of the elements in the data
     * array are undefined.
     *
     * @serial
     */
    private int size = 0;

    //=============================CONSTRUCTORS========================//

    /**
     * Constructs a new ContinuousColumn using the given variable.
     *
     * @param var the variable for the column.
     */
    private CountColumn(final CountVariable var) {
        this.variable = var;
        this.data = new int[100];
        Arrays.fill(this.data, CountVariable.getIntMissingValue());
    }

    /**
     * Constructs a new ContinuousColumn using the given variable and
     * initialized using the given array, the first 'size' values of which are
     * meaningful data.
     *
     * @param var  the variable for the column.
     * @param data the initial data for the column.
     * @param size the number of actual data points in the column.
     */
    public CountColumn(CountVariable var, int[] data, int size) {
        if (data.length < size()) {
            throw new IllegalArgumentException("The length of the data array " +
                    "is less than the specified size; more data points are needed.");
        }

        this.variable = var;
        this.data = data;
        this.size = size;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CountColumn serializableInstance() {
        return new CountColumn(CountVariable.serializableInstance());
    }

    //==========================PUBLIC METHODS==========================//

    /**
     * Inserts the given object (Number or String) into the column at position
     * 'index'.  Data is shifted down in the column  to accomodate.
     *
     * @param index   the index at which the new value is inserted.
     * @param element the Object to add.
     */
    @Override
	public final void add(int index, Object element) {
        int datum = getValueFromObject(element);

        if (index < this.size) {
            ensureCapacity(this.size);
            System.arraycopy(this.data, index, this.data, index + 1,
                    this.size - index);
            this.data[index] = datum;
            this.size++;
        }
        else {
            ensureCapacity(index + 1);
            Arrays.fill(this.data, this.size, index,
                    CountVariable.getIntMissingValue());
            this.data[index] = datum;
            this.size = index + 1;
        }
    }

    /**
     * Ensures that the backing array has as length of at least 'minCapacity'.
     *
     * @param minCapacity
     */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = this.data.length;

        if (minCapacity > oldCapacity) {
            int[] oldData = this.data;
            int newCapacity = oldCapacity * 2;

            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            data = new int[newCapacity];
            Arrays.fill(this.data, CountVariable.getIntMissingValue());

            System.arraycopy(oldData, 0, this.data, 0, this.size);
        }
    }

    /**
     * Returns the value at the given index as a Integer object.
     *
     * @param index the index of the value sought.
     * @return the value of the array at 'index', or null if the index is out
     *         of range.
     */
    @Override
	public final Object get(int index) {
        return (index < this.size) ? this.data[index] : null;
    }

    /**
     * Returns the underlying int[] array as an Object.  Must be cast to int[].
     * Elements in the array beyond the indicated size of the column returned by
     * size() are not meaningful.
     *
     * @return the supporting array for the column.
     */
    @Override
	public final Object getRawData() {
        return data;
    }

    /**
     * Attempts to translate the given Object into a int value.  The Object
     * must be either a Number or a String.
     *
     * @param element the Object to translate.
     * @return the translated int value.
     */
    private int getValueFromObject(Object element) {
        if ("*".equals(element)) {
            return CountVariable.getIntMissingValue();
        }
        else if (element instanceof Number) {
            return ((Number) element).intValue();
        }
        else if (element instanceof String) {
            return new Integer((String) element);
        }
        else {
            throw new IllegalArgumentException(
                    "The argument 'element' must be " +
                            "either a Number or a String.");
        }
    }

    /**
     * Returns the AbstractVariable for this column, a ContinuousVariable.
     *
     * @return this column's variable.
     * @see Variable
     */
    @Override
	public final Node getVariable() {
        return variable;
    }

    /**
     * Removes the value at the specified index of the column and shifts all
     * values below 'index' up.
     *
     * @param index the index of the element to remove.
     * @return the value of the old number at that index, returned as a
     *         Integer object.
     */
    @Override
	public final Object remove(int index) {
        if (index >= this.size) {
            throw new ArrayIndexOutOfBoundsException("Specified index out of " +
                    "bounds for this column: " + index + " >= " + size);
        }
        else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Index must be >= 0.");
        }

        Object oldDatum = this.data[index];
        int j = this.size - index - 1;

        if (j > 0) {
            System.arraycopy(this.data, index + 1, this.data, index, j);
        }

        Arrays.fill(this.data, index + j, this.data.length,
                CountVariable.getIntMissingValue());

        size--;

        return oldDatum;
    }

    /**
     * Sets the value at position 'index' in the column to the int value
     * of 'value', which must be either a Number or a String.
     *
     * @param index the index at which the new value is set.
     * @param value the Object to set.
     * @return the old value which was set at position 'index' as a Integer
     *         object.
     */
    @Override
	public final Object set(int index, Object value) {
        if (value == null) {
            throw new NullPointerException("Value must not be null.");
        }

        Object oldDatum = get(index);
        int datum = getValueFromObject(value);

        ensureCapacity(index + 1);

        this.data[index] = datum;

        if (index + 1 > this.size) {
            this.size = index + 1;
        }

        return oldDatum;
    }

    /**
     * The size of the column.  Note that this is the number of elements in the
     * supporting array which are actual data points (index 0 to index size -
     * 1), not the length of the supporting array, which is typically longer
     * than this.
     *
     * @return the number of data points in the column.
     */
    @Override
	public final int size() {
        return this.size;
    }

    /**
     * Override the equals method in AbstractList so that columns are equal iff
     * they are object identical.
     */
    @Override
	public final boolean equals(Object o) {
        return this == o;
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

        if (variable == null) {
            throw new NullPointerException();
        }

        if (data == null) {
            throw new NullPointerException();
        }

        if (data.length < size) {
            throw new IllegalStateException();
        }
    }
}


