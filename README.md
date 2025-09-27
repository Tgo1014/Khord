# Khord

[![](https://jitpack.io/v/Tgo1014/Khord.svg)](https://jitpack.io/#Tgo1014/Khord)
![](https://img.shields.io/github/languages/code-size/Tgo1014/Khord)
![](https://img.shields.io/badge/Kotlin-2.2.10-blueviolet)

A utility library to help handling lyrics with chords, allowing finding the indexes of chords and automatic transposition.

## Usage

### Finding chords

```kotlin
val lyric = "C            G#         G\n" +
            "Smoke on the water, and fire in the sky\n" +
            "C            G#   E G A  E G B A  E G A  G E  2x\n" +
            "Smoke on the water"
val chordsList = Khord.find(lyric)
println(chordsList.map { it.chord }.distinct()) // [C, G#, G, E, A, B]
```

### Finding chord info

```kotlin
val lyric = "C            G#\n" +
            "Smoke on the water\n"
val chordsList = Khord.find(lyric)
println(chordsList) // [Chord(chord=C, startIndex=0, endIndex=1), Chord(chord=G#, startIndex=13, endIndex=15)]
```

### Transposing chords

```kotlin
val lyric = "C            G#\n" +
            "Smoke on the water\n"
val chordsList = Khord.transposeText(lyric, ChordRoot.Bb, ChordRoot.C)
println(chordsList) 
// D            Bb
//Smoke on the water
```

## Adding to your porject

1 - Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
      ...
      maven("https://jitpack.io")
      ...
  }
}
```

2 - Add the dependency:
```gradle
dependencies {
  ...
  implementation "com.github.Tgo1014:Khord:${lastRelease}"
  ...
}
```
