package edu.cmu.tetrad.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a static method for sampling without replacement from a dataset to
 * create a new dataset with a sample size supplied by the user.
 *
 * @author Frank Wimberly
 * @version $Revision: 4524 $ $Date: 2005-12-13 17:51:58 -0500 (Tue, 13 Dec
 *          2005) $
 */
public final class RandomSampler {

    /**
     * This method takes a dataset and a sample size and creates a new dataset
     * containing that number of samples by drawing with replacement from the
     * original dataset.
     */
    public static RectangularDataSet sample(RectangularDataSet dataSet,
                                            int newSampleSize) {
        if (newSampleSize < 1) {
            throw new IllegalArgumentException("Sample size must be > 0.");
        }

        if (dataSet.getNumRows() < 1) {
            throw new IllegalArgumentException("Dataset must contain samples.");
        }

        if (dataSet.getNumRows() < newSampleSize) {
            throw new IllegalArgumentException("Not enough cases in data to " +
                    "generate " + newSampleSize + " samples without replacement.");
        }

        List<Integer> indices = new ArrayList<Integer>(dataSet.getNumRows());

        for (int i = 0; i < dataSet.getNumRows(); i++) {
            indices.add(i);
        }

        Collections.shuffle(indices);

        //Number of samples in input dataset
        int ncols = dataSet.getNumColumns();

        RectangularDataSet newDataSet =
                new ColtDataSet(newSampleSize, dataSet.getVariables());

        for (int i = 0; i < newSampleSize; i++) {
            int oldCase = indices.get(i);

            for (int j = 0; j < ncols; j++) {
                newDataSet.setObject(i, j, dataSet.getObject(oldCase, j));
            }
        }

        return newDataSet;
    }
}
