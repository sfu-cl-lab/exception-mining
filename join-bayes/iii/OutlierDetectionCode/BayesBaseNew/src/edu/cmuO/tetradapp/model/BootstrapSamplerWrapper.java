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

import edu.cmu.tetrad.data.BootstrapSampler;
import edu.cmu.tetrad.data.RectangularDataSet;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a data model so that a random sample will automatically be drawn on
 * construction from a BayesIm.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2005-11-29 16:02:50 -0500 (Tue, 29 Nov
 *          2005) $
 */
public class BootstrapSamplerWrapper extends DataWrapper {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private RectangularDataSet outputDataSet;

    //=============================CONSTRUCTORS===========================//

    public BootstrapSamplerWrapper(DataWrapper wrapper,
            BootstrapSamplerParams params) {
        if (wrapper == null) {
            throw new NullPointerException();
        }

        if (params == null) {
            throw new NullPointerException();
        }

        RectangularDataSet dataSet =
                (RectangularDataSet) wrapper.getSelectedDataModel();

        BootstrapSampler sampler = new BootstrapSampler();
        outputDataSet = sampler.sample(dataSet, params.getSampleSize());
        setDataModel(outputDataSet);
        setSourceGraph(wrapper.getSourceGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new BootstrapSamplerWrapper(DataWrapper.serializableInstance(),
                BootstrapSamplerParams.serializableInstance());
    }

    //=============================PUBLIC METHODS=========================//

    public RectangularDataSet getOutputDataset() {
        return outputDataSet;
    }

    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (outputDataSet == null) {
            throw new NullPointerException();
        }
    }
}


