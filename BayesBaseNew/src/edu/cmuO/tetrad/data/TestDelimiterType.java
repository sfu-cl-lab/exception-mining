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

//import edu.cmu.tetrad.bayes.BayesPm;
//import edu.cmu.tetrad.bayes.MlBayesIm;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests to make sure the DelimiterType enumeration hasn't been tampered with.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-09 11:04:56 -0500 (Mon, 09 Jan
 *          2006) $
 */
public final class TestDelimiterType extends TestCase {
    public TestDelimiterType(String name) {
        super(name);
    }

    public final void testTypes() {
        assertTrue("Tab".equals(DelimiterType.TAB.toString()));
        assertTrue("Whitespace".equals(DelimiterType.WHITESPACE.toString()));
        assertTrue("Comma".equals(DelimiterType.COMMA.toString()));
        assertTrue(!(DelimiterType.TAB.equals(DelimiterType.WHITESPACE)));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestDelimiterType.class);
    }
}


