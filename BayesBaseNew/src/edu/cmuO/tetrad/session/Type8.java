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

import edu.cmu.tetrad.util.TetradSerializableExcluded;


/**
 * A sample class to be wrapped in a SessionNode as a model.
 */
public class Type8 implements SessionModel, TetradSerializableExcluded {
    static final long serialVersionUID = 23L;

    public Type8(Type7 model1) {
    }

    public Type8(Type7 model1, Type9 model2) {
    }

    public static Type8 serializableInstance() {
        return new Type8(Type7.serializableInstance());
    }

    /**
     * Sets the name of the session model.
     */
    @Override
	public void setName(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the name of the session model.
     */
    @Override
	public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}



