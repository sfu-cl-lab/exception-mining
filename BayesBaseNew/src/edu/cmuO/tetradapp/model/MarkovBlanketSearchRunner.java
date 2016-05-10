package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.Executable;

import java.util.List;

/**
 * Represents a runner for a Markov blanket search.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Jun 18, 2007 3:54:24 AM $
 */
public interface MarkovBlanketSearchRunner extends Executable {
    static final long serialVersionUID = 23L;

    /**
     * Returns the search params.
     */
    MbSearchParams getParams();


    /**
     * Return the source for the search.
     */
    RectangularDataSet getSource();
    

    /**
     * Returns the data model for the variables in the markov blanket.
     */
    RectangularDataSet getDataModelForMarkovBlanket();


    /**
     * Returns the variables in the markov blanket.
     */
    List<Node> getMarkovBlanket();


    /**
     * Returns the name of the search.
     */
    String getSearchName();


    /**
     * Sets the search name.
     */
    void setSearchName(String n);


}
