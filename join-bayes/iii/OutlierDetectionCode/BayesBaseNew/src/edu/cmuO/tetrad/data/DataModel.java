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

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * <p>Interface implemented by classes, instantiations of which can serve as
 * data models in Tetrad. Data models may be named if desired; if provided,
 * these names will be used for display purposes.</p> <p>This interface is
 * relatively free of methods, mainly because classes that can serve as data
 * models in Tetrad are diverse, including continuous and discrete data sets,
 * covariance and correlation matrices, graphs, and lists of other data models.
 * So this is primarily a taqging interface.</p>
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public interface DataModel
        extends KnowledgeTransferable, VariableSource, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the name of the data model (may be null).
     */
    String getName();

    /**
     * Sets the name of the data model (may be null).
     */
    void setName(String name);
}

