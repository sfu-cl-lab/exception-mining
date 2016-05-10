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

package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.session.Session;
import edu.cmu.tetradapp.model.SessionWrapper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the basic functionality of the SessionEditorWorkbench.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-06 15:57:49 -0500 (Fri, 06 Jan
 *          2006) $
 */
public final class TestSessionEditorWorkbench extends TestCase {

    /**
     * The session being tested.
     */
    private SessionEditorWorkbench workbench;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestSessionEditorWorkbench(String name) {
        super(name);
    }

    @Override
	public final void setUp() {

        Session session = new Session("Test");
        SessionWrapper sessionWrapper = new SessionWrapper(session);

        this.workbench = new SessionEditorWorkbench(sessionWrapper);
    }

    public final void testAddNodes() {
        this.workbench.setNextButtonType("Graph");

        //        Node tetradNode = this.workbench.getNewModelNode();

        //        assertEquals("Graph1", tetradNode.getName());

        //AbstractGraphNode graphNode = this.workbench.getNewDisplayNode(tetradNode);
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestSessionEditorWorkbench.class);
    }
}


