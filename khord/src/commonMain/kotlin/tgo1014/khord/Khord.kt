package tgo1014.khord

import tgo1014.khord.models.Chord
import tgo1014.khord.models.ChordRoot
import tgo1014.khord.models.TextWord
import kotlin.math.max

public object Khord {

    /**
     * Search the provided [text] for known [Chord]s
     *
     * @param text The text to search for chords.
     * @param simplify If true, simplifies chords (e.g., Cmaj7 becomes C). Defaults to false.
     * @return list of detected [Chord]s on text or empty
     */
    public fun find(
        text: String,
        simplify: Boolean = false,
    ): List<Chord> {
        var foundChordsList = mutableListOf<TextWord>()
        var offset = 0
        text.fixWeirdLineBreaks()
            .lines()
            .forEach { lineText ->
                val wordsInLine = detectWordsInLine(lineText)
                val mappedChords = wordsInLine.map {
                    TextWord(
                        word = it.word,
                        startIndex = it.startIndex + offset,
                        endIndex = it.endIndex + offset,
                        isConfirmedChord = isValidChord(it.word)
                    )
                }
                // If more than half of the items are chords, consider as a chord line
                val filteredMostAreChords =  mappedChords.filter { it.word !in listOf("(",")") }  // Ignore parenthesis
                val mostAreChords = if (filteredMostAreChords.size == 1) {
                    filteredMostAreChords.first().isConfirmedChord
                } else {
                    filteredMostAreChords
                        .filter { it.word !in listOf("(",")") }
                        .count { it.isConfirmedChord } > max(filteredMostAreChords.size / 2, 1)
                }
                if (mostAreChords) {
                    foundChordsList.addAll(mappedChords.confirmedList)
                }
                offset += lineText.length + 1 // +1 for line break
            }
        if (simplify) {
            foundChordsList = foundChordsList.map { it.simplify() }.toMutableList()
        }
        return foundChordsList.map { it.toChord() }
    }

    /**
     * Finds all chords in a given [text] and simplifies them.
     *
     * For example, a line containing "Cmaj7 G7 Am" would be transformed into "C     G  Am",
     * preserving the alignment by padding with spaces.
     *
     * @param text The input string containing chords to be simplified.
     * @return A new string with the chords simplified and alignment preserved.
     */
    public fun simplifyChordsInText(text: String): String {
        val originalChordList = find(text)
        var simplifiedText = text.fixWeirdLineBreaks()
        originalChordList.forEach {
            val simpleChord = it.simplify()
            val sizeDiffInSpaces = List(it.chord.length - simpleChord.chord.length) { " " }.joinToString("")
            simplifiedText = simplifiedText.replaceRange(
                startIndex = it.startIndex ,
                endIndex = it.endIndex,
                replacement = simpleChord.chord + sizeDiffInSpaces
            )
        }
        return simplifiedText
    }

    /**
     * Transpose the found [Chord]s in the [text] from the [originalTone] to the desired [newTone]
     *
     * @return text with chords transposed
     */
    public fun transposeText(text: String, originalTone: ChordRoot, newTone: ChordRoot? = null): String {
        if (originalTone == newTone || newTone == null) {
            return text
        }
        var transposedText = text.fixWeirdLineBreaks()
        val chordList = find(text)
        var transposedChordsSizeDiffOffset = 0
        chordList.forEach { chord ->
            val chordSize = chord.endIndex - chord.startIndex
            val transposedChord = transposeChord(chord, originalTone, newTone)
            transposedText = transposedText.replaceRange(
                startIndex = chord.startIndex + transposedChordsSizeDiffOffset,
                endIndex = chord.endIndex + transposedChordsSizeDiffOffset,
                replacement = transposedChord
            )
            transposedChordsSizeDiffOffset += (transposedChord.length - chordSize)
        }
        return transposedText
    }

    /**
     * Receives a single [Chord] and transpose it from the [originalTone] to the [newTone]
     *
     * @return string with transposed chord
     */
    public fun transposeChord(
        chord: Chord,
        originalTone: ChordRoot,
        newTone: ChordRoot
    ): String {
        try {
            if (originalTone == newTone) return chord.chord
            val transposeDiff = newTone.ordinal - originalTone.ordinal
            val root = ChordRoot.from(chord.chord)
            val newRoot = ChordRoot.asCircularList()[root.ordinal + transposeDiff]
            val transposeChord = chord.chord.replaceRange(0, root.root.length, newRoot.root)
            if (!transposeChord.contains("/")) {
                return transposeChord
            }
            var reversedRoot = transposeChord.substringAfter("/")
            reversedRoot = transposeChord(find(reversedRoot).first(), originalTone, newTone)
            return transposeChord.substringBefore("/") + "/" + reversedRoot
        } catch (_: Exception) {
            // In case there's a weird symbol that gets recognized as chord and fails to transpose,
            // just return it back instead of crashing
            println("Khord: Failed to transpose chord: $chord")
            return chord.chord
        }
    }

