package edu.cmu.tetrad.util;

/**
 * Generates (nonrecursively) all of the combinations of objects, where the
 * number of objects in each dimension is specified. The sequence of choices is
 * obtained by repeatedly calling the next() method.  When the sequence is
 * finished, null is returned.
 * <p/>
 * A valid combination for the sequence of combinations for a choose b generated
 * by this class is an array x[] of b integers i, 0 <= i < a, such that x[j] <
 * x[j + 1] for each j from 0 to b - 1.
 *
 * @author Joseph Ramsey
 */
public final class CombinationGenerator {

    /**
     * The number of items for each dimension.
     */
    int[] dims;

    /**
     * The internally stored choice.
     */
    private int[] local;

    /**
     * The choice that is returned. Used, since the returned array can be
     * modified by the user.
     */
    private int[] returned;

    /**
     * Indicates whether the next() method has been called since the last
     * initialization.
     */
    private boolean begun;

    /**
     * Constructs a new combination of objects, choosing one object from
     * each dimension.
     * @param dims the number of objects in each dimension. Each member must
     * be >= 0.
     * @throws NullPointerException if dims is null.
     */
    public CombinationGenerator(int[] dims) {
        this.dims = dims;
        local = new int[dims.length];
        returned = new int[dims.length];

        // Initialize the combination array with successive integers [0 1 2 ...].
        // Set the value at the last index one less than it would be in such
        // a series, ([0 1 2 ... b - 2]) so that on the first call to next()
        // the first combination ([0 1 2 ... b - 1]) is returned correctly.
        for (int i = 0; i < dims.length - 1; i++) {
            local[i] = 0;
        }

        if (local.length > 0) {
            local[local.length - 1] = -1;
        }

        begun = false;
    }

    /**
     * Returns the next combination in the series, or null if the series is
     * finished.  The array that is produced should not be altered by the user,
     * as it is reused by the choice generator.
     * <p/>
     * If the number of items chosen is zero, a zero-length array will be
     * returned once, with null after that.
     * <p/>
     * The array that is returned is reused, but modifying it will not change
     * the sequence of choices returned.
     *
     * @return the next combination in the series, or null if the series is
     *         finished.
     */
    public int[] next() {
        int i = getNumObjects();

        // Scan from the right for the first index whose value is less than
        // its expected maximum (i + diff) and perform the fill() operation
        // at that index.
        while (--i > -1) {
            if (this.local[i] < dims[i] - 1) {
                fill(i);
                begun = true;
                System.arraycopy(local, 0, returned, 0, getNumObjects());
                return returned;
            }
        }

        if (this.begun) {
            return null;
        } else {
            begun = true;
            System.arraycopy(local, 0, returned, 0, getNumObjects());
            return returned;
        }
    }

    /**
     * This static method will print the series of combinations for a choose b
     * to System.out.
     */
    public static void testPrint(int[] dims) {
        CombinationGenerator cg = new CombinationGenerator(dims);
        int[] choice;

        System.out.println();
        System.out.print("Printing combinations for (");

        for (int i = 0; i < dims.length; i++) {
            System.out.print(dims[i]);

            if (i < dims.length - 1) {
                System.out.print(", ");
            }
        }

        System.out.println(")\n");

        while ((choice = cg.next()) != null) {
            if (choice.length == 0) {
                System.out.println("zero-length array");
            } else {
                for (int aChoice : choice) {
                    System.out.print(aChoice + "\t");
                }

                System.out.println();
            }
        }

        System.out.println();
    }

    /**
     * Returns the number of objects being chosen.
     */
    public int getNumObjects() {
        return local.length;
    }

    /**
     * Fills the 'choice' array, from index 'index' to the end of the array,
     * with successive integers starting with choice[index] + 1.
     *
     * @param index the index to begin this incrementing operation.
     */
    private void fill(int index) {
        this.local[index]++;

        for (int i = index + 1; i < getNumObjects(); i++) {
            local[i] = 0;
        }
    }
}
