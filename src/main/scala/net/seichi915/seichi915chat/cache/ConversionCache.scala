package net.seichi915.seichi915chat.cache

import scala.collection.mutable

object ConversionCache {
  private var conversionCache = mutable.Map[String, String]()

  def add(original: String, converted: String): Unit =
    conversionCache.get(original) match {
      case Some(_) =>
      case None    => conversionCache += original -> converted
    }

  def remove(original: String): Unit =
    conversionCache.get(original) match {
      case Some(_) => conversionCache.remove(original)
      case None    =>
    }

  def get(original: String): Option[String] = conversionCache.get(original)
}
