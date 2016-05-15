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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Mar 26, 2010 Time: 11:53:19 AM To change this template use File |
 * Settings | File Templates.
 */
public interface IImages extends GraphScorer {
    boolean isAggressivelyPreventCycles();

    void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles);

    Graph search();

    Graph search(List<Node> nodes);

    Knowledge getKnowledge();

    void setKnowledge(Knowledge knowledge);

    double scoreGraph(Graph graph);

//    void setStructurePrior(double structurePrior);
//
//    void setSamplePrior(double samplePrior);
//

    long getElapsedTime();

    void setElapsedTime(long elapsedTime);

    void addPropertyChangeListener(PropertyChangeListener l);

    double getPenaltyDiscount();

    void setPenaltyDiscount(double penaltyDiscount);

    int getMaxNumEdges();

    void setMaxNumEdges(int maxNumEdges);

    double getModelScore();

    double getScore(Graph dag);

    SortedSet<ScoredGraph> getTopGraphs();

    int getNumPatternsToStore();

    void setNumPatternsToStore(int numPatternsToStore);

    Map<Edge, Integer> getBoostrapCounts(int numBootstraps);

    String bootstrapPercentagesString(int numBootstraps);

    String gesCountsString();

    Map<Edge, Double> averageStandardizedCoefficients();

    Map<Edge, Double> averageStandardizedCoefficients(Graph graph);

    String averageStandardizedCoefficientsString();

    String averageStandardizedCoefficientsString(Graph graph);

    String logEdgeBayesFactorsString(Graph dag);

    Map<Edge, Double> logEdgeBayesFactors(Graph dag);

//    void setfCutoffP(double v);
//
//    void setUseFCutoff(boolean useFCutoff);

    void setMinJump(double minJump);
}

