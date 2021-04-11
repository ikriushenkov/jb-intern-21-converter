package expression

class Const(val value: Int): ArithmeticExpression {
    override fun putInElement(expression: Expression): Expression {
        return this
    }

    override fun simplify(): Expression {
        return this
    }

    override fun toString(): String {
        return value.toString()
    }
}