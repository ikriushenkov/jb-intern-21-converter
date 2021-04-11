package call

import expression.Expression

class MapCall(expression: Expression) : Call(expression) {
    override fun toString(): String {
        return "map{${expression}}"
    }
}