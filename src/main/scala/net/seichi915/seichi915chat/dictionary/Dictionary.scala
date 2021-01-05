package net.seichi915.seichi915chat.dictionary

import net.seichi915.seichi915chat.database.Database

object Dictionary {
  def add(original: String, converted: String): Unit =
    Database.addToDictionary(original, converted)

  def get(original: String): Option[String] =
    Database.getFromDictionary(original)
}
