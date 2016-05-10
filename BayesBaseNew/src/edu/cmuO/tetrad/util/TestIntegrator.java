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

public class TestIntegrator extends TestCase {
    private Function function;

    public TestIntegrator(String name) {
        super(name);
    }

    @Override
	public void setUp() {
        function = new Function() {
            @Override
			public double valueAt(double x) {
                return x;
            }

            @Override
			public String toString() {
                return "y=x.";
            }
        };
    }

    public void testPdfIntegration() {
        assertEquals("Integrator not integrate properly under the function: " +
                function, 0.5, Integrator.getArea(function, 0.0, 1.0, 10000),
                0.000000001);
    }

    public static Test suite() {
        return new TestSuite(TestIntegrator.class);
    }
}


