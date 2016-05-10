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


public class NbFunctionSV extends AbstractNbComponent {
    public NbFunctionSV(double factor, double power, NbComponent[] parents,
            int[] inhibitExcite, String name) {

        super(factor, power, parents, inhibitExcite, name);
    }

    @Override
	public void update() {
        //System.out.println("Updating " + name);
        //double sum = 0.0;
        //for(int i = 0; i < nparents; i++) {
        //  double v = parents[i].getParamValue();
        //  sum += v;
        //}
        //System.out.println("Sum = " + sum);
        //if(inhibitExcite[0] > 0)
        double v = getParents()[0].getValue();
        setValue(getFactor() * Math.pow(v, getPower()));
    }
}
