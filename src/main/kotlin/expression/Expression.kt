package expression

interface Expression {
    fun putInElement(expression: Expression): Expression

    fun simplify(): Expression
}