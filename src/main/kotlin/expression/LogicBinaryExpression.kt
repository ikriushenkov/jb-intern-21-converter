package expression

import Operation

class LogicBinaryExpression(
    firstExpression: LogicExpression,
    secondExpression: LogicExpression,
    operation: Operation.Logic
) : BinaryExpression(firstExpression, secondExpression, operation),
    LogicExpression {
    override fun simplify(): Expression {
        return this
    }
}