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

package edu.cmu.tetrad.graph;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public final class TestUniformGraphGenerator extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestUniformGraphGenerator(String name) {
        super(name);
    }

    public void testRandomDag1() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.ANY_DAG);
        generator.setNumNodes(20);
        generator.setMaxDegree(3);
        generator.setNumIterations(50000);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    public void testRandomDag2() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.CONNECTED_DAG);
        generator.setNumNodes(50);
        generator.setMaxDegree(4);
        generator.setMaxEdges(20);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    public void testRandomDag3() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.CONNECTED_DAG);
        generator.setNumNodes(20);
        generator.setMaxDegree(3);
        generator.setNumIterations(20000);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    public void testRandomDag4() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.CONNECTED_DAG);
        generator.setNumNodes(20);
        generator.setMaxInDegree(1);
        generator.setMaxDegree(3);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    public void testRandomDag5() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.CONNECTED_DAG);
        generator.setNumNodes(20);
        generator.setMaxDegree(3);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    public void testRandomDag6() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.ANY_DAG);
        generator.setNumNodes(40);
        generator.setMaxDegree(39);
        generator.setMaxInDegree(2);

        generator.setMaxEdges(10);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        generator.printEdges();

        System.out.println(generator.getDag());
    }

    public void testRandomDag7() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.CONNECTED_DAG);
        generator.setNumNodes(20);
        generator.setMaxInDegree(2);
        generator.setMaxOutDegree(3);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    public void testRandomDag8() {
        long start = System.currentTimeMillis();

        UniformGraphGenerator generator =
                new UniformGraphGenerator(UniformGraphGenerator.CONNECTED_DAG);
        generator.setNumNodes(15);
        generator.setMaxDegree(14);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    /**
     * Tests the second version of the generator that generates random DAGs
     * (unconnected only) with #edges in a given range.
     */
    public void testRandomDag9() {
        long start = System.currentTimeMillis();

        int N = 25;
        int E = N * (N - 1) / 2;

        System.out.println("N = " + N + ", E = " + E);

        UniformGraphGenerator2 generator =
                new UniformGraphGenerator2(UniformGraphGenerator.ANY_DAG);
        generator.setNumNodes(N);
        generator.setMaxEdges(E);
        generator.setMinEdges(E - 5);
        generator.generate();

        long stop = System.currentTimeMillis();
        System.out.println("Elapsed time " + (stop - start) + " ms.");

        System.out.println(generator.getDag());
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestUniformGraphGenerator.class);
    }
}


