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
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.FactoredBayesStructuralEM;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.session.SessionModel;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 * @author Frank Wimberly adapted for EM Bayes estimator and structural EM Bayes
 *         search
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class StructEmBayesSearchRunner implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private BayesPm bayesPm;

    /**
     * @serial Cannot be null.
     */
    private RectangularDataSet dataSet;

    /**
     * @serial Cannot be null.
     */
    private BayesIm estimatedBayesIm;

    //===============================CONSTRUCTORS============================//

    public StructEmBayesSearchRunner(DataWrapper dataWrapper,
            BayesPmWrapper bayesPmWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException(
                    "BayesDataWrapper must not be null.");
        }

        if (bayesPmWrapper == null) {
            throw new NullPointerException("BayesPmWrapper must not be null");
        }

        this.dataSet = (RectangularDataSet) dataWrapper.getSelectedDataModel();
        this.bayesPm = bayesPmWrapper.getBayesPm();

        estimate(this.dataSet, this.bayesPm);
    }

    public StructEmBayesSearchRunner(BayesDataWrapper dataWrapper,
            BayesPmWrapper bayesPmWrapper) {
        this((DataWrapper) dataWrapper, bayesPmWrapper);
    }

    public StructEmBayesSearchRunner(DataWrapper dataWrapper,
            BayesPmWrapper bayesPmWrapper, StructEmBayesSearchParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (bayesPmWrapper == null) {
            throw new NullPointerException();
        }

        if (params == null) {
            throw new NullPointerException();
        }

        RectangularDataSet dataSet =
                (RectangularDataSet) dataWrapper.getSelectedDataModel();

        FactoredBayesStructuralEM estimator = new FactoredBayesStructuralEM(
                dataSet, bayesPmWrapper.getBayesPm());
        this.dataSet = estimator.getDataSet();

        try {
            this.estimatedBayesIm =
                    estimator.maximization(params.getTolerance());

        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public StructEmBayesSearchRunner(DataWrapper dataWrapper,
            BayesImWrapper bayesImWrapper, StructEmBayesSearchParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (bayesImWrapper == null) {
            throw new NullPointerException();
        }

        if (params == null) {
            throw new NullPointerException();
        }

        RectangularDataSet dataSet =
                (RectangularDataSet) dataWrapper.getSelectedDataModel();

        this.bayesPm = bayesImWrapper.getBayesIm().getBayesPm();

        FactoredBayesStructuralEM estimator =
                new FactoredBayesStructuralEM(dataSet, bayesPm);
        this.dataSet = estimator.getDataSet();

        try {
            this.estimatedBayesIm =
                    estimator.maximization(params.getTolerance());
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Please specify the search tolerance first.");
        }
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static StructEmBayesSearchRunner serializableInstance() {
        return new StructEmBayesSearchRunner(DataWrapper.serializableInstance(),
                BayesPmWrapper.serializableInstance());
    }

    //================================PUBLIC METHODS========================//

    public BayesIm getEstimatedBayesIm() {
        return this.estimatedBayesIm;
    }

    private void estimate(RectangularDataSet DataSet, BayesPm bayesPm) {
        double thresh = 0.0001;

        //        for (Iterator i = graph.getNodes().iterator(); i.hasNext();) {
        //            Node node = (Node) i.next();
        //            if (node.getNodeType() == NodeType.LATENT) {
        //                throw new IllegalArgumentException("Estimation of Bayes IM's " +
        //                        "with latents is not supported.");
        //            }
        //        }

        try {
            FactoredBayesStructuralEM estimator =
                    new FactoredBayesStructuralEM(DataSet, bayesPm);
            this.dataSet = estimator.getDataSet();
            this.estimatedBayesIm = estimator.maximization(thresh);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new RuntimeException("Value assignments between Bayes PM " +
                    "and discrete data set do not match.");
        }
    }

    public RectangularDataSet getDataSet() {
        return this.dataSet;
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

//        if (bayesPm == null) {
//            throw new NullPointerException();
//        }

        if (estimatedBayesIm == null) {
            throw new NullPointerException();
        }

        if (dataSet == null) {
            throw new NullPointerException();
        }
    }

    @Override
	public Graph getGraph() {
        return estimatedBayesIm.getBayesPm().getDag();
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


