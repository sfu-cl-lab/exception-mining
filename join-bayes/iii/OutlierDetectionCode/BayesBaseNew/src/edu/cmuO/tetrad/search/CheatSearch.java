package edu.cmu.tetrad.search;

import java.util.List;

import edu.cmu.tetrad.data.RectangularDataSet;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;


/**
 * Not a search at all, but rather returns the true DAG with the coefficients
 * reconstructed through regression.
 * 
 * @author Gustavo
 *
 */
public class CheatSearch implements SemLearningMethod {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "true DAG";
	}


	/**
	 * makes a PatternWithParameters made from generatingDag.
	 */
	@Override
	public GraphWithParameters run(RectangularDataSet dataSet, boolean estimateCoefficients, TestFastIca.PwpPlusGeneratingParameters pwpPlusParms) {
		GraphWithParameters estimatedTrueGraph = null;
		if (estimateCoefficients){
			try {
				SemPm semPmTrueDag = new SemPm(pwpPlusParms.generatingPwp.getGraph());
				SemEstimator estimatorTrueDag = new SemEstimator(dataSet,semPmTrueDag);
				estimatorTrueDag.estimate();
				SemIm semImTrueDag = estimatorTrueDag.getEstimatedSem();

				estimatedTrueGraph = new GraphWithParameters(semImTrueDag, pwpPlusParms.generatingPwp.getGraph());
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println("Skip this pattern");    		
			}
		}
		else{
			estimatedTrueGraph = new GraphWithParameters(pwpPlusParms.generatingPwp.getGraph());
		}
		
		return estimatedTrueGraph;
	}

	
	/**
	 * makes a PatternWithParameters made from generatingDag.
	 */
	public GraphWithParameters run(RectangularDataSet dataSet, boolean estimateCoefficients, List<Edge> edgesToEvaluateCoeffs, TestFastIca.PwpPlusGeneratingParameters standardPwpPlusParms) {
		return run(dataSet,estimateCoefficients,standardPwpPlusParms);
	}

}
