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

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.MarshalledObject;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6163 $ $Date: 2006-01-06 23:02:37 -0500 (Fri, 06 Jan
 *          2006) $
 */
public class SemImWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private final SemIm semIm;

    //============================CONSTRUCTORS==========================//

    public SemImWrapper(SemEstimatorWrapper semEstWrapper) {
        if (semEstWrapper == null) {
            throw new NullPointerException();
        }

        SemIm oldSemIm = semEstWrapper.getSemEstimator().getEstimatedSem();

        try {
            this.semIm = (SemIm) new MarshalledObject(oldSemIm).get();
        }
        catch (Exception e) {
            throw new RuntimeException("SemIm could not be deep cloned.", e);
        }
        log(semIm);
    }

    public SemImWrapper(SemPmWrapper semPmWrapper) {
        if (semPmWrapper == null) {
            throw new NullPointerException("SemPmWrapper must not be null.");
        }

        this.semIm = new SemIm(semPmWrapper.getSemPm());
        log(semIm);
    }

    public SemImWrapper(SemUpdaterWrapper semUpdaterWrapper) {
        if (semUpdaterWrapper == null) {
            throw new NullPointerException("SemPmWrapper must not be null.");
        }

        this.semIm =
                new SemIm(semUpdaterWrapper.getSemUpdater().getUpdatedSemIm());
        log(semIm);
    }

    public SemImWrapper(SemImWrapper semPmWrapper) {
        if (semPmWrapper == null) {
            throw new NullPointerException("SemPmWrapper must not be null.");
        }

        this.semIm = new SemIm(semPmWrapper.getSemIm());
        log(semIm);
    }

    public SemImWrapper(PValueImproverWrapper wrapper) {
        if (wrapper == null) {
            throw new NullPointerException("PValue Improver must not be null.");
        }

        this.semIm = wrapper.getEstSem();
        log(semIm);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemImWrapper serializableInstance() {
        return new SemImWrapper(SemPmWrapper.serializableInstance());
    }

    //===========================PUBLIC METHODS=========================//

    public SemIm getSemIm() {
        return this.semIm;
    }


    @Override
	public Graph getGraph() {
        return semIm.getSemPm().getGraph();
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    //======================== Private methods =======================//

    private void log(SemIm im) {
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());
        if (config != null) {
            TetradLogger.getInstance().setTetradLoggerConfig(config);
            TetradLogger.getInstance().info("IM type = SEM");
            TetradLogger.getInstance().log("im", im.toString());
            TetradLogger.getInstance().reset();
        }
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

        if (semIm == null) {
            throw new NullPointerException();
        }
    }

}


