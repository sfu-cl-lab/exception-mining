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

package edu.cmu.tetrad.regression;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Nov 28, 2008 Time: 11:52:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegressionUtils {
    public static DataSet residuals1(DataSet dataSet, Dag dag) {

        Regression regression = new RegressionDataset(dataSet);

        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(dataSet.getNumRows(),
                dataSet.getNumColumns());

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node target = dataSet.getVariable(i);
            Node _target = dag.getNode(target.getName());

            if (!dag.containsNode(_target)) {
                continue;
            }

            List<Node> _regressors = dag.getParents(_target);
            List<Node> regressors = new LinkedList<Node>();

            for (Node node : _regressors) {
                regressors.add(dataSet.getVariable(node.getName()));
            }

            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        return ColtDataSet.makeContinuousData(dataSet.getVariables(), residuals);
    }

    public static DataSet residuals(DataSet dataSet, Graph graph) {
        Regression regression = new RegressionDataset(dataSet);
        DoubleMatrix2D residuals = new DenseDoubleMatrix2D(dataSet.getNumRows(), dataSet.getNumColumns());

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node target = dataSet.getVariable(i);
            Node _target = graph.getNode(target.getName());

            if (_target == null) {
                throw new IllegalArgumentException("Data variable not in graph: " + target);
            }

            Set<Node> _regressors = new HashSet<Node>(graph.getParents(_target));
            boolean changed = true;

            while (changed) {
                changed = false;

                for (Node node : new ArrayList<Node>(_regressors)) {
                    if (_regressors.contains(node)) {
                        boolean inData = dataSet.getVariable(node.getName()) != null;
                        if (!inData) {
                            _regressors.remove(node);
                            _regressors.addAll(graph.getParents(node));
                            changed = true;
                        }
                    }
                }
            }

            System.out.println("For " + target + " regressors are " + _regressors);

            List<Node> regressors = new LinkedList<Node>();

            for (Node node : _regressors) {
                regressors.add(dataSet.getVariable(node.getName()));
            }

            RegressionResult result = regression.regress(target, regressors);
            DoubleMatrix1D residualsColumn = result.getResiduals();
            residuals.viewColumn(i).assign(residualsColumn);
        }

        return ColtDataSet.makeContinuousData(dataSet.getVariables(), residuals);
    }
}

