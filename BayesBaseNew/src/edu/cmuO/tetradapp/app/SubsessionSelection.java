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

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.GraphNode;
import edu.cmu.tetradapp.model.EditorUtils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.List;

/**
 * Holds a list of session nodes for cut/paste operations. Note that a deep
 * clone of the session elements list is made on creation, and once the data is
 * retrieved, it is deleted.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 4524 $ $Date: 2006-01-06 15:57:49 -0500 (Fri, 06 Jan
 *          2006) $
 */
final class SubsessionSelection implements Transferable {

    /**
     * The list of session nodes that constitutes the selection.
     */
    private List sessionElements;

    /**
     * Supported dataflavors--only one.
     */
    private final DataFlavor[] dataFlavors = new DataFlavor[]{
            new DataFlavor(SubsessionSelection.class, "Subsession Selection")};

    /**
     * Returns the number of pastes made of this object so far.
     */
    private int numPastes = 0;

    /**
     * Constructs a new selection with the given list of session nodes.
     */
    public SubsessionSelection(List sessionElements) {
        if (sessionElements == null) {
            throw new NullPointerException(
                    "List of session elements must " + "not be null.");
        }

        for (Object sessionElement : sessionElements) {
            if (!(sessionElement instanceof GraphNode ||
                    sessionElement instanceof Edge)) {
                throw new IllegalArgumentException("Model node list contains " +
                        "an object that is not a GraphNode or an Edge: " +
                        sessionElement);
            }
        }

        try {
            this.sessionElements =
                    (List) new MarshalledObject(sessionElements).get();
        }
        catch (Exception e1) {
            e1.printStackTrace();
            throw new IllegalStateException("Could not clone.");
        }
    }

    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the
     * flavor.
     *
     * @param flavor the requested flavor for the data
     * @throws IOException                if the data is no longer available in
     *                                    the requested flavor.
     * @throws UnsupportedFlavorException if the requested data flavor is not
     *                                    supported.
     * @see DataFlavor#getRepresentationClass
     */
    @Override
	public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        try {
            List returnList =
                    (List) new MarshalledObject(sessionElements).get();
            Point point = EditorUtils.getTopLeftPoint(returnList);
            point.translate(50, 50);
//            List returnList = this.sessionElements;
//            this.sessionElements = null;
            numPastes++;
            return returnList;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether or not the specified data flavor is supported for this
     * object.
     *
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    @Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(getTransferDataFlavors()[0]);
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least
     * descriptive).
     *
     * @return an array of data flavors in which this data can be transferred
     */
    @Override
	public DataFlavor[] getTransferDataFlavors() {
        return this.dataFlavors;
    }

    public int getNumPastes() {
        return numPastes;
    }
}


