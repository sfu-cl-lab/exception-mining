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
 * Represents a real-valued variable. The values are doubles, and the default
 * missing value marker for is Double.NaN.
 *
 * @author Willie Wheeler 07/99
 * @author Joseph Ramsey modifications 12/00
 * @version $Revision: 5683 $ $Date: 2006-01-09 11:04:56 -0500 (Mon, 09 Jan
 *          2006) $
 */
public final class ContinuousVariable extends AbstractVariable
        implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * This is the value which represents missing data in data columns for
     * this variable.
     */
    private static final double MISSING_VALUE = Double.NaN;

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

    //============================CONSTRUCTORS=========================//

    /**
     * Constructs a new continuous variable with the given name.
     *
     * @param name the name of the variable.
     */
    public ContinuousVariable(String name) {
        super(name);
    }

    /**
     * Copy constructor.
     */
    public ContinuousVariable(ContinuousVariable variable) {
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
    public static ContinuousVariable serializableInstance() {
        return new ContinuousVariable("X");
    }

    //==============================PUBLIC METHODS======================//

    /**
     * Checks the value to make sure it's a legitimate value for this
     * column.
     *
     * @param value the value to check.
     * @return true iff the value is legitimate.
     */
    @Override
	public boolean checkValue(final Object value) {
        if (value instanceof Double) {
            return true;
        }
        else if (value instanceof String) {
            try {
                Double.parseDouble((String) value);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the double value that represents missing data for this
     * variable, wrapped in a Double.  The default is Double.NaN.
     *
     * @return the missing value marker, wrapped as a Double.
     */
    @Override
	public Object getMissingValueMarker() {
        return MISSING_VALUE;
    }

    /**
     * Returns the double value that represents missing data for this
     * variable. The default is Double.NaN.
     *
     * @return the missing value marker.
     */
    public static double getDoubleMissingValue() {
        return MISSING_VALUE;
    }

    /**
     * Determines whether the argument is equal to the missing value marker.
     *
     * @param value the Object to test--should be a wrapped version of the
     *              missing value marker.
     * @return true iff it really is a wrapped version of the missing value
     *         marker.
     */
    public static boolean isDoubleMissingValue(double value) {
        return Double.isNaN(value);
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
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            return Double.isNaN(doubleValue);
        }

        return false;
    }

    @Override
	public int hashCode() {
        int hashCode = 63;
        hashCode = 17 * hashCode + getName().hashCode();
        hashCode = 17 * hashCode + getNodeType().hashCode();
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

        if (!(o instanceof ContinuousVariable)) {
            return false;
        }

        ContinuousVariable variable = (ContinuousVariable) o;

        if (!(getNodeType() == variable.getNodeType())) {
            return false;
        }
        
        if (getName().hashCode() != variable.getName().hashCode()) {
            return false;
        }

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
        setCenterX(centerX);
        setCenterY(centerY);
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
