package net.seichi915.seichi915chat.converter

import net.seichi915.seichi915chat.cache.ConversionCache
import net.seichi915.seichi915chat.dictionary.Dictionary

object ChatConverter {
  def convert(original: String): String =
    ConversionCache.get(original) match {
      case Some(converted) => converted
      case None =>
        Dictionary.get(original) match {
          case Some(converted) =>
            ConversionCache.add(original, converted)
            converted
          case None =>
            val converted = Converter.convert(original)
            ConversionCache.add(original, converted)
            Dictionary.add(original, converted)
            converted
        }
    }
}
