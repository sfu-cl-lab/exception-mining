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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.PermutationGenerator;
import edu.cmu.tetrad.util.RandomUtil;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A gadget to count truth table functions for Clark. Counting the number of truth table functions from a set of "cause"
 * binary variables to an outcome binary variable such that for each variable there is are two rows that differ only in
 * the value of that variable such that the function value for those two rows is different.
 *
 * @author Joseph Ramsey
 */
public class BinaryFunctionUtils {

    /**
     * The number of arguments of the binary function.
     */
    private int numArgs;

    public BinaryFunctionUtils(int numArgs) {
        this.numArgs = numArgs;
    }

    public void printAllFunctions() {
        for (int i = 0; i < getNumFunctions(); i++) {
            System.out.println(new BinaryFunction(numArgs, i));
        }
    }

    public long getNumFunctions() {
        int numRows = getNumRows();
        long numFunctions = 1L;

        for (int i = 0; i < numRows; i++) {
            numFunctions *= 2L;
        }
        return numFunctions;
    }

    public int getNumRows() {
        BinaryFunction first = new BinaryFunction(numArgs, 0);
        return first.getNumRows();
    }

    public long count() {
        Set<Integer> variables = new HashSet<Integer>();
        for (int i = 0; i < numArgs; i++) variables.add(i);
        long numValidated = 0;
        long numFunctions = getNumFunctions();
        BinaryFunction function = new BinaryFunction(numArgs, 0);

        FUNCTION:
        for (long index = 0; index < numFunctions; index++) {
            if (index % 100000 == 0) {
                System.out.println("..." + index + " counted so far, " +
                        numValidated + " validated tables so far...");
            }

            function.resetFunction(index);
            Set<Integer> validated = new HashSet<Integer>();

            for (int row = 0; row < getNumRows(); row++) {
                boolean[] vals = function.getRow(row);
                boolean val1 = function.getValue(vals);

                for (int v : variables) {
                    if (validated.contains(v)) continue;

                    vals[v] = !vals[v];
                    boolean val2 = function.getValue(vals);

                    if (val1 != val2) {
                        validated.add(v);
                        if (validated.size() == numArgs) {
                            if (function.getOppositeFunction() < index) {
                                continue FUNCTION;
                            }

                            if (function.getSymmetricFunction() < index) {
                                continue FUNCTION;
                            }

                            if (function.getSymmetricOppositeFunction() < index) {
                                continue FUNCTION;
                            }

//                            System.out.println(new BinaryFunction(numArgs, index));

                            numValidated++;
                            continue FUNCTION;
                        }
                    }

                    vals[v] = !vals[v];
                }
            }
        }

        return numValidated;
    }

    public boolean satisfiesTestPair(BinaryFunction function) {
        Set<Integer> variables = new HashSet<Integer>();
        for (int i = 0; i < numArgs; i++) variables.add(i);
        Set<Integer> validated = new HashSet<Integer>();

        for (int row = 0; row < getNumRows(); row++) {
            boolean[] vals = function.getRow(row);
            boolean val1 = function.getValue(vals);

            for (int v : variables) {
                if (validated.contains(v)) continue;

                vals[v] = !vals[v];
                boolean val2 = function.getValue(vals);

                if (val1 != val2) {
                    validated.add(v);
                    if (validated.size() == numArgs) {
                        return true;
                    }
                }

                vals[v] = !vals[v];
            }
        }

        return false;
    }

    public List<BinaryFunction> findNontransitiveTriple() {
        long numFunctions = getNumFunctions();
        int n = 0;

        RandomUtil random = RandomUtil.getInstance();

        for (long t1 = 0; t1 < numFunctions; t1++) {
//        for (int i = 1; i < 500; i++) {
//            long t1 = random.nextLong(numFunctions);

            BinaryFunction g1 = new BinaryFunction(numArgs, t1);
            if (!satisfiesTestPair(g1)) continue;

            PermutationGenerator cg1 = new PermutationGenerator(numArgs);
            int[] p1;

            while ((p1 = cg1.next()) != null) {
                long switched1 = g1.switchColsFull(p1);
                BinaryFunction g2 = new BinaryFunction(g1.getNumArgs(), switched1);
                if (g1.equals(g2)) continue;
                if (!satisfiesTestPair(g2)) continue;

                PermutationGenerator cg2 = new PermutationGenerator(numArgs);
                int[] p2;

                while ((p2 = cg2.next()) != null) {
                    long switched2 = g2.switchColsFull(p2);
                    BinaryFunction g3 = new BinaryFunction(g2.getNumArgs(), switched2);
                    if (g3.equals(g1)) continue;
                    if (g3.equals(g2)) continue;
                    if (!satisfiesTestPair(g3)) continue;

                    boolean transposable = equalsUnderSomePermutation(g1, g3);

                    if (!transposable) {
                        LinkedList<BinaryFunction> list = new LinkedList<BinaryFunction>();
                        list.add(g1);
                        list.add(g2);
                        list.add(g3);

                        ++n;

                        System.out.println("#" + n);

                        System.out.println("G1" + g1);
                        System.out.println("G2" + g2);
                        System.out.println("G3" + g3);
                        System.out.println("==============");

//                        if (n == 100) {
//                            return list;
//                        }
                    }
                }
            }
        }

        return null;
    }

