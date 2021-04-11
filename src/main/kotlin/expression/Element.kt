package expression

class Element : ArithmeticExpression {
    override fun putInElement(expression: Expression): Expression {
        return expression
    }

    override fun simplify(): Expression {
        return this
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private const val name = "element"
    }
}