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

package edu.cmu.tetrad.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests basic functionality o the NodeArrangement class.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public class TestNodeArrangement extends TestCase {
    public TestNodeArrangement(String name) {
        super(name);
    }

    public void testArrange() {
        NodeArrangement nodeArrangement = new NodeArrangement();

        nodeArrangement.put("X1", new Point(25, 29));
        nodeArrangement.put("X2", new Point(35, 233));
        nodeArrangement.put("X3", new Point(64, 95));
        nodeArrangement.put("X1", new Point(88, 65));

        System.out.println(nodeArrangement);

        Point px1 = nodeArrangement.get("X1");
        assertEquals(px1, new Point(88, 65));

        NodeArrangement nodeArrangement2 = new NodeArrangement(nodeArrangement);

        System.out.println(nodeArrangement2);
        assertEquals(nodeArrangement, nodeArrangement2);
    }

    public static Test suite() {
        return new TestSuite(TestNodeArrangement.class);
    }
}


