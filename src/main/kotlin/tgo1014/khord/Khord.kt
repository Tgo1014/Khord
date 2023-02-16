package tgo1014.khord

import tgo1014.khord.models.Chord
import tgo1014.khord.models.ChordRoot
import tgo1014.khord.models.TextWord
import kotlin.math.max

public object Khord {

    /**
     * Search the provided [text] for known [Chord]s
     *
     * @return list of detected [Chord]s on text or empty
     */
    public fun find(text: String): List<Chord> {
        val foundChordsList = mutableListOf<TextWord>()
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
                val mostAreChords =
                    mappedChords.count { it.isConfirmedChord } >= max(mappedChords.size / 2, 1)
                if (mostAreChords) {
                    foundChordsList.addAll(mappedChords.confirmedList)
                }
                offset += lineText.length + 1 // +1 for line break
            }
        return foundChordsList.map { it.toChord() }
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
            val transposeDiff = newTone.compareTo(originalTone)
            val root = ChordRoot.from(chord.chord)
            val newRoot = ChordRoot.asCircularList()[root.ordinal + transposeDiff]
            val transposeChord = chord.chord.replaceRange(0, root.root.length, newRoot.root)
            if (!transposeChord.contains("/")) {
                return transposeChord
            }
            var reversedRoot = transposeChord.substringAfter("/")
            reversedRoot = transposeChord(find(reversedRoot).first(), originalTone, newTone)
            return transposeChord.substringBefore("/") + "/" + reversedRoot
        } catch (e: Exception) {
            // In case there's a weird symbol that gets recognized as chord and fails to transpose,
            // just return it back instead of crashing
            println("Failed to transpose chord: $chord")
            return chord.chord
        }
    }

    private fun isValidChord(chord: String): Boolean {
        if (chord.length == 1 && chord in ChordRoot.allChords) {
            return true // Need to check if there's other chords in the same line
        }
        // "º", "°" are different, thanks ASCII!
        if (chord.length == 2) { // confirm Xm chords
            val validList = listOf('m', '#', 'b', 'º', '°')
            return validList.contains(chord[1]) || chord[1].isDigit() // 6, 7, 13, etc...
        }
        if (chord.length >= 3 && chord[2] == 'm') { // confirm X#m chords
            val validList = listOf('#', 'b', 'º', '°')
            return validList.contains(chord[1]) || chord[1].isDigit()
        }
        val chordFirstChar = chord.firstOrNull { it.isUpperCase() }?.toString()
        if (chordFirstChar in ChordRoot.allChords && chord.findAnyOf(listOf("add", "/", "º", "°")) != null) {
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
        val removedSymbolsString = this.replace("(", " ").replace(")", " ")
        return regex.findAll(removedSymbolsString).map {
            TextWord(
                word = it.value,
                startIndex = it.range.first,
                endIndex = it.range.last + 1,
            )
        }.toList()
    }

    private fun String.fixWeirdLineBreaks() = replace("\"", "")
        .replace("\\\\n", System.lineSeparator())
        .replace("\\n", System.lineSeparator())

    private val List<TextWord>.confirmedList get() = this.filter { it.isConfirmedChord }

}