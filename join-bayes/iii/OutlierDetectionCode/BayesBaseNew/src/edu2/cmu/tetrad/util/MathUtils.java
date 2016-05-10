///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Some extra mathematical functions not contained in java.lang.Math.
 *
 * @author Joseph Ramsey
 */
public class MathUtils {

    /**
     * @param x a double value.
     * @return the logistic function of x = 1 / (1 + exp(-x)).
     */
    public static double logistic(double x) {
        return 1. / (1. + Math.exp(-x));
    }

    public static int factorial(int n) {
        int i = 1;

        for (int j = 1; j <= n; j++) {
            i *= j;
        }

        return i;
    }

//    public static double bigExp(double x) {
////        double value = Math.exp(x);
////
////        if (!Double.isInfinite(value)) {
////            return value;
////        }
//
//        BigDecimal d1 = new BigDecimal(x);
//        BigDecimal f = new BigDecimal(1);
//        BigDecimal d2 = new BigDecimal(1);
//        BigDecimal sum = new BigDecimal(1);
//
//        for (int i = 1; i <= 20; i++) {
//            f = f.multiply(new BigDecimal(i));
//            d2 = d2.multiply(d1);
//            BigDecimal frac = d2.divide(f, 8, RoundingMode.UP);
//            sum = sum.add(frac);
//        }
//
//        return sum.doubleValue();
//    }
}

