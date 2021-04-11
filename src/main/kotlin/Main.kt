fun main() {
    val simplify = Parser.Companion::simplify

    val string = generateSequence(::readLine).joinToString()

    println(simplify(string))
}