    private fun isValidChord(chord: String): Boolean {
        if (chord.length == 1 && chord in ChordRoot.allChords) {
            return true // Need to check if there's other chords in the same line
        }
        if (chord.length == 2) { // confirm Xm chords
            val validList = listOf('m', '#', 'b', 'º', '°') // "º", "°" are different, thanks ASCII!
            return validList.contains(chord[1]) || chord[1].isDigit() // 6, 7, 13, etc...
        }
        if (chord.length >= 3 && chord[2] == 'm') { // confirm X#m chords
            val validList = listOf('#', 'b', 'º', '°')
            return validList.contains(chord[1]) || chord[1].isDigit()
        }
        val chordFirstChar = chord.firstOrNull { it.isUpperCase() }?.toString()
        if (chordFirstChar in ChordRoot.allChords && chord.findAnyOf(listOf("add", "/", "º", "°", "sus")) != null) {
            return true
        }
        if (chordFirstChar in ChordRoot.allChords && chord.any { it.isDigit() }) {
            return true
        }
        return false
    }

    private fun detectWordsInLine(line: String): List<TextWord> {
        return line.splitIntoWordsWithIndexes()
            .map { textWord ->
                val couldBeChord = ChordRoot.allChords.any { textWord.word.startsWith(it) }
                textWord.copy(couldBeChord = couldBeChord)
            }
    }

    private fun String.splitIntoWordsWithIndexes(): List<TextWord> {
        val regex = "[^\\s\\n]+".toRegex() // find anything but spaces and new lines
        val wordList = regex.findAll(this).map {
            TextWord(
                word = it.value,
                startIndex = it.range.first,
                endIndex = it.range.last + 1,
            )
        }.toList()
        val finalList = mutableListOf<TextWord>()
        // When we have parenthesis, but they are not part of a chord, split them into their own TextWord
        wordList.forEach {
            if (it.word.first() == '(' && !it.word.contains(")")) {
                finalList.add(TextWord(word = "(", startIndex = it.startIndex, endIndex = it.startIndex + 1))
                finalList.add(TextWord(word = it.word.removeRange(0..0), startIndex = it.startIndex + 1, endIndex = it.endIndex))
                return@forEach
            }
            if (it.word.last() == ')' && !it.word.contains("(")) {
                finalList.add(TextWord(word = it.word.substring(0..<it.word.lastIndex), startIndex = it.startIndex, endIndex = it.endIndex - 1))
                finalList.add(TextWord(word = ")", startIndex = it.endIndex - 1, endIndex = it.endIndex))
                return@forEach
            }
            finalList.add(it)
        }
        return finalList.filter { it.word.isNotBlank() }
    }


    /**
     * Simplifies a [TextWord] if it's a confirmed chord and matches a simplification rule.
     *
     * For example, "Cmaj7" would be simplified to "C".
     *
     * The simplification rules are defined in `simplificationMap`.
     * If the chord doesn't match any rule or isn't a confirmed chord, it's returned unchanged.
     *
     * @return The simplified [TextWord], or the original if no simplification occurred.
     */
    private fun TextWord.simplify(): TextWord {
        if (!this.isConfirmedChord) {
            return this
        }
        val (simplifiedWord, lengthDiff) = simplifyChordString(this.word)
        return this.copy(
            word = simplifiedWord,
            endIndex = this.endIndex - lengthDiff
        )
    }

    /**
     * Simplifies a given [Chord] by removing complex notations.
     *
     * This function takes a [Chord] object and simplifies its string representation
     * based on a predefined map of simplification rules. For example, a chord like "Am7(5-)"
     * might be simplified to "Am7". If no simplification rule applies, the original chord is returned.
     *
     * @return A new [Chord] object with the simplified chord string, or the original [Chord] if no simplification was performed.
     */
    private fun Chord.simplify(): Chord {
        val (simplifiedChord, lengthDiff) = simplifyChordString(this.chord)
        return this.copy(
            chord = simplifiedChord,
            endIndex = this.endIndex - lengthDiff
        )
    }

    /**
     * Simplifies a chord string based on the rules in `simplificationMap`.
     *
     * @return A [Pair] containing the simplified chord string and the difference in length.
     */
    private fun simplifyChordString(chordString: String): Pair<String, Int> {
        for ((complex, simple) in simplificationMap) {
            if (chordString.contains(complex)) {
                val simplified = chordString.replace(complex, simple)
                return simplified to (chordString.length - simplified.length)
            }
        }
        return chordString to 0
    }

    private val simplificationMap = mapOf(
        "7(9-)"  to "7",
        "m7(5-)" to "m7",
        "maj9"   to "maj7",
        "Δ9"     to "maj7",
        "7M(9)"  to "7M",
        "maj7"   to "",
        "Δ7"     to "",
        "7M"     to "",
        "add9"   to "",
        "13"     to "7",
        "11"     to "7",
        "9"      to "7",
        "7b9"    to "7",
        "7#9"    to "7",
        "7b5"    to "7",
        "7#5"    to "7",
        "m7b5"   to "m7",
        "m7(5b)" to "m7",
        "ø7"     to "m7",
        "dim7"   to "m7",
        "º7"     to "m7",
        "m9"     to "m7",
        "m11"    to "m7",
        "m13"    to "m7",
        "mMaj7"  to "m",
        "mΔ7"    to "m",
        "m7M"    to "m",
        "sus2"   to "",
        "sus4"   to "",
        "6"      to "",
        "m6"     to "m",
        "aug"    to "",
        "+"      to "",
        "dim"    to "m",
        "º"      to "m",
        "5"      to "",
        "m7b5"   to "m7",
    )

    private fun String.fixWeirdLineBreaks() = replace("\"", "")
        .replace("\\\\n", lineSeparator)
        .replace("\\n", lineSeparator)

    private val List<TextWord>.confirmedList get() = this.filter { it.isConfirmedChord }

}