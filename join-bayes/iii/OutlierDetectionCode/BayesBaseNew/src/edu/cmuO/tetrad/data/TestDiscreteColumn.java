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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;

/**
 * Tests the basic functionality of the DiscreteColumn.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-05 17:33:58 -0500 (Thu, 05 Jan
 *          2006) $
 * @deprecated
 */
@Deprecated
public final class TestDiscreteColumn extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestDiscreteColumn(final String name) {
        super(name);
    }

    /**
     * Tests construction.
     */
    public static void testChangeVariable() {
        DiscreteVariable v1 = new DiscreteVariable("X",
                Arrays.asList(new String[]{"high", "med", "low"}));
        DiscreteColumn column = new DiscreteColumn(v1);

        column.add("high");
        column.add("med");
        column.add("low");
        column.add("med");
        column.add("high");

        assertEquals("high", column.get(0));
        assertEquals("med", column.get(1));
        assertEquals("low", column.get(2));
        assertEquals("med", column.get(3));
        assertEquals("high", column.get(4));

        System.out.println(column);

        DiscreteVariable v2 = new DiscreteVariable("X",
                Arrays.asList(new String[]{"low", "med", "high"}));
        column.changeVariable(v2);

        assertEquals("high", column.get(0));
        assertEquals("med", column.get(1));
        assertEquals("low", column.get(2));
        assertEquals("med", column.get(3));
        assertEquals("high", column.get(4));

        System.out.println(column);

        DiscreteVariable v3 = new DiscreteVariable("X",
                Arrays.asList(new String[]{"low", "high"}));
        column.changeVariable(v3);

        System.out.println(column);

        assertEquals("high", column.get(0));
        assertEquals("*", column.get(1));
        assertEquals("low", column.get(2));
        assertEquals("*", column.get(3));
        assertEquals("high", column.get(4));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestDiscreteColumn.class);
    }
}


