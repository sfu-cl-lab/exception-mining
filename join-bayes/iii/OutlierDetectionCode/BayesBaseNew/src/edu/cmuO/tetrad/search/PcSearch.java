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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements the PC ("Peter/Clark") algorithm, as specified in Chapter 6 of
 * Spirtes, Glymour, and Scheines, "Causation, Prediction, and Search," 2nd
 * edition, with a modified rule set in step D due to Chris Meek. For the
 * modified rule set, see Chris Meek (1995), "Causal inference and causal
 * explanation with background knowledge."
 *
 * @author Joseph Ramsey (this version).
 */
public class PcSearch implements GraphSearch {
	
    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * Forbidden and required edges for the search.
     */
    private Knowledge knowledge;

    /**
     * Sepset information accumulated in the search.
     */
    private SepsetMap sepset;

    /**
     * The maximum number of nodes conditioned on in the search.
     */
    private int depth = Integer.MAX_VALUE;

    /**
     * The graph that's constructed during the search.
     */
    private Graph graph;

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;


    /**
     * True if cycles are to be aggressively prevented. May be expensive
     * for large graphs (but also useful for large graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * Count of independence tests.
     */
    private int numIndependenceTests = 0;


    /**
     * The logger to use.
     */
    private TetradLogger logger = TetradLogger.getInstance();


    private Set<Triple> unshieldedColliders;
    private Set<Triple> unshieldedNoncolliders;

	private double alpha;

    //=============================CONSTRUCTORS==========================//

    public PcSearch(IndependenceTest independenceTest, Knowledge knowledge) {
        if (independenceTest == null) {
            throw new NullPointerException();
        }

        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.knowledge = knowledge;

        TetradLoggerConfig config = logger.getTetradLoggerConfigForModel(this.getClass());

        if (config != null) {
            logger.setTetradLoggerConfig(config);
        }
    }

    //input: data containing the variable names
    //output: the output of running PC search
    public static Graph runPc(RectangularDataSet data, double alpha) {

//		System.out.println("");
//		System.out.println("****************************************************");
//		System.out.println("**************** Running PC search *****************");
//		System.out.println("****************************************************");
//
        Knowledge knowledge = new Knowledge();
        IndependenceTest indT = new IndTestFisherZ(data, alpha);
        GraphSearch pcSearch = new PcPattern(indT, knowledge);
        return pcSearch.search();
    }


    PcSearch(double alpha){
        this.alpha = alpha;
    }


    //==============================PUBLIC METHODS========================//

    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }


    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    public SepsetMap getSepset() {
        return sepset;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Graph getPartialGraph() {
        return new EdgeListGraph(graph);
    }

    /**
     * Runs PC starting with a fully connected graph over all of the variables
     * in the domain of the independence test.
     */
    @Override
	public Graph search() {
        return search(independenceTest.getVariables());
    }

    @Override
	public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Runs PC on just the given variables, all of which must be in the domain of
     * the independence test.
     */
    public Graph search(List<Node> nodes) {
        this.logger.log("info", "Starting PC algorithm");
        this.logger.log("info", "Independence test = " + getIndependenceTest() + ".");

//        this.logger.log("info", "Variables " + independenceTest.getVariables());

        long startTime = System.currentTimeMillis();
        numIndependenceTests = 0;

        if (getIndependenceTest() == null) {
            throw new NullPointerException();
        }

        List allNodes = getIndependenceTest().getVariables();
        if (!allNodes.containsAll(nodes)) {
            throw new IllegalArgumentException("All of the given nodes must " +
                    "be in the domain of the independence test provided.");
        }

        graph = new EdgeListGraph(nodes);
        graph.fullyConnect(Endpoint.TAIL);

        FastAdjacencySearch fas = new FastAdjacencySearch(graph, getIndependenceTest());
        fas.setKnowledge(getKnowledge());
        fas.setDepth(getDepth());
        this.sepset = fas.search();
        this.numIndependenceTests = fas.getNumIndependenceTests();

        enumerateTriples();

        SearchGraphUtils.pcOrientbk(knowledge, graph, nodes);
        SearchGraphUtils.orientCollidersUsingSepsets(sepset, knowledge, graph);
        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);

        this.logger.log("graph", "\nReturning this graph: " + graph);

        this.elapsedTime = System.currentTimeMillis() - startTime;

        this.logger.info("Elapsed time = " + (elapsedTime) / 1000. + " s");
        this.logger.info("Finishing PC Algorithm.");
        this.logger.flush();

        return graph;
    }

    private void enumerateTriples() {
        this.unshieldedColliders = new HashSet<Triple>();
        this.unshieldedNoncolliders = new HashSet<Triple>();

        for (Node y : graph.getNodes()) {
            List<Node> adj = graph.getAdjacentNodes(y);

            if (adj.size() < 2) {
                continue;
            }

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                Node x = adj.get(choice[0]);
                Node z = adj.get(choice[1]);

//                if (graph.isAdjacentTo(x, z)) {
//                    continue;
//                }

                List<Node> nodes = sepset.get(x, z);

                // Note that checking adj(x, z) does not suffice when knowledge
                // has been specified.
                if (nodes == null) {
                    continue;
                }

                if (nodes.contains(y)) {
                    getUnshieldedNoncolliders().add(new Triple(x, y, z));
                } else {
                    getUnshieldedColliders().add(new Triple(x, y, z));
                }
            }
        }
    }

    public Set<Triple> getUnshieldedColliders() {
        return unshieldedColliders;
    }

    public Set<Triple> getUnshieldedNoncolliders() {
        return unshieldedNoncolliders;
    }

	public String getName() {
		return "PcSearch(alpha="+alpha+")";
	}
}

