///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

package edu.cmu.tetrad.sem;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jdramsey
 * Date: Mar 20, 2009
 * Time: 2:58:09 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISemIm extends TetradSerializable {
    static final long serialVersionUID = 23L;

    SemPm getSemPm();

    double[] getFreeParamValues();

    void setFreeParamValues(double[] params);

    double getParamValue(Parameter parameter);

    void setParamValue(Parameter parameter, double value);

    void setFixedParamValue(Parameter parameter, double value);

    double getParamValue(Node nodeA, Node nodeB);

    void setParamValue(Node nodeA, Node nodeB, double value);

    List<Parameter> getFreeParameters();

    int getNumFreeParams();

    List<Parameter> getFixedParameters();

    int getSampleSize();

    void setParameterBoundsEnforced(boolean b);

    double getFml();

    boolean isParameterBoundsEnforced();

    List<Node> listUnmeasuredLatents();

    boolean isCyclic();

    boolean isEstimated();

    List<Node> getVariableNodes();

    double getMean(Node node);

    double getMeanStdDev(Node node);

    double getIntercept(Node node);

    void setErrVar(Node nodeA, double value);

    void setEdgeCoef(Node x, Node y, double value);

    void setIntercept(Node y, double intercept);

    void setMean(Node node, double value);

    double getStandardError(Parameter parameter, int maxFreeParamsForStatistics);

    double getTValue(Parameter parameter, int maxFreeParamsForStatistics);

    double getPValue(Parameter parameter, int maxFreeParamsForStatistics);

    double getPValue();

    double getVariance(Node nodeA);

    double getStdDev(Node node);

    List getMeasuredNodes();

    DoubleMatrix2D getImplCovarMeas();

    DoubleMatrix2D getImplCovar();

    double getBicScore();

    double getChiSquare();

    boolean isSimulatedPositiveDataOnly();

    void setSimulatedPositiveDataOnly(boolean simulatedPositiveDataOnly);
}

