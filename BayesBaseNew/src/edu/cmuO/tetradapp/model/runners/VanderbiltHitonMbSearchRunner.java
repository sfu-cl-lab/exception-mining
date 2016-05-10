package edu.cmu.tetradapp.model.runners;

import edu.cmu.tetrad.search.VanderbiltHitonMb;
import edu.cmu.tetradapp.model.AbstractMBSearchRunner;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MbSearchParams;

/**
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Jun 22, 2007 11:54:36 PM $
 */
public class VanderbiltHitonMbSearchRunner extends AbstractMBSearchRunner {
    static final long serialVersionUID = 23L;


    public VanderbiltHitonMbSearchRunner(DataWrapper data, MbSearchParams params) {
        super(data.getSelectedDataModel(), params);
    }


    @Override
	public void execute() throws Exception {
        VanderbiltHitonMb search = new VanderbiltHitonMb(getIndependenceTest(),
                getParams().getDepth(),true);
        this.setSearchResults(search.findMb(this.getParams().getTargetName()));
        this.setSearchName(search.getAlgorithmName());
    }
}
