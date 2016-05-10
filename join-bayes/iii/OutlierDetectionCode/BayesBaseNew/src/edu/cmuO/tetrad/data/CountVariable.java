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

import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.util.TetradSerializable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Represents a variable that ranges over counts--that is, positive integers
 * that represent numbers of things. This variable should be used when integer
 * values representing numbers of things need to be recorded, and a continuous
 * variable will not suffice due to problems of inaccuracy. To do statistical
 * analyses using data for this variable, they will need to be converted to
 * discrete or continuous data first.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5308 $ $Date: 2006-01-09 11:04:56 -0500 (Mon, 09 Jan
 *          2006) $
 */
public final class CountVariable extends AbstractVariable
        implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * This is the value which represents missing data in data columns for
     * this variable.
     */
    private final static int missingValue = -99;

    /**
     * The node type.
     *
     * @serial
     */
    private NodeType nodeType = NodeType.MEASURED;

    /**
     * The x coordinate of the center of the node.
     *
     * @serial
     */
    private int centerX = -1;

    /**
     * The y coordinate of the center of the node.
     *
     * @serial
     */
    private int centerY = -1;

    /**
     * Fires property change events.
     */
    private transient PropertyChangeSupport pcs;

    //==============================CONSTRUCTORS=========================//

    /**
     * Constructs a new continuous variable with the given name.
     *
     * @param name the name of the variable.
     */
    private CountVariable(String name) {
        super(name);
    }

    /**
     * Copy constructor.
     */
    public CountVariable(CountVariable variable) {
        super(variable.getName());
        this.nodeType = variable.nodeType;
        this.centerX = variable.centerX;
        this.centerY = variable.centerY;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static CountVariable serializableInstance() {
        return new CountVariable("X");
    }

    //==============================PUBLIC METHODS=======================//

    /**
     * Checks the value to make sure it's a legitimate value for this
     * column.
     *
     * @param value the value to check.
     * @return true iff the value is legitimate.
     */
    @Override
	public boolean checkValue(Object value) {
        return ((Integer) value) > 0;
    }

    /**
     * Returns the integer value that represents missing data for this
     * variable, wrapped in a Integer.  The default is -1.
     *
     * @return the missing value marker, wrapped as a Integer.
     */
    @Override
	public Object getMissingValueMarker() {
        return missingValue;
    }

    /**
     * Returns the int value that represents missing data for this variable.
     * The default is -1.
     *
     * @return the missing value marker.
     */
    public static int getIntMissingValue() {
        return missingValue;
    }

    /**
     * Determines whether the argument is equal to the missing value marker.
     *
     * @param value the Object to test--should be a wrapped version of the
     *              missing value marker.
     * @return true iff it really is a wrapped version of the missing value
     *         marker.
     */
    @Override
	public boolean isMissingValue(Object value) {
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            return intValue == missingValue;
        }

        return false;
    }

    @Override
	public int hashCode() {
        int hashCode = 63;
        hashCode = 17 * hashCode + getName().hashCode();
        return hashCode;
    }

    /**
     * Two continuous variables are equal if they have the same name and the
     * same missing value marker.
     */
    @Override
	public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof CountVariable)) {
            return false;
        }

        CountVariable variable = (CountVariable) o;

        return getName().equals(variable.getName());
    }

    @Override
	public NodeType getNodeType() {
        return nodeType;
    }

    @Override
	public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Returns the x coordinate of the center of the node.
     */
    @Override
	public int getCenterX() {
        return this.centerX;
    }

    /**
     * Sets the x coordinate of the center of this node.
     */
    @Override
	public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    /**
     * Returns the y coordinate of the center of the node.
     */
    @Override
	public int getCenterY() {
        return this.centerY;
    }

    /**
     * Sets the y coordinate of the center of this node.
     */
    @Override
	public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    /**
     * Sets the (x, y) coordinates of the center of this node.
     */
    @Override
	public void setCenter(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * Adds a property change listener.
     */
    @Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
        getPcs().addPropertyChangeListener(l);
    }

    private PropertyChangeSupport getPcs() {
        if (this.pcs == null) {
            this.pcs = new PropertyChangeSupport(this);
        }

        return this.pcs;
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

        if (nodeType == null) {
            throw new NullPointerException();
        }
    }
}


