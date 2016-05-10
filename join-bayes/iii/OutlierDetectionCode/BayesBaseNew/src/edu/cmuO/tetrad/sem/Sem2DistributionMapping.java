package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetrad.util.Distribution;
import edu.cmu.tetrad.util.NormalDistribution;
import edu.cmu.tetrad.util.TetradSerializable;

/**
 * A mapping allowing the value of a distribution parameter to be retrieved and
 * modified.
 *
 * @author Joseph Ramsey
 * @version $Revision$
 */
public class Sem2DistributionMapping implements Sem2Mapping, TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The distribution whose parameter is to be modified.
     */
    private Distribution distribution;

    /**
     * The index (0, 1, ...) of the parameter to be modified.
     */
    private int index;

    /**
     * The parameter itself (from the PM).
     */
    private Parameter parameter;

    /**
     * Constructs the mapping.
     * @param distribution The underlying distribution.
     * @param index The index (0, 1, ...) of the parameter.
     * @param parameter The parameter itself from the PM.
     */
    public Sem2DistributionMapping(Distribution distribution, int index,
                               Parameter parameter) {
        if (index > distribution.getNumParameters()) {
            throw new IllegalArgumentException();
        }

        this.distribution = distribution;
        this.index = index;
        this.parameter = parameter;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Sem2DistributionMapping serializableInstance() {
        return new Sem2DistributionMapping(NormalDistribution.serializableInstance(),
                0, new Parameter("P1", ParamType.DIST, new GraphNode("X1"),
                new GraphNode("X2")));
    }

    /**
     * Sets the value of the parameter.
     * @param x The value to be set.
     * @throws IllegalArgumentException if the parameter cannot be set to that
     * value.
     */
    @Override
	public void setValue(double x) {
        distribution.setParameter(index, x);
    }

    /**
     * Returns the value of the parameter.
     * @return the value of the parameter.
     */
    @Override
	public double getValue() {
        return distribution.getParameter(index);
    }

    /**
     * Returns the parameter from the PM.
     * @return the parameter from the PM.
     */
    @Override
	public Parameter getParameter() {
        return parameter;
    }
}
