class Source(val source: String) {
    private var index: Int = 0

    fun nextSym(): Char {
        ++index
        return curSym()
    }

    fun curSym() = if (index < source.length) source[index] else END

    companion object {
        const val END = 0.toChar()
    }
}