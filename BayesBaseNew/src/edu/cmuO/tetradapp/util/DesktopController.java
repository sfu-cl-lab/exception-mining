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

/**
 * Indirect control for the desktop to avoid package cycles. The reference to
 * the desktop is set using the <code>activate</code> method, as a
 * DesktopControllable. Once set, the method calls in the DesktopControllable
 * interface are passed on to it.
 * <p/>
 * Note that all argument types are interface-tagged as well to avoid further
 * package cycles.
 * <p/>
 * Not pretty, but easier and cleaner by far than passing the reference to the
 * desktop down through all of the relevant classes in tetradapp.
 *
 * @author Joseph Ramsey
 * @version $Revision$ $Date$
 */
public class DesktopController implements DesktopControllable {
    private static DesktopControllable INSTANCE;


    public static DesktopControllable getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the reference to the desktop that will be used throughout the
     * application when needed. Done once when the Tetrad application is
     * launched.
     */
    public static void setReference(DesktopControllable component) {
        INSTANCE = component;
    }

    @Override
	public void newSessionEditor() {
        getInstance().newSessionEditor();
    }

    @Override
	public SessionEditorIndirectRef getFrontmostSessionEditor() {
        return getInstance().getFrontmostSessionEditor();
    }

    @Override
	public void exitProgram() {
        getInstance().exitProgram();
    }

    @Override
	public boolean existsSessionByName(String name) {
        return getInstance().existsSessionByName(name);
    }

    @Override
	public void addSessionEditor(SessionEditorIndirectRef editor) {
        getInstance().addSessionEditor(editor);
    }

    @Override
	public void closeEmptySessions() {
        getInstance().closeAllSessions();
    }

    @Override
	public void putMetadata(SessionWrapperIndirectRef sessionWrapper,
            TetradMetadataIndirectRef metadata) {
        getInstance().putMetadata(sessionWrapper, metadata);
    }

    @Override
	public TetradMetadataIndirectRef getTetradMetadata(
            SessionWrapperIndirectRef sessionWrapper) {
        return getInstance().getTetradMetadata(sessionWrapper);
    }

    @Override
	public void addEditorWindow(EditorWindowIndirectRef editorWindow) {
        getInstance().addEditorWindow(editorWindow);
    }
  

    @Override
	public void closeFrontmostSession() {
        getInstance().closeFrontmostSession();
    }

    @Override
	public boolean closeAllSessions() {
        return getInstance().closeAllSessions();
    }
}


