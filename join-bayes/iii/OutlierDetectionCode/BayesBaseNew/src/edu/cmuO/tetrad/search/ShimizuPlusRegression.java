package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.RectangularDataSet;


/**
 * uses Shimizu to get the graph, and regression to get the coefficients
 */
public class ShimizuPlusRegression extends Shimizu2006SearchOld {

	ShimizuPlusRegression(double alpha){
		super(alpha);
	}

	@Override
	public GraphWithParameters run(RectangularDataSet dataSet) {
		//if alpha is different OR we don't have a cached Shimizu-graph
		// create a new Shimizu-graph
		if (isGraphUsed()||getAlpha()!=super.getLastAlpha()){ 
			GraphWithParameters shimizuGraph = lingamDiscovery_DAG(dataSet);
			return GraphWithParameters.regress(dataSet, shimizuGraph.getGraph());
		}
		else { //use the cached Shimizu graph
			Shimizu2006SearchOld.setIsGraphUsed(true);
			return GraphWithParameters.regress(dataSet, lastShimizuGraph.getGraph());
		}	
	}

	public GraphWithParameters run(RectangularDataSet dataSet, GraphWithParameters generatingGraph) {
		return run(dataSet);
	}
	
	@Override
	public String getName() {
		return "Shimizu(alpha="+getAlpha()+")+Regression";
	}
	
	
}
