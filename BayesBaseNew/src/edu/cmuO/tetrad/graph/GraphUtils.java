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

package edu.cmu.tetrad.graph;

import edu.cmu.tetrad.util.PersistentRandomUtil;

import java.text.NumberFormat;
import java.util.*;

/**
 * Basic graph utilities.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6574 $ $Date: 2006-01-11 13:36:55 -0500 (Wed, 11 Jan
 *          2006) $
 */
public final class GraphUtils {

    /**
     * Arranges the nodes in the graph in a circle.
     *
     * @param centerx
     * @param centery
     * @param radius  The radius of the circle in pixels; a good default is
     *                150.
     */
    public static void arrangeInCircle(Graph graph, int centerx, int centery,
                                       int radius) {
        List<Node> nodes = graph.getNodes();

        Collections.sort(nodes, new Comparator<Node>() {
            @Override
			public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        double rad = 6.28 / nodes.size();
        double phi = .75 * 6.28;    // start from 12 o'clock.

        for (Object node1 : nodes) {
            Node node = (Node) node1;
            int centerX = centerx + (int) (radius * Math.cos(phi));
            int centerY = centery + (int) (radius * Math.sin(phi));

            node.setCenterX(centerX);
            node.setCenterY(centerY);

            phi += rad;
        }
    }

    public static void arrangeByGraphTiers(Graph graph) {
        List<List<Node>> tiers = getTiers(graph);

        int y = 0;

        for (List<Node> tier1 : tiers) {
            y += 50;
            int x = 0;

            for (Object aTier : tier1) {
                x += 90;
                Node node = (Node) aTier;
                node.setCenterX(x);
                node.setCenterY(y);
            }
        }
    }

    public static void hierarchicalLayout(Graph graph) {
        LayeredDrawing layout = new LayeredDrawing(graph);
        layout.doLayout();
    }

    public static void kamadaKawaiLayout(Graph graph,
                                         boolean randomlyInitialized, double naturalEdgeLength,
                                         double springConstant, double stopEnergy) {
        KamadaKawaiLayout layout = new KamadaKawaiLayout(graph);
        layout.setRandomlyInitialized(randomlyInitialized);
        layout.setNaturalEdgeLength(naturalEdgeLength);
        layout.setSpringConstant(springConstant);
        layout.setStopEnergy(stopEnergy);
        layout.doLayout();
    }

    public static void fruchtermanReingoldLayout(Graph graph) {
        FruchtermanReingoldLayout layout = new FruchtermanReingoldLayout(graph);
        layout.doLayout();
    }

    /**
     * Finds the set of nodes which have no children, followed by the set of
     * their parents, then the set of the parents' parents, and so on.  The
     * result is returned as a List of Lists.
     *
     * @return the tiers of this digraph.
     */
    public static List<List<Node>> getTiers(Graph graph) {
        Set<Node> found = new HashSet<Node>();
        Set<Node> notFound = new HashSet<Node>();
        List<List<Node>> tiers = new LinkedList<List<Node>>();

        // first copy all the nodes into 'notFound'.
        notFound.addAll(graph.getNodes());

        // repeatedly run through the nodes left in 'notFound'.  If any node
        // has all of its parents already in 'found', then add it to the
        // current tier.
        int notFoundSize = 0;
        boolean jumpstart = false;

        while (!notFound.isEmpty()) {
            List<Node> thisTier = new LinkedList<Node>();

            for (Object aNotFound : notFound) {
                Node node = (Node) aNotFound;
                List<Node> adj = graph.getAdjacentNodes(node);
                List<Node> parents = new LinkedList<Node>();

                for (Object anAdj : adj) {
                    Node _node = (Node) anAdj;
                    Edge edge = graph.getEdge(node, _node);

                    //                    if (Edges.isDirectedEdge(edge) &&
                    //                            Edges.getDirectedEdgeHead(edge) == node) {
                    //                        parents.add(_node);
                    //                    }

                    if (edge.getProximalEndpoint(node) == Endpoint.ARROW &&
                            edge.getDistalEndpoint(node) == Endpoint.TAIL) {
                        parents.add(_node);
                    }
                }

                if (found.containsAll(parents)) {
                    thisTier.add(node);
                } else if (jumpstart) {
                    for (Object parent : parents) {
                        Node _node = (Node) parent;
                        if (!found.contains(_node)) {
                            thisTier.add(_node);
                        }
                    }

                    if (!found.contains(node)) {
                        thisTier.add(node);
                    }

                    jumpstart = false;
                }
            }

            // shift all the nodes in this tier from 'notFound' to 'found'.
            notFound.removeAll(thisTier);
            found.addAll(thisTier);
            if (notFoundSize == notFound.size()) {
                jumpstart = true;
            }

            notFoundSize = notFound.size();

            // add the current tier to the list of tiers.
            if (!thisTier.isEmpty()) {
                tiers.add(thisTier);
            }
        }

        return tiers;
    }


    /**
     * Arranges the nodes in the graph in a circle, organizing by cluster
     */
    public static void arrangeClustersInCircle(Graph graph) {
        List<Node> latents = new LinkedList<Node>();
        List<List<Node>> partition = new LinkedList<List<Node>>();
        int totalSize = getMeasurementModel(graph, latents, partition);
        boolean gaps[] = new boolean[totalSize];
        List<Node> nodes = new LinkedList<Node>();
        int count = 0;
        for (int i = latents.size() - 1; i >= 0; i--) {
            nodes.add(latents.get(i));
            gaps[count++] = (i == 0);
        }

        for (Object aPartition : partition) {
            List<Node> cluster = (List<Node>) aPartition;
            for (int i = 0; i < cluster.size(); i++) {
                nodes.add(cluster.get(i));
                gaps[count++] = (i == cluster.size() - 1);
            }
        }

        double rad = 6.28 / (nodes.size() + partition.size() + 1);
        double phi = .75 * 6.28;    // start from 12 o'clock.

        for (int i = 0; i < nodes.size(); i++) {
            Node n1 = nodes.get(i);
            int centerX = 200 + (int) (150 * Math.cos(phi));
            int centerY = 200 + (int) (150 * Math.sin(phi));

            n1.setCenterX(centerX);
            n1.setCenterY(centerY);

            if (gaps[i]) {
                phi += 2 * rad;
            } else {
                phi += rad;
            }
        }
    }

    /**
     * Arranges the nodes in the graph in a line, organizing by cluster
     */
    private static final int NODE_GAP = 50;

    public static void arrangeClustersInLine(Graph graph, boolean jitter) {
        List<Node> latents = new LinkedList<Node>();
        List<List<Node>> partition = new LinkedList<List<Node>>();
        getMeasurementModel(graph, latents, partition);
        List<Node> nodes = new LinkedList<Node>();
        double clusterWidth[] = new double[partition.size()];
        double indicatorWidth[][] = new double[partition.size()][];
        double latentWidth[] = new double[partition.size()];

        for (int i = 0; i < latents.size(); i++) {
            nodes.add(latents.get(i));
            latentWidth[i] = 60;
        }
        for (int k = 0; k < partition.size(); k++) {
            List<Node> cluster = partition.get(k);
            clusterWidth[k] = 0.;
            indicatorWidth[k] = new double[cluster.size()];
            for (int i = 0; i < cluster.size(); i++) {
                nodes.add(cluster.get(i));
                indicatorWidth[k][i] = 60;
                clusterWidth[k] += 60;
            }
            clusterWidth[k] += (cluster.size() - 1.) * NODE_GAP;
        }

        int currentPos = NODE_GAP;
        for (int k = 0; k < partition.size(); k++) {
            Node nl = latents.get(k);
            nl.setCenterX(currentPos + (int) (clusterWidth[k] / 2.));
            int noise = 0;
            if (jitter) {
                noise = PersistentRandomUtil.getInstance().nextInt(50) - 25;
            }
            nl.setCenterY(100 + noise);
            List<Node> cluster = partition.get(k);
            for (int i = 0; i < cluster.size(); i++) {
                Node ni = cluster.get(i);
                int centerX = currentPos + (int) (indicatorWidth[k][i] / 2.);
                ni.setCenterX(centerX);
                ni.setCenterY(200);
                currentPos += indicatorWidth[k][i] + NODE_GAP;
            }
            currentPos += 2. * NODE_GAP;
        }
    }

    /**
     * Decompose a latent variable graph into its measurement model
     */
    public static int getMeasurementModel(Graph graph, List<Node> latents,
                                          List<List<Node>> partition) {
        int totalSize = 0;

        for (Object o : graph.getNodes()) {
            Node node = (Node) o;
            if (node.getNodeType() == NodeType.LATENT) {
                Collection<Node> children = graph.getChildren(node);
                List<Node> newCluster = new LinkedList<Node>();

                for (Object aChildren : children) {
                    Node child = (Node) aChildren;
                    if (child.getNodeType() == NodeType.MEASURED) {
                        newCluster.add(child);
                    }
                }

                latents.add(node);
                partition.add(newCluster);
                totalSize += 1 + newCluster.size();
            }
        }
        return totalSize;
    }

    /**
     * Implements the method in Melancon and Dutour, "Random Generation of
     * Directed Graphs," with optional biases added.
     */
    public static Dag createRandomDag(int numNodes, int numLatentNodes,
                                      int maxNumEdges, int maxDegree,
                                      int maxIndegree, int maxOutdegree,
                                      boolean connected) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (maxNumEdges < 0 || maxNumEdges > numNodes * (numNodes - 1)) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    maxNumEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        UniformGraphGenerator generator;

        if (connected) {
            generator = new UniformGraphGenerator(
                    UniformGraphGenerator.CONNECTED_DAG);
        } else {
            generator =
                    new UniformGraphGenerator(UniformGraphGenerator.ANY_DAG);
        }

        generator.setNumNodes(numNodes);
        generator.setMaxEdges(maxNumEdges);
        generator.setMaxDegree(maxDegree);
        generator.setMaxInDegree(maxIndegree);
        generator.setMaxOutDegree(maxOutdegree);
        generator.generate();
        Dag dag = generator.getDag();

        // Create a list of nodes. Add the nodes in the list to the
        // dag. Arrange the nodes in a circle.
        List<Node> nodes = dag.getNodes();
        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = PersistentRandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        return dag;
    }

    /**
     * Creates a random DAG by selecting a random edge x-->y from a node earlier
     * in the list of nodes to a node later in the list of nodes at each state,
     * where all such nodes are weighted equally. This is biased toward
     * divergence for nodes near the beginning of the list and divergence for
     * nodes toward the end of the list.
     */
    public static Dag createRandomDagC(int numNodes, int numLatentNodes,
                                       int numEdges) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (numEdges < 0 || numEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    numEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        Dag graph = new Dag();

        // Create a list of nodes. Add the nodes in the list to the
        // graph. Arrange the nodes in a circle.
        List<Node> nodes = new ArrayList<Node>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        for (int i = 1; i <= numNodes; i++) {
            Node node = new GraphNode("X" + nf.format(i));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = PersistentRandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        // Add nodes to graph.
        for (Object node3 : nodes) {
            graph.addNode((Node) node3);
        }

        GraphUtils.arrangeInCircle(graph, 200, 200, 150);

        // Iterate through all pairs of nodes and add a directed
        // edge between a pair if a randomly chosen number in [0,
        // 1] is < probability 'probIncludeEdge'. Flip a coin to
        // determine the direction of the arrow.
        int numPossibleEdges = numNodes * numNodes;
        int edgeCount = 0;

        while (edgeCount < numEdges) {
            int edgeIndex = PersistentRandomUtil.getInstance().nextInt(
                    numPossibleEdges);
            int first = edgeIndex / numNodes;
            int second = edgeIndex % numNodes;

            if (first == second) {
                continue;
            }

            Node node1 = nodes.get(first);
            Node node2 = nodes.get(second);

            // Add from lower index node to higher index node to guarantee
            // acyclicity.
            if (!(graph.getEdge(node1, node2) != null)) {
                if (first < second) {
                    graph.addDirectedEdge(node1, node2);
                } else {
                    graph.addDirectedEdge(node2, node1);
                }

                edgeCount++;
            }
        }

        return graph;
    }

    /**
     * Creates a random DAG by choosing each edge with uniform probability from
     * available edges at each stage and adding it. This is biased in the
     * direction of slighly longer path lengths with respect to an unbiased
     * method, with the bias increasing as the number of nodes increases, but it
     * is fine for relatively low numbers of edges.
     */
    public static Dag createRandomDagB(int numNodes, int numLatentNodes,
                                       int numEdges, double convergenceBias, double divergenceBias,
                                       double chainingBias) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (numEdges < 0 || numEdges > numNodes * (numNodes - 1) / 2) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    numEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        Dag graph = new Dag();
        Dag dpathGraph = new Dag();

        // Create a list of nodes. Add the nodes in the list to the
        // graph. Arrange the nodes in a circle.
        List<Node> nodes = new ArrayList<Node>();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        for (int i = 1; i <= numNodes; i++) {
            Node node = new GraphNode("X" + nf.format(i));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = PersistentRandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        for (Object node3 : nodes) {
            Node node = (Node) node3;
            graph.addNode(node);
            dpathGraph.addNode(node);
        }

        GraphUtils.arrangeInCircle(graph, 200, 200, 150);

        while (graph.getNumEdges() < numEdges) {
            double[] fromWeights = new double[numNodes];

            for (int j = 0; j < numNodes; j++) {
                Node from = nodes.get(j);

                for (int k = 0; k < numNodes; k++) {
                    if (j == k) {
                        continue;
                    }

                    Node to = nodes.get(k);

                    if (graph.isParentOf(from, to)) {
                        continue;
                    }

                    if (dpathGraph.isParentOf(to, from)) {
                        continue;
                    }

                    fromWeights[j] += 1.0;
                }

                int indegree = graph.getIndegree(from);
                int outdegree = graph.getOutdegree(from);

                if (outdegree > 0) {
                    fromWeights[j] *= multiplier(divergenceBias, numNodes);

                    if (fromWeights[j] == 0.0) {
                        fromWeights[j] = 1.e-10;
                    }
                }

                if (indegree == 1 && outdegree == 0) {
                    fromWeights[j] *= multiplier(chainingBias, numNodes);

                    if (fromWeights[j] == 0.0) {
                        fromWeights[j] = 1.e-10;
                    }
                }
            }

            int fromIndex = getIndex(fromWeights);

            // Make array of weights for to nodes.
            double[] toWeights = new double[numNodes];
            boolean foundPositiveWeight = false;

            for (int j = 0; j < numNodes; j++) {
                if (j == fromIndex) {
                    continue;
                }

                Node from = nodes.get(fromIndex);
                Node to = nodes.get(j);

                if (graph.isParentOf(from, to)) {
                    continue;
                }

                if (graph.isAncestorOf(to, from)) {
                    continue;
                }

                toWeights[j] = 1.0;

                int indegree = graph.getIndegree(to);
                int outdegree = graph.getOutdegree(to);

                if (indegree > 0) {
                    toWeights[j] *= multiplier(convergenceBias, numNodes);

                    if (toWeights[j] == 0.0) {
                        toWeights[j] = 1.e-10;
                    }
                }

                if (indegree == 0 && outdegree == 1) {
                    toWeights[j] *= multiplier(chainingBias, numNodes);

                    if (toWeights[j] == 0.0) {
                        toWeights[j] = 1.e-10;
                    }
                }

                foundPositiveWeight = true;
            }

            if (!foundPositiveWeight) {
                continue;
            }

            int toIndex = getIndex(toWeights);

            Node node1 = nodes.get(fromIndex);
            Node node2 = nodes.get(toIndex);

            if (dpathGraph.isParentOf(node2, node1)) {
                throw new IllegalStateException();
            }

            graph.addDirectedEdge(node1, node2);

            if (!dpathGraph.isParentOf(node1, node2)) {
                dpathGraph.addDirectedEdge(node1, node2);
            }

            List<Node> parents = dpathGraph.getParents(node1);

            for (Object parent1 : parents) {
                Node parent = (Node) parent1;
                if (!dpathGraph.isParentOf(parent, node2)) {
                    dpathGraph.addDirectedEdge(parent, node2);
                }
            }
        }

        return graph;
    }

    /**
     * Implements the method in Melancon and Dutour, "Random Generation of
     * Directed Graphs," with optional biases added.
     */
    public static Dag createRandomDagE(int numNodes, int numLatentNodes,
                                       int minNumEdges, int maxNumEdges) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException(
                    "NumNodes most be > 0: " + numNodes);
        }

        if (maxNumEdges < 0 || maxNumEdges > numNodes * (numNodes - 1)) {
            throw new IllegalArgumentException("NumEdges must be " +
                    "greater than 0 and <= (#nodes)(#nodes - 1) / 2: " +
                    maxNumEdges);
        }

        if (numLatentNodes < 0 || numLatentNodes > numNodes) {
            throw new IllegalArgumentException("NumLatents must be " +
                    "greater than 0 and less than the number of nodes: " +
                    numLatentNodes);
        }

        UniformGraphGenerator2 generator =
                new UniformGraphGenerator2(UniformGraphGenerator.ANY_DAG);

        generator.setNumNodes(numNodes);
        generator.setMinEdges(minNumEdges);
        generator.setMaxEdges(maxNumEdges);
        generator.generate();
        Dag dag = generator.getDag();

        // Create a list of nodes. Add the nodes in the list to the
        // dag. Arrange the nodes in a circle.
        List<Node> nodes = dag.getNodes();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        int numDigits = (int) Math.ceil(Math.log(numNodes) / Math.log(10.0));
        nf.setMinimumIntegerDigits(numDigits);

        for (int i = 0; i < numNodes; i++) {
            Node node = nodes.get(i);
            node.setName("X" + nf.format(i + 1));
            nodes.add(node);
        }

        int numNodesMadeLatent = 0;

        while (numNodesMadeLatent < numLatentNodes) {
            int index = PersistentRandomUtil.getInstance().nextInt(numNodes);
            Node node = nodes.get(index);
            if (node.getNodeType() == NodeType.LATENT) {
                continue;
            }
            node.setNodeType(NodeType.LATENT);
            numNodesMadeLatent++;
        }

        return dag;
    }

