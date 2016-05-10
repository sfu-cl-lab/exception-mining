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

package edu.cmu.tetrad.util;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import java.util.Date;

/**
 * Provides a common set of random number generators that should be used
 * throughout the Tetrad application, to avoid problems with random number
 * generators being created more often than once a millisecond. When this
 * happens, random numbers are not properly generated, because
 * System.currentTimeMillis() is standardly used as a seed for these generators.
 * So creating your own instances of Random will fail for this reason; there
 * will be a lot less randomness than you think there should be, and this will
 * probably be the reason.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5813 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public class PersistentRandomUtil implements RandomUtil {
    private static RandomUtil INSTANCE = new PersistentRandomUtil();

    private Uniform uniform;
    private Normal normal;

    RandomEngine randomEngine = new MersenneTwister();

    private PersistentRandomUtil() {
        MersenneTwister mersenneTwister = new MersenneTwister(new Date());
        this.uniform = new Uniform(0.0, 1.0, mersenneTwister);
        this.normal = new Normal(0.0, 1.0, mersenneTwister);
    }

    /**
     * Returns the singleton instance of this class.
     */
    public static RandomUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Returns a random integer between 0 and n - 1, inclusive.
     */
    @Override
	public int nextInt(int n) {
        double r = uniform.nextDouble();
        return (int) (r * n);

        // This seems to be problematic for bootstrap sampling
        // with large sample sizes. jdramsey 12/13/2005
//        return uniform.nextIntFromTo(0, n - 1);
    }

    @Override
	public long nextLong(long n) {
        return uniform.nextLongFromTo(0, n);
    }

    @Override
	public double nextDouble() {
        return uniform.nextDouble();
    }

    @Override
	public double nextGaussian() {
        return normal.nextDouble();
    }

    @Override
	public RandomEngine getRandomEngine() {
        return randomEngine;
    }
}


