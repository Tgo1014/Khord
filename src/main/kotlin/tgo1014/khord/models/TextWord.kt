package tgo1014.khord.models

internal data class TextWord(
    val word: String,
    val startIndex: Int = 0,
    val endIndex: Int = 0,
    val couldBeChord: Boolean = false,
    var isConfirmedChord: Boolean = false,
) {
    fun toChord() = Chord(word, startIndex, endIndex)
}