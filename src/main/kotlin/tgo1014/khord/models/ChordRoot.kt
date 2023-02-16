package tgo1014.khord.models

public enum class ChordRoot(
    public val root: String,
    public val aliasList: List<String> = emptyList()
) {
    C("C"),
    Db("C#", listOf("Db")),
    D("D"),
    Eb("D#", listOf("Eb")),
    E("E"),
    F("F"),
    Gb("F#", listOf("Gb")),
    G("G"),
    Ab("G#", listOf("Ab")),
    A("A"),
    Bb("Bb", listOf("A#")),
    B("B");

    public val allAliasList: List<String> = listOf(root) + aliasList

    internal companion object {

        private val chordMap = mutableMapOf<String, ChordRoot>()
            .apply {
                values().forEach { chordRoot ->
                    chordRoot.allAliasList.forEach {
                        this[it] = chordRoot
                    }
                }
            }
            .toMap()

        val allChords = values()
            .flatMap { it.aliasList + it.root }
            .sortedByDescending { it.length }

        fun asCircularList() = CircularList(values().toList())

        fun from(chordString: String): ChordRoot {
            val two = chordString.take(2)
            val chord = chordMap[two]
            if (chord != null) {
                return chord
            }
            return chordMap[chordString.take(1)]!!
        }
    }

}