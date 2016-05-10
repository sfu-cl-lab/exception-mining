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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated
 */
@Deprecated
public final class DiscreteColumn extends AbstractList implements Column {
    static final long serialVersionUID = 23L;

    /**
     * The variable for this column.
     */
    private DiscreteVariable variable;

    /**
     * The data for this column.
     */
    private int[] data;

    /**
     * The number of data points stored in this column.  Data points range from
     * index 0 to index (size() - 1); The rest of the elements in the data array
     * are undefined.
     */
    private int size = 0;

    /**
     * True iff the column should adjust the discrete variable to accomodate new
     * categories added, false if new categories should be rejected.
     */
    private boolean newCategoriesAccomodated = true;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new ContinuousColumn using the given variable.
     *
     * @param variable the variable for the column.
     */
    public DiscreteColumn(DiscreteVariable variable) {
        this(variable, 100);
    }

    /**
     * Constructs a new ContinuousColumn using the given variable.
     *
     * @param variable the variable for the column.
     */
    public DiscreteColumn(DiscreteVariable variable, int initialCapacity) {
        if (variable == null) {
            throw new NullPointerException();
        }

        if (initialCapacity < 1) {
            throw new IllegalArgumentException(
                    "Initial capacity must be > 0: " + initialCapacity);
        }

        this.variable = variable;
        this.data = new int[initialCapacity];
        Arrays.fill(this.data, DiscreteVariable.MISSING_VALUE);
    }

    /**
     * Constructs a new ContinuousColumn using the given variable and
     * initialized using the given array, the first 'size' values of which are
     * meaningful data.
     *
     * @param variable the variable for the column.
     * @param data     the initial data for the column.
     * @param size     the initial size of the column.
     */
    public DiscreteColumn(DiscreteVariable variable, int[] data, int size) {
        for (int aData : data) {
            if (aData >= variable.getNumCategories()) {
                accomodateIndex(aData);
            }
        }

        this.variable = variable;
        this.data = data;
        this.size = size;
    }

    /**
     * Copy constructor.
     */
    public DiscreteColumn(DiscreteColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }

        this.variable = new DiscreteVariable(column.variable);
        this.data = new int[column.data.length];
        System.arraycopy(column.data, 0, this.data, 0, this.data.length);
        this.size = column.size;
        this.newCategoriesAccomodated = column.newCategoriesAccomodated;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DiscreteColumn serializableInstance() {
        return new DiscreteColumn(DiscreteVariable.serializableInstance());
    }

    //=============================PUBLIC METHODS=========================//

    /**
     * Adjusts the category indices in the underlying data to reflect the new
     * category list. If a category from the old variable are not available in
     * the new category, its instances in the column are set to the missing
     * value marker. Finally, sets the variable of the column to the given
     * variable.
     */
    public final void changeVariable(DiscreteVariable variable) {
        if (variable == null) {
            throw new NullPointerException();
        }

        List<String> oldCategories = this.variable.getCategories();
        List<String> newCategories = variable.getCategories();

        int[] indexArray = new int[oldCategories.size()];

        for (int i = 0; i < oldCategories.size(); i++) {
            indexArray[i] = newCategories.indexOf(oldCategories.get(i));
        }

        for (int i = 0; i < size(); i++) {
            if (data[i] == DiscreteVariable.MISSING_VALUE) {
                break;
            }

            int newIndex = indexArray[data[i]];

            if (newIndex == -1) {
                data[i] = DiscreteVariable.MISSING_VALUE;
            }
            else {
                data[i] = newIndex;
            }
        }

        this.variable = variable;
    }

