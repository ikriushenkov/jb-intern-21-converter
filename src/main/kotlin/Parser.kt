import call.Call
import call.CallChain
import call.FilterCall
import call.MapCall
import exceptions.SyntaxException
import exceptions.TypeException
import expression.*

class Parser private constructor(string: String) {
    private val source = Source(string)

    private fun parseConstant(): Const {
        var number: String = if (source.curSym() == '-') {
            "-"
        } else {
            source.curSym().toString()
        }

        while (source.nextSym().isDigit()) {
            number += source.curSym()
        }

        return Const(number.toInt())
    }

    private fun checkString(string: String) {
        var res = source.curSym().toString()

        for (i in 2..string.length) {
            res += source.nextSym()
        }

        if (res != string) {
            throw SyntaxException()
        }

        source.nextSym()
    }

    private fun parseElement(): Element {
        checkString("element")

        return Element()
    }

    private fun parseBinary(): BinaryExpression {
        checkString("(")
        val firstExpression = parseExpression()
        val operation = Operation.createFromChar(source.curSym())
        source.nextSym()
        val secondExpression = parseExpression()
        checkString(")")
        return when (operation) {
            is Operation.Logic -> {
                if (firstExpression is LogicExpression && secondExpression is LogicExpression) {
                    LogicBinaryExpression(firstExpression, secondExpression, operation)
                } else {
                    throw TypeException()
                }
            }
            is Operation.Arithmetic -> {
                if (firstExpression is ArithmeticExpression && secondExpression is ArithmeticExpression) {
                    ArithmeticBinaryExpression(firstExpression, secondExpression, operation)
                } else {
                    throw TypeException()
                }
            }
            is Operation.ArithmeticToLogic -> {
                if (firstExpression is ArithmeticExpression && secondExpression is ArithmeticExpression) {
                    ArithmeticToLogicBinaryExpression(firstExpression, secondExpression, operation)
                } else {
                    throw TypeException()
                }
            }
            else -> throw SyntaxException()
        }
    }

    private fun parseExpression(): Expression {
        val char = source.curSym()
        return if (char.isDigit() || char == '-') {
            parseConstant()
        } else {
            if (char == '(') {
                parseBinary()
            } else {
                parseElement()
            }
        }
    }

    private fun parseFilterCall(): FilterCall {
        checkString("filter{")

        val expression = parseExpression()

        checkString("}")

        return FilterCall(
            expression as? LogicExpression ?: throw TypeException()
        )
    }

    private fun parseMapCall(): MapCall {
        checkString("map{")

        val expression = parseExpression()

        checkString("}")

        return MapCall(expression)
    }

    private fun parseCall(): Call {
        return if (source.curSym() == 'f') {
            parseFilterCall()
        } else {
            parseMapCall()
        }
    }

    private fun parseCallChain(): CallChain {
        val calls = if (source.curSym() != Source.END) {
            listOf(parseCall()).toMutableList()
        } else {
            emptyList<Call>().toMutableList()
        }

        while (source.curSym() != Source.END) {
            checkString("%>%")
            calls += parseCall()
        }

        return CallChain(calls)
    }

    companion object {
        fun parse(string: String): CallChain {
            return Parser(string).parseCallChain().shorten()
        }

        fun shorten(string: String): String {
            return parse(string).toString()
        }

        fun simplify(string: String): String {
            return parse(string).simplify().toString()
        }
    }
}