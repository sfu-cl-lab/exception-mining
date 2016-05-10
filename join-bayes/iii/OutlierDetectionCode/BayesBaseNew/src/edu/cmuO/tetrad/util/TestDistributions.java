package edu.cmu.tetrad.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;

public class TestDistributions extends TestCase {
    private Function function;

    public TestDistributions(String name) {
        super(name);
    }

    public void testNormal() {
        Distribution normal = new NormalDistribution(10, 1);
        printSortedRandoms(normal);
    }

    public void testUniform() {
        Distribution distribution = new UniformDistribution(-2, 2);
        printSortedRandoms(distribution);
    }

    private void printSortedRandoms(Distribution distribution) {
        double[] values = new double[100];

        for (int i = 0; i < 100; i++) {
            values[i] = distribution.nextRandom();
        }

        Arrays.sort(values);

        for (int i = 0; i < 100; i++) {
            System.out.println(values[i]);
        }
    }

    public static Test suite() {
        return new TestSuite(TestDistributions.class);
    }
}
