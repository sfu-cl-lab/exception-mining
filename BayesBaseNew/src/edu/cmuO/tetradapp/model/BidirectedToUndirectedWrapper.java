package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.*;

/**
 * Picks a DAG from the given graph.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Mar 11, 2007 5:55:06 PM $
 */
public class BidirectedToUndirectedWrapper extends GraphWrapper{
    static final long serialVersionUID = 23L;



    public BidirectedToUndirectedWrapper(GraphSource source){
        this(source.getGraph());
    }


    public BidirectedToUndirectedWrapper(Graph graph){
        super(pickDagFromPattern(graph));
    }


    public static BidirectedToUndirectedWrapper serializableInstance(){
        return new BidirectedToUndirectedWrapper(EdgeListGraph.serializableInstance());
    }


    //======================== Private Methods ================================//


    private static Graph pickDagFromPattern(Graph graph){
        return GraphUtils.bidirectedToUndirected(graph);
    }
}