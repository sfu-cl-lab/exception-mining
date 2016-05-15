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

package edu.cmu.tetrad.data;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.NumberFormatUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;

/**
 * Provides static methods for saving data to files.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6337 $ $Date: 2006-01-19 17:54:39 -0500 (Thu, 19 Jan
 *          2006) $
 */
public final class DataSavers {

    /**
     * Saves out a dataset. The dataset may have continuous and/or discrete
     * columns.
     * @param dataSet   The data set to save.
     * @param out       The writer to print the output to.
     * @param separator The character separating fields, usually '\t' or ','.
     * @throws IOException If there is some problem dealing with the writer.
     */
    public static void saveRectangularData(RectangularDataSet dataSet,
            Writer out, char separator) throws IOException {
        NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();
        StringBuffer buf = new StringBuffer();

        boolean isCaseMultipliersCollapsed = dataSet.isMulipliersCollapsed();

        if (isCaseMultipliersCollapsed) {
            buf.append("MULT").append(separator);
        }

        for (int col = 0; col < dataSet.getNumColumns(); col++) {
            String name = dataSet.getVariable(col).getName();

            if (name.trim().equals("")) {
                name = "C" + (col - 1);
            }

            buf.append(name);

            if (col < dataSet.getNumColumns() - 1) {
                buf.append(separator);
            }
        }

        for (int row = 0; row < dataSet.getNumRows(); row++) {
            buf.append("\n");

            if (isCaseMultipliersCollapsed) {
                int multiplier = dataSet.getMultiplier(row);
                buf.append(multiplier + separator);
            }

            for (int col = 0; col < dataSet.getNumColumns(); col++) {
                Node variable = dataSet.getVariable(col);

                if (variable instanceof ContinuousVariable) {
                    double value = dataSet.getDouble(row, col);

                    if (ContinuousVariable.isDoubleMissingValue(value)) {
                        buf.append("*");
                    }
                    else {
                        buf.append(nf.format(value));
                    }

                    if (col < dataSet.getNumColumns() - 1) {
                        buf.append(separator);
                    }
                }
                else if (variable instanceof DiscreteVariable) {
                    Object obj = dataSet.getObject(row, col);
                    String val = ((obj == null) ? "" : obj.toString());

                    buf.append(val);

                    if (col < dataSet.getNumColumns() - 1) {
                        buf.append(separator);
                    }
                }
            }
        }

        buf.append("\n");
        out.write(buf.toString());
    }

    public static void saveCovMatrix(CovarianceMatrix covMatrix,
            PrintWriter out, NumberFormat nf) {
        int numVars = covMatrix.getVariableNames().size();
//        out.println("/Covariance");
        out.println(covMatrix.getSampleSize());

        for (int i = 0; i < numVars; i++) {
            String name = covMatrix.getVariableNames().get(i);
            out.print(name + "\t");
        }

        out.println();

        for (int j = 0; j < numVars; j++) {
            for (int i = 0; i <= j; i++) {
                out.print(nf.format(covMatrix.getValue(i, j)) + "\t");
            }
            out.println();
        }
    }
}


