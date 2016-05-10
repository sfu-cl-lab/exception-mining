package edu.cmu.tetrad.cluster;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Clusters voxels by their ability to produce a average signal.
 */
public class SignalClustering implements ClusteringAlgorithm {

    private DoubleMatrix2D xyzData;
    private int[][][] brainMap;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;
    private DoubleMatrix2D timeSeries;
    private DoubleMatrix1D average;
    private int clusterMin = 2;

    public SignalClustering(DoubleMatrix2D xyzCoords) {
        this.xyzData = xyzCoords;

    }

    /**
     * The cutoff value. If the affinity of a point with cOpen is less than t |
     * cOpen |, then that point is high affinity; otherwise, it is low affinity.
     * (Note that the original algorithm has been modified to use dissimilarity
     * rather than similarity.
     */
    private double threshold = 1.0;

    /**
     * The returned clusters.
     */
    private List<List<Integer>> clusters;

    /**
     * True if verbose output should be printed to System.out.
     */
    private boolean verbose = true;


    @Override
	public void cluster(DoubleMatrix2D timeSeries) {
        this.timeSeries = timeSeries;

        // Make a brain map to make it easier to find neighbors.
        DoubleArrayList cluster0 = new DoubleArrayList(xyzData.viewColumn(0).toArray());
        DoubleArrayList cluster1 = new DoubleArrayList(xyzData.viewColumn(1).toArray());
        DoubleArrayList cluster2 = new DoubleArrayList(xyzData.viewColumn(2).toArray());

        int minX = (int) Descriptive.min(cluster0);
        int minY = (int) Descriptive.min(cluster1);
        int minZ = (int) Descriptive.min(cluster2);

        int maxX = (int) Descriptive.max(cluster0);
        int maxY = (int) Descriptive.max(cluster1);
        int maxZ = (int) Descriptive.max(cluster2);

        double mean0 = Descriptive.mean(cluster0);
        double mean1 = Descriptive.mean(cluster1);
        double mean2 = Descriptive.mean(cluster2);

        double sd0 = standardDeviation(cluster0);
        double sd1 = standardDeviation(cluster1);
        double sd2 = standardDeviation(cluster2);

        System.out.println("X = " + minX + " to " + maxX + " mean = " + mean0 + " SD = " + sd0);
        System.out.println("Y = " + minY + " to " + maxY + " mean = " + mean1 + " SD = " + sd1);
        System.out.println("Z = " + minZ + " to " + maxZ + " mean = " + mean2 + " SD = " + sd2);

        int[][][] brainMap = new int[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    brainMap[x - minX][y - minY][z - minZ] = -1;
                }
            }
        }

        for (int i = 0; i < xyzData.rows(); i++) {
            int x = (int) xyzData.get(i, 0);
            int y = (int) xyzData.get(i, 1);
            int z = (int) xyzData.get(i, 2);

            if (!(brainMap[x - minX][y - minY][z - minZ] == -1)) {
                System.out.println("Duplicate coordinates: <" + x + ", " + y + ", " + z + ">");
            }

            brainMap[x - minX][y - minY][z - minZ] = i;
        }

        this.brainMap = brainMap;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;

        // Calculate SS for all voxels. We pick the first voxel as the maximum
        // unused voxel using this score.
        double[] sum = new double[timeSeries.columns()];
        average = new DenseDoubleMatrix1D(timeSeries.columns());
        Set<Integer> setT = new LinkedHashSet<Integer>();

        for (int i = 0; i < timeSeries.rows(); i++) {
            setT.add(i);
        }

        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
