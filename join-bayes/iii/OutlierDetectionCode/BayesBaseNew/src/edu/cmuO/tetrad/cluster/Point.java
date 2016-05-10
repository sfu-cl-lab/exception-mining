package edu.cmu.tetrad.cluster;

import cern.colt.matrix.DoubleMatrix1D;

public class Point implements Comparable {
    DoubleMatrix1D vector;

    public Point(DoubleMatrix1D vector) {
        this.vector = vector.copy();
    }

    public double getValue(int index) {
        return vector.get(index);
    }

    public int getSize() {
        return vector.size();
    }

    @Override
	public int compareTo(Object o) {
        if (o == this) {
            return 0;
        }

        Point p = (Point) o;

        for (int i = 0; i < getSize(); i++) {
            if (getValue(i) != p.getValue(i)) {
                return (int) Math.signum(p.getValue(i) - getValue(i));
            }
        }

        return 0;
    }

    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("<");

        for (int i = 0; i < getSize(); i++) {
            buf.append(getValue(i));

            if (i < getSize() - 1) {
                buf.append(", ");
            }
        }

        buf.append(">");
        return buf.toString();
    }

    public DoubleMatrix1D getVector() {
        return this.vector;
    }
}
