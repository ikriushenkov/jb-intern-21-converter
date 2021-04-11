package call

import Operation
import expression.*

class CallChain(private val calls: List<Call>) {
    fun shorten(): CallChain {
        var maps: Expression = Element()
        val listFilters = emptyList<LogicExpression>().toMutableList()

        for (i in calls) {
            if (i is MapCall) {
                maps = i.expression.putInElement(maps)
            } else {
                listFilters += i.expression.putInElement(maps) as? LogicExpression ?: throw Exception("TYPE ERROR")
            }
        }

        val resFilterCall = if (listFilters.isNotEmpty()) {
            val first = listFilters.removeAt(0)

            listFilters.fold(first) { expression, filter ->
                LogicBinaryExpression(expression, filter, Operation.Logic.AND)
            }
        } else {
            ArithmeticToLogicBinaryExpression(
                Const(0), Const(0),
                Operation.ArithmeticToLogic.EQUALS
            )
        }

        return CallChain(listOf(FilterCall(resFilterCall), MapCall(maps)))
    }

    fun simplify(): CallChain {
        return CallChain(calls.map { it.also { it.expression = it.expression.simplify() } })
    }

    override fun toString(): String {
        return calls.joinToString("%>%")
    }
}