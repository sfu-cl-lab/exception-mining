package edu.cmu.tetradapp.model.calculator.expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract expression.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: May 8, 2007 8:44:42 PM $
 */
abstract class AbstractExpression implements Expression{

    /**
     * The sub expressionts
     */
    protected final List<Expression> expressions;


    /**
     * Constructs the abstract expression given the sub-expressions.
     */
    public AbstractExpression(Expression... expressions) {
        this.expressions = Arrays.asList(expressions);
    }


    /**
     * Returns the sub expressions.
     */
    @Override
	public List<Expression> getSubExpressions(){
        return Collections.unmodifiableList(this.expressions);
    }


    /**
     * Returns a Double using the value returned by evaluate
     */
    public Object evaluateGeneric(Context c){
        return evaluate(c);
    }

}
