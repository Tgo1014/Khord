package tgo1014.khord

import tgo1014.khord.models.Chord
import tgo1014.khord.models.ChordRoot
import tgo1014.khord.models.TextWord

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
        val fixedText = text.fixWeirdLineBreaks()
        var offset = 0
        val foundChordsList = buildList {
            fixedText.lines().forEach { lineText ->
                val wordsInLine = detectWordsInLine(lineText)
                val mappedChords = wordsInLine.map {
                    it.copy(
                        startIndex = it.startIndex + offset,
                        endIndex = it.endIndex + offset,
                        isConfirmedChord = isValidChord(it.word)
                    )
                }
                // If more than half of the items are chords, consider as a chord line
                val filteredMostAreChords = mappedChords.filter { it.word !in listOf("(", ")") }
                val mostAreChords = if (filteredMostAreChords.isNotEmpty()) {
                    filteredMostAreChords.count { it.isConfirmedChord } > filteredMostAreChords.size / 2
                } else {
                    false
                }
                if (mostAreChords) {
                    addAll(mappedChords.confirmedList.map { textWord ->
                        val chord = textWord.toChord()
                        if (chord.chord.startsWith("(") && chord.chord.endsWith(")")) {
                            chord.copy(
                                chord = chord.chord.removeSurrounding("(", ")"),
                                startIndex = chord.startIndex + 1,
                                endIndex = chord.endIndex - 1
                            )
                        } else chord
                    })
                }
                offset += lineText.length + 1 // +1 for line break
            }
        }
        return if (simplify) {
            foundChordsList.map { it.simplify() }
        } else {
            foundChordsList
        }
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
        val fixedText = text.fixWeirdLineBreaks()
        val originalChordList = find(fixedText)
        var simplifiedText = fixedText
        originalChordList.forEach {
            val simpleChord = it.simplify()
            val sizeDiffInSpaces = " ".repeat((it.chord.length - simpleChord.chord.length).coerceAtLeast(0))
            simplifiedText = simplifiedText.replaceRange(
                startIndex = it.startIndex,
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
        val fixedText = text.fixWeirdLineBreaks()
        val chordList = find(fixedText)
        var transposedText = fixedText
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
        return try {
            if (originalTone == newTone) return chord.chord
            val root = ChordRoot.from(chord.chord)
            val newRoot = transposeRoot(root, originalTone, newTone)
            val transposeChord = chord.chord.replaceRange(0, root.root.length, newRoot.root)
            if (!transposeChord.contains("/")) {
                return transposeChord
            }
            val reversedRootStr = transposeChord.substringAfter("/")
            val reversedRoot = ChordRoot.from(reversedRootStr)
            val newReversedRoot = transposeRoot(reversedRoot, originalTone, newTone)
            transposeChord.substringBefore("/") + "/" + newReversedRoot.root
        } catch (_: Exception) {
            // In case there's a weird symbol that gets recognized as chord and fails to transpose,
            // just return it back instead of crashing
            println("Khord: Failed to transpose chord: $chord")
            chord.chord
        }
    }

    private fun transposeRoot(root: ChordRoot, originalTone: ChordRoot, newTone: ChordRoot): ChordRoot {
        val transposeDiff = newTone.ordinal - originalTone.ordinal
        return ChordRoot.asCircularList()[root.ordinal + transposeDiff]
    }

    private fun isValidChord(chord: String): Boolean {
        val cleaned = chord.removePrefix("(").removeSuffix(")")
        val rootStr = ChordRoot.allChords.firstOrNull { cleaned.startsWith(it) } ?: return false
        if (cleaned.length == rootStr.length) return true
        val suffix = cleaned.substring(rootStr.length)
        if (suffix.startsWith('m')) {
            if (suffix.length == 1) return true
            if (suffix[1].isDigit() || suffix[1] in listOf('º', '°')) return true
        }
        if (suffix.any { it.isDigit() }) return true
        if (suffix.findAnyOf(listOf("add", "/", "º", "°", "sus", "maj", "dim", "aug")) != null) return true
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
        return buildList {
            wordList.forEach { word ->
                var current = word
                if (current.word.length > 1 && current.word.startsWith('(') && !current.word.contains(')')) {
                    add(TextWord(word = "(", startIndex = current.startIndex, endIndex = current.startIndex + 1))
                    current = current.copy(word = current.word.substring(1), startIndex = current.startIndex + 1)
                }
                if (current.word.length > 1 && current.word.endsWith(')') && !current.word.contains('(')) {
                    add(current.copy(word = current.word.substring(0, current.word.length - 1), endIndex = current.endIndex - 1))
                    add(TextWord(word = ")", startIndex = current.endIndex - 1, endIndex = current.endIndex))
                } else {
                    add(current)
                }
            }
        }.filter { it.word.isNotBlank() }
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
    )

    private fun String.fixWeirdLineBreaks() = replace("\"", "")
        .replace("\\\\n", lineSeparator)
        .replace("\\n", lineSeparator)

    private val List<TextWord>.confirmedList get() = this.filter { it.isConfirmedChord }

}