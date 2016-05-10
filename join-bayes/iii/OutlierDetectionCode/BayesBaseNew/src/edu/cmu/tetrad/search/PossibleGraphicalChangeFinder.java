///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Finds the minimal graphical changes that block a collection of d-connecting paths.
 *
 * @author Tyler Gibson
 */
class PossibleGraphicalChangeFinder {


    /**
     * The pag that the paths are relative to.
     */
    private Graph pag;


    /**
     * The separation sets.
     */
    private List<List<Node>> separations;

    /**
     * Constructs the finder given the paths that must be blocked.
     */
    public PossibleGraphicalChangeFinder(Graph pag, Collection<Collection<Node>> separations) {
        if (pag == null) {
            throw new NullPointerException("The given pag must not be null.");
        }
        if (separations == null) {
            throw new NullPointerException("The given separation sets must not be null.");
        }
        this.pag = pag;
        this.separations = new ArrayList<List<Node>>();
        for (Collection<Node> sep : separations) {
            this.separations.add(new ArrayList<Node>(sep));
        }
    }


    /**
     * Returns the minimal set of graphical changes that block the given path (relative to the pag and separations given
     * at consturction time).
     */
    public List<GraphicalChange> findGraphicalChanges(PossibleDConnectingPath path) {
        List<Node> nodes = path.getPath();

        return null;
    }

    //================================ Inner classes ========================================//


    /**
     * Represents collider orientation.
     */
    private static class ColliderOrientation implements GraphicalChange {

        /**
         * Represents the triple that should be oriented.
         */
        private Triple triple;


        public ColliderOrientation(Triple triple) {
            if (triple == null) {
                throw new NullPointerException("The given triple must not be null.");
            }
            this.triple = triple;
        }


        public void apply(Graph g) {
            Node x = triple.getX();
            Node y = triple.getY();
            Node z = triple.getZ();
            if (g.containsNode(x) && g.containsNode(y) && g.containsNode(z) &&
                    g.isAdjacentTo(x, y) && g.isAdjacentTo(y, z)) {
                g.setEndpoint(x, y, Endpoint.ARROW);
                g.setEndpoint(y, x, Endpoint.TAIL);
                g.setEndpoint(y, z, Endpoint.ARROW);
                g.setEndpoint(z, y, Endpoint.TAIL);
            }
        }
    }

    /**
     * Represents Def non collider orientation.
     */
    private static class DefNonCollider implements GraphicalChange {

        /**
         * Represents the triple that should be oriented.
         */
        private Triple triple;


        public DefNonCollider(Triple triple) {
            if (triple == null) {
                throw new NullPointerException("The given triple must not be null.");
            }
            this.triple = triple;
        }


        public void apply(Graph pag) {
//            pag should be a PAG.
            Node x = triple.getX();
            Node y = triple.getY();
            Node z = triple.getZ();
            if (pag.containsNode(x) && pag.containsNode(y) && pag.containsNode(z) &&
                    pag.isAdjacentTo(x, y) && pag.isAdjacentTo(y, z) && !pag.isUnderlineTriple(x, y, z)) {
                pag.addUnderlineTriple(x, y, z);
            }
        }
    }


    /**
     * Represents the removal of some edge.
     */
    private static class EdgeRemoval implements GraphicalChange {

        /**
         * The edge to be removed.
         */
        private Edge edge;

        public EdgeRemoval(Edge edge) {
            if (edge == null) {
                throw new NullPointerException("Edge must not be null.");
            }
            this.edge = edge;
        }

        public void apply(Graph g) {
            if (g.containsEdge(edge)) {
                g.removeEdge(edge);
            }
        }
    }


}

