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

package edu.cmu.tetrad.gene.graph;

import edu.cmu.tetrad.gene.history.LaggedFactor;

/**
 * Translates display names of lagged variables (e.g. "V1:L1") into model names
 * (e.g. "V1:1") and vice-versa.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-03-07 13:55:02 -0500 (Mon, 07 Mar
 *          2005) $
 */
public final class DisplayNameHandler {

    /**
     * Converts the given lagged factor into a display string.
     */
    public static String getDisplayString(LaggedFactor laggedFactor) {
        return getDisplayString(laggedFactor.getFactor(),
                laggedFactor.getLag());
    }

    /**
     * Uses the given factor and lag information to construct a display string.
     */
    public static String getDisplayString(String factor, int lag) {
        return factor + ":L" + lag;
    }

    /**
     * Parses the given string and returns the LaggedFactor it represents.
     */
    public static LaggedFactor getLaggedFactor(String displayString) {

        String factor = extractFactor_Display(displayString);
        int lag = extractLag_Display(displayString);

        return new LaggedFactor(factor, lag);
    }

    /**
     * Parses the given string representing a lagged factor and return the part
     * that represents the factor.
     */
    public static String extractFactor_Display(String laggedFactor) {

        int colonIndex = laggedFactor.indexOf(":L");
        String factor = laggedFactor.substring(0, colonIndex);

        return factor;
    }

    /**
     * Extracts the lag from the lagged factor name string. </p> precondition
     * laggedFactor is a legal lagged factor.
     *
     * @param laggedFactor the lagged factor whose lag is wanted.
     * @return the lag of this lagged factor.
     */
    public static int extractLag_Display(String laggedFactor) {

        int colonIndex = laggedFactor.indexOf(":L");
        int lag = Integer.parseInt(
                laggedFactor.substring(colonIndex + 2, laggedFactor.length()));

        return lag;
    }
}


