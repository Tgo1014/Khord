package tgo1014.khord

import tgo1014.khord.models.Chord
import tgo1014.khord.models.ChordRoot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KhordTest {

    private val testText = "        C            F\n" +
            "Sem ressalva, sem escalas\n" +
            " D#m7-/G          F\n" +
            "Demorei mas fui\n" +
            "            Dm               F\n" +
            "E da janela vejo a esfera azul Uh Uh Uuuuh"

    @Test
    fun `GIVEN a text WHEN searching chord THEN return correct number of chords`() {
        assertEquals(6, Khord.find(testText).size)
    }

    @Test
    fun `GIVEN a invalid chord list WHEN searching for chord THEN return empty list`() {
        val invalidChordList = listOf("Car", "\\nBuscai", "2a.vez,", "À tua cruz", "imensidao/")
        invalidChordList.forEach {
            assertTrue(Khord.find(it).isEmpty())
        }
    }

    @Test
    fun `GIVEN a valid chords list WHEN searching for chords THEN return valid chords`() {
        val validChordsList = listOf("D/F#", "G#4", "G7M", "C#m7M")
        validChordsList.forEach {
            assertTrue(Khord.find(it).isNotEmpty())
        }
    }

    @Test
    fun `GIVEN a phrase without chords WHEN searching for chords THEN return empty list`() {
        val phasesList = listOf("Em nome de Cristo, que e a nossa paz!")
        phasesList.forEach {
            assertTrue(Khord.find(it).isEmpty())
        }
    }

    @Test
    fun `WHEN transposing to the same root THEN return same chord`() {
        val result = Khord.transposeText("C", ChordRoot.C, ChordRoot.C)
        assertEquals("C", result)
    }

    @Test
    fun `GIVEN a transposition WHEN transposing THEN return correct chord`() {
        val result = Khord.transposeText("C", ChordRoot.C, ChordRoot.Db)
        assertEquals("C#", result)
        val result2 = Khord.transposeText("C", ChordRoot.C, ChordRoot.Bb)
        assertEquals("Bb", result2)
    }

    @Test
    fun `GIVEN a text with chords and phrases WHEN transposing text THEN return same text with transposed notes`() {
        val result = Khord.transposeText("C\nCar", ChordRoot.C, ChordRoot.D)
        assertEquals("D\nCar", result)
    }

    @Test
    fun `GIVEN a text with just a chord WHEN transposing text THEN return transposed chord`() {
        val result = Khord.transposeText("G", ChordRoot.G, ChordRoot.C)
        assertEquals("C", result)
    }

    @Test
    fun `GIVEN a chord diminished chord WHEN transposing chord THEN return correct chord`() {
        val result = Khord.transposeChord(Khord.find("F#º").first(), ChordRoot.G, ChordRoot.Ab)
        assertEquals("Gº", result)
        val result2 = Khord.transposeChord(Khord.find("F#º").first(), ChordRoot.G, ChordRoot.A)
        assertEquals("G#º", result2)
    }

    @Test
    fun `GIVEN a text with chords text and breaklines WHEN transposing text THEN return correct chords`() {
        val result = Khord.transposeText(
            "       Em           C           Am7        F#º\nVim buscar e vim salvar o que estava já perdido",
            ChordRoot.C,
            ChordRoot.D
        )
        assertEquals(
            "       F#m          D           Bm7        G#º\nVim buscar e vim salvar o que estava já perdido",
            result
        )
    }

    @Test
    fun `GIVEN chords with parenthesis WHEN searching chords THEN return proper chords`() {
        val result = Khord.find("(C F# G)")
        assertEquals("C", result[0].chord)
        assertEquals("F#", result[1].chord)
        assertEquals("G", result[2].chord)
    }

    @Test
    fun `GIVEN a chord with reversed root WHEN transposing THEN transpose also the root`() {
        val result = Khord.transposeChord(Khord.find("C/D").first(), ChordRoot.C, ChordRoot.D)
        assertEquals("D/E", result)
        val result2 = Khord.transposeChord(Khord.find("C/D").first(), ChordRoot.C, ChordRoot.Eb)
        assertEquals("D#/F", result2)
    }

    @Test
    fun `GIVEN a ChatGPT _hehe!_ generated chord list WHEN searching chords THEN all are valid`() {
        val chordList =
            "C, G, Am, F, Dm, Em, A, E, D, Bb, Gm, B, C7, Fmaj7, G7, Am7, D7, B7, E7, Cmaj7, Fm, Ab, Gmaj7, Cm, Eb, A7, Dm7, Gm7, C#m, F#m, Bm, E6, Cdim, G#dim, D#dim, Adim, C#7, F#7, B7b9, G#7b9, D#7b9, A7b9, D#7, G#7, C#m7, F#m7, Bm7, Em7, A7#5, D#7#5, G#7#5, C#7#5, D7#9, G7#9, A7#9, C7#9, Dm6, Am6, G6, C6, Bb6, F6, D7b5, G7b5, A7b5, C7b5, Dm9, Am9, G9, C9, Bb9, F9, Emaj7, Bb7, G7b9, A7b13, C13, D13, G13, E13, A13, B13, F#13, C#13, F13, Bb13, Abmaj7, Dbmaj7, Gbmaj7, Gbm7, Db7, Ab7, Gb7, Bbmaj7, Ebmaj7, F#7, C#m9, F#m9, Bm9, Em9, Amaj7#5, D7alt, G7alt, C7alt, F7alt"
        val split = chordList.split(",")
        val valids = split.filter {
            Khord.find(chordList).isNotEmpty()
        }
        assertEquals(valids.size, split.size)
    }

    @Test
    fun `GIVEN an invalid chord WHEN transposing THEN return same word`() {
        val chord = Chord("Hello World", 0, 1)
        val transposed = Khord.transposeChord(chord, ChordRoot.C, ChordRoot.F)
        assertEquals(transposed, chord.chord)
    }

    @Test
    fun `GIVEN string that's text WHEN there's just one chord THEN ignore line as chord`() {
        val result = Khord.find("Só em Ti")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `GIVEN chord with parenthesis WHEN searching for it THEN replace it with slash`() {
        val result = Khord.find("G6(9)")
        assertEquals("G6(9)", result.first().chord)
    }

    @Test
    fun `GIVEN string with multiple parenthesis WHEN find chords THEN map chords properly and ignore text`() {
        val result = Khord.find("G6(9) G6(9) (test)")
        assertEquals("G6(9)", result[0].chord)
        assertEquals("G6(9)", result[1].chord)
        assertNull(result.getOrNull(2))
    }

    @Test
    fun `GIVEN chord with parenthesis WHEN transposing THEN transposes correctly`() {
        val chord = Khord.find("G6(9)").first()
        val result = Khord.transposeChord(chord, ChordRoot.C, ChordRoot.Db)
        assertEquals("G#6(9)", result)
    }

    @Test
    fun `GIVEN chord is sus WHEN searching THEN return as valid chord`() {
        val result = Khord.find("Gsus G")
        assertEquals("Gsus", result[0].chord)
        assertEquals("G", result[1].chord)
    }

    @Test
    fun `GIVEN sus chord WHEN transposing THEN transposes correctly`() {
        val chord = Khord.find("Gsus").first()
        val result = Khord.transposeChord(chord, ChordRoot.C, ChordRoot.Db)
        assertEquals("G#sus", result)
    }

    @Test
    fun `GIVEN chords text WHEN chords between parenthesis THEN chords identified correctly`() {
        val result = Khord.find("(  F   G   C  )")
        assertEquals("F", result[0].chord)
        assertEquals("G", result[1].chord)
        assertEquals("C", result[2].chord)
    }

    @Test
    fun `GIVEN chords text WHEN chords between parenthesis inside parenthesis THEN chords identified correctly`() {
        val result = Khord.find("(  C#7M   Gm7(5b)   C7   Fm7  )")
        assertEquals("C#7M", result[0].chord)
        assertEquals("Gm7(5b)", result[1].chord)
        assertEquals("C7", result[2].chord)
        assertEquals("Fm7", result[3].chord)
    }

    @Test
    fun `GIVEN complex chords WHEN simplifying THEN simple versions returned`() {
        val result = Khord.find(text = "(  C#7M   Gm7(5b)   C7   Fm7  )", simplify = true)
        assertEquals("C#, Gm7, C7, Fm7", result.joinToString { it.chord })
    }

    @Test
    fun `GIVEN complex chords WHEN not simplifying THEN complex versions returned`() {
        val result = Khord.find(text = "(  C#7M   Gm7(5b)   C7   Fm7  )")
        assertEquals("C#7M, Gm7(5b), C7, Fm7", result.joinToString { it.chord })
    }

    @Test
    fun `GIVEN complex chords WHEN simplifying THEN index is updated accordingly`() {
        val result = Khord.find(text = "C#7M   Gm7(5b)", simplify = true)
        assertEquals("(0, 2), (7, 10)", result.map { it.startIndex to it.endIndex }.joinToString())
    }

    @Test
    fun `GIVEN complex chords WHEN not simplifying THEN index is kept`() {
        val result = Khord.find(text = "C#7M   Gm7(5b)")
        assertEquals("(0, 4), (7, 14)", result.map { it.startIndex to it.endIndex }.joinToString())
    }

    @Test
    fun `GIVEN chords inside parenthesis WHEN finding chords THEN chords identified correctly`() {
        val result = Khord.find("(  A/B  )  (  C/D  )")
        assertEquals("A/B", result[0].chord)
        assertEquals("C/D", result[1].chord)
    }

    private val simplifyTestText = "Bm        Bm/A          Abm7(5-)      G\n" +
            "Há quanto tempo te esperava, Amado meu\n" +
            "Bm         Bm/A      Abm7(5-)      G\n" +
            "Há tantas noites vigiava, Esposo meu"

    @Test
    fun `GIVEN chords text WHEN simplifying THEN simple versions returned`() {
        val result = Khord.simplifyChordsInText(simplifyTestText)
        assertEquals(
            "Bm        Bm            Abm7          G\n" +
                    "Há quanto tempo te esperava, Amado meu\n" +
                    "Bm         Bm        Abm7          G\n" +
                    "Há tantas noites vigiava, Esposo meu",
            result,
        )
    }

    @Test
    fun `GIVEN simplifying WHEN searching chords THEN return correct chords`() {
        val result = Khord.simplifyChordsInText(testText).run { Khord.find(this) }
        assertEquals(
            "(8, 9), (21, 22), (50, 55), (67, 68), (97, 99), (114, 115)",
            result.map { it.startIndex to it.endIndex }.joinToString()
        )
        assertEquals(6, result.size)
    }

    @Test
    fun `GIVEN chord is complex WHEN simplifying THEN return Abm7`() {
        val result = Khord.simplifyChordsInText("Abm7(5-)")
        assertEquals("Abm7    ", result) // Abm7(5-) is 8 chars, Abm7 is 4 chars, so 4 spaces

        val result2 = Khord.simplifyChordsInText("D7(9-)")
        assertEquals("D7    ", result2) // D7(9-) is 6 chars, D7 is 2 chars, so 4 spaces
    }

    @Test
    fun `GIVEN a chord wrapped in parenthesis WHEN searching for chords THEN identify it as valid`() {
        val result = Khord.find("(C)")
        assertEquals(1, result.size)
        assertEquals("C", result.first().chord)
    }

    @Test
    fun `GIVEN a chord with maj, dim or aug extension WHEN searching for chords THEN identify it as valid`() {
        val chords = listOf("Cmaj7", "Cdim", "Caug")
        chords.forEach {
            val result = Khord.find(it)
            assertEquals(1, result.size, "Failed to find $it")
            assertEquals(it, result.first().chord)
        }
    }

    @Test
    fun `GIVEN a slash chord WHEN transposing THEN transpose both parts correctly`() {
        val result = Khord.transposeChord(Chord("C/E", 0, 3), ChordRoot.C, ChordRoot.D)
        assertEquals("D/F#", result)
    }

    @Test
    fun `GIVEN simplifying chords WHEN simplified chord is shorter THEN pad with correct number of spaces`() {
        // Cmaj7 (5 chars) simplified to C (1 char) -> 4 spaces added
        val result = Khord.simplifyChordsInText("Cmaj7 G")
        assertEquals("C     G", result)
    }

    @Test
    fun `GIVEN a text with escaped line breaks WHEN searching chords THEN identify them correctly`() {
        val text = "C\\nF"
        val result = Khord.find(text)
        assertEquals(2, result.size)
        assertEquals("C", result[0].chord)
        assertEquals("F", result[1].chord)
    }

    @Test
    fun `GIVEN newTone is null WHEN transposing text THEN return original text`() {
        val result = Khord.transposeText("C F G", ChordRoot.C, null)
        assertEquals("C F G", result)
    }

    @Test
    fun `GIVEN empty text WHEN searching for chords THEN return empty list`() {
        assertTrue(Khord.find("").isEmpty())
    }

    @Test
    fun `GIVEN text with no chords WHEN simplifying THEN return same text`() {
        val text = "Hello world, these are just lyrics"
        assertEquals(text, Khord.simplifyChordsInText(text))
    }

    @Test
    fun `GIVEN text with already simple chords WHEN simplifying THEN return same text`() {
        val text = "C  G  Am  F"
        assertEquals(text, Khord.simplifyChordsInText(text))
    }

    @Test
    fun `GIVEN multiline chord text WHEN finding chords THEN return correct chord values`() {
        val result = Khord.find(testText)
        assertEquals("C", result[0].chord)
        assertEquals("F", result[1].chord)
        assertEquals("D#m7-/G", result[2].chord)
        assertEquals("F", result[3].chord)
        assertEquals("Dm", result[4].chord)
        assertEquals("F", result[5].chord)
    }

    @Test
    fun `GIVEN multiline chord text WHEN finding chords THEN return correct chord indices`() {
        val result = Khord.find(testText)
        assertEquals(8, result[0].startIndex); assertEquals(9, result[0].endIndex)
        assertEquals(21, result[1].startIndex); assertEquals(22, result[1].endIndex)
        assertEquals(50, result[2].startIndex); assertEquals(57, result[2].endIndex)
        assertEquals(67, result[3].startIndex); assertEquals(68, result[3].endIndex)
        assertEquals(97, result[4].startIndex); assertEquals(99, result[4].endIndex)
        assertEquals(114, result[5].startIndex); assertEquals(115, result[5].endIndex)
    }

    @Test
    fun `GIVEN transposition changes chord length WHEN transposing text THEN alignment is preserved`() {
        val result = Khord.transposeText("Bb C", ChordRoot.Bb, ChordRoot.C)
        assertEquals("C  D", result)
    }

    @Test
    fun `GIVEN chord with degree symbol WHEN searching THEN identify as valid`() {
        val resultOrdinal = Khord.find("Cº")
        assertEquals(1, resultOrdinal.size)
        assertEquals("Cº", resultOrdinal.first().chord)

        val resultDegree = Khord.find("C°")
        assertEquals(1, resultDegree.size)
        assertEquals("C°", resultDegree.first().chord)
    }

    @Test
    fun `GIVEN a line where exactly half of words are chords WHEN searching THEN ignore the line`() {
        // 2 words, 1 chord (50% is NOT > 50%) -> no chord line
        val result = Khord.find("C text")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `GIVEN transposeChord with same original and new tone WHEN transposing THEN return original chord`() {
        val chord = Chord("Cmaj7", 0, 5)
        val result = Khord.transposeChord(chord, ChordRoot.C, ChordRoot.C)
        assertEquals("Cmaj7", result)
    }

    @Test
    fun `GIVEN chord with add9 extension WHEN searching THEN identify as valid`() {
        val result = Khord.find("Cadd9")
        assertEquals(1, result.size)
        assertEquals("Cadd9", result.first().chord)
    }

    @Test
    fun `GIVEN text with literal double quotes WHEN finding chords THEN quotes are stripped`() {
        val result = Khord.find("\"C F\"")
        assertEquals(2, result.size)
        assertEquals("C", result[0].chord)
        assertEquals("F", result[1].chord)
    }

    // Simplification: chain fixes (maj9/Δ9/7M(9) used to stop at an intermediate form)

    @Test
    fun `GIVEN maj9 chord WHEN simplifying THEN simplifies directly to root`() {
        // "Cmaj9" (5 chars) → "C    " (C + 4 spaces), not the intermediate "Cmaj7"
        assertEquals("C    ", Khord.simplifyChordsInText("Cmaj9"))
    }

    @Test
    fun `GIVEN delta9 chord WHEN simplifying THEN simplifies directly to root`() {
        // "CΔ9" (3 chars) → "C  " (C + 2 spaces), not the intermediate "Cmaj7"
        assertEquals("C  ", Khord.simplifyChordsInText("CΔ9"))
    }

    @Test
    fun `GIVEN 7M(9) chord WHEN simplifying THEN simplifies directly to root`() {
        // "C7M(9)" (6 chars) → "C     " (C + 5 spaces), not the intermediate "C7M"
        assertEquals("C     ", Khord.simplifyChordsInText("C7M(9)"))
    }

    // Simplification: degree sign ° (U+00B0) — distinct from ordinal indicator º (U+00BA)

    @Test
    fun `GIVEN diminished chord with degree sign WHEN simplifying THEN simplifies to minor`() {
        // "C°" (2 chars) → "Cm" (2 chars), same length so no padding
        assertEquals("Cm", Khord.simplifyChordsInText("C°"))
        // "C°7" (3 chars) → "Cm7" (3 chars), same length so no padding
        assertEquals("Cm7", Khord.simplifyChordsInText("C°7"))
    }

    // Simplification: new entries

    @Test
    fun `GIVEN sus chord without number WHEN simplifying THEN simplifies to root`() {
        // "Gsus" (4 chars) → "G   " (G + 3 spaces), keeping alignment with next chord
        assertEquals("G    G", Khord.simplifyChordsInText("Gsus G"))
    }

    @Test
    fun `GIVEN add2 or add4 chord WHEN simplifying THEN simplifies to root`() {
        // "Cadd2" (5 chars) → "C    " (C + 4 spaces)
        assertEquals("C    ", Khord.simplifyChordsInText("Cadd2"))
        assertEquals("C    ", Khord.simplifyChordsInText("Cadd4"))
    }

    @Test
    fun `GIVEN altered chord WHEN simplifying THEN alt suffix is removed`() {
        // "G7alt" (5 chars) → "G7   " (G7 + 3 spaces), keeping alignment with next chord
        assertEquals("G7    G", Khord.simplifyChordsInText("G7alt G"))
    }

    // Simplification: chord inversions (slash chords)

    @Test
    fun `GIVEN slash chord WHEN simplifying THEN inversion is stripped`() {
        // "C/E" (3 chars) → "C  " (C + 2 spaces)
        assertEquals("C  ", Khord.simplifyChordsInText("C/E"))
        // "Bm/A" (4 chars) → "Bm  " (Bm + 2 spaces)
        assertEquals("Bm  ", Khord.simplifyChordsInText("Bm/A"))
        // "D/F#" (4 chars) → "D   " (D + 3 spaces)
        assertEquals("D   ", Khord.simplifyChordsInText("D/F#"))
        // "G/B" (3 chars) → "G  " (G + 2 spaces)
        assertEquals("G  ", Khord.simplifyChordsInText("G/B"))
    }

    @Test
    fun `GIVEN complex slash chord WHEN simplifying THEN both inversion and complexity are removed`() {
        // "Cmaj7/E" (7 chars) → "C      " (C + 6 spaces): inversion stripped + maj7 removed
        assertEquals("C      ", Khord.simplifyChordsInText("Cmaj7/E"))
        // "Am7/G" (5 chars) → "Am7  " (Am7 + 2 spaces): inversion stripped, 7 is not complex
        assertEquals("Am7  ", Khord.simplifyChordsInText("Am7/G"))
    }

    @Test
    fun `GIVEN slash chord WHEN finding with simplify THEN inversion is stripped`() {
        val result = Khord.find("C/E  G/B", simplify = true)
        assertEquals("C", result[0].chord)
        assertEquals("G", result[1].chord)
    }

    @Test
    fun `GIVEN E major chord sheet WHEN transposing to G THEN chord columns are preserved`() {
        val text = "E9                B/D#\n" +
                    "   Eu tão simples,     tão pequeno\n" +
                    "C#m                   G#m7(13-)\n" +
                    "    Um carpinteiro e nada mais\n" +
                    "         A9                E/G#\n" +
                    "Mas meu Deus olhou pra mim"
        val result = Khord.transposeText(text, ChordRoot.E, ChordRoot.G)
        assertEquals("G9                D/F#\n" +
                    "   Eu tão simples,     tão pequeno\n" +
                    "Em                    Bm7(13-) \n" +
                    "    Um carpinteiro e nada mais\n" +
                    "         C9                G/B \n" +
                    "Mas meu Deus olhou pra mim",
            result
        )
    }

    @Test
    fun `GIVEN chord grows with enough space after it WHEN transposing THEN space is absorbed and next chord stays in column`() {
        val result = Khord.transposeText("A9   G", ChordRoot.A, ChordRoot.Bb)
        assertEquals("Bb9  G#", result)
    }

    @Test
    fun `GIVEN chord grows with only one space after it WHEN transposing THEN space is eaten and chords become adjacent`() {
        val result = Khord.transposeText("A9 G", ChordRoot.A, ChordRoot.Bb)
        assertEquals("Bb9G#", result)
    }

    @Test
    fun `GIVEN chord with parenthesized tension WHEN simplifying THEN tension group is stripped`() {
        // F#m7(11): (11) should be dropped, leaving F#m7
        val result = Khord.find("F#m7(11)", simplify = true)
        assertEquals("F#m7", result.first().chord)

        // B7(4/9): slash inside parens is not an inversion, (4/9) should be dropped, leaving B7
        val result2 = Khord.find("B7(4/9)", simplify = true)
        assertEquals("B7", result2.first().chord)
    }

    @Test
    fun `GIVEN chord with parenthesized tension WHEN not simplifying THEN chord is returned as-is`() {
        assertEquals("F#m7(11)", Khord.find("F#m7(11)").first().chord)
        assertEquals("B7(4/9)", Khord.find("B7(4/9)").first().chord)
    }
}
