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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Extends AbstractAlgorithmRunner to produce a wrapper for the Regression
 * algorithm.
 *
 * @author Frank Wimberly after Joe Ramsey's PcRunner
 * @version $Revision: 6086 $
 */
public class RegressionRunner implements AlgorithmRunner {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private RegressionParams params;

    /**
     * @serial Cannot be null.
     */
    private String targetName;

    /**
     * @serial Cannot be null. Note that the name of this field can't be
     * changed because of serialization. Ugh.
     */
    private DataModel dataSet;

    /**
     * @serial Can be null.
     */
    private Graph outGraph;

    /**
     * The result of the regression--that is, coefficients, p-values, etc.
     */
    private RegressionResult result;

    /**
     * @deprecated
     */
    @Deprecated
	private String report;

    //=========================CONSTRUCTORS===============================//

    /**
     * Constructs a wrapper for the given DataWrapper. The DataWrapper must
     * contain a DataSet that is either a DataSet or a DataSet or a DataList
     * containing either a DataSet or a DataSet as its selected model.
     */
    public RegressionRunner(DataWrapper dataWrapper, RegressionParams params) {
        if (dataWrapper == null) {
            throw new NullPointerException();
        }

        if (params == null) {
            throw new NullPointerException();
        }

        DataModel dataModel = dataWrapper.getSelectedDataModel();

        if (dataModel instanceof RectangularDataSet) {
            RectangularDataSet _dataSet = (RectangularDataSet) dataModel;
            if (!_dataSet.isContinuous()) {
                throw new IllegalArgumentException("Data set must be continuous.");
            }
        }

        this.params = params;
        this.targetName = params.getTargetName();
        this.dataSet = dataModel;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static RegressionRunner serializableInstance() {
        List<Node> variables = new LinkedList<Node>();
        ContinuousVariable var1 = new ContinuousVariable("X");
        ContinuousVariable var2 = new ContinuousVariable("Y");

        variables.add(var1);
        variables.add(var2);
        RectangularDataSet _dataSet = new ColtDataSet(3, variables);
        double[] col1data = new double[]{0.0, 1.0, 2.0};
        double[] col2data = new double[]{2.3, 4.3, 2.5};

        for (int i = 0; i < 3; i++) {
            _dataSet.setDouble(i, 0, col1data[i]);
            _dataSet.setDouble(i, 1, col2data[i]);
        }

        DataWrapper dataWrapper = new DataWrapper(_dataSet);
        return new RegressionRunner(dataWrapper,
                RegressionParams.serializableInstance());
    }

    //===========================PUBLIC METHODS============================//

    @Override
	public DataModel getDataSet() {
        //return (DataModel) this.dataWrapper.getDataModelList().get(0);
        return this.dataSet;
    }

    public void resetParams(Object params) {
        //ignore
        //this.params = (RegressionParams) params;
    }

    public void setParams(RegressionParams params) {
        this.params = params;
    }

    public boolean isSearchingOverSubset() {
        return false;
    }

    @Override
	public SearchParams getParams() {
        return params;
    }

    @Override
	public Graph getResultGraph() {
        return outGraph;
    }

    public void setResultGraph(Graph graph) {
        this.outGraph = graph;
    }

    @Override
	public Graph getSourceGraph() {
        return null;
    }
    //=================PUBLIC METHODS OVERRIDING ABSTRACT=================//

    /**
     * Executes the algorithm, producing (at least) a result workbench. Must be
     * implemented in the extending class.
     */
    @Override
	public void execute() {

        if (params.getRegressorNames().length == 0 ||
                params.getTargetName() == null) {
            outGraph = new EdgeListGraph();
            return;
        }

        if (Arrays.asList(params.getRegressorNames()).contains(
                params.getTargetName())) {
            outGraph = new EdgeListGraph();
            return;
        }

        Regression regression;
        Node target;
        List<Node> regressors;

        if (dataSet instanceof RectangularDataSet) {
            RectangularDataSet _dataSet = (RectangularDataSet) dataSet;
            regression = new RegressionDataset(_dataSet);
            target = _dataSet.getVariable(params.getTargetName());
            String[] regressorNames = params.getRegressorNames();
            regressors = new LinkedList<Node>();

            for (String regressorName : regressorNames) {
                regressors.add(_dataSet.getVariable(regressorName));
            }

            double alpha = params.getAlpha();
            regression.setAlpha(alpha);

            result = regression.regress(target, regressors);
            outGraph = regression.getGraph();
        }
        else if (dataSet instanceof CovarianceMatrix) {
            CovarianceMatrix covariances = (CovarianceMatrix) dataSet;
            regression = new RegressionCovariance(covariances);
            target = covariances.getVariable(params.getTargetName());
            String[] regressorNames = params.getRegressorNames();
            regressors = new LinkedList<Node>();

            for (String regressorName : regressorNames) {
                regressors.add(covariances.getVariable(regressorName));
            }

            double alpha = params.getAlpha();
            regression.setAlpha(alpha);

            result = regression.regress(target, regressors);
            outGraph = regression.getGraph();
        }

        setResultGraph(outGraph);
    }

    @Override
	public boolean supportsKnowledge() {
        return false;
    }

    @Override
	public ImpliedOrientation getMeekRules() {
        throw new UnsupportedOperationException();
    }

    public RegressionResult getResult() {
        return result;
    }

    public Graph getOutGraph() {
        return outGraph;
    }

    public String getTargetName() {
        return targetName;
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

        if (params == null) {
            throw new NullPointerException();
        }

        /*
        if (targetName == null) {
            throw new NullPointerException();
        }
        */

        if (dataSet == null) {
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


