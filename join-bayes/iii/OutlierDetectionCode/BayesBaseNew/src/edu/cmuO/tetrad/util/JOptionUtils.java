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

package edu.cmu.tetrad.util;

import javax.swing.*;
import java.awt.*;

/**
 * Stores some utility items for displaying JOptionPane messages.
 *
 * @author Joseph Ramsey
 */
public class JOptionUtils {
    private static JComponent COMPONENT = null;

    /**
     * Sets the centering component used throughout. May be null.
     */
    public static void setCenteringComp(JComponent component) {
        COMPONENT = component;
    }

    /**
     * Returns the centering component used throughout for JOptionPanes.
     */
    public static JComponent centeringComp() {
        return COMPONENT;
    }


    public static Frame getCenteringFrame(){
        for(Container c = COMPONENT; c != null; c = c.getParent()){
            if(c instanceof Frame){
                return (Frame)c;
            }
        }
        return null;
    }


}


