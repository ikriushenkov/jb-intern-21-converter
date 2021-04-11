import exceptions.SyntaxException

interface Operation<T, V> {
    val char: Char

    enum class Arithmetic(override val char: Char) : Operation<Int, Int> {
        PLUS('+') {
            override fun apply(a: Int, b: Int): Int {
                return a + b
            }
        },
        MINUS('-') {
            override fun apply(a: Int, b: Int): Int {
                return a - b
            }
        },
        MULTIPLY('*') {
            override fun apply(a: Int, b: Int): Int {
                return a * b
            }
        };

        override fun toString() = char.toString()
    }

    enum class Logic(override val char: Char) : Operation<Boolean, Boolean> {
        AND('&') {
            override fun apply(a: Boolean, b: Boolean): Boolean {
                return a && b
            }
        },
        OR('|') {
            override fun apply(a: Boolean, b: Boolean): Boolean {
                return a || b
            }
        };

        override fun toString() = char.toString()
    }

    enum class ArithmeticToLogic(override val char: Char) : Operation<Int, Boolean> {
        MORE('>') {
            override fun apply(a: Int, b: Int): Boolean {
                return a > b
            }
        },
        LESS('<') {
            override fun apply(a: Int, b: Int): Boolean {
                return a < b
            }
        },
        EQUALS('=') {
            override fun apply(a: Int, b: Int): Boolean {
                return a == b
            }
        };

        override fun toString() = char.toString()
    }

    fun apply(a: T, b: T): V

    companion object {
        private val operations: Map<Char, Operation<*, *>> =
            (Arithmetic.values().toList() +
                    Logic.values() + ArithmeticToLogic.values())
                .associateBy { it.char }


        fun createFromChar(char: Char): Operation<*, *> {
            return operations[char] ?: throw SyntaxException()
        }
    }
}