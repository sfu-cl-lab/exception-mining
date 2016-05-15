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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.RectangularDataSet;

/**
 * Implements a test of tetrad constraints in a known correlation matrix. It
 * might be useful for debugging BuildPureClusters/Purify-like algorithms.
 *
 * @author Ricardo Silva
 * @version $Revision: 4524 $ $Date: 2006-01-20 15:58:02 -0500 (Fri, 20 Jan
 *          2006) $
 */

public class PopulationTetradTest implements TetradTest {
    private CorrelationMatrix CorrelationMatrix;
    private boolean bvalues[];
    private final double epsilon = 0.001;

    public PopulationTetradTest(CorrelationMatrix CorrelationMatrix) {
        this.CorrelationMatrix = CorrelationMatrix;
        bvalues = new boolean[3];
    }

    @Override
	public String[] getVarNames() {
        return CorrelationMatrix.getVariableNames().toArray(new String[0]);
    }

    @Override
	public RectangularDataSet getDataSet() {
        return null;
    }

    /**
     * Population scores: assumes CorrelationMatrix is the population covariance
     * CorrelationMatrix. Due to numerical rounding problems, we need a
     * parameter epsilon to control it. Nothing here is implemented for discrete
     * data (yet).
     */

    @Override
	public int tetradScore(int v1, int v2, int v3, int v4) {
        int count = 0;

        double p_12 = CorrelationMatrix.getValue(v1, v2);
        double p_13 = CorrelationMatrix.getValue(v1, v3);
        double p_14 = CorrelationMatrix.getValue(v1, v4);
        double p_23 = CorrelationMatrix.getValue(v2, v3);
        double p_24 = CorrelationMatrix.getValue(v2, v4);
        double p_34 = CorrelationMatrix.getValue(v3, v4);

        for (int i = 0; i < 3; i++) {
            bvalues[i] = false;
        }

        if (Math.abs(p_12 * p_34 - p_13 * p_24) < epsilon) {
            count++;
            bvalues[0] = true;
        }
        if (Math.abs(p_12 * p_34 - p_14 * p_23) < epsilon) {
            count++;
            bvalues[1] = true;
        }
        if (Math.abs(p_13 * p_24 - p_14 * p_23) < epsilon) {
            count++;
            bvalues[2] = true;
        }
        return count;
    }

    @Override
	public boolean tetradScore3(int v1, int v2, int v3, int v4) {
        return tetradScore(v1, v2, v3, v4) == 3;
    }

    @Override
	public boolean tetradScore1(int v1, int v2, int v3, int v4) {
        if (tetradScore(v1, v2, v3, v4) != 1) {
            return false;
        }
        return bvalues[2];
    }

    @Override
	public boolean tetradHolds(int v1, int v2, int v3, int v4) {
        double p_12 = CorrelationMatrix.getValue(v1, v2);
        double p_13 = CorrelationMatrix.getValue(v1, v3);
        double p_24 = CorrelationMatrix.getValue(v2, v4);
        double p_34 = CorrelationMatrix.getValue(v3, v4);
        bvalues[0] = Math.abs(p_12 * p_34 - p_13 * p_24) < epsilon;
        return bvalues[0];
    }

    @Override
	public boolean oneFactorTest(int a, int b, int c, int d) {
        return tetradScore3(a, b, c, d);
    }

    @Override
	public boolean oneFactorTest(int a, int b, int c, int d, int e) {
        return tetradScore3(a, b, c, d) && tetradScore3(a, b, c, e) &&
                tetradScore3(b, c, d, e);
    }

    @Override
	public boolean oneFactorTest(int a, int b, int c, int d, int e, int f) {
        return tetradScore3(a, b, c, d) && tetradScore3(b, c, d, e) &&
                tetradScore3(c, d, e, f);
    }

    @Override
	public boolean twoFactorTest(int a, int b, int c, int d) {
        tetradScore(a, b, c, d);
        return bvalues[2];
    }

    @Override
	public boolean twoFactorTest(int a, int b, int c, int d, int e) {
        tetradScore(a, b, d, e);

        if (!bvalues[2]) {
            return false;
        }

        tetradScore(a, c, d, e);

        if (!bvalues[2]) {
            return false;
        }

        tetradScore(b, c, d, e);
        return bvalues[2];
    }

    @Override
	public boolean twoFactorTest(int a, int b, int c, int d, int e, int f) {
        if (!twoFactorTest(a, b, c, d, e)) {
            return false;
        }

        if (!twoFactorTest(a, b, c, d, f)) {
            return false;
        }

        return twoFactorTest(a, b, c, e, f);
    }

    @Override
	public double tetradPValue(int v1, int v2, int v3, int v4) {
        //TODO: evalTetradDifference(v1, v2, v3, v4);
        //return prob[0];
        return -1;
    }

    @Override
	public double getSignificance() {
        return 0;
    }

    @Override
	public void setSignificance(double sig) {
        throw new UnsupportedOperationException();
    }
}


