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

package edu.cmu.tetradapp.workbench;

import edu.cmu.tetrad.graph.EdgeListGraph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the GraphWorkbench class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public class TestGraphWorkbench extends TestCase {

    private GraphWorkbench graphWorkbench;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestGraphWorkbench(String name) {
        super(name);
    }

    @Override
	public void setUp() {
        this.graphWorkbench = new GraphWorkbench(new EdgeListGraph());
    }

    public void testNextVariableName() {
        // TODO: Not an independent test yet--some other test may
        // change the workbench.
        assertTrue("X1".equals(this.graphWorkbench.nextVariableName("X")));
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parents to match the name
        // of this class.
        return new TestSuite(TestGraphWorkbench.class);
    }
}


