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

package edu.cmu.tetrad.data;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradSerializable;

import java.text.NumberFormat;
import java.util.*;

/**
 * A ghost implementation of a previously used, slow, ugly, buggy data
 * container. None of the methods are implemented, and the only constructor is
 * serializableInstance, which returns a ColtDataSet. There is really no point
 * to even looking at this. Stop. Turn back now. Boo.
 * <p/>
 * This is only here because we're using serialization to save sessions and
 * can't delete it for fear of breaking old saved sessions.
 *
 * @deprecated Use ColtDataSet instead.
 */
@Deprecated
public class DataSet implements TetradSerializable, RectangularDataSet {
    static final long serialVersionUID = 23L;

    /**
     * @serial
     */
    private final List<Column> columns = new ArrayList<Column>();

    /**
     * @serial
     */
    private String name;

    /**
     * @serial
     */
    private Set<Node> selection;

    /**
     * @serial DO NOT ERASE THIS! IT'S NEEDED SO SERIALIZATION WILL WORK!
     */
    private Set<Column> colSelection;

    /**
     * @serial
     */
    private Map caseMultipliers = new HashMap<Integer, Integer>();

    /**
     * @serial
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * The initial capacity for columns in the data set.
     */
    private int initCapacity = 100;

    //============================CONSTRUCTORS==========================//

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static RectangularDataSet serializableInstance() {
        return new ColtDataSet(0, new LinkedList<Node>());
    }


    @Override
	public void addVariable(Node variable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void addVariable(int index, Node variable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void changeVariable(Node from, Node to) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void clearSelection() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void ensureColumns(int columns, List<String> excludedVariableNames) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void ensureRows(int rows) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

  /*  @Override
	public int getMultiplier(int caseNumber) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
*/
    @Override
	public int getColumn(Node variable) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public DoubleMatrix2D getCorrelationMatrix() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public DoubleMatrix2D getCovarianceMatrix() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public double getDouble(int row, int column) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public DoubleMatrix2D getDoubleData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public int getInt(int row, int column) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public int getNumColumns() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public int getNumRows() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public Object getObject(int row, int col) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public int[] getSelectedIndices() {
        return new int[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public Node getVariable(int column) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public Node getVariable(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public List<String> getVariableNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public List<Node> getVariables() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    
    /**
     * A map from cases to case multipliers. If a case is not in the domain of
     * this map, its case multiplier is by default 1. This is the number of
     * repetitions of the case in the dataset. The sample size is obtained by
     * summing over these multipliers.
     *
     * @serial
     */
    private Map<Integer, Integer> multipliers = new HashMap<Integer, Integer>();
    /**
     * Returns the set of case multipliers..
     */
    private Map<Integer, Integer> getMultipliers() {
        return multipliers;
    }
    /**
     * Returns true if case multipliers are being used for this data set.
     */
    public final boolean isMulipliersCollapsed() {
        for (int i : getMultipliers().keySet()) {
            if (getMultipliers().get(i) != 1) {
                return true;
            }
        }

        return false;

//
//        return !getMultipliers().keySet().isEmpty();
    }
 
    /**
     * Returns the case multiplise for the given case (i.e. row) in the data
     * set. Is this is n > 1, the interpretation is that the data set
     * effectively contains n copies of that case.
     */
    public final int getMultiplier(int caseNumber) {
        Integer multiplierInt = getMultipliers().get(caseNumber);
        return multiplierInt == null ? 1 : multiplierInt;
    }
    
   

    @Override
	public boolean isContinuous() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public boolean isDiscrete() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public boolean isMixed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public boolean isSelected(Node variable) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void removeColumn(int index) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void removeColumn(Node variable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void removeCols(int[] selectedCols) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void removeRows(int[] selectedRows) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setMultiplier(int caseNumber, int multiplier) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setCaseId(int caseNumber, String id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public String getCaseId(int caseNumber) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setDouble(int row, int column, double value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setInt(int row, int col, int value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setObject(int row, int col, Object value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setSelected(Node variable, boolean selected) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void shiftColumnDown(int row, int col, int numRowsShifted) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public RectangularDataSet subsetColumns(List<Node> vars) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public RectangularDataSet subsetColumns(int columns[]) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public RectangularDataSet subsetRows(int rows[]) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public boolean isNewCategoriesAccomodated() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setNewCategoriesAccomodated(boolean newCategoriesAccomodated) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setNumberFormat(NumberFormat nf) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setOutputDelimiter(Character character) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void permuteRows() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public Knowledge getKnowledge() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setKnowledge(Knowledge knowledge) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void setName(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}


