package edu.cmu.tetradapp.model.datamanip;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataFilter;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

/**
 * Add description
 *
 * @author Tyler Gibson
 * @version $Revision: 1 $ $Date: Jan 29, 2007 3:23:25 AM $
 */
public class RemoveMissingCasesDataFilter implements DataFilter {


    @Override
	public RectangularDataSet filter(RectangularDataSet data) {
        List<Node> variables = data.getVariables();
        RectangularDataSet newDataSet = new ColtDataSet(0, variables);
        int newRow = 0;

        ROWS:
        for (int row = 0; row < data.getNumRows(); row++) {
            for (int col = 0; col < data.getNumColumns(); col++) {
                Node variable = data.getVariable(col);
                if (((Variable) variable).isMissingValue(data.getObject(row, col))) {
                    continue ROWS;
                }
            }

            for (int col = 0; col < data.getNumColumns(); col++) {
                newDataSet.setObject(newRow, col, data.getObject(row, col));
            }
            newRow++;
        }

        return newDataSet;
    }
}
