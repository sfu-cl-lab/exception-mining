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

package edu.cmu.tetrad.sem;

import pal.math.ConjugateDirectionSearch;
import pal.math.MultivariateFunction;
import edu.cmu.tetrad.util.TetradLogger;

/**
 * Optimizes a SEM using the ConjugateDirectionSearch class in the PAL library.
 *
 * @author Ricardo Silva
 * @author Joseph Ramsey
 */
public class SemOptimizerPalCds implements SemOptimizer {
    static final long serialVersionUID = 23L;

    /**
     * Absolute tolerance of function value.
     */
    private static final double FUNC_TOLERANCE = 1.0e-4;

    /**
     * Absolute tolerance of each parameter.
     */
    private static final double PARAM_TOLERANCE = 1.0e-3;

    //=========================CONSTRUCTORS============================//

    /**
     * Blank constructor.
     */
    public SemOptimizerPalCds() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemOptimizerPalCds serializableInstance() {
        return new SemOptimizerPalCds();
    }

    //=========================PUBLIC METHODS==========================//

    /**
     * Optimizes the fitting function for the SEM.
     */
    @Override
	public void optimize(SemIm semIm) {
        // Optimize the semIm. Note that the the covariance matrix of the
        // sample data is made available to the following PalFittingFunction.
        ConjugateDirectionSearch search = new ConjugateDirectionSearch();
        search.step = 10.0;
        search.optimize(fittingFunction(semIm),
                semIm.getFreeParamValues(), FUNC_TOLERANCE, PARAM_TOLERANCE);
    }

    private PalFittingFunction fittingFunction(SemIm sem) {
        return new PalFittingFunction(sem);
    }

    /**
     * Wraps the SEM maximum likelihood fitting function for purposes of being
     * evaluated using the PAL ConjugateDirection optimizer.
     *
     * @author Joseph Ramsey
     * @version $Revision: 6112 $ $Date: 2005-07-07 17:17:28 -0400 (Thu, 07 Jul
     *          2005) $
     */
    static class PalFittingFunction implements MultivariateFunction {

        /**
         * The wrapped Sem.
         */
        private final SemIm sem;

        /**
         * Constructs a new PalFittingFunction for the given Sem.
         */
        public PalFittingFunction(SemIm sem) {
            this.sem = sem;
        }

        /**
         * Computes the maximum likelihood function value for the given
         * argument values as given by the optimizer. These values are mapped to
         * parameter values.
         */
        @Override
		public double evaluate(final double[] argument) {
            for (int i = 0; i < argument.length; i++) {
                if (Double.isNaN(argument[i])) {
                    throw new IllegalArgumentException("Attempt to set parameter " +
                            "value to NaN.");
                }
            }

            this.sem.setFreeParamValues(argument);

            double fml = sem.getFml();

            TetradLogger.getInstance().info("FML = " + fml);
            System.out.println("FML = " + fml);

            if (Double.isNaN(fml)) {
                return 10000.0;
            }

            return fml;
        }

        /**
         * Returns the number of arguments. Required by the MultivariateFunction
         * interface.
         */
        @Override
		public int getNumArguments() {
            return this.sem.getNumFreeParams();
        }

        /**
         * Returns the lower bound of argument n. Required by the
         * MultivariateFunction interface.
         */
        @Override
		public double getLowerBound(final int n) {
            Parameter param = this.sem.getFreeParameters().get(n);
            return (param.getType() == ParamType.COEF ||
                    param.getType() == ParamType.COVAR) ? -10000.0 : 0.0001;
        }

        /**
         * Returns the upper bound of argument n. Required by the
         * MultivariateFunction interface.
         */
        @Override
		public double getUpperBound(final int n) {
            return 10000.0;
        }
    }
}







