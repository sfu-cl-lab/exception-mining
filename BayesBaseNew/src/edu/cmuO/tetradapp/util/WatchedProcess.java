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

package edu.cmu.tetradapp.util;

import edu.cmu.tetrad.util.JOptionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Runs a process, popping up a dialog with a stop button if the time to
 * complete is too long. The process to be run should override the watch()
 * method.
 *
 * @author Joseph Ramsey
 */
public abstract class WatchedProcess {

    /**
     * The thread that the watched process dialog runs in. This thread can
     * be stopped (using wonderful yet deprecated stop( ) method, giving the
     * user control over any process that's running in it.
     */
    private Thread thread;

    /**
     * If the thread stops with an error, the error message is stored here.
     */
    private String errorMessage;

    /**
     * The number of milliseconds the thread sleeps before checking on user
     * input again.
     */
    private final long delay = 200L;

    /**
     * The dialog displayed to the user that lets them click "Stop" when they
     * want the process to stop.
     */
    private JDialog stopDialog;

    /**
     * The anstor Window in front of which the stop dialog is being displayed.
     */
    private Window owner;

    /**
     * True iff the "watch process" dialogs should display. These threads block,
     * so displaying them makes debugging difficult. On the other hand, not
     * displaying them means that users cannot stop processes, so they hate
     * that. SO...if you set this to false, make sure you set it to true before
     * you're done working!
     * <p/>
     * It must be set to true for posted versions. There's  unit test that
     * checks for that.
     */
    private static boolean SHOW_DIALOG = true;

    /**
     * Constructs a new watched process.
     * @param owner The ancestor window in front of which the stop dialog
     * is being displayed.
     */
    public WatchedProcess(Window owner) {
        if (owner == null) {
            throw new NullPointerException();
        }

        this.owner = owner;
        watchProcess();
    }

    //=============================PUBLIC METHODS========================//

    public abstract void watch();

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JDialog getStopDialog() {
        return stopDialog;
    }

    public void setStopDialog(JDialog stopDialog) {
        this.stopDialog = stopDialog;
    }

    public Window getOwner() {
        return owner;
    }

    public boolean isShowDialog() {
        return SHOW_DIALOG;
    }

    public void setShowDialog(boolean showDialog) {
        SHOW_DIALOG = showDialog;
    }

    public boolean isAlive() {
        return thread.isAlive();
    }

    //================================PRIVATE METHODS====================//

    private void watchProcess() {
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
                try {
                    watch();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    String message = e.getMessage();

                    if (e.getCause() != null) {
                        message = e.getCause().getMessage();
                    }

                    setErrorMessage(message);
                }
            }
        };

        Thread thread = new Thread(runnable);
        setThread(thread);
        thread.start();

        if (isShowDialog()) {
            Thread watcher = new Thread() {
                @Override
				public void run() {
                    try {
                        sleep(delay);
                    }
                    catch (InterruptedException e) {
                        return;
                    }

                    if (getErrorMessage() != null) {
                        JOptionPane.showMessageDialog(
                                JOptionUtils.centeringComp(),
                                "Stopped with error:\n" + getErrorMessage());
                        return;
                    }

                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setIndeterminate(true);

                    JButton stopButton = new JButton("Stop");

                    stopButton.addActionListener(new ActionListener() {
                        @Override
						public void actionPerformed(ActionEvent e) {
                            if (getThread() != null) {
                                while (getThread().isAlive()) {
                                    getThread().stop();
                                    try {
                                        sleep(500);
                                    } catch (InterruptedException e1) {
                                        JOptionPane.showMessageDialog(
                                                JOptionUtils.centeringComp(),
                                                "Could not stop thread.");
                                        return;
                                    }
                                }
//                                JOptionPane.showMessageDialog(
//                                        JOptionUtils.centeringComp(),
//                                        "Execution stopped.");
                            }
                        }
                    });

                    Box b = Box.createVerticalBox();
                    Box b1 = Box.createHorizontalBox();
                    b1.add(progressBar);
                    b1.add(stopButton);
                    b.add(b1);

//                    final JTextArea anomaliesTextArea = new JTextArea();
//                    final TextAreaOutputStream out = new TextAreaOutputStream(
//                            anomaliesTextArea);
//
//                    Box b2 = Box.createHorizontalBox();
//                    JScrollPane scroll = new JScrollPane(anomaliesTextArea);
//                    scroll.setPreferredSize(new Dimension(300, 50));
//                    b2.add(scroll);
//                    b.add(b2);

                    if (isShowDialog()) {
                        Frame ancestor = (Frame) JOptionUtils.centeringComp()
                                .getTopLevelAncestor();
                        JDialog dialog =
                                new JDialog(ancestor, "Executing...", false);
                        setStopDialog(dialog);

                        dialog.getContentPane().add(b);
                        dialog.pack();
                        dialog.setLocationRelativeTo(
                                JOptionUtils.centeringComp());

//                        LogUtils.getInstance().add(out, Level.FINER);

                        while (getThread().isAlive()) {
                            try {
                                sleep(200);
                                if (existsOtherDialog()) {
                                    dialog.setVisible(false);
                                } else {
                                    dialog.setVisible(true);
                                    dialog.toFront();
                                }

//                                anomaliesTextArea.setCaretPosition(out.getLengthWritten());
                            }
                            catch (InterruptedException e) {
                                return;
                            }
                        }

//                        LogUtils.getInstance().remove(out);
                        dialog.setVisible(false);
                        dialog.dispose();

                        if (getErrorMessage() != null) {
                            JOptionPane.showMessageDialog(
                                    JOptionUtils.centeringComp(),
                                    "Stopped with error:\n" +
                                            getErrorMessage());
                        }
                    }
                }
            };

            watcher.start();
        }
    }

    private Thread getThread() {
        return thread;
    }

    private void setThread(Thread thread) {
        this.thread = thread;
    }

    private boolean existsOtherDialog() {
        Frame ancestor = (Frame) JOptionUtils.centeringComp()
                .getTopLevelAncestor();
        Window[] ownedWindows = ancestor.getOwnedWindows();

        for (Window window : ownedWindows) {
            if (window instanceof Dialog &&
                    !(window == getStopDialog()) &&
                    !(window == getOwner())) {
                Dialog dialog = (Dialog) window;
                if (dialog.isVisible()) {
                    return true;
                }
            }
        }

        return false;
    }
}


