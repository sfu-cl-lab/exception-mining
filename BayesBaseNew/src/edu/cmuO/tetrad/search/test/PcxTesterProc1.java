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

package edu.cmu.tetrad.search.test;

import edu.cmu.tetrad.data.DataParser;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.IndTestChiSquare;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.PcxSearch;

import java.io.File;
import java.io.IOException;

/**
 * @deprecated
 */
@Deprecated
public class PcxTesterProc1 {
    public static void main(String[] args) {

        try {
            //Test with discrete data.
            String filenameD = "../../../test_data/markovBlanketTestDisc.dat";
            File fileD = new File(filenameD);

            DataParser parser = new DataParser();
            RectangularDataSet dds = parser.parseTabular(fileD);

            IndependenceTest indTestD;
            PcxSearch pcxD;
            double alpha = 0.05;
            indTestD = new IndTestChiSquare(dds, alpha);

            pcxD = new PcxSearch(indTestD, 2);
            Graph mbD = pcxD.search("A6");

            System.out.println("Markov Blanket for A6 in discrete dataset");
            System.out.println(mbD);
            System.out.println("Test search completed for discrete data.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}


