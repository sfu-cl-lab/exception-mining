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

import edu.cmu.tetrad.util.PersistentRandomUtil;

/**
 * Provides a static method for sampling with replacement from a dataset to
 * create a new dataset with a sample size supplied by the user.
 * <p/>
 * Since sampling is done with replacement, the output dataset can have more
 * samples than the input.
 *
 * @author Frank Wimberly
 * @version $Revision: 6039 $ $Date: 2005-12-13 17:51:58 -0500 (Tue, 13 Dec
 *          2005) $
 */
public final class BootstrapSampler {


   // private TetradLogger logger = TetradLogger.getInstance();


    /**
     * Constructs a sample that does not do any logging.
     */
    public BootstrapSampler(){

    }


    //============================== Public methods ================================//


    /**
     * This method takes a dataset and a sample size and creates a new dataset
     * containing that number of samples by drawing with replacement from the
     * original dataset.
     */
    public RectangularDataSet sample(RectangularDataSet dataSet, int newSampleSize) {
        if (newSampleSize < 1) {
            throw new IllegalArgumentException("Sample size must be > 0.");
        }

        if (dataSet.getNumRows() < 1) {
            throw new IllegalArgumentException("Dataset must contain samples.");
        }
     //   this.logger.log("sampleSize", String.valueOf(newSampleSize));
        //Number of samples in input dataset
        int sampleSize = dataSet.getNumRows();
        int ncols = dataSet.getNumColumns();

        RectangularDataSet newDataSet = new ColtDataSet(newSampleSize, dataSet.getVariables());
        for (int row = 0; row < newSampleSize; row++) {
            double r = PersistentRandomUtil.getInstance().nextDouble();
            int oldCase = (int) (r * sampleSize);
            for (int col = 0; col < ncols; col++) {
                newDataSet.setObject(row, col, dataSet.getObject(oldCase, col));
            }
        }


    //    this.logger.flush();

        return newDataSet;
    }


}


