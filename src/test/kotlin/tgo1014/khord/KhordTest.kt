package tgo1014.khord

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tgo1014.khord.models.Chord
import tgo1014.khord.models.ChordRoot

class KhordTest {

    private val testText = "        C            F\n" +
            "Sem ressalva, sem escalas\n" +
            " D#m7-/G          F\n" +
            "Demorei mas fui\n" +
            "            Dm               F\n" +
            "E da janela vejo a esfera azul Uh Uh Uuuuh"

    @Test
    fun `GIVEN a text WHEN searching chord THEN return correct number of chords`() {
        assert(Khord.find(testText).size == 6)
    }

    @Test
    fun `GIVEN a invalid chord list WHEN searching for chord THEN return empty list`() {
        val invalidChordList = listOf("Car", "\\nBuscai", "2a.vez,", "À tua cruz", "imensidao/")
        invalidChordList.forEach {
            assert(Khord.find(it).isEmpty())
        }
    }

    @Test
    fun `GIVEN a valid chords list WHEN searching for chords THEN return valid chords`() {
        val validChordsList = listOf("D/F#", "G#4", "G7M", "C#m7M")
        validChordsList.forEach {
            assert(Khord.find(it).isNotEmpty())
        }
    }

    @Test
    fun `GIVEN a phrase without chords WHEN searching for chords THEN return empty list`() {
        val phasesList = listOf("Em nome de Cristo, que e a nossa paz!")
        phasesList.forEach {
            assert(Khord.find(it).isEmpty())
        }
    }

    @Test
    fun `WHEN transposing to the same root THEN return same chord`() {
        val result = Khord.transposeText("C", ChordRoot.C, ChordRoot.C)
        assert(result == "C")
    }

    @Test
    fun `GIVEN a transposition WHEN transposing THEN return correct chord`() {
        val result = Khord.transposeText("C", ChordRoot.C, ChordRoot.Db)
        assert(result == "C#")
        val result2 = Khord.transposeText("C", ChordRoot.C, ChordRoot.Bb)
        assert(result2 == "Bb")
    }

    @Test
    fun `GIVEN a text with chords and phrases WHEN transposing text THEN return same text with transposed notes`() {
        val result = Khord.transposeText("C\nCar", ChordRoot.C, ChordRoot.D)
        assert(result == "D\nCar")
    }

    @Test
    fun `GIVEN a text with just a chord WHEN transposing text THEN return transposed chord`() {
        val result = Khord.transposeText("G", ChordRoot.G, ChordRoot.C)
        assert(result == "C")
    }

    @Test
    fun `GIVEN a chord diminished chord WHEN transposing chord THEN return correct chord`() {
        val result = Khord.transposeChord(Khord.find("F#º").first(), ChordRoot.G, ChordRoot.Ab)
        assertEquals("Gº", result)
        val result2 = Khord.transposeChord(Khord.find("F#º").first(), ChordRoot.G, ChordRoot.A)
        assertEquals("G#º", result2)
    }

    @Test
    fun `GIVEN a text with chords, text and breaklines WHEN transposing text THEN return correct chords`() {
        val result = Khord.transposeText(
            "       Em           C           Am7        F#º\nVim buscar e vim salvar o que estava já perdido",
            ChordRoot.C,
            ChordRoot.D
        )
        assertEquals(
            "       F#m           D           Bm7        G#º\nVim buscar e vim salvar o que estava já perdido",
            result
        )
    }

    @Test
    fun `GIVEN chords with parenthesis WHEN searching chords THEN return proper chords`() {
        val result = Khord.find("(C F# G)")
        assertEquals("C", result[0].chord )
        assertEquals("F#", result[1].chord )
        assertEquals("G", result[2].chord )
    }

    @Test
    fun `GIVEN a chord with reversed root WHEN transposing THEN transpose also the root`() {
        val result = Khord.transposeChord(Khord.find("C/D").first(), ChordRoot.C, ChordRoot.D)
        assertEquals("D/E", result)
        val result2 = Khord.transposeChord(Khord.find("C/D").first(), ChordRoot.C, ChordRoot.Eb)
        assertEquals("D#/F", result2)
    }

    @Test
    fun `GIVEN a ChatGPT (heh!) generated chord list WHEN searching chords THEN all are valid`() {
        val chordList =
            "C, G, Am, F, Dm, Em, A, E, D, Bb, Gm, B, C7, Fmaj7, G7, Am7, D7, B7, E7, Cmaj7, Fm, Ab, Gmaj7, Cm, Eb, A7, Dm7, Gm7, C#m, F#m, Bm, E6, Cdim, G#dim, D#dim, Adim, C#7, F#7, B7b9, G#7b9, D#7b9, A7b9, D#7, G#7, C#m7, F#m7, Bm7, Em7, A7#5, D#7#5, G#7#5, C#7#5, D7#9, G7#9, A7#9, C7#9, Dm6, Am6, G6, C6, Bb6, F6, D7b5, G7b5, A7b5, C7b5, Dm9, Am9, G9, C9, Bb9, F9, Emaj7, Bb7, G7b9, A7b13, C13, D13, G13, E13, A13, B13, F#13, C#13, F13, Bb13, Abmaj7, Dbmaj7, Gbmaj7, Gbm7, Db7, Ab7, Gb7, Bbmaj7, Ebmaj7, F#7, C#m9, F#m9, Bm9, Em9, Amaj7#5, D7alt, G7alt, C7alt, F7alt"
        val split = chordList.split(",")
        val valids = split.filter {
            Khord.find(chordList).isNotEmpty()
        }
        assert(valids.size == split.size)
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
        assert(result.isEmpty())
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

}