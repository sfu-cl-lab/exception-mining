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

package edu.cmu.tetradapp.app;

import javax.help.*;
import javax.help.UnsupportedOperationException;
import java.awt.*;
import java.net.URL;
import java.util.Locale;

public final class TetradHelpBroker implements HelpBroker {
    private static final TetradHelpBroker ourInstance = new TetradHelpBroker();

    /**
     * Keeps a reference to the help broker for the application.
     */
    private HelpBroker helpBroker;

    /**
     * The path to the help set.
     */
    private final String helpsetName = "javahelp/TetradHelp";


    public static TetradHelpBroker getInstance() {
        return ourInstance;
    }

    private TetradHelpBroker() {
        try {
            ClassLoader cl = TetradDesktop.class.getClassLoader();
            URL url = HelpSet.findHelpSet(cl, helpsetName);
            HelpSet helpSet = new HelpSet(cl, url);
            this.helpBroker = helpSet.createHelpBroker();
        }
        catch (Exception e) {
            this.helpBroker = null;
        }
    }

    public boolean isHelpDefined() {
        return helpBroker != null;
    }

    @Override
	public void enableHelp(Component component, String s, HelpSet helpSet) {
        helpBroker.enableHelp(component, s, helpSet);
    }

    @Override
	public void enableHelp(MenuItem menuItem, String s, HelpSet helpSet) {
        helpBroker.enableHelp(menuItem, s, helpSet);
    }

    @Override
	public void enableHelpKey(Component component, String s, HelpSet helpSet) {
        helpBroker.enableHelpKey(component, s, helpSet);
    }

    @Override
	public void enableHelpOnButton(Component component, String s,
            HelpSet helpSet) throws IllegalArgumentException {
        helpBroker.enableHelpOnButton(component, s, helpSet);
    }

    @Override
	public void enableHelpOnButton(MenuItem menuItem, String s,
            HelpSet helpSet) {
        helpBroker.enableHelpOnButton(menuItem, s, helpSet);
    }

    @Override
	public Map.ID getCurrentID() {
        return helpBroker.getCurrentID();
    }

    @Override
	public URL getCurrentURL() {
        return helpBroker.getCurrentURL();
    }

    @Override
	public String getCurrentView() {
        return helpBroker.getCurrentView();
    }

    @Override
	public Font getFont() {
        return helpBroker.getFont();
    }

    @Override
	public HelpSet getHelpSet() {
        return helpBroker.getHelpSet();
    }

    @Override
	public Locale getLocale() {
        return helpBroker.getLocale();
    }

    @Override
	public Point getLocation() throws UnsupportedOperationException {
        return helpBroker.getLocation();
    }

    @Override
	public Dimension getSize() throws UnsupportedOperationException {
        return helpBroker.getSize();
    }

    @Override
	public void initPresentation() {
        helpBroker.initPresentation();
    }

    @Override
	public boolean isDisplayed() {
        return helpBroker.isDisplayed();
    }

    @Override
	public boolean isViewDisplayed() {
        return helpBroker.isViewDisplayed();
    }

    @Override
	public void setCurrentID(String s) throws BadIDException {
        helpBroker.setCurrentID(s);
    }

    @Override
	public void setCurrentID(Map.ID id) throws InvalidHelpSetContextException {
        helpBroker.setCurrentID(id);
    }

    @Override
	public void setCurrentURL(URL url) {
        helpBroker.setCurrentURL(url);
    }

    @Override
	public void setCurrentView(String s) {
        helpBroker.setCurrentView(s);
    }

    @Override
	public void setDisplayed(boolean b) throws UnsupportedOperationException {
        helpBroker.setDisplayed(b);
    }

    @Override
	public void setFont(Font font) {
        helpBroker.setFont(font);
    }

    @Override
	public void setHelpSet(HelpSet helpSet) {
        helpBroker.setHelpSet(helpSet);
    }

    @Override
	public void setLocale(Locale locale) {
        helpBroker.setLocale(locale);
    }

    @Override
	public void setLocation(Point point) throws UnsupportedOperationException {
        helpBroker.setLocation(point);
    }

    @Override
	public void setSize(Dimension dimension)
            throws UnsupportedOperationException {
        helpBroker.setSize(dimension);
    }

    @Override
	public void setViewDisplayed(boolean b) {
        helpBroker.setViewDisplayed(b);
    }
}


