package edu.cmu.tetrad.search;

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores a map from (variable, parents) to score.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-20 15:49:26 -0500 (Fri, 20 Jan
 *          2006) $
 */
public class LocalScoreCache {
    private MultiKeyMap map;

    public LocalScoreCache() {
        map = new MultiKeyMap();
    }

    public void add(int variable, int[] parents, double score) {
        Set<Integer> _parents = new HashSet<Integer>(parents.length);

        for (int parent : parents) {
            _parents.add(parent);
        }

        map.put(variable, _parents, score);
    }

    public double get(int variable, int[] parents) {
        Set<Integer> _parents = new HashSet<Integer>(parents.length);

        for (int parent : parents) {
            _parents.add(parent);
        }

        Double _score = (Double) map.get(variable, _parents);
        return _score == null ? Double.NaN : (_score);
    }

    public void clear() {
        map.clear();
    }
}
