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

package edu.cmu;

import edu.cmu.tetradapp.util.WatchedProcess;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Tests to make sure the field <code>showDialog</code> in WatchProcess is set
 * to <code>true</code>. This must be the case in order for the little progress
 * dialogs to be displayed while algorithms are running, etc. It is convenient
 * to set this to false while debugging, but Tetrad must not be posted with this
 * set to false.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-06 22:37:50 -0500 (Fri, 06 Jan
 *          2006) $
 * @see edu.cmu.tetradapp.util.TetradSerializableUtils
 */
public class TestWatchedProcessDialogs extends TestCase {
    public TestWatchedProcessDialogs(String name) {
        super(name);
    }

    public void testWatchedProcessDialogs() {
        try {
            Field field = WatchedProcess.class.getDeclaredField("SHOW_DIALOG");

            int modifiers = field.getModifiers();
            boolean _static = Modifier.isStatic(modifiers);
            field.setAccessible(true);

            if (!_static || !(field.getBoolean(null))) {
                throw new RuntimeException("Class does not define static " +
                        "boolean SHOW_DIALOG = true. Please revise before " +
                        "posting next time.");
            }
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    "No field showDialog in WatchedProcess!");
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}


