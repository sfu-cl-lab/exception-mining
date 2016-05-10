package edu.cmu.tetradapp.model.runners;

import edu.cmu.tetrad.search.VanderbiltIamb;
import edu.cmu.tetradapp.model.AbstractMBSearchRunner;
import edu.cmu.tetradapp.model.DataWrapper;
import edu.cmu.tetradapp.model.MbSearchParams;

/**
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Jun 22, 2007 11:54:36 PM $
 */
public class VanderbiltIambMbSearchRunner extends AbstractMBSearchRunner {
    static final long serialVersionUID = 23L;


    public VanderbiltIambMbSearchRunner(DataWrapper data, MbSearchParams params) {
        super(data.getSelectedDataModel(), params);
    }


    @Override
	public void execute() throws Exception {
        VanderbiltIamb search = new VanderbiltIamb(getIndependenceTest());
        this.setSearchResults(search.findMb(this.getParams().getTargetName()));
        this.setSearchName(search.getAlgorithmName());
    }
}
