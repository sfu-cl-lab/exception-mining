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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class TestPersistentRandomUtil extends TestCase {

    public TestPersistentRandomUtil(String name) {
        super(name);
    }

    public void testIntegralSumToOne() {
        int numBoxes = 100;
        int[] boxes = new int[numBoxes];

        for (int i = 0; i < 100000000; i++) {
            int randomint = RandomUtil.getInstance().nextInt(numBoxes);
            boxes[randomint]++;
        }

        for (int i = 0; i < numBoxes; i++) {
            System.out.println("Count for box " + i + " = " + boxes[i]);
        }
    }


    public void testGaussian() {
        int numBoxes = 40;
        double low = -4;
        double high = 4;
        NumberFormat nf = new DecimalFormat("0.000");

        int[] boxes = new int[numBoxes];

        double[] cutoffs = new double[numBoxes - 1];

        for (int i = 0; i < cutoffs.length; i++) {
             cutoffs[i] = low + (i / (double) (cutoffs.length - 1)) * (high - low);
        }

        LOOP:
        for (int i = 0; i < 1000000; i++) {
            double r = RandomUtil.getInstance().nextNormal(0, 1);

            for (int j = 0; j < cutoffs.length; j++) {
                if (r < cutoffs[j]) {
                    boxes[j]++;
                    continue LOOP;
                }
            }

            boxes[boxes.length - 1]++;
        }

        for (int i = 0; i < cutoffs.length; i++) {
            System.out.println("< " + nf.format(cutoffs[i]) + " = " + boxes[i]);
        }

        System.out.println("> " + nf.format(cutoffs[cutoffs.length - 1]) + " = " + boxes[cutoffs.length]);
    }

    public void testDouble() {
        int numBoxes = 40;
        double low = 0;
        double high = 1;
        NumberFormat nf = new DecimalFormat("0.000");

        int[] boxes = new int[numBoxes];

        double[] cutoffs = new double[numBoxes - 1];

        for (int i = 0; i < cutoffs.length; i++) {
             cutoffs[i] = low + (i / (double) (cutoffs.length - 1)) * (high - low);
        }

        LOOP:
        for (int i = 0; i < 1000000; i++) {
            double r = RandomUtil.getInstance().nextDouble();

            for (int j = 0; j < cutoffs.length; j++) {
                if (r < cutoffs[j]) {
                    boxes[j]++;
                    continue LOOP;
                }
            }

            boxes[boxes.length - 1]++;
        }

        for (int i = 0; i < cutoffs.length; i++) {
            System.out.println("< " + nf.format(cutoffs[i]) + " = " + boxes[i]);
        }

        System.out.println("> " + nf.format(cutoffs[cutoffs.length - 1]) + " = " + boxes[cutoffs.length]);
    }

    public static Test suite() {
        return new TestSuite(TestPersistentRandomUtil.class);
    }
}
