package call

import expression.LogicExpression

class FilterCall(expression: LogicExpression) : Call(expression) {
    override fun toString(): String {
        return "filter{${expression}}"
    }
}