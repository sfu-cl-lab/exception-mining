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

package edu.cmu.shuttle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the BayesIm.
 *
 * @author Joseph Ramsey
 * @version $Date: 2006-01-09 00:12:03 -0500 (Mon, 09 Jan 2006) $
 */
public final class TestConvertUtils extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestConvertUtils(String name) {
        super(name);
    }

    public static void testParseYearTime() {
        long milliseconds = ConvertUtils.parseYearTime("98:20:38:43/807");
        assertEquals(8541523807L, milliseconds);
    }

    public static void testParseLaunchTime() {
        long milliseconds = ConvertUtils.parseLaunchTime("-0:0:8:59");
        assertEquals(539000L, milliseconds);
    }

    public static void testParseVarString() {
        int varNumber = ConvertUtils.parseVarNumber("v3294");
        assertEquals(3294, varNumber);
    }

    public static void testIsContinuousDatum() {
        assertTrue(ConvertUtils.isContinuousDatum("0.0"));
        assertTrue(ConvertUtils.isContinuousDatum("0.243"));
        assertTrue(ConvertUtils.isContinuousDatum("2435.0"));
        assertTrue(ConvertUtils.isContinuousDatum("0.235e7"));
        assertTrue(ConvertUtils.isContinuousDatum("2344.2344E-7"));
        assertTrue(ConvertUtils.isContinuousDatum("NaN"));
        assertTrue(!ConvertUtils.isContinuousDatum("ON"));
    }

    public static void testIsDiscreteDatum() {
        assertTrue(ConvertUtils.isDiscreteDatum("ON"));
        assertTrue(ConvertUtils.isDiscreteDatum("OFF"));
        assertTrue(ConvertUtils.isDiscreteDatum("WET"));
        assertTrue(ConvertUtils.isDiscreteDatum("DRY"));
        assertTrue(!ConvertUtils.isDiscreteDatum("NaN"));
        assertTrue(!ConvertUtils.isDiscreteDatum("2344.2344E-7"));
        assertTrue(!ConvertUtils.isDiscreteDatum("Zifflezot"));
    }

    public static void testIsLaunchTimeDatum() {
        assertTrue(ConvertUtils.isLaunchTimeDatum("-0:0:8:59"));
        assertTrue(!ConvertUtils.isLaunchTimeDatum("Zifflezot"));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestConvertUtils.class);
    }
}

