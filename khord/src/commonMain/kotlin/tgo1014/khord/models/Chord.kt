package tgo1014.khord.models

/**
 * Class used to represent a found known Chord found in a text
 *
 * @property chord the string of a valid chord
 * @property startIndex the start index of the chord in the text
 * @property endIndex the end index of the chord in the text
 */
public data class Chord(
    val chord: String,
    val startIndex: Int,
    val endIndex: Int,
)