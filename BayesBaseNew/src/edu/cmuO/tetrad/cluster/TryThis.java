package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix1D;
import java.util.*;

import edu.cmu.tetrad.cluster.metrics.Dissimilarity;
import edu.cmu.tetrad.cluster.metrics.SquaredErrorLoss;

/**
 * Implements divisive clustering. The measure can be modified.
 *
 * @author Joseph Ramsey
 */
public class TryThis implements ClusteringAlgorithm {
    private Dissimilarity metric = new SquaredErrorLoss();
    private double epsilon = 10;
    private List<List<Integer>> clusters;

    @Override
	public void cluster(DoubleMatrix2D data) {
        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        Map<List<Integer>, DoubleMatrix1D> prototypes
                = new IdentityHashMap<List<Integer>, DoubleMatrix1D>();

//        for (List<Integer> cluster : clusters) {
//            DoubleMatrix1D prototype = data.viewRow(0).like();
//
//            for (int i : cluster) {
//                DoubleMatrix1D rowI = data.viewRow(i);
//
//                for (int j = 0; j < prototype.size(); j++) {
//                    prototype.set(j, prototype.get(j) + rowI.get(j));
//                }
//            }
//
//            prototype.assign(Mult.div(cluster.size()));
//
//            for (int j = 0; j < prototype.size(); j++) {
//                if (prototype.get(j) > 0.5) {
//                    prototype.set(j, 1);
//                } else {
//                    prototype.set(j, 0);
//                }
//            }
//        }

        I:
        for (int i = 0; i < data.rows(); i++) {
            if (i % 1000 == 0) System.out.println(i);

            for (int k = 0; k < clusters.size(); k++) {
                DoubleMatrix1D v1 = data.viewRow(i);
                DoubleMatrix1D v2 = data.viewRow(clusters.get(k).get(0));
                double dissimilarity = getMetric().dissimilarity(v1, v2);

//                System.out.println("Dissimilarity = " + dissimilarity + " epsilon = " + getEpsilon());

                if (dissimilarity <= getEpsilon()) {
                    clusters.get(k).add(i);
                    continue I;
                }
            }

            List<Integer> cluster = new ArrayList<Integer>();
            cluster.add(i);
            clusters.add(cluster);
            prototypes.put(cluster, data.viewRow(i));
        }

        for (List<Integer> cluster : new ArrayList<List<Integer>>(clusters)) {
            if (cluster.size() < 50) {
                clusters.remove(cluster);
                prototypes.remove(cluster);
            }
        }


        this.clusters = clusters;
    }

    @Override
	public List<List<Integer>> getClusters() {
        return clusters;
    }

    @Override
	public DoubleMatrix2D getPrototypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setVerbose(boolean verbose) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Dissimilarity getMetric() {
        return metric;
    }

    public void setMetric(Dissimilarity metric) {
        this.metric = metric;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }
}
