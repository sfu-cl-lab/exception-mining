package edu.cmu.tetradapp.model.calculator.expression;

/**
 * Tyler didn't document this.
 *
 * @author Tyler Gibson
 */
class ExponentExpressionDescriptor extends AbstractExpressionDescriptor {


    public ExponentExpressionDescriptor() {
        super("Exponentiation", "EXP", Position.PREFIX, false, false, "number", "number");
    }

    //=========================== Public Methods =========================//

    @Override
	public Expression createExpression(Expression... expressions) throws ExpressionInitializationException {
        if (expressions.length != 2) {
            throw new ExpressionInitializationException("Exponents must have two arguments.");
        }

        return new AbstractExpression(expressions){

            @Override
			public double evaluate(Context context) {
                Expression exp1 = expressions.get(0);
                Expression exp2 = expressions.get(1);

                return StrictMath.pow(exp1.evaluate(context), exp2.evaluate(context));
            }
        };
    }


}
