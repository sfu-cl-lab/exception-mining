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

package edu.cmu.tetrad.session;


/**
 * Tags models whose parameters can be reset.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-08 12:28:12 -0500 (Tue, 08 Mar
 *          2005) $
 */
public interface ParamsResettable {
    static final long serialVersionUID = 23L;

    /**
     * In some cases (for instance, algorithm runners), cloned session models
     * need to have the object-identically same parameter objects as before
     * cloning. This method lets Tetrad set that automatically.
     */
    void resetParams(Object params);

    /**
     * Returns the parameter object of a non-cloned model so that it can be set
     * on the cloned model.
     */
    Object getResettableParams();
}


