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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Returns a data set in variables for columns with missing values are augmented
 * with an extra category that represents the missing values, with missing
 * values being reported as belong this category.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class ExtraCategoryInterpolator implements DataFilter {
    @Override
	public final RectangularDataSet filter(RectangularDataSet dataSet) {

        // Why does it have to be discrete? Why can't we simply expand
        // whatever discrete columns are there and leave the continuous
        // ones untouched? jdramsey 7/4/2005
        if (!(dataSet.isDiscrete())) {
            throw new IllegalArgumentException("Data set must be discrete.");
        }

        List<Node> variables = new LinkedList<Node>();

        // Add all of the variables to the new data set.
        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            DiscreteVariable variable =
                    (DiscreteVariable) dataSet.getVariable(i);

            String oldName = variable.getName();
            List<String> oldCategories = variable.getCategories();
            List<String> newCategories = new LinkedList<String>(oldCategories);
            newCategories.add("MissingValue");
            String newName = oldName + "+";
            DiscreteVariable newVariable =
                    new DiscreteVariable(newName, newCategories);

            variables.add(newVariable);
        }

        RectangularDataSet newDataSet =
                new ColtDataSet(dataSet.getNumRows(), variables);

        // Copy old values to new data set, replacing missing values with new
        // "MissingValue" categories.
        for (int i = 0; i < dataSet.getNumRows(); i++) {
            for (int j = 0; j < dataSet.getNumColumns(); j++) {
                DiscreteVariable variable =
                        (DiscreteVariable) dataSet.getVariable(j);
                int numCategories = variable.getNumCategories();
                int value = dataSet.getInt(i, j);

                if (value == DiscreteVariable.MISSING_VALUE) {
                    newDataSet.setInt(i, j, numCategories);
                }
                else {
                    newDataSet.setInt(i, j, value);
                }
            }
        }

        return newDataSet;
    }
}


