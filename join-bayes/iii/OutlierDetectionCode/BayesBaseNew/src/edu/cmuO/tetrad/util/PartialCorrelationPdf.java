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


/**
 * Frequency function of partial correlation r(12|34...k), assuming that the
 * true partial correlation is equal to zero.  Uses the equation (29.13.4) from
 * Cramer's _Mathematical Methods of Statistics_.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5177 $ $Date: 2005-07-06 13:39:56 -0400 (Wed, 06 Jul
 *          2005) $
 */
public class PartialCorrelationPdf implements Function, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * Number of data points in the sample.
     *
     * @serial
     */
    private int n = 0;

    /**
     * Number of compared variables--that is, 2 + (#conditioning variables).
     *
     * @serial
     */
    private int k = 0;

    /**
     * The ratio of the two gamma expressions in the distribution function for
     * zero partial correlation.
     *
     * @serial
     */
    double gammaRatio = Double.NaN;

    /**
     * The aggregate value of the constant expression in the distribution
     * function for zero partial correlation.
     *
     * @serial
     */
    double constant = Double.NaN;

    /**
     * The power to which the variable expression is raised in the distribution
     * function for zero partial correlation.
     *
     * @serial
     */
    double outsideExp = 0.0;

    //===========================CONSTRUCTORS========================//

    /**
     * Constructs a new zero partial correlation distribution function with the
     * given values for n and k.
     *
     * @param n sample size
     * @param k the number of variables being compared.
     */
    public PartialCorrelationPdf(int n, int k) {
        this.n = n;
        this.k = k;
        this.gammaRatio = gammaRatio(n, k);
        this.constant = (1 / Math.pow(Math.PI, 0.5)) * gammaRatio;
        this.outsideExp = (n - k - 2) / 2.0;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static PartialCorrelationPdf serializableInstance() {
        return new PartialCorrelationPdf(5, 2);
    }

    //==========================PUBLIC METHODS========================//

    /**
     * Calculates the value of the function at the given domain point.
     *
     * @param x the domain point.
     * @return the value of the function at x.
     */
    @Override
	public double valueAt(double x) {
        return constant * Math.pow(1 - x * x, outsideExp);
    }

    /**
     * Calculates the ratio of gamma values in the distribution equation.
     *
     * @param n sample size
     * @param k the number of variables being compared.
     * @return this ratio.
     */
    private double gammaRatio(int n, int k) {
        double top = (n - k + 1) / 2.0;
        double bottom = (n - k) / 2.0;
        double lngamma = ProbUtils.lngamma(top) - ProbUtils.lngamma(bottom);
        return Math.exp(lngamma);
    }

    /**
     * Returns a description of the function.
     */
    @Override
	public String toString() {
        return "Zero partial correlation distribution with n = " + getN() +
                " and k = " + getK() + "\n\n";
    }

    public int getN() {
        return n;
    }

    /**
     * The number of compared variables = 2 + # conditioning variables.
     */
    public int getK() {
        return k;
    }
}


