package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.RectangularDataSet;

public interface CyclicDiscoveryMethod {

	String getName();	//name of the cyclic discovery method	
	GraphWithParameters run(RectangularDataSet dataSet);
}
