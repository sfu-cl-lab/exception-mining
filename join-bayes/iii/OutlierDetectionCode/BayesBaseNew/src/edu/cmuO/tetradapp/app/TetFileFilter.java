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

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Filters out all but .tet file when loading and saving.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-06 15:57:49 -0500 (Fri, 06 Jan
 *          2006) $
 */
final class TetFileFilter extends FileFilter {

    /**
     * Accepts a file if its name ends with ".tet".
     */
    @Override
	public boolean accept(File file) {
        return file.isDirectory() || file.getName().endsWith(".tet");
    }

    /**
     * Returns the description of this file filter that will be displayed in a
     * JFileChooser.
     */
    @Override
	public String getDescription() {
        return "Tetrad serialized session workbench (.tet)";
    }
}


