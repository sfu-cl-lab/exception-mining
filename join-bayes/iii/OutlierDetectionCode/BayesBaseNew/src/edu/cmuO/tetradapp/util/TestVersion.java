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

package edu.cmu.tetradapp.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the Version class, to make sure it can load versions from string
 * representations and generate string representations correctly.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class TestVersion extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestVersion(final String name) {
        super(name);
    }

    public void testRoundtrip() {
        Version version = new Version("4.3.1-5");
        String versionString = version.toString();
        Version version2 = new Version(versionString);
        assertTrue(version.equals(version2));
    }

    public void testNextVersion() {
        Version version = new Version("4.3.1-5");

        Version version2 = version.nextMajorVersion();
        assertEquals(version2, new Version("5.0.0-0"));

        Version version3 = version.nextMinorVersion();
        assertEquals(version3, new Version("4.4.0-0"));

        Version version4 = version.nextMinorSubversion();
        assertEquals(version4, new Version("4.3.2-0"));

        Version version5 = version.nextIncrementalRelease();
        assertEquals(version5, new Version("4.3.1-6"));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestVersion.class);
    }
}


