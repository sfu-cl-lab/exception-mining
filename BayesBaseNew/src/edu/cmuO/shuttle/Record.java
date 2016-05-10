package edu.cmu.shuttle;

/**
 * Stores string data from one line of Choh Man's file.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2006-01-12 20:04:03 -0500 (Thu, 12 Jan
 *          2006) $
 */
public final class Record {
    private String time;
    private String variable;
    private String datum;

    public Record(String time, String variable, String datum) {
        this.time = time;
        this.variable = variable;
        this.datum = datum;
    }

    public final String getTime() {
        return time;
    }

    public final String getVariable() {
        return variable;
    }

    public final String getDatum() {
        return datum;
    }
}