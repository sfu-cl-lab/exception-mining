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

import edu.cmu.tetrad.bayes.MeanInterpolator;
import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.DataSet;

/**
 * Returns a data set in which missing values in each column are filled using
 * the mean of that column.
 *
 * @author Joseph Ramsey
 */
public final class RegressionInterpolator implements DataFilter {
    public DataSet filter(DataSet dataSet) {
        DataSet d1 = new ColtDataSet((ColtDataSet) dataSet);
        DataSet d2 = new ColtDataSet((ColtDataSet) dataSet);
        d2 = new MeanInterpolator().filter(d2);

        // Copy out columns and names from mean-interpolated d2 to feed to
        // the regression class.
        int numVars = d2.getNumColumns();
        int numCases = d2.getNumRows();
        double[][] columns = new double[numVars][numCases];
        String[] names = new String[numVars];

        for (int i = 0; i < numCases; i++) {
            for (int j = 0; j < numVars; j++) {
                columns[j][i] = d2.getDouble(i, j);
            }
        }

        for (int j = 0; j < numVars; j++) {
            names[j] = d2.getVariable(j).getName();
        }

        // An array to keep track of visited variables. visited[i] will be
        // set to true when variable i is visited.
        int j;

        while ((j = columnWithMaxMissing(d1)) != -1) {
            System.out.println(j);

            // Impute missing values in d1 using regression models from d2.
            double[] target = columns[j];
            String targetName = names[j];
            double[][] regressors = new double[numVars - 1][numCases];
            String[] regressorNames = new String[numVars - 1];

            int k = -1;

            for (int m = 0; m < numVars; m++) {
                if (m == j) continue;

                ++k;
                regressors[k] = columns[m];
                regressorNames[k] = names[m];
            }

            RegressionOld regression = new RegressionOld();
            regression.setRegressors(regressors);
            regression.setRegressorNames(regressorNames);
            RegressionResult result = regression.regress(target, targetName);


//            System.out.println(result);

            for (int i = 0; i < numCases; i++) {
                if (!Double.isNaN(d1.getDouble(i, j))) {
                    continue;
                }

                // Build array of case values.
                k = -1;
                double[] values = new double[numVars - 1];

                for (int m = 0; m < numVars; m++) {
                    if (m == j) continue;

                    ++k;
                    values[k] = d2.getDouble(i, m);
                }

                double predicted = result.getPredictedValue(values);
                d1.setDouble(i, j, predicted);
            }

            // Copy column into d2.
            for (int i = 0; i < numCases; i++) {
                d2.setDouble(i, j, d1.getDouble(i, j));
            }
        }

//        System.out.println(dataSet1);

        return d1;

    }

    private int columnWithMaxMissing(DataSet d1) {
        int max = -1;
        int maxCol = -1;

        for (int j = 0; j < d1.getNumColumns(); j++) {
            int n = 0;

            for (int i = 0; i < d1.getNumRows(); i++) {
                if (Double.isNaN(d1.getDouble(i, j))) {
                    n++;
                }
            }

            if (n > 0 && n > max) {
                max = n;
                maxCol = j;
            }
        }

        return maxCol;
    }
}

