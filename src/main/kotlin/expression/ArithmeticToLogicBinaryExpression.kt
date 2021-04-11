package expression

import Operation

class ArithmeticToLogicBinaryExpression(
    firstExpression: ArithmeticExpression,
    secondExpression: ArithmeticExpression,
    operation: Operation.ArithmeticToLogic
) : BinaryExpression(firstExpression, secondExpression, operation),
    LogicExpression {
    override fun simplify(): Expression {
        return this
    }
}