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

import edu.cmu.tetrad.data.RectangularDataSet;

/**
 * Interface implemented by classes that test tetrad constraints. For the
 * continuous case, we have a variety of tests, including a distribution-free
 * one (which may not be currently practical when the number of variables is too
 * large).
 *
 * @author Ricardo Silva
 * @version $Revision: 5956 $ 
 */

public interface TetradTest {
    public RectangularDataSet getDataSet();

    public int tetradScore(int i, int j, int k, int q);

    public boolean tetradScore3(int i, int j, int k, int q);

    public boolean tetradScore1(int i, int j, int k, int q);

    public boolean tetradHolds(int i, int j, int k, int q);

    public double tetradPValue(int i, int j, int k, int q);

    public boolean oneFactorTest(int a, int b, int c, int d);

    public boolean oneFactorTest(int a, int b, int c, int d, int e);

    public boolean oneFactorTest(int a, int b, int c, int d, int e, int f);

    public boolean twoFactorTest(int a, int b, int c, int d);

    public boolean twoFactorTest(int a, int b, int c, int d, int e);

    public boolean twoFactorTest(int a, int b, int c, int d, int e, int f);

    public double getSignificance();

    public void setSignificance(double sig);

    public String[] getVarNames();
}


