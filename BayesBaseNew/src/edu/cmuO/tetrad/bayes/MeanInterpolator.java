package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Returns a data set in which missing values in each column are filled using
 * the mean of that column.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class MeanInterpolator implements DataFilter {
    @Override
	public RectangularDataSet filter(RectangularDataSet dataSet) {
        List<Node> variables = new LinkedList<Node>();

        for (int i = 0; i < dataSet.getNumColumns(); i++) {
            Node variable = dataSet.getVariable(i);
            variables.add(variable);
        }

        RectangularDataSet newDataSet = new ColtDataSet((ColtDataSet) dataSet);

        for (int j = 0; j < newDataSet.getNumColumns(); j++) {
            if (newDataSet.getVariable(j) instanceof ContinuousVariable) {
                double sum = 0.0;
                int count = 0;

                for (int i = 0; i < newDataSet.getNumRows(); i++) {
                    if (!Double.isNaN(newDataSet.getDouble(i, j))) {
                        sum += newDataSet.getDouble(i, j);
                        count++;
                    }
                }

                double mean = sum / count;

                for (int i = 0; i < newDataSet.getNumRows(); i++) {
                    if (Double.isNaN(newDataSet.getDouble(i, j))) {
                        newDataSet.setDouble(i, j, mean);
                    }
                }
            }
        }

        return newDataSet;
    }
}
