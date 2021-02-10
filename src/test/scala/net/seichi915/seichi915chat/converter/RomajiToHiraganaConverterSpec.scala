package net.seichi915.seichi915chat.converter

import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec

class RomajiToHiraganaConverterSpec extends AnyFlatSpec with Diagrams {
  "RomajiToHiraganaConverter.convert" should "Convert Romaji to Hiragana" in {
    assert(RomajiToHiraganaConverter.convert("aiueo") === "あいうえお")
    assert(RomajiToHiraganaConverter.convert("asfnhuqi3huod") === "あsfんふくぃ3ふおd")
    assert(RomajiToHiraganaConverter.convert("konnnitiha") === "こんにちは")
    assert(RomajiToHiraganaConverter.convert("ohayougozaimasu") === "おはようございます")
    assert(
      RomajiToHiraganaConverter.convert("ohayougozaimasen") === "おはようございません")
    assert(RomajiToHiraganaConverter.convert("asopasomaso") === "あそぱそまそ")
    assert(RomajiToHiraganaConverter.convert("giltuto") === "ぎっと")
    assert(RomajiToHiraganaConverter.convert("gitto") === "ぎっと")
    assert(RomajiToHiraganaConverter.convert("a-,[]") === "あー、「」")
    assert(RomajiToHiraganaConverter.convert("seichi") === "整地")
  }
}
