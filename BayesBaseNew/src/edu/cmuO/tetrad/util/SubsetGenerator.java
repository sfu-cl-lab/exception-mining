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
 * <p>Generates indicators for all subsets of a set of size a. A boolean array
 * is returned, where positions marked with 'true' correspond to items including
 * in the subset. </p> </p> <p>This class is especially useful for selecting all
 * possible combinations of orientations of edges belonging to a given node. See
 * the RobustPcSearch class for a application of this class.</p>
 *
 * @author Ricardo Bezerra
 * @version $Revision: 5177 $ $Date: 2006-01-05 17:09:26 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class SubsetGenerator {

    /**
     * The size of the set.
     */
    private int a;

    /**
     * The number of selected items on a given moment
     */
    private int b;

    /**
     * The choice that is returned.  This value should not be altered by the
     * user.
     */
    private boolean[] choice;


    private ChoiceGenerator cg;

    /**
     * Constructs a new subset generator for a set of a items.
     *
     * @param a the size of the set.
     */
    public SubsetGenerator(int a) {

        if (a < 0) {
            throw new IllegalArgumentException(
                    "For subset generation, a should be nonnegative.");
        }

        this.a = a;
        b = 0;
        cg = new ChoiceGenerator(a, b);
        choice = new boolean[a];
    }

    /**
     * <p>Returns the next combination in the series, or null if the series is
     * finished.  The array that is produced should not be altered by the user,
     * as it is reused by the choice generator.</p>
     *
     * @return the next combination in the series, or null if the series is
     *         finished.
     */
    public boolean[] next() {

        int[] next;

        if ((next = cg.next()) != null) {
            fill(next);

            return choice;
        }

        if (b < a) {
            b++;

            cg = new ChoiceGenerator(a, b);
            next = cg.next();

            fill(next);

            return choice;
        }

        return null;
    }

    /**
     * Fills the 'choice' array, from index 'index' to the end of the array,
     * with boolean corresponding for the chosen elements in 'index'.
     *
     * @param index the index to begin this mounting operation.
     */
    private void fill(int[] index) {

        for (int i = 0; i < a; i++) {
            choice[i] = false;
        }

        for (int i = 0; i < b; i++) {
            choice[index[i]] = true;
        }
    }

    /**
     * This static method will print all selections for a subsets of a set of a
     * elements to System.out.
     *
     * @param a the size of the set.
     */
    public static void testPrint(int a) {

        SubsetGenerator ag = new SubsetGenerator(a);
        boolean[] choice;
        int total = 0;

        System.out.println();
        System.out.println("Printing all subsets of " + a + " elements :");
        System.out.println();

        while ((choice = ag.next()) != null) {
            for (int i = 0; i < a; i++) {
                if (choice[i]) {
                    System.out.print("1 ");
                }
                else {
                    System.out.print("0 ");
                }
            }

            total++;

            System.out.println();
        }

        System.out.println("Number of combinations: " + total);
    }
}


