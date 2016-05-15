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

package edu.cmu.tetrad.gene.history;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * A distribution from which noise values are drawn.  Each call to nextRandom()
 * draws a new noise value from the distribution.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5308 $ $Date: 2005-07-06 13:39:56 -0400 (Wed, 06 Jul
 *          2005) $
 */
public interface RandomDistribution extends TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Draws a new noise value from the underlying distribution.
     *
     * @return this value.
     */
    double nextRandom();
}


