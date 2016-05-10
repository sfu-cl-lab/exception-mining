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

import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.bayes.DirichletEstimator;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a DirichletEstimator.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public class DirichletEstimatorWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    public DirichletBayesIm dirichletBayesIm;

    //============================CONSTRUCTORS============================//

    public DirichletEstimatorWrapper(DataWrapper dataWrapper,
            DirichletBayesImWrapper dirichletPriorWrapper) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (dirichletPriorWrapper == null) {
            throw new NullPointerException();
        }

     
       
        RectangularDataSet dataSet =
                (RectangularDataSet) dataWrapper.getSelectedDataModel();
        DirichletBayesIm dirichletBayesIm =
                dirichletPriorWrapper.getDirichletBayesIm();

        try {
            this.dirichletBayesIm =
                    DirichletEstimator.estimate(dirichletBayesIm, dataSet);
        }
         catch (IllegalArgumentException e) {
             throw new RuntimeException(
                     "Please fully specify the Dirichlet prior first.");
         }
         log(dirichletBayesIm); 
        
    }

    public DirichletEstimatorWrapper(DataWrapper dataWrapper,
            BayesPmWrapper bayesPmWrapper, DirichletEstimatorParams params) {
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
        DirichletBayesIm dirichletBayesIm =
                DirichletBayesIm.symmetricDirichletIm(
                        bayesPmWrapper.getBayesPm(),
                        params.getSymmetricAlpha());

        try {
            this.dirichletBayesIm =
                    DirichletEstimator.estimate(dirichletBayesIm, dataSet);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Please fully specify the Dirichlet prior first.");
        }
        log(dirichletBayesIm);                
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DirichletEstimatorWrapper serializableInstance() {
        return new DirichletEstimatorWrapper(DataWrapper.serializableInstance(),
                DirichletBayesImWrapper.serializableInstance());
    }

    //===============================PUBLIC METHODS=======================//

    public DirichletBayesIm getEstimatedBayesIm() {
        return this.dirichletBayesIm;
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
        LogUtils.getInstance().finer("Estimated Bayes IM:");

     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (dirichletBayesIm == null) {
            throw new NullPointerException();
        }
    }

    @Override
	public Graph getGraph() {
        return dirichletBayesIm.getBayesPm().getDag();
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    private void log(DirichletBayesIm im){
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());
        if(config != null){
            TetradLogger.getInstance().setTetradLoggerConfig(config);
            TetradLogger.getInstance().info("Estimated Dirichlet Bayes IM:");
            TetradLogger.getInstance().log("im", "" + im);
            TetradLogger.getInstance().reset();
        }
    }


}



