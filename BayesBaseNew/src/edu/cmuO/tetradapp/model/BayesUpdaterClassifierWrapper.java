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

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.search.BayesUpdaterClassifier;
import edu.cmu.tetrad.session.SessionModel;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a DirichletEstimator.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class BayesUpdaterClassifierWrapper implements SessionModel {
    static final long serialVersionUID = 23L;

    /**
     * @serial Cannot be null.
     */
    private BayesUpdaterClassifier classifier;

    /**
     * @serial Can be null.
     */
    private String name;

    //==============================CONSTRUCTORS===========================//

    public BayesUpdaterClassifierWrapper(BayesImWrapper bayesImWrapper,
            DataWrapper dataWrapper) {
        if (bayesImWrapper == null) {
            throw new NullPointerException();
        }

        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        RectangularDataSet dataSet =
                (RectangularDataSet) dataWrapper.getSelectedDataModel();
        BayesIm bayesIm = bayesImWrapper.getBayesIm();

        this.classifier = new BayesUpdaterClassifier(bayesIm, dataSet);
    }

    public BayesUpdaterClassifierWrapper(DirichletBayesImWrapper bayesImWrapper,
            DataWrapper dataWrapper) {
        if (bayesImWrapper == null) {
            throw new NullPointerException();
        }

        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        RectangularDataSet dataSet =
                (RectangularDataSet) dataWrapper.getSelectedDataModel();
        BayesIm bayesIm = bayesImWrapper.getDirichletBayesIm();

        this.classifier = new BayesUpdaterClassifier(bayesIm, dataSet);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BayesUpdaterClassifierWrapper serializableInstance() {
        return new BayesUpdaterClassifierWrapper(
                BayesImWrapper.serializableInstance(),
                DataWrapper.serializableInstance());
    }

    //==============================PUBLIC METHODS=======================//

    public BayesUpdaterClassifier getClassifier() {
        return classifier;
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (classifier == null) {
            throw new NullPointerException();
        }
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }
}


