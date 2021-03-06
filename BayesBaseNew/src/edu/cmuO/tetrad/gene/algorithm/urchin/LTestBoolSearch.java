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

package edu.cmu.tetrad.gene.algorithm.urchin;

import java.io.*;
import java.util.StringTokenizer;

/**
 * This is merely a main program used to read a binarized measurement data set
 * and to instantiate a BoolSearch and run one or more search methods of that
 * instance.
 *
 * @author Frank Wimberly
 */
public class LTestBoolSearch {
    static int ngenes = 6;
    static int ntimes = 80;

    static int[][] cases = new int[ntimes][ngenes];

    public static void main(String[] argv) {

        String fileName = argv[0];

        InputStream s;
        StringTokenizer st;

        try {
            s = new FileInputStream(fileName);
        }
        catch (IOException e) {
            System.out.println("Cannot open file " + fileName);
            return;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(s));
        for (int k = 0; k < ntimes; k++) {
            try {
                st = new StringTokenizer(in.readLine());
                for (int j = 0; j < ngenes; j++) {
                    cases[k][j] = Integer.parseInt(st.nextToken("\t"));
                }
            }
            catch (IOException e) {
                System.out.println("Read error in " + fileName);
                return;
            }
        }

        for (int k = 0; k < ntimes; k++) {
            for (int j = 0; j < ngenes; j++) {
                if (cases[k][j] == -1) {
                    cases[k][j] = 0;
                }
            }
        }

        String[] names = {"Wnt8", "Krl", "SoxB1", "Krox", "Otx", "Eve"};

        BoolSearch bs = new BoolSearch(cases, names);

        bs.bool2(3);
    }

}