    /**
     * Inserts the given object (Number or String) into the column at position
     * 'index'.  Data is shifted down in the column  to accomodate.
     *
     * @param index   the index at which the new value is inserted.
     * @param element the Object to add.
     * @throws OutOfMemoryError if an expanded array cannot be allocated to
     *                          accomodate the new element.
     */
    @Override
	public final void add(int index, Object element) throws OutOfMemoryError {
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
                    DiscreteVariable.MISSING_VALUE);
            this.data[index] = datum;
            this.size = index + 1;
        }
    }

    //=============================PRIVATE METHODS=======================//

    /**
     * Returns true iff this variable is set to accomodate new categories
     * encountered.
     */
    private boolean isNewCategoriesAccomodated() {
        return this.newCategoriesAccomodated;
    }

    /**
     * Increases the number of categories if necessary to make sure that this
     * variable has the given index.
     */
    private void accomodateIndex(int index) {
        if (!isNewCategoriesAccomodated()) {
            throw new IllegalArgumentException("This variable is not set " +
                    "to accomodate new categories.");
        }

        if (index >= variable.getNumCategories()) {
            adjustCategories(index + 1);
        }
    }

    /**
     * Adjusts the size of the categories list to match the current number of
     * categories. If the list is too short, it is padded with default
     * categories. If it is too long, the extra categories are removed.
     */
    private void adjustCategories(int numCategories) {
        List<String> categories =
                new LinkedList<String>(variable.getCategories());
        List<String> newCategories = new LinkedList<String>(categories);

        if (categories.size() > numCategories) {
            for (int i = variable.getCategories().size() - 1;
                    i >= numCategories; i++) {
                newCategories.remove(i);
            }
        }
        else if (categories.size() < numCategories) {
            for (int i = categories.size(); i < numCategories; i++) {
                String category = DataUtils.defaultCategory(i);

                if (categories.contains(category)) {
                    throw new IllegalArgumentException("Category " + category +
                            " already contained in categories (" + categories +
                            ")");
                }

                newCategories.add(category);
            }
        }

        changeVariable(new DiscreteVariable(variable.getName(), newCategories));
    }

    /**
     * Ensures that the backing array has as length of at least 'minCapacity'.
     *
     * @throws OutOfMemoryError if a larger array cannot be allocated.
     */
    private void ensureCapacity(int minCapacity) throws OutOfMemoryError {
        int oldCapacity = this.data.length;

        if (minCapacity + 1 > oldCapacity) {
            int[] oldData = this.data;
            int newCapacity = 2 * oldCapacity;

            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            data = new int[newCapacity];
            Arrays.fill(this.data, DiscreteVariable.MISSING_VALUE);

            System.arraycopy(oldData, 0, this.data, 0, this.size);
        }
    }

    /**
     * Returns the value at the given index as a Double object.
     *
     * @param index the index of the value sought.
     * @return the value of the array at 'index', or null if the index is out
     *         of range.
     */
    @Override
	public final Object get(int index) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException(
                    "Index not in range (size = " + size() + ": " + index);
        }

        DiscreteVariable variable = (DiscreteVariable) getVariable();

        if (variable.isCategoryNamesDisplayed()) {
            return variable.getCategory(data[index]);
        }
        else {
            return data[index];
        }
    }

    /**
     * Returns the underlying double[] array as an Object.  Must be cast to
     * double[].  Elements in the array beyond the indicated size of the column
     * returned by size() are not meaningful.
     *
     * @return the supporting array for the column.
     */
    @Override
	public final Object getRawData() {
        return data;
    }

    /**
     * If the given category is not already a category for a cagetory, augments
     * the range of category by one and sets the category of the new value to
     * the given category.
     */
    private void accomodateCategory(String category) {
        if (category == null) {
            throw new NullPointerException();
        }

        DiscreteVariable variable = (DiscreteVariable) getVariable();
        List<String> categories = variable.getCategories();

        if (!categories.contains(category)) {
            List<String> newCategories = new LinkedList<String>(categories);
            newCategories.add(category);
            DiscreteVariable newVariable =
                    new DiscreteVariable(variable.getName(), newCategories);
            changeVariable(newVariable);
        }
    }

    /**
     * Attempts to translate the given Object into a double value.  The
     * Object must be either a Number or a String.
     *
     * @param element the Object to translate.
     * @return the translated double value.
     */
    private int getValueFromObject(Object element) {
        if ("*".equals(element) || "".equals(element)) {
            return DiscreteVariable.MISSING_VALUE;
        }

        if (element instanceof Number) {
            int index = ((Number) element).intValue();

            if (!this.variable.checkValue(index)) {
                if (index >= this.variable.getNumCategories() &&
                        isNewCategoriesAccomodated()) {
                    accomodateIndex(index);
                }
                else {
                    throw new IllegalArgumentException();
                }
            }

            return index;
        }
        else if (element instanceof String) {
            String label = (String) element;

            if ("".equals(label)) {
                throw new IllegalArgumentException(
                        "Blank category names not " + "permitted.");
            }

            if (isNewCategoriesAccomodated()) {
                accomodateCategory(label);
            }

            int value = this.variable.getIndex(label);

            if (value == -1) {
                List<String> categories = this.variable.getCategories();
                StringBuffer buf = new StringBuffer();
                buf.append("The categories for this variable are: ");

                for (int i = 0; i < categories.size() - 1; i++) {
                    buf.append(categories.get(i));
                    buf.append(", ");
                }

                buf.append(categories.get(categories.size() - 1));
                buf.append(".");
                throw new IllegalArgumentException(buf.toString());
            }

            return value;
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
     * @see edu.cmu.tetrad.data.Variable
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
     *         Double object.
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

        Object oldDatum = (double) this.data[index];
        int j = this.size - index - 1;

        if (j > 0) {
            System.arraycopy(this.data, index + 1, this.data, index, j);
        }

        Arrays.fill(this.data, index + j, this.data.length,
                DiscreteVariable.MISSING_VALUE);

        size--;

        return oldDatum;
    }

    /**
     * Sets the value at position 'index' in the column to the double
     * value of 'value', which must be either a Number or a String.
     *
     * @param index the index at which the new value is set.
     * @param value the Object to set.
     * @return the old value which was set at position 'index' as a Double
     *         object.
     */
    @Override
	public final Object set(int index, Object value) {
        if (value == null) {
            throw new NullPointerException("Value must not be null.");
        }

        Object oldDatum = null;

        if (index < size()) {
            oldDatum = get(index);
        }

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

    @Override
	public final int hashCode() {
        int hash = 17;
        hash = 37 * hash + variable.hashCode();
        hash = 37 * hash + size;
        hash = 37 * hash + data.hashCode();

        return hash;
    }

    /**
     * Two discrete columns are equal if their variables are equal and their
     * data is equal.
     */
    @Override
	public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof DiscreteColumn)) {
            return false;
        }

        DiscreteColumn column = (DiscreteColumn) o;

        if (!column.variable.equals(this.variable)) {
            return false;
        }

        if (!(column.size == this.size)) {
            return false;
        }

        for (int i = 0; i < this.size; i++) {
            if (!(column.data[i] == this.data[i])) {
                return false;
            }
        }

        return true;
    }

    public final int getInt(int row) {
        return data[row];
    }
}

