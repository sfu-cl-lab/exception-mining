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

package edu.cmu.tetrad.bayes;

import edu.cmu.tetrad.data.RectangularDataSet;
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
 * @version $Date: 2007-07-26 13:32:06 -0400 (Thu, 26 Jul 2007) $
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
        Graph graph3 = GraphConverter.convert("X1,X2-->X3,X4,X5-->X6,X7-->X8");
        //Graph graph3 = GraphConverter.convert("intelligence(student0)-->capability(prof0,student0),RA(prof0,student0)-->capability(prof0,student0),grade(course0,student0)-->diff(course0),registration(course0,student0)-->diff(course0),ranking(student0)-->grade(course0,student0),registration(course0,student0)-->grade(course0,student0),intelligence(student0)-->ranking(student0),popularity(prof0)-->ranking(student0),RA(prof0,student0)-->ranking(student0),capability(prof0,student0)-->rating(course0),RA(prof0,student0)-->ratin(course0),registration(course0,student0)-->rating(course0),capability(prof0,student0)-->salary(prof0,student0),popularity(prof0)-->salary(prof0,student0),rating(course0)-->salary(prof0,student0),RA(prof0,student0)-->salary(prof0,student0),registration(course0,student0)-->salary(prof0,student0),grade(course0,student0)-->sat(course0,student0),intelligence(student0)-->sat(course0,student0),registration(course0,student0)-->sat(course0,student0),popularity(prof0)-->teachingability(prof0)");

        Dag dag1 = new Dag(graph1);
        BayesPm bayesPm1 = new BayesPm(dag1);
        BayesIm bayesIm1 = new MlBayesIm(bayesPm1, MlBayesIm.RANDOM);

        Dag dag3 = new Dag(graph3);
        BayesPm bayesPm3 = new BayesPm(dag3);
        BayesIm bayesIm3 = new MlBayesIm(bayesPm3, MlBayesIm.RANDOM);

        int n = 1000;
        RectangularDataSet dataSet3Discrete = bayesIm3.simulateData(n, false);
        //RectangularDataSet dataSet1Discrete = bayesIm1.simulateData(n, false);
        BayesProperties scorer = new BayesProperties(dataSet3Discrete, graph3);
        System.out.println("scores:"+scorer.getBic());
        System.out.println("P-value = " + scorer.getPValue());
        System.out.println("getPValueChisq = " + scorer.getPValueChisq());
        
       
       
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

