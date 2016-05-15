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

package edu.cmu.tetradapp;

import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.app.TetradDesktop;
import edu.cmu.tetradapp.util.DesktopController;
import edu.cmu.tetradapp.util.ImageUtils;
import edu.cmu.tetradapp.util.SplashScreen;
import edu.cmu.tetradapp.util.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * <p>Launches Tetrad as an application.  The intended class path in either case
 * is "edu.cmu.tetradapp.Tetrad", so care should be taken not to move this class
 * out of the "INSTANCE" package. The launch itself is carried out by the method
 * "launchFrame()", which generates a new frame for the application.</p> </p>
 * <p>Note to programmers: <b>Please don't make any changes to this class.</b>
 * If you need another way of launching Tetrad for special purposes, it's easy
 * enough to create a copy of this class with a different name and modify
 * it.</p>
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6130 $
 */
public final class Tetrad implements PropertyChangeListener {

    /**
     * The launch frame.
     */
    private JFrame frame;

    /**
     * The desktop placed into the launch frame.
     */
    private TetradDesktop desktop;

    /**
     * The main application title.
     */
    private final String mainTitle =
            "Tetrad " + Version.currentViewableVersion()
                    .toString();

    //==============================CONSTRUCTORS===========================//

    public Tetrad() {
    }

    //==============================PUBLIC METHODS=========================//

    /**
     * Responds to "exitProgram" property change events by disposing of the
     * Tetrad IV frame and exiting if possible.
     *
     * @param e the property change event
     */
    @Override
	public void propertyChange(final PropertyChangeEvent e) {
        if ("exitProgram".equals(e.getPropertyName())) {
            exitApplication();
        }
    }

    /**
     * <p>Launches Tetrad as an application.  One way to launch Tetrad IV as an
     * application is the following:</p> </p>
     * <pre>java -cp jarname.jar INSTANCE.Tetrad</pre>
     * </p> <p>where "jarname.jar" is a jar containing all of the classes of
     * Tetrad IV, properly compiled, along with all of the auxiliary jar
     * contents and all of the images which Tetrad IV uses, all in their proper
     * relative directories.</p>
     *
     * @param argv command line arguments (none for now).
     */
    public static void main(final String[] argv) {
        setLookAndFeel();
        new Tetrad().launchFrame();
    }

    //===============================PRIVATE METHODS=======================//

    private static void setLookAndFeel() {
        try {
            String os = System.getProperties().getProperty("os.name");
            if (os.equals("Windows XP")) {
                // The only system look and feel that seems to work well is the
                // one for Windows XP. When running on Mac the mac look and
                // feel is forced. The new look (synth or whatever its called)
                // and feel for linux on 1.5 looks
                // pretty bad so it shouldn't be used.
                // By default linux will use the metal look and feel.
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Launches the frame. (This is left as a separate method in case we ever
     * want to launch it as an applet.)
     */
    private void launchFrame() {

        // Set up the desktop.
        this.desktop = new TetradDesktop();
        getDesktop().addPropertyChangeListener(this);
        JOptionUtils.setCenteringComp(getDesktop());
        DesktopController.setReference(getDesktop());

        // Set up the frame. Note the order in which the next few steps
        // happen. First, the frame is given a preferred size, so that if
        // someone unmaximizes it it doesn't shrivel up to the top left
        // corner. Next, the content pane is set. Next, it is packed. Finally,
        // it is maximized. For some reason, most of the details of this
        // order are important. jdramsey 12/14/02
        this.frame = new JFrame(this.mainTitle) {
            @Override
			public Dimension getPreferredSize() {
                return Toolkit.getDefaultToolkit().getScreenSize();
//                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
//                return new Dimension(size.width - 100, size.height - 100);
            }

//            public Dimension getMinimumSize() {
//                return new Dimension(400, 400);
//            }
//
            @Override
			public Dimension getMaximumSize() {
                return Toolkit.getDefaultToolkit().getScreenSize();
            }
        };

        SplashScreen.show(getFrame(), "Loading Tetrad...", 1000);

        getFrame().setContentPane(getDesktop());
        getFrame().pack();
        getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
        Image image = ImageUtils.getImage(getFrame(), "tyler16.png");
        getFrame().setIconImage(image);

        // Add an initial session editor to the desktop. Must be done
        // from here, not in the constructor of TetradDesktop.
        getDesktop().newSessionEditor();

        getFrame().setVisible(true);
        getFrame().setDefaultCloseOperation(
                WindowConstants.DO_NOTHING_ON_CLOSE);
        getFrame().addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(final WindowEvent e) {
                exitApplication();
            }
        });

        SplashScreen.hide();
    }

    /**
     * Exits the application gracefully.
     */
    private void exitApplication() {
        boolean succeeded = getDesktop().closeAllSessions();

        if (!succeeded) {
            return;
        }

        getFrame().setVisible(false);
        getFrame().dispose();

        try {
            System.exit(0);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JFrame getFrame() {
        return frame;
    }

    public TetradDesktop getDesktop() {
        return desktop;
    }
}


