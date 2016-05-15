package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract subclass for Markov Blanket searches. This should be used so that the markov blanket search
 * can also be used as input for a search box.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Jun 18, 2007 4:00:00 AM $
 */
public abstract class AbstractMBSearchRunner extends DataWrapper implements MarkovBlanketSearchRunner {
    static final long serialVersionUID = 23L;

    /**
     * Data model.
     *
     * @serial may be null.
     */
    private RectangularDataSet dataModel;


    /**
     * The variables in the markov blanket.
     *
     * @serial may be null.
     */
    private List<Node> variables;


    /**
     * The source data model.
     *
     * @serial not null.
     */
    private RectangularDataSet source;

    /**
     * The search params.
     *
     * @serial not null.
     */
    private MbSearchParams params;




    /**
     * The name of the search algorithm
     *
     * @serial may be null.
     */
    private String searchName;


    /**
     * Conctructs the abstract search runner.
     *
     * @param source - The source data the search is acting on.
     * @param params - The params for the search.
     */
    public AbstractMBSearchRunner(DataModel source, MbSearchParams params) {
        super(casteData(source));
        if (source == null) {
            throw new NullPointerException("The source data was null.");
        }
        if (params == null) {
            throw new NullPointerException("Search params were null.");
        }
        this.params = params;
        this.source = (RectangularDataSet) source;
    }


    /**
     * Returns the parameters for the search.
     */
    @Override
	public MbSearchParams getParams() {
        return this.params;
    }


    /**
     * Returns the data model for the variables in the Markov blanket or null if
     * the runner has not executed yet.
     */
    @Override
	public RectangularDataSet getDataModelForMarkovBlanket() {
        return this.dataModel;
    }


    /**
     * Returns the variables in the MB searhc.
     */
    @Override
	public List<Node> getMarkovBlanket() {
        return this.variables;
    }


    /**
     * Returns the source of the search.
     */
    @Override
	public RectangularDataSet getSource() {
        return this.source;
    }


    @Override
	public void setSearchName(String n) {
        this.searchName = n;
    }

    /**
     * Returns the search name, or "Markov Blanket Search" by default.
     */
    @Override
	public String getSearchName() {
        if (this.searchName == null) {
            return "Markov Blanket Search";
        }
        return this.searchName;
    }

    //============== Protected methods ===============================//


    /**
     * Makes sure the data is not empty.
     */
    protected void validate() {
        if (this.source.getNumColumns() == 0 || this.source.getNumRows() == 0) {
            throw new IllegalStateException("Cannot run algorithm on an empty data set.");
        }
    }


    /**
     * Sets the results of the search.
     */
    protected void setSearchResults(List<Node> nodes) {
        if (nodes == null) {
            throw new NullPointerException("nodes were null.");
        }
        this.variables = new ArrayList<Node>(nodes);
        if (nodes.isEmpty()) {
            this.dataModel = new ColtDataSet(source.getNumRows(), nodes);
        } else {
            this.dataModel = this.source.subsetColumns(nodes);
        }
        this.setDataModel(this.dataModel);
    }


    /**
     * Returns an appropriate independence test given the type of data set and values
     * in the params.
     */
    protected IndependenceTest getIndependenceTest() {
        IndTestType type = params.getIndTestType();
        if (this.source.isContinuous() || this.source.getNumColumns() == 0) {
            if (IndTestType.CORRELATION_T == type) {
                return new IndTestCramerT(this.source, params.getAlpha());
            }
            if (IndTestType.FISHER_Z == type) {
                return new IndTestFisherZ(this.source, params.getAlpha());
            }
            if (IndTestType.FISHER_ZD == type) {
                return new IndTestFisherZD(this.source, params.getAlpha());
            }
            if (IndTestType.FISHER_Z_BOOTSTRAP == type) {
                return new IndTestFisherZBootstrap(this.source, params.getAlpha(), 15, this.source.getNumRows() / 2);
            }
            if (IndTestType.LINEAR_REGRESSION == type) {
                return new IndTestLinearRegression(this.source, params.getAlpha());
            } else {
                params.setIndTestType(IndTestType.CORRELATION_T);
                return new IndTestCramerT(this.source, params.getAlpha());
            }
        }
        if (this.source.isDiscrete()) {
            if (IndTestType.G_SQUARE == type) {
                return new IndTestGSquare(this.source, params.getAlpha());
            }
            if (IndTestType.CHI_SQUARE == type) {
                return new IndTestChiSquare(this.source, params.getAlpha());
            } else {
                params.setIndTestType(IndTestType.CHI_SQUARE);
                return new IndTestChiSquare(this.source, params.getAlpha());
            }
        }

        throw new IllegalStateException("Cannot find Independence for Data source.");
    }

    //==================== Private Methods ===========================//


    private static RectangularDataSet casteData(DataModel model){
        if(model instanceof RectangularDataSet){
            return (RectangularDataSet)model;
        }
        throw new IllegalStateException("The data model must be a rectangular data set.");
    }




    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (params == null) {
            throw new NullPointerException();
        }
        if (this.source == null) {
            throw new NullPointerException();
        }
    }


}
