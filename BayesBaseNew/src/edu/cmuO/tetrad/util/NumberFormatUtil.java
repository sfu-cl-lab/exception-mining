package edu.cmu.tetrad.util;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

/**
 * Provides an application-wide "memory" of the number format to be used.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5813 $
 */
public class NumberFormatUtil {
    private static NumberFormatUtil INSTANCE = new NumberFormatUtil();
    private NumberFormat nf = new DecimalFormat(Preferences.userRoot()
            .get("numberFormat", "0.0000"));

    private NumberFormatUtil() {
    }

    /**
     * Returns the singleton instance of this class.
     */
    public static NumberFormatUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the number format, <code>nf</code>.
     * @throws NullPointerException if nf is null.
     */
    public void setNumberFormat(NumberFormat nf) {
        if (nf == null) {
            throw new NullPointerException();
        }
                                                                  
        this.nf = nf;
    }

    /**
     * Returns the stored number format.
     */
    public NumberFormat getNumberFormat() {
        return nf;
    }
}