;        double[] variances = new double[timeSeries.rows()];

        for (int i = 0; i < variances.length; i++) {
            variances[i] = variance(timeSeries.viewRow(i));
        }

        while (true) {
            int w = maximalVariance(setT, variances);

            if (w == -1) {
                System.out.println("Can't find a suitable starting place.");
                break;
            }

            double initialVariance = variance(timeSeries.viewRow(w));

            if (initialVariance < 10) {
                System.out.println("Can't find a starting place with variance >= 10");
                break;
            }

            Set<Integer> cOpen = new LinkedHashSet<Integer>();
            cOpen.add(w);
            setT.remove(w);
            clearSum(sum);
            addToSum(sum, timeSeries.viewRow(w));

            System.out.println();
            System.out.println("Initial variance: " + variance(timeSeries.viewRow(w)));

            while (!setT.isEmpty()) {
                Set<Integer> periphery = periphery(cOpen, setT);
                int t = -1;
                double max = 0.0;

                for (int x : periphery) {
                    addToSum(sum, this.timeSeries.viewRow(x));
                    DoubleMatrix1D average = average(sum, cOpen.size() + 1);
                    double var = variance(average);

                    if (var > max && var > threshold * initialVariance) {
                        max = var;
                        t = x;
                    }

                    removeFromSum(sum, this.timeSeries.viewRow(x));
                }

                if (t == -1) {
                    break;
                }

                addToSum(sum, timeSeries.viewRow(t));
                cOpen.add(t);
                setT.remove(t);
                double var = variance(average(sum, cOpen.size()));
                System.out.println("Adding " + t + " Variance = " + var + " >= " +
                        getThreshold() * initialVariance  + " |T| = " + setT.size());
            }

            if (cOpen.size() > getClusterMin()) {
                System.out.println("Closing: size = " + cOpen.size());
                clusters.add(new ArrayList<Integer>(cOpen));
            }
        }

        this.clusters = clusters;
    }

    private void clearSum(double[] sum) {
        for (int i = 0; i < sum.length; i++) sum[i] = 0;
    }

    private DoubleMatrix1D average(double[] cOpenSum, int size) {
        for (int i = 0; i < average.size(); i++) {
            average.set(i, cOpenSum[i] / size);
        }

        return average;
    }

    private int maximalVariance(Set<Integer> setU, double[] ss) {
        double max = 0.0;
        int u = -1;

        for (int _u : setU) {
            if (ss[_u] > max) {
                max = ss[_u];
                u = _u;
            }
        }

        return u;
    }

    private double variance(DoubleMatrix1D row) {
        double sum = 0.0;

        for (int j = 0; j < row.size(); j++) {
            double v = row.get(j);
            sum += v * v;
        }

        return sum / row.size();
    }

    private double standardDeviation(DoubleArrayList cluster) {
        double sumX = Descriptive.sum(cluster);
        double sumSqX = Descriptive.sumOfSquares(cluster);
        double variance = Descriptive.variance(cluster.size(), sumX, sumSqX);
        return Descriptive.standardDeviation(variance);
    }

    private void addToSum(double[] cOpenSum, DoubleMatrix1D vectorToAdd) {
        for (int i = 0; i < cOpenSum.length; i++) {
            cOpenSum[i] += vectorToAdd.get(i);
        }
    }

    private void removeFromSum(double[] cOpenAverage, DoubleMatrix1D vectorToSubtract) {
        for (int i = 0; i < cOpenAverage.length; i++) {
            cOpenAverage[i] -= vectorToSubtract.get(i);
        }
    }

    @Override
	public List<List<Integer>> getClusters() {
        return clusters;
    }

    @Override
	public DoubleMatrix2D getPrototypes() {
        throw new UnsupportedOperationException();
    }

    @Override
	public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private Set<Integer> periphery(Set<Integer> cOpen, Set<Integer> setT) {
        Set<Integer> periphery = new LinkedHashSet<Integer>();

        for (int i : cOpen) {
            int x = (int) xyzData.get(i, 0);
            int y = (int) xyzData.get(i, 1);
            int z = (int) xyzData.get(i, 2);

            for (int _x = x - 1; _x <= x + 1; _x++) {
                for (int _y = y - 1; _y <= y + 1; _y++) {
                    for (int _z = z - 1; _z <= z + 1; _z++) {
                        if (_x < minX || _x > maxX) continue;
                        if (_y < minY || _y > maxY) continue;
                        if (_z < minZ || _z > maxZ) continue;

                        if (_x == x && _y == y && _z == z) {
                            continue;
                        }

                        int i2 = brainMap[_x - minX][_y - minY][_z - minZ];
                        if (i2 == -1) continue;

                        if (!cOpen.contains(i2)) periphery.add(i2);
                    }
                }
            }
        }

        periphery.retainAll(setT);
        return periphery;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getClusterMin() {
        return clusterMin;
    }

    public void setClusterMin(int clusterMin) {
        this.clusterMin = clusterMin;
    }
}
