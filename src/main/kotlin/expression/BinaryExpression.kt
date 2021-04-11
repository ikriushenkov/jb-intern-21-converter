package expression

import Operation

abstract class BinaryExpression(
    protected var firstExpression: Expression,
    protected var secondExpression: Expression,
    protected var operation: Operation<*, *>
) : Expression {
    override fun putInElement(expression: Expression): Expression {
        return also {
            it.firstExpression = firstExpression.putInElement(expression)
            it.secondExpression = secondExpression.putInElement(expression)
        }
    }

    override fun toString(): String {
        return "(${firstExpression}${operation}${secondExpression})"
    }
}