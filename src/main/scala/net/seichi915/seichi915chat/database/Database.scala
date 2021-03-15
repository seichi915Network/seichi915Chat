package net.seichi915.seichi915chat.database

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.playerdata.PlayerData
import net.seichi915.seichi915chat.util.Implicits._
import scalikejdbc._

import java.io.{File, FileOutputStream}
import java.util.UUID

object Database {
  Class.forName("org.sqlite.JDBC")

  private val dbName =
    Seichi915Chat.instance.getDescription.getName.toLowerCase

  ConnectionPool.add(
    dbName,
    s"jdbc:sqlite:${Seichi915Chat.instance.getDataFolder.getAbsolutePath}/database.db",
    "",
    "")

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = false
  )

  def saveDefaultDatabase: Boolean =
    try {
      if (!Seichi915Chat.instance.getDataFolder.exists())
        Seichi915Chat.instance.getDataFolder.mkdir()
      val databaseFile =
        new File(Seichi915Chat.instance.getDataFolder, "database.db")
      if (!databaseFile.exists()) {
        val inputStream =
          Seichi915Chat.instance.getResourceAsStream("database.db")
        val outputStream = new FileOutputStream(databaseFile)
        val bytes = new Array[Byte](1024)
        var read = 0
        while ({
          read = inputStream.read(bytes)
          read
        } != -1) outputStream.write(bytes, 0, read)
        inputStream.close()
        outputStream.close()
      }
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }

  def getPlayerData(proxiedPlayer: ProxiedPlayer): Option[PlayerData] =
    NamedDB(dbName) localTx { implicit session =>
      sql"SELECT japanese_conversion_enabled, blocking_uuid_list FROM playerdata WHERE uuid = ${proxiedPlayer.getUniqueId}"
        .map(resultSet =>
          PlayerData(
            resultSet.boolean("japanese_conversion_enabled"),
            if (resultSet.string("blocking_uuid_list").isEmpty) Set()
            else
              resultSet
                .string("blocking_uuid_list")
                .split(",")
                .map(UUID.fromString)
                .toSet
        ))
        .list()
        .apply()
        .headOption
    }

  def createNewPlayerData(proxiedPlayer: ProxiedPlayer): Unit =
    NamedDB(dbName) localTx { implicit session =>
      sql"INSERT INTO playerdata (uuid, japanese_conversion_enabled, blocking_uuid_list) VALUES (${proxiedPlayer.getUniqueId}, 1, '')"
        .update()
        .apply()
    }

  def savePlayerData(proxiedPlayer: ProxiedPlayer,
                     playerData: PlayerData): Unit =
    NamedDB(dbName) localTx { implicit session =>
      sql"UPDATE playerdata SET japanese_conversion_enabled=${playerData.isJapaneseConversionEnabled.toInt}, blocking_uuid_list=${if (playerData.getBlockingUUIDList.isEmpty) ""
      else playerData.getBlockingUUIDList.mkString(",")} WHERE uuid = ${proxiedPlayer.getUniqueId}"
        .update()
        .apply()
    }

  def getFromDictionary(original: String): Option[String] =
    NamedDB(dbName) localTx { implicit session =>
      sql"SELECT converted FROM dictionary WHERE original = $original"
        .map(_.string("converted"))
        .list()
        .apply()
        .headOption
    }

  def addToDictionary(original: String, converted: String): Unit =
    NamedDB(dbName) localTx { implicit session =>
      sql"INSERT INTO dictionary (original, converted) VALUES ($original, $converted)"
        .update()
        .apply()
    }
}