    public static Graph createRandomMim(int numStructuralNodes,
                                        int numStructuralEdges, int numMeasurementsPerLatent,
                                        int numLatentMeasuredImpureParents,
                                        int numMeasuredMeasuredImpureParents,
                                        int numMeasuredMeasuredImpureAssociations) {
        Dag dag = GraphUtils.createRandomDagB(numStructuralNodes,
                numStructuralNodes, numStructuralEdges, 0.0, 0.0, 0.0);
        Graph graph = new EdgeListGraph(dag);

        List<Node> latents = graph.getNodes();

        for (int i = 0; i < latents.size(); i++) {
            Node latent = latents.get(i);

            if (!(latent.getNodeType() == NodeType.LATENT)) {
                throw new IllegalArgumentException("Expected latent.");
            }

            latent.setName("L" + (i + 1));
        }

        int measureIndex = 0;

        for (Object latent1 : latents) {
            Node latent = (Node) latent1;

            for (int j = 0; j < numMeasurementsPerLatent; j++) {
                Node measurement = new GraphNode("X" + (++measureIndex));
                graph.addNode(measurement);
                graph.addDirectedEdge(latent, measurement);
            }
        }

        // Latent-->measured.
        int misses = 0;

        for (int i = 0; i < numLatentMeasuredImpureParents; i++) {
            if (misses > 10) {
                break;
            }

            int j = PersistentRandomUtil.getInstance().nextInt(latents.size());
            Node latent = latents.get(j);
            List<Node> nodes = graph.getNodes();
            List<Node> measures = graph.getNodesOutTo(latent, Endpoint.ARROW);
            measures.removeAll(latents);
            nodes.removeAll(latents);
            nodes.removeAll(measures);

            if (nodes.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int k = PersistentRandomUtil.getInstance().nextInt(nodes.size());
            Node measure = nodes.get(k);

            if (graph.getEdge(latent, measure) != null ||
                    graph.isAncestorOf(measure, latent)) {
                i--;
                misses++;
                continue;
            }

            graph.addDirectedEdge(latent, measure);
        }

        // Measured-->measured.
        misses = 0;

        for (int i = 0; i < numMeasuredMeasuredImpureParents; i++) {
            if (misses > 10) {
                break;
            }

            int j = PersistentRandomUtil.getInstance().nextInt(latents.size());
            Node latent = latents.get(j);
            List<Node> nodes = graph.getNodes();
            List<Node> measures = graph.getNodesOutTo(latent, Endpoint.ARROW);
            measures.removeAll(latents);

            if (measures.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int m = PersistentRandomUtil.getInstance().nextInt(measures.size());
            Node measure1 = measures.get(m);

            nodes.removeAll(latents);
            nodes.removeAll(measures);

            if (nodes.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int k = PersistentRandomUtil.getInstance().nextInt(nodes.size());
            Node measure2 = nodes.get(k);

            if (graph.getEdge(measure1, measure2) != null ||
                    graph.isAncestorOf(measure2, measure1)) {
                i--;
                misses++;
                continue;
            }

            graph.addDirectedEdge(measure1, measure2);
        }

        // Measured<->measured.
        misses = 0;

        for (int i = 0; i < numMeasuredMeasuredImpureAssociations; i++) {
            if (misses > 10) {
                break;
            }

            int j = PersistentRandomUtil.getInstance().nextInt(latents.size());
            Node latent = latents.get(j);
            List<Node> nodes = graph.getNodes();
            List<Node> measures = graph.getNodesOutTo(latent, Endpoint.ARROW);
            measures.removeAll(latents);

            if (measures.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int m = PersistentRandomUtil.getInstance().nextInt(measures.size());
            Node measure1 = measures.get(m);

            nodes.removeAll(latents);
            nodes.removeAll(measures);

            if (nodes.isEmpty()) {
                i--;
                misses++;
                continue;
            }

            int k = PersistentRandomUtil.getInstance().nextInt(nodes.size());
            Node measure2 = nodes.get(k);

            if (graph.getEdge(measure1, measure2) != null) {
                i--;
                misses++;
                continue;
            }

            graph.addBidirectedEdge(measure1, measure2);
        }

        GraphUtils.arrangeInCircle(graph, 200, 200, 150);
        GraphUtils.fruchtermanReingoldLayout(graph);
        return graph;
    }

    private static double multiplier(double bias, int numNodes) {
        if (bias > 0.0) {
            return numNodes * bias + 1.0;
        } else {
            return bias + 1.0;
        }
    }

    private static int getIndex(double[] weights) {
        double sum = 0.0;

        for (double weight : weights) {
            sum += weight;
        }

        double random = PersistentRandomUtil.getInstance().nextDouble() * sum;
        double partialSum = 0.0;

        for (int j = 0; j < weights.length; j++) {
            partialSum += weights[j];

            if (partialSum > random) {
                return j;
            }
        }

        throw new IllegalStateException();
    }

    /**
     * Arranges the nodes in the result graph according to their positions in
     * the source graph.
     *
     * @param resultGraph
     * @param sourceGraph
     * @return true if all of the nodes were arranged, false if not.
     */
    public static boolean arrangeBySourceGraph(Graph resultGraph,
                                               Graph sourceGraph) {
        if (resultGraph == null) {
            throw new IllegalArgumentException("Graph must not be null.");
        }

        if (sourceGraph == null) {
            GraphUtils.arrangeInCircle(resultGraph, 200, 200, 150);
            return true;
        }

        boolean arrangedAll = true;

        // There is a source graph. Position the nodes in the
        // result graph correspondingly.
        for (Object o : resultGraph.getNodes()) {
            Node node = (Node) o;
            String name = node.getName();
            Node sourceNode = sourceGraph.getNode(name);

            if (sourceNode == null) {
                arrangedAll = false;
                continue;
            }

            node.setCenterX(sourceNode.getCenterX());
            node.setCenterY(sourceNode.getCenterY());
        }

        return arrangedAll;
    }

    /**
     * Returns the node associated with a given error node. This should be the
     * only child of the error node, E --> N.
     */
    public static Node getAssociatedNode(Node errorNode, Graph graph) {
        if (errorNode.getNodeType() != NodeType.ERROR) {
            throw new IllegalArgumentException(
                    "Can only get an associated node " + "for an error node: " +
                            errorNode);
        }

        List<Node> children = graph.getChildren(errorNode);

        if (children.size() != 1) {
            System.out.println("children of " + errorNode + " = " + children);
            System.out.println(graph);

            throw new IllegalArgumentException(
                    "An error node should have only " +
                            "one child, which is its associated node: " +
                            errorNode);
        }

        return children.get(0);
    }

    /**
     * Returns true if <code>set</code> is a clique in <code>graph</code>. </p>
     * R. Silva, June 2004
     */

    public static boolean isClique(Set<Node> set, Graph graph) {
        List<Node> setv = new LinkedList<Node>(set);
        for (int i = 0; i < setv.size() - 1; i++) {
            for (int j = i + 1; j < setv.size(); j++) {
                if (!graph.isAdjacentTo(setv.get(i), setv.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculates the Markov blanket of a target in a DAG. This includes the
     * target, the parents of the target, the children of the target, the
     * parents of the children of the target, edges from parents to target,
     * target to children, parents of children to children, and parent to
     * parents of children. (Edges among children are implied by the inclusion
     * of edges from parents of children to children.) Edges among parents and
     * among parents of children not explicitly included above are not included.
     * (Joseph Ramsey 8/6/04)
     *
     * @param target a node in the given DAG.
     * @param dag    the DAG with respect to which a Markov blanket DAG is to to
     *               be calculated. All of the nodes and edges of the Markov
     *               Blanket DAG are in this DAG.
     */
    public static Dag markovBlanketDag(Node target, Graph dag) {
        if (dag.getNode(target.getName()) == null) {
            throw new NullPointerException("Target node not in graph: " + target);
        }

        Graph blanket = new EdgeListGraph();
        blanket.addNode(target);

        // Add parents of target.
        List<Node> parents = dag.getParents(target);
        for (Object parent1 : parents) {
            Node parent = (Node) parent1;
            blanket.addNode(parent);

            blanket.addDirectedEdge(parent, target);
        }

        // Add children of target and parents of children of target.
        List<Node> children = dag.getChildren(target);
        List<Node> parentsOfChildren = new LinkedList<Node>();
        for (Object aChildren : children) {
            Node child = (Node) aChildren;

            if (!blanket.containsNode(child)) {
                blanket.addNode(child);
            }

            blanket.addDirectedEdge(target, child);

            List<Node> parentsOfChild = dag.getParents(child);
            parentsOfChild.remove(target);
            for (Object aParentsOfChild : parentsOfChild) {
                Node parentOfChild = (Node) aParentsOfChild;

                if (!parentsOfChildren.contains(parentOfChild)) {
                    parentsOfChildren.add(parentOfChild);
                }

                if (!blanket.containsNode(parentOfChild)) {
                    blanket.addNode(parentOfChild);
                }

                blanket.addDirectedEdge(parentOfChild, child);
            }
        }

        // Add in edges connecting parents and parents of children.
        parentsOfChildren.removeAll(parents);

        for (Object parent2 : parents) {
            Node parent = (Node) parent2;

            for (Object aParentsOfChildren : parentsOfChildren) {
                Node parentOfChild = (Node) aParentsOfChildren;
                Edge edge1 = dag.getEdge(parent, parentOfChild);
                Edge edge2 = blanket.getEdge(parent, parentOfChild);

                if (edge1 != null && edge2 == null) {
                    Edge newEdge = new Edge(parent, parentOfChild,
                            edge1.getProximalEndpoint(parent),
                            edge1.getProximalEndpoint(parentOfChild));

                    blanket.addEdge(newEdge);
                }
            }
        }

        // Add in edges connecting children and parents of children.
        for (Object aChildren1 : children) {
            Node child = (Node) aChildren1;

            for (Object aParentsOfChildren : parentsOfChildren) {
                Node parentOfChild = (Node) aParentsOfChildren;
                Edge edge1 = dag.getEdge(child, parentOfChild);
                Edge edge2 = blanket.getEdge(child, parentOfChild);

                if (edge1 != null && edge2 == null) {
                    Edge newEdge = new Edge(child, parentOfChild,
                            edge1.getProximalEndpoint(child),
                            edge1.getProximalEndpoint(parentOfChild));

                    blanket.addEdge(newEdge);
                }
            }
        }

        return new Dag(blanket);
    }

    /**
     * Returns the connected components of the given graph, as a list of lists
     * of nodes.
     */
    public static List<List<Node>> connectedComponents(Graph graph) {
        List<List<Node>> components = new LinkedList<List<Node>>();
        List<Node> unsortedNodes = new ArrayList<Node>(graph.getNodes());

        while (!unsortedNodes.isEmpty()) {
            Node seed = unsortedNodes.get(0);
            Set<Node> component = new HashSet<Node>();
            collectComponentVisit(seed, component, graph, unsortedNodes);
            components.add(new ArrayList<Node>(component));
        }

        return components;
    }


    /**
     * Assumes node should be in component.
     */
    private static void collectComponentVisit(Node node, Set<Node> component,
                                              Graph graph, List<Node> unsortedNodes) {
        component.add(node);
        unsortedNodes.remove(node);
        List<Node> adj = graph.getAdjacentNodes(node);

        for (Object anAdj : adj) {
            Node _node = (Node) anAdj;

            if (!component.contains(_node)) {
                collectComponentVisit(_node, component, graph, unsortedNodes);
            }
        }
    }

    /**
     * Returns the first directed cycle encountered, or null if none is
     * encountered.
     *
     * @param graph The graph in which a directed cycle is sought.
     * @return the first directed cycle encountered in <code>graph</code>.
     */
    public static List<Node> directedCycle(Graph graph) {
        for (Node node : graph.getNodes()) {
            List<Node> path = directedPathFromTo(graph, node, node);

            if (path != null) {
                return path;
            }
        }

        return null;
    }

    /**
     * Returns the first directed path encountered from <code>node1</code>
     * to <code>node2</code>, or null if no such path is found.
     *
     * @param graph The graph in which a directed path is sought.
     * @param node1 The 'from' node.
     * @param node2 The 'to'node.
     * @return A path from <code>node1</code> to <code>node2</code>, or null
     *         if there is no path.
     */
    public static List<Node> directedPathFromTo(Graph graph, Node node1, Node node2) {
        return directedPathVisit(graph, node1, node2, new LinkedList<Node>());
    }

    /**
     * Returns the path of the first directed path found from node1 to node2,
     * if any.
     */
    private static List<Node> directedPathVisit(Graph graph, Node node1, Node node2,
                                                LinkedList<Node> path) {
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return path;
            }

            if (path.contains(child)) {
                continue;
            }

            if (directedPathVisit(graph, child, node2, path) != null) {
                return path;
            }
        }

        path.removeLast();
        return null;
    }

    //all adjancencies are directed <=> there is no uncertainty about who the parents of 'node' are.
    public static boolean allAdjacenciesAreDirected(Node node, Graph graph) {
        List<Edge> nodeEdges = graph.getEdges(node);
        for (Edge edge : nodeEdges) {
            if (!edge.isDirected())
                return false;
        }
        return true;
    }

    public static void removeBidirectedOrientations(Graph estPattern) {
        // Make bidirected edges undirected.
        for (Edge edge : estPattern.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                estPattern.removeEdge(edge);
                estPattern.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }
    }

    public static Graph undirectedGraph(Graph graph) {
        Graph graph2 = new EdgeListGraph(graph);

        for (Edge edge : graph2.getEdges()) {
            graph2.removeEdge(edge);
            graph2.addUndirectedEdge(edge.getNode1(), edge.getNode2());
        }

        return graph2;
    }

    public static List<List<Node>> directedPathsFromTo(Graph graph, Node node1, Node node2) {
        List<List<Node>> paths = new LinkedList<List<Node>>();
        directedPathsFromToVisit(graph, node1, node2, new LinkedList<Node>(), paths);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void directedPathsFromToVisit(Graph graph, Node node1, Node node2,
                                                LinkedList<Node> path, List<List<Node>> paths) {
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList<Node> _path = new LinkedList<Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            directedPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List<List<Node>> allPathsFromTo(Graph graph, Node node1, Node node2) {
        List<List<Node>> paths = new LinkedList<List<Node>>();
        allPathsFromToVisit(graph, node1, node2, new LinkedList<Node>(), paths);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void allPathsFromToVisit(Graph graph, Node node1, Node node2,
                                           LinkedList<Node> path, List<List<Node>> paths) {
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverse(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList<Node> _path = new LinkedList<Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            allPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List<List<Node>> allPathsFromToExcluding(Graph graph, Node node1, Node node2, List<Node> excludes) {
        List<List<Node>> paths = new LinkedList<List<Node>>();
        allPathsFromToExcludingVisit(graph, node1, node2, new LinkedList<Node>(), paths, excludes);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    public static void allPathsFromToExcludingVisit(Graph graph, Node node1, Node node2,
                                           LinkedList<Node> path, List<List<Node>> paths, List<Node> excludes) {
        if (excludes.contains(node1)) {
            return;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverse(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                LinkedList<Node> _path = new LinkedList<Node>(path);
                _path.add(child);
                paths.add(_path);
                continue;
            }

            if (path.contains(child)) {
                continue;
            }

            allPathsFromToVisit(graph, child, node2, path, paths);
        }

        path.removeLast();
    }

    public static List<List<Node>> treks(Graph graph, Node node1, Node node2) {
        List<List<Node>> paths = new LinkedList<List<Node>>();
        treks(graph, node1, node2, new LinkedList<Node>(), paths);
        return paths;
    }

    /**
     * Returns the path of the first directed path found from node1 to node2, if
     * any.
     */
    private static void treks(Graph graph, Node node1, Node node2,
                             LinkedList<Node> path, List<List<Node>> paths) {
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node next = Edges.traverse(node1, edge);

            if (next == null) {
                continue;
            }

            if (path.size() > 1) {
                Node node0 = path.get(path.size() - 2);

                if (next == node0) {
                    continue;
                }

                if (graph.isDefiniteCollider(node0, node1, next)) {
                    continue;
                }
            }

            if (next == node2) {
                LinkedList<Node> _path = new LinkedList<Node>(path);
                _path.add(next);
                paths.add(_path);
                continue;
            }

            if (path.contains(next)) {
                continue;
            }

            treks(graph, next, node2, path, paths);
        }

        path.removeLast();
    }

    public static List<List<Node>> dConnectingPaths(Graph graph, Node node1, Node node2,
                                  List<Node> conditioningNodes) {

        List<List<Node>> paths = new LinkedList<List<Node>>();

        Set<Node> conditioningNodesClosure = new HashSet<Node>();

        for (Object conditioningNode : conditioningNodes) {
            doParentClosureVisit(graph, (Node) (conditioningNode),
                    conditioningNodesClosure);
        }

        // Calls the recursive method to discover a d-connecting path from node1
        // to node2, if one exists.  If such a path is found, true is returned;
        // otherwise, false is returned.
        Endpoint incomingEndpoint = null;
        isDConnectedToVisit(graph, node1, incomingEndpoint, node2, new LinkedList<Node>(), paths,
                conditioningNodes, conditioningNodesClosure);

        return paths;
    }

    private static void doParentClosureVisit(Graph graph, Node node, Set<Node> closure) {
        if (!closure.contains(node)) {
            closure.add(node);

            for (Edge edge1 : graph.getEdges(node)) {
                Node sub = Edges.traverseReverseDirected(node, edge1);

                if (sub == null) {
                    continue;
                }

                doParentClosureVisit(graph, sub, closure);
            }
        }
    }

    private static void isDConnectedToVisit(Graph graph, Node currentNode,
                                        Endpoint inEdgeEndpoint, Node node2, LinkedList<Node> path, List<List<Node>> paths,
                                        List<Node> conditioningNodes, Set<Node> conditioningNodesClosure) {
//        System.out.println("Visiting " + currentNode);

        if (currentNode == node2) {
            LinkedList<Node> _path = new LinkedList<Node>(path);
            _path.add(currentNode);
            paths.add(_path);
            return;
        }

//        if (path.size() >= 2) {
//            return;
//        }

//        if (currentNode == node2) {
//            return true;
//        }

        if (path.contains(currentNode)) {
            return;
        }

        path.addLast(currentNode);

        for (Edge edge1 : graph.getEdges(currentNode)) {
            Endpoint outEdgeEndpoint = edge1.getProximalEndpoint(currentNode);

            // Apply the definition of d-connection to determine whether
            // we can pass through on a path from this incoming edge to
            // this outgoing edge through this node.  it all depends
            // on whether this path through the node is a collider or
            // not--that is, whether the incoming endpoint and the outgoing
            // endpoint are both arrow endpoints.
            boolean isCollider = (inEdgeEndpoint == Endpoint.ARROW) &&
                    (outEdgeEndpoint == Endpoint.ARROW);
            boolean passAsCollider = isCollider &&
                    conditioningNodesClosure.contains(currentNode);
            boolean passAsNonCollider =
                    !isCollider && !conditioningNodes.contains(currentNode);

            if (passAsCollider || passAsNonCollider) {
                Node nextNode = Edges.traverse(currentNode, edge1);
                //if (nextNode != null) {
                Endpoint previousEndpoint = edge1.getProximalEndpoint(nextNode);
                isDConnectedToVisit(graph, nextNode, previousEndpoint, node2,
                        path, paths, conditioningNodes, conditioningNodesClosure);
            }
        }

        path.removeLast();
    }

    /**
     * Returns the edges that are in <code>graph1</code> but not in <code>graph2</code>.
     * @param graph1
     * @param graph2
     * @return
     */
    public static List<Edge> edgesComplement(Graph graph1, Graph graph2) {
        List<Edge> edges = new ArrayList<Edge>();

        for (Edge edge1 : graph1.getEdges()) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            Node node21 = graph2.getNode(name1);
            Node node22 = graph2.getNode(name2);

            Edge edge2 = graph2.getEdge(node21, node22);

            if (edge2 == null || !edge1.equals(edge2)) {
                edges.add(edge1);
            }
        }

        return edges;
    }

    /**
     * Returns the edges that are in <code>graph1</code> but not in
     * <code>graph2</code>, as a list of undirected edges..
     */
    public static List<Edge> adjacenciesComplement(Graph graph1, Graph graph2) {
        List<Edge> edges = new ArrayList<Edge>();

        for (Edge edge1 : graph1.getEdges()) {
            String name1 = edge1.getNode1().getName();
            String name2 = edge1.getNode2().getName();

            Node node21 = graph2.getNode(name1);
            Node node22 = graph2.getNode(name2);

            if (!graph2.isAdjacentTo(node21, node22)) {
                edges.add(Edges.undirectedEdge(node21, node22));
            }
        }

        return edges;
    }

    /**
     * Returns a new graph in which the bidirectred edges of the given
     * graph have been changed to undirected edges.
     */
    public static Graph bidirectedToUndirected(Graph graph) {
        Graph newGraph = new EdgeListGraph(graph);

        for (Edge edge : newGraph.getEdges()) {
            if (Edges.isBidirectedEdge(edge)) {
                newGraph.removeEdge(edge);
                newGraph.addUndirectedEdge(edge.getNode1(), edge.getNode2());
            }
        }

        return newGraph;
    }

    public static String pathString(Graph graph, List<Node> path) {
        return pathString(graph, path, new LinkedList<Node>());
    }

    public static String pathString(Graph graph, List<Node> path, List<Node> conditioningVars) {
        StringBuffer buf = new StringBuffer();

        buf.append(path.get(0).toString());

        if (conditioningVars.contains(path.get(0))) {
            buf.append("(C)");
        }

        for (int m = 1; m < path.size(); m++) {
            Node n0 = path.get(m - 1);
            Node n1 = path.get(m);

            Edge edge = graph.getEdge(n0, n1);

            if (edge == null) {
                buf.append("(-)");
            }
            else {
                Endpoint endpoint0 = edge.getProximalEndpoint(n0);
                Endpoint endpoint1 = edge.getProximalEndpoint(n1);

                buf.append(endpoint0 == Endpoint.ARROW ? "<" : "-");
                buf.append("-");
                buf.append(endpoint1 == Endpoint.ARROW ? ">" : "-");
            }

            buf.append(n1.toString());

            if (conditioningVars.contains(n1)) {
                buf.append("(C)");
            }
        }
        return buf.toString();
    }

    public static List<Node> asList(int[] indices, List<Node> nodes) {
        List<Node> list = new LinkedList<Node>();

        for (int i : indices) {
            list.add(nodes.get(i));
        }

        return list;
    }

    /**
     * Converts the given graph, <code>originalGraph</code>, to use the new
     * variables (with the same names as the old).
     * @param originalGraph The graph to be converted.
     * @param newVariables The new variables to use, with the same names as
     * the old ones.
     * @return A new, converted, graph.
     */
    public static Graph convertNodes(Graph originalGraph, List<Node> newVariables) {
        Graph convertedGraph = new EdgeListGraph(newVariables);

        for (Edge edge : originalGraph.getEdges()) {
            Node node1 = convertedGraph.getNode(edge.getNode1().getName());
            Node node2 = convertedGraph.getNode(edge.getNode2().getName());
            Endpoint endpoint1 = edge.getEndpoint1();
            Endpoint endpoint2 = edge.getEndpoint2();
            Edge newEdge = new Edge(node1, node2, endpoint1, endpoint2);
            convertedGraph.addEdge(newEdge);
        }

        return convertedGraph;
    }

    /**
     * Converts the given list of nodes, <code>originalNodes</code>, to use the new
     * variables (with the same names as the old).
     * @param originalNodes The list of nodes to be converted.
     * @param newNodes A list of new nodes, containing as a subset nodes with
     * the same names as those in <code>originalNodes</code>.
     * the old ones.
     * @return The converted list of nodes.
     */
    public static List<Node> convertNodes(List<Node> originalNodes, List<Node> newNodes) {
        List<Node> convertedNodes = new LinkedList<Node>();

        for (Node node : originalNodes) {
            for (Node _node : newNodes) {
                if (node.getName().equals(_node.getName())) {
                    convertedNodes.add(_node);
                }
            }
        }

        return convertedNodes;
    }

    /**
     * Converts the given list of nodes, <code>originalNodes</code>, to use the
     * replacement nodes for them by the same name in the given <code>graph</code>.
     * @param originalNodes The list of nodes to be converted.
     * @param graph A graph to be used as a source of new nodes.
     * @return A new, converted, graph.
     */
    public static List<Node> convertNodes(List<Node> originalNodes, Graph graph) {
        List<Node> convertedNodes = new LinkedList<Node>();

        for (Node node : originalNodes) {
            convertedNodes.add(graph.getNode(node.getName()));
        }

        return convertedNodes;
    }
}

