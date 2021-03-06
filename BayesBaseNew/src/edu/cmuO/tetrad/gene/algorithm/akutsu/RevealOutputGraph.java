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

package edu.cmu.tetrad.gene.algorithm.akutsu;

import edu.cmu.tetrad.gene.algorithm.util.OutputGraph;

public class RevealOutputGraph implements OutputGraph {

    private int ngenes;
    private int[][] parents;
    private int[][] lags;
    String[] names;
    String graphName;

    public RevealOutputGraph(int ngenes, int[][] parents, int[][] lags,
            String[] names, String graphName) {
        this.ngenes = ngenes;
        this.parents = parents;
        this.lags = lags;
        this.names = names;
        this.graphName = graphName;
    }

    @Override
	public int getSize() {
        return ngenes;
    }

    @Override
	public int[] getParents(int index) {
        return parents[index];
    }

    @Override
	public int[] getLags(int index) {
        return lags[index];
    }

    @Override
	public String getNodeName(int index) {
        return names[index];
    }

    @Override
	public String getGraphName() {
        return graphName;
    }
}