    public long count2() {
        Set<Integer> variables = new HashSet<Integer>();
        for (int i = 0; i < numArgs; i++) variables.add(i);
        long numFunctions = getNumFunctions();

        Set<BinaryFunction> prototypes = new HashSet<BinaryFunction>();

        for (long index = 0; index < numFunctions; index++) {
            if (index % 1000 == 0) System.out.println(index + " " + prototypes.size());

            BinaryFunction candidate = new BinaryFunction(numArgs, index);

            if (candidate.getOppositeFunction() < index) {
                continue;
            }

            if (candidate.getSymmetricFunction() < index) {
                continue;
            }

            if (candidate.getSymmetricOppositeFunction() < index) {
                continue;
            }

            if (!satisfiesTestPair(candidate)) {
                continue;
            }

//            System.out.println("a");

            if (classOfSomePrototype(candidate, prototypes)) {
                continue;
            }

            prototypes.add(candidate);
        }

        return prototypes.size();

    }

    private boolean classOfSomePrototype(BinaryFunction candidate, Set<BinaryFunction> prototypes) {
        for (BinaryFunction f3 : prototypes) {
            if (equalsUnderSomePermutation(candidate, f3)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if f1 is equal to f2 under some column permutation.
     */
    private boolean equalsUnderSomePermutation(BinaryFunction f1, BinaryFunction f2) {
        PermutationGenerator pg = new PermutationGenerator(f1.getNumArgs());
        int[] permutation;

        while ((permutation = pg.next()) != null) {
            if (equalsUnderPermutation(f1, f2, permutation)) {
                return true;
            }
        }

        return false;
    }

    private boolean equalsUnderBinaryPermutation(BinaryFunction f1, BinaryFunction f2) {
        ChoiceGenerator pg = new ChoiceGenerator(f1.getNumArgs(), 2);
        int[] permutation;

        while ((permutation = pg.next()) != null) {
            if (equalsUnderBinaryPermutation(f1, f2, permutation)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkTriple(int numArgs, boolean[] g1Rows, boolean[] g2Rows, boolean[] g3Rows) {
        BinaryFunction g1 = new BinaryFunction(numArgs, g1Rows);
        BinaryFunction g2 = new BinaryFunction(numArgs, g2Rows);
        BinaryFunction g3 = new BinaryFunction(numArgs, g3Rows);

        System.out.println("g1");
        System.out.println(g1);
        System.out.println("g2");
        System.out.println(g2);
        System.out.println("g3");
        System.out.println(g3);

        if (g1.equals(g2) || g2.equals(g3) || g1.equals(g3)) {
            return false;
        }

        if (!satisfiesTestPair(g1)) {
            System.out.println("g1 fails test pair condition.");
//            return false;
        }

        if (!satisfiesTestPair(g2)) {
            System.out.println("g2 fails test pair condition.");
//            return false;
        }

        if (!satisfiesTestPair(g3)) {
            System.out.println("g3 fails test pair condition.");
//            return false;
        }

        if (!equalsUnderSomePermutation(g1, g2)) {
            System.out.println("g1 and g2 not transposable.");
//            return false;
        }

        if (!equalsUnderSomePermutation(g2, g3)) {
            System.out.println("g2 and g3 not transposable.");
//            return false;
        }

        if (equalsUnderSomePermutation(g1, g3)) {
            System.out.println("g1 and g3 transposable.");
//            return false;
        }

        return true;
    }

    /**
     * True if f1 equals f3, where f3 has been tranposed according to choice.
     */
    private boolean equalsUnderBinaryPermutation(BinaryFunction f1, BinaryFunction f3, int[] choice) {
        boolean isTransposable = true;

        for (int i = 0; i < f1.getNumRows(); i++) {
            boolean[] row = f1.getRow(i);

            boolean temp = row[choice[0]];
            row[choice[0]] = row[choice[1]];
            row[choice[1]] = temp;

            if (f1.getValue(row) != f3.getValue(row)) {
                isTransposable = false;
            }
        }

        return isTransposable;
    }

    private boolean equalsUnderPermutation(BinaryFunction f1,
                                           BinaryFunction f3,
                                           int[] permutation) {
        for (int i = 0; i < f1.getNumRows(); i++) {
            boolean[] row = f1.getRow(i);
            boolean[] newRow = new boolean[row.length];

            for (int j = 0; j < row.length; j++) {
                newRow[j] = row[permutation[j]];
            }

            if (f1.getValue(row) != f3.getValue(newRow)) {
                return false;
            }
        }

        return true;
    }

}



