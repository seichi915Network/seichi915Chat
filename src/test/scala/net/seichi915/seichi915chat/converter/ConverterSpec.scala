package net.seichi915.seichi915chat.converter

import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec

class ConverterSpec extends AnyFlatSpec with Diagrams {
  "Converter.convert" should "Convert with Google IME" in {
    assert(Converter.convert("aiueo") === "あいうえお")
    assert(Converter.convert("donarudo") === "ドナルド")
    assert(Converter.convert("tyatto") == "チャット")
    assert(Converter.convert("henkan") == "変換")
  }
}
