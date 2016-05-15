package edu.cmu.tetrad.graph;

/**
 * An unordered pair of nodes.
 *
 * @author Tyler Gibson
 * @version $Revision: 1 $ $Date: Apr 22, 2007 4:59:24 AM $
 */
public class NodePair {


    /**
     * The "First" node.
     */
    private Node first;


    /**
     * The "second" node.
     */
    private Node second;


    public NodePair(Node first, Node second){
        if(first == null){
            throw new NullPointerException("First node must not be null.");
        }
        if(second == null){
            throw new NullPointerException("Second node must not be null.");
        }
        this.first = first;
        this.second = second;
    }

    //============================== Public methods =============================//

    public Node getFirst(){
        return this.first;
    }

    public Node getSecond(){
        return this.second;
    }

    @Override
	public int hashCode(){
        return this.first.hashCode() + this.second.hashCode();
    }


    @Override
	public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(!(o instanceof NodePair)){
            return false;
        }
        NodePair thatPair = (NodePair)o;
        if(this.first.equals(thatPair.first) && this.second.equals(thatPair.second)){
            return true;
        }
        return this.first.equals(thatPair.second) && this.second.equals(thatPair.first);
    }

    @Override
	public String toString(){
        return "{" + this.first + ", " + this.second + "}";
    }

}
