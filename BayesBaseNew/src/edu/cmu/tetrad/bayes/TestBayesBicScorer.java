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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphConverter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the BayesIm.
 *
 * @author Joseph Ramsey
 */
public final class TestBayesBicScorer extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestBayesBicScorer(String name) {
        super(name);
    }

    public static void testPValue() {
        Graph graph1 = GraphConverter.convert("X1,X2-->X3,X4,X5-->X6,X7,X8");
        Graph graph2 = GraphConverter.convert("X1,X2-->X3,X4,X5,X6,X7-->X8");

        Dag dag2 = new Dag(graph2);
        BayesPm bayesPm2 = new BayesPm(dag2);
        BayesIm bayesIm2 = new MlBayesIm(bayesPm2, MlBayesIm.RANDOM);

        int n = 1000;
        DataSet dataSet2Discrete = bayesIm2.simulateData(n, false);

        BayesProperties scorer = new BayesProperties(dataSet2Discrete, graph1);
        System.out.println("P-value = " + scorer.getLikelihoodRatioP());
    }

    /**
     * This method uses reflection to collect up all of the test methods from
     * this class and return them to the test runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestBayesBicScorer.class);
    }
}


