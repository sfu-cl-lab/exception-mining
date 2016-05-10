package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.PatternToDagSearch;

/**
 * Picks a DAG from the given graph.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Mar 11, 2007 5:55:06 PM $
 */
public class ChooseDagGraphWrapper extends GraphWrapper{
    static final long serialVersionUID = 23L;


            
    public ChooseDagGraphWrapper(GraphSource source){
        this(source.getGraph());
    }


    public ChooseDagGraphWrapper(Graph graph){
        super(pickDagFromPattern(graph));
    }


    public static ChooseDagGraphWrapper serializableInstance(){
        return new ChooseDagGraphWrapper(EdgeListGraph.serializableInstance());
    }


    //======================== Private Methods ================================//


    private static Graph pickDagFromPattern(Graph graph){
        Graph newGraph = new EdgeListGraph(graph);

        for(Edge edge : newGraph.getEdges()){
            if(Edges.isBidirectedEdge(edge)){
                newGraph.removeEdge(edge);
            }
        }
        PatternToDagSearch search = new PatternToDagSearch(new Pattern(newGraph));
        return search.patternToDagMeekRules();
    }





}
