package expression

import Operation

class ArithmeticBinaryExpression(
    firstExpression: ArithmeticExpression,
    secondExpression: ArithmeticExpression,
    operation: Operation.Arithmetic
) : BinaryExpression(firstExpression, secondExpression, operation),
    ArithmeticExpression {
    override fun simplify(): ArithmeticExpression {
        val first = firstExpression.simplify()
        val second = secondExpression.simplify()
        val op = operation as Operation.Arithmetic

        return when (first) {
            is Const -> when (second) {
                is Const -> Const(op.apply(first.value, second.value))
                is Element -> fixExpression(ArithmeticBinaryExpression(second, first, op))
                is ArithmeticBinaryExpression -> constWithBinary(first, second, op)
                else -> this
            }
            is Element -> when (second) {
                is Element -> twoElements(op)
                is ArithmeticBinaryExpression -> elementWithBinary(second, op)
                else -> fixExpression(this)
            }
            is ArithmeticBinaryExpression -> when (second) {
                is Const -> constWithBinary(second, first, op)
                is Element -> elementWithBinary(first, op)
                is ArithmeticBinaryExpression -> twoBinary(first, second, op)
                else -> this
            }
            else -> this
        }
    }

    companion object {
        private fun twoElements(operation: Operation.Arithmetic): ArithmeticExpression {
            return when (operation) {
                Operation.Arithmetic.PLUS ->
                    ArithmeticBinaryExpression(Element(), Const(2), Operation.Arithmetic.MULTIPLY)
                Operation.Arithmetic.MINUS ->
                    Const(0)
                Operation.Arithmetic.MULTIPLY ->
                    ArithmeticBinaryExpression(Element(), Element(), Operation.Arithmetic.MULTIPLY)
            }
        }

        private fun fixExpression(expression: ArithmeticBinaryExpression): ArithmeticExpression {
            if (expression.secondExpression is Const) {
                val const = expression.secondExpression as Const
                if (const.value == 0) {
                    return if (expression.operation == Operation.Arithmetic.MULTIPLY) {
                        Const(0)
                    } else {
                        expression.firstExpression as ArithmeticExpression
                    }
                }
                if (const.value == 1 && expression.operation == Operation.Arithmetic.MULTIPLY) {
                    return expression.firstExpression as ArithmeticExpression
                }
                if (const.value < 0 &&
                    (expression.operation == Operation.Arithmetic.PLUS ||
                            expression.operation == Operation.Arithmetic.MINUS)
                ) {
                    return expression.also {
                        it.operation =
                            if (it.operation == Operation.Arithmetic.PLUS)
                                Operation.Arithmetic.MINUS else
                                Operation.Arithmetic.PLUS
                        it.secondExpression = Const(-const.value)
                    }
                }
            }
            return expression
        }

        private fun constWithBinary(
            firstExpression: Const,
            secondExpression: ArithmeticBinaryExpression,
            operation: Operation.Arithmetic
        ): ArithmeticExpression {
            return when (operation) {
                Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                    if (secondExpression.secondExpression is Const &&
                        (secondExpression.operation == Operation.Arithmetic.PLUS ||
                                secondExpression.operation == Operation.Arithmetic.MINUS)
                    ) {

                        fixExpression(
                            ArithmeticBinaryExpression(
                                secondExpression.firstExpression as ArithmeticExpression,
                                Const(
                                    if (operation != secondExpression.operation) {
                                        if (operation == Operation.Arithmetic.MINUS) {
                                            operation
                                                .apply(
                                                    (secondExpression.secondExpression as Const).value,
                                                    firstExpression.value
                                                )
                                        } else {
                                            operation
                                                .apply(
                                                    (secondExpression.secondExpression as Const).value,
                                                    -firstExpression.value
                                                )
                                        }
                                    } else {
                                        Operation.Arithmetic.PLUS
                                            .apply(
                                                (secondExpression.secondExpression as Const).value,
                                                firstExpression.value
                                            )
                                    }
                                ), secondExpression.operation as Operation.Arithmetic
                            )
                        )
                    } else {
                        ArithmeticBinaryExpression(secondExpression, firstExpression, operation)
                    }
                }
                Operation.Arithmetic.MULTIPLY -> {
                    when (secondExpression.operation as Operation.Arithmetic) {
                        Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                            fixExpression(
                                ArithmeticBinaryExpression(
                                    ArithmeticBinaryExpression(
                                        secondExpression.firstExpression as ArithmeticExpression,
                                        firstExpression as ArithmeticExpression, Operation.Arithmetic.MULTIPLY
                                    ).simplify(),
                                    ArithmeticBinaryExpression(
                                        secondExpression.secondExpression as ArithmeticExpression,
                                        firstExpression as ArithmeticExpression, Operation.Arithmetic.MULTIPLY
                                    ).simplify(),
                                    secondExpression.operation as Operation.Arithmetic
                                )
                            )
                        }
                        Operation.Arithmetic.MULTIPLY -> {
                            if (secondExpression.secondExpression is Const) {
                                fixExpression(
                                    ArithmeticBinaryExpression(
                                        secondExpression.firstExpression as ArithmeticExpression,
                                        Const(
                                            operation
                                                .apply(
                                                    (secondExpression.secondExpression as Const).value,
                                                    firstExpression.value
                                                )
                                        ),
                                        secondExpression.operation as Operation.Arithmetic
                                    )
                                )
                            } else {
                                ArithmeticBinaryExpression(
                                    secondExpression,
                                    firstExpression,
                                    secondExpression.operation as Operation.Arithmetic
                                )
                            }
                        }
                    }
                }
            }
        }

        private fun elementWithBinary(
            second: ArithmeticBinaryExpression,
            operation: Operation.Arithmetic
        ): ArithmeticExpression {
            return if (second.firstExpression is Element && second.secondExpression is Const) {
                val op = second.operation as Operation.Arithmetic
                when (operation) {
                    Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS ->
                        when (op) {
                            Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS ->
                                ArithmeticBinaryExpression(
                                    twoElements(operation),
                                    second.secondExpression as Const, op
                                ).simplify()
                            Operation.Arithmetic.MULTIPLY ->
                                fixExpression(
                                    ArithmeticBinaryExpression(
                                        Element(),
                                        Const(operation.apply((second.secondExpression as Const).value, 1)), op
                                    )
                                )
                        }
                    Operation.Arithmetic.MULTIPLY -> {
                        when (op) {
                            Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS ->
                                ArithmeticBinaryExpression(
                                    twoElements(operation),
                                    ArithmeticBinaryExpression(
                                        Element(),
                                        second.secondExpression as Const, operation
                                    ),
                                    op
                                ).simplify()
                            Operation.Arithmetic.MULTIPLY -> {
                                ArithmeticBinaryExpression(
                                    twoElements(operation),
                                    second.secondExpression as Const, operation
                                ).simplify()
                            }
                        }
                    }
                }
            } else {
                if (second.secondExpression is Element) {
                    when (operation) {
                        Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                            fixExpression(
                                ArithmeticBinaryExpression(
                                    second.firstExpression as ArithmeticExpression,
                                    twoElements(operation), second.operation as Operation.Arithmetic
                                )
                            )
                        }
                        Operation.Arithmetic.MULTIPLY -> {
                            ArithmeticBinaryExpression(
                                ArithmeticBinaryExpression(
                                    second.firstExpression as ArithmeticExpression, Element(), operation
                                ).simplify(),
                                twoElements(operation),
                                second.operation as Operation.Arithmetic
                            )
                        }
                    }
                } else {
                    when (operation) {
                        Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                            when (second.operation as Operation.Arithmetic) {
                                Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                                    if (second.secondExpression is ArithmeticBinaryExpression) {
                                        val bin = second.secondExpression as ArithmeticBinaryExpression
                                        if (bin.firstExpression is Element) {
                                            fixExpression(
                                                ArithmeticBinaryExpression(
                                                    second.firstExpression as ArithmeticExpression,
                                                    ArithmeticBinaryExpression(bin, Element(), operation).simplify(),
                                                    second.operation as Operation.Arithmetic
                                                )
                                            )
                                        } else {
                                            ArithmeticBinaryExpression(second, Element(), operation)
                                        }
                                    } else {
                                        ArithmeticBinaryExpression(second, Element(), operation)
                                    }
                                }
                                Operation.Arithmetic.MULTIPLY -> {
                                    ArithmeticBinaryExpression(second, Element(), operation)
                                }
                            }
                        }
                        Operation.Arithmetic.MULTIPLY -> {
                            when (second.operation as Operation.Arithmetic) {
                                Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                                    ArithmeticBinaryExpression(
                                        ArithmeticBinaryExpression(
                                            second.firstExpression as ArithmeticExpression,
                                            Element(),
                                            operation
                                        ).simplify(),
                                        ArithmeticBinaryExpression(
                                            second.secondExpression as ArithmeticExpression,
                                            Element(),
                                            operation
                                        ).simplify(),
                                        second.operation as Operation.Arithmetic
                                    )
                                }
                                Operation.Arithmetic.MULTIPLY -> {
                                    ArithmeticBinaryExpression(
                                        ArithmeticBinaryExpression(
                                            second.firstExpression as ArithmeticExpression,
                                            Element(),
                                            operation
                                        ).simplify(),
                                        second.secondExpression as ArithmeticExpression,
                                        second.operation as Operation.Arithmetic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        private fun twoBinary(
            first: ArithmeticBinaryExpression,
            second: ArithmeticBinaryExpression,
            operation: Operation.Arithmetic
        ): ArithmeticExpression {
            return when (operation) {
                Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                    when (second.operation as Operation.Arithmetic) {
                        Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                            ArithmeticBinaryExpression(
                                ArithmeticBinaryExpression(
                                    first,
                                    second.firstExpression as ArithmeticExpression, operation
                                ).simplify(),
                                second.secondExpression as ArithmeticExpression,
                                second.operation as Operation.Arithmetic
                            ).simplify()
                        }
                        else -> ArithmeticBinaryExpression(first, second, operation)
                    }
                }
                else -> when (second.operation as Operation.Arithmetic) {
                    Operation.Arithmetic.PLUS, Operation.Arithmetic.MINUS -> {
                        ArithmeticBinaryExpression(
                            ArithmeticBinaryExpression(
                                first,
                                second.firstExpression as ArithmeticExpression,
                                operation
                            ).simplify(),
                            ArithmeticBinaryExpression(
                                first,
                                second.secondExpression as ArithmeticExpression,
                                operation
                            ).simplify(),
                            second.operation as Operation.Arithmetic
                        ).simplify()
                    }
                    else -> ArithmeticBinaryExpression(first, second, operation)
                }
            }
        }
    }
}