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

package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.util.TetradSerializable;

import java.util.List;

/**
 * Stores basic independence test parameters.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-06 16:31:25 -0500 (Fri, 06 Jan
 *          2006) $
 */
public interface MimIndTestParams extends TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the alpha level of the test, in [0, 1].
     */
    double getAlpha();

    /**
     * Sets the alpha level of the test, in [0, 1].
     */
    void setAlpha(double alpha);

    /**
     * Returns the most recently set list of variable names (Strings).
     */
    List getVarNames();

    /**
     * Sets the list of variable names (Strings).
     */
    void setVarNames(List<String> varNames);
}


