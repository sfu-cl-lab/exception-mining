package edu.cmu.tetradapp.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utils, for layouts.
 *
 * @author Tyler
 * @version $Revision: 1.1 $ $Date: Jan 7, 2007 3:54:02 AM $
 */
public class LayoutUtils {


    public static void setPreferredAsSize(Component comp){
        Dimension pref = comp.getPreferredSize();
        comp.setMaximumSize(pref);
        comp.setMinimumSize(pref);
    }


    public static void setAllSizes(Component comp, Dimension dim){
        comp.setPreferredSize(dim);
        comp.setMaximumSize(dim);
        comp.setMinimumSize(dim);
        comp.setSize(dim);
    }


    public static Box leftAlignJLabel(JLabel label){
        Box box = Box.createHorizontalBox();
        box.add(label);
        box.add(Box.createHorizontalGlue());
        return box;
    }


}
