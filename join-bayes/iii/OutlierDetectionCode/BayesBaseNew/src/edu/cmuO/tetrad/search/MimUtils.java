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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Clusters;
import edu.cmu.tetrad.graph.*;

import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds some utility methods for Purify, Build Clusters, and Mimbuild.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5876 $ $Date: 2006-01-20 15:49:26 -0500 (Fri, 20 Jan
 *          2006) $
 */
public final class MimUtils {

    /**
     * Converts a disconnected multiple indicator model into a set of clusters.
     * Assumes the given graph contains a number of latents Li, i = 0,...,n-1,
     * for each of which there is a list of indicators Wj, j = 0,...,m_i-1, such
     * that , Li-->Wj. Returns a Clusters object mapping i to Wj. The name for
     * cluster i is set to Li.
     */
    public static Clusters convertToClusters(Graph clusterGraph) {
        List<String> latents = new ArrayList<String>();
        Clusters clusters = new Clusters();

        for (Node node : clusterGraph.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                latents.add(node.getName());
            }
        }

        Collections.sort(latents);

        for (int i = 0; i < latents.size(); i++) {
            String name = latents.get(i);
            clusters.setClusterName(i, name);
            Node latent = clusterGraph.getNode(name);
            List<Node> measured =
                    clusterGraph.getNodesOutTo(latent, Endpoint.ARROW);

            for (Node _node : measured) {
                if (!(_node.getNodeType() == NodeType.LATENT)) {
                    clusters.addToCluster(i, _node.getName());
                }
            }
        }

        return clusters;
    }

    /**
     * @throws Exception if the graph cannot be cloned properly due to a
     *                   serialization problem.
     */
    public static Graph extractStructureGraph(Graph clusterGraph)
            throws Exception {
        List<Edge> edges = clusterGraph.getEdges();
        Graph structureGraph = new EdgeListGraph();

        for (Edge edge : edges) {
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();

            if (node1.getNodeType() == NodeType.LATENT) {
                if (!structureGraph.containsNode(node1)) {
                    structureGraph.addNode(node1);
                }
            }

            if (node2.getNodeType() == NodeType.LATENT) {
                if (!structureGraph.containsNode(node2)) {
                    structureGraph.addNode(node2);
                }
            }

            if (node1.getNodeType() == NodeType.LATENT &&
                    node2.getNodeType() == NodeType.LATENT) {
                structureGraph.addEdge(edge);
            }
        }

        Graph clone = (Graph) new MarshalledObject(structureGraph).get();
        GraphUtils.arrangeInCircle(clone, 200, 200, 150);
        GraphUtils.fruchtermanReingoldLayout(clone);
        return clone;
    }
}


