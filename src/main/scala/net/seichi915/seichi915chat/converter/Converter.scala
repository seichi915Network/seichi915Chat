package net.seichi915.seichi915chat.converter

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL, URLEncoder}
import java.nio.charset.Charset

import com.google.common.io.CharStreams
import com.google.gson.{Gson, JsonArray}

object Converter {
  private val googleIMEURL: String =
    "https://www.google.com/transliterate?langpair=ja-Hira|ja&text="

  @SuppressWarnings(Array("UnstableApiUsage"))
  def convert(string: String): String = {
    val hiragana = RomajiToHiraganaConverter.convert(string)
    val url = new URL(
      s"$googleIMEURL${URLEncoder.encode(hiragana, Charset.forName("UTF-8"))}")
    val httpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
    httpURLConnection.setRequestMethod("GET")
    httpURLConnection.setInstanceFollowRedirects(false)
    httpURLConnection.connect()
    val bufferedReader = new BufferedReader(
      new InputStreamReader(httpURLConnection.getInputStream,
                            Charset.forName("UTF-8")))
    val json = CharStreams.toString(bufferedReader)
    val parsed = {
      val stringBuilder = new StringBuilder
      new Gson()
        .fromJson(json, classOf[JsonArray])
        .forEach(response =>
          stringBuilder.append(
            response.getAsJsonArray.get(1).getAsJsonArray.get(0).getAsString))
      stringBuilder.toString()
    }
    httpURLConnection.disconnect()
    bufferedReader.close()
    parsed
  }
}